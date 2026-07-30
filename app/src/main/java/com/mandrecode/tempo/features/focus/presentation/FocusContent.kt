package com.mandrecode.tempo.features.focus.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.mandrecode.tempo.R
import com.mandrecode.tempo.core.ui.components.TempoLoadingIndicator
import com.mandrecode.tempo.core.ui.components.WavyDivider
import com.mandrecode.tempo.core.ui.navigation.floatingNavigationBottomClearancePadding
import com.mandrecode.tempo.core.ui.theme.groupLabel
import com.mandrecode.tempo.core.ui.theme.sectionHeader
import com.mandrecode.tempo.features.focus.domain.model.FocusAgendaItem
import com.mandrecode.tempo.features.focus.presentation.components.FocusSummaryHero
import com.mandrecode.tempo.features.focus.presentation.components.RunningSessionCard
import com.mandrecode.tempo.features.focus.presentation.components.SessionFinishedSheet
import com.mandrecode.tempo.features.focus.presentation.components.StartSessionButton
import com.mandrecode.tempo.features.focus.presentation.components.UpNextCard
import com.mandrecode.tempo.features.focus.presentation.components.WorkedOnActions
import com.mandrecode.tempo.features.focus.presentation.components.upNextMetadata
import com.mandrecode.tempo.features.routines.presentation.components.cards.HabitCard
import com.mandrecode.tempo.features.routines.presentation.components.cards.HabitChainCard
import com.mandrecode.tempo.features.tasks.presentation.components.cards.TaskItem
import kotlinx.collections.immutable.ImmutableList
import kotlinx.datetime.LocalDate

private val ContentBlockTopCornerRadius = 28.dp

