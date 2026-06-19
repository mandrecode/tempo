# ============================================
# ProGuard / R8 Rules — Tempo & Habits
# ============================================

# --- Crash reports: keep file names and line numbers ---
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# --- kotlinx-serialization ---
# Required for Navigation 3 @Serializable routes & domain models
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt

-keepclassmembers class kotlinx.serialization.json.** {
    *** Companion;
}
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# Keep generated serializers for all project classes
-keep,includedescriptorclasses class com.mandredotdev.tempo.**$$serializer { *; }
-keepclassmembers class com.mandredotdev.tempo.** {
    *** Companion;
}
-keepclasseswithmembers class com.mandredotdev.tempo.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# --- kotlinx-datetime ---
-dontwarn kotlinx.datetime.**