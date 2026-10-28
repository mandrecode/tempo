#!/usr/bin/env python3
"""Composes the designed Play Store listing images from the raw captures in
distribution/screenshots/<form-factor>/ (GitHub issue #181). Not part of the
CI/app build.

Each listing image is an HTML page — headline, supporting line, feature chips,
the raw capture framed in a device — rendered to PNG by headless Chrome at the
exact pixel size Play Console wants for that form factor. The font is the app's
own Google Sans Flex (dialed the way Type.kt dials it) and the palette is the
Tempo brand palette from Color.kt, so the listing reads as the same product the
screenshots show.

The five images per locale tell the same story as the raw set (Focus today, a
session running, Routines, Tasks) and close on privacy. Light and dark captures
alternate so the carousel shows both themes without repeating a screen.

Usage: compose-store-screenshots.py [--chrome PATH] [--only phone,medium,...]
                                    [--locale en|es] [--keep-html DIR]
Output: distribution/screenshots/play-store/<form-factor>/<locale>_<n>_<view>.png
"""
import argparse
import html
import math
import shutil
import subprocess
import sys
import tempfile
from pathlib import Path

REPO = Path(__file__).resolve().parent.parent
RAW = REPO / "distribution" / "screenshots"
OUT = RAW / "play-store"
FONT = REPO / "app" / "src" / "main" / "res" / "font" / "google_sans_flex_variable.ttf"
ICON = REPO / "app" / "src" / "main" / "res" / "mipmap-xxxhdpi" / "ic_launcher_round.webp"

DEFAULT_CHROME = "/Applications/Google Chrome.app/Contents/MacOS/Google Chrome"

# Canvas sizes. Play Console accepts 16:9 or 9:16 for every listing type; phone
# and portrait tablet go 9:16 so the device can fill the frame, the wide form
# factors go 16:9. All sides stay inside Play's 320/720/1080–3840/7680 bounds.
FORM_FACTORS = {
    "phone":    {"size": (1080, 1920), "layout": "portrait",  "device": "phone"},
    "medium":   {"size": (1440, 2560), "layout": "portrait",  "device": "tablet"},
    "expanded": {"size": (2560, 1440), "layout": "landscape", "device": "tablet"},
    "desktop":  {"size": (2560, 1440), "layout": "landscape", "device": "monitor"},
    "xr":       {"size": (2560, 1440), "layout": "landscape", "device": "spatial"},
}

# (raw view suffix, theme of the capture used). The privacy slide shows Focus in
# both themes side by side, so it lists two.
SLIDES = [
    ("1_focus_today", ["light"]),
    ("2_focus_session", ["dark"]),
    ("3_routines_today", ["light"]),
    ("4_tasks_personal", ["dark"]),
    ("5_private", ["light", "dark"]),
]
SLIDE_RAW = {"5_private": "1_focus_today"}

# Copy. `<em>` marks the part of the headline that gets the accent colour and
# the wavy underline — the app's own wavy-divider motif.
COPY = {
    "en": [
        {"eyebrow": "Focus", "title": "Your whole day, <em>on one screen</em>",
         "body": "Tasks, habits and routines due now, with what to do next up front.",
         "chips": ["Up next, picked for you", "Nothing overdue slips by"]},
        {"eyebrow": "Focus sessions", "title": "One tap to <em>deep focus</em>",
         "body": "Start a timer on anything. The countdown follows you around the app.",
         "chips": ["25-minute sessions", "Pause or finish in one tap"]},
        {"eyebrow": "Routines", "title": "Chain habits into <em>routines</em>",
         "body": "Link habits that flow one into the next, and watch your streaks grow.",
         "chips": ["Habits to build or quit", "Habit chains"]},
        {"eyebrow": "Tasks", "title": "To-dos that <em>stay sorted</em>",
         "body": "Categories, sections, priorities and due dates, captured in seconds.",
         "chips": ["Priorities & due dates", "Categories & sections"]},
        {"eyebrow": "Privacy", "title": "Private, offline, <em>yours</em>",
         "body": "Encrypted on your device. No accounts, no ads, no tracking.",
         "chips": ["Encrypted at rest", "Works offline", "No ads", "Light & dark"]},
    ],
    "es": [
        {"eyebrow": "Enfoque", "title": "Todo tu día, <em>en una pantalla</em>",
         "body": "Tareas, hábitos y rutinas de hoy, con lo siguiente siempre a mano.",
         "chips": ["Lo siguiente, elegido por ti", "Nada vencido se escapa"]},
        {"eyebrow": "Sesiones de enfoque", "title": "Un toque y a <em>concentrarte</em>",
         "body": "Pon un temporizador a lo que sea. La cuenta atrás te acompaña por toda la app.",
         "chips": ["Sesiones de 25 minutos", "Pausa o termina con un toque"]},
        {"eyebrow": "Rutinas", "title": "Encadena hábitos en <em>rutinas</em>",
         "body": "Une hábitos que fluyen uno tras otro y mira crecer tus rachas.",
         "chips": ["Hábitos a crear o a dejar", "Cadenas de hábitos"]},
        {"eyebrow": "Tareas", "title": "Tus pendientes, <em>en orden</em>",
         "body": "Categorías, secciones, prioridades y fechas, anotadas en segundos.",
         "chips": ["Prioridades y fechas", "Categorías y secciones"]},
        {"eyebrow": "Privacidad", "title": "Privada, sin conexión, <em>tuya</em>",
         "body": "Cifrada en tu dispositivo. Sin cuentas, sin anuncios, sin rastreo.",
         "chips": ["Cifrado en reposo", "Funciona sin conexión", "Sin anuncios", "Claro y oscuro"]},
    ],
}

