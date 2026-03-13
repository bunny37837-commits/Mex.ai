package com.builder.aiphoneoperator.domain.agent

import android.content.Context
import kotlinx.coroutines.delay

object SessionOrchestrator {
    private const val MAX_LOOP_ITERATIONS = 16
    private const val MAX_WAIT_RETRIES = 3

    suspend fun run(
        context: Context,
        workflow: ExecutionAction.NavigateWorkflow,
        onUpdate: (AgentSessionStatus, String, Int, Int, Int, String?) -> Unit,
        captureObservation: () -> ScreenObservation?,
        executeDeviceAction: (DeviceAction) -> Boolean,
        launchApp: (ExecutionAction.OpenApp) -> ExecutionResult,
    ): AgentSession {
        var stepIndex = 0
        var retries = 0
        val totalSteps = workflow.steps.size

        repeat(MAX_LOOP_ITERATIONS) {
            val observed = captureObservation()
            val settledBeforeDecision = UiStabilityMonitor.waitForSettled(observed, captureObservation)
            val observation = settledBeforeDecision.observation
            val role = ScreenRoleDetector.detect(observation, workflow.resolvedPackageName)
            onUpdate(AgentSessionStatus.EXECUTING, settleMessage("Observed ${role.name.lowercase().replace('_', ' ')}.", settledBeforeDecision), stepIndex, totalSteps, retries, role.name)

            when (val decision = NavigationDecider.decide(workflow, observation, stepIndex, retries)) {
                is NavigationDecision.Complete -> {
                    onUpdate(AgentSessionStatus.COMPLETED, decision.reason, stepIndex, totalSteps, retries, role.name)
                    return buildSession(workflow, AgentSessionStatus.COMPLETED, decision.reason, null, stepIndex, totalSteps, role.name, retries)
                }
                is NavigationDecision.Fail -> {
                    onUpdate(AgentSessionStatus.FAILED, decision.reason, stepIndex, totalSteps, retries, role.name)
                    return buildSession(workflow, AgentSessionStatus.FAILED, decision.reason, decision.reason, stepIndex, totalSteps, role.name, retries)
                }
                is NavigationDecision.Wait -> {
                    val waitOutcome = handleWait(workflow, observation, stepIndex, retries)
                    stepIndex = waitOutcome.stepIndex
                    retries = waitOutcome.retries
                    onUpdate(AgentSessionStatus.EXECUTING, waitOutcome.message, stepIndex, totalSteps, retries, role.name)
                    if (waitOutcome.failed) {
                        return buildSession(workflow, AgentSessionStatus.FAILED, waitOutcome.message, waitOutcome.message, stepIndex, totalSteps, role.name, retries)
                    }
                }
                is NavigationDecision.LaunchApp -> {
                    val result = executeLaunchApp(decision, stepIndex, totalSteps, retries, role.name, onUpdate, launchApp, captureObservation)
                    stepIndex = if (result.advanceStep) stepIndex + 1 else stepIndex
                    retries = result.retries
                    if (result.failed) {
                        return buildSession(workflow, AgentSessionStatus.FAILED, result.message, result.message, stepIndex, totalSteps, role.name, retries)
                    }
                }
                is NavigationDecision.Execute -> {
                    val step = workflow.steps.getOrNull(stepIndex)
                    val result = executeStep(
                        workflow = workflow,
                        step = step,
                        decision = decision,
                        retries = retries,
                        totalSteps = totalSteps,
                        stepIndex = stepIndex,
                        roleName = role.name,
                        captureObservation = captureObservation,
                        executeDeviceAction = executeDeviceAction,
                        launchApp = launchApp,
                        onUpdate = onUpdate,
                    )
                    stepIndex = if (result.advanceStep) stepIndex + 1 else stepIndex
                    retries = result.retries
                    if (result.failed) {
                        return buildSession(workflow, AgentSessionStatus.FAILED, result.message, result.message, stepIndex, totalSteps, role.name, retries)
                    }
                }
            }
        }

        val finalObservation = captureObservation()
        val role = ScreenRoleDetector.detect(finalObservation, workflow.resolvedPackageName)
        val reason = "Loop ended before final verification succeeded."
        onUpdate(AgentSessionStatus.FAILED, reason, stepIndex, totalSteps, retries, role.name)
        return buildSession(workflow, AgentSessionStatus.FAILED, reason, reason, stepIndex, totalSteps, role.name, retries)
    }

