package com.builder.aiphoneoperator.domain.status

import com.builder.aiphoneoperator.model.RequirementStatus

interface RepairStatusChecker {
    suspend fun requiresRepair(): Boolean
    suspend fun getBrokenRequirements(): List<RequirementStatus>
}
