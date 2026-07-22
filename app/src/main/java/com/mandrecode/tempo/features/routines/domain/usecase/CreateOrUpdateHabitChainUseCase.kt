package com.mandrecode.tempo.features.routines.domain.usecase

import androidx.annotation.StringRes
import com.mandrecode.tempo.R
import com.mandrecode.tempo.core.domain.model.DayOfWeek
import com.mandrecode.tempo.core.domain.model.ScheduleResult
import com.mandrecode.tempo.core.domain.util.TitleDescriptionValidationResult
import com.mandrecode.tempo.core.domain.util.ValidationResult
import com.mandrecode.tempo.core.domain.util.ValidationUtils
import com.mandrecode.tempo.features.routines.domain.model.HabitChain
import com.mandrecode.tempo.features.routines.domain.model.HabitType
import com.mandrecode.tempo.features.routines.domain.repository.HabitChainRepository
import com.mandrecode.tempo.features.routines.domain.repository.HabitRepository
import com.mandrecode.tempo.features.routines.domain.scheduler.HabitReminderScheduler
import com.mandrecode.tempo.features.routines.domain.util.HabitReminderDateUtil
import jakarta.inject.Inject
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

class CreateOrUpdateHabitChainUseCase
    @Inject
    constructor(
        private val habitRepository: HabitRepository,
        private val habitChainRepository: HabitChainRepository,
        private val habitReminderScheduler: HabitReminderScheduler,
    ) {
        data class Params(
            val title: String,
            val description: String,
            val habitIds: List<Long>,
            val colorKey: String?,
            val icon: String?,
            val reminderDate: LocalDateTime?,
            val repeatDays: Set<DayOfWeek>?,
            val editingHabitChain: HabitChain?,
        )

        sealed class Result {
            data class Success(
                @StringRes val messageResId: Int,
                val reminderAdvanced: Boolean = false,
            ) : Result()

            data class ValidationError(
                val type: ValidationErrorType,
            ) : Result()
        }

        enum class ValidationErrorType {
            TITLE_EMPTY,
            TITLE_TOO_LONG,
            DESCRIPTION_TOO_LONG,
            TOO_MANY_HABITS,
            QUIT_HABITS_NOT_ALLOWED,
        }

        @OptIn(ExperimentalTime::class)
        suspend operator fun invoke(params: Params): Result {
            val trimmedTitle = params.title.trim()
            val trimmedDescription = params.description.trim()
            when (ValidationUtils.validateTitleAndDescription(trimmedTitle, trimmedDescription)) {
                TitleDescriptionValidationResult.TitleEmpty ->
                    return Result.ValidationError(ValidationErrorType.TITLE_EMPTY)
                TitleDescriptionValidationResult.TitleTooLong ->
                    return Result.ValidationError(ValidationErrorType.TITLE_TOO_LONG)
                TitleDescriptionValidationResult.DescriptionTooLong ->
                    return Result.ValidationError(ValidationErrorType.DESCRIPTION_TOO_LONG)
                TitleDescriptionValidationResult.Valid -> {}
            }
            if (ValidationUtils.validateHabitChainSize(params.habitIds.size) is ValidationResult.TooManyItems) {
                return Result.ValidationError(ValidationErrorType.TOO_MANY_HABITS)
            }

            // Validate quit-habit invariant BEFORE any side effects so a violating call
            // never clears the user's existing habit reminders. Defense-in-depth: the UI
            // hides quit habits from the chain picker, but enforce the invariant at the
            // domain boundary so callers (tests, future entry points, the migration
            // purge) cannot violate it silently. Surfaced as a graceful ValidationError
            // rather than a thrown exception so any future caller that does reach this
            // branch shows a snackbar instead of crashing the app.
            val habits =
                if (params.habitIds.isNotEmpty()) {
                    habitRepository.getHabitsByIds(params.habitIds)
                } else {
                    emptyList()
                }
            if (habits.any { it.habitType == HabitType.QUIT }) {
                return Result.ValidationError(ValidationErrorType.QUIT_HABITS_NOT_ALLOWED)
            }

            // Clear individual habit reminders (now safe — invariant validated above)
            if (params.habitIds.isNotEmpty()) {
                habitRepository.clearRemindersForHabits(params.habitIds)
                habits.forEach { habit ->
                    habitReminderScheduler.cancelHabit(habit)
                }
            }

            // Sync color keys
            if (params.habitIds.isNotEmpty()) {
                habitRepository.updateHabitsColorKey(params.habitIds, params.colorKey)
            }

            val createdDate = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())

            if (params.editingHabitChain != null) {
                val updatedChain =
                    params.editingHabitChain.copy(
                        title = trimmedTitle,
                        description = trimmedDescription,
                        colorKey = params.colorKey,
                        icon = params.icon,
                        habitIds = params.habitIds,
                        periodicReminder = params.reminderDate,
                        repeatDays = params.repeatDays,
                    )
                val advancedReminder =
                    HabitReminderDateUtil.advanceReminderIfNeeded(
                        updatedChain.periodicReminder,
                        updatedChain.repeatDays,
                    )
                val adjustedChain = updatedChain.copy(periodicReminder = advancedReminder)
                val reminderAdvanced = advancedReminder != updatedChain.periodicReminder

                habitChainRepository.updateHabitChain(adjustedChain)
                habitReminderScheduler.cancelHabitChain(params.editingHabitChain)

                val scheduleResult =
                    if (adjustedChain.periodicReminder != null) {
                        habitReminderScheduler.scheduleHabitChain(adjustedChain)
                    } else {
                        ScheduleResult.Skipped
                    }

                val messageResId =
                    when {
                        scheduleResult is ScheduleResult.PermissionError -> R.string.msg_permission_needed
                        scheduleResult is ScheduleResult.Failure -> R.string.msg_habit_chain_update_failed_scheduling
                        reminderAdvanced -> R.string.msg_habit_chain_updated_reminder_advanced
                        else -> R.string.msg_habit_chain_updated_success
                    }
                return Result.Success(messageResId, reminderAdvanced)
            } else {
                val newChain =
                    HabitChain(
                        title = trimmedTitle,
                        description = trimmedDescription,
                        colorKey = params.colorKey,
                        icon = params.icon,
                        habitIds = params.habitIds,
                        periodicReminder = params.reminderDate,
                        repeatDays = params.repeatDays,
                        createdDate = createdDate,
                    )
                val advancedReminder =
                    HabitReminderDateUtil.advanceReminderIfNeeded(
                        newChain.periodicReminder,
                        newChain.repeatDays,
                    )
                val adjustedChain = newChain.copy(periodicReminder = advancedReminder)
                val reminderAdvanced = advancedReminder != newChain.periodicReminder

                val chainId = habitChainRepository.insertHabitChain(adjustedChain)
                val chainWithId = adjustedChain.copy(id = chainId)

                val scheduleResult =
                    if (chainWithId.periodicReminder != null) {
                        habitReminderScheduler.scheduleHabitChain(chainWithId)
                    } else {
                        ScheduleResult.Skipped
                    }

                val messageResId =
                    when {
                        scheduleResult is ScheduleResult.PermissionError -> R.string.msg_permission_needed
                        scheduleResult is ScheduleResult.Failure -> R.string.msg_habit_chain_create_failed_scheduling
                        reminderAdvanced -> R.string.msg_habit_chain_created_reminder_advanced
                        else -> R.string.msg_habit_chain_created_success
                    }
                return Result.Success(messageResId, reminderAdvanced)
            }
        }
    }
