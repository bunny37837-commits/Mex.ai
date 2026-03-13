package com.builder.aiphoneoperator.domain.agent

enum class ScreenRole {
    PERMISSION_DIALOG,
    SYSTEM_POPUP,
    KEYBOARD_VISIBLE,
    HOME,
    APP_LAUNCHER,
    SEARCH,
    FORM,
    LIST,
    DETAIL,
    TARGET_APP,
    UNKNOWN,
}

object ScreenRoleDetector {
    fun detect(observation: ScreenObservation?, targetPackageName: String? = null): ScreenRole {
        if (observation == null) return ScreenRole.UNKNOWN
        if (observation.blockers.any { it.type == BlockerType.PERMISSION_DIALOG }) return ScreenRole.PERMISSION_DIALOG
        if (observation.blockers.any { it.type == BlockerType.SYSTEM_POPUP }) return ScreenRole.SYSTEM_POPUP
        if (observation.blockers.any { it.type == BlockerType.KEYBOARD_OBSTRUCTION }) return ScreenRole.KEYBOARD_VISIBLE

        val packageName = observation.packageName.orEmpty().lowercase()
        val textCorpus = observation.nodes.joinToString(" ") {
            listOfNotNull(it.text, it.contentDescription, it.className).joinToString(" ")
        }.lowercase()
        val clickableCount = observation.nodes.count { it.clickable }
        val editableCount = observation.nodes.count { it.editable }

        if (targetPackageName != null && packageName == targetPackageName.lowercase()) {
            return when {
                editableCount > 0 -> ScreenRole.FORM
                textCorpus.contains("search") -> ScreenRole.SEARCH
                clickableCount >= 6 -> ScreenRole.LIST
                clickableCount in 1..5 -> ScreenRole.DETAIL
                else -> ScreenRole.TARGET_APP
            }
        }
        if (packageName.contains("launcher") || packageName.contains("quickstep") || packageName.contains("miui.home")) return ScreenRole.HOME

        return when {
            textCorpus.contains("search") -> ScreenRole.SEARCH
            editableCount > 0 -> ScreenRole.FORM
            clickableCount >= 8 -> ScreenRole.LIST
            clickableCount in 1..7 -> ScreenRole.DETAIL
            packageName.contains("settings") || packageName.contains("launcher") -> ScreenRole.APP_LAUNCHER
            else -> ScreenRole.UNKNOWN
        }
    }
}