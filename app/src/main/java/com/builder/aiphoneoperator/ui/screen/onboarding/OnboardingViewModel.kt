package com.builder.aiphoneoperator.ui.screen.onboarding

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

class OnboardingViewModel : ViewModel() {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    val appState: StateFlow<OperatorAppState> = OperatorRepository.appState
    val repairStatus: StateFlow<List<RequirementStatus>> = OperatorRepository.repairStatus
    val state = OnboardingUiState()

    fun refresh(context: Context) {
        scope.launch { OperatorRepository.refreshStatuses(context) }
    }

    fun acknowledgeAutostart() {
        OperatorRepository.acknowledgeHyperOsAutostart()
    }
}

data class OnboardingUiState(
    val title: String = "Onboarding",
    val summary: String = "Set up accessibility, notifications, and local-first operator basics.",
    val checklist: List<String> = listOf(
        "Grant accessibility service access",
        "Review service status",
        "Keep processing local-first",
    ),
)
