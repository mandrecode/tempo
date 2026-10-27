package com.mandrecode.tempo.core.ui.glass

import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Shader
import android.os.Build
import android.view.View
import android.view.ViewTreeObserver
import androidx.annotation.RequiresApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.isSpecified
import androidx.compose.ui.graphics.BlurEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.RenderEffect
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.addOutline
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.drawOutline
import androidx.compose.ui.graphics.drawscope.ContentDrawScope
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.isSpecified
import androidx.compose.ui.graphics.layer.GraphicsLayer
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.node.CompositionLocalConsumerModifierNode
import androidx.compose.ui.node.DrawModifierNode
import androidx.compose.ui.node.GlobalPositionAwareModifierNode
import androidx.compose.ui.node.ModifierNodeElement
import androidx.compose.ui.node.currentValueOf
import androidx.compose.ui.node.invalidateDraw
import androidx.compose.ui.node.requireGraphicsContext
import androidx.compose.ui.node.requireView
import androidx.compose.ui.platform.InspectorInfo
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.mandrecode.tempo.core.ui.theme.LocalTempoGlass
import com.mandrecode.tempo.core.ui.theme.TempoGlass
import kotlin.math.ceil

/**
 * Floating chrome glass: blurs what is behind [shape] (the [LocalGlassBackdrop] the chrome sits
 * over) and lays a fill on top. No outline: the chrome's own tone sets it apart.
 *
 * [tint] is the fill colour — neutral glass when unspecified, or a role colour (the add button's
 * `primaryContainer`) for tinted glass, which keeps its hue and loses only enough opacity to show
 * the blur. Where there is no blur (API < 31, previews, low-RAM, or no backdrop) the fill is
 * denser, so it stays legible over sharp content.
 *
 * Use it in place of a container colour: give the component a transparent container and put this on
 * its modifier, with the same shape.
 */
fun Modifier.glass(
    shape: Shape,
    tint: Color = Color.Unspecified,
): Modifier = this then GlassElement(shape = shape, tint = tint)

/**
 * The content block's material: [color], with a faint wash of the secondary tone down from its top
 * edge. No blur: what is behind a panel is the ambient gradient, already as soft as a blur would be.
 */
fun Modifier.glassPanel(
    shape: Shape,
    color: Color,
): Modifier = this then GlassPanelElement(shape = shape, color = color)

private data class GlassElement(
    val shape: Shape,
    val tint: Color,
) : ModifierNodeElement<GlassNode>() {
    override fun create() = GlassNode(shape, tint)

    override fun update(node: GlassNode) {
        node.shape = shape
        node.tint = tint
        node.invalidateDraw()
    }

    override fun InspectorInfo.inspectableProperties() {
        name = "glass"
        properties["shape"] = shape
        properties["tint"] = tint
    }
}

private class GlassNode(
    var shape: Shape,
    var tint: Color,
) : Modifier.Node(),
    DrawModifierNode,
    GlobalPositionAwareModifierNode,
    CompositionLocalConsumerModifierNode {
    private var positionInWindow = Offset.Unspecified
    private var blurredLastFrame = false
    private var observedView: View? = null
    private val clip = OutlineClip()
    private var frost: RenderEffect? = null
    private var frostRadius = Float.NaN

    /**
     * Content changing behind the glass (a list scrolling, a tab switching) does not redraw the
     * glass by itself. So, while it is blurring, the surface marks itself dirty just before each
     * frame the window draws anyway: inside that frame, so it costs a re-record rather than
     * scheduling frames of its own.
     */
    private val preDrawListener =
        ViewTreeObserver.OnPreDrawListener {
            if (blurredLastFrame) invalidateDraw()
            true
        }

    override fun onAttach() {
        observedView = requireView().also { it.viewTreeObserver.addOnPreDrawListener(preDrawListener) }
    }

    override fun onDetach() {
        observedView?.viewTreeObserver?.removeOnPreDrawListener(preDrawListener)
        observedView = null
    }

    override fun onGloballyPositioned(coordinates: LayoutCoordinates) {
        val position = coordinates.positionInWindow()
        if (position != positionInWindow) {
            positionInWindow = position
            invalidateDraw()
        }
    }

    override fun ContentDrawScope.draw() {
        val glass = currentValueOf(LocalTempoGlass)
        val backdrop = currentValueOf(LocalGlassBackdrop)
        val sourceLayer = backdrop?.layer?.takeIf { glass.blurEnabled && positionInWindow.isSpecified }
        val blurred = sourceLayer != null
        blurredLastFrame = blurred

        clipPath(clip.pathFor(shape, size, layoutDirection, this)) {
            if (sourceLayer != null) drawBlurredBackdrop(sourceLayer, backdrop.positionInWindow, glass)
            drawRect(fillColor(glass, blurred))
        }
        drawContent()
    }

    private fun fillColor(
        glass: TempoGlass,
        blurred: Boolean,
    ): Color =
        when {
            !tint.isSpecified -> if (blurred) glass.chromeTint else glass.chromeFallbackTint
            else -> tint.copy(alpha = tint.alpha * if (blurred) glass.tintedAlpha else glass.tintedFallbackAlpha)
        }

    /**
     * Records the slice of [source] behind this surface — padded by the blur radius, so the edges
     * blur in real content rather than clamped pixels — into a layer with the blur applied.
     *
     * A fresh layer every draw, released once drawn: a reused one keeps showing what was behind the
     * glass when it was first drawn, however often it is re-recorded — the renderer holds on to the
     * blurred output of a node it has already seen.
     */
    private fun DrawScope.drawBlurredBackdrop(
        source: GraphicsLayer,
        sourcePosition: Offset,
        glass: TempoGlass,
    ) {
        val radius = glass.blurRadius.toPx()
        val padding = ceil(radius * 2).toInt()
        val graphicsContext = requireGraphicsContext()
        val layer = graphicsContext.createGraphicsLayer()
        // The effect only depends on the radius, so it is built once rather than every frame.
        layer.renderEffect = frost.takeIf { frostRadius == radius } ?: frostedBlur(radius).also {
            frost = it
            frostRadius = radius
        }
        layer.topLeft = IntOffset(-padding, -padding)
        layer.record(
            size =
                IntSize(
                    width = size.width.toInt() + padding * 2,
                    height = size.height.toInt() + padding * 2,
                ),
        ) {
            val offset = sourcePosition - positionInWindow
            translate(offset.x + padding, offset.y + padding) {
                drawLayer(source)
            }
        }
        drawLayer(layer)
        graphicsContext.releaseGraphicsLayer(layer)
    }
}

