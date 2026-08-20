package com.mandrecode.tempo.util

import com.mandrecode.tempo.BuildConfig

data class AppVersionInfo(
    val versionName: String,
    val versionCode: Int,
    val commitSha: String = "",
) {
    val displayName: String
        get() {
            val normalizedCommitSha = commitSha.trim()
            return if (normalizedCommitSha.isBlank()) versionName else "$versionName ($normalizedCommitSha)"
        }
}

interface AppVersionProvider {
    fun getVersionInfo(): AppVersionInfo
}

class AppVersionProviderImpl : AppVersionProvider {
    override fun getVersionInfo(): AppVersionInfo =
        AppVersionInfo(
            versionName = BuildConfig.VERSION_NAME,
            versionCode = BuildConfig.VERSION_CODE,
            commitSha = BuildConfig.COMMIT_SHA,
        )
}
