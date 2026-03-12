package com.builder.aiphoneoperator.ui.screen.settings

import androidx.lifecycle.ViewModel

class SettingsViewModel : ViewModel() {
    val state = SettingsUiState()
}

data class SettingsUiState(
    val title: String = "Settings",
    val sections: List<SettingsSectionUi> = listOf(
        SettingsSectionUi("Accessibility", "Accessibility-first integration and status"),
        SettingsSectionUi("Privacy", "Local-first controls and future model toggles"),
        SettingsSectionUi("Repair", "Diagnostic and repair surface placeholders"),
    ),
)

data class SettingsSectionUi(
    val title: String,
    val description: String,
)
