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
): ContentTransform {
    // The main entry is always first, whether the scene is a plain single-pane scene or an
    // EditorSupportingPaneScene (entries = [mainEntry, editorEntry]) with an editor pane open.
    val initialEntry = initialScene.entries.first()
    val initialMainRoute = initialEntry.metadata[EDITOR_MAIN_ROUTE_METADATA]
    val targetMainRoute = targetScene.entries.first().metadata[EDITOR_MAIN_ROUTE_METADATA]
    return when {
        initialEntry.metadata.containsKey(ONBOARDING_ROUTE_METADATA) &&
            (targetMainRoute == RoutinesRoute || targetMainRoute == TasksRoute) ->
            onboardingHandoffEnterTransition() togetherWith onboardingHandoffExitTransition()

        isTopLevelTabSwitch(initialMainRoute, targetMainRoute) -> tabSwitchTransition()

        else -> defaultNavigationTransition()
    }
}

internal fun navigationPopTransition(
    initialScene: Scene<NavKey>,
    targetScene: Scene<NavKey>,
): ContentTransform = navigationTransition(initialScene, targetScene)

private fun isTopLevelTabSwitch(
    initialMainRoute: Any?,
    targetMainRoute: Any?,
): Boolean =
    initialMainRoute != targetMainRoute &&
        (initialMainRoute == RoutinesRoute || initialMainRoute == TasksRoute) &&
        (targetMainRoute == RoutinesRoute || targetMainRoute == TasksRoute)

/**
 * Tasks/Routines bottom-tab switch: an instant cut, no animation at all — matching how apps like
 * Google Health swap peer bottom-nav tabs. [defaultNavigationTransition]'s fade sets alpha < 1 on
 * both scenes mid-transition, which blends them against whatever's behind (MainActivity's root
 * Surface); a slide avoids alpha but still has visible motion. Neither reads as "instant", and
 * both were tried and rejected during this feature's development — an outright cut is the only
 * option with zero risk of a background-color flash and no animation to dislike.
 */
private fun tabSwitchTransition(): ContentTransform = EnterTransition.None togetherWith ExitTransition.None

private fun defaultNavigationTransition(): ContentTransform =
    fadeIn(animationSpec = tween(TempoMotionTokens.DURATION_STANDARD_MILLIS)) togetherWith
        fadeOut(animationSpec = tween(TempoMotionTokens.DURATION_STANDARD_MILLIS))

private const val ONBOARDING_HANDOFF_ENTER_SCALE = 0.94f
private const val ONBOARDING_HANDOFF_EXIT_SCALE = 1.04f
