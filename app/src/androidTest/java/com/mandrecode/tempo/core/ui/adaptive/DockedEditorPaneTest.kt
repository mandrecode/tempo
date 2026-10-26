package com.mandrecode.tempo.core.ui.adaptive

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.unit.dp
import com.google.common.truth.Truth.assertThat
import com.mandrecode.tempo.core.ui.theme.TempoTheme
import com.mandrecode.tempo.features.tasks.domain.model.Category
import com.mandrecode.tempo.features.tasks.presentation.TASKS_CONTENT_BLOCK_TEST_TAG
import com.mandrecode.tempo.features.tasks.presentation.TasksContent
import com.mandrecode.tempo.features.tasks.presentation.TasksContract
import kotlinx.collections.immutable.persistentListOf
import org.junit.Rule
import org.junit.Test

/**
 * The docked editor's surface has to start where the list's rounded content block does, and sit a
 * gutter away from it, or the two white surfaces merge at the seam or look staggered. The block's
 * top depends on the measured header above it, which the pane only learns a frame later — so the
 * compose preview screenshots cannot pin this, and it is checked here instead.
 */
class DockedEditorPaneTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun dockedPane_startsLevelWithTheContentBlock_aGutterAway() {
        composeTestRule.setContent {
            TempoTheme {
                var contentBlockTop by remember { mutableStateOf(0.dp) }
                Row(modifier = Modifier.fillMaxSize()) {
                    Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
                        TasksContent(
                            uiState =
                                TasksContract.UiState(
                                    categories =
                                        persistentListOf(
                                            Category(id = 1L, name = "Inbox", icon = "inbox", isDefault = true),
                                        ),
                                    isLoading = false,
                                    selectedCategoryId = 1L,
                                ),
                            onEvent = {},
                            onContentBlockTopChange = { contentBlockTop = it },
                        )
                    }
                    DockedEditorPane(visible = true, contentBlockTop = contentBlockTop) {
                        Box(modifier = Modifier.fillMaxSize().testTag(PANE_TEST_TAG))
                    }
                }
            }
        }
        composeTestRule.waitForIdle()

        val block = composeTestRule.onNodeWithTag(TASKS_CONTENT_BLOCK_TEST_TAG).getUnclippedBoundsInRoot()
        val pane = composeTestRule.onNodeWithTag(PANE_TEST_TAG).getUnclippedBoundsInRoot()

        assertThat(block.top.value).isGreaterThan(0f)
        assertThat(pane.top.value).isWithin(0.5f).of(block.top.value)
        assertThat((pane.left - block.right).value).isWithin(0.5f).of(DockedEditorPadding.value)
        assertThat(pane.bottom.value).isWithin(0.5f).of(block.bottom.value)
    }

    private companion object {
        const val PANE_TEST_TAG = "docked_editor_pane"
    }
}
