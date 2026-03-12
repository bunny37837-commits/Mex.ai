package com.builder.aiphoneoperator.ui.screen.settings

import androidx.lifecycle.ViewModel
import com.builder.aiphoneoperator.data.repository.OperatorRepository
import com.builder.aiphoneoperator.runtime.OperatorAppState
import kotlinx.coroutines.flow.StateFlow

class SettingsViewModel : ViewModel() {
    val appState: StateFlow<OperatorAppState> = OperatorRepository.appState
    val state = SettingsUiState()

    fun setLocalAiOnly(enabled: Boolean) = OperatorRepository.setLocalAiOnly(enabled)
    fun setConversationMemoryEnabled(enabled: Boolean) = OperatorRepository.setConversationMemoryEnabled(enabled)
    fun setDebugModeEnabled(enabled: Boolean) = OperatorRepository.setDebugModeEnabled(enabled)
}

data class SettingsUiState(
    val title: String = "Settings",
    val sections: List<SettingsSectionUi> = listOf(
        SettingsSectionUi("Accessibility", "Accessibility-first integration and status"),
        SettingsSectionUi("Agent", "Truthful settings for the current real agent core"),
        SettingsSectionUi("Diagnostics", "Runtime and debug controls"),
    ),
)

data class SettingsSectionUi(
    val title: String,
    val description: String,
)
