package com.builder.aiphoneoperator.domain.agent

object CommandParser {
    fun parse(command: AgentCommand): ExecutionAction? {
        return NavigationPlanner.build(command)
            ?: parseLegacy(command)
    }

    private fun parseLegacy(command: AgentCommand): ExecutionAction? {
        val text = command.normalizedText
        return when {
            text.startsWith("open ") -> ExecutionAction.OpenApp(query = text.removePrefix("open ").trim())
            text.startsWith("launch ") -> ExecutionAction.OpenApp(query = text.removePrefix("launch ").trim())
            else -> null
        }
    }
}