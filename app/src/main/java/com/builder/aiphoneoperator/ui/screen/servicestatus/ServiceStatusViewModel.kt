package com.builder.aiphoneoperator.ui.screen.servicestatus

import android.content.Context
import androidx.lifecycle.ViewModel
import com.builder.aiphoneoperator.data.repository.OperatorRepository
import com.builder.aiphoneoperator.runtime.OperatorAppState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ServiceStatusViewModel : ViewModel() {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    val appState: StateFlow<OperatorAppState> = OperatorRepository.appState
    val state = ServiceStatusUiState()

    fun refresh(context: Context) {
        scope.launch { OperatorRepository.refreshStatuses(context) }
    }
}

data class ServiceStatusUiState(
    val title: String = "Service Status",
    val summary: String = "Accessibility and foreground service status are repository-backed.",
)
