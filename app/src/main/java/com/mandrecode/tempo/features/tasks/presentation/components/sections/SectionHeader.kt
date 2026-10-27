package com.mandrecode.tempo.features.tasks.presentation.components.sections

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.mandrecode.tempo.R
import com.mandrecode.tempo.core.domain.util.ValidationUtils
import com.mandrecode.tempo.core.ui.theme.groupLabel
import com.mandrecode.tempo.core.ui.theme.sectionHeader

private const val COLLAPSED_CHEVRON_ROTATION = 0f
private const val EXPANDED_CHEVRON_ROTATION = 90f
private const val EMPTY_PLACEHOLDER_ALPHA = 0.6f
private const val DRAFT_CHEVRON_ALPHA = 0.4f
private const val PLACEHOLDER_ALPHA = 0.45f
private val SectionHeaderMinHeight = 48.dp
private val SectionNameMaxWidth = 220.dp

/** The leading slot, as wide as the trailing menu button, so both ends of a header sit alike. */
private val SectionEdgeSlot = 48.dp

/**
 * The divider that introduces a section of a category (#61): collapse toggle, name and active-task
 * count, then a hairline that sets it apart from the wavy date dividers and the Completed pill, an
 * add-task action and the section menu. A collapsed section still shows its overdue tasks.
 *
 * The chevron sits centred in a [SectionEdgeSlot] at the start, mirroring the menu button at the
 * end, and the hairline keeps the same gap to the count as to the add button's glyph.
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
        // 12dp on both sides: the title keeps 12dp after the count, the add button its own 12dp
        // around the glyph.
        HorizontalDivider(
            modifier = Modifier.weight(1f),
            color = MaterialTheme.colorScheme.outlineVariant,
        )
        if (isCollapsed && overdueCount > 0) {
            OverdueBadge(overdueCount)
        }
        IconButton(onClick = onAddTask) {
            Icon(
                painter = painterResource(R.drawable.ic_add),
                contentDescription = stringResource(R.string.section_add_task_cd, name),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp),
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
                // Room after the count inside the toggle, so its ripple does not end on the number.
                .padding(end = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(modifier = Modifier.size(SectionEdgeSlot), contentAlignment = Alignment.Center) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(22.dp).rotate(rotation),
            )
        }
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
        modifier = Modifier.padding(start = 8.dp),
    ) {
        Text(
            text = pluralStringResource(R.plurals.section_overdue_count, count, count),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onErrorContainer,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
        )
    }
}

/** The section's options, drawn exactly like the category chip's menu. */
@Composable
private fun SectionMenu(
    name: String,
    onRename: () -> Unit,
    onReorderSections: () -> Unit,
    onAddSectionBelow: () -> Unit,
    onDelete: () -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    val haptic = LocalHapticFeedback.current
    val choose: (() -> Unit) -> Unit = { action ->
        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        action()
        expanded = false
    }
    Box {
        IconButton(onClick = { expanded = true }) {
            Icon(
                imageVector = Icons.Default.MoreVert,
                contentDescription = stringResource(R.string.section_options_cd, name),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            shape = RoundedCornerShape(16.dp),
        ) {
            SectionMenuItem(R.string.section_rename, R.drawable.ic_edit) { choose(onRename) }
            SectionMenuItem(R.string.section_reorder, R.drawable.ic_drag_handle) { choose(onReorderSections) }
            SectionMenuItem(R.string.section_add_below, R.drawable.ic_add_row_below) { choose(onAddSectionBelow) }
            SectionMenuItem(R.string.section_delete, R.drawable.ic_delete_forever, isDestructive = true) {
                choose(onDelete)
            }
        }
    }
}

@Composable
private fun SectionMenuItem(
    @StringRes labelRes: Int,
    @DrawableRes iconRes: Int,
    isDestructive: Boolean = false,
    onClick: () -> Unit,
) {
    val tint = if (isDestructive) MaterialTheme.colorScheme.error else LocalContentColor.current
    DropdownMenuItem(
        text = {
            Text(
                text = stringResource(labelRes),
                color = if (isDestructive) MaterialTheme.colorScheme.error else Color.Unspecified,
            )
        },
        leadingIcon = {
            Icon(
                painter = painterResource(iconRes),
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = tint,
            )
        },
        onClick = onClick,
    )
}

/**
 * An in-place name field, used both to rename a section and to name a new one. Drawn as the header
 * it becomes — chevron slot, name in the header style, hairline — with cancel and save where the
 * header keeps its add and menu buttons, so naming already looks like the result. Saves on the
 * keyboard's Done or the check button; [onDismiss] discards.
 *
 * Plain [remember], not saveable: the list item for a new section keeps one key, and a saveable
 * draft would come back pre-filled with the last name the next time a section is added.
 */
@Composable
internal fun SectionNameField(
    initialName: String,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var name by remember(initialName) { mutableStateOf(initialName) }
    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(Unit) { focusRequester.requestFocus() }
    val confirm = { if (name.isNotBlank()) onConfirm(name) else onDismiss() }

    Row(
        modifier = modifier.fillMaxWidth().heightIn(min = SectionHeaderMinHeight),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(modifier = Modifier.size(SectionEdgeSlot), contentAlignment = Alignment.Center) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary.copy(alpha = DRAFT_CHEVRON_ALPHA),
                modifier = Modifier.size(22.dp).rotate(EXPANDED_CHEVRON_ROTATION),
            )
        }
        SectionNameInput(
            name = name,
            onNameChange = { name = it },
            onDone = confirm,
            // Sized to the name (or the placeholder), not a text field's minimum width, so the
            // hairline follows the text exactly as it will in the header.
            // Capped rather than weighted: the hairline alone takes the rest of the row, keeping
            // cancel and save exactly where the header's add and menu buttons sit.
            modifier =
                Modifier
                    .widthIn(max = SectionNameMaxWidth)
                    .width(IntrinsicSize.Max)
                    .focusRequester(focusRequester),
        )
        HorizontalDivider(
            modifier = Modifier.weight(1f).padding(start = 12.dp),
            color = MaterialTheme.colorScheme.outlineVariant,
        )
        IconButton(onClick = onDismiss) {
            Icon(
                painter = painterResource(R.drawable.ic_close),
                contentDescription = stringResource(R.string.cancel),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp),
            )
        }
        IconButton(onClick = confirm) {
            // The plain rounded check, from the same Material Symbols family and at the same size
            // as the cancel mark beside it (ic_check is the circled one, for notifications).
            Icon(
                painter = painterResource(R.drawable.ic_done),
                contentDescription = stringResource(R.string.section_save_name),
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp),
            )
        }
    }
}

