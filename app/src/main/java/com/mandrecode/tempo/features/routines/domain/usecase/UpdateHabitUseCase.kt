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

class UpdateHabitUseCase
    @Inject
    constructor(
        private val habitRepository: HabitRepository,
        private val habitReminderScheduler: HabitReminderScheduler,
        private val clock: Clock,
    ) {
        sealed class Result {
            data class Success(
                val scheduleResult: ScheduleResult,
                val reminderAdvanced: Boolean = false,
            ) : Result()

            data class ValidationError(
                val type: CreateHabitUseCase.ValidationErrorType,
            ) : Result()
        }

        suspend operator fun invoke(habit: Habit): Result {
            val trimmedHabit = habit.copy(title = habit.title.trim(), description = habit.description.trim())
            when (ValidationUtils.validateTitleAndDescription(trimmedHabit.title, trimmedHabit.description)) {
                TitleDescriptionValidationResult.TitleEmpty ->
                    return Result.ValidationError(CreateHabitUseCase.ValidationErrorType.TITLE_EMPTY)
                TitleDescriptionValidationResult.TitleTooLong ->
                    return Result.ValidationError(CreateHabitUseCase.ValidationErrorType.TITLE_TOO_LONG)
                TitleDescriptionValidationResult.DescriptionTooLong ->
                    return Result.ValidationError(CreateHabitUseCase.ValidationErrorType.DESCRIPTION_TOO_LONG)
                TitleDescriptionValidationResult.Valid -> {}
            }

            // Quit habits are daily by definition — coerce repeatDays = null at the domain
            // boundary so callers can't violate the invariant. Also auto-set a default evening
            // reminder (next upcoming 21:00) for quit habits without one — mirrors
            // CreateHabitUseCase so the rule holds across both creation and edit paths.
            val now = clock.now().toLocalDateTime(TimeZone.currentSystemDefault())
            val coercedHabit =
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
                    coercedHabit.reminderDate,
                    coercedHabit.repeatDays,
                    now,
                )
            val adjustedHabit = coercedHabit.copy(reminderDate = advancedReminder)
            val reminderAdvanced = advancedReminder != coercedHabit.reminderDate

            habitRepository.updateHabit(adjustedHabit)
            habitReminderScheduler.cancelHabit(adjustedHabit)
            val scheduleResult =
                if (adjustedHabit.reminderDate != null) {
                    habitReminderScheduler.scheduleHabit(adjustedHabit)
                } else {
                    ScheduleResult.Skipped
                }
            return Result.Success(scheduleResult, reminderAdvanced)
        }
    }
