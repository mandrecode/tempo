package com.mandrecode.tempo.features.widget.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import dagger.hilt.android.AndroidEntryPoint

/**
 * Transparent trampoline launched from the home-screen quick-add-task widget. Hosts only the
 * quick-add Compose surface; the rest of the window stays transparent so the previous screen
 * shows through behind the dimmed dialog scrim.
 */
@AndroidEntryPoint
class QuickAddTaskActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            QuickAddTaskScreen(onClose = { finish() })
        }
    }
}
