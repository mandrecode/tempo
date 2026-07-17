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

SOURCE_HEIGHT = 2558  # The XR emulator's native square capture size.
CROP_TOP = 620
# 2558 isn't evenly divisible by 16, so this is the closest integer-height
# 16:9 crop at full width (2558 / 1439 = 1.7776..., vs 16/9 = 1.7778...),
# not a mathematically exact ratio.
CROP_HEIGHT = 1439


def main():
    if len(sys.argv) != 2:
        print(__doc__, file=sys.stderr)
        sys.exit(1)
    directory = Path(sys.argv[1])
    for path in sorted(directory.glob("*.png")):
        with Image.open(path) as im:
            im.load()
            w, h = im.size
            if h == CROP_HEIGHT:
                # Already cropped (e.g. from an earlier locale's run into
                # the same directory) — skip so re-running is safe.
                continue
            if h != SOURCE_HEIGHT:
                raise RuntimeError(
                    f"{path} is {w}x{h}, expected a {SOURCE_HEIGHT}-tall square XR "
                    f"capture or an already-cropped {CROP_HEIGHT}-tall image. Refusing "
                    "to guess a crop window for an unexpected size.",
                )
            cropped = im.crop((0, CROP_TOP, w, CROP_TOP + CROP_HEIGHT))
        cropped.save(path)
        print(f"Cropped {path} to {w}x{CROP_HEIGHT}")


if __name__ == "__main__":
    main()
