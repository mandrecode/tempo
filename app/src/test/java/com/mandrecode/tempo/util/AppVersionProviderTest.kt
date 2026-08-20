package com.mandrecode.tempo.util

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class AppVersionProviderTest {
    @Test
    fun `display name combines version and commit sha`() {
        val versionInfo = AppVersionInfo(versionName = "1.2.3", versionCode = 1, commitSha = "au4b6i")

        assertThat(versionInfo.displayName).isEqualTo("1.2.3 (au4b6i)")
    }

    @Test
    fun `missing commit sha is omitted from display name`() {
        val versionInfo = AppVersionInfo(versionName = "1.2.3", versionCode = 1)

        assertThat(versionInfo.displayName).isEqualTo("1.2.3")
    }
}
