package com.mandrecode.tempo.features.backup.data.mapper

import com.mandrecode.tempo.core.domain.model.DayOfWeek
import com.mandrecode.tempo.core.domain.model.MonthDayOption
import com.mandrecode.tempo.core.domain.model.Periodicity
import com.mandrecode.tempo.core.domain.model.Priority
import com.mandrecode.tempo.features.backup.data.model.BackupFileDto
import com.mandrecode.tempo.features.backup.data.model.CategoryBackupDto
import com.mandrecode.tempo.features.backup.data.model.ChainMemberBackupDto
import com.mandrecode.tempo.features.backup.data.model.HabitBackupDto
import com.mandrecode.tempo.features.backup.data.model.HabitChainBackupDto
import com.mandrecode.tempo.features.backup.data.model.TaskBackupDto
import com.mandrecode.tempo.features.backup.domain.model.BackupData
import com.mandrecode.tempo.features.backup.domain.model.ChainMembership
import com.mandrecode.tempo.features.routines.domain.model.Habit
import com.mandrecode.tempo.features.routines.domain.model.HabitChain
import com.mandrecode.tempo.features.routines.domain.model.HabitType
import com.mandrecode.tempo.features.tasks.domain.model.Category
import com.mandrecode.tempo.features.tasks.domain.model.Task
import kotlinx.datetime.LocalDateTime

/**
 * Maps between the stable backup DTOs and the domain [BackupData] snapshot.
 * Decoding throws [IllegalArgumentException] on unknown enum names or malformed
 * dates; the repository reports those as a corrupt file.
 */

fun BackupData.toDto(
    schemaVersion: Int,
    appVersion: String,
    exportedAt: LocalDateTime,
): BackupFileDto =
    BackupFileDto(
        schemaVersion = schemaVersion,
        appVersion = appVersion,
        exportedAt = exportedAt.toString(),
        categories = categories.map { it.toDto() },
        tasks = tasks.map { it.toDto() },
        habits = habits.map { it.toDto() },
        habitChains = habitChains.map { it.toDto() },
        habitChainMembers = chainMemberships.map { ChainMemberBackupDto(it.chainId, it.habitId, it.sortOrder) },
        settings = settings?.toDto(),
    )

fun BackupFileDto.toDomain(): BackupData =
    BackupData(
        categories = categories.map { it.toDomain() },
        tasks = tasks.map { it.toDomain() },
        habits = habits.map { it.toDomain() },
        habitChains = habitChains.map { it.toDomain() },
        chainMemberships = habitChainMembers.map { ChainMembership(it.chainId, it.habitId, it.sortOrder) },
        settings = settings?.toDomain(),
    )

private fun Category.toDto(): CategoryBackupDto =
    CategoryBackupDto(
        id = id,
        name = name,
        color = color,
        icon = icon,
        isDefault = isDefault,
        sortOrder = sortOrder,
    )

private fun CategoryBackupDto.toDomain(): Category =
    Category(
        id = id,
        name = name,
        color = color,
        icon = icon,
        isDefault = isDefault,
        sortOrder = sortOrder,
    )

private fun Task.toDto(): TaskBackupDto =
    TaskBackupDto(
        id = id,
        title = title,
        description = description,
        isCompleted = isCompleted,
        categoryId = categoryId,
        priority = priority?.name,
        reminderDate = reminderDate?.toString(),
        periodicity = periodicity?.name,
        periodicityInterval = periodicityInterval,
        repeatDays = repeatDays?.map { it.value },
        monthDayOption = monthDayOption?.name,
        parentTaskId = parentTaskId,
        sortOrder = sortOrder,
        completedAt = completedAt?.toString(),
        nextInstanceId = nextInstanceId,
    )

private fun TaskBackupDto.toDomain(): Task =
    Task(
        id = id,
        title = title,
        description = description,
        isCompleted = isCompleted,
        categoryId = categoryId,
        priority = priority?.let { enumValueOf<Priority>(it) },
        reminderDate = reminderDate?.let { LocalDateTime.parse(it) },
        periodicity = periodicity?.let { enumValueOf<Periodicity>(it) },
        periodicityInterval = periodicityInterval,
        repeatDays = repeatDays?.toDayOfWeekSet(),
        monthDayOption = monthDayOption?.let { enumValueOf<MonthDayOption>(it) },
        parentTaskId = parentTaskId,
        sortOrder = sortOrder,
        completedAt = completedAt?.let { LocalDateTime.parse(it) },
        nextInstanceId = nextInstanceId,
    )

private fun Habit.toDto(): HabitBackupDto =
    HabitBackupDto(
        id = id,
        title = title,
        description = description,
        icon = icon,
        colorKey = colorKey,
        reminderDate = reminderDate?.toString(),
        isCompleted = isCompleted,
        habitType = habitType.name,
        createdDate = createdDate.toString(),
        completionHistory = completionHistory,
        repeatDays = repeatDays?.map { it.value },
    )

private fun HabitBackupDto.toDomain(): Habit =
    Habit(
        id = id,
        title = title,
        description = description,
        icon = icon,
        colorKey = colorKey,
        reminderDate = reminderDate?.let { LocalDateTime.parse(it) },
        isCompleted = isCompleted,
        habitType = enumValueOf<HabitType>(habitType),
        createdDate = LocalDateTime.parse(createdDate),
        completionHistory = completionHistory,
        repeatDays = repeatDays?.toDayOfWeekSet(),
    )

private fun HabitChain.toDto(): HabitChainBackupDto =
    HabitChainBackupDto(
        id = id,
        title = title,
        description = description,
        colorKey = colorKey,
        icon = icon,
        periodicReminder = periodicReminder?.toString(),
        createdDate = createdDate.toString(),
        completionHistory = completionHistory,
        repeatDays = repeatDays?.map { it.value },
    )

private fun HabitChainBackupDto.toDomain(): HabitChain =
    HabitChain(
        id = id,
        title = title,
        description = description,
        colorKey = colorKey,
        icon = icon,
        periodicReminder = periodicReminder?.let { LocalDateTime.parse(it) },
        createdDate = LocalDateTime.parse(createdDate),
        completionHistory = completionHistory,
        repeatDays = repeatDays?.toDayOfWeekSet(),
    )

private fun List<Int>.toDayOfWeekSet(): Set<DayOfWeek> =
    mapTo(mutableSetOf()) { value ->
        requireNotNull(DayOfWeek.fromValue(value)) { "Unknown day of week: $value" }
    }