@Composable
fun FocusContent(
    uiState: FocusContract.UiState,
    onEvent: (FocusContract.UiEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val listBottomPadding = floatingNavigationBottomClearancePadding(defaultPadding = 16.dp)

    Box(
        // Matches the Scaffold containerColor this normally sits in, so the rounded seam below
        // resolves against the right colour when previewed or tested standalone.
        modifier = modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
    ) {
        if (uiState.isLoading) {
            TempoLoadingIndicator(message = stringResource(R.string.focus))
            return@Box
        }

        Column(modifier = Modifier.fillMaxWidth()) {
            FocusSummaryHero(
                today = uiState.today,
                streakDays = uiState.streakDays,
                history = uiState.history,
                scheduledCount = uiState.scheduledCount,
                completedCount = uiState.completedCount,
                progress = uiState.progress,
                band = uiState.headlineBand,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
            )

            Box(
                modifier =
                    Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        // clip before background so list overscroll cannot draw past the seam.
                        .clip(
                            RoundedCornerShape(
                                topStart = ContentBlockTopCornerRadius,
                                topEnd = ContentBlockTopCornerRadius,
                            ),
                        ).background(MaterialTheme.colorScheme.surface),
            ) {
                uiState.finishedSession?.let { finished ->
                    SessionFinishedSheet(
                        finished = finished,
                        nextSessionMinutes = uiState.defaultSessionLengthMinutes,
                        onEvent = onEvent,
                    )
                }

                if (uiState.isDayEmpty) {
                    FocusEmptyState(
                        undatedTaskCount = uiState.undatedTaskCount,
                        onUndatedClick = { onEvent(FocusContract.UiEvent.UndatedTasksClicked) },
                    )
                } else {
                    // `today` is always set once loading finishes; the guard keeps the chain card
                    // from needing a sentinel date.
                    uiState.today?.let { day ->
                        FocusAgendaList(
                            uiState = uiState,
                            today = day,
                            onEvent = onEvent,
                            bottomPadding = listBottomPadding,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FocusAgendaList(
    uiState: FocusContract.UiState,
    today: LocalDate,
    onEvent: (FocusContract.UiEvent) -> Unit,
    bottomPadding: Dp,
) {
    LazyColumn(
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = bottomPadding),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxSize(),
    ) {
        upNextSection(uiState = uiState, onEvent = onEvent)

        if (uiState.visibleOverdue.isNotEmpty()) {
            item(key = "overdue_header") {
                SectionHeader(
                    label = stringResource(R.string.focus_section_overdue, uiState.visibleOverdue.size),
                    modifier = Modifier.animateItem(),
                )
            }
            items(uiState.visibleOverdue, key = { it.id }) { entry ->
                AgendaRow(
                    entry = entry,
                    today = today,
                    expandedChainIds = uiState.expandedChainIds,
                    expandedTaskIds = uiState.expandedTaskIds,
                    onEvent = onEvent,
                )
            }
        }

        if (uiState.visibleToday.isNotEmpty()) {
            item(key = "today_header") {
                SectionHeader(
                    label = stringResource(R.string.focus_section_today, uiState.visibleToday.size),
                    modifier = Modifier.animateItem(),
                )
            }
            items(uiState.visibleToday, key = { it.id }) { entry ->
                AgendaRow(
                    entry = entry,
                    today = today,
                    expandedChainIds = uiState.expandedChainIds,
                    expandedTaskIds = uiState.expandedTaskIds,
                    onEvent = onEvent,
                )
            }
        }

        if (uiState.undatedTaskCount > 0) {
            item(key = "undated_footer") {
                UndatedTasksFooter(
                    count = uiState.undatedTaskCount,
                    onClick = { onEvent(FocusContract.UiEvent.UndatedTasksClicked) },
                    modifier = Modifier.animateItem(),
                )
            }
        }
    }
}

/**
 * The spotlight slot: the Up next card, or the same card transformed into the running session.
 * Lifted out of the list builder so neither grows past what fits on a screen.
 */
private fun LazyListScope.upNextSection(
    uiState: FocusContract.UiState,
    onEvent: (FocusContract.UiEvent) -> Unit,
) {
    val session = uiState.session
    val upNext = uiState.upNext
    if (session == null && upNext.isEmpty()) return

    item(key = "up_next_label") {
        Text(
            // The header names what the slot is holding. It said FOCUSING through a break too,
            // which is the same thing the card was doing one level down.
            text =
                stringResource(
                    when {
                        session == null -> R.string.focus_up_next
                        session.isBreak -> R.string.focus_session_break
                        else -> R.string.focus_session_focusing
                    },
                ).uppercase(),
            style = MaterialTheme.typography.groupLabel,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
    // One row whatever is happening: the running card is pinned first and the rest of the
    // shortlist stays beside it, so you can still see what is queued without ending what you are
    // doing to look.
    upNextRow(uiState, onEvent)
}

/**
 * The shortlist you swipe through.
 *
 * A row rather than a single card, because the best next thing is not always the one you are ready
 * to do, and the alternative was leaving the screen to find the one you are. Cards stop short of
 * the full width so the next one shows at the edge — the only honest way to say there is more.
 *
 * A running session takes the first place and keeps it: it is the one card you must be able to
 * reach without hunting, and the queue reads as what comes after it.
 */
private fun LazyListScope.upNextRow(
    uiState: FocusContract.UiState,
    onEvent: (FocusContract.UiEvent) -> Unit,
) {
    val session = uiState.session
    val queued = uiState.upNext.filterNot { it.task.id == session?.taskId }

    item(key = "up_next_row") {
        val listState = rememberLazyListState()
        BoxWithConstraints(modifier = Modifier.fillMaxWidth().animateItem()) {
            val cardCount = queued.size + if (session != null) 1 else 0
            val cardWidth = if (cardCount > 1) maxWidth * SINGLE_PEEK_FRACTION else maxWidth
            LazyRow(
                state = listState,
                flingBehavior = rememberSnapFlingBehavior(listState),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                // Cards start on the same line and end wherever their content does. A running
                // session is a deeper card than a queued one, and a task already worked on carries
                // two buttons where an untouched one carries a single pill — one height for all of
                // them meant padding the short ones out to match the tallest thing on screen.
                verticalAlignment = Alignment.Top,
            ) {
                if (session != null) {
                    item(key = "running_session") {
                        RunningSessionCard(
                            session = session,
                            subtasks = uiState.sessionSubtasks,
                            onExpand = { onEvent(FocusContract.UiEvent.OpenSessionScreen) },
                            onPauseResume = {
                                onEvent(
                                    if (session.isPaused) {
                                        FocusContract.UiEvent.ResumeSession
                                    } else {
                                        FocusContract.UiEvent.PauseSession
                                    },
                                )
                            },
                            onStop = { onEvent(FocusContract.UiEvent.StopSession) },
                            onComplete = { onEvent(FocusContract.UiEvent.CompleteSessionTask) },
                            onBackToWork = { onEvent(FocusContract.UiEvent.BackToWork) },
                            modifier = Modifier.width(cardWidth),
                        )
                    }
                }
                items(queued, key = { it.id }) { entry ->
                    UpNextCard(
                        title = entry.displayTitle(),
                        metadata = entry.upNextMetadata(),
                        metadataIconRes = R.drawable.ic_flag.takeIf { entry.priority != null },
                        onClick = {
                            onEvent(FocusContract.UiEvent.PreviewUpNext(entry.task.id))
                        },
                        modifier = Modifier.width(cardWidth),
                        trailingContent = {
                            UpNextAction(
                                entry = entry,
                                defaultSessionLengthMinutes = uiState.defaultSessionLengthMinutes,
                                onEvent = onEvent,
                            )
                        },
                    )
                }
            }
        }
    }
}

/**
 * What a queued card offers: starting, or picking back up work that already had a run at it.
 *
 * A task with sessions behind it and no tick is neither untouched nor finished, and offering
 * "Start 25 min" as though nothing had happened lost that — which is exactly what dismissing the
 * break sheet without choosing used to do.
 */
@Composable
private fun UpNextAction(
    entry: FocusAgendaItem.TaskEntry,
    defaultSessionLengthMinutes: Int,
    onEvent: (FocusContract.UiEvent) -> Unit,
) {
    if (entry.sessionsToday > 0) {
        WorkedOnActions(
            onBackToWork = { onEvent(FocusContract.UiEvent.StartSession(taskId = entry.task.id)) },
            onComplete = { onEvent(FocusContract.UiEvent.ToggleTaskCompletion(entry.task)) },
        )
    } else {
        StartSessionButton(
            minutes = defaultSessionLengthMinutes,
            onClick = { onEvent(FocusContract.UiEvent.StartSession(taskId = entry.task.id)) },
        )
    }
}

/** Leaves the next card showing at the edge, so the row reads as a row. */
private const val SINGLE_PEEK_FRACTION = 0.88f

@Composable
private fun AgendaRow(
    entry: FocusAgendaItem,
    today: LocalDate,
    expandedChainIds: ImmutableList<Long>,
    expandedTaskIds: ImmutableList<Long>,
    onEvent: (FocusContract.UiEvent) -> Unit,
) {
    when (entry) {
        is FocusAgendaItem.TaskEntry ->
            TaskItem(
                task = entry.task,
                subtasks = entry.subtasks,
                onToggleCompletion = { onEvent(FocusContract.UiEvent.ToggleTaskCompletion(it)) },
                onEdit = { onEvent(FocusContract.UiEvent.EditTask(it)) },
                // Without both of these the card is pinned open: its default is expanded and its
                // toggle goes nowhere, so the chevron did nothing at all here.
                isSubtasksExpanded = entry.task.id in expandedTaskIds,
                onToggleSubtasksExpansion = {
                    onEvent(FocusContract.UiEvent.ToggleSubtasksExpanded(entry.task.id))
                },
                onAddSubtask = { onEvent(FocusContract.UiEvent.AddSubtask(it)) },
            )

        is FocusAgendaItem.HabitEntry ->
            // HabitCard, not HabitItem: the card is what resolves the habit's colour and draws its
            // surface — HabitItem is only the row inside it, and on its own renders an uncoloured
            // habit as bare text on the background.
            HabitCard(
                habit = entry.habit,
                selectedDate = today,
                onEdit = { onEvent(FocusContract.UiEvent.EditHabit(entry.habit)) },
                // Focus has no destructive actions; deleting a habit stays in Routines.
                onDelete = {},
                onToggle = { habitId, isCompleted ->
                    onEvent(FocusContract.UiEvent.ToggleHabitCompletion(habitId, isCompleted))
                },
                showTimeline = false,
            )

        is FocusAgendaItem.ChainEntry ->
            HabitChainCard(
                habitChain = entry.chain,
                chainHabits = entry.habits,
                selectedDate = today,
                isExpanded = entry.chain.id in expandedChainIds,
                onEdit = { },
                onToggleExpansion = {
                    onEvent(FocusContract.UiEvent.ToggleChainExpanded(entry.chain.id))
                },
                onHabitToggle = { habitId, isCompleted ->
                    onEvent(FocusContract.UiEvent.ToggleHabitCompletion(habitId, isCompleted))
                },
                onHabitClick = { habitId ->
                    entry.habits.firstOrNull { it.id == habitId }?.let { habit ->
                        onEvent(FocusContract.UiEvent.EditHabit(habit))
                    }
                },
                showTimeline = false,
            )
    }
}

@Composable
private fun SectionHeader(
    label: String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth().padding(top = 12.dp, bottom = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.sectionHeader,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(end = 12.dp),
        )
        WavyDivider(modifier = Modifier.weight(1f))
    }
}

@Composable
private fun UndatedTasksFooter(
    count: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val haptic = LocalHapticFeedback.current
    Text(
        text = pluralStringResource(R.plurals.focus_undated_tasks, count, count),
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center,
        modifier =
            modifier
                .fillMaxWidth()
                .clickable {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onClick()
                }.padding(vertical = 20.dp),
    )
}

@Composable
private fun FocusEmptyState(
    undatedTaskCount: Int,
    onUndatedClick: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = stringResource(R.string.focus_empty_title),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
        )
        Text(
            text = stringResource(R.string.focus_empty_message),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 8.dp),
        )
        if (undatedTaskCount > 0) {
            UndatedTasksFooter(count = undatedTaskCount, onClick = onUndatedClick)
        }
    }
}

@Composable
private fun FocusAgendaItem.displayTitle(): String =
    when (this) {
        is FocusAgendaItem.TaskEntry -> task.title
        is FocusAgendaItem.HabitEntry -> habit.title
        is FocusAgendaItem.ChainEntry -> chain.title
    }

private fun FocusAgendaItem.editEvent(): FocusContract.UiEvent =
    when (this) {
        is FocusAgendaItem.TaskEntry -> FocusContract.UiEvent.EditTask(task)
        is FocusAgendaItem.HabitEntry -> FocusContract.UiEvent.EditHabit(habit)
        is FocusAgendaItem.ChainEntry -> FocusContract.UiEvent.ToggleChainExpanded(chain.id)
    }
