package com.mandrecode.tempo.features.onboarding.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalMediaQueryApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.derivedMediaQuery
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

internal data class OnboardingLayout(
    val isExpanded: Boolean,
    val isShort: Boolean,
) {
    val outerHorizontalPadding: Dp =
        when {
            isExpanded -> 48.dp
            isShort -> 16.dp
            else -> 24.dp
        }
    val outerVerticalPadding: Dp = if (isShort) 8.dp else 16.dp
    val sectionSpacing: Dp = if (isShort) 12.dp else 20.dp
    val pageVerticalPadding: Dp = if (isShort) 0.dp else 24.dp
    val pageMaxWidth: Dp = if (isExpanded) 1120.dp else OnboardingMaxWidth
    val footerMaxWidth: Dp = if (isExpanded) 720.dp else OnboardingMaxWidth
}

@OptIn(ExperimentalMediaQueryApi::class)
@Composable
internal fun rememberOnboardingLayout(): OnboardingLayout {
    val isExpanded by derivedMediaQuery { windowWidth >= ExpandedOnboardingWidth }
    val isShort by derivedMediaQuery { windowHeight < ShortOnboardingHeight }
    return OnboardingLayout(isExpanded = isExpanded, isShort = isShort)
}

@Composable
internal fun AdaptiveOnboardingPage(
    layout: OnboardingLayout,
    intro: @Composable ColumnScope.() -> Unit,
    body: @Composable ColumnScope.() -> Unit,
    modifier: Modifier = Modifier,
    introAlignment: Alignment.Horizontal = Alignment.CenterHorizontally,
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        if (layout.isExpanded) {
            Row(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .widthIn(max = layout.pageMaxWidth)
                        .verticalScroll(rememberScrollState())
                        .padding(vertical = layout.pageVerticalPadding)
                        .testTag(OnboardingTestTags.TWO_PANE),
                horizontalArrangement = Arrangement.spacedBy(if (layout.isShort) 24.dp else 48.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = introAlignment,
                    verticalArrangement = Arrangement.spacedBy(layout.sectionSpacing),
                    content = intro,
                )
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(layout.sectionSpacing),
                    content = body,
                )
            }
        } else {
            Column(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .widthIn(max = layout.pageMaxWidth)
                        .verticalScroll(rememberScrollState())
                        .padding(vertical = layout.pageVerticalPadding)
                        .testTag(OnboardingTestTags.SINGLE_PANE),
                horizontalAlignment = introAlignment,
                verticalArrangement = Arrangement.spacedBy(layout.sectionSpacing),
            ) {
                intro()
                body()
            }
        }
    }
}

private val ExpandedOnboardingWidth = 840.dp
private val ShortOnboardingHeight = 600.dp
