package com.mandrecode.tempo.core.ui.glass

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color
import com.mandrecode.tempo.core.ui.theme.LocalTempoGlass

// The glass finish for content components. They are flat surfaces in their own colours, with no
// outline, sheen or shadow (pass `border = null` where a component would draw one by default): on
// the two-tone page their colours set them apart, and only the floating chrome is glass ([glass]).

/** A surface's fill: its own colour [solid], with the glass's touch of translucency. */
@Composable
@ReadOnlyComposable
fun glassCardColor(solid: Color): Color = solid.copy(alpha = solid.alpha * LocalTempoGlass.current.cardAlpha)

/** A secondary (outlined) button's fill: the cards' frost, in place of the outline it would draw. */
@Composable
@ReadOnlyComposable
fun glassButtonContainer(): Color = LocalTempoGlass.current.cardFrost
