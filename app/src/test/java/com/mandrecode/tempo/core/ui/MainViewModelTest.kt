package com.mandrecode.tempo.core.ui

import androidx.lifecycle.SavedStateHandle
import com.google.common.truth.Truth.assertThat
import com.mandrecode.tempo.core.data.preferences.NavigationPreferencesRepository
import com.mandrecode.tempo.core.data.preferences.ThemePreferencesRepository
import com.mandrecode.tempo.core.domain.model.ThemeMode
import com.mandrecode.tempo.core.ui.model.MainUiState
import com.mandrecode.tempo.core.ui.navigation.PendingNotificationAction
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import org.junit.After
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MainViewModelTest {
    private lateinit var navigationPreferencesRepository: NavigationPreferencesRepository
    private lateinit var themePreferencesRepository: ThemePreferencesRepository
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        navigationPreferencesRepository = mockk()
        themePreferencesRepository = mockk()

        every { themePreferencesRepository.getThemeMode() } returns flowOf(ThemeMode.SYSTEM)
        every { themePreferencesRepository.getUseTempoColors() } returns flowOf(false)
        every { navigationPreferencesRepository.getDefaultTab() } returns flowOf("routines")
        every { navigationPreferencesRepository.isRoutinesTabEnabled() } returns flowOf(true)
        every { navigationPreferencesRepository.isTasksTabEnabled() } returns flowOf(true)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state is Loading`() =
        runTest {
            val viewModel = createViewModel()

            assertThat(viewModel.uiState.value).isEqualTo(MainUiState.Loading)
        }

    @Test
    fun `emits Success with combined preferences`() =
        runTest {
            val viewModel = createViewModel()

            val values = mutableListOf<MainUiState>()
            val job =
                backgroundScope.launch(testDispatcher) {
                    viewModel.uiState.collect { values.add(it) }
                }

            advanceUntilIdle()

            val success = values.last()
            assertThat(success).isInstanceOf(MainUiState.Success::class.java)
            success as MainUiState.Success
            assertThat(success.themeMode).isEqualTo(ThemeMode.SYSTEM)
            assertThat(success.useTempoColors).isFalse()
            assertThat(success.defaultTab).isEqualTo("routines")
            assertThat(success.isRoutinesTabEnabled).isTrue()
            assertThat(success.isTasksTabEnabled).isTrue()

            job.cancel()
        }

    @Test
    fun `emits Success with dark theme`() =
        runTest {
            every { themePreferencesRepository.getThemeMode() } returns flowOf(ThemeMode.DARK)

            val viewModel = createViewModel()

            val values = mutableListOf<MainUiState>()
            val job =
                backgroundScope.launch(testDispatcher) {
                    viewModel.uiState.collect { values.add(it) }
                }

            advanceUntilIdle()

            val success = values.last() as MainUiState.Success
            assertThat(success.themeMode).isEqualTo(ThemeMode.DARK)

            job.cancel()
        }

    @Test
    fun `emits Success with disabled tabs`() =
        runTest {
            every { navigationPreferencesRepository.isRoutinesTabEnabled() } returns flowOf(false)
            every { navigationPreferencesRepository.isTasksTabEnabled() } returns flowOf(false)

            val viewModel = createViewModel()

            val values = mutableListOf<MainUiState>()
            val job =
                backgroundScope.launch(testDispatcher) {
                    viewModel.uiState.collect { values.add(it) }
                }

            advanceUntilIdle()

            val success = values.last() as MainUiState.Success
            assertThat(success.isRoutinesTabEnabled).isFalse()
            assertThat(success.isTasksTabEnabled).isFalse()

            job.cancel()
        }

    @Test
    fun `pending task notification action is saved and exposed`() =
        runTest {
            val viewModel = createViewModel()
            val originalReminderDate = LocalDateTime(2026, 5, 8, 9, 30)

            viewModel.setPendingNotificationAction(
                PendingNotificationAction.OpenTask(
                    taskId = 42L,
                    originalReminderDate = originalReminderDate,
                ),
            )

            assertThat(viewModel.pendingNotificationAction.value)
                .isEqualTo(PendingNotificationAction.OpenTask(42L, originalReminderDate))
        }

    @Test
    fun `pending notification action is restored from saved state`() =
        runTest {
            val savedStateHandle =
                SavedStateHandle(
                    mapOf(
                        "pending_notification_action_type" to "habit_chain",
                        "pending_notification_action_id" to 7L,
                    ),
                )

            val viewModel = createViewModel(savedStateHandle)

            assertThat(viewModel.pendingNotificationAction.value)
                .isEqualTo(PendingNotificationAction.OpenHabitChain(7L))
        }

    @Test
    fun `pending habit chain notification action saves scheduled date`() =
        runTest {
            val savedStateHandle = SavedStateHandle()
            val viewModel = createViewModel(savedStateHandle)
            val scheduledDate = LocalDate(2026, 5, 8)

            viewModel.setPendingNotificationAction(
                PendingNotificationAction.OpenHabitChain(
                    chainId = 7L,
                    scheduledDate = scheduledDate,
                ),
            )

            assertThat(viewModel.pendingNotificationAction.value)
                .isEqualTo(PendingNotificationAction.OpenHabitChain(7L, scheduledDate))
            assertThat(savedStateHandle.get<String>("pending_scheduled_date"))
                .isEqualTo("2026-05-08")
        }

    @Test
    fun `pending habit chain notification action restores scheduled date from saved state`() =
        runTest {
            val savedStateHandle =
                SavedStateHandle(
                    mapOf(
                        "pending_notification_action_type" to "habit_chain",
                        "pending_notification_action_id" to 7L,
                        "pending_scheduled_date" to "2026-05-08",
                    ),
                )

            val viewModel = createViewModel(savedStateHandle)

            assertThat(viewModel.pendingNotificationAction.value)
                .isEqualTo(PendingNotificationAction.OpenHabitChain(7L, LocalDate(2026, 5, 8)))
        }

    @Test
    fun `invalid pending habit chain scheduled date restores as null`() =
        runTest {
            val savedStateHandle =
                SavedStateHandle(
                    mapOf(
                        "pending_notification_action_type" to "habit_chain",
                        "pending_notification_action_id" to 7L,
                        "pending_scheduled_date" to "not-a-date",
                    ),
                )

            val viewModel = createViewModel(savedStateHandle)

            assertThat(viewModel.pendingNotificationAction.value)
                .isEqualTo(PendingNotificationAction.OpenHabitChain(7L))
        }

    @Test
    fun `consumePendingNotificationAction clears pending action`() =
        runTest {
            val viewModel = createViewModel()

            viewModel.setPendingNotificationAction(PendingNotificationAction.OpenHabit(5L))
            viewModel.consumePendingNotificationAction()

            assertThat(viewModel.pendingNotificationAction.value).isNull()
        }

    @Test
    fun `unknown pending notification action type is cleared from saved state`() =
        runTest {
            val savedStateHandle =
                SavedStateHandle(
                    mapOf(
                        "pending_notification_action_type" to "unknown",
                        "pending_notification_action_id" to 7L,
                        "pending_original_reminder_date" to "2026-05-08T09:30",
                        "pending_scheduled_date" to "2026-05-08",
                    ),
                )

            val viewModel = createViewModel(savedStateHandle)

            assertThat(viewModel.pendingNotificationAction.value).isNull()
            assertThat(savedStateHandle.get<String>("pending_notification_action_type")).isNull()
            assertThat(savedStateHandle.get<Long>("pending_notification_action_id")).isNull()
            assertThat(savedStateHandle.get<String>("pending_original_reminder_date")).isNull()
            assertThat(savedStateHandle.get<String>("pending_scheduled_date")).isNull()
        }

    @Test
    fun `incomplete pending notification action is cleared from saved state`() =
        runTest {
            val savedStateHandle =
                SavedStateHandle(
                    mapOf(
                        "pending_notification_action_type" to "task",
                    ),
                )

            val viewModel = createViewModel(savedStateHandle)

            assertThat(viewModel.pendingNotificationAction.value).isNull()
            assertThat(savedStateHandle.get<String>("pending_notification_action_type")).isNull()
        }

    private fun createViewModel(savedStateHandle: SavedStateHandle = SavedStateHandle()): MainViewModel =
        MainViewModel(
            savedStateHandle = savedStateHandle,
            navigationPreferencesRepository = navigationPreferencesRepository,
            themePreferencesRepository = themePreferencesRepository,
        )
}