    private suspend fun executeStep(
        workflow: ExecutionAction.NavigateWorkflow,
        step: WorkflowStep?,
        decision: NavigationDecision.Execute,
        retries: Int,
        totalSteps: Int,
        stepIndex: Int,
        roleName: String,
        captureObservation: () -> ScreenObservation?,
        executeDeviceAction: (DeviceAction) -> Boolean,
        launchApp: (ExecutionAction.OpenApp) -> ExecutionResult,
        onUpdate: (AgentSessionStatus, String, Int, Int, Int, String?) -> Unit,
    ): StepOutcome {
        onUpdate(AgentSessionStatus.EXECUTING, decision.reason, stepIndex, totalSteps, retries, roleName)
        val before = captureObservation()
        val executed = executeDeviceAction(decision.action)
        delay(250)
        val settled = UiStabilityMonitor.waitForSettled(captureObservation(), captureObservation)
        val after = settled.observation
        val verification = verify(decision.action, before, after, decision.expectedSignals)

        return when {
            executed && verification.outcome == VerificationOutcome.SUCCESS -> {
                onUpdate(AgentSessionStatus.EXECUTING, verification.message, stepIndex + 1, totalSteps, 0, ScreenRoleDetector.detect(after, workflow.resolvedPackageName).name)
                StepOutcome(message = verification.message, retries = 0, advanceStep = decision.advanceStep)
            }
            verification.outcome == VerificationOutcome.SETTLING -> {
                val nextRetries = retries + 1
                val message = "${verification.message} Waiting for stability (${settled.attempts} checks)."
                onUpdate(AgentSessionStatus.EXECUTING, message, stepIndex, totalSteps, nextRetries, ScreenRoleDetector.detect(after, workflow.resolvedPackageName).name)
                StepOutcome(message = message, retries = nextRetries)
            }
            else -> {
                val recovery = StepRecoveryPlanner.plan(workflow, step, decision.action, before, after, retries)
                applyRecovery(recovery, workflow, stepIndex, totalSteps, retries + 1, captureObservation, executeDeviceAction, launchApp, onUpdate)
            }
        }
    }

    private suspend fun applyRecovery(
        recovery: RecoveryPlan,
        workflow: ExecutionAction.NavigateWorkflow,
        stepIndex: Int,
        totalSteps: Int,
        retries: Int,
        captureObservation: () -> ScreenObservation?,
        executeDeviceAction: (DeviceAction) -> Boolean,
        launchApp: (ExecutionAction.OpenApp) -> ExecutionResult,
        onUpdate: (AgentSessionStatus, String, Int, Int, Int, String?) -> Unit,
    ): StepOutcome {
        val roleName = ScreenRoleDetector.detect(captureObservation(), workflow.resolvedPackageName).name
        return when (recovery) {
            is RecoveryPlan.Fail -> {
                onUpdate(AgentSessionStatus.FAILED, recovery.reason, stepIndex, totalSteps, retries, roleName)
                StepOutcome(recovery.reason, retries, failed = true)
            }
            is RecoveryPlan.Wait -> {
                val settled = UiStabilityMonitor.waitForSettled(captureObservation(), captureObservation)
                val message = settleMessage(recovery.reason, settled)
                onUpdate(AgentSessionStatus.EXECUTING, message, stepIndex, totalSteps, retries, roleName)
                StepOutcome(message, retries)
            }
            is RecoveryPlan.ReopenApp -> {
                onUpdate(AgentSessionStatus.EXECUTING, recovery.reason, stepIndex, totalSteps, retries, roleName)
                when (val result = launchApp(recovery.action)) {
                    is ExecutionResult.Success -> {
                        val settled = UiStabilityMonitor.waitForSettled(captureObservation(), captureObservation)
                        val verified = verifyRecoveryState(workflow, settled.observation)
                        val message = if (verified) "${recovery.reason} Recovery verified." else "${recovery.reason} Recovery did not reach target state yet."
                        onUpdate(AgentSessionStatus.EXECUTING, message, stepIndex, totalSteps, retries, ScreenRoleDetector.detect(settled.observation, workflow.resolvedPackageName).name)
                        StepOutcome(message, retries)
                    }
                    is ExecutionResult.Failure -> {
                        onUpdate(AgentSessionStatus.EXECUTING, result.reason, stepIndex, totalSteps, retries, roleName)
                        StepOutcome(result.reason, retries, failed = retries >= MAX_WAIT_RETRIES)
                    }
                }
            }
            is RecoveryPlan.Action -> {
                onUpdate(AgentSessionStatus.EXECUTING, recovery.reason, stepIndex, totalSteps, retries, roleName)
                val before = captureObservation()
                val executed = executeDeviceAction(recovery.action)
                val settled = UiStabilityMonitor.waitForSettled(captureObservation(), captureObservation)
                val after = settled.observation
                val verification = verify(recovery.action, before, after, recovery.expectedSignals)
                val message = "${recovery.reason} ${verification.message}"
                onUpdate(AgentSessionStatus.EXECUTING, message, stepIndex, totalSteps, retries, ScreenRoleDetector.detect(after, workflow.resolvedPackageName).name)
                StepOutcome(message, retries, failed = !executed && retries >= MAX_WAIT_RETRIES)
            }
        }
    }

