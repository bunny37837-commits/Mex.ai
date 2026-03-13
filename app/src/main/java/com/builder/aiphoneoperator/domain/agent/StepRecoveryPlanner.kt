package com.builder.aiphoneoperator.domain.agent

sealed interface RecoveryPlan {
    data class Action(
        val action: DeviceAction,
        val reason: String,
        val expectedSignals: List<String> = emptyList(),
    ) : RecoveryPlan
    data class ReopenApp(val action: ExecutionAction.OpenApp, val reason: String) : RecoveryPlan
    data class Wait(val reason: String) : RecoveryPlan
    data class Fail(val reason: String) : RecoveryPlan
}

object StepRecoveryPlanner {
    fun plan(
        workflow: ExecutionAction.NavigateWorkflow,
        step: WorkflowStep?,
        failedAction: DeviceAction?,
        before: ScreenObservation?,
        after: ScreenObservation?,
        retries: Int,
    ): RecoveryPlan {
        if (retries >= 3) return RecoveryPlan.Fail("Recovery limit reached.")
        if (UiStabilityMonitor.isTransient(after)) return RecoveryPlan.Wait("UI is still settling.")
        if (after?.blockers?.isNotEmpty() == true) return RecoveryPlan.Wait("Blocker detected after action.")

        return when (step) {
            is WorkflowStep.TapStep -> recoverTap(workflow, step, before, after, retries)
            is WorkflowStep.InputStep -> recoverInput(step, before, after, retries)
            is WorkflowStep.OpenAppStep -> recoverOpenApp(workflow, step, after)
            WorkflowStep.BackStep -> recoverBack(workflow, before, after)
            is WorkflowStep.VerifyAppStep, null -> genericRecovery(workflow, after)
        }
    }

    private fun recoverTap(
        workflow: ExecutionAction.NavigateWorkflow,
        step: WorkflowStep.TapStep,
        before: ScreenObservation?,
        after: ScreenObservation?,
        retries: Int,
    ): RecoveryPlan {
        val observation = after ?: before
        val alternateQueries = (step.alternatives + step.label).distinct().drop(retries)
        val alternate = observation?.let { AccessibilityGroundingEngine.findBestTarget(it, alternateQueries) }
        if (alternate != null) {
            return RecoveryPlan.Action(DeviceAction.Tap(alternate), "Retrying tap with alternate target '${alternate.label}'.", step.expectAnyOf)
        }
        if (workflow.resolvedPackageName != null && observation?.packageName != workflow.resolvedPackageName) {
            return RecoveryPlan.ReopenApp(
                ExecutionAction.OpenApp(workflow.appQuery ?: workflow.rawGoal, workflow.resolvedPackageName, workflow.resolvedLabel),
                "Reopening target app after tap failure.",
            )
        }
        return RecoveryPlan.Action(DeviceAction.Back, "Backing out after tap failure.")
    }

    private fun recoverInput(
        step: WorkflowStep.InputStep,
        before: ScreenObservation?,
        after: ScreenObservation?,
        retries: Int,
    ): RecoveryPlan {
        val observation = after ?: before ?: return RecoveryPlan.Fail("No observation available for input recovery.")
        val hints = if (step.fieldHints.isNotEmpty()) step.fieldHints else listOf("search", "text", "message")
        val field = AccessibilityGroundingEngine.findBestTarget(observation, hints, requireEditable = true)
        if (field != null && retries == 0) {
            return RecoveryPlan.Action(DeviceAction.Tap(field), "Refocusing input field.")
        }
        val focusedField = observation.nodes.firstOrNull { it.editable }
        if (focusedField != null) {
            val target = GroundedTarget(
                id = focusedField.id,
                label = focusedField.text ?: focusedField.contentDescription ?: "input field",
                bounds = focusedField.bounds,
                source = GroundingSource.ACCESSIBILITY,
                affordances = setOf(TargetAffordance.INPUT_TEXT),
                confidence = 0.85f,
            )
            return RecoveryPlan.Action(DeviceAction.InputText(target, step.text), "Retrying text input.", listOf(step.text))
        }
        return RecoveryPlan.Action(DeviceAction.Back, "Backing out after input failure.")
    }

    private fun recoverOpenApp(
        workflow: ExecutionAction.NavigateWorkflow,
        step: WorkflowStep.OpenAppStep,
        after: ScreenObservation?,
    ): RecoveryPlan {
        if (workflow.resolvedPackageName != null && after?.packageName == workflow.resolvedPackageName) {
            return RecoveryPlan.Wait("Target app became visible during recovery.")
        }
        return RecoveryPlan.ReopenApp(
            ExecutionAction.OpenApp(step.query, workflow.resolvedPackageName, workflow.resolvedLabel),
            "Retrying app launch.",
        )
    }

    private fun recoverBack(
        workflow: ExecutionAction.NavigateWorkflow,
        before: ScreenObservation?,
        after: ScreenObservation?,
    ): RecoveryPlan {
        return if (workflow.resolvedPackageName != null && after?.packageName != workflow.resolvedPackageName) {
            RecoveryPlan.ReopenApp(
                ExecutionAction.OpenApp(workflow.appQuery ?: workflow.rawGoal, workflow.resolvedPackageName, workflow.resolvedLabel),
                "Back led away from target flow; reopening app.",
            )
        } else if (before != null && after != null && UiStabilityMonitor.sameFingerprint(before, after)) {
            RecoveryPlan.Fail("Back had no effect and no safe recovery path remained.")
        } else {
            RecoveryPlan.Wait("Back changed state; reevaluating.")
        }
    }

    private fun genericRecovery(workflow: ExecutionAction.NavigateWorkflow, after: ScreenObservation?): RecoveryPlan {
        return if (workflow.resolvedPackageName != null && after?.packageName != workflow.resolvedPackageName) {
            RecoveryPlan.ReopenApp(
                ExecutionAction.OpenApp(workflow.appQuery ?: workflow.rawGoal, workflow.resolvedPackageName, workflow.resolvedLabel),
                "Recovering by reopening target app.",
            )
        } else {
            RecoveryPlan.Wait("Reevaluating current screen.")
        }
    }
}