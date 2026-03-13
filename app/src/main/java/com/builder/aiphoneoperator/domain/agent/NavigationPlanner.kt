package com.builder.aiphoneoperator.domain.agent

object NavigationPlanner {
    fun build(command: AgentCommand): ExecutionAction.NavigateWorkflow? {
        val raw = command.rawText.trim()
        if (raw.isBlank()) return null

        val segments = raw.split(Regex("\\b(?:and then|then|after that)\\b", RegexOption.IGNORE_CASE))
            .map { it.trim(' ', ',', '.') }
            .filter { it.isNotBlank() }

        val steps = mutableListOf<WorkflowStep>()
        val successIndicators = mutableListOf<String>()
        var appQuery: String? = null

        for (segment in segments) {
            val normalized = segment.lowercase()
            when {
                normalized.startsWith("open ") || normalized.startsWith("launch ") -> {
                    val query = segment.substringAfter(' ').trim().trim('"')
                    if (query.isNotBlank()) {
                        appQuery = appQuery ?: query
                        steps += WorkflowStep.OpenAppStep(query)
                        successIndicators += query
                    }
                }
                normalized.startsWith("tap ") || normalized.startsWith("click ") || normalized.startsWith("press ") || normalized.startsWith("select ") -> {
                    val label = segment.substringAfter(' ').trim().trim('"')
                    if (label.isNotBlank()) {
                        steps += WorkflowStep.TapStep(
                            label = label,
                            alternatives = synonymAlternatives(label),
                            expectAnyOf = expectationHints(label),
                        )
                        successIndicators += expectationHints(label)
                    }
                }
                normalized.startsWith("type ") || normalized.startsWith("enter ") || normalized.startsWith("input ") -> {
                    val text = segment.substringAfter(' ').trim().trim('"')
                    if (text.isNotBlank()) {
                        steps += WorkflowStep.InputStep(text = text, fieldHints = listOf("search", "name", "message", "title"))
                        successIndicators += text
                    }
                }
                normalized.startsWith("search for ") -> {
                    val query = segment.removePrefix("search for ").trim().trim('"')
                    if (query.isNotBlank()) {
                        steps += WorkflowStep.TapStep(
                            label = "search",
                            alternatives = listOf("search", "find", "search box", "search field"),
                            expectAnyOf = listOf(query),
                        )
                        steps += WorkflowStep.InputStep(text = query, fieldHints = listOf("search", "find"))
                        successIndicators += query
                    }
                }
                normalized.startsWith("go to ") -> {
                    val label = segment.removePrefix("go to ").trim().trim('"')
                    if (label.isNotBlank()) {
                        steps += WorkflowStep.TapStep(label = label, alternatives = synonymAlternatives(label), expectAnyOf = expectationHints(label))
                        successIndicators += expectationHints(label)
                    }
                }
                normalized.startsWith("verify ") || normalized.startsWith("confirm ") -> {
                    val hint = segment.substringAfter(' ').trim().trim('"')
                    if (hint.isNotBlank()) successIndicators += hint
                }
                normalized == "back" || normalized == "go back" -> steps += WorkflowStep.BackStep
            }
        }

        if (steps.isEmpty() && appQuery == null) return null
        return ExecutionAction.NavigateWorkflow(
            rawGoal = raw,
            appQuery = appQuery,
            steps = steps,
            successIndicators = successIndicators.distinct(),
        )
    }

    private fun synonymAlternatives(label: String): List<String> {
        val normalized = label.lowercase()
        return buildList {
            add(label)
            when {
                normalized == "search" -> addAll(listOf("find", "search box", "search field", "search here"))
                normalized == "settings" -> addAll(listOf("preferences", "options"))
                normalized == "continue" -> addAll(listOf("next", "ok"))
                normalized == "done" -> addAll(listOf("save", "confirm", "finish"))
            }
        }.distinct()
    }

    private fun expectationHints(label: String): List<String> {
        val normalized = label.lowercase()
        return buildList {
            add(label)
            when {
                normalized.contains("settings") -> add("settings")
                normalized.contains("search") -> add("search")
                normalized.contains("profile") -> add("profile")
                normalized.contains("done") || normalized.contains("save") -> addAll(listOf("saved", "done", "updated"))
            }
        }.distinct()
    }
}