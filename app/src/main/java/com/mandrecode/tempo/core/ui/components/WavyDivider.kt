package com.mandrecode.tempo.core.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

private val WAVE_WAVELENGTH = 14.dp
private val WAVE_AMPLITUDE = 3.dp
private val WAVE_STROKE_WIDTH = 2.dp

/**
 * A horizontal sine-like line for section separators — the wavy stroke Material 3 Expressive
 * uses for progress indicators, applied to a divider instead of a plain
 * [androidx.compose.material3.HorizontalDivider].
 */
@Composable
fun WavyDivider(
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f),
    thickness: Dp = WAVE_STROKE_WIDTH,
    wavelength: Dp = WAVE_WAVELENGTH,
    amplitude: Dp = WAVE_AMPLITUDE,
) {
    // The stroke extends thickness/2 beyond the path's crest/trough, so the canvas needs that
    // much extra height on top of the amplitude or Compose clips the wave's peaks.
    Canvas(modifier = modifier.height(amplitude * 2 + thickness)) {
        // Guards against a caller-supplied wavelength resolving to ~0px, which would leave
        // nextX == x below and spin the while loop forever.
        val halfWavelengthPx = (wavelength.toPx() / 2f).coerceAtLeast(1f)
        val amplitudePx = amplitude.toPx()
        val midY = size.height / 2f

        // StrokeCap.Round extends thickness/2 past each path endpoint in the direction of
        // travel — for a horizontal path starting at x=0 and ending at size.width, that pushes
        // the caps past the canvas bounds. Inset the drawn path by that much on both sides.
        val inset = (thickness.toPx() / 2f).coerceAtMost(size.width / 2f)
        val waveWidth = size.width - inset * 2f

        val path =
            Path().apply {
                moveTo(inset, midY)
                var x = 0f
                var crestUp = true
                while (x < waveWidth) {
                    val nextX = (x + halfWavelengthPx).coerceAtMost(waveWidth)
                    val controlX = inset + (x + nextX) / 2f
                    val controlY = if (crestUp) midY - amplitudePx else midY + amplitudePx
                    quadraticTo(controlX, controlY, inset + nextX, midY)
                    x = nextX
                    crestUp = !crestUp
                }
            }

        drawPath(
            path = path,
            color = color,
            style = Stroke(width = thickness.toPx(), cap = StrokeCap.Round),
        )
    }
}