/** The borderless name input, styled as the header name it becomes. */
@Composable
private fun SectionNameInput(
    name: String,
    onNameChange: (String) -> Unit,
    onDone: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val label = stringResource(R.string.section_name_label)
    BasicTextField(
        value = name,
        onValueChange = { if (it.length <= ValidationUtils.MAX_CATEGORY_NAME_LENGTH) onNameChange(it) },
        singleLine = true,
        textStyle = MaterialTheme.typography.sectionHeader.copy(color = MaterialTheme.colorScheme.primary),
        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
        keyboardOptions =
            KeyboardOptions(capitalization = KeyboardCapitalization.Sentences, imeAction = ImeAction.Done),
        keyboardActions = KeyboardActions(onDone = { onDone() }),
        modifier = modifier.semantics { contentDescription = label },
        decorationBox = { innerTextField ->
            Box(contentAlignment = Alignment.CenterStart) {
                if (name.isEmpty()) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.sectionHeader,
                        // The section colour, greyed: it previews the name without competing with it.
                        color = MaterialTheme.colorScheme.primary.copy(alpha = PLACEHOLDER_ALPHA),
                        maxLines = 1,
                        softWrap = false,
                    )
                }
                innerTextField()
            }
        },
    )
}

/**
 * What an expanded section with no active tasks shows: a quiet invitation to add or drop one.
 * A soft tint rather than an outline, so empty sections do not draw lines across the list; it
 * only lights up while a dragged task is over it.
 */
@Composable
internal fun EmptySectionPlaceholder(
    modifier: Modifier = Modifier,
    isDropTarget: Boolean = false,
) {
    Surface(
        modifier = modifier.fillMaxWidth().heightIn(min = 48.dp),
        shape = RoundedCornerShape(24.dp),
        color =
            if (isDropTarget) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.background.copy(alpha = EMPTY_PLACEHOLDER_ALPHA)
            },
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(vertical = 14.dp)) {
            Text(
                text = stringResource(R.string.section_empty_placeholder),
                style = MaterialTheme.typography.bodySmall,
                color =
                    if (isDropTarget) {
                        MaterialTheme.colorScheme.onPrimaryContainer
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
            )
        }
    }
}

/** The end-of-list action that starts naming a new section; its "+" lines up with the chevrons. */
@Composable
internal fun AddSectionButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(modifier = modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Start) {
        TextButton(onClick = onClick, contentPadding = PaddingValues(start = 14.dp, end = 16.dp)) {
            Icon(painterResource(R.drawable.ic_add), contentDescription = null, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(10.dp))
            Text(stringResource(R.string.section_add))
        }
    }
}
