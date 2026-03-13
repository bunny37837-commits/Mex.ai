package com.builder.aiphoneoperator.domain.agent

object PostActionVerifier {
    fun verifyTap(
        before: ScreenObservation?,
        after: ScreenObservation?,
        target: GroundedTarget,
        expectedSignals: List<String> = emptyList(),
    ): VerificationResult {
        if (before == null || after == null) return VerificationResult(VerificationOutcome.FAILURE, "Missing observation for verification.")
        if (UiStabilityMonitor.isTransient(after)) {
            return VerificationResult(VerificationOutcome.SETTLING, "UI is still settling after tap.")
        }
        if (hasBlockingRegression(after, expectedSignals)) {
            return VerificationResult(VerificationOutcome.WRONG_STATE, "Tap resulted in a blocking or wrong screen state.")
        }
        if (expectedSignals.isNotEmpty()) {
            return if (containsAny(after, expectedSignals)) {
                VerificationResult(VerificationOutcome.SUCCESS, "Expected screen signal appeared after tap.")
            } else {
                VerificationResult(VerificationOutcome.NO_EFFECT, "Expected screen signal did not appear after tap.")
            }
        }
        if (before.packageName != after.packageName || before.className != after.className) {
            return VerificationResult(VerificationOutcome.SUCCESS, "Screen changed after tap.")
        }
        val beforeNode = before.nodes.firstOrNull { it.id == target.id }
        val afterNode = after.nodes.firstOrNull { it.id == target.id }
        return when {
            beforeNode != null && afterNode == null -> VerificationResult(VerificationOutcome.SUCCESS, "Target disappeared after tap.")
            beforeNode != null && afterNode != null && (beforeNode.focused != afterNode.focused) -> VerificationResult(VerificationOutcome.SUCCESS, "Target state changed after tap.")
            else -> VerificationResult(VerificationOutcome.NO_EFFECT, "Tap had no observable effect.")
        }
    }

    fun verifyBack(before: ScreenObservation?, after: ScreenObservation?): VerificationResult {
        if (before == null || after == null) return VerificationResult(VerificationOutcome.FAILURE, "Missing observation for verification.")
        if (UiStabilityMonitor.isTransient(after)) {
            return VerificationResult(VerificationOutcome.SETTLING, "UI is still settling after back.")
        }
        return if (before.packageName != after.packageName || before.className != after.className) {
            VerificationResult(VerificationOutcome.SUCCESS, "Back changed the visible screen.")
        } else {
            VerificationResult(VerificationOutcome.NO_EFFECT, "Back had no observable effect.")
        }
    }

    fun verifyInput(after: ScreenObservation?, target: GroundedTarget, expectedText: String): VerificationResult {
        if (after == null) return VerificationResult(VerificationOutcome.FAILURE, "Missing observation for verification.")
        if (UiStabilityMonitor.isTransient(after)) {
            return VerificationResult(VerificationOutcome.SETTLING, "UI is still settling after input.")
        }
        if (hasBlockingRegression(after, listOf(expectedText))) {
            return VerificationResult(VerificationOutcome.WRONG_STATE, "Input resulted in a blocking or wrong state.")
        }
        val node = after.nodes.firstOrNull { it.id == target.id }
        val visibleText = listOfNotNull(node?.text, node?.contentDescription).joinToString(" ")
        return if (visibleText.contains(expectedText, ignoreCase = false) || containsAny(after, listOf(expectedText))) {
            VerificationResult(VerificationOutcome.SUCCESS, "Text input verified.")
        } else {
            VerificationResult(VerificationOutcome.NO_EFFECT, "Expected text not visible after input.")
        }
    }

    fun containsAny(observation: ScreenObservation, signals: List<String>): Boolean {
        val corpus = observation.nodes.joinToString(" ") {
            listOfNotNull(it.text, it.contentDescription, it.className).joinToString(" ")
        }.lowercase()
        return signals.any { signal ->
            val normalized = signal.lowercase().trim()
            normalized.isNotBlank() && corpus.contains(normalized)
        }
    }

    private fun hasBlockingRegression(observation: ScreenObservation, expectedSignals: List<String>): Boolean {
        if (observation.blockers.any { it.type == BlockerType.PERMISSION_DIALOG || it.type == BlockerType.SYSTEM_POPUP }) {
            return true
        }
        val hasErrorCopy = containsAny(observation, listOf("error", "try again", "failed", "not found"))
        return hasErrorCopy && expectedSignals.none { containsAny(observation, listOf(it)) }
    }
}