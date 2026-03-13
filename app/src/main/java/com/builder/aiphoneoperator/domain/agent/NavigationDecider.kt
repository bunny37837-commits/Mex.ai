package com.builder.aiphoneoperator.domain.agent

sealed interface NavigationDecision {
    data class Execute(val action: DeviceAction, val reason: String, val advanceStep: Boolean = true) : NavigationDecision
    data class LaunchApp(val action: ExecutionAction.OpenApp, val reason: String, val advanceStep: Boolean = true) : NavigationDecision
    data class Complete(val reason: String) : NavigationDecision
    data class Fail(val reason: String) : NavigationDecision
    data class Wait(val reason: String) : NavigationDecision
}

object NavigationDecider {
    fun decide(
        workflow: ExecutionAction.NavigateWorkflow,
        observation: ScreenObservation?,
        currentStepIndex: Int,
        retries: Int,
    ): NavigationDecision {
        val role = ScreenRoleDetector.detect(observation, workflow.resolvedPackageName)

        val blockerDecision = BlockerHandler.resolve(observation)
        if (blockerDecision != null) return blockerDecision

        if (currentStepIndex >= workflow.steps.size) {
            val ok = FinalStateVerifier.verify(workflow, observation)
            return if (ok) NavigationDecision.Complete("Task verified complete.")
            else wrongScreenRecovery(workflow, observation, retries)
        }

        return when (val step = workflow.steps[currentStepIndex]) {
            is WorkflowStep.OpenAppStep -> {
                val targetPackage = workflow.resolvedPackageName
                if (targetPackage != null && observation?.packageName == targetPackage) {
                    NavigationDecision.Wait("Target app is visible.")
                } else {
                    NavigationDecision.LaunchApp(
                        action = ExecutionAction.OpenApp(
                            query = step.query,
                            resolvedPackageName = workflow.resolvedPackageName,
                            resolvedLabel = workflow.resolvedLabel,
                        ),
                        reason = "Opening ${workflow.resolvedLabel ?: step.query}."
                    )
                }
            }
            is WorkflowStep.TapStep -> {
                val target = observation?.let { AccessibilityGroundingEngine.findTarget(it, step.label) }
                if (target != null) {
                    NavigationDecision.Execute(DeviceAction.Tap(target), "Tapping '${target.label}'.")
                } else if (role == ScreenRole.KEYBOARD_VISIBLE) {
                    NavigationDecision.Execute(DeviceAction.Back, "Keyboard is obstructing the screen.", advanceStep = false)
                } else {
                    wrongScreenRecovery(workflow, observation, retries, "Could not find '${step.label}'.")
                }
            }
            is WorkflowStep.InputStep -> {
                val node = observation?.nodes?.firstOrNull { it.focused && it.editable }
                    ?: observation?.nodes?.firstOrNull { it.editable }
                if (node != null) {
                    val target = GroundedTarget(
                        id = node.id,
                        label = node.text ?: node.contentDescription ?: "input field",
                        bounds = node.bounds,
                        source = GroundingSource.ACCESSIBILITY,
                        affordances = setOf(TargetAffordance.INPUT_TEXT),
                        confidence = 0.95f,
                    )
                    NavigationDecision.Execute(DeviceAction.InputText(target, step.text), "Entering text.")
                } else {
                    wrongScreenRecovery(workflow, observation, retries, "No editable field is focused.")
                }
            }
            WorkflowStep.BackStep -> NavigationDecision.Execute(DeviceAction.Back, "Navigating back.")
            is WorkflowStep.VerifyAppStep -> {
                if (FinalStateVerifier.verify(workflow, observation)) NavigationDecision.Complete("Task verified complete.")
                else wrongScreenRecovery(workflow, observation, retries)
            }
        }
    }

    private fun wrongScreenRecovery(
        workflow: ExecutionAction.NavigateWorkflow,
        observation: ScreenObservation?,
        retries: Int,
        reason: String = "Unexpected screen state.",
    ): NavigationDecision {
        return when {
            retries >= 3 -> NavigationDecision.Fail("$reason Recovery limit reached.")
            workflow.resolvedPackageName != null && observation?.packageName != workflow.resolvedPackageName -> {
                NavigationDecision.LaunchApp(
                    action = ExecutionAction.OpenApp(
                        query = workflow.appQuery ?: workflow.resolvedLabel ?: workflow.rawGoal,
                        resolvedPackageName = workflow.resolvedPackageName,
                        resolvedLabel = workflow.resolvedLabel,
                    ),
                    reason = "Recovering by reopening ${workflow.resolvedLabel ?: workflow.appQuery ?: "target app"}.",
                    advanceStep = false,
                )
            }
            else -> NavigationDecision.Execute(DeviceAction.Back, "$reason Recovering with back.", advanceStep = false)
        }
    }
}

private object BlockerHandler {
    fun resolve(observation: ScreenObservation?): NavigationDecision? {
        val blocker = observation?.blockers?.firstOrNull() ?: return null
        val nodes = observation.nodes
        return when (blocker.type) {
            BlockerType.PERMISSION_DIALOG -> {
                val allowNode = nodes.firstOrNull {
                    val text = listOfNotNull(it.text, it.contentDescription).joinToString(" ").lowercase()
                    it.clickable && (text.contains("allow") || text.contains("while using") || text.contains("ok"))
                } ?: return NavigationDecision.Fail("Permission dialog is blocking progress and no allow action was found.")
                NavigationDecision.Execute(
                    action = DeviceAction.Tap(
                        GroundedTarget(
                            id = allowNode.id,
                            label = allowNode.text ?: allowNode.contentDescription ?: "allow",
                            bounds = allowNode.bounds,
                            source = GroundingSource.ACCESSIBILITY,
                            affordances = setOf(TargetAffordance.TAP),
                            confidence = 0.98f,
                        )
                    ),
                    reason = "Accepting permission dialog.",
                    advanceStep = false,
                )
            }
            BlockerType.SYSTEM_POPUP -> {
                val dismissNode = nodes.firstOrNull {
                    val text = listOfNotNull(it.text, it.contentDescription).joinToString(" ").lowercase()
                    it.clickable && (text.contains("ok") || text.contains("close") || text.contains("dismiss") || text.contains("cancel") || text.contains("not now") || text.contains("continue"))
                }
                if (dismissNode != null) {
                    NavigationDecision.Execute(
                        action = DeviceAction.Tap(
                            GroundedTarget(
                                id = dismissNode.id,
                                label = dismissNode.text ?: dismissNode.contentDescription ?: "dismiss",
                                bounds = dismissNode.bounds,
                                source = GroundingSource.ACCESSIBILITY,
                                affordances = setOf(TargetAffordance.TAP),
                                confidence = 0.95f,
                            )
                        ),
                        reason = "Handling popup blocker.",
                        advanceStep = false,
                    )
                } else {
                    NavigationDecision.Execute(DeviceAction.Back, "Dismissing popup blocker.", advanceStep = false)
                }
            }
            BlockerType.KEYBOARD_OBSTRUCTION -> NavigationDecision.Execute(DeviceAction.Back, "Hiding keyboard blocker.", advanceStep = false)
        }
    }
}

private object FinalStateVerifier {
    fun verify(workflow: ExecutionAction.NavigateWorkflow, observation: ScreenObservation?): Boolean {
        if (observation == null) return false
        val packageMatches = workflow.resolvedPackageName == null || observation.packageName == workflow.resolvedPackageName
        val noBlockingPopup = observation.blockers.none { it.type == BlockerType.PERMISSION_DIALOG || it.type == BlockerType.SYSTEM_POPUP }
        return packageMatches && noBlockingPopup
    }
}