package com.builder.aiphoneoperator.domain.agent

data class AgentCommand(
    val rawText: String,
    val normalizedText: String = rawText.trim().lowercase(),
)
