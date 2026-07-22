package com.mandrecode.tempo.features.whatsnew.presentation.model

import androidx.annotation.StringRes

data class WhatsNewEntry(
    val id: String,
    @StringRes val titleRes: Int,
    @StringRes val descriptionRes: Int,
)
