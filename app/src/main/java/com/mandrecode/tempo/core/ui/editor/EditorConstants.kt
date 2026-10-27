package com.mandrecode.tempo.core.ui.editor

import androidx.compose.ui.unit.dp

internal const val EDITOR_MAX_TITLE_LENGTH = 65
internal const val EDITOR_AUTO_SAVE_DEBOUNCE_MS = 350L
internal val EDITOR_PROPERTY_ROW_GAP = 20.dp

/** The height of a row's control (chips, selectors, pills), so every row reads the same size. */
internal val EDITOR_CONTROL_HEIGHT = 48.dp

/**
 * The corner radius of an item that has been set — a reminder chip, an attached file. Squarer than
 * the 24dp pill of the Add button it replaces: in this app a smaller radius says "it's marked", as
 * on a completed card or a selected chip.
 */
internal val EDITOR_SET_ITEM_CORNER_RADIUS = 16.dp

/**
 * The empty space a borderless text field keeps below its text. A gap after one is shortened by
 * this, so the space between its text and the next row's control matches the gap between controls.
 */
internal val EDITOR_TEXT_FIELD_BOTTOM_INSET = 16.dp
internal val EDITOR_DELETE_BUTTON_CORNER_RADIUS = 24.dp
