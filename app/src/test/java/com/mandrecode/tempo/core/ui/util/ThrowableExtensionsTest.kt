package com.mandrecode.tempo.core.ui.util

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class ThrowableExtensionsTest {
    @Test
    fun `uses the localized message when present`() {
        val error =
            object : IllegalStateException("raw") {
                override fun getLocalizedMessage(): String = "localized"
            }
        assertThat(error.toUserFacingMessage()).isEqualTo("localized")
    }

    @Test
    fun `falls back to the message when the localized message is blank`() {
        val error =
            object : IllegalStateException("raw") {
                override fun getLocalizedMessage(): String = " "
            }
        assertThat(error.toUserFacingMessage()).isEqualTo("raw")
    }

    @Test
    fun `falls back to the class simple name when both messages are blank`() {
        assertThat(IllegalStateException(" ").toUserFacingMessage()).isEqualTo("IllegalStateException")
    }

    @Test
    fun `falls back to the class simple name when there is no message`() {
        assertThat(IllegalStateException().toUserFacingMessage()).isEqualTo("IllegalStateException")
    }

    @Test
    fun `falls back to the class name for anonymous throwables without a message`() {
        val error = object : RuntimeException() {}
        assertThat(error.toUserFacingMessage()).isEqualTo(error.javaClass.name)
    }
}
