package com.builder.aiphoneoperator.domain.agent

object AccessibilityGroundingEngine {
    fun findTarget(observation: ScreenObservation, query: String, requireEditable: Boolean = false): GroundedTarget? {
        val normalized = query.trim().lowercase()
        val node = observation.nodes.firstOrNull {
            val label = listOf(it.text, it.contentDescription).filterNotNull().joinToString(" ").lowercase()
            val affordanceOk = if (requireEditable) it.editable else it.clickable || it.editable
            affordanceOk && label.contains(normalized)
        } ?: return null
        val label = node.text ?: node.contentDescription ?: normalized
        return GroundedTarget(
            id = node.id,
            label = label,
            bounds = node.bounds,
            source = GroundingSource.ACCESSIBILITY,
            affordances = buildSet {
                if (node.clickable) add(TargetAffordance.TAP)
                if (node.editable) add(TargetAffordance.INPUT_TEXT)
            },
            confidence = 0.92f,
        )
    }
}
