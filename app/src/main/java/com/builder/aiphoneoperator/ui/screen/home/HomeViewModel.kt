package com.builder.aiphoneoperator.ui.screen.home

import androidx.lifecycle.ViewModel
import com.builder.aiphoneoperator.data.repository.OperatorRepository
import com.builder.aiphoneoperator.runtime.OperatorAppState
import kotlinx.coroutines.flow.StateFlow

class HomeViewModel : ViewModel() {
    val appState: StateFlow<OperatorAppState> = OperatorRepository.appState
    val state = HomeUiState()

    fun onCommandSubmitted(command: String) {
        OperatorRepository.submitCommand(command)
    }
}

data class HomeUiState(
    val title: String = "AI Phone Agent",
    val summary: String = "Current real capability: open installed apps by name.",
    val status: String = "Agent core active",
)