# Tempo brand palette (Color.kt). A light slide sits on the primary container
# fading to the light background; a dark one on the dark background with a glow
# of the brand primary.
THEMES = {
    "light": {
        "bg": "radial-gradient(120% 70% at 15% 0%, #9EF2E4 0%, rgba(158,242,228,0) 60%),"
              "radial-gradient(90% 60% at 100% 100%, #CCE5FF 0%, rgba(204,229,255,0) 70%),"
              "linear-gradient(180deg, #CCE8E3 0%, #F4FBF8 100%)",
        "eyebrow": "#006A60", "title": "#00201C", "accent": "#006A60", "body": "#3F4947",
        "wave": "rgba(0,106,96,0.16)", "chip_bg": "#FFFFFF", "chip_fg": "#005048",
        "chip_dot": "#9EF2E4", "chip_border": "rgba(0,106,96,0.12)",
        "shadow": "0 40px 90px rgba(0,56,50,0.28), 0 12px 30px rgba(0,56,50,0.16)",
        "ring": "rgba(0,106,96,0.10)",
    },
    "dark": {
        "bg": "radial-gradient(110% 65% at 85% 0%, rgba(0,106,96,0.75) 0%, rgba(0,106,96,0) 65%),"
              "radial-gradient(80% 50% at 0% 100%, rgba(70,97,121,0.55) 0%, rgba(70,97,121,0) 70%),"
              "linear-gradient(180deg, #00201C 0%, #0E1513 100%)",
        "eyebrow": "#82D5C8", "title": "#ECF2EF", "accent": "#9EF2E4", "body": "#BEC9C6",
        "wave": "rgba(130,213,200,0.18)", "chip_bg": "#1A2B28", "chip_fg": "#CCE8E3",
        "chip_dot": "#005048", "chip_border": "rgba(130,213,200,0.22)",
        "shadow": "0 40px 100px rgba(0,0,0,0.55), 0 0 0 1px rgba(130,213,200,0.10)",
        "ring": "rgba(130,213,200,0.10)",
    },
}


def wave_path(width, amplitude, wavelength, phase=0.0):
    pts = []
    for x in range(0, int(width) + 1, 6):
        y = amplitude * math.sin(2 * math.pi * x / wavelength + phase)
        pts.append(f"{x},{y + amplitude:.1f}")
    return "M" + " L".join(pts)


def device_html(kind, img_uri, width, extra_style=""):
    """A device frame `width` CSS px wide around one raw capture."""
    if kind == "phone":
        bezel, radius = width * 0.028, width * 0.12
        return (f'<div class="dev" style="width:{width}px;padding:{bezel}px;border-radius:{radius}px;'
                f'background:#121816;{extra_style}"><img src="{img_uri}" '
                f'style="border-radius:{radius - bezel}px"></div>')
    if kind == "tablet":
        bezel, radius = width * 0.022, width * 0.05
        return (f'<div class="dev" style="width:{width}px;padding:{bezel}px;border-radius:{radius}px;'
                f'background:#121816;{extra_style}"><img src="{img_uri}" '
                f'style="border-radius:{radius - bezel}px"></div>')
    if kind == "monitor":
        bezel, radius = width * 0.014, width * 0.018
        return (f'<div class="mon" style="width:{width}px;{extra_style}">'
                f'<div class="dev" style="padding:{bezel}px;border-radius:{radius}px;background:#121816">'
                f'<img src="{img_uri}" style="border-radius:{radius * 0.4}px"></div>'
                f'<div class="neck" style="width:{width * 0.12}px;height:{width * 0.07}px"></div>'
                f'<div class="foot" style="width:{width * 0.28}px;height:{width * 0.012}px"></div></div>')
    # spatial: the XR capture is already a floating panel in a passthrough scene,
    # so it gets no bezel — just rounded corners and a glow, like a window in space.
    radius = width * 0.03
    return (f'<div class="dev spatial" style="width:{width}px;border-radius:{radius}px;{extra_style}">'
            f'<img src="{img_uri}" style="border-radius:{radius}px"></div>')


