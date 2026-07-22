package com.mandrecode.tempo.features.routines.domain.usecase

import com.mandrecode.tempo.core.domain.model.ScheduleResult
import com.mandrecode.tempo.core.domain.util.TitleDescriptionValidationResult
import com.mandrecode.tempo.core.domain.util.ValidationUtils
import com.mandrecode.tempo.features.routines.domain.model.Habit
import com.mandrecode.tempo.features.routines.domain.model.HabitType
import com.mandrecode.tempo.features.routines.domain.repository.HabitRepository
import com.mandrecode.tempo.features.routines.domain.scheduler.HabitReminderScheduler
import com.mandrecode.tempo.features.routines.domain.util.HabitReminderDateUtil
import jakarta.inject.Inject
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock

class CreateHabitUseCase
    @Inject
    constructor(
        private val habitRepository: HabitRepository,
        private val habitReminderScheduler: HabitReminderScheduler,
        private val clock: Clock,
    ) {
        sealed class Result {
            data class Success(
                val habitId: Long,
                val scheduleResult: ScheduleResult,
                val reminderAdvanced: Boolean = false,
            ) : Result()

            data class ValidationError(
                val type: ValidationErrorType,
            ) : Result()
        }

        enum class ValidationErrorType { TITLE_EMPTY, TITLE_TOO_LONG, DESCRIPTION_TOO_LONG }

        suspend operator fun invoke(habit: Habit): Result {
            val trimmedHabit = habit.copy(title = habit.title.trim(), description = habit.description.trim())
            when (ValidationUtils.validateTitleAndDescription(trimmedHabit.title, trimmedHabit.description)) {
                TitleDescriptionValidationResult.TitleEmpty ->
                    return Result.ValidationError(ValidationErrorType.TITLE_EMPTY)
                TitleDescriptionValidationResult.TitleTooLong ->
                    return Result.ValidationError(ValidationErrorType.TITLE_TOO_LONG)
                TitleDescriptionValidationResult.DescriptionTooLong ->
                    return Result.ValidationError(ValidationErrorType.DESCRIPTION_TOO_LONG)
                TitleDescriptionValidationResult.Valid -> {}
            }

            // Quit habits are daily by definition — coerce repeatDays = null at the domain
            // boundary so callers can't violate the invariant. Also auto-set a default evening
            // reminder (next upcoming 21:00) for quit habits without one — using the next
            // upcoming occurrence avoids landing in the past, which would silently advance
            // the reminder and surface a misleading "reminder advanced" snackbar to the user.
            val now = clock.now().toLocalDateTime(TimeZone.currentSystemDefault())
            val habitWithDefaults =
                if (trimmedHabit.habitType == HabitType.QUIT) {
                    val quitReminder =
                        trimmedHabit.reminderDate
                            ?: HabitReminderDateUtil.nextUpcomingTime(
                                HabitReminderDateUtil.QUIT_DEFAULT_REMINDER_HOUR,
                                0,
                                now,
                            )
                    trimmedHabit.copy(
                        repeatDays = null,
                        reminderDate = quitReminder,
                    )
                } else {
                    trimmedHabit
                }

            val advancedReminder =
                HabitReminderDateUtil.advanceReminderIfNeeded(
                    habitWithDefaults.reminderDate,
                    habitWithDefaults.repeatDays,
                    now,
                )
            val adjustedHabit = habitWithDefaults.copy(reminderDate = advancedReminder)
            val reminderAdvanced = advancedReminder != habitWithDefaults.reminderDate
            val habitId = habitRepository.insertHabit(adjustedHabit)
            val habitWithId = adjustedHabit.copy(id = habitId)
            val scheduleResult =
                if (habitWithId.reminderDate != null) {
                    habitReminderScheduler.scheduleHabit(habitWithId)
                } else {
                    ScheduleResult.Skipped
                }
            return Result.Success(habitId, scheduleResult, reminderAdvanced)
        }
    }
