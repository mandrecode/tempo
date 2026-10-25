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
 * Each is held under its own [tabViewModelKey], never the default key: other screens outside the
 * tabs' navigation entries (the Focus session overlay and the task editor it opens) resolve their
 * own view models of the same classes against the activity, and must keep getting separate ones.
 *
 * Outside an activity that owns view models (previews, tests) it falls back to the usual owner.
 */
@Composable
inline fun <reified VM : ViewModel> tabViewModel(): VM {
    val owner = LocalActivity.current as? ViewModelStoreOwner ?: checkNotNull(LocalViewModelStoreOwner.current)
    return viewModel(viewModelStoreOwner = owner, key = tabViewModelKey(VM::class.java))
}

/** The key a tab's view model is held under in the activity; see [tabViewModel]. */
fun tabViewModelKey(viewModelClass: Class<out ViewModel>): String = "tempo.tab:${viewModelClass.name}"
