package com.mandrecode.tempo.util

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import androidx.annotation.ChecksSdkIntAtLeast
import androidx.annotation.RequiresApi
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme

/**
 * Lint-aware SDK gate for Material 3 dynamic color (introduced in API 31, "S").
 *
 * The `@ChecksSdkIntAtLeast` annotation tells the Android lint flow analyzer
 * that any branch guarded by this property may safely call API 31+ symbols.
 * Lint's flow analysis does not always infer this when the same check is
 * written inline as `Build.VERSION.SDK_INT >= Build.VERSION_CODES.S` inside
 * a `when` arm or across a lambda boundary (e.g. `remember { ... }`).
 */
@get:ChecksSdkIntAtLeast(api = Build.VERSION_CODES.S)
val supportsDynamicColor: Boolean
    get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S

/**
 * Returns the platform's dynamic [ColorScheme]. Caller MUST gate on
 * [supportsDynamicColor] (enforced by `@RequiresApi`); the lint suppression
 * inside this function is safe because the API contract pushes the runtime
 * check to the caller.
 */
@RequiresApi(Build.VERSION_CODES.S)
@SuppressLint("NewApi")
fun dynamicColorScheme(
    context: Context,
    isDark: Boolean,
): ColorScheme = if (isDark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