private data class GlassPanelElement(
    val shape: Shape,
    val color: Color,
) : ModifierNodeElement<GlassPanelNode>() {
    override fun create() = GlassPanelNode(shape, color)

    override fun update(node: GlassPanelNode) {
        node.shape = shape
        node.color = color
        node.invalidateDraw()
    }

    override fun InspectorInfo.inspectableProperties() {
        name = "glassPanel"
        properties["shape"] = shape
        properties["color"] = color
    }
}

private class GlassPanelNode(
    var shape: Shape,
    var color: Color,
) : Modifier.Node(),
    DrawModifierNode,
    CompositionLocalConsumerModifierNode {
    override fun ContentDrawScope.draw() {
        val glass = currentValueOf(LocalTempoGlass)
        val outline = shape.createOutline(size, layoutDirection, this)
        drawOutline(outline, color.copy(alpha = color.alpha * glass.panelAlpha))
        if (glass.panelWash.alpha > 0f) {
            // The one colour inside the panel: a very smooth wash of the secondary tone from its top
            // edge, fading out well before the bottom, under the cards.
            drawOutline(outline, brush = topGlow(glass.panelWash, PanelWashExtent.toPx()))
        }
        drawContent()
    }
}

/** How far down the content panel its secondary wash reaches before it has faded out. */
private val PanelWashExtent = 520.dp

/**
 * The chrome's frost: a blur of what is behind it with the colour pushed up afterwards, the way a
 * CSS `backdrop-filter: blur() saturate()` frosts. A blur alone averages a busy backdrop toward
 * grey; the saturation brings back the colour the glass is picking up.
 */
private fun frostedBlur(radius: Float): RenderEffect =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        saturatedBlur(radius)
    } else {
        BlurEffect(radius, radius, TileMode.Clamp)
    }

@RequiresApi(Build.VERSION_CODES.S)
private fun saturatedBlur(radius: Float): RenderEffect {
    val blur = android.graphics.RenderEffect.createBlurEffect(radius, radius, Shader.TileMode.CLAMP)
    val saturation = ColorMatrix().apply { setSaturation(FROST_SATURATION) }
    return android.graphics.RenderEffect
        .createColorFilterEffect(ColorMatrixColorFilter(saturation), blur)
        .asComposeRenderEffect()
}

private const val FROST_SATURATION = 1.2f

/** A clip path for a shape, rebuilt only when the shape, size or direction changes. */
private class OutlineClip {
    private val path = Path()
    private var shape: Shape? = null
    private var size = Size.Unspecified
    private var direction: LayoutDirection? = null

    fun pathFor(
        shape: Shape,
        size: Size,
        direction: LayoutDirection,
        density: Density,
    ): Path {
        if (shape != this.shape || size != this.size || direction != this.direction) {
            path.reset()
            path.addOutline(shape.createOutline(size, direction, density))
            this.shape = shape
            this.size = size
            this.direction = direction
        }
        return path
    }
}
