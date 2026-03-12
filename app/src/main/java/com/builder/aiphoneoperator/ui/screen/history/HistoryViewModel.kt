package com.builder.aiphoneoperator.ui.screen.history

import androidx.lifecycle.ViewModel

class HistoryViewModel : ViewModel() {
    val state = HistoryUiState()
}

data class HistoryUiState(
    val title: String = "History",
    val items: List<String> = listOf(
        "No history wired yet",
    ),
)
