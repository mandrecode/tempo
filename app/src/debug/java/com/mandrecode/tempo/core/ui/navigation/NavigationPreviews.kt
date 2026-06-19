package com.mandrecode.tempo.core.ui.navigation

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mandrecode.tempo.R
import com.mandrecode.tempo.core.ui.theme.TempoTheme

private const val LANDSCAPE_DEVICE = "spec:parent=pixel_9,orientation=landscape"

/**
 * Preview-only stand-in for [TempoBottomNavigation], which can't be previewed directly because it
 * requires a live NavController + NavigationPreferencesRepository. This mirrors the production pill
 * (surfaceContainer Surface, secondaryContainer selected item) for the "both tabs" scenarios.
 */
@Composable
private fun NavBarPillPreviewStub(
    routinesSelected: Boolean,
    isLandscape: Boolean,
) {
    Surface(
        shape = RoundedCornerShape(36.dp),
        color = MaterialTheme.colorScheme.surfaceContainer,
    ) {
        if (isLandscape) {
            Column(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(FloatingToolbarItemSpacing),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                NavBarPillItems(routinesSelected)
            }
        } else {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(FloatingToolbarItemSpacing, Alignment.CenterHorizontally),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                NavBarPillItems(routinesSelected)
            }
        }
    }
}

@Composable
private fun NavBarPillItems(routinesSelected: Boolean) {
    NavBarPillItem(
        selectedIcon = R.drawable.ic_routine,
        unselectedIcon = R.drawable.ic_routine_outlined,
        titleRes = R.string.routines,
        selected = routinesSelected,
    )
    NavBarPillItem(
        selectedIcon = R.drawable.ic_tasks,
        unselectedIcon = R.drawable.ic_tasks_outlined,
        titleRes = R.string.tasks,
        selected = !routinesSelected,
    )
}

@Composable
private fun NavBarPillItem(
    selectedIcon: Int,
    unselectedIcon: Int,
    titleRes: Int,
    selected: Boolean,
) {
    val iconRes = if (selected) selectedIcon else unselectedIcon
    if (selected) {
        FilledIconButton(
            onClick = {},
            modifier = Modifier.size(FloatingToolbarItemSize),
            shape = CircleShape,
            colors =
                IconButtonDefaults.filledIconButtonColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                ),
        ) {
            Icon(
                painter = painterResource(id = iconRes),
                contentDescription = stringResource(titleRes),
            )
        }
    } else {
        IconButton(
            onClick = {},
            modifier = Modifier.size(FloatingToolbarItemSize),
        ) {
            Icon(
                painter = painterResource(id = iconRes),
                contentDescription = stringResource(titleRes),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Preview(name = "Both tabs · portrait · light", showBackground = true, device = "id:pixel_9")
@Preview(
    name = "Both tabs · portrait · dark",
    showBackground = true,
    device = "id:pixel_9",
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Composable
private fun BothTabsPortraitPreview() {
    TempoTheme {
        TempoBottomRail(
            navigationContent = { NavBarPillPreviewStub(routinesSelected = false, isLandscape = false) },
            actionButton = {
                TempoBottomRailActionButton(
                    iconRes = R.drawable.ic_add,
                    contentDescription = stringResource(R.string.add_task),
                    onClick = {},
                )
            },
            soloActionContent = null,
            isLandscape = false,
        )
    }
}

@Preview(name = "Routines only · portrait · light", showBackground = true, device = "id:pixel_9")
@Preview(
    name = "Routines only · portrait · dark",
    showBackground = true,
    device = "id:pixel_9",
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Composable
private fun RoutinesOnlyPortraitPreview() {
    TempoTheme {
        TempoBottomRail(
            navigationContent = {},
            soloActionContent = {
                TempoSoloActionButton(
                    iconRes = R.drawable.ic_add,
                    label = stringResource(R.string.add_habit),
                    onClick = {},
                    expanded = true,
                )
            },
            isLandscape = false,
        )
    }
}

@Preview(name = "Tasks only · portrait · light", showBackground = true, device = "id:pixel_9")
@Preview(
    name = "Tasks only · portrait · dark",
    showBackground = true,
    device = "id:pixel_9",
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Composable
private fun TasksOnlyPortraitPreview() {
    TempoTheme {
        TempoBottomRail(
            navigationContent = {},
            soloActionContent = {
                TempoSoloActionButton(
                    iconRes = R.drawable.ic_add,
                    label = stringResource(R.string.add_task),
                    onClick = {},
                    expanded = true,
                )
            },
            isLandscape = false,
        )
    }
}

@Preview(name = "Tasks only · scrolled (collapsed) · light", showBackground = true, device = "id:pixel_9")
@Preview(
    name = "Tasks only · scrolled (collapsed) · dark",
    showBackground = true,
    device = "id:pixel_9",
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Composable
private fun TasksOnlyCollapsedPreview() {
    TempoTheme {
        TempoBottomRail(
            navigationContent = {},
            soloActionContent = {
                TempoSoloActionButton(
                    iconRes = R.drawable.ic_add,
                    label = stringResource(R.string.add_task),
                    onClick = {},
                    expanded = false,
                )
            },
            isLandscape = false,
        )
    }
}

@Preview(name = "Both tabs · landscape rail · light", showBackground = true, device = LANDSCAPE_DEVICE)
@Preview(
    name = "Both tabs · landscape rail · dark",
    showBackground = true,
    device = LANDSCAPE_DEVICE,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Composable
private fun BothTabsLandscapePreview() {
    TempoTheme {
        TempoBottomRail(
            navigationContent = { NavBarPillPreviewStub(routinesSelected = true, isLandscape = true) },
            actionButton = {
                TempoBottomRailActionButton(
                    iconRes = R.drawable.ic_add,
                    contentDescription = stringResource(R.string.add_habit),
                    onClick = {},
                )
            },
            soloActionContent = null,
            isLandscape = true,
        )
    }
}

@Preview(name = "Tasks only · landscape · light", showBackground = true, device = LANDSCAPE_DEVICE)
@Preview(
    name = "Tasks only · landscape · dark",
    showBackground = true,
    device = LANDSCAPE_DEVICE,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Composable
private fun TasksOnlyLandscapePreview() {
    TempoTheme {
        TempoBottomRail(
            navigationContent = {},
            soloActionContent = {
                TempoSoloActionButton(
                    iconRes = R.drawable.ic_add,
                    label = stringResource(R.string.add_task),
                    onClick = {},
                    expanded = true,
                )
            },
            isLandscape = true,
        )
    }
}
