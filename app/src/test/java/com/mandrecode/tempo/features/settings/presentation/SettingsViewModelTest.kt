package com.mandrecode.tempo.features.settings.presentation

import android.net.Uri
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.mandrecode.tempo.core.data.preferences.NavigationPreferencesRepository
import com.mandrecode.tempo.core.data.preferences.NavigationPreferencesRepository.Companion.DEFAULT_TAB_ROUTINES
import com.mandrecode.tempo.core.data.preferences.NavigationPreferencesRepository.Companion.DEFAULT_TAB_TASKS
import com.mandrecode.tempo.core.data.preferences.ThemePreferencesRepository
import com.mandrecode.tempo.core.domain.model.ThemeMode
import com.mandrecode.tempo.features.backup.domain.model.ImportMode
import com.mandrecode.tempo.features.backup.domain.model.ImportOutcome
import com.mandrecode.tempo.features.backup.domain.model.ImportSummary
import com.mandrecode.tempo.features.backup.domain.usecase.ExportBackupUseCase
import com.mandrecode.tempo.features.backup.domain.usecase.ImportBackupUseCase
import com.mandrecode.tempo.features.tasks.domain.repository.CompletedTaskRetentionPreferences
import com.mandrecode.tempo.features.tasks.domain.usecase.ConfigureCompletedTaskRetentionUseCase
import com.mandrecode.tempo.infrastructure.backup.BackupFileDataSource
import com.mandrecode.tempo.util.AppVersionInfo
import com.mandrecode.tempo.util.AppVersionProvider
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelTest {
    private lateinit var viewModel: SettingsViewModel
    private lateinit var themePreferencesRepository: ThemePreferencesRepository
    private lateinit var navigationPreferencesRepository: NavigationPreferencesRepository
    private lateinit var appVersionProvider: AppVersionProvider
    private lateinit var completedTaskRetentionPreferences: CompletedTaskRetentionPreferences
    private lateinit var configureCompletedTaskRetention: ConfigureCompletedTaskRetentionUseCase
    private lateinit var exportBackup: ExportBackupUseCase
    private lateinit var importBackup: ImportBackupUseCase
    private lateinit var backupFileDataSource: BackupFileDataSource
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        themePreferencesRepository = mockk(relaxed = true)
        navigationPreferencesRepository = mockk(relaxed = true)
        appVersionProvider =
            mockk {
                every { getVersionInfo() } returns AppVersionInfo("1.0", 1)
            }
        completedTaskRetentionPreferences = mockk(relaxed = true)
        configureCompletedTaskRetention = mockk(relaxed = true)
        exportBackup = mockk(relaxed = true)
        importBackup = mockk(relaxed = true)
        backupFileDataSource = mockk(relaxed = true)

        coEvery { themePreferencesRepository.getThemeMode() } returns flowOf(ThemeMode.SYSTEM)
        coEvery { themePreferencesRepository.getUseTempoColors() } returns flowOf(false)
        coEvery { navigationPreferencesRepository.isRoutinesTabEnabled() } returns flowOf(true)
        coEvery { navigationPreferencesRepository.isTasksTabEnabled() } returns flowOf(true)
        coEvery { navigationPreferencesRepository.getDefaultTab() } returns flowOf(DEFAULT_TAB_ROUTINES)
        every { completedTaskRetentionPreferences.isEnabled } returns MutableStateFlow(false)
        every { completedTaskRetentionPreferences.retentionDays } returns MutableStateFlow(30)

        viewModel = createViewModel()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initialState has correct default values`() =
        runTest {
            advanceUntilIdle()

            assertThat(viewModel.uiState.value.selectedThemeMode).isEqualTo(ThemeMode.SYSTEM)
            assertThat(viewModel.uiState.value.isRoutinesTabEnabled).isTrue()
            assertThat(viewModel.uiState.value.isTasksTabEnabled).isTrue()
            assertThat(viewModel.uiState.value.defaultTab).isEqualTo(SettingsContract.DefaultTab.ROUTINES)
        }

    @Test
    fun `themeModeSelected calls repository`() =
        runTest {
            advanceUntilIdle()
            viewModel.onEvent(SettingsContract.UiEvent.ThemeModeSelected(ThemeMode.DARK))
            coVerify { themePreferencesRepository.setThemeMode(ThemeMode.DARK) }
        }

    @Test
    fun `tempoColorsToggled calls repository`() =
        runTest {
            advanceUntilIdle()
            viewModel.onEvent(SettingsContract.UiEvent.TempoColorsToggled(true))
            coVerify { themePreferencesRepository.setUseTempoColors(true) }
        }

    @Test
    fun `tempoColorsObserver updates state when repository changes`() =
        runTest {
            coEvery { themePreferencesRepository.getUseTempoColors() } returns flowOf(true)

            // Re-init VM
            viewModel = createViewModel()
            advanceUntilIdle()

            assertThat(viewModel.uiState.value.useTempoColors).isTrue()
        }

    @Test
    fun `routinesTabToggled when tasks enabled calls repository`() =
        runTest {
            advanceUntilIdle()
            viewModel.onEvent(SettingsContract.UiEvent.RoutinesTabToggled(false))
            coVerify { navigationPreferencesRepository.setRoutinesTabEnabled(false) }
        }

    @Test
    fun `routinesTabToggled when tasks disabled does not call repository`() =
        runTest {
            coEvery { navigationPreferencesRepository.isTasksTabEnabled() } returns flowOf(false)

            // Re-init VM
            viewModel = createViewModel()
            advanceUntilIdle()

            viewModel.onEvent(SettingsContract.UiEvent.RoutinesTabToggled(false))
            coVerify(exactly = 0) { navigationPreferencesRepository.setRoutinesTabEnabled(false) }
        }

    @Test
    fun `tasksTabToggled when routines enabled calls repository`() =
        runTest {
            advanceUntilIdle()
            viewModel.onEvent(SettingsContract.UiEvent.TasksTabToggled(false))
            coVerify { navigationPreferencesRepository.setTasksTabEnabled(false) }
        }

    @Test
    fun `tasksTabToggled when routines disabled does not call repository`() =
        runTest {
            coEvery { navigationPreferencesRepository.isRoutinesTabEnabled() } returns flowOf(false)

            // Re-init VM
            viewModel = createViewModel()
            advanceUntilIdle()

            viewModel.onEvent(SettingsContract.UiEvent.TasksTabToggled(false))
            coVerify(exactly = 0) { navigationPreferencesRepository.setTasksTabEnabled(false) }
        }

    @Test
    fun `routinesTabDisabled when default tab switches to tasks`() =
        runTest {
            advanceUntilIdle()
            viewModel.onEvent(SettingsContract.UiEvent.RoutinesTabToggled(false))
            coVerify { navigationPreferencesRepository.setDefaultTab(DEFAULT_TAB_TASKS) }
        }

    @Test
    fun `tasksTabDisabled when default tab switches to routines`() =
        runTest {
            coEvery { navigationPreferencesRepository.getDefaultTab() } returns flowOf(DEFAULT_TAB_TASKS)

            // Re-init VM
            viewModel = createViewModel()
            advanceUntilIdle()

            viewModel.onEvent(SettingsContract.UiEvent.TasksTabToggled(false))
            coVerify { navigationPreferencesRepository.setDefaultTab(DEFAULT_TAB_ROUTINES) }
        }

    @Test
    fun `defaultTabSelected routines when enabled calls repository`() =
        runTest {
            advanceUntilIdle()
            viewModel.onEvent(SettingsContract.UiEvent.DefaultTabSelected(SettingsContract.DefaultTab.ROUTINES))
            coVerify { navigationPreferencesRepository.setDefaultTab(DEFAULT_TAB_ROUTINES) }
        }

    @Test
    fun `defaultTabSelected tasks when enabled calls repository`() =
        runTest {
            advanceUntilIdle()
            viewModel.onEvent(SettingsContract.UiEvent.DefaultTabSelected(SettingsContract.DefaultTab.TASKS))
            coVerify { navigationPreferencesRepository.setDefaultTab(DEFAULT_TAB_TASKS) }
        }

    @Test
    fun `themeModeObserver updates state when repository changes`() =
        runTest {
            coEvery { themePreferencesRepository.getThemeMode() } returns flowOf(ThemeMode.LIGHT)

            // Re-init VM
            viewModel = createViewModel()
            advanceUntilIdle()

            assertThat(viewModel.uiState.value.selectedThemeMode).isEqualTo(ThemeMode.LIGHT)
        }

    @Test
    fun `availableThemeModes contains all three options`() =
        runTest {
            advanceUntilIdle()
            val availableModes = viewModel.uiState.value.availableThemeModes

            assertThat(availableModes).hasSize(3)
            assertThat(availableModes).contains(ThemeMode.LIGHT)
            assertThat(availableModes).contains(ThemeMode.DARK)
            assertThat(availableModes).contains(ThemeMode.SYSTEM)
        }

    @Test
    fun `initialState loads version info`() =
        runTest {
            advanceUntilIdle()
            val state = viewModel.uiState.value

            assertThat(state.appVersion).isNotEmpty()
        }

    @Test
    fun `retention preferences update settings state`() =
        runTest {
            every { completedTaskRetentionPreferences.isEnabled } returns MutableStateFlow(true)
            every { completedTaskRetentionPreferences.retentionDays } returns MutableStateFlow(45)
            viewModel = createViewModel()

            advanceUntilIdle()

            assertThat(viewModel.uiState.value.autoRemoveCompletedTasksEnabled).isTrue()
            assertThat(viewModel.uiState.value.completedTaskRetentionDays).isEqualTo(45)
        }

    @Test
    fun `auto removal toggle configures current retention`() =
        runTest {
            advanceUntilIdle()

            viewModel.onEvent(SettingsContract.UiEvent.AutoRemoveCompletedTasksToggled(true))

            verify { configureCompletedTaskRetention(true, 30) }
            assertThat(viewModel.uiState.value.autoRemoveCompletedTasksEnabled).isTrue()
        }

    @Test
    fun `retention day change is normalized and configured`() =
        runTest {
            advanceUntilIdle()

            viewModel.onEvent(SettingsContract.UiEvent.CompletedTaskRetentionDaysChanged(44))

            verify { configureCompletedTaskRetention(false, 44) }
            assertThat(viewModel.uiState.value.completedTaskRetentionDays).isEqualTo(45)
        }

    @Test
    fun `export click generates backup and launches picker with suggested name`() =
        runTest {
            coEvery { exportBackup() } returns
                ExportBackupUseCase.Export(json = "{}", suggestedFileName = "tempo-backup-20260721-1000.json")

            viewModel.uiEffect.test {
                viewModel.onEvent(SettingsContract.UiEvent.ExportClicked)
                advanceUntilIdle()

                assertThat(awaitItem()).isEqualTo(
                    SettingsContract.UiEffect.LaunchExportPicker("tempo-backup-20260721-1000.json"),
                )
            }
        }

    @Test
    fun `picked export destination writes the pending json and confirms`() =
        runTest {
            coEvery { exportBackup() } returns
                ExportBackupUseCase.Export(json = "{\"schemaVersion\":1}", suggestedFileName = "f.json")
            val uri = mockk<Uri>()

            viewModel.uiEffect.test {
                viewModel.onEvent(SettingsContract.UiEvent.ExportClicked)
                advanceUntilIdle()
                awaitItem()
                viewModel.onEvent(SettingsContract.UiEvent.ExportDestinationPicked(uri))
                advanceUntilIdle()

                coVerify { backupFileDataSource.write(uri, "{\"schemaVersion\":1}") }
                assertThat(awaitItem()).isEqualTo(
                    SettingsContract.UiEffect.ShowMessage(com.mandrecode.tempo.R.string.backup_export_success),
                )
            }
        }

    @Test
    fun `picked import file shows the mode chooser dialog`() =
        runTest {
            viewModel.onEvent(SettingsContract.UiEvent.ImportFilePicked(mockk()))
            advanceUntilIdle()

            assertThat(viewModel.uiState.value.backupDialog)
                .isEqualTo(SettingsContract.BackupDialog.ChooseImportMode)
        }

    @Test
    fun `choosing a mode runs the import and shows the summary`() =
        runTest {
            val uri = mockk<Uri>()
            coEvery { backupFileDataSource.read(uri) } returns "{}"
            coEvery { importBackup("{}", ImportMode.MERGE) } returns
                ImportOutcome.Success(ImportSummary())

            viewModel.onEvent(SettingsContract.UiEvent.ImportFilePicked(uri))
            viewModel.onEvent(SettingsContract.UiEvent.ImportModeChosen(ImportMode.MERGE))
            advanceUntilIdle()

            val dialog = viewModel.uiState.value.backupDialog
            assertThat(dialog).isInstanceOf(SettingsContract.BackupDialog.ImportSucceeded::class.java)
            assertThat(viewModel.uiState.value.backupInProgress).isFalse()
        }

    @Test
    fun `unreadable import file shows a read error`() =
        runTest {
            val uri = mockk<Uri>()
            coEvery { backupFileDataSource.read(uri) } throws java.io.IOException("boom")

            viewModel.onEvent(SettingsContract.UiEvent.ImportFilePicked(uri))
            viewModel.onEvent(SettingsContract.UiEvent.ImportModeChosen(ImportMode.REPLACE))
            advanceUntilIdle()

            assertThat(viewModel.uiState.value.backupDialog)
                .isEqualTo(
                    SettingsContract.BackupDialog.ImportFailed(SettingsContract.ImportError.ReadFailed),
                )
        }

    @Test
    fun `unsupported version outcome maps to the version error dialog`() =
        runTest {
            val uri = mockk<Uri>()
            coEvery { backupFileDataSource.read(uri) } returns "{}"
            coEvery { importBackup(any(), any()) } returns
                ImportOutcome.UnsupportedVersion(fileVersion = 7, maxSupported = 1)

            viewModel.onEvent(SettingsContract.UiEvent.ImportFilePicked(uri))
            viewModel.onEvent(SettingsContract.UiEvent.ImportModeChosen(ImportMode.MERGE))
            advanceUntilIdle()

            assertThat(viewModel.uiState.value.backupDialog)
                .isEqualTo(
                    SettingsContract.BackupDialog.ImportFailed(
                        SettingsContract.ImportError.UnsupportedVersion(fileVersion = 7, maxSupported = 1),
                    ),
                )
        }

    @Test
    fun `dismissing the backup dialog clears it`() =
        runTest {
            viewModel.onEvent(SettingsContract.UiEvent.ImportFilePicked(mockk()))
            viewModel.onEvent(SettingsContract.UiEvent.BackupDialogDismissed)
            advanceUntilIdle()

            assertThat(viewModel.uiState.value.backupDialog).isNull()
        }

    @Test
    fun `export destination without pending json re-runs the export use case`() =
        runTest {
            coEvery { exportBackup() } returns
                ExportBackupUseCase.Export(json = "{}", suggestedFileName = "f.json")
            val uri = mockk<Uri>()

            viewModel.onEvent(SettingsContract.UiEvent.ExportDestinationPicked(uri))
            advanceUntilIdle()

            coVerify { exportBackup() }
            coVerify { backupFileDataSource.write(uri, "{}") }
        }

    @Test
    fun `failed export write shows the error message`() =
        runTest {
            coEvery { exportBackup() } returns
                ExportBackupUseCase.Export(json = "{}", suggestedFileName = "f.json")
            coEvery { backupFileDataSource.write(any(), any()) } throws java.io.IOException("nope")
            val uri = mockk<Uri>()

            viewModel.uiEffect.test {
                viewModel.onEvent(SettingsContract.UiEvent.ExportDestinationPicked(uri))
                advanceUntilIdle()

                assertThat(awaitItem()).isEqualTo(
                    SettingsContract.UiEffect.ShowMessage(com.mandrecode.tempo.R.string.backup_export_error),
                )
            }
        }

    @Test
    fun `choosing a mode without a picked file does nothing`() =
        runTest {
            viewModel.onEvent(SettingsContract.UiEvent.ImportModeChosen(ImportMode.MERGE))
            advanceUntilIdle()

            assertThat(viewModel.uiState.value.backupDialog).isNull()
            coVerify(exactly = 0) { importBackup(any(), any()) }
        }

    @Test
    fun `export cancelled clears the pending json so a later pick re-exports`() =
        runTest {
            coEvery { exportBackup() } returns
                ExportBackupUseCase.Export(json = "{}", suggestedFileName = "f.json")
            val uri = mockk<Uri>()

            viewModel.onEvent(SettingsContract.UiEvent.ExportClicked)
            advanceUntilIdle()
            viewModel.onEvent(SettingsContract.UiEvent.ExportCancelled)
            viewModel.onEvent(SettingsContract.UiEvent.ExportDestinationPicked(uri))
            advanceUntilIdle()

            coVerify(exactly = 2) { exportBackup() }
        }

    @Test
    fun `corrupt file outcome maps to the corrupt error dialog`() =
        runTest {
            val uri = mockk<Uri>()
            coEvery { backupFileDataSource.read(uri) } returns "{}"
            coEvery { importBackup(any(), any()) } returns ImportOutcome.CorruptFile

            viewModel.onEvent(SettingsContract.UiEvent.ImportFilePicked(uri))
            viewModel.onEvent(SettingsContract.UiEvent.ImportModeChosen(ImportMode.MERGE))
            advanceUntilIdle()

            assertThat(viewModel.uiState.value.backupDialog)
                .isEqualTo(
                    SettingsContract.BackupDialog.ImportFailed(SettingsContract.ImportError.CorruptFile),
                )
        }

    @Test
    fun `validation failure outcome maps to the validation error dialog`() =
        runTest {
            val uri = mockk<Uri>()
            coEvery { backupFileDataSource.read(uri) } returns "{}"
            coEvery { importBackup(any(), any()) } returns ImportOutcome.ValidationFailed(emptyList())

            viewModel.onEvent(SettingsContract.UiEvent.ImportFilePicked(uri))
            viewModel.onEvent(SettingsContract.UiEvent.ImportModeChosen(ImportMode.REPLACE))
            advanceUntilIdle()

            assertThat(viewModel.uiState.value.backupDialog)
                .isInstanceOf(SettingsContract.BackupDialog.ImportFailed::class.java)
        }

    private fun createViewModel(): SettingsViewModel =
        SettingsViewModel(
            themePreferencesRepository,
            navigationPreferencesRepository,
            appVersionProvider,
            completedTaskRetentionPreferences,
            configureCompletedTaskRetention,
            SettingsBackupDelegate(exportBackup, importBackup, backupFileDataSource),
        )
}
