package com.mandrecode.tempo.core.ui.glass

import androidx.compose.foundation.BorderStroke
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawOutline
import androidx.compose.ui.graphics.drawscope.ContentDrawScope
import androidx.compose.ui.node.CompositionLocalConsumerModifierNode
import androidx.compose.ui.node.DrawModifierNode
import androidx.compose.ui.node.ModifierNodeElement
import androidx.compose.ui.node.currentValueOf
import androidx.compose.ui.node.invalidateDraw
import androidx.compose.ui.platform.InspectorInfo
import androidx.compose.ui.unit.LayoutDirection
import com.mandrecode.tempo.core.ui.theme.LocalTempoGlass

/**
 * A component's glass edge, for its `border`: none. On the two-tone page content components are set
 * apart by their own colours; only the floating chrome keeps an edge (drawn by [glass]).
 */
@Composable
@ReadOnlyComposable
fun glassRim(): BorderStroke = LocalTempoGlass.current.componentRim

/** A surface's fill: its own colour [solid], with the glass's touch of translucency. */
@Composable
@ReadOnlyComposable
fun glassCardColor(solid: Color): Color = solid.copy(alpha = solid.alpha * LocalTempoGlass.current.cardAlpha)

/** A secondary (outlined) button's fill: the cards' frost. */
@Composable
@ReadOnlyComposable
fun glassButtonContainer(): Color = LocalTempoGlass.current.cardFrost

/** A secondary (outlined) button's edge: the glass rim. */
@Composable
@ReadOnlyComposable
fun glassButtonBorder(): BorderStroke = LocalTempoGlass.current.componentRim

/**
 * The glass finish's sheen: a faint light across the upper half of [shape], laid over the
 * component so it catches its fill whatever colour that is. Put it last in a card's or surface's
 * modifier chain, with the component's own shape, next to a [glassRim] border.
 */
fun Modifier.glassSheen(shape: Shape): Modifier = this then GlassSheenElement(shape)

private data class GlassSheenElement(
    val shape: Shape,
) : ModifierNodeElement<GlassSheenNode>() {
    override fun create() = GlassSheenNode(shape)

    override fun update(node: GlassSheenNode) {
        if (node.shape != shape) {
            node.shape = shape
            node.outline = null
            node.invalidateDraw()
        }
    }

    override fun InspectorInfo.inspectableProperties() {
        name = "glassSheen"
        properties["shape"] = shape
    }
}

private class GlassSheenNode(
    var shape: Shape,
) : Modifier.Node(),
    DrawModifierNode,
    CompositionLocalConsumerModifierNode {
    var outline: Outline? = null
    private var outlineSize = Size.Unspecified
    private var outlineDirection: LayoutDirection? = null

    override fun ContentDrawScope.draw() {
        drawContent()
        val sheen = currentValueOf(LocalTempoGlass).componentSheen
        if (sheen.alpha == 0f) return
        val current =
            outline?.takeIf { outlineSize == size && outlineDirection == layoutDirection }
                ?: shape.createOutline(size, layoutDirection, this).also {
                    outline = it
                    outlineSize = size
                    outlineDirection = layoutDirection
                }
        drawOutline(
            outline = current,
            brush =
                Brush.verticalGradient(
                    0f to sheen,
                    FINISH_SHEEN_END to Color.Transparent,
                ),
        )
    }
}

/** Where the finish's sheen has faded out: the upper half, as on the chrome. */
private const val FINISH_SHEEN_END = 0.5f
