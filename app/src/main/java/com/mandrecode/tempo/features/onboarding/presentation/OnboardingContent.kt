package com.mandrecode.tempo.features.onboarding.presentation

import android.widget.ImageView
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.mandrecode.tempo.R
import com.mandrecode.tempo.features.settings.presentation.ColorSchemeSection
import com.mandrecode.tempo.features.settings.presentation.ThemeSection

@Composable
fun OnboardingContent(
    uiState: OnboardingContract.UiState,
    onEvent: (OnboardingContract.UiEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val layout = rememberOnboardingLayout()
    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .windowInsetsPadding(WindowInsets.safeDrawing)
                    .padding(
                        horizontal = layout.outerHorizontalPadding,
                        vertical = layout.outerVerticalPadding,
                    ),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            OnboardingSkip(
                isVisible = !uiState.isLastPage,
                layout = layout,
                onEvent = onEvent,
            )
            OnboardingProgress(
                currentPage = uiState.currentPage,
                pageCount = OnboardingContract.PAGE_COUNT,
                modifier = Modifier.fillMaxWidth().widthIn(max = layout.footerMaxWidth),
            )
            OnboardingPage(
                uiState = uiState,
                onEvent = onEvent,
                layout = layout,
                modifier = Modifier.weight(1f).fillMaxWidth(),
            )
            OnboardingFooter(uiState = uiState, layout = layout, onEvent = onEvent)
        }
    }
}

@Composable
private fun OnboardingSkip(
    isVisible: Boolean,
    layout: OnboardingLayout,
    onEvent: (OnboardingContract.UiEvent) -> Unit,
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .widthIn(max = layout.pageMaxWidth)
                .heightIn(min = 48.dp),
        horizontalArrangement = Arrangement.End,
    ) {
        if (isVisible) {
            AnimatedOnboardingButton(
                label = stringResource(R.string.onboarding_skip),
                onClick = { onEvent(OnboardingContract.UiEvent.SkipClicked) },
                style = OnboardingButtonStyle.Text,
                modifier = Modifier.testTag(OnboardingTestTags.SKIP),
            )
        }
    }
}

@Composable
private fun OnboardingPage(
    uiState: OnboardingContract.UiState,
    onEvent: (OnboardingContract.UiEvent) -> Unit,
    layout: OnboardingLayout,
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
        modifier = modifier.testTag(OnboardingTestTags.PAGE),
    ) { page ->
        Box(
            modifier = Modifier.fillMaxSize().clipToBounds(),
            contentAlignment = Alignment.Center,
        ) {
            when (page) {
                0 -> TasksAndCategoriesPage(layout = layout)
                1 -> RoutinesAndRemindersPage(layout = layout)
                2 -> AppearancePage(uiState = uiState, layout = layout, onEvent = onEvent)
                SETUP_PAGE_INDEX -> OnboardingSetupPage(uiState = uiState, layout = layout, onEvent = onEvent)
                else -> OnboardingWelcomePage(layout = layout)
            }
        }
    }
}

@Composable
internal fun OnboardingWelcomePage(
    layout: OnboardingLayout,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        val appName = stringResource(R.string.app_name)
        AndroidView(
            factory = { context ->
                ImageView(context).apply {
                    setImageResource(R.mipmap.ic_launcher)
                    scaleType = ImageView.ScaleType.FIT_CENTER
                }
            },
            update = { imageView -> imageView.contentDescription = appName },
            modifier =
                Modifier
                    .size(if (layout.isShort) 96.dp else 144.dp)
                    .testTag(OnboardingTestTags.WELCOME_LOGO),
        )
        Text(
            text = stringResource(R.string.app_name),
            style =
                if (layout.isShort) {
                    MaterialTheme.typography.headlineLarge
                } else {
                    MaterialTheme.typography.displayMedium
                },
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = layout.sectionSpacing),
        )
    }
}

