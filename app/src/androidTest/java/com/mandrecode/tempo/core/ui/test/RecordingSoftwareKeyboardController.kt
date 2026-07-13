package com.mandrecode.tempo.core.ui.test

import androidx.compose.ui.platform.SoftwareKeyboardController

class RecordingSoftwareKeyboardController : SoftwareKeyboardController {
    var hideCalls: Int = 0
        private set

    override fun show() = Unit

    override fun hide() {
        hideCalls++
    }
}
