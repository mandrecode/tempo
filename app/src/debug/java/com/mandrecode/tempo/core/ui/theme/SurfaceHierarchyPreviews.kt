package com.mandrecode.tempo.core.ui.theme

import android.content.res.Configuration
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Preview(name = "Light", device = "id:pixel_9")
@Composable
private fun SurfaceHierarchyLightPreview() {
    TempoTheme(
        darkTheme = false,
        dynamicColor = false,
        useTempoColors = true,
    ) {
        SurfaceHierarchyContent()
    }
}

@Preview(name = "Dark", device = "id:pixel_9", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun SurfaceHierarchyDarkPreview() {
    TempoTheme(
        darkTheme = true,
        dynamicColor = false,
        useTempoColors = true,
    ) {
        SurfaceHierarchyContent()
    }
}

@Composable
private fun SurfaceHierarchyContent() {
    val colorScheme = MaterialTheme.colorScheme

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = colorScheme.background,
        contentColor = colorScheme.onBackground,
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            PreviewSurfaceRole.entries.forEach { role ->
                val shape = RoundedCornerShape(24.dp)
                Surface(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .border(1.dp, colorScheme.outlineVariant, shape),
                    shape = shape,
                    color = role.containerColor(colorScheme),
                    contentColor = role.contentColor(colorScheme),
                ) {
                    Text(
                        text = role.name,
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 24.dp),
                    )
                }
            }
        }
    }
}

private enum class PreviewSurfaceRole {
    Background,
    Surface,
    ContainerLow,
    Container,
    ContainerHigh,
    ContainerHighest,
    ;

    fun containerColor(colorScheme: ColorScheme): Color =
        when (this) {
            Background -> colorScheme.background
            Surface -> colorScheme.surface
            ContainerLow -> colorScheme.surfaceContainerLow
            Container -> colorScheme.surfaceContainer
            ContainerHigh -> colorScheme.surfaceContainerHigh
            ContainerHighest -> colorScheme.surfaceContainerHighest
        }

    fun contentColor(colorScheme: ColorScheme): Color =
        when (this) {
            Background -> colorScheme.onBackground
            else -> colorScheme.onSurface
        }
}
