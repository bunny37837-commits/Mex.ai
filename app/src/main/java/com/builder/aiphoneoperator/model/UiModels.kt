package com.builder.aiphoneoperator.model

import androidx.compose.runtime.Stable

// ── Enums ─────────────────────────────────────────────────────────────────────

enum class ServiceState { ACTIVE, INACTIVE, LOADING, ERROR }
enum class TaskState    { RUNNING, PAUSED, COMPLETED, FAILED, CANCELLED, PARTIAL }
enum class StepState    { PENDING, RUNNING, COMPLETED, FAILED }
enum class AiMode       { LOCAL, API, FALLBACK, OFFLINE }
enum class SeverityLevel { CRITICAL, WARNING, OPTIONAL }
enum class MemoryTab    { MACROS, PATTERNS, DEVICE, CONTEXT }

enum class InterruptionReason {
    INCOMING_CALL,
    BATTERY_LOW,
    SCREEN_LOCKED,
    APP_SWITCHED,
    ACCESSIBILITY_LOST,
    SYSTEM_KILLED,
    INTERNET_LOST
}

enum class SensitiveActionType {
    SEND_MESSAGE,
    PLACE_CALL,
    CHANGE_SETTING,
    SHARE_SCREENSHOT,
    DESTRUCTIVE
}

enum class RepairRequirement {
    ACCESSIBILITY_SERVICE,
    RESTRICTED_SETTINGS,
    AI_SERVICE_RUNNING,
    BATTERY_OPTIMIZATION,
    AUTOSTART,
    MODEL_LOADED,
    NOTIFICATIONS
}

// ── UI State Data Classes ─────────────────────────────────────────────────────

@Stable
data class SystemStatusUiState(
    val aiMode            : AiMode          = AiMode.LOCAL,
    val modelName         : String          = "Phi-3 Mini",
    val accessibilityOn   : Boolean         = true,
    val isPrivacyLocal    : Boolean         = true,
    val serviceState      : ServiceState    = ServiceState.ACTIVE,
    val apiConnected      : Boolean         = false,
    val batteryOk         : Boolean         = true,
    val autostartOn       : Boolean         = true
)

@Stable
data class TaskHistoryItem(
    val id          : String,
    val command     : String,
    val state       : TaskState,
    val stepCount   : Int,
    val elapsedMs   : Long,
    val timestamp   : String,           // display string e.g. "14:32"
    val dateLabel   : String            // e.g. "Today", "Yesterday"
)

@Stable
data class TaskStep(
    val index       : Int,
    val label       : String,
    val state       : StepState,
    val subLabel    : String    = "",   // live status text for running step
    val timestampMs : Long      = 0L
)

@Stable
data class RunningTaskUiState(
    val command         : String            = "",
    val taskState       : TaskState         = TaskState.RUNNING,
    val steps           : List<TaskStep>    = emptyList(),
    val currentStep     : Int               = 0,
    val elapsedSeconds  : Int               = 0,
    val isSafe          : Boolean           = true,
    val safetyNote      : String            = "",
    val interruption    : InterruptionReason? = null,
    val pendingAction   : SensitiveActionType? = null,
    val logLines        : List<String>      = emptyList()
)

@Stable
data class RequirementStatus(
    val requirement : RepairRequirement,
    val isOk        : Boolean,
    val severity    : SeverityLevel,
    val title       : String,
    val description : String,
    val impactLine  : String
)

@Stable
data class MemoryMacro(
    val id          : String,
    val name        : String,
    val steps       : List<String>,
    val runCount    : Int,
    val lastRun     : String
)

@Stable
data class MemoryPattern(
    val command     : String,
    val usageCount  : Int,
    val successRate : Int,          // 0–100
    val avgDurationMs: Long
)

@Stable
data class CapabilityItem(
    val iconName    : String,       // Material Symbols name reference
    val label       : String,
    val description : String,
    val exampleCmd  : String,
    val category    : String
)

@Stable
data class LogEntry(
    val timestamp   : String,
    val message     : String,
    val type        : LogType
)

enum class LogType { NAV, SENSITIVE, SUCCESS, ERROR, INFO }

// ── Sample / Placeholder Data ─────────────────────────────────────────────────

object SampleData {

    val systemStatus = SystemStatusUiState()

