package com.mandrecode.tempo.features.routines.presentation.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.mandrecode.tempo.R

internal const val VACATION_MODE_NOTICE_TEST_TAG = "vacation_mode_notice"

private val NoticeShape = RoundedCornerShape(16.dp)
private val NoticeIconSize = 20.dp

/**
 * Explains what an active pause actually does, shown in the habit editor right above the
 * reminder and history rows it changes the meaning of — reminders stay silent, and a skipped
 * day no longer breaks the streak.
 */
@Composable
internal fun VacationModeNotice(modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.fillMaxWidth().testTag(VACATION_MODE_NOTICE_TEST_TAG),
        shape = NoticeShape,
        color = MaterialTheme.colorScheme.secondaryContainer,
        contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_beach_access),
                contentDescription = null,
                modifier = Modifier.size(NoticeIconSize),
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = stringResource(R.string.vacation_mode_notice),
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}
