package com.builder.aiphoneoperator.domain.agent

object BlockerDetector {
    fun detect(observation: ScreenObservation): List<Blocker> {
        val blockers = mutableListOf<Blocker>()
        val nodes = observation.nodes
        val permissionNode = nodes.firstOrNull {
            val text = listOf(it.text, it.contentDescription).filterNotNull().joinToString(" ").lowercase()
            text.contains("allow") || text.contains("permission") || text.contains("while using the app")
        }
        if (permissionNode != null && observation.packageName == "com.android.permissioncontroller") {
            blockers += Blocker(BlockerType.PERMISSION_DIALOG, "Permission dialog is blocking progress.", permissionNode.id)
        }
        val popupNode = nodes.firstOrNull {
            val className = it.className.orEmpty()
            className.contains("Dialog", ignoreCase = true)
        }
        if (popupNode != null) {
            blockers += Blocker(BlockerType.SYSTEM_POPUP, "Dialog or popup detected.", popupNode.id)
        }
        val keyboardNode = nodes.firstOrNull {
            (it.packageName ?: "").contains("inputmethod", ignoreCase = true)
        }
        if (keyboardNode != null) {
            blockers += Blocker(BlockerType.KEYBOARD_OBSTRUCTION, "Keyboard is visible and may obstruct targets.", keyboardNode.id)
        }
        return blockers.distinctBy { it.type }
    }
}
