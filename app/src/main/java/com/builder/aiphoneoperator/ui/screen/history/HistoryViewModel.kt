package com.builder.aiphoneoperator.ui.screen.history

import androidx.lifecycle.ViewModel
import com.builder.aiphoneoperator.data.repository.OperatorRepository
import com.builder.aiphoneoperator.runtime.OperatorAppState
import kotlinx.coroutines.flow.StateFlow

class HistoryViewModel : ViewModel() {
    val appState: StateFlow<OperatorAppState> = OperatorRepository.appState
    val state = HistoryUiState()
}

data class HistoryUiState(
    val title: String = "History",
)
