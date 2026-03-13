package com.builder.aiphoneoperator.domain.agent

object NavigationPlanner {
    fun build(command: AgentCommand): ExecutionAction.NavigateWorkflow? {
        val raw = command.rawText.trim()
        if (raw.isBlank()) return null

        val segments = raw.split(Regex("\\b(?:and then|then)\\b", RegexOption.IGNORE_CASE))
            .map { it.trim(' ', ',', '.') }
            .filter { it.isNotBlank() }

        val steps = mutableListOf<WorkflowStep>()
        var appQuery: String? = null

        for (segment in segments) {
            val normalized = segment.lowercase()
            when {
                normalized.startsWith("open ") || normalized.startsWith("launch ") -> {
                    val query = segment.substringAfter(' ').trim()
                    if (query.isNotBlank()) {
                        appQuery = appQuery ?: query
                        steps += WorkflowStep.OpenAppStep(query)
                    }
                }
                normalized.startsWith("tap ") || normalized.startsWith("click ") || normalized.startsWith("press ") || normalized.startsWith("select ") -> {
                    val label = segment.substringAfter(' ').trim().trim('"')
                    if (label.isNotBlank()) steps += WorkflowStep.TapStep(label)
                }
                normalized.startsWith("type ") || normalized.startsWith("enter ") || normalized.startsWith("input ") -> {
                    val text = segment.substringAfter(' ').trim().trim('"')
                    if (text.isNotBlank()) steps += WorkflowStep.InputStep(text)
                }
                normalized.startsWith("search for ") -> {
                    val query = segment.removePrefix("search for ").trim().trim('"')
                    if (query.isNotBlank()) {
                        steps += WorkflowStep.TapStep("search")
                        steps += WorkflowStep.InputStep(query)
                    }
                }
                normalized == "back" || normalized == "go back" -> steps += WorkflowStep.BackStep
            }
        }

        if (steps.isEmpty() && appQuery == null) return null
        return ExecutionAction.NavigateWorkflow(rawGoal = raw, appQuery = appQuery, steps = steps)
    }
}