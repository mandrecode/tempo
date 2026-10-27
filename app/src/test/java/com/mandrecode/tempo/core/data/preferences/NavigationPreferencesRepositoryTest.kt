package com.mandrecode.tempo.core.data.preferences

import android.content.Context
import android.content.SharedPreferences
import com.google.common.truth.Truth.assertThat
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import org.junit.Test

class NavigationPreferencesRepositoryTest {
    private lateinit var mockEditor: SharedPreferences.Editor
    private lateinit var mockPrefs: SharedPreferences

    private fun createRepository(): NavigationPreferencesRepository {
        mockEditor =
            mockk<SharedPreferences.Editor>(relaxed = true) {
                every { putString(any(), any()) } returns this
                every { apply() } just Runs
            }
        mockPrefs =
            mockk {
                every { edit() } returns mockEditor
                every { getString("last_route", null) } returns null
            }
        val mockContext =
            mockk<Context> {
                every { getSharedPreferences(any(), any()) } returns mockPrefs
            }
        return NavigationPreferencesRepositoryImpl(mockContext)
    }

    @Test
    fun `saveAndGetLastRoute`() {
        val repository = createRepository()
        every { mockPrefs.getString("last_route", null) } returns "settings"

        repository.saveLastRoute("settings")

        assertThat(repository.getLastRoute()).isEqualTo("settings")
        verify(exactly = 1) { mockEditor.putString("last_route", "settings") }
        verify { mockEditor.apply() }
    }

    @Test
    fun `getLastRouteReturnsNullByDefault`() {
        assertThat(createRepository().getLastRoute()).isNull()
    }
}
