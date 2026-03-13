package com.builder.aiphoneoperator.domain.agent

sealed interface ExecutionAction {
    data class OpenApp(
        val query: String,
        val resolvedPackageName: String? = null,
        val resolvedLabel: String? = null,
    ) : ExecutionAction

    data class NavigateWorkflow(
        val rawGoal: String,
        val appQuery: String? = null,
        val steps: List<WorkflowStep>,
        val resolvedPackageName: String? = null,
        val resolvedLabel: String? = null,
        val successIndicators: List<String> = emptyList(),
    ) : ExecutionAction
}

sealed interface WorkflowStep {
    data class OpenAppStep(val query: String) : WorkflowStep
    data class TapStep(
        val label: String,
        val alternatives: List<String> = emptyList(),
        val expectAnyOf: List<String> = emptyList(),
    ) : WorkflowStep
    data class InputStep(
        val text: String,
        val fieldHints: List<String> = emptyList(),
    ) : WorkflowStep
    data object BackStep : WorkflowStep
    data class VerifyAppStep(val packageName: String?, val label: String?) : WorkflowStep
}