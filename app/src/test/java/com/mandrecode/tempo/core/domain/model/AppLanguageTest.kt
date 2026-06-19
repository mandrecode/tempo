package com.mandrecode.tempo.core.domain.model

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class AppLanguageTest {
    @Test
    fun `fromString returns SYSTEM for valid SYSTEM string`() {
        assertThat(AppLanguage.fromString("SYSTEM")).isEqualTo(AppLanguage.SYSTEM)
    }

    @Test
    fun `fromString returns ENGLISH for valid ENGLISH string`() {
        assertThat(AppLanguage.fromString("ENGLISH")).isEqualTo(AppLanguage.ENGLISH)
    }

    @Test
    fun `fromString returns SYSTEM for invalid string`() {
        assertThat(AppLanguage.fromString("FRENCH")).isEqualTo(AppLanguage.SYSTEM)
    }

    @Test
    fun `fromString returns SYSTEM for empty string`() {
        assertThat(AppLanguage.fromString("")).isEqualTo(AppLanguage.SYSTEM)
    }

    @Test
    fun `fromString is case sensitive`() {
        assertThat(AppLanguage.fromString("english")).isEqualTo(AppLanguage.SYSTEM)
    }
}
