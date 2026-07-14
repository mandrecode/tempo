package com.mandrecode.tempo.core.ui.navigation

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hasRoute
import com.mandrecode.tempo.core.ui.theme.TempoMotionTokens

internal fun onboardingHandoffEnterTransition(from: NavDestination): EnterTransition? =
    if (from.hasRoute<OnboardingRoute>()) {
        fadeIn(animationSpec = tween(TempoMotionTokens.DURATION_LONG_MILLIS)) +
            scaleIn(
                initialScale = ONBOARDING_HANDOFF_ENTER_SCALE,
                animationSpec = tween(TempoMotionTokens.DURATION_LONG_MILLIS),
            )
    } else {
        null
    }

internal fun onboardingHandoffExitTransition(to: NavDestination): ExitTransition? =
    if (to.hasRoute<RoutinesRoute>() || to.hasRoute<TasksRoute>()) {
        fadeOut(animationSpec = tween(TempoMotionTokens.DURATION_LONG_MILLIS)) +
            scaleOut(
                targetScale = ONBOARDING_HANDOFF_EXIT_SCALE,
                animationSpec = tween(TempoMotionTokens.DURATION_LONG_MILLIS),
            )
    } else {
        null
    }

private const val ONBOARDING_HANDOFF_ENTER_SCALE = 0.94f
private const val ONBOARDING_HANDOFF_EXIT_SCALE = 1.04f
