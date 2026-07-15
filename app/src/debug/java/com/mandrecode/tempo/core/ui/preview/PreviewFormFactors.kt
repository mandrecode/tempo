package com.mandrecode.tempo.core.ui.preview

import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview

/**
 * Multipreview covering the major form factors, per the Android adaptive guidance:
 * compact phone (portrait and landscape), unfolded foldable, tablet, and desktop window.
 */
@Preview(name = "Phone", device = Devices.PHONE, showBackground = true)
@Preview(
    name = "Phone landscape",
    device = "spec:width=891dp,height=411dp,dpi=420",
    showBackground = true,
)
@Preview(name = "Foldable", device = Devices.FOLDABLE, showBackground = true)
@Preview(name = "Tablet", device = Devices.TABLET, showBackground = true)
@Preview(name = "Desktop", device = Devices.DESKTOP, showBackground = true)
annotation class PreviewFormFactors
