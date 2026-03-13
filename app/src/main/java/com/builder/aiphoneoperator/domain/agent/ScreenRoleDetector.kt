package com.builder.aiphoneoperator.domain.agent

enum class ScreenRole {
    PERMISSION_DIALOG,
    SYSTEM_POPUP,
    KEYBOARD_VISIBLE,
    HOME,
    APP_LAUNCHER,
    SEARCH,
    FORM,
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
        if (targetPackageName != null && packageName == targetPackageName.lowercase()) return ScreenRole.TARGET_APP
        if (packageName.contains("launcher") || packageName.contains("quickstep") || packageName.contains("miui.home")) return ScreenRole.HOME

        val allText = observation.nodes.joinToString(" ") {
            listOfNotNull(it.text, it.contentDescription, it.className).joinToString(" ")
        }.lowercase()

        return when {
            allText.contains("search") -> ScreenRole.SEARCH
            observation.nodes.any { it.editable } -> ScreenRole.FORM
            packageName.contains("settings") || packageName.contains("launcher") -> ScreenRole.APP_LAUNCHER
            else -> ScreenRole.UNKNOWN
        }
    }
}