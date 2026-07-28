package com.mandrecode.tempo.features.routines.presentation.components

import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.mandrecode.tempo.R

private val BadgeSize = 28.dp

/** Palm marking the Routines title while vacation mode is pausing every habit. */
@Composable
internal fun VacationModeTitleBadge(modifier: Modifier = Modifier) {
    Icon(
        painter = painterResource(R.drawable.ic_beach_access),
        contentDescription = stringResource(R.string.vacation_mode_active),
        tint = MaterialTheme.colorScheme.primary,
        modifier = modifier.size(BadgeSize),
    )
}