    private suspend fun executeLaunchApp(
        decision: NavigationDecision.LaunchApp,
        stepIndex: Int,
        totalSteps: Int,
        retries: Int,
        roleName: String,
        onUpdate: (AgentSessionStatus, String, Int, Int, Int, String?) -> Unit,
        launchApp: (ExecutionAction.OpenApp) -> ExecutionResult,
        captureObservation: () -> ScreenObservation?,
    ): StepOutcome {
        onUpdate(AgentSessionStatus.EXECUTING, decision.reason, stepIndex, totalSteps, retries, roleName)
        return when (val result = launchApp(decision.action)) {
            is ExecutionResult.Success -> {
                val settled = UiStabilityMonitor.waitForSettled(captureObservation(), captureObservation)
                val message = settleMessage(result.message, settled)
                onUpdate(AgentSessionStatus.EXECUTING, message, stepIndex, totalSteps, retries, ScreenRoleDetector.detect(settled.observation, decision.action.resolvedPackageName).name)
                StepOutcome(message = message, retries = 0, advanceStep = decision.advanceStep)
            }
            is ExecutionResult.Failure -> {
                val nextRetries = retries + 1
                onUpdate(AgentSessionStatus.EXECUTING, result.reason, stepIndex, totalSteps, nextRetries, roleName)
                StepOutcome(message = result.reason, retries = nextRetries, failed = nextRetries >= MAX_WAIT_RETRIES)
            }
        }
    }

    private fun handleWait(workflow: ExecutionAction.NavigateWorkflow, observation: ScreenObservation?, stepIndex: Int, retries: Int): StepOutcome {
        return if (stepIndex < workflow.steps.size && workflow.steps[stepIndex] is WorkflowStep.OpenAppStep && workflow.resolvedPackageName == observation?.packageName) {
            StepOutcome("Target app is visible.", retries = 0, advanceStep = true)
        } else {
            val nextRetries = retries + 1
            StepOutcome("Waiting did not produce grounded progress.", retries = nextRetries, failed = nextRetries >= MAX_WAIT_RETRIES)
        }
    }

    private fun verify(
        action: DeviceAction,
        before: ScreenObservation?,
        after: ScreenObservation?,
        expectedSignals: List<String> = emptyList(),
    ): VerificationResult {
        return when (action) {
            is DeviceAction.Tap -> PostActionVerifier.verifyTap(before, after, action.target, expectedSignals)
            DeviceAction.Back -> PostActionVerifier.verifyBack(before, after)
            is DeviceAction.InputText -> PostActionVerifier.verifyInput(after, action.target, action.text)
        }
    }

    private fun verifyRecoveryState(workflow: ExecutionAction.NavigateWorkflow, observation: ScreenObservation?): Boolean {
        if (observation == null) return false
        if (workflow.resolvedPackageName != null && observation.packageName != workflow.resolvedPackageName) return false
        return observation.blockers.none { it.type == BlockerType.PERMISSION_DIALOG || it.type == BlockerType.SYSTEM_POPUP }
    }

    private fun settleMessage(prefix: String, settled: UiSettleResult): String {
        return when (settled.state) {
            UiSettleState.SETTLED -> prefix
            UiSettleState.STILL_SETTLING -> "$prefix UI still settling after ${settled.attempts} checks."
            UiSettleState.NO_PROGRESS -> "$prefix UI showed no meaningful progress after ${settled.attempts} checks."
        }
    }

    private fun buildSession(
        workflow: ExecutionAction.NavigateWorkflow,
        status: AgentSessionStatus,
        message: String,
        error: String?,
        stepIndex: Int,
        totalSteps: Int,
        roleName: String?,
        retries: Int,
    ) = AgentSession(
        id = "",
        commandText = workflow.rawGoal,
        createdAt = System.currentTimeMillis(),
        status = status,
        action = workflow,
        message = message,
        error = error,
        currentStepIndex = stepIndex,
        totalSteps = totalSteps,
        lastScreenRole = roleName,
        retries = retries,
    )
}

private data class StepOutcome(
    val message: String,
    val retries: Int,
    val advanceStep: Boolean = false,
    val failed: Boolean = false,
)