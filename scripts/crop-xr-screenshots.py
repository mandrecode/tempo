#!/usr/bin/env python3
"""Crops XR_Headset screenshots to 16:9 for the Play Store Android XR
listing (GitHub issue #169). Not part of the CI/app build.

The XR emulator's screencap always captures the full square passthrough
scene (2558x2558) regardless of `adb shell wm size`, so square captures
must be cropped after the fact. The floating app window (window-chrome
pill + panel) sits at a fixed vertical position within that square
scene, roughly y=658 to y=2022 across all four canonical views, so a
single crop window works for the whole set.

Usage: crop-xr-screenshots.py <dir-with-xr-pngs>
  e.g. crop-xr-screenshots.py distribution/screenshots/xr
"""
import sys
from pathlib import Path

from PIL import Image

CROP_TOP = 620
CROP_HEIGHT = 1439  # 2558 * 9 / 16, giving an exact 16:9 crop at full width.


def main():
    if len(sys.argv) != 2:
        print(__doc__, file=sys.stderr)
        sys.exit(1)
    directory = Path(sys.argv[1])
    for path in sorted(directory.glob("*.png")):
        im = Image.open(path)
        w, _h = im.size
        im.crop((0, CROP_TOP, w, CROP_TOP + CROP_HEIGHT)).save(path)
        print(f"Cropped {path} to {w}x{CROP_HEIGHT}")


if __name__ == "__main__":
    main()
