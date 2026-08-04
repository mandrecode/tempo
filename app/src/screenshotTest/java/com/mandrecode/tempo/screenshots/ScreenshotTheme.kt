package com.mandrecode.tempo.screenshots

import androidx.compose.runtime.Composable
import com.mandrecode.tempo.core.ui.theme.TempoTheme

/**
 * [TempoTheme] with the palette pinned to Tempo's own brand colors.
 *
 * `useTempoColors = true` is the app's real stored default (see `MainActivity`), and unlike the
 * dynamic-color path it does not read the platform's wallpaper palette — so the reference images
 * stay stable across `compileSdk`/layoutlib bumps instead of churning on every one.
 *
 * Light/dark comes from the preview's `uiMode` via [TempoTheme]'s `isSystemInDarkTheme()` default.
 */
@Composable
internal fun ScreenshotTheme(content: @Composable () -> Unit) {
    TempoTheme(useTempoColors = true, content = content)
}
