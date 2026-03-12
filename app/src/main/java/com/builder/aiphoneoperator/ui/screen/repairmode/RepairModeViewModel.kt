package com.builder.aiphoneoperator.ui.screen.repairmode

import android.content.Context
import androidx.lifecycle.ViewModel
import com.builder.aiphoneoperator.data.repository.OperatorRepository
import com.builder.aiphoneoperator.model.RequirementStatus
import com.builder.aiphoneoperator.runtime.OperatorAppState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class RepairModeViewModel : ViewModel() {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    val appState: StateFlow<OperatorAppState> = OperatorRepository.appState
    val repairStatus: StateFlow<List<RequirementStatus>> = OperatorRepository.repairStatus
    val state = RepairModeUiState()

    fun refresh(context: Context) {
        scope.launch { OperatorRepository.refreshStatuses(context) }
    }

    fun acknowledgeAutostart() {
        OperatorRepository.acknowledgeHyperOsAutostart()
    }
}

data class RepairModeUiState(
    val title: String = "Repair Mode",
    val summary: String = "Repair surfaces are connected to runtime requirement state.",
)
