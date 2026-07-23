package com.mandrecode.tempo.features.widget.presentation

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.glance.ColorFilter
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalContext
import androidx.glance.action.ActionParameters
import androidx.glance.action.actionParametersOf
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.padding
import androidx.glance.layout.size
import com.mandrecode.tempo.MainActivity
import com.mandrecode.tempo.R
import com.mandrecode.tempo.util.supportsDynamicColor

class QuickAddTaskWidget : GlanceAppWidget() {
    override val sizeMode = SizeMode.Single

    override suspend fun provideGlance(
        context: Context,
        id: GlanceId,
    ) {
        provideContent {
            QuickAddTaskWidgetContent()
        }
    }

    companion object {
        // Read by MainActivity.handleIntent() to open the existing task-creation sheet
        // instead of the app's default start destination.
        const val EXTRA_OPEN_NEW_TASK_DIALOG = "OPEN_NEW_TASK_DIALOG"
        val openNewTaskDialogKey = ActionParameters.Key<Boolean>(EXTRA_OPEN_NEW_TASK_DIALOG)
    }
}

@Composable
private fun QuickAddTaskWidgetContent() {
    // GlanceTheme.colors (Glance's own default, read here rather than passed implicitly) is
    // itself built from day/night- and dynamic-color-aware Android resources, so it needs no
    // resolving on our side — unlike TempoGlanceColorScheme.kt's static bridge, it already
    // switches automatically on a system light/dark or wallpaper change.
    val colors =
        if (shouldUseTempoStaticColors(supportsDynamicColor)) {
            TempoGlanceColorScheme
        } else {
            GlanceTheme.colors
        }

    GlanceTheme(colors = colors) {
        val label = LocalContext.current.getString(R.string.widget_quick_add_task_label)

        Box(
            modifier =
                GlanceModifier
                    .fillMaxSize()
                    .background(GlanceTheme.colors.background)
                    .clickable(
                        actionStartActivity<MainActivity>(
                            parameters = actionParametersOf(QuickAddTaskWidget.openNewTaskDialogKey to true),
                        ),
                    ).padding(12.dp),
            contentAlignment = Alignment.Center,
        ) {
            Image(
                provider = ImageProvider(R.drawable.ic_add_task),
                contentDescription = label,
                colorFilter = ColorFilter.tint(GlanceTheme.colors.primary),
                modifier = GlanceModifier.size(40.dp),
            )
        }
    }
}
