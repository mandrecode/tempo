package com.mandrecode.tempo.screenshots

import android.content.res.Configuration
import androidx.compose.ui.tooling.preview.Preview

/**
 * The three window widths that bracket the app's adaptive breakpoints, in light and dark.
 *
 * - [PHONE_SPEC] is below every breakpoint: bottom navigation, editors as bottom sheets.
 * - [FOLDABLE_SPEC] is past the navigation-rail breakpoint but below the docking one, so the
 *   floating rail is showing while editors are still bottom sheets.
 * - [TABLET_SPEC] is past `sheetPlacement`'s 1200dp large-window breakpoint, so editors dock
 *   beside live content and the rail expands. This is the width that shipped broken in #366.
 *
 * All three are pinned at `dpi=160` (1dp == 1px). Layout regressions are dp-shaped, so this
 * loses no sensitivity, and it keeps the committed reference images roughly seven times
 * smaller than they would be at a phone's real 420dpi.
 */
internal const val PHONE_SPEC = "spec:width=411dp,height=891dp,dpi=160"
internal const val FOLDABLE_SPEC = "spec:width=673dp,height=841dp,dpi=160"
internal const val TABLET_SPEC = "spec:width=1280dp,height=800dp,dpi=160"

@Preview(name = "phone", device = PHONE_SPEC, showBackground = true)
@Preview(
    name = "phone-dark",
    device = PHONE_SPEC,
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Preview(name = "foldable", device = FOLDABLE_SPEC, showBackground = true)
@Preview(
    name = "foldable-dark",
    device = FOLDABLE_SPEC,
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Preview(name = "tablet", device = TABLET_SPEC, showBackground = true)
@Preview(
    name = "tablet-dark",
    device = TABLET_SPEC,
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
annotation class PreviewAdaptiveFormFactors
