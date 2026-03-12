package com.builder.aiphoneoperator.domain.agent

sealed interface ExecutionResult {
    data class Success(val message: String) : ExecutionResult
    data class Failure(val reason: String) : ExecutionResult
}
