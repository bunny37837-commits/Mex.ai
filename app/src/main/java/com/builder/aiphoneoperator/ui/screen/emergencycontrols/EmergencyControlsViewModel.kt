package com.builder.aiphoneoperator.ui.screen.emergencycontrols

import androidx.lifecycle.ViewModel
import com.builder.aiphoneoperator.data.repository.OperatorRepository
import com.builder.aiphoneoperator.runtime.OperatorAppState
import com.builder.aiphoneoperator.runtime.RuntimeControlBus
import kotlinx.coroutines.flow.StateFlow

class EmergencyControlsViewModel : ViewModel() {
    val appState: StateFlow<OperatorAppState> = OperatorRepository.appState
    val state = EmergencyControlsUiState()

    fun stopAll() {
        OperatorRepository.stopTask()
        RuntimeControlBus.stop()
    }

    fun pauseCurrent() {
        OperatorRepository.pauseTask()
        RuntimeControlBus.pause()
    }

    fun cancelCurrent() {
        OperatorRepository.cancelTask()
        RuntimeControlBus.cancel()
    }

    fun setSafetyLock(enabled: Boolean) {
        OperatorRepository.setSafetyLock(enabled)
    }
}

data class EmergencyControlsUiState(
    val title: String = "Emergency Controls",
    val summary: String = "High-priority stop and safety controls will live here.",
)
