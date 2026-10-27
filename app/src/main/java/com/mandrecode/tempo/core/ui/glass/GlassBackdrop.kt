package com.mandrecode.tempo.core.ui.glass

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.ContentDrawScope
import androidx.compose.ui.graphics.layer.GraphicsLayer
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.node.CompositionLocalConsumerModifierNode
import androidx.compose.ui.node.DrawModifierNode
import androidx.compose.ui.node.GlobalPositionAwareModifierNode
import androidx.compose.ui.node.ModifierNodeElement
import androidx.compose.ui.node.currentValueOf
import androidx.compose.ui.node.requireGraphicsContext
import androidx.compose.ui.platform.InspectorInfo
import com.mandrecode.tempo.core.ui.theme.LocalTempoGlass

/**
 * What the floating chrome sees through itself: the screen content recorded into a [GraphicsLayer]
 * as it is drawn, plus where that content sits in the window so a glass surface can line its slice
 * of it up with what is really behind it.
 *
 * Recording costs nothing extra on the content's side — the layer is drawn instead of the content,
 * not as well as it — and a glass surface draws the layer by reference, so the content redrawing
 * (a list scrolling under the bar) reaches the glass without the content re-recording anything.
 */
@Stable
class GlassBackdrop internal constructor() {
    internal var layer: GraphicsLayer? by mutableStateOf(null)
    internal var positionInWindow: Offset by mutableStateOf(Offset.Zero)
}

/**
 * The backdrop glass surfaces outside [GlassBackdropSource] sample, or `null` where there is none —
 * inside the source itself, or in a separate window such as a dialog. Glass with no backdrop falls
 * back to its denser, unblurred tint.
 */
internal val LocalGlassBackdrop = staticCompositionLocalOf<GlassBackdrop?> { null }

@Composable
fun rememberGlassBackdrop(): GlassBackdrop = remember { GlassBackdrop() }

/**
 * Records [content] into [backdrop]. Glass placed *inside* [content] cannot sample it — it would be
 * drawing a layer that contains itself — so the source clears [LocalGlassBackdrop] for its content,
 * and any glass in there quietly takes the no-blur fallback.
 */
@Composable
fun GlassBackdropSource(
    backdrop: GlassBackdrop,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Box(modifier = modifier.then(GlassBackdropSourceElement(backdrop))) {
        CompositionLocalProvider(LocalGlassBackdrop provides null) {
            content()
        }
    }
}

/** Makes [backdrop] what glass surfaces in [content] blur. Use it for chrome drawn above the source. */
@Composable
fun ProvideGlassBackdrop(
    backdrop: GlassBackdrop,
    content: @Composable () -> Unit,
) {
    CompositionLocalProvider(LocalGlassBackdrop provides backdrop, content = content)
}

private data class GlassBackdropSourceElement(
    val backdrop: GlassBackdrop,
) : ModifierNodeElement<GlassBackdropSourceNode>() {
    override fun create() = GlassBackdropSourceNode(backdrop)

    override fun update(node: GlassBackdropSourceNode) {
        node.updateBackdrop(backdrop)
    }

    override fun InspectorInfo.inspectableProperties() {
        name = "glassBackdropSource"
    }
}

private class GlassBackdropSourceNode(
    private var backdrop: GlassBackdrop,
) : Modifier.Node(),
    DrawModifierNode,
    GlobalPositionAwareModifierNode,
    CompositionLocalConsumerModifierNode {
    private var layer: GraphicsLayer? = null

    fun updateBackdrop(newBackdrop: GlassBackdrop) {
        if (newBackdrop === backdrop) return
        backdrop.layer = null
        backdrop = newBackdrop
        backdrop.layer = layer
    }

    override fun onDetach() {
        releaseLayer()
    }

    override fun onGloballyPositioned(coordinates: LayoutCoordinates) {
        backdrop.positionInWindow = coordinates.positionInWindow()
    }

    override fun ContentDrawScope.draw() {
        if (!currentValueOf(LocalTempoGlass).blurEnabled) {
            // Nothing will sample it, so do not hold a layer for nobody.
            releaseLayer()
            drawContent()
            return
        }
        val target = layer ?: requireGraphicsContext().createGraphicsLayer().also { layer = it }
        if (backdrop.layer !== target) backdrop.layer = target
        target.record { this@draw.drawContent() }
        drawLayer(target)
    }

    private fun releaseLayer() {
        layer?.let { requireGraphicsContext().releaseGraphicsLayer(it) }
        layer = null
        if (backdrop.layer != null) backdrop.layer = null
    }
}
