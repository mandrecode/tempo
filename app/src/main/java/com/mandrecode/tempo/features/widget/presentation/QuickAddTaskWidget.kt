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
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import com.mandrecode.tempo.R

class QuickAddTaskWidget : GlanceAppWidget() {
    override val sizeMode = SizeMode.Single

    override suspend fun provideGlance(
        context: Context,
        id: GlanceId,
    ) {
        provideContent {
            GlanceTheme(colors = TempoGlanceColorScheme) {
                QuickAddTaskWidgetContent()
            }
        }
    }
}

@Composable
private fun QuickAddTaskWidgetContent() {
    val label = LocalContext.current.getString(R.string.widget_quick_add_task_label)

    Column(
        modifier =
            GlanceModifier
                .fillMaxSize()
                .background(GlanceTheme.colors.background)
                .clickable(actionStartActivity<QuickAddTaskActivity>())
                .padding(12.dp),
        horizontalAlignment = Alignment.Horizontal.CenterHorizontally,
        verticalAlignment = Alignment.Vertical.CenterVertically,
    ) {
        Image(
            provider = ImageProvider(R.drawable.ic_add_task),
            contentDescription = label,
            colorFilter = ColorFilter.tint(GlanceTheme.colors.primary),
            modifier = GlanceModifier.size(28.dp),
        )
        Text(
            text = label,
            style = TextStyle(color = GlanceTheme.colors.onBackground),
        )
    }
}
