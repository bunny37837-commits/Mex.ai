package com.builder.aiphoneoperator.data.repository

import android.content.Context
import android.view.accessibility.AccessibilityEvent
import com.builder.aiphoneoperator.data.store.OperatorPreferencesStore
import com.builder.aiphoneoperator.domain.agent.AgentCommand
import com.builder.aiphoneoperator.domain.agent.AgentSession
import com.builder.aiphoneoperator.domain.agent.AgentSessionStatus
import com.builder.aiphoneoperator.domain.agent.CommandParser
import com.builder.aiphoneoperator.domain.agent.ExecutionResult
import com.builder.aiphoneoperator.domain.agent.OpenAppExecutor
import com.builder.aiphoneoperator.domain.agent.OpenAppResolver
import com.builder.aiphoneoperator.domain.status.AndroidOnboardingStatusChecker
import com.builder.aiphoneoperator.domain.status.AndroidRepairStatusChecker
import com.builder.aiphoneoperator.model.RequirementStatus
import com.builder.aiphoneoperator.runtime.AppRuntimeState
import com.builder.aiphoneoperator.runtime.HonestSettingsState
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
import java.util.UUID

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

    override fun submitCommand(command: String) {
        val context = appContext ?: return
        val now = System.currentTimeMillis()
        val created = AgentSession(
            id = UUID.randomUUID().toString(),
            commandText = command,
            createdAt = now,
            status = AgentSessionStatus.CREATED,
            message = "Session created.",
        )
        AppRuntimeState.setActiveSession(created)
        persistRuntimeMetadata()

        val parsing = created.copy(status = AgentSessionStatus.PARSING, message = "Parsing command…")
        AppRuntimeState.setActiveSession(parsing)

        val action = CommandParser.parse(AgentCommand(command))
        if (action == null) {
            val unsupported = parsing.copy(
                status = AgentSessionStatus.UNSUPPORTED,
                message = "This build currently supports only 'open app by name' commands.",
            )
            AppRuntimeState.archiveSession(unsupported)
            persistRuntimeMetadata()
            return
        }

        val resolved = when (action) {
            is com.builder.aiphoneoperator.domain.agent.ExecutionAction.OpenApp -> OpenAppResolver.resolve(context, action.query)
        }
        if (resolved == null) {
            val failed = parsing.copy(
                status = AgentSessionStatus.FAILED,
                action = action,
                message = "Could not find an installed app matching '${action.query}'.",
                error = "App resolution failed",
            )
            AppRuntimeState.archiveSession(failed)
            persistRuntimeMetadata()
            return
        }

        val resolvedSession = parsing.copy(
            status = AgentSessionStatus.RESOLVED,
            action = resolved,
            message = "Resolved ${resolved.resolvedLabel ?: resolved.query}.",
        )
        AppRuntimeState.setActiveSession(resolvedSession)

        val executing = resolvedSession.copy(status = AgentSessionStatus.EXECUTING, message = "Opening ${resolved.resolvedLabel ?: resolved.query}…")
        AppRuntimeState.setActiveSession(executing)

        when (val result = OpenAppExecutor.execute(context, resolved)) {
            is ExecutionResult.Success -> {
                val completed = executing.copy(status = AgentSessionStatus.COMPLETED, message = result.message)
                AppRuntimeState.archiveSession(completed)
            }
            is ExecutionResult.Failure -> {
                val failed = executing.copy(status = AgentSessionStatus.FAILED, message = result.reason, error = result.reason)
                AppRuntimeState.archiveSession(failed)
            }
        }
        persistRuntimeMetadata()
    }

    override fun pauseTask() {
        AppRuntimeState.pauseSession()
        persistRuntimeMetadata()
    }

    override fun stopTask() {
        AppRuntimeState.stopSession()
        persistRuntimeMetadata()
    }

    override fun cancelTask() {
        AppRuntimeState.cancelSession()
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

    fun setLocalAiOnly(enabled: Boolean) {
        scope.launch {
            preferencesStore?.setLocalAiOnly(enabled)
            syncPersistedState()
        }
    }

    fun setConversationMemoryEnabled(enabled: Boolean) {
        scope.launch {
            preferencesStore?.setConversationMemoryEnabled(enabled)
            syncPersistedState()
        }
    }

    fun setDebugModeEnabled(enabled: Boolean) {
        scope.launch {
            preferencesStore?.setDebugModeEnabled(enabled)
            syncPersistedState()
        }
    }

    private fun persistRuntimeMetadata() {
        scope.launch {
            val state = appState.value
            preferencesStore?.setRuntimeMetadata(
                serviceRunning = state.foreground.running,
                accessibilityConnected = state.accessibility.connected,
                runtimeCommand = state.activeSession?.commandText ?: state.persisted.lastRuntimeCommand,
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
        AppRuntimeState.updateSettings(
            HonestSettingsState(
                localAiOnly = prefs.localAiOnly,
                conversationMemoryEnabled = prefs.conversationMemoryEnabled,
                debugModeEnabled = prefs.debugModeEnabled,
            )
        )
    }
}
