package com.mandrecode.tempo.features.onboarding.presentation

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.mandrecode.tempo.R
import com.mandrecode.tempo.features.settings.presentation.ColorSchemeSection
import com.mandrecode.tempo.features.settings.presentation.ThemeSection

@Composable
fun OnboardingContent(
    uiState: OnboardingContract.UiState,
    onEvent: (OnboardingContract.UiEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .windowInsetsPadding(WindowInsets.safeDrawing)
                    .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            OnboardingSkip(onEvent = onEvent)
            OnboardingPage(
                uiState = uiState,
                onEvent = onEvent,
                modifier = Modifier.weight(1f).fillMaxWidth(),
            )
            OnboardingFooter(uiState = uiState, onEvent = onEvent)
        }
    }
}

@Composable
private fun OnboardingSkip(onEvent: (OnboardingContract.UiEvent) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().widthIn(max = OnboardingMaxWidth),
        horizontalArrangement = Arrangement.End,
    ) {
        AnimatedOnboardingButton(
            label = stringResource(R.string.onboarding_skip),
            onClick = { onEvent(OnboardingContract.UiEvent.SkipClicked) },
            style = OnboardingButtonStyle.Text,
            modifier = Modifier.testTag(OnboardingTestTags.SKIP),
        )
    }
}

@Composable
private fun OnboardingPage(
    uiState: OnboardingContract.UiState,
    onEvent: (OnboardingContract.UiEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    AnimatedContent(
        targetState = uiState.currentPage,
        transitionSpec = {
            val direction = if (targetState > initialState) 1 else -1
            (slideInHorizontally { width -> direction * width } + fadeIn()) togetherWith
                (slideOutHorizontally { width -> -direction * width } + fadeOut()) using
                SizeTransform(clip = false)
        },
        label = "onboardingPage",
        modifier = modifier,
    ) { page ->
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            when (page) {
                0 -> TasksAndCategoriesPage()
                1 -> RoutinesAndRemindersPage()
                2 -> AppearancePage(uiState = uiState, onEvent = onEvent)
                else -> OnboardingSetupPage(uiState = uiState, onEvent = onEvent)
            }
        }
    }
}

@Composable
private fun OnboardingFooter(
    uiState: OnboardingContract.UiState,
    onEvent: (OnboardingContract.UiEvent) -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth().widthIn(max = OnboardingMaxWidth),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        OnboardingProgress(
            currentPage = uiState.currentPage,
            pageCount = OnboardingContract.PAGE_COUNT,
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            if (uiState.isFirstPage) {
                Spacer(modifier = Modifier.weight(1f))
            } else {
                AnimatedOnboardingButton(
                    label = stringResource(R.string.back),
                    onClick = { onEvent(OnboardingContract.UiEvent.BackClicked) },
                    style = OnboardingButtonStyle.Outlined,
                    modifier = Modifier.weight(1f).testTag(OnboardingTestTags.BACK),
                )
            }
            val label =
                if (uiState.isLastPage) {
                    R.string.onboarding_start
                } else {
                    R.string.onboarding_next
                }
            AnimatedOnboardingButton(
                label = stringResource(label),
                onClick = {
                    val event =
                        if (uiState.isLastPage) {
                            OnboardingContract.UiEvent.FinishClicked
                        } else {
                            OnboardingContract.UiEvent.NextClicked
                        }
                    onEvent(event)
                },
                style = OnboardingButtonStyle.Primary,
                modifier = Modifier.weight(1f).testTag(OnboardingTestTags.FORWARD),
            )
        }
    }
}

@Composable
private fun TasksAndCategoriesPage() {
    EducationPage(
        iconRes = R.drawable.ic_tasks,
        title = stringResource(R.string.onboarding_tasks_title),
        description = stringResource(R.string.onboarding_tasks_description),
        concepts =
            listOf(
                EducationConcept(
                    iconRes = R.drawable.ic_add_task,
                    title = stringResource(R.string.onboarding_tasks_concept_title),
                    description = stringResource(R.string.onboarding_tasks_concept_description),
                ),
                EducationConcept(
                    iconRes = R.drawable.ic_category,
                    title = stringResource(R.string.onboarding_categories_concept_title),
                    description = stringResource(R.string.onboarding_categories_concept_description),
                ),
            ),
    )
}

@Composable
private fun RoutinesAndRemindersPage() {
    EducationPage(
        iconRes = R.drawable.ic_routine,
        title = stringResource(R.string.onboarding_routines_title),
        description = stringResource(R.string.onboarding_routines_description),
        concepts =
            listOf(
                EducationConcept(
                    iconRes = R.drawable.ic_repeat,
                    title = stringResource(R.string.onboarding_routines_concept_title),
                    description = stringResource(R.string.onboarding_routines_concept_description),
                ),
                EducationConcept(
                    iconRes = R.drawable.ic_reminder,
                    title = stringResource(R.string.onboarding_reminders_concept_title),
                    description = stringResource(R.string.onboarding_reminders_concept_description),
                ),
            ),
    )
}

@Composable
private fun EducationPage(
    iconRes: Int,
    title: String,
    description: String,
    concepts: List<EducationConcept>,
) {
    Column(
        modifier = Modifier.widthIn(max = OnboardingMaxWidth).verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        HeroIcon(iconRes = iconRes)
        Text(
            text = title,
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center,
        )
        Text(
            text = description,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            concepts.forEach { concept -> ConceptCard(concept) }
        }
    }
}

@Composable
private fun ConceptCard(concept: EducationConcept) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                painter = painterResource(concept.iconRes),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(28.dp),
            )
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(concept.title, style = MaterialTheme.typography.titleMedium)
                Text(
                    concept.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun AppearancePage(
    uiState: OnboardingContract.UiState,
    onEvent: (OnboardingContract.UiEvent) -> Unit,
) {
    val settingsUiState = uiState.toSettingsUiState()
    val onSettingsEvent = { event: com.mandrecode.tempo.features.settings.presentation.SettingsContract.UiEvent ->
        onEvent(event.toOnboardingEvent())
    }

    Column(
        modifier = Modifier.widthIn(max = OnboardingMaxWidth).verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        HeroIcon(iconRes = R.drawable.ic_palette)
        Text(
            text = stringResource(R.string.onboarding_appearance_title),
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center,
        )
        Text(
            text = stringResource(R.string.onboarding_appearance_description),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        ThemeSection(uiState = settingsUiState, onEvent = onSettingsEvent)
        ColorSchemeSection(uiState = settingsUiState, onEvent = onSettingsEvent)
    }
}

@Composable
private fun HeroIcon(iconRes: Int) {
    Surface(
        shape = RoundedCornerShape(28.dp),
        color = MaterialTheme.colorScheme.primaryContainer,
        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
    ) {
        Icon(
            painter = painterResource(iconRes),
            contentDescription = null,
            modifier = Modifier.padding(24.dp).size(48.dp),
        )
    }
}

private data class EducationConcept(
    val iconRes: Int,
    val title: String,
    val description: String,
)

internal object OnboardingTestTags {
    const val SKIP = "onboarding_skip"
    const val BACK = "onboarding_back"
    const val FORWARD = "onboarding_forward"
    const val PROGRESS_SEGMENT = "onboarding_progress_segment"
}

internal val OnboardingMaxWidth = 600.dp
