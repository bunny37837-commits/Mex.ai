package com.builder.aiphoneoperator.ui.screen.capabilities

import androidx.lifecycle.ViewModel

class CapabilitiesViewModel : ViewModel() {
    val state = CapabilitiesUiState()
}

data class CapabilitiesUiState(
    val title: String = "Capabilities",
    val capabilities: List<String> = listOf(
        "Accessibility-first control surface",
        "Foreground service runtime shell",
        "Local-first architecture",
    ),
)