CHECK = ('<svg viewBox="0 0 24 24"><path d="M5 12.5l4.5 4.5L19 7.5" fill="none" '
         'stroke="currentColor" stroke-width="3" stroke-linecap="round" stroke-linejoin="round"/></svg>')


def chips_html(chips, cls="", styles=None):
    out = []
    for i, c in enumerate(chips):
        st = styles[i] if styles else ""
        out.append(f'<div class="chip {cls}" style="{st}"><span class="dot">{CHECK}</span>'
                   f'{html.escape(c)}</div>')
    return "".join(out)


def build_page(ff, locale, idx, raw_paths):
    spec = FORM_FACTORS[ff]
    W, H = spec["size"]
    slide_id, themes = SLIDES[idx]
    # The duo slide carries both themes; it sits on dark so the light phone pops.
    theme = THEMES["dark" if len(themes) > 1 else themes[0]]
    copy = COPY[locale][idx]
    portrait = spec["layout"] == "portrait"
    u = W / 1080 if portrait else H / 1440  # scale unit

    title_size = (92 if portrait else 84) * u
    body_size = (38 if portrait else 40) * u
    eyebrow_size = (30 if portrait else 30) * u
    chip_size = (30 if portrait else 32) * u

    uris = [p.as_uri() for p in raw_paths]
    is_duo = len(uris) == 2
    kind = spec["device"]

    # --- device placement ---
    if portrait:
        dev_w = W * (0.70 if kind == "phone" else 0.84)
        top = 640 * u
        if is_duo:
            w2 = dev_w * 0.82
            devices = (
                device_html(kind, uris[0], w2,
                            f"position:absolute;left:{W * 0.06}px;top:{top + 80 * u}px;transform:rotate(-6deg)")
                + device_html(kind, uris[1], w2,
                              f"position:absolute;right:{W * 0.06}px;top:{top + 20 * u}px;transform:rotate(5deg)")
            )
            chip_block = f'<div class="chipgrid" style="top:{top - 40 * u}px">{chips_html(copy["chips"])}</div>'
            # the duo pushes its chips above the devices, so move devices down
            devices = devices.replace(f"top:{top + 80 * u}px", f"top:{top + 260 * u}px").replace(
                f"top:{top + 20 * u}px", f"top:{top + 200 * u}px")
        else:
            devices = device_html(kind, uris[0], dev_w,
                                  f"position:absolute;left:{(W - dev_w) / 2}px;top:{top}px")
            # Chips float over the lower part of the device, staggered so they read
            # as call-outs. Low down because that is where every capture has the
            # least to say — the top of each screen is the part worth seeing.
            # A tablet's taller screen leaves more empty room at the bottom, so its
            # chips can sit lower still.
            low, high = (110, 250) if kind == "phone" else (60, 190)
            c1 = f"position:absolute;left:{W * 0.05}px;bottom:{high * u}px;transform:rotate(-2deg)"
            c2 = f"position:absolute;right:{W * 0.05}px;bottom:{low * u}px;transform:rotate(2deg)"
            chip_block = chips_html(copy["chips"], "float", [c1, c2])
        text_style = f"left:{70 * u}px;right:{70 * u}px;top:{110 * u}px;text-align:center;align-items:center"
    else:
        text_w = 1100 * u
        dev_left = 1140 * u
        avail = W - dev_left - 90 * u
        if kind == "monitor":
            dev_w = avail
            dev_h = dev_w * (1080 / 1920) + dev_w * 0.028 + dev_w * 0.082
        elif kind == "spatial":
            dev_w = avail
            dev_h = dev_w * (1439 / 2558)
        else:
            # The tablet bleeds off the right edge: contained, it leaves a band of
            # empty background above and below and reads small next to the text.
            dev_w = avail * 1.18
            dev_h = dev_w * (1600 / 2560) + dev_w * 0.044
        dev_top = (H - dev_h) / 2
        if is_duo:
            w2 = avail * 0.80
            h2 = dev_h * w2 / dev_w
            devices = (
                device_html(kind, uris[0], w2,
                            f"position:absolute;left:{dev_left}px;top:{dev_top - 30 * u}px")
                + device_html(kind, uris[1], w2,
                              f"position:absolute;left:{dev_left + avail - w2}px;top:{dev_top + dev_h - h2 + 30 * u}px")
            )
        else:
            devices = device_html(kind, uris[0], dev_w,
                                  f"position:absolute;left:{dev_left}px;top:{dev_top}px")
        chip_block = ""
        text_style = (f"left:{130 * u}px;width:{text_w - 130 * u}px;top:0;bottom:0;"
                      f"justify-content:center;align-items:flex-start;text-align:left")

    chips_inline = "" if portrait else f'<div class="chiplist">{chips_html(copy["chips"])}</div>'
    brand = ""
    if idx == 0:
        brand = (f'<div class="brand"><img src="{ICON.as_uri()}"><span>Tempo</span></div>')

    waves = "".join(
        f'<path d="{wave_path(W + 40, a * u, wl * u, ph)}" transform="translate(-20,{y * H})" />'
        for (a, wl, ph, y) in [(14, 220, 0.0, 0.30), (10, 180, 1.4, 0.62), (16, 260, 2.6, 0.86)]
    )

    t = theme
    return f"""<!doctype html><html><head><meta charset="utf-8"><style>
@font-face {{ font-family: 'GSF'; src: url('{FONT.as_uri()}'); font-weight: 1 1000; font-stretch: 25% 151%; }}
* {{ box-sizing: border-box; margin: 0; padding: 0; }}
html, body {{ width: {W}px; height: {H}px; overflow: hidden; }}
body {{ background: {t['bg']}; font-family: 'GSF', sans-serif; position: relative;
  font-variation-settings: 'ROND' 100, 'opsz' 18; -webkit-font-smoothing: antialiased; }}
svg.waves {{ position: absolute; inset: 0; width: {W}px; height: {H}px; }}
svg.waves path {{ fill: none; stroke: {t['wave']}; stroke-width: {5 * u}px; stroke-linecap: round; }}
.ring {{ position: absolute; border-radius: 50%; border: {34 * u}px solid {t['ring']}; }}
.text {{ position: absolute; display: flex; flex-direction: column; gap: {26 * u}px; z-index: 5; }}
.brand {{ display: flex; align-items: center; gap: {16 * u}px; margin-bottom: {4 * u}px; }}
.brand img {{ width: {64 * u}px; height: {64 * u}px; border-radius: 50%; box-shadow: 0 6px 18px rgba(0,0,0,.18); }}
.brand span {{ font-size: {40 * u}px; color: {t['title']};
  font-variation-settings: 'ROND' 100, 'wght' 800, 'wdth' 120, 'opsz' 48; }}
.eyebrow {{ font-size: {eyebrow_size}px; letter-spacing: .14em; text-transform: uppercase; color: {t['eyebrow']};
  font-variation-settings: 'ROND' 100, 'wght' 750, 'wdth' 110, 'opsz' 24; }}
h1 {{ font-size: {title_size}px; line-height: 1.04; margin-bottom: {0.12 * title_size}px; color: {t['title']}; letter-spacing: -0.01em;
  font-variation-settings: 'ROND' 100, 'wght' 850, 'wdth' {118 if portrait else 108}, 'opsz' 144; text-wrap: {'balance' if portrait else 'wrap'}; }}
h1 em {{ font-style: normal; color: {t['accent']}; text-decoration: underline wavy {t['accent']}88;
  text-decoration-thickness: {0.045 * title_size}px; text-underline-offset: {0.2 * title_size}px;
  text-decoration-skip-ink: none; white-space: {'nowrap' if portrait else 'normal'}; display: {'inline' if portrait else 'block'}; }}
p.body {{ font-size: {body_size}px; line-height: 1.32; color: {t['body']}; max-width: {(860 if portrait else 640) * u}px;
  font-variation-settings: 'ROND' 100, 'wght' 450, 'wdth' 100, 'opsz' 24; text-wrap: pretty; }}
.dev {{ box-shadow: {t['shadow']}; z-index: 2; }}
.dev img {{ display: block; width: 100%; }}
.spatial {{ overflow: hidden; box-shadow: 0 0 0 {3 * u}px rgba(158,242,228,.55), 0 30px 120px rgba(0,106,96,.55), 0 20px 60px rgba(0,0,0,.5); }}
.mon {{ display: flex; flex-direction: column; align-items: center; z-index: 2; }}
.mon .dev {{ width: 100%; }}
.neck {{ background: linear-gradient(180deg, #2A3230, #3A4441); }}
.foot {{ background: #3A4441; border-radius: 999px 999px 8px 8px; box-shadow: {t['shadow']}; }}
.chip {{ display: inline-flex; align-items: center; gap: {14 * u}px; padding: {18 * u}px {30 * u}px {18 * u}px {18 * u}px;
  border-radius: 999px; background: {t['chip_bg']}; color: {t['chip_fg']}; font-size: {chip_size}px; white-space: nowrap;
  border: {2 * u}px solid {t['chip_border']}; box-shadow: 0 {14 * u}px {36 * u}px rgba(0,0,0,.18);
  font-variation-settings: 'ROND' 100, 'wght' 650, 'wdth' 104, 'opsz' 24; z-index: 6; }}
.chip .dot {{ width: {44 * u}px; height: {44 * u}px; border-radius: 50%; background: {t['chip_dot']}; color: {t['chip_fg']};
  display: inline-flex; align-items: center; justify-content: center; flex: none; }}
.chip .dot svg {{ width: 62%; height: 62%; }}
.chiplist {{ display: flex; flex-direction: column; align-items: flex-start; gap: {18 * u}px; margin-top: {22 * u}px; }}
.chipgrid {{ position: absolute; left: 0; right: 0; display: flex; flex-wrap: wrap; justify-content: center;
  gap: {18 * u}px; padding: 0 {60 * u}px; z-index: 6; }}
</style></head><body>
<svg class="waves" viewBox="0 0 {W} {H}">{waves}</svg>
<div class="ring" style="width:{(900 if portrait else 1100) * u}px;height:{(900 if portrait else 1100) * u}px;
  right:-{(320 if portrait else 200) * u}px;top:{(460 if portrait else -260) * u}px"></div>
<div class="text" style="{text_style}">
  {brand}
  <div class="eyebrow">{html.escape(copy['eyebrow'])}</div>
  <h1>{copy['title']}</h1>
  <p class="body">{html.escape(copy['body'])}</p>
  {chips_inline}
</div>
{devices}
{chip_block}
</body></html>"""


