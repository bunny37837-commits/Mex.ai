package com.builder.aiphoneoperator.domain.usecase

import android.content.Context
import android.content.Intent
import android.os.Build
import com.builder.aiphoneoperator.data.repository.OperatorRepository
import com.builder.aiphoneoperator.model.RepairRequirement
import com.builder.aiphoneoperator.platform.SystemSettingsNavigator
import com.builder.aiphoneoperator.service.OperatorForegroundService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

object RequirementFixUseCase {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    fun execute(context: Context, requirement: RepairRequirement) {
        when (requirement) {
            RepairRequirement.ACCESSIBILITY_SERVICE,
            RepairRequirement.NOTIFICATIONS,
            RepairRequirement.BATTERY_OPTIMIZATION,
            RepairRequirement.AUTOSTART -> {
                SystemSettingsNavigator.openFixPath(context, requirement)
            }
            RepairRequirement.AI_SERVICE_RUNNING -> {
                val intent = Intent(context, OperatorForegroundService::class.java)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(intent)
                } else {
                    context.startService(intent)
                }
            }
            else -> {
                // Safe fallback for future requirement states.
            }
        }
        scope.launch {
            OperatorRepository.refreshStatuses(context.applicationContext)
        }
    }
}
