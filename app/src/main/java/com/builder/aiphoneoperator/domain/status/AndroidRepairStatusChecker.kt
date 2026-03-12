package com.builder.aiphoneoperator.domain.status

import com.builder.aiphoneoperator.model.RequirementStatus
import com.builder.aiphoneoperator.runtime.OperatorAppState

class AndroidRepairStatusChecker(
    private val onboardingStatusChecker: OnboardingStatusChecker,
    private val currentStateProvider: () -> OperatorAppState,
) : RepairStatusChecker {
    override suspend fun requiresRepair(): Boolean = getBrokenRequirements().isNotEmpty()

    override suspend fun getBrokenRequirements(): List<RequirementStatus> {
        val onboarding = onboardingStatusChecker.getStatus()
        val baseState = currentStateProvider()
        return baseState.copy(onboardingStatus = onboarding).toRepairRequirements().filterNot { it.isOk }
    }
}
