package com.builder.aiphoneoperator.domain.agent

import kotlin.math.max

object AccessibilityGroundingEngine {
    fun findTarget(observation: ScreenObservation, query: String, requireEditable: Boolean = false): GroundedTarget? {
        return findBestTarget(observation, listOf(query), requireEditable)
    }

    fun findBestTarget(observation: ScreenObservation, queries: List<String>, requireEditable: Boolean = false): GroundedTarget? {
        val normalizedQueries = queries.map { it.normalizeForMatching() }.filter { it.isNotBlank() }.distinct()
        if (normalizedQueries.isEmpty()) return null

        val candidate = observation.nodes
            .asSequence()
            .filter { node ->
                if (!node.enabled) return@filter false
                if (requireEditable) node.editable else node.clickable || node.editable
            }
            .mapNotNull { node ->
                val labelParts = listOfNotNull(node.text, node.contentDescription, node.className)
                val normalizedLabel = labelParts.joinToString(" ").normalizeForMatching()
                if (normalizedLabel.isBlank()) return@mapNotNull null

                val bestScore = normalizedQueries.maxOf { query -> score(query, normalizedLabel, node) }
                if (bestScore <= 0) return@mapNotNull null
                node to bestScore
            }
            .sortedByDescending { (_, score) -> score }
            .firstOrNull()
            ?: return null

        val node = candidate.first
        val score = candidate.second
        val label = node.text ?: node.contentDescription ?: queries.first()
        return GroundedTarget(
            id = node.id,
            label = label,
            bounds = node.bounds,
            source = GroundingSource.ACCESSIBILITY,
            affordances = buildSet {
                if (node.clickable) add(TargetAffordance.TAP)
                if (node.editable) add(TargetAffordance.INPUT_TEXT)
            },
            confidence = score.coerceIn(0f, 1f),
        )
    }

    private fun score(query: String, label: String, node: UiNodeSnapshot): Float {
        var score = 0f
        if (label == query) score += 1f
        if (label.contains(query)) score += 0.72f
        if (query.contains(label) && label.length >= 3) score += 0.4f

        val queryTokens = query.split(' ').filter { it.isNotBlank() }
        val labelTokens = label.split(' ').filter { it.isNotBlank() }
        val sharedTokens = queryTokens.count { it in labelTokens }
        if (queryTokens.isNotEmpty()) {
            score += 0.2f * (sharedTokens.toFloat() / queryTokens.size.toFloat())
        }

        if (node.clickable) score += 0.08f
        if (node.focused) score += 0.05f
        if (node.editable) score += 0.05f

        return max(score, 0f)
    }
}

private fun String.normalizeForMatching(): String =
    lowercase()
        .replace(Regex("[^a-z0-9 ]"), " ")
        .replace(Regex("\\s+"), " ")
        .trim()