package com.builder.aiphoneoperator.domain.agent

enum class AgentSessionStatus {
    CREATED,
    PARSING,
    UNSUPPORTED,
    RESOLVED,
    EXECUTING,
    BLOCKED,
    COMPLETED,
    FAILED,
    CANCELLED,
    PAUSED,
}

data class AgentSession(
    val id: String,
    val commandText: String,
    val createdAt: Long,
    val status: AgentSessionStatus,
    val action: ExecutionAction? = null,
    val message: String,
    val error: String? = null,
)
