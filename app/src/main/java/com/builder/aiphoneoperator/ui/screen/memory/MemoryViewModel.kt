package com.builder.aiphoneoperator.ui.screen.memory

import androidx.lifecycle.ViewModel

class MemoryViewModel : ViewModel() {
    val state = MemoryUiState()
}

data class MemoryUiState(
    val title: String = "Memory",
    val summary: String = "Local memory views will be connected without cloud dependency.",
)
