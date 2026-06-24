package com.mandrecode.tempo.features.routines.presentation.components

import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.mandrecode.tempo.R
import com.mandrecode.tempo.core.domain.model.DayOfWeek
import com.mandrecode.tempo.core.ui.theme.TempoTheme
import com.mandrecode.tempo.features.routines.domain.model.Habit
import com.mandrecode.tempo.features.routines.domain.model.HabitChain
import com.mandrecode.tempo.features.routines.domain.model.HabitType
import com.mandrecode.tempo.features.routines.presentation.RoutinesContract
import com.mandrecode.tempo.features.routines.presentation.RoutinesContract.HabitSheetTab
import com.mandrecode.tempo.features.routines.presentation.RoutinesPreviewFixtures.PREVIEW_TODAY
import com.mandrecode.tempo.features.routines.presentation.RoutinesPreviewFixtures.chain
import com.mandrecode.tempo.features.routines.presentation.RoutinesPreviewFixtures.habit
import com.mandrecode.tempo.features.routines.presentation.RoutinesPreviewFixtures.quitHabit
import com.mandrecode.tempo.features.routines.presentation.RoutinesPreviewFixtures.sampleBuildHabits
import kotlinx.collections.immutable.toPersistentSet
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime

private fun buildFullWindowHistoryWithoutToday(): String = buildHistoryForDaysAgo(20 downTo 1)

private fun buildFullWindowHistoryWithToday(): String = buildHistoryForDaysAgo(20 downTo 0)

private fun buildHistoryForDaysAgo(daysAgoRange: IntProgression): String {
    val todayEpochDays = PREVIEW_TODAY.toEpochDays()
    return daysAgoRange.joinToString(",") { daysAgo ->
        LocalDate.fromEpochDays((todayEpochDays - daysAgo).toInt()).toString()
    }
}

@Composable
private fun PreviewSheet(
    formState: RoutinesContract.HabitFormState,
    habits: List<Habit> = sampleBuildHabits(),
    habitChains: List<HabitChain> = emptyList(),
    onToggleHabitCompletion: ((habitId: Long, isCompleted: Boolean) -> Unit)? = null,
) {
    TempoTheme {
        HabitBottomSheet(
            formState = formState,
            selectedDate = PREVIEW_TODAY,
            habits = habits,
            habitChains = habitChains,
            onSelectTab = {},
            onSetHabitType = {},
            onSetReminder = { _, _, _, _, _ -> },
            onClearReminder = {},
            onSetColorKey = {},
            onClearColor = {},
            onSetIcon = {},
            onClearIcon = {},
            onDismiss = {},
            onClearErrors = {},
            onConfirmHabit = { _, _ -> },
            onConfirmHabitChain = { _, _, _ -> },
            onSetRepeatDays = {},
            onToggleHabitCompletion = onToggleHabitCompletion,
        )
    }
}

// region New habit – defaults

