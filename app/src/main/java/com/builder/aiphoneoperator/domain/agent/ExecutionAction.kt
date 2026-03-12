package com.builder.aiphoneoperator.domain.agent

sealed interface ExecutionAction {
    data class OpenApp(
        val query: String,
        val resolvedPackageName: String? = null,
        val resolvedLabel: String? = null,
    ) : ExecutionAction
}
