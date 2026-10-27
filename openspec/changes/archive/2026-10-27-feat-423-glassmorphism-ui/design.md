# Design

## Principle: glass is for the layer that floats

Glass is used where something sits *above* the content: the floating bar and rail, modal sheets and dialogs. Content (cards, text, inputs) stays opaque. Frosted content cards look good in a mock-up and fail in use: their contrast changes with whatever scrolls behind them. The page gets a soft ambient backdrop so the glass has colour to pick up, and the content block is only slightly translucent over it.

## Material tokens (`TempoGlass`)

`tempoGlass(colorScheme, darkTheme, blurEnabled)` derives every value from the final colour scheme, so the Tempo palette, dynamic colour and both themes get a material that matches them. It is provided by `TempoTheme` as `LocalTempoGlass` (a static local, since it changes only with the theme).

| Token | Light | Dark | Why |
|:---|:---|:---|:---|
| chrome fill (blurred) | `surfaceContainerLowest` @ 62 % | `surfaceContainerHighest` @ 58 % | enough of the blur shows through to read as glass; icons keep contrast |
| chrome fill (no blur) | @ 95 % | @ 94 % | over sharp content a thin fill would hurt legibility |
| tinted chrome | role colour @ 80 % / 97 % | @ 78 % / 96 % | keeps the add button's identity |
| rim | white 95 % → 30 % | white 22 % → 4 % | a light edge catching the top |
| panel | `surface` @ 80 % + top rim | @ 82 % | the ambient glows through faintly |
| sheet (window blurred) | `surfaceContainerHigh` @ 88 % | @ 86 % | |
| blur radius | 28 dp | 28 dp | |

`blurEnabled` is false below API 31, in edit mode (layoutlib cannot render it, so screenshot references show the fallback) and on low-RAM devices.

## Ambient backdrop

`Modifier.ambientBackground()` paints `background` plus three radial washes (primary container top-start, tertiary container upper end, secondary container low start), sized from the page's longer side so phones and tablets get the same composition. It is opaque, so the tab crossfade still blends two identical pictures (the reason the root stays `surface`, see `HorizontalInsetMarginStrips`).

It is painted **once per page, at the page root**, and everything inside is transparent. The glows are laid out against the bounds they are drawn in, so a second copy on an inset Scaffold or a header box would not line up. A window-anchored version was considered and rejected: Settings slides in as an opaque overlay, and a backdrop fixed to the window would make its leading edge invisible during the slide, a wipe with no edge.

## In-window backdrop blur

There is no backdrop-filter in Compose, so the chrome builds one:

1. `GlassBackdropSource` records the content plane (margin strips, `NavDisplay` and the slide overlays) into a `GraphicsLayer` as it draws, and draws that layer instead of the content, so the recording is free.
2. The floating bar is drawn above it, under `ProvideGlassBackdrop`. `Modifier.glass(shape, tint)` draws, clipped to its shape:
   - the source layer, translated by the difference of the two window positions and padded by twice the blur radius so the edges blur real pixels, recorded into a layer with `BlurEffect`;
   - the fill, then a sheen over the upper half;
   - the content, then the rim (above the content, so a selected tab touching the edge does not hide it).
3. Glass inside the source would record a layer that contains itself. `GlassBackdropSource` provides a `null` backdrop to its content, and any glass in there uses the fallback fill.

Two renderer details, both found on device:

- **A fresh blur layer per draw.** A reused layer kept showing whatever was behind the glass when it was first drawn, however often it was re-recorded. The pill showed the Focus tab's empty panel while sitting over the Tasks list. The layer is created and released each draw.
- **Redraw on pre-draw.** Content changing behind the glass (a scroll, a tab switch) does not redraw the floating bar. Each glass surface registers a `ViewTreeObserver.OnPreDrawListener` and invalidates itself while it is blurring. This runs inside a frame that is already being drawn, so it never schedules frames of its own; `gfxinfo` reports 0 frames over 4 idle seconds.

## Modal glass

Sheets and alerts are separate windows, so they use the platform's cross-window blur (`FLAG_BLUR_BEHIND` + `blurBehindRadius`), which the compositor does on the GPU. `rememberWindowBlurBehind(visible)`:

- follows `addCrossWindowBlurEnabledListener`, because battery saver or the developer setting can disable blurs while the dialog is open;
- eases the radius with the standard motion duration, out again when the sheet starts dismissing;
- returns whether the blur is showing, so the sheet becomes translucent (no shadow, which would show through as a grey cast), the scrim drops to half and the dialog dim to 55 %. Otherwise everything stays as before.

`DialogWindowGlass()` wraps this for `AlertDialog`s. It is called from the title slot because that slot runs inside the dialog window.

## Alternatives considered

- **Haze**: the de facto library for this, with the same technique underneath. It needs approval under `TECH_STACK.md`, and the app needs only a fraction of it.
- **`Modifier.blur` on the content when a modal opens**: blurs the sheet too (sheets are drawn from inside the screens) and has no fallback signal. The window blur is cheaper and correct.
- **Glass cards**: rejected for legibility (see the principle above).