@Preview(name = "Light", showBackground = true, device = "id:pixel_9")
@Preview(name = "Dark", showBackground = true, device = "id:pixel_9", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun HabitBottomSheetNewBuildHabitPreview() {
    PreviewSheet(formState = RoutinesContract.HabitFormState(isVisible = true))
}

@Preview(name = "Light", showBackground = true, device = "id:pixel_9")
@Preview(name = "Dark", showBackground = true, device = "id:pixel_9", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun HabitBottomSheetNewQuitHabitPreview() {
    PreviewSheet(
        formState =
            RoutinesContract.HabitFormState(
                isVisible = true,
                selectedHabitType = HabitType.QUIT,
            ),
    )
}

// endregion

// region Editing existing habit

@Preview(name = "Light", showBackground = true, device = "id:pixel_9")
@Preview(name = "Dark", showBackground = true, device = "id:pixel_9", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun HabitBottomSheetEditBuildHabitPreview() {
    val editingHabit =
        habit(
            id = 42L,
            title = "Morning Stretch",
            description = "10 minutes of full-body stretching",
            icon = "fitness",
            colorKey = "color_m3_green",
            reminderHour = 7,
            reminderMinute = 30,
        )

    PreviewSheet(
        formState =
            RoutinesContract.HabitFormState(
                isVisible = true,
                editingHabit = editingHabit,
                selectedColorKey = editingHabit.colorKey,
                selectedIcon = editingHabit.icon,
                reminderDate = editingHabit.reminderDate,
            ),
    )
}

@Preview(name = "Light", showBackground = true, device = "id:pixel_9")
@Preview(name = "Dark", showBackground = true, device = "id:pixel_9", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun HabitBottomSheetEditQuitHabitPreview() {
    val editingHabit =
        quitHabit(
            id = 99L,
            title = "No Smoking",
            description = "Stay clean",
            completionHistory = "2025-06-13,2025-06-14,2025-06-15",
        )

    PreviewSheet(
        formState =
            RoutinesContract.HabitFormState(
                isVisible = true,
                editingHabit = editingHabit,
                selectedHabitType = HabitType.QUIT,
                selectedColorKey = editingHabit.colorKey,
                selectedIcon = editingHabit.icon,
                reminderDate = editingHabit.reminderDate,
            ),
    )
}

@Preview(name = "Light", showBackground = true, device = "id:pixel_9")
@Preview(name = "Dark", showBackground = true, device = "id:pixel_9", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun HabitBottomSheetEditWithErrorPreview() {
    PreviewSheet(
        formState =
            RoutinesContract.HabitFormState(
                isVisible = true,
                titleError = R.string.task_title_required,
            ),
    )
}

@Preview(name = "Light", showBackground = true, device = "id:pixel_9")
@Preview(name = "Dark", showBackground = true, device = "id:pixel_9", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun HabitBottomSheetLongTitlePreview() {
    val editingHabit =
        habit(
            id = 7L,
            title = "Run for at least thirty minutes outside before breakfast",
            description = "Long-title preview to verify the title field keeps the full width.",
            icon = "run",
            colorKey = "color_m3_orange",
            reminderHour = 6,
        )

    PreviewSheet(
        formState =
            RoutinesContract.HabitFormState(
                isVisible = true,
                editingHabit = editingHabit,
                selectedColorKey = editingHabit.colorKey,
                selectedIcon = editingHabit.icon,
                reminderDate = editingHabit.reminderDate,
            ),
    )
}

@Preview(name = "Light – Edit Full Capacity Before Toggle", showBackground = true, device = "id:pixel_9")
@Preview(
    name = "Dark – Edit Full Capacity Before Toggle",
    showBackground = true,
    device = "id:pixel_9",
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Composable
private fun HabitBottomSheetEditFullCapacityBeforeTogglePreview() {
    val editingHabit =
        habit(
            id = 687L,
            title = "Habit history full row",
            description = "Regression preview for #687",
            icon = "fitness",
            colorKey = "color_m3_blue",
        ).copy(
            createdDate = LocalDateTime(2025, 5, 20, 8, 0),
            completionHistory = buildFullWindowHistoryWithoutToday(),
        )

    PreviewSheet(
        formState =
            RoutinesContract.HabitFormState(
                isVisible = true,
                editingHabit = editingHabit,
                selectedColorKey = editingHabit.colorKey,
                selectedIcon = editingHabit.icon,
                reminderDate = editingHabit.reminderDate,
            ),
        onToggleHabitCompletion = { _, _ -> },
    )
}

@Preview(name = "Light – Edit Full Capacity After Toggle", showBackground = true, device = "id:pixel_9")
@Preview(
    name = "Dark – Edit Full Capacity After Toggle",
    showBackground = true,
    device = "id:pixel_9",
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Composable
private fun HabitBottomSheetEditFullCapacityAfterTogglePreview() {
    val editingHabit =
        habit(
            id = 688L,
            title = "Habit history full row",
            description = "Regression preview for #687",
            icon = "fitness",
            colorKey = "color_m3_blue",
        ).copy(
            createdDate = LocalDateTime(2025, 5, 20, 8, 0),
            completionHistory = buildFullWindowHistoryWithToday(),
        )

    PreviewSheet(
        formState =
            RoutinesContract.HabitFormState(
                isVisible = true,
                editingHabit = editingHabit,
                selectedColorKey = editingHabit.colorKey,
                selectedIcon = editingHabit.icon,
                reminderDate = editingHabit.reminderDate,
            ),
        onToggleHabitCompletion = { _, _ -> },
    )
}

// endregion

// region Repeat days / reminder variants

@Preview(name = "Light", showBackground = true, device = "id:pixel_9")
@Preview(name = "Dark", showBackground = true, device = "id:pixel_9", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun HabitBottomSheetWithCustomDaysPreview() {
    val editingHabit =
        habit(
            id = 8L,
            title = "Workout",
            icon = "fitness",
            colorKey = "color_m3_red",
            reminderHour = 18,
            repeatDays = setOf(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY),
        )

    PreviewSheet(
        formState =
            RoutinesContract.HabitFormState(
                isVisible = true,
                editingHabit = editingHabit,
                selectedColorKey = editingHabit.colorKey,
                selectedIcon = editingHabit.icon,
                reminderDate = editingHabit.reminderDate,
                selectedRepeatDays = editingHabit.repeatDays?.toPersistentSet(),
            ),
    )
}

@Preview(name = "Light", showBackground = true, device = "id:pixel_9")
@Preview(name = "Dark", showBackground = true, device = "id:pixel_9", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun HabitBottomSheetWithReminderPreview() {
    PreviewSheet(
        formState =
            RoutinesContract.HabitFormState(
                isVisible = true,
                reminderDate = LocalDateTime(2025, 6, 15, 9, 30),
            ),
    )
}

// endregion

// region Habit chain tab

@Preview(name = "Light", showBackground = true, device = "id:pixel_9")
@Preview(name = "Dark", showBackground = true, device = "id:pixel_9", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun HabitBottomSheetNewChainPreview() {
    PreviewSheet(
        formState =
            RoutinesContract.HabitFormState(
                isVisible = true,
                selectedTab = HabitSheetTab.HABIT_CHAIN,
            ),
    )
}

@Preview(name = "Light", showBackground = true, device = "id:pixel_9")
@Preview(name = "Dark", showBackground = true, device = "id:pixel_9", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun HabitBottomSheetEditChainPreview() {
    val habits = sampleBuildHabits()
    val chainEntity: HabitChain =
        chain(
            id = 555L,
            title = "Morning Routine",
            description = "Kick off the day",
            colorKey = "color_m3_purple",
            icon = "spa",
            habitIds = listOf(habits[0].id, habits[1].id),
            reminderHour = 7,
        )

    PreviewSheet(
        formState =
            RoutinesContract.HabitFormState(
                isVisible = true,
                selectedTab = HabitSheetTab.HABIT_CHAIN,
                editingHabitChain = chainEntity,
                selectedColorKey = chainEntity.colorKey,
                selectedIcon = chainEntity.icon,
                reminderDate = chainEntity.periodicReminder,
            ),
        habits = habits,
        habitChains = listOf(chainEntity),
    )
}

// endregion
