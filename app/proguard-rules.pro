# ============================================
# ProGuard / R8 Rules — Tempo & Habits
# ============================================
# Keep this file minimal: every extra -keep reduces R8 optimization coverage,
# which Google Play measures (DEX optimization threshold). kotlinx-serialization,
# Hilt, Room, WorkManager and Compose ship their own consumer rules.

# --- Crash reports: keep file names and line numbers ---
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# --- kotlinx-datetime ---
-dontwarn kotlinx.datetime.**
