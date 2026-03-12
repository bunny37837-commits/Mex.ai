package com.builder.aiphoneoperator.ui.screen.capabilities

import androidx.lifecycle.ViewModel

class CapabilitiesViewModel : ViewModel() {
    val state = CapabilitiesUiState(
        title = "Capabilities",
        capabilities = listOf(
            "Open installed apps by name",
        )
    )
}

data class CapabilitiesUiState(
    val title: String,
    val capabilities: List<String>,
)
