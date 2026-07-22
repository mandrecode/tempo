package com.mandrecode.tempo

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.NavKey
import com.mandrecode.tempo.core.data.preferences.NavigationPreferencesRepository
import com.mandrecode.tempo.core.data.preferences.ThemePreferencesRepository
import com.mandrecode.tempo.core.domain.model.ThemeMode
import com.mandrecode.tempo.core.ui.MainViewModel
import com.mandrecode.tempo.core.ui.model.MainUiState
import com.mandrecode.tempo.core.ui.navigation.OnboardingRoute
import com.mandrecode.tempo.core.ui.navigation.PendingNotificationAction
import com.mandrecode.tempo.core.ui.navigation.RoutinesRoute
import com.mandrecode.tempo.core.ui.navigation.TasksRoute
import com.mandrecode.tempo.core.ui.navigation.TempoNavHost
import com.mandrecode.tempo.core.ui.theme.TempoTheme
import com.mandrecode.tempo.features.whatsnew.presentation.components.WhatsNewBottomSheet
import com.mandrecode.tempo.features.widget.presentation.QuickAddTaskWidget
import com.mandrecode.tempo.infrastructure.reminders.ReminderRefreshScheduler
import com.mandrecode.tempo.infrastructure.reminders.receivers.HabitReminderReceiver
import com.mandrecode.tempo.infrastructure.reminders.receivers.MarkAsCompletedReceiver
import com.mandrecode.tempo.infrastructure.reminders.receivers.TaskReminderReceiver
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject
    lateinit var navigationPreferencesRepository: NavigationPreferencesRepository

    @Inject
    lateinit var themePreferencesRepository: ThemePreferencesRepository

    private val mainViewModel: MainViewModel by viewModels()

    // Triggers for navigation
    private val routinesNavigationTrigger = mutableLongStateOf(0L)
    private val tasksNavigationTrigger = mutableLongStateOf(0L)

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)

        // Establish edge-to-edge before first composition so the app draws behind the system bars
        // from the first frame. TempoTheme's SideEffect re-applies the theme-aware transparent
        // dark/light style on every recomposition.
        enableEdgeToEdge()

        setContent {
            val uiState by mainViewModel.uiState.collectAsStateWithLifecycle()
            val pendingNotificationAction by mainViewModel.pendingNotificationAction.collectAsStateWithLifecycle()

            when (val state = uiState) {
                is MainUiState.Loading -> {
                    // Keep splash screen or show a blank loading surface
                    // Since we use installSplashScreen(), the system splash stays until the first frame is drawn.
                    // If we render nothing or a Box, it might flash white.
                    // Ideally we use keepOnScreenCondition in installSplashScreen, but simpler here:
                    // Render a dummy surface matching the background.
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background,
                    ) {}
                }

                is MainUiState.Success -> {
                    val startDestination = rememberStartDestination(state)
                    var isOnboardingSectionActive by remember { mutableStateOf(false) }

                    TempoTheme(
                        darkTheme =
                            when (state.themeMode) {
                                ThemeMode.LIGHT -> false
                                ThemeMode.DARK -> true
                                ThemeMode.SYSTEM -> isSystemInDarkTheme()
                            },
                        useTempoColors = state.useTempoColors,
                    ) {
                        Surface(
                            modifier = Modifier.fillMaxSize(),
                            color = MaterialTheme.colorScheme.background,
                        ) {
                            TempoNavHost(
                                navigationPreferencesRepository = navigationPreferencesRepository,
                                routinesNavigationTrigger = routinesNavigationTrigger.longValue,
                                tasksNavigationTrigger = tasksNavigationTrigger.longValue,
                                pendingNotificationAction = pendingNotificationAction,
                                onConsumePendingNotificationAction = {
                                    mainViewModel.consumePendingNotificationAction()
                                    intent.clearNotificationExtras()
                                },
                                startDestination = startDestination,
                                onRouteChange = { routeName ->
                                    navigationPreferencesRepository.saveLastRoute(routeName)
                                },
                                onOnboardingActiveChange = { isOnboardingSectionActive = it },
                            )

                            val whatsNewEntry = state.whatsNewEntry
                            if (whatsNewEntry != null && !isOnboardingSectionActive) {
                                WhatsNewBottomSheet(
                                    entry = whatsNewEntry,
                                    onDismissRequest = mainViewModel::onWhatsNewDismissed,
                                )
                            }
                        }
                    }
                }
            }

            LaunchedEffect(Unit) {
                withFrameNanos { }
                ReminderRefreshScheduler.enqueuePeriodicRefresh(applicationContext)
            }
        }

        handleIntent(intent)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        if (intent == null) return

        var handledTaskNotificationOpen = false
        var handledRoutineNotificationOpen = false

        if (intent.hasExtra(TaskReminderReceiver.EXTRA_TASK_ID)) {
            val taskId = intent.getLongExtra(TaskReminderReceiver.EXTRA_TASK_ID, -1L)
            // Periodic tasks have their reminderDate pre-advanced by TaskReminderReceiver
            // before the notification fires. The notification embeds the original
            // reminderDate so the bottom-sheet completion toggle can compute the next
            // occurrence from the correct anchor (mirrors MarkAsCompletedReceiver).
            val originalReminderDate =
                intent
                    .getStringExtra(MarkAsCompletedReceiver.EXTRA_ORIGINAL_REMINDER_DATE)
                    ?.let { runCatching { LocalDateTime.parse(it) }.getOrNull() }
            if (taskId != -1L) {
                mainViewModel.setPendingNotificationAction(
                    PendingNotificationAction.OpenTask(
                        taskId = taskId,
                        originalReminderDate = originalReminderDate,
                    ),
                )
                tasksNavigationTrigger.longValue++
                handledTaskNotificationOpen = true
            }
        }

        if (intent.hasExtra(HabitReminderReceiver.EXTRA_HABIT_ID)) {
            val habitId = intent.getLongExtra(HabitReminderReceiver.EXTRA_HABIT_ID, -1L)
            if (habitId != -1L) {
                mainViewModel.setPendingNotificationAction(PendingNotificationAction.OpenHabit(habitId))
                routinesNavigationTrigger.longValue++
                handledRoutineNotificationOpen = true
            }
        }

        if (intent.hasExtra(HabitReminderReceiver.EXTRA_HABIT_CHAIN_ID)) {
            val chainId = intent.getLongExtra(HabitReminderReceiver.EXTRA_HABIT_CHAIN_ID, -1L)
            val scheduledDate = intent.getHabitChainScheduledDate()
            if (chainId != -1L) {
                mainViewModel.setPendingNotificationAction(
                    PendingNotificationAction.OpenHabitChain(
                        chainId = chainId,
                        scheduledDate = scheduledDate,
                    ),
                )
                routinesNavigationTrigger.longValue++
                handledRoutineNotificationOpen = true
            }
        }

        handleExplicitNavigationRequests(intent, handledRoutineNotificationOpen, handledTaskNotificationOpen)
    }

    private fun handleExplicitNavigationRequests(
        intent: Intent,
        handledRoutineNotificationOpen: Boolean,
        handledTaskNotificationOpen: Boolean,
    ) {
        if (
            !handledRoutineNotificationOpen &&
            intent.getBooleanExtra(HabitReminderReceiver.EXTRA_OPEN_ROUTINES, false)
        ) {
            routinesNavigationTrigger.longValue++
            intent.removeExtra(HabitReminderReceiver.EXTRA_OPEN_ROUTINES)
        }
        if (
            !handledTaskNotificationOpen &&
            intent.getBooleanExtra(TaskReminderReceiver.EXTRA_OPEN_TASKS, false)
        ) {
            tasksNavigationTrigger.longValue++
            intent.removeExtra(TaskReminderReceiver.EXTRA_OPEN_TASKS)
        }

        // Launched from the home-screen quick-add-task widget
        if (intent.getBooleanExtra(QuickAddTaskWidget.EXTRA_OPEN_NEW_TASK_DIALOG, false)) {
            mainViewModel.setPendingNotificationAction(PendingNotificationAction.OpenNewTaskDialog)
            tasksNavigationTrigger.longValue++
        }
    }

    private fun Intent.getHabitChainScheduledDate(): LocalDate? =
        getStringExtra(HabitReminderReceiver.EXTRA_SCHEDULED_DATE)
            ?.let { runCatching { LocalDate.parse(it) }.getOrNull() }

    private fun Intent.clearNotificationExtras() {
        removeExtra(TaskReminderReceiver.EXTRA_TASK_ID)
        removeExtra(TaskReminderReceiver.EXTRA_OPEN_TASKS)
        removeExtra(MarkAsCompletedReceiver.EXTRA_ORIGINAL_REMINDER_DATE)
        removeExtra(HabitReminderReceiver.EXTRA_HABIT_ID)
        removeExtra(HabitReminderReceiver.EXTRA_HABIT_CHAIN_ID)
        removeExtra(HabitReminderReceiver.EXTRA_OPEN_ROUTINES)
        removeExtra(HabitReminderReceiver.EXTRA_SCHEDULED_DATE)
        removeExtra(QuickAddTaskWidget.EXTRA_OPEN_NEW_TASK_DIALOG)
    }
}

@Composable
internal fun rememberStartDestination(state: MainUiState.Success): NavKey =
    remember {
        when {
            !state.isOnboardingCompleted -> OnboardingRoute()

            state.defaultTab == NavigationPreferencesRepository.DEFAULT_TAB_ROUTINES &&
                state.isRoutinesTabEnabled -> RoutinesRoute

            state.defaultTab == NavigationPreferencesRepository.DEFAULT_TAB_TASKS &&
                state.isTasksTabEnabled -> TasksRoute

            state.isRoutinesTabEnabled -> RoutinesRoute
            state.isTasksTabEnabled -> TasksRoute
            else -> RoutinesRoute
        }
    }
