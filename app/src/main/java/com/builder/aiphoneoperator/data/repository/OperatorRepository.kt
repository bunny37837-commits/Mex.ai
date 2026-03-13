package com.builder.aiphoneoperator.data.repository

import android.content.Context
import android.view.accessibility.AccessibilityEvent
import com.builder.aiphoneoperator.data.store.OperatorPreferencesStore
import com.builder.aiphoneoperator.domain.agent.AccessibilityActionExecutor
import com.builder.aiphoneoperator.domain.agent.AccessibilityGroundingEngine
import com.builder.aiphoneoperator.domain.agent.AccessibilityObservationProvider
import com.builder.aiphoneoperator.domain.agent.AgentCommand
import com.builder.aiphoneoperator.domain.agent.AgentSession
import com.builder.aiphoneoperator.domain.agent.AgentSessionStatus
import com.builder.aiphoneoperator.domain.agent.BlockerType
import com.builder.aiphoneoperator.domain.agent.CommandParser
import com.builder.aiphoneoperator.domain.agent.DeviceAction
import com.builder.aiphoneoperator.domain.agent.ExecutionAction
import com.builder.aiphoneoperator.domain.agent.ExecutionResult
import com.builder.aiphoneoperator.domain.agent.GroundedTarget
import com.builder.aiphoneoperator.domain.agent.GroundingSource
import com.builder.aiphoneoperator.domain.agent.OpenAppExecutor
import com.builder.aiphoneoperator.domain.agent.OpenAppResolver
import com.builder.aiphoneoperator.domain.agent.PostActionVerifier
import com.builder.aiphoneoperator.domain.agent.ScreenObservation
import com.builder.aiphoneoperator.domain.agent.SessionOrchestrator
import com.builder.aiphoneoperator.domain.agent.TargetAffordance
import com.builder.aiphoneoperator.domain.agent.VerificationOutcome
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

    override fun captureObservation(): ScreenObservation? {
        val observation = AccessibilityObservationProvider.capture()
        AppRuntimeState.setCurrentObservation(observation)
        return observation
    }

    override fun tapTargetByText(query: String): Boolean {
        val before = captureObservation()
        val blocked = before?.blockers?.firstOrNull()
        if (blocked != null && blocked.type != BlockerType.KEYBOARD_OBSTRUCTION) {
            AppRuntimeState.updateActiveSession { session ->
                session?.copy(status = AgentSessionStatus.BLOCKED, message = blocked.message, error = blocked.message)
            }
            return false
        }
        val observation = before ?: return false
        val target = AccessibilityGroundingEngine.findTarget(observation, query) ?: return false
        AppRuntimeState.updateActiveSession { session ->
            session?.copy(status = AgentSessionStatus.EXECUTING, message = "Tapping '${target.label}'.")
        }
        val executed = AccessibilityActionExecutor.execute(DeviceAction.Tap(target))
        val after = captureObservation()
        val verification = PostActionVerifier.verifyTap(before, after, target)
        AppRuntimeState.updateActiveSession { session ->
            session?.copy(
                status = verificationToStatus(verification.outcome),
                message = verification.message,
                error = if (executed) null else "Tap execution failed",
            )
        }
        return executed && verification.outcome == VerificationOutcome.SUCCESS
    }

    override fun inputTextIntoFocusedField(text: String): Boolean {
        val before = captureObservation() ?: return false
        val focused = before.nodes.firstOrNull { it.focused && it.editable }
            ?: before.nodes.firstOrNull { it.editable }
            ?: return false
        val target = AccessibilityGroundingEngine.findTarget(before, focused.text ?: focused.contentDescription ?: focused.id, requireEditable = true)
            ?: GroundedTarget(
                id = focused.id,
                label = focused.text ?: focused.contentDescription ?: "focused field",
                bounds = focused.bounds,
                source = GroundingSource.ACCESSIBILITY,
                affordances = setOf(TargetAffordance.INPUT_TEXT),
                confidence = 0.95f,
            )
        AppRuntimeState.updateActiveSession { session ->
            session?.copy(status = AgentSessionStatus.EXECUTING, message = "Typing into focused field.")
        }
        val executed = AccessibilityActionExecutor.execute(DeviceAction.InputText(target, text))
        val after = captureObservation()
        val verification = PostActionVerifier.verifyInput(after, target, text)
        AppRuntimeState.updateActiveSession { session ->
            session?.copy(
                status = verificationToStatus(verification.outcome),
                message = verification.message,
                error = if (executed) null else "Text input execution failed",
            )
        }
        return executed && verification.outcome == VerificationOutcome.SUCCESS
    }

    override fun performBack(): Boolean {
        val before = captureObservation()
        AppRuntimeState.updateActiveSession { session ->
            session?.copy(status = AgentSessionStatus.EXECUTING, message = "Navigating back.")
        }
        val executed = AccessibilityActionExecutor.execute(DeviceAction.Back)
        val after = captureObservation()
        val verification = PostActionVerifier.verifyBack(before, after)
        AppRuntimeState.updateActiveSession { session ->
            session?.copy(
                status = verificationToStatus(verification.outcome),
                message = verification.message,
                error = if (executed) null else "Back execution failed",
            )
        }
        return executed && verification.outcome == VerificationOutcome.SUCCESS
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

        scope.launch {
            val parsing = created.copy(status = AgentSessionStatus.PARSING, message = "Parsing command…")
            AppRuntimeState.setActiveSession(parsing)

            val action = CommandParser.parse(AgentCommand(command))
            if (action == null) {
                val unsupported = parsing.copy(
                    status = AgentSessionStatus.UNSUPPORTED,
                    message = "Could not turn that request into a navigable workflow.",
                )
                AppRuntimeState.archiveSession(unsupported)
                persistRuntimeMetadata()
                return@launch
            }

            when (action) {
                is ExecutionAction.OpenApp -> executeOpenAppOnly(context, parsing, action)
                is ExecutionAction.NavigateWorkflow -> executeWorkflow(context, parsing, action)
            }
            persistRuntimeMetadata()
        }
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

    private suspend fun executeOpenAppOnly(context: Context, parsing: AgentSession, action: ExecutionAction.OpenApp) {
        val resolved = OpenAppResolver.resolve(context, action.query)
        if (resolved == null) {
            val failed = parsing.copy(
                status = AgentSessionStatus.FAILED,
                action = action,
                message = "Could not find an installed app matching '${action.query}'.",
                error = "App resolution failed",
            )
            AppRuntimeState.archiveSession(failed)
            return
        }

        val resolvedSession = parsing.copy(
            status = AgentSessionStatus.RESOLVED,
            action = resolved,
            message = "Resolved ${resolved.resolvedLabel ?: resolved.query}.",
            totalSteps = 1,
        )
        AppRuntimeState.setActiveSession(resolvedSession)

        when (val result = OpenAppExecutor.execute(context, resolved)) {
            is ExecutionResult.Success -> AppRuntimeState.archiveSession(
                resolvedSession.copy(
                    status = AgentSessionStatus.COMPLETED,
                    message = result.message,
                    currentStepIndex = 1,
                )
            )
            is ExecutionResult.Failure -> AppRuntimeState.archiveSession(
                resolvedSession.copy(
                    status = AgentSessionStatus.FAILED,
                    message = result.reason,
                    error = result.reason,
                )
            )
        }
    }

    private suspend fun executeWorkflow(context: Context, parsing: AgentSession, action: ExecutionAction.NavigateWorkflow) {
        val resolvedPackage = action.appQuery?.let { OpenAppResolver.resolve(context, it) }
        val resolved = action.copy(
            resolvedPackageName = resolvedPackage?.resolvedPackageName,
            resolvedLabel = resolvedPackage?.resolvedLabel,
        )
        val resolvedSession = parsing.copy(
            status = AgentSessionStatus.RESOLVED,
            action = resolved,
            message = if (resolved.resolvedLabel != null) "Resolved ${resolved.resolvedLabel}." else "Workflow ready.",
            totalSteps = resolved.steps.size,
        )
        AppRuntimeState.setActiveSession(resolvedSession)

        val finalSession = SessionOrchestrator.run(
            context = context,
            workflow = resolved,
            onUpdate = { status, message, stepIndex, totalSteps, retries, role ->
                AppRuntimeState.updateActiveSession { session ->
                    session?.copy(
                        status = status,
                        message = message,
                        currentStepIndex = stepIndex,
                        totalSteps = totalSteps,
                        retries = retries,
                        lastScreenRole = role,
                    )
                }
            },
            captureObservation = { captureObservation() },
            executeDeviceAction = { actionToRun -> AccessibilityActionExecutor.execute(actionToRun) },
            launchApp = { openAction -> OpenAppExecutor.execute(context, openAction) },
        )

        val current = appState.value.activeSession ?: resolvedSession
        AppRuntimeState.archiveSession(
            current.copy(
                status = finalSession.status,
                message = finalSession.message,
                error = finalSession.error,
                currentStepIndex = finalSession.currentStepIndex,
                totalSteps = finalSession.totalSteps,
                lastScreenRole = finalSession.lastScreenRole,
                retries = finalSession.retries,
            )
        )
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

    private fun verificationToStatus(outcome: VerificationOutcome): AgentSessionStatus = when (outcome) {
        VerificationOutcome.SUCCESS -> AgentSessionStatus.COMPLETED
        VerificationOutcome.SETTLING -> AgentSessionStatus.EXECUTING
        VerificationOutcome.NO_EFFECT, VerificationOutcome.WRONG_STATE, VerificationOutcome.FAILURE -> AgentSessionStatus.FAILED
    }
}