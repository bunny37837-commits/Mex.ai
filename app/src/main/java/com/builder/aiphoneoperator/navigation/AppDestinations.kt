package com.builder.aiphoneoperator.navigation

sealed class AppDestination(val route: String) {
    data object Home : AppDestination("home")
    data object RunningTask : AppDestination("running_task")
    data object Onboarding : AppDestination("onboarding")
    data object RepairMode : AppDestination("repair_mode")
    data object Settings : AppDestination("settings")
    data object ServiceStatus : AppDestination("service_status")
    data object History : AppDestination("history")
    data object Memory : AppDestination("memory")
    data object Capabilities : AppDestination("capabilities")
    data object EmergencyControls : AppDestination("emergency_controls")
}
