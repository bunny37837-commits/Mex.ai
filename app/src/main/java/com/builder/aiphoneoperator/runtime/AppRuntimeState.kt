package com.builder.aiphoneoperator.runtime

import android.view.accessibility.AccessibilityEvent
import com.builder.aiphoneoperator.domain.agent.AgentSession
import com.builder.aiphoneoperator.domain.agent.AgentSessionStatus
import com.builder.aiphoneoperator.model.RepairRequirement
import com.builder.aiphoneoperator.model.RequirementStatus
import com.builder.aiphoneoperator.model.SeverityLevel
import com.builder.aiphoneoperator.model.ServiceState
import com.builder.aiphoneoperator.model.SystemStatusUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class HonestSettingsState(
    val localAiOnly: Boolean = true,
    val conversationMemoryEnabled: Boolean = false,
    val debugModeEnabled: Boolean = false,
)

data class OnboardingStatus(
    val accessibilityEnabled: Boolean = false,
    val notificationPermissionGranted: Boolean = false,
    val batteryOptimizationDisabled: Boolean = false,
    val hyperOsAutostartEnabled: Boolean = false,
) {
    val isComplete: Boolean
        get() = accessibilityEnabled && notificationPermissionGranted && batteryOptimizationDisabled && hyperOsAutostartEnabled
}

data class PersistedRuntimeState(
    val hyperOsAutostartAcknowledged: Boolean = false,
    val onboardingCompletionSnapshot: Boolean = false,
    val safetyLockEnabled: Boolean = false,
    val lastServiceRunning: Boolean = false,
    val lastAccessibilityConnected: Boolean = false,
    val lastRuntimeCommand: String = "",
    val lastEventSummary: String = "",
    val lastMetadataUpdatedAt: Long = 0L,
)

data class AccessibilityRuntimeState(
    val connected: Boolean = false,
    val lastEventSummary: String? = null,
)

data class ForegroundRuntimeState(
    val running: Boolean = false,
)

data class OperatorAppState(
    val onboardingStatus: OnboardingStatus = OnboardingStatus(),
    val persisted: PersistedRuntimeState = PersistedRuntimeState(),
    val settings: HonestSettingsState = HonestSettingsState(),
    val accessibility: AccessibilityRuntimeState = AccessibilityRuntimeState(),
    val foreground: ForegroundRuntimeState = ForegroundRuntimeState(),
    val activeSession: AgentSession? = null,
    val sessionHistory: List<AgentSession> = emptyList(),
) {
    fun toSystemStatusUiState(): SystemStatusUiState = SystemStatusUiState(
        modelName = "Open-app executor",
        accessibilityOn = onboardingStatus.accessibilityEnabled && accessibility.connected,
        isPrivacyLocal = settings.localAiOnly,
        serviceState = if (foreground.running) ServiceState.ACTIVE else ServiceState.INACTIVE,
        apiConnected = false,
        batteryOk = onboardingStatus.batteryOptimizationDisabled,
        autostartOn = onboardingStatus.hyperOsAutostartEnabled || persisted.hyperOsAutostartAcknowledged,
    )

    fun toRepairRequirements(): List<RequirementStatus> = listOf(
        RequirementStatus(
            requirement = RepairRequirement.ACCESSIBILITY_SERVICE,
            isOk = onboardingStatus.accessibilityEnabled,
            severity = SeverityLevel.CRITICAL,
            title = "Accessibility Service",
            description = "Required for observing and eventually controlling UI.",
            impactLine = "Without this, the operator cannot interact with the device.",
        ),
        RequirementStatus(
            requirement = RepairRequirement.NOTIFICATIONS,
            isOk = onboardingStatus.notificationPermissionGranted,
            severity = SeverityLevel.WARNING,
            title = "Notification Permission",
            description = "Needed for foreground visibility and user-facing runtime updates.",
            impactLine = "Without this, runtime visibility is reduced.",
        ),
        RequirementStatus(
            requirement = RepairRequirement.BATTERY_OPTIMIZATION,
            isOk = onboardingStatus.batteryOptimizationDisabled,
            severity = SeverityLevel.WARNING,
            title = "Battery Optimization",
            description = "Background restrictions can kill the operator runtime.",
            impactLine = "Without this, long-running operations may be interrupted.",
        ),
        RequirementStatus(
            requirement = RepairRequirement.AUTOSTART,
            isOk = onboardingStatus.hyperOsAutostartEnabled || persisted.hyperOsAutostartAcknowledged,
            severity = SeverityLevel.WARNING,
            title = "HyperOS Autostart",
            description = "HyperOS may block restart and persistence unless autostart is enabled.",
            impactLine = "Without this, the operator may not survive restarts or idle conditions.",
        ),
        RequirementStatus(
            requirement = RepairRequirement.AI_SERVICE_RUNNING,
            isOk = foreground.running,
            severity = SeverityLevel.CRITICAL,
            title = "Foreground Service",
            description = "Persistent runtime host for the operator shell.",
            impactLine = "Without this, the operator runtime is offline.",
        ),
    )
}

object AppRuntimeState {
    private val _state = MutableStateFlow(OperatorAppState())
    val state: StateFlow<OperatorAppState> = _state.asStateFlow()

    fun updateOnboardingStatus(status: OnboardingStatus) = _state.update { it.copy(onboardingStatus = status) }
    fun updatePersistedState(persisted: PersistedRuntimeState) = _state.update { it.copy(persisted = persisted) }
    fun updateSettings(settings: HonestSettingsState) = _state.update { it.copy(settings = settings) }
    fun setSafetyLock(enabled: Boolean) = _state.update { it.copy(persisted = it.persisted.copy(safetyLockEnabled = enabled)) }
    fun setAccessibilityConnected(connected: Boolean) = _state.update { it.copy(accessibility = it.accessibility.copy(connected = connected)) }
    fun setAccessibilityEvent(event: AccessibilityEvent?) = _state.update {
        it.copy(accessibility = it.accessibility.copy(lastEventSummary = event?.let { e -> "type=${e.eventType} package=${e.packageName}" }))
    }
    fun setForegroundRunning(running: Boolean) = _state.update { it.copy(foreground = ForegroundRuntimeState(running)) }

    fun setActiveSession(session: AgentSession) = _state.update { it.copy(activeSession = session) }
    fun updateActiveSession(transform: (AgentSession?) -> AgentSession?) = _state.update { current ->
        val updated = transform(current.activeSession)
        current.copy(activeSession = updated)
    }
    fun archiveSession(session: AgentSession) = _state.update { current ->
        current.copy(activeSession = session, sessionHistory = listOf(session) + current.sessionHistory)
    }
    fun pauseSession() = _state.update { current ->
        val session = current.activeSession ?: return@update current
        current.copy(activeSession = session.copy(status = AgentSessionStatus.PAUSED, message = "Session paused."))
    }
    fun stopSession() = _state.update { current ->
        val session = current.activeSession ?: return@update current
        current.copy(activeSession = session.copy(status = AgentSessionStatus.CANCELLED, message = "Session stopped."))
    }
    fun cancelSession() = _state.update { current ->
        val session = current.activeSession ?: return@update current
        current.copy(activeSession = session.copy(status = AgentSessionStatus.CANCELLED, message = "Session cancelled."))
    }
}
