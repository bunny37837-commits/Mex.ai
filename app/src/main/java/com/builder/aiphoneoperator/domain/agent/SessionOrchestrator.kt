package com.builder.aiphoneoperator.domain.agent

import android.content.Context
import kotlinx.coroutines.delay

object SessionOrchestrator {
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

        repeat(12) {
            val observation = captureObservation()
            val role = ScreenRoleDetector.detect(observation, workflow.resolvedPackageName)
            onUpdate(AgentSessionStatus.EXECUTING, "Observed ${role.name.lowercase().replace('_', ' ')}.", stepIndex, totalSteps, retries, role.name)

            when (val decision = NavigationDecider.decide(workflow, observation, stepIndex, retries)) {
                is NavigationDecision.Complete -> {
                    onUpdate(AgentSessionStatus.COMPLETED, decision.reason, stepIndex, totalSteps, retries, role.name)
                    return AgentSession(
                        id = "",
                        commandText = workflow.rawGoal,
                        createdAt = System.currentTimeMillis(),
                        status = AgentSessionStatus.COMPLETED,
                        action = workflow,
                        message = decision.reason,
                        currentStepIndex = stepIndex,
                        totalSteps = totalSteps,
                        lastScreenRole = role.name,
                        retries = retries,
                    )
                }
                is NavigationDecision.Fail -> {
                    onUpdate(AgentSessionStatus.FAILED, decision.reason, stepIndex, totalSteps, retries, role.name)
                    return AgentSession(
                        id = "",
                        commandText = workflow.rawGoal,
                        createdAt = System.currentTimeMillis(),
                        status = AgentSessionStatus.FAILED,
                        action = workflow,
                        message = decision.reason,
                        error = decision.reason,
                        currentStepIndex = stepIndex,
                        totalSteps = totalSteps,
                        lastScreenRole = role.name,
                        retries = retries,
                    )
                }
                is NavigationDecision.Wait -> {
                    if (stepIndex < totalSteps && workflow.steps[stepIndex] is WorkflowStep.OpenAppStep && workflow.resolvedPackageName == observation?.packageName) {
                        stepIndex += 1
                    } else {
                        retries += 1
                    }
                    delay(350)
                }
                is NavigationDecision.LaunchApp -> {
                    onUpdate(AgentSessionStatus.EXECUTING, decision.reason, stepIndex, totalSteps, retries, role.name)
                    when (val result = launchApp(decision.action)) {
                        is ExecutionResult.Success -> {
                            if (decision.advanceStep) stepIndex += 1
                            delay(700)
                        }
                        is ExecutionResult.Failure -> {
                            retries += 1
                            onUpdate(AgentSessionStatus.EXECUTING, result.reason, stepIndex, totalSteps, retries, role.name)
                            delay(350)
                        }
                    }
                }
                is NavigationDecision.Execute -> {
                    onUpdate(AgentSessionStatus.EXECUTING, decision.reason, stepIndex, totalSteps, retries, role.name)
                    val before = observation
                    val executed = executeDeviceAction(decision.action)
                    delay(500)
                    val after = captureObservation()
                    val verification = verify(decision.action, before, after, decision.expectedSignals)
                    if (executed && verification.outcome == VerificationOutcome.SUCCESS) {
                        if (decision.advanceStep) stepIndex += 1
                        retries = 0
                        onUpdate(AgentSessionStatus.EXECUTING, verification.message, stepIndex, totalSteps, retries, ScreenRoleDetector.detect(after, workflow.resolvedPackageName).name)
                    } else {
                        retries += 1
                        onUpdate(AgentSessionStatus.EXECUTING, verification.message, stepIndex, totalSteps, retries, ScreenRoleDetector.detect(after, workflow.resolvedPackageName).name)
                    }
                }
            }
        }

        val finalObservation = captureObservation()
        val role = ScreenRoleDetector.detect(finalObservation, workflow.resolvedPackageName)
        val reason = "Loop ended before final verification succeeded."
        onUpdate(AgentSessionStatus.FAILED, reason, stepIndex, totalSteps, retries, role.name)
        return AgentSession(
            id = "",
            commandText = workflow.rawGoal,
            createdAt = System.currentTimeMillis(),
            status = AgentSessionStatus.FAILED,
            action = workflow,
            message = reason,
            error = reason,
            currentStepIndex = stepIndex,
            totalSteps = totalSteps,
            lastScreenRole = role.name,
            retries = retries,
        )
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
}