package com.builder.aiphoneoperator.domain.agent

import kotlinx.coroutines.delay

data class UiSettleResult(
    val state: UiSettleState,
    val observation: ScreenObservation?,
    val attempts: Int,
)

enum class UiSettleState {
    SETTLED,
    STILL_SETTLING,
    NO_PROGRESS,
}

object UiStabilityMonitor {
    private val loadingHints = listOf("loading", "please wait", "opening", "starting", "just a sec", "buffering")

    fun isTransient(observation: ScreenObservation?): Boolean {
        if (observation == null) return true
        if (observation.blockers.any { it.type == BlockerType.PERMISSION_DIALOG || it.type == BlockerType.SYSTEM_POPUP }) return false
        val corpus = observation.nodes.joinToString(" ") { listOfNotNull(it.text, it.contentDescription).joinToString(" ") }.lowercase()
        val actionableCount = observation.nodes.count { it.clickable || it.editable }
        val smallTree = observation.nodes.size <= 2
        return loadingHints.any { corpus.contains(it) } || (smallTree && actionableCount == 0)
    }

    suspend fun waitForSettled(
        initial: ScreenObservation?,
        captureObservation: () -> ScreenObservation?,
        maxAttempts: Int = 4,
        delayMs: Long = 250L,
    ): UiSettleResult {
        var previous = initial
        repeat(maxAttempts) { index ->
            val current = if (index == 0) previous else captureObservation()
            if (!isTransient(current) && sameFingerprint(previous, current)) {
                return UiSettleResult(UiSettleState.SETTLED, current, index + 1)
            }
            previous = current
            delay(delayMs)
        }
        val finalObservation = captureObservation()
        return if (isTransient(finalObservation)) {
            UiSettleResult(UiSettleState.STILL_SETTLING, finalObservation, maxAttempts)
        } else {
            UiSettleResult(UiSettleState.NO_PROGRESS, finalObservation, maxAttempts)
        }
    }

    fun sameFingerprint(first: ScreenObservation?, second: ScreenObservation?): Boolean {
        if (first == null || second == null) return false
        if (first.packageName != second.packageName || first.className != second.className) return false
        val firstIds = first.nodes.take(12).map { it.id }
        val secondIds = second.nodes.take(12).map { it.id }
        return firstIds == secondIds
    }
}