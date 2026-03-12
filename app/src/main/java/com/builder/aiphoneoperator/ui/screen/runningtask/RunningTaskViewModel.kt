package com.builder.aiphoneoperator.ui.screen.runningtask

import androidx.lifecycle.ViewModel
import com.builder.aiphoneoperator.data.repository.OperatorRepository
import com.builder.aiphoneoperator.runtime.OperatorAppState
import com.builder.aiphoneoperator.runtime.RuntimeControlBus
import kotlinx.coroutines.flow.StateFlow

class RunningTaskViewModel : ViewModel() {
    val appState: StateFlow<OperatorAppState> = OperatorRepository.appState
    val state = RunningTaskUiState()

    fun onPause() {
        OperatorRepository.pauseTask()
        RuntimeControlBus.pause()
    }

    fun onStop() {
        OperatorRepository.stopTask()
        RuntimeControlBus.stop()
    }

    fun onCancel() {
        OperatorRepository.cancelTask()
        RuntimeControlBus.cancel()
    }

    fun captureObservation() {
        OperatorRepository.captureObservation()
    }

    fun performBack() {
        OperatorRepository.performBack()
    }

    fun tapTargetByText(query: String) {
        OperatorRepository.tapTargetByText(query)
    }

    fun inputTextIntoFocusedField(text: String) {
        OperatorRepository.inputTextIntoFocusedField(text)
    }
}

data class RunningTaskUiState(
    val title: String = "Agent Session",
    val summary: String = "Shows the real command/session lifecycle for the current executor.",
)
