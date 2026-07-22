package com.mandrecode.tempo.core.ui.editor

import androidx.compose.material3.TextFieldColors
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/** Transparent-container styling shared by the title/description text fields in editor sheets. */
@Composable
internal fun editorTransparentTextFieldColors(): TextFieldColors =
    TextFieldDefaults.colors(
        focusedContainerColor = Color.Transparent,
        unfocusedContainerColor = Color.Transparent,
        focusedIndicatorColor = Color.Transparent,
        unfocusedIndicatorColor = Color.Transparent,
        errorIndicatorColor = Color.Transparent,
        errorContainerColor = Color.Transparent,
    )
