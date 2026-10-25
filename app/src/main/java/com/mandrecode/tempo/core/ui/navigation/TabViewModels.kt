package com.mandrecode.tempo.core.ui.navigation

import androidx.activity.compose.LocalActivity
import androidx.compose.runtime.Composable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.viewModel

/**
 * A top-level tab's view model, held by the activity rather than by the tab's navigation entry
 * (#438).
 *
 * The activity creates these as soon as the database is ready (see `MainActivity`), while the
 * user is still on the first tab, so each tab's first data is already loaded by the time it is
 * opened: the tab's first frame is the finished screen, not its bare background with the content
 * arriving a moment later. A tab's entry lives as long as the activity anyway — the tabs keep
 * their back stacks — so holding the view model there changes when it is created, not how long
 * it lives.
 *
 * Outside an activity that owns view models (previews, tests) it falls back to the usual owner.
 */
@Composable
inline fun <reified VM : ViewModel> tabViewModel(): VM {
    val owner = LocalActivity.current as? ViewModelStoreOwner ?: checkNotNull(LocalViewModelStoreOwner.current)
    return viewModel(viewModelStoreOwner = owner)
}
