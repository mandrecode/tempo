package com.mandrecode.tempo.core.ui.navigation

import androidx.compose.animation.ContentTransform
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.scene.Scene
import com.mandrecode.tempo.core.ui.theme.TempoMotionTokens

internal fun onboardingHandoffEnterTransition(): EnterTransition =
    fadeIn(animationSpec = tween(TempoMotionTokens.DURATION_LONG_MILLIS)) +
        scaleIn(
            initialScale = ONBOARDING_HANDOFF_ENTER_SCALE,
            animationSpec = tween(TempoMotionTokens.DURATION_LONG_MILLIS),
        )

internal fun onboardingHandoffExitTransition(): ExitTransition =
    fadeOut(animationSpec = tween(TempoMotionTokens.DURATION_LONG_MILLIS)) +
        scaleOut(
            targetScale = ONBOARDING_HANDOFF_EXIT_SCALE,
            animationSpec = tween(TempoMotionTokens.DURATION_LONG_MILLIS),
        )

internal fun navigationTransition(
    initialScene: Scene<NavKey>,
    targetScene: Scene<NavKey>,
    settingsAsTopLevel: Boolean = false,
): ContentTransform {
    val initialRoute = initialScene.entries.last().contentKey
    val targetRoute = targetScene.entries.last().contentKey
    return when {
        initialRoute is OnboardingRoute && (targetRoute == RoutinesRoute || targetRoute == TasksRoute) ->
            onboardingHandoffEnterTransition() togetherWith onboardingHandoffExitTransition()

        targetRoute == SettingsRoute && !settingsAsTopLevel ->
            settingsEnterTransition() togetherWith
                fadeOut(animationSpec = tween(TempoMotionTokens.DURATION_STANDARD_MILLIS))

        else -> defaultNavigationTransition()
    }
}

internal fun navigationPopTransition(
    initialScene: Scene<NavKey>,
    targetScene: Scene<NavKey>,
    settingsAsTopLevel: Boolean = false,
): ContentTransform =
    if (initialScene.entries.last().contentKey == SettingsRoute && !settingsAsTopLevel) {
        settingsPopEnterTransition() togetherWith settingsExitTransition()
    } else {
        navigationTransition(initialScene, targetScene, settingsAsTopLevel)
    }

private fun defaultNavigationTransition(): ContentTransform =
    fadeIn(animationSpec = tween(TempoMotionTokens.DURATION_STANDARD_MILLIS)) togetherWith
        fadeOut(animationSpec = tween(TempoMotionTokens.DURATION_STANDARD_MILLIS))

private const val ONBOARDING_HANDOFF_ENTER_SCALE = 0.94f
private const val ONBOARDING_HANDOFF_EXIT_SCALE = 1.04f