    val taskSteps = listOf(
        TaskStep(0, "Opened WhatsApp",            StepState.COMPLETED),
        TaskStep(1, "Navigated to Chats",         StepState.COMPLETED),
        TaskStep(2, "Searching for 'Mom'…",       StepState.RUNNING, "Scrolling contact list…"),
        TaskStep(3, "Composing message",          StepState.PENDING),
        TaskStep(4, "Confirm & Send",             StepState.PENDING)
    )

    val runningTask = RunningTaskUiState(
        command        = "Send a WhatsApp message to Mom: 'On my way'",
        taskState      = TaskState.RUNNING,
        steps          = taskSteps,
        currentStep    = 2,
        elapsedSeconds = 12,
        logLines       = listOf(
            "14:32:01 → UI Click: WhatsApp icon",
            "14:32:02 → Screen: Chat list loaded",
            "14:32:04 → Scroll: 3 items down",
            "14:32:06 → Searching: 'Mom'…"
        )
    )

    val taskHistory = listOf(
        TaskHistoryItem("1", "Open WhatsApp",             TaskState.COMPLETED, 5, 8200,  "14:32", "Today"),
        TaskHistoryItem("2", "Read last 5 notifications", TaskState.COMPLETED, 3, 4100,  "14:18", "Today"),
        TaskHistoryItem("3", "Set timer for 10 minutes",  TaskState.FAILED,    2, 1200,  "13:55", "Today"),
        TaskHistoryItem("4", "Turn on Wi-Fi",             TaskState.COMPLETED, 2, 2800,  "11:30", "Today"),
        TaskHistoryItem("5", "Open Camera",               TaskState.COMPLETED, 1, 900,   "09:15", "Yesterday"),
        TaskHistoryItem("6", "Send SMS to Dad",           TaskState.CANCELLED, 4, 15000, "18:40", "Yesterday")
    )

    val requirements = listOf(
        RequirementStatus(RepairRequirement.ACCESSIBILITY_SERVICE, false, SeverityLevel.CRITICAL,
            "Accessibility Service", "AI cannot read or interact with any UI",
            "Without this, AI cannot start any task."),
        RequirementStatus(RepairRequirement.RESTRICTED_SETTINGS, false, SeverityLevel.CRITICAL,
            "Restricted Settings", "Required to bind accessibility on Android 13+",
            "Without this, AI cannot start any task."),
        RequirementStatus(RepairRequirement.AI_SERVICE_RUNNING, true, SeverityLevel.CRITICAL,
            "AI Service Running", "Foreground service is active",
            "Without this, no tasks can execute."),
        RequirementStatus(RepairRequirement.BATTERY_OPTIMIZATION, false, SeverityLevel.WARNING,
            "Battery Optimization", "Service may be killed mid-task",
            "Tasks may stop unexpectedly on long operations."),
        RequirementStatus(RepairRequirement.AUTOSTART, false, SeverityLevel.WARNING,
            "Autostart (HyperOS)", "Service will not survive screen-off on MIUI/HyperOS",
            "Tasks may fail after screen turns off."),
        RequirementStatus(RepairRequirement.MODEL_LOADED, true, SeverityLevel.WARNING,
            "Model Loaded", "Phi-3 Mini · 3.8 GB loaded",
            "Without a model, AI has no intelligence."),
        RequirementStatus(RepairRequirement.NOTIFICATIONS, true, SeverityLevel.OPTIONAL,
            "Notifications", "Task alerts are enabled",
            "Without this you won't receive task progress updates.")
    )

    val macros = listOf(
        MemoryMacro("m1", "Morning Routine",
            listOf("Open News app", "Read top emails", "Check calendar"),
            47, "Today 08:14"),
        MemoryMacro("m2", "Night Mode",
            listOf("Enable Do Not Disturb", "Lower brightness to 20%", "Turn off Wi-Fi"),
            12, "Yesterday 22:30")
    )

    val patterns = listOf(
        MemoryPattern("Open Settings",         24, 100, 3100),
        MemoryPattern("Read notifications",    18, 94,  4500),
        MemoryPattern("Open WhatsApp",         31, 97,  2800),
        MemoryPattern("Turn on Bluetooth",     9,  88,  2200)
    )

    val logLines = listOf(
        LogEntry("14:32:01", "UI Click: WhatsApp icon",     LogType.NAV),
        LogEntry("14:32:02", "Screen: Chat list loaded",    LogType.NAV),
        LogEntry("14:32:04", "Scroll: 3 items down",        LogType.INFO),
        LogEntry("14:32:06", "Sensitive: message content",  LogType.SENSITIVE),
        LogEntry("14:32:08", "Task completed successfully", LogType.SUCCESS)
    )
}
