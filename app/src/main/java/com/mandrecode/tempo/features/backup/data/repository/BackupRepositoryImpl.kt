package com.mandrecode.tempo.features.backup.data.repository

import android.content.Context
import androidx.room.withTransaction
import com.mandrecode.tempo.BuildConfig
import com.mandrecode.tempo.R
import com.mandrecode.tempo.core.data.entity.CategoryEntity
import com.mandrecode.tempo.core.data.entity.HabitChainEntity
import com.mandrecode.tempo.core.data.entity.HabitChainMemberEntity
import com.mandrecode.tempo.core.data.local.TempoDatabase
import com.mandrecode.tempo.features.backup.data.BackupSettingsDataSource
import com.mandrecode.tempo.features.backup.data.mapper.toDomain
import com.mandrecode.tempo.features.backup.data.mapper.toDto
import com.mandrecode.tempo.features.backup.data.model.BackupEnvelopeDto
import com.mandrecode.tempo.features.backup.data.model.BackupFileDto
import com.mandrecode.tempo.features.backup.domain.model.BackupData
import com.mandrecode.tempo.features.backup.domain.model.ChainMembership
import com.mandrecode.tempo.features.backup.domain.model.ImportCounts
import com.mandrecode.tempo.features.backup.domain.model.ImportMode
import com.mandrecode.tempo.features.backup.domain.model.ImportOutcome
import com.mandrecode.tempo.features.backup.domain.model.ImportSummary
import com.mandrecode.tempo.features.backup.domain.model.MergePlan
import com.mandrecode.tempo.features.backup.domain.repository.BackupRepository
import com.mandrecode.tempo.features.backup.domain.util.BackupPayloadValidator
import com.mandrecode.tempo.features.backup.domain.util.MergePlanner
import com.mandrecode.tempo.features.routines.data.mapper.toDomain
import com.mandrecode.tempo.features.routines.data.mapper.toEntity
import com.mandrecode.tempo.features.routines.domain.model.HabitChain
import com.mandrecode.tempo.features.tasks.data.mapper.toDomain
import com.mandrecode.tempo.features.tasks.data.mapper.toEntity
import com.mandrecode.tempo.features.tasks.domain.model.Category
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.SerializationException
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject
import kotlin.time.Clock

/** Highest backup schema version this app can read and the version it writes. */
const val BACKUP_SCHEMA_VERSION = 1

