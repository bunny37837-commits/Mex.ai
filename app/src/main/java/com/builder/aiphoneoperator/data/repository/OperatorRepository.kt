package com.builder.aiphoneoperator.data.repository

import android.content.Context
import android.view.accessibility.AccessibilityEvent
import com.builder.aiphoneoperator.data.store.OperatorPreferencesStore
import com.builder.aiphoneoperator.domain.status.AndroidOnboardingStatusChecker
import com.builder.aiphoneoperator.domain.status.AndroidRepairStatusChecker
import com.builder.aiphoneoperator.model.RequirementStatus
import com.builder.aiphoneoperator.runtime.AppRuntimeState
import com.builder.aiphoneoperator.runtime.OperatorAppState
import com.builder.aiphoneoperator.runtime.PersistedRuntimeState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

object OperatorRepository : AppStateRepository {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var appContext: Context? = null
    private var preferencesStore: OperatorPreferencesStore? = null

    override val appState: StateFlow<OperatorAppState> = AppRuntimeState.state

    private val _repairStatus = MutableStateFlow<List<RequirementStatus>>(emptyList())
    override val repairStatus: StateFlow<List<RequirementStatus>> = _repairStatus.asStateFlow()

    override suspend fun initialize(context: Context) {
        appContext = context.applicationContext
        preferencesStore = OperatorPreferencesStore(appContext!!)
        syncPersistedState()
        refreshStatuses(context.applicationContext)
    }

    override suspend fun refreshStatuses() {
        appContext?.let { refreshStatuses(it) }
    }

    suspend fun refreshStatuses(context: Context) {
        val onboardingChecker = AndroidOnboardingStatusChecker(context)
        val onboardingStatus = onboardingChecker.getStatus()
        AppRuntimeState.updateOnboardingStatus(onboardingStatus)
        preferencesStore?.setOnboardingCompletionSnapshot(onboardingStatus.isComplete)
        syncPersistedState()

        val repairChecker = AndroidRepairStatusChecker(onboardingChecker) { appState.value }
        _repairStatus.value = repairChecker.getBrokenRequirements()
    }

    override fun setAccessibilityConnected(connected: Boolean) {
        AppRuntimeState.setAccessibilityConnected(connected)
        persistRuntimeMetadata()
    }

    override fun setAccessibilityEvent(event: AccessibilityEvent?) {
        AppRuntimeState.setAccessibilityEvent(event)
        persistRuntimeMetadata()
    }

    override fun setForegroundRunning(running: Boolean) {
        AppRuntimeState.setForegroundRunning(running)
        persistRuntimeMetadata()
    }

    override fun startTask(command: String) {
        AppRuntimeState.startTask(command)
        persistRuntimeMetadata()
    }

    override fun pauseTask() {
        AppRuntimeState.pauseTask()
        persistRuntimeMetadata()
    }

    override fun stopTask() {
        AppRuntimeState.stopTask()
        persistRuntimeMetadata()
    }

    override fun cancelTask() {
        AppRuntimeState.cancelTask()
        persistRuntimeMetadata()
    }

    fun acknowledgeHyperOsAutostart() {
        scope.launch {
            preferencesStore?.setHyperOsAutostartAcknowledged(true)
            syncPersistedState()
            refreshStatuses()
        }
    }

    fun setSafetyLock(enabled: Boolean) {
        AppRuntimeState.setSafetyLock(enabled)
        scope.launch {
            preferencesStore?.setSafetyLockEnabled(enabled)
            syncPersistedState()
        }
    }

    private fun persistRuntimeMetadata() {
        scope.launch {
            val state = appState.value
            preferencesStore?.setRuntimeMetadata(
                serviceRunning = state.foreground.running,
                accessibilityConnected = state.accessibility.connected,
                runtimeCommand = state.runningTask.command,
                eventSummary = state.accessibility.lastEventSummary.orEmpty(),
            )
            syncPersistedState()
        }
    }

    private suspend fun syncPersistedState() {
        val prefs = preferencesStore?.preferencesFlow?.first() ?: return
        AppRuntimeState.updatePersistedState(
            PersistedRuntimeState(
                hyperOsAutostartAcknowledged = prefs.hyperOsAutostartAcknowledged,
                onboardingCompletionSnapshot = prefs.onboardingCompletionSnapshot,
                safetyLockEnabled = prefs.safetyLockEnabled,
                lastServiceRunning = prefs.lastServiceRunning,
                lastAccessibilityConnected = prefs.lastAccessibilityConnected,
                lastRuntimeCommand = prefs.lastRuntimeCommand,
                lastEventSummary = prefs.lastEventSummary,
                lastMetadataUpdatedAt = prefs.lastMetadataUpdatedAt,
            )
        )
    }
}