def render(chrome, html_path, png_path, size):
    W, H = size
    subprocess.run(
        [chrome, "--headless=new", "--disable-gpu", "--hide-scrollbars", "--force-device-scale-factor=1",
         "--allow-file-access-from-files", f"--window-size={W},{H}", "--default-background-color=00000000",
         "--virtual-time-budget=3000", f"--screenshot={png_path}", html_path.as_uri()],
        check=True, capture_output=True,
    )


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument("--chrome", default=DEFAULT_CHROME)
    ap.add_argument("--only", default=",".join(FORM_FACTORS))
    ap.add_argument("--locale", choices=["en", "es"], action="append")
    ap.add_argument("--keep-html", type=Path)
    args = ap.parse_args()
    if not Path(args.chrome).exists() and not shutil.which(args.chrome):
        sys.exit(f"Chrome not found at {args.chrome} — pass --chrome")
    locales = args.locale or ["en", "es"]
    work = args.keep_html or Path(tempfile.mkdtemp())
    work.mkdir(parents=True, exist_ok=True)
    for ff in args.only.split(","):
        out_dir = OUT / ff
        out_dir.mkdir(parents=True, exist_ok=True)
        for locale in locales:
            for idx, (slide_id, themes) in enumerate(SLIDES):
                raw_view = SLIDE_RAW.get(slide_id, slide_id)
                raws = [RAW / ff / f"{ff}_{locale}_{th}_{raw_view}.png" for th in themes]
                missing = [r for r in raws if not r.exists()]
                if missing:
                    print(f"skip {ff} {locale} {slide_id}: missing {missing[0].name}")
                    continue
                page = work / f"{ff}_{locale}_{slide_id}.html"
                page.write_text(build_page(ff, locale, idx, raws), encoding="utf-8")
                png = out_dir / f"{locale}_{slide_id}.png"
                render(args.chrome, page, png, FORM_FACTORS[ff]["size"])
                print(png.relative_to(REPO))


if __name__ == "__main__":
    main()