class BackupRepositoryImpl
    @Inject
    constructor(
        private val database: TempoDatabase,
        private val validator: BackupPayloadValidator,
        private val mergePlanner: MergePlanner,
        private val settingsDataSource: BackupSettingsDataSource,
        @param:ApplicationContext private val context: Context,
    ) : BackupRepository {
        private val jsonFormat =
            Json {
                ignoreUnknownKeys = true
                encodeDefaults = true
            }

        override suspend fun exportToJson(): String {
            val data =
                database
                    .withTransaction { readAll() }
                    .copy(settings = settingsDataSource.snapshot())
            val exportedAt = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
            return jsonFormat.encodeToString(
                data.toDto(
                    schemaVersion = BACKUP_SCHEMA_VERSION,
                    appVersion = BuildConfig.VERSION_NAME,
                    exportedAt = exportedAt,
                ),
            )
        }

        override suspend fun importFromJson(
            json: String,
            mode: ImportMode,
        ): ImportOutcome {
            val fileVersion = decodeSchemaVersion(json)
            return when {
                fileVersion == null -> ImportOutcome.CorruptFile
                fileVersion > BACKUP_SCHEMA_VERSION ->
                    ImportOutcome.UnsupportedVersion(
                        fileVersion = fileVersion,
                        maxSupported = BACKUP_SCHEMA_VERSION,
                    )

                else -> validateAndApply(json, mode)
            }
        }

        private fun decodeSchemaVersion(json: String): Int? =
            try {
                jsonFormat.decodeFromString<BackupEnvelopeDto>(json).schemaVersion
            } catch (_: SerializationException) {
                null
            }

        private fun decodeData(json: String): BackupData? =
            try {
                jsonFormat.decodeFromString<BackupFileDto>(json).toDomain()
            } catch (_: SerializationException) {
                null
            } catch (_: IllegalArgumentException) {
                null
            }

        private suspend fun validateAndApply(
            json: String,
            mode: ImportMode,
        ): ImportOutcome {
            val data = decodeData(json) ?: return ImportOutcome.CorruptFile
            return when (val result = validator.validate(data)) {
                is BackupPayloadValidator.Result.Invalid ->
                    ImportOutcome.ValidationFailed(result.issues)

                is BackupPayloadValidator.Result.Valid ->
                    when (mode) {
                        ImportMode.REPLACE -> ImportOutcome.Success(replace(result.sanitized))
                        ImportMode.MERGE -> ImportOutcome.Success(merge(result.sanitized))
                    }
            }
        }

        private suspend fun readAll(): BackupData =
            BackupData(
                categories = database.categoryDao().getAllCategoriesSync().toDomain(),
                tasks = database.taskDao().getAllTasksSync().toDomain(),
                habits = database.habitDao().getAllHabitsSync().toDomain(),
                habitChains = database.habitChainDao().getAllHabitChainsSync().map { it.toDomainChain() },
                chainMemberships =
                    database.habitChainMemberDao().getAllMembersSync().map {
                        ChainMembership(chainId = it.chainId, habitId = it.habitId, sortOrder = it.sortOrder)
                    },
            )

        /**
         * Wipes all tables and restores the payload exactly, preserving original ids.
         * Settings (SharedPreferences-backed, not part of the Room transaction) are
         * applied only after the database restore commits.
         */
        private suspend fun replace(data: BackupData): ImportSummary {
            val summary = replaceDatabase(data)
            data.settings?.let(settingsDataSource::apply)
            return summary
        }

        private suspend fun replaceDatabase(data: BackupData): ImportSummary =
            database.withTransaction {
                val memberDao = database.habitChainMemberDao()
                memberDao.deleteAllMembers()
                database.habitChainDao().deleteAllHabitChains()
                database.habitDao().deleteAllHabits()
                database.taskDao().deleteAllTasks()
                database.categoryDao().deleteAllCategories()

                data.categories.forEach { database.categoryDao().insertCategory(it.toEntity()) }
                ensureDefaultCategory(data.categories)
                data.tasks.forEach { database.taskDao().insertTask(it.toEntity()) }
                data.habits.forEach { database.habitDao().insertHabit(it.toEntity()) }
                data.habitChains.forEach { database.habitChainDao().insertHabitChain(it.toEntity()) }
                memberDao.insertMembers(
                    data.chainMemberships.map {
                        HabitChainMemberEntity(chainId = it.chainId, habitId = it.habitId, sortOrder = it.sortOrder)
                    },
                )

                ImportSummary(
                    categories = ImportCounts(imported = data.categories.size),
                    tasks = ImportCounts(imported = data.tasks.size),
                    habits = ImportCounts(imported = data.habits.size),
                    habitChains = ImportCounts(imported = data.habitChains.size),
                )
            }

        /**
         * Restores the app invariant that a default category always exists when the
         * payload does not carry one (pre-existing exports or edited files).
         */
        private suspend fun ensureDefaultCategory(categories: List<Category>) {
            if (categories.any { it.isDefault }) return
            val inboxId = -1L
            if (categories.any { it.id == inboxId }) {
                database.categoryDao().setDefault(inboxId)
            } else {
                database.categoryDao().insertCategory(
                    CategoryEntity(
                        id = inboxId,
                        name = context.getString(R.string.category_inbox),
                        icon = "inbox",
                        isDefault = true,
                        sortOrder = -1,
                    ),
                )
            }
        }

        /** Applies a [MergePlan]: inserts new records with fresh ids, remapping references. */
        private suspend fun merge(data: BackupData): ImportSummary =
            database.withTransaction {
                val plan = mergePlanner.plan(incoming = data, local = readAll())
                val categoryIds = plan.categoryIdMap.toMutableMap()
                plan.categoriesToInsert.forEach { category ->
                    categoryIds[category.id] =
                        database.categoryDao().insertCategory(category.copy(id = 0).toEntity())
                }
                val taskIds = insertMergedTasks(plan, categoryIds)
                val habitIds = plan.habitIdMap.toMutableMap()
                plan.habitsToInsert.forEach { habit ->
                    habitIds[habit.id] = database.habitDao().insertHabit(habit.copy(id = 0).toEntity())
                }
                val chainIds = plan.chainIdMap.toMutableMap()
                plan.chainsToInsert.forEach { chain ->
                    chainIds[chain.id] =
                        database.habitChainDao().insertHabitChain(chain.copy(id = 0).toEntity())
                }
                database.habitChainMemberDao().insertMembers(
                    plan.membershipsToInsert.map { membership ->
                        HabitChainMemberEntity(
                            chainId = chainIds.getValue(membership.chainId),
                            habitId = habitIds.getValue(membership.habitId),
                            sortOrder = membership.sortOrder,
                        )
                    },
                )
                plan.summary
            }

        private suspend fun insertMergedTasks(
            plan: MergePlan,
            categoryIds: Map<Long, Long>,
        ): Map<Long, Long> {
            val taskIds = plan.taskIdMap.toMutableMap()
            plan.tasksToInsert.forEach { task ->
                taskIds[task.id] =
                    database.taskDao().insertTask(
                        task
                            .copy(
                                id = 0,
                                categoryId = categoryIds.getValue(task.categoryId),
                                parentTaskId = task.parentTaskId?.let(taskIds::getValue),
                                nextInstanceId = null,
                            ).toEntity(),
                    )
            }
            // Second pass: next-instance links may point at tasks inserted later.
            plan.tasksToInsert
                .filter { it.nextInstanceId != null }
                .forEach { task ->
                    taskIds[task.nextInstanceId]?.let { target ->
                        database.taskDao().updateTaskNextInstanceId(taskIds.getValue(task.id), target)
                    }
                }
            return taskIds
        }
    }

private fun HabitChainEntity.toDomainChain(): HabitChain =
    HabitChain(
        id = id,
        title = title,
        description = description,
        colorKey = colorKey,
        icon = icon,
        periodicReminder = periodicReminder,
        createdDate = createdDate,
        completionHistory = completionHistory,
        repeatDays = repeatDays,
    )
