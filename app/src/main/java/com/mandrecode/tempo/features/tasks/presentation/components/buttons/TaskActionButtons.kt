package com.mandrecode.tempo.features.tasks.presentation.components.buttons

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.mandrecode.tempo.R
import com.mandrecode.tempo.core.ui.util.getIconForSortOption
import com.mandrecode.tempo.core.ui.util.rememberPressableIconButtonAnimation
import com.mandrecode.tempo.features.tasks.presentation.model.SortOption

@Composable
fun SortButton(
    sortOption: SortOption,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    expanded: Boolean = false,
) {
    val baseSortRadius = if (sortOption != SortOption.MANUAL) 16.dp else 48.dp
    val (sortInteractionSource, buttonCornerRadius) =
        rememberPressableIconButtonAnimation(
            baseRadius = baseSortRadius,
        )
    val (containerColor, contentColor) = sortButtonColors(sortOption)

    if (expanded) {
        ExtendedFloatingActionButton(
            text = { Text(stringResource(R.string.sort_by)) },
            icon = {
                Icon(
                    painter = painterResource(getIconForSortOption(sortOption)),
                    contentDescription = stringResource(R.string.sort_tasks_by_format, sortOption.value),
                )
            },
            onClick = onClick,
            modifier = modifier.then(Modifier.height(48.dp)),
            shape = RoundedCornerShape(buttonCornerRadius.value),
            containerColor = containerColor,
            contentColor = contentColor,
            elevation =
                FloatingActionButtonDefaults.bottomAppBarFabElevation(
                    defaultElevation = 0.dp,
                    pressedElevation = 0.dp,
                ),
            interactionSource = sortInteractionSource,
        )
        return
    }
    FloatingActionButton(
        onClick = onClick,
        modifier = modifier.then(Modifier.size(48.dp)),
        shape = RoundedCornerShape(buttonCornerRadius.value),
        containerColor = containerColor,
        contentColor = contentColor,
        elevation =
            FloatingActionButtonDefaults.bottomAppBarFabElevation(
                defaultElevation = 0.dp,
                pressedElevation = 0.dp,
            ),
        interactionSource = sortInteractionSource,
    ) {
        Icon(
            painter = painterResource(getIconForSortOption(sortOption)),
            contentDescription = stringResource(R.string.sort_tasks_by_format, sortOption.value),
        )
    }
}

@Composable
private fun sortButtonColors(sortOption: SortOption): Pair<Color, Color> {
    val isSorted = sortOption != SortOption.MANUAL
    val containerColor by animateColorAsState(
        targetValue =
            if (isSorted) {
                MaterialTheme.colorScheme.secondaryContainer
            } else {
                MaterialTheme.colorScheme.surfaceContainer
            },
        animationSpec = tween(300),
        label = "sort_button_container_color",
    )
    val contentColor by animateColorAsState(
        targetValue =
            if (isSorted) {
                MaterialTheme.colorScheme.onSecondaryContainer
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            },
        animationSpec = tween(300),
        label = "sort_button_content_color",
    )
    return containerColor to contentColor
}

@Composable
fun ClearCompletedButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    expanded: Boolean = false,
) {
    val (deleteInteractionSource, deleteCornerRadius) = rememberPressableIconButtonAnimation()

    if (expanded) {
        ExtendedFloatingActionButton(
            text = { Text(stringResource(R.string.clear_completed)) },
            icon = {
                Icon(
                    painter = painterResource(R.drawable.ic_remove_done),
                    contentDescription = stringResource(R.string.delete_all_completed_tasks),
                )
            },
            onClick = onClick,
            modifier = modifier.then(Modifier.height(48.dp)),
            shape = RoundedCornerShape(deleteCornerRadius.value),
            containerColor = MaterialTheme.colorScheme.error,
            contentColor = MaterialTheme.colorScheme.onError,
            elevation =
                FloatingActionButtonDefaults.bottomAppBarFabElevation(
                    defaultElevation = 0.dp,
                    pressedElevation = 0.dp,
                ),
            interactionSource = deleteInteractionSource,
        )
        return
    }
    FloatingActionButton(
        onClick = onClick,
        modifier = modifier.then(Modifier.size(48.dp)),
        shape = RoundedCornerShape(deleteCornerRadius.value),
        containerColor = MaterialTheme.colorScheme.error,
        contentColor = MaterialTheme.colorScheme.onError,
        elevation =
            FloatingActionButtonDefaults.bottomAppBarFabElevation(
                defaultElevation = 0.dp,
                pressedElevation = 0.dp,
            ),
        interactionSource = deleteInteractionSource,
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_remove_done),
            contentDescription = stringResource(R.string.delete_all_completed_tasks),
        )
    }
}