@Composable
private fun OnboardingFooter(
    uiState: OnboardingContract.UiState,
    layout: OnboardingLayout,
    onEvent: (OnboardingContract.UiEvent) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth().widthIn(max = layout.footerMaxWidth),
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
            modifier =
                Modifier
                    .weight(if (uiState.isLastPage) FINAL_ACTION_WEIGHT else 1f)
                    .testTag(OnboardingTestTags.FORWARD),
        )
    }
}

@Composable
private fun TasksAndCategoriesPage(layout: OnboardingLayout) {
    EducationPage(
        layout = layout,
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
private fun RoutinesAndRemindersPage(layout: OnboardingLayout) {
    EducationPage(
        layout = layout,
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
    layout: OnboardingLayout,
    iconRes: Int,
    title: String,
    description: String,
    concepts: List<EducationConcept>,
) = AdaptiveOnboardingPage(
    layout = layout,
    intro = {
        if (!layout.isShort) {
            HeroIcon(iconRes = iconRes)
        }
        Text(
            text = title,
            style =
                if (layout.isShort) {
                    MaterialTheme.typography.headlineMedium
                } else {
                    MaterialTheme.typography.headlineLarge
                },
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center,
        )
        Text(
            text = description,
            style =
                if (layout.isShort) {
                    MaterialTheme.typography.bodyMedium
                } else {
                    MaterialTheme.typography.bodyLarge
                },
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    },
    body = {
        concepts.forEach { concept -> ConceptCard(concept = concept, compact = layout.isShort) }
    },
)

@Composable
private fun ConceptCard(
    concept: EducationConcept,
    compact: Boolean,
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.padding(if (compact) 12.dp else 16.dp),
            horizontalArrangement = Arrangement.spacedBy(if (compact) 12.dp else 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                painter = painterResource(concept.iconRes),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(if (compact) 24.dp else 28.dp),
            )
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    concept.title,
                    style =
                        if (compact) {
                            MaterialTheme.typography.titleSmall
                        } else {
                            MaterialTheme.typography.titleMedium
                        },
                )
                Text(
                    concept.description,
                    style =
                        if (compact) {
                            MaterialTheme.typography.bodySmall
                        } else {
                            MaterialTheme.typography.bodyMedium
                        },
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun AppearancePage(
    uiState: OnboardingContract.UiState,
    layout: OnboardingLayout,
    onEvent: (OnboardingContract.UiEvent) -> Unit,
) {
    val settingsUiState = uiState.toSettingsUiState()
    val onSettingsEvent = { event: com.mandrecode.tempo.features.settings.presentation.SettingsContract.UiEvent ->
        val onboardingEvent = event.toOnboardingEvent()
        if (onboardingEvent != null) onEvent(onboardingEvent)
    }

    AdaptiveOnboardingPage(
        layout = layout,
        intro = {
            if (!layout.isShort) {
                HeroIcon(iconRes = R.drawable.ic_palette)
            }
            Text(
                text = stringResource(R.string.onboarding_appearance_title),
                style =
                    if (layout.isShort) {
                        MaterialTheme.typography.headlineMedium
                    } else {
                        MaterialTheme.typography.headlineLarge
                    },
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
            )
            Text(
                text = stringResource(R.string.onboarding_appearance_description),
                style =
                    if (layout.isShort) {
                        MaterialTheme.typography.bodyMedium
                    } else {
                        MaterialTheme.typography.bodyLarge
                    },
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        },
        body = {
            ThemeSection(uiState = settingsUiState, onEvent = onSettingsEvent)
            ColorSchemeSection(uiState = settingsUiState, onEvent = onSettingsEvent)
        },
    )
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
            modifier =
                Modifier
                    .padding(24.dp)
                    .size(48.dp),
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
    const val PROGRESS = "onboarding_progress"
    const val PROGRESS_SEGMENT = "onboarding_progress_segment"
    const val PAGE = "onboarding_page"
    const val WELCOME_LOGO = "onboarding_welcome_logo"
    const val SINGLE_PANE = "onboarding_single_pane"
    const val TWO_PANE = "onboarding_two_pane"
}

internal val OnboardingMaxWidth = 600.dp

private const val FINAL_ACTION_WEIGHT = 2f
private const val SETUP_PAGE_INDEX = 3
