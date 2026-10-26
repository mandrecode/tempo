package com.mandrecode.tempo.features.tasks.presentation.components.sections

import androidx.annotation.StringRes
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.mandrecode.tempo.R
import com.mandrecode.tempo.core.domain.util.ValidationUtils
import com.mandrecode.tempo.core.ui.theme.groupLabel
import com.mandrecode.tempo.core.ui.theme.sectionHeader

private const val COLLAPSED_CHEVRON_ROTATION = 0f
private const val EXPANDED_CHEVRON_ROTATION = 90f
private val SectionHeaderMinHeight = 48.dp

/**
 * The divider that introduces a section of a category (#61): collapse toggle, name and active-task
 * count, then a hairline that sets it apart from the wavy date dividers and the Completed pill, an
 * add-task action and the section menu. A collapsed section still shows its overdue tasks.
 */
@Composable
internal fun SectionHeader(
    name: String,
    taskCount: Int,
    overdueCount: Int,
    isCollapsed: Boolean,
    onToggleCollapse: () -> Unit,
    onAddTask: () -> Unit,
    onRename: () -> Unit,
    onReorderSections: () -> Unit,
    onAddSectionBelow: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
    isDropTarget: Boolean = false,
) {
    val headerShape = RoundedCornerShape(14.dp)

    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .heightIn(min = SectionHeaderMinHeight)
                .clip(headerShape)
                .background(
                    if (isDropTarget) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
                ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        SectionTitle(name, taskCount, isCollapsed, onToggleCollapse, headerShape)
        HorizontalDivider(
            modifier = Modifier.weight(1f).padding(horizontal = 8.dp),
            color = MaterialTheme.colorScheme.outlineVariant,
        )
        if (isCollapsed && overdueCount > 0) {
            OverdueBadge(overdueCount)
        }
        IconButton(onClick = onAddTask) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = stringResource(R.string.section_add_task_cd, name),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        SectionMenu(
            name = name,
            onRename = onRename,
            onReorderSections = onReorderSections,
            onAddSectionBelow = onAddSectionBelow,
            onDelete = onDelete,
        )
    }
}

/** Collapse toggle, name and count: one control announced as the section's heading. */
@Composable
private fun SectionTitle(
    name: String,
    taskCount: Int,
    isCollapsed: Boolean,
    onToggleCollapse: () -> Unit,
    shape: RoundedCornerShape,
) {
    val rotation by animateFloatAsState(
        targetValue = if (isCollapsed) COLLAPSED_CHEVRON_ROTATION else EXPANDED_CHEVRON_ROTATION,
        label = "sectionChevron",
    )
    val countDescription = pluralStringResource(R.plurals.tasks_count, taskCount, taskCount)
    val stateText =
        stringResource(if (isCollapsed) R.string.section_state_collapsed else R.string.section_state_expanded)
    Row(
        modifier =
            Modifier
                .heightIn(min = SectionHeaderMinHeight)
                .clip(shape)
                .semantics(mergeDescendants = true) {
                    heading()
                    contentDescription = "$name, $countDescription"
                    stateDescription = stateText
                }.clickable(role = Role.Button, onClick = onToggleCollapse)
                .padding(horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(22.dp).rotate(rotation),
        )
        Spacer(Modifier.width(6.dp))
        Text(
            text = name,
            style = MaterialTheme.typography.sectionHeader,
            color = MaterialTheme.colorScheme.primary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text = taskCount.toString(),
            style = MaterialTheme.typography.groupLabel,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun OverdueBadge(count: Int) {
    Surface(
        color = MaterialTheme.colorScheme.errorContainer,
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier.padding(end = 4.dp),
    ) {
        Text(
            text = pluralStringResource(R.plurals.section_overdue_count, count, count),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onErrorContainer,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
        )
    }
}

@Composable
private fun SectionMenu(
    name: String,
    onRename: () -> Unit,
    onReorderSections: () -> Unit,
    onAddSectionBelow: () -> Unit,
    onDelete: () -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        IconButton(onClick = { expanded = true }) {
            Icon(
                imageVector = Icons.Default.MoreVert,
                contentDescription = stringResource(R.string.section_options_cd, name),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            SectionMenuItem(R.string.section_rename, { Icon(Icons.Default.Edit, contentDescription = null) }) {
                expanded = false
                onRename()
            }
            SectionMenuItem(
                R.string.section_reorder,
                { Icon(painterResource(R.drawable.ic_drag_handle), contentDescription = null) },
            ) {
                expanded = false
                onReorderSections()
            }
            SectionMenuItem(R.string.section_add_below, { Icon(Icons.Default.Add, contentDescription = null) }) {
                expanded = false
                onAddSectionBelow()
            }
            HorizontalDivider()
            DropdownMenuItem(
                text = { Text(stringResource(R.string.section_delete), color = MaterialTheme.colorScheme.error) },
                leadingIcon = {
                    Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                },
                onClick = {
                    expanded = false
                    onDelete()
                },
            )
        }
    }
}

@Composable
private fun SectionMenuItem(
    @StringRes labelRes: Int,
    leadingIcon: @Composable () -> Unit,
    onClick: () -> Unit,
) {
    DropdownMenuItem(
        text = { Text(stringResource(labelRes)) },
        leadingIcon = leadingIcon,
        onClick = onClick,
    )
}

/**
 * An in-place name field, used both to rename a section and to name a new one. Saves on the IME
 * action or the check button; [onDismiss] discards.
 */
@Composable
internal fun SectionNameField(
    initialName: String,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var name by rememberSaveable { mutableStateOf(initialName) }
    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(Unit) { focusRequester.requestFocus() }
    val confirm = { if (name.isNotBlank()) onConfirm(name) else onDismiss() }

    Row(modifier = modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        TextField(
            value = name,
            onValueChange = { if (it.length <= ValidationUtils.MAX_CATEGORY_NAME_LENGTH) name = it },
            label = { Text(stringResource(R.string.section_name_label)) },
            singleLine = true,
            textStyle = MaterialTheme.typography.sectionHeader,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { confirm() }),
            colors =
                TextFieldDefaults.colors(
                    focusedTextColor = MaterialTheme.colorScheme.primary,
                    unfocusedTextColor = MaterialTheme.colorScheme.primary,
                ),
            modifier = Modifier.weight(1f).focusRequester(focusRequester),
        )
        IconButton(onClick = onDismiss) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = stringResource(R.string.cancel),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        IconButton(onClick = confirm) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = stringResource(R.string.section_save_name),
                tint = MaterialTheme.colorScheme.primary,
            )
        }
    }
}

/** What an expanded section with no active tasks shows: an invitation to add or drop one. */
@Composable
internal fun EmptySectionPlaceholder(
    modifier: Modifier = Modifier,
    isDropTarget: Boolean = false,
) {
    Surface(
        modifier = modifier.fillMaxWidth().height(52.dp),
        shape = RoundedCornerShape(24.dp),
        color = if (isDropTarget) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
        border =
            BorderStroke(
                1.5.dp,
                if (isDropTarget) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
            ),
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = stringResource(R.string.section_empty_placeholder),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

/** The end-of-list action that starts naming a new section. */
@Composable
internal fun AddSectionButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(modifier = modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Start) {
        TextButton(onClick = onClick) {
            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(8.dp))
            Text(stringResource(R.string.section_add))
        }
    }
}
