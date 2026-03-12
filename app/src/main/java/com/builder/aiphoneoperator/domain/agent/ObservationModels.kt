package com.builder.aiphoneoperator.domain.agent

import android.graphics.Rect

data class UiNodeSnapshot(
    val id: String,
    val text: String?,
    val contentDescription: String?,
    val className: String?,
    val packageName: String?,
    val bounds: Rect,
    val clickable: Boolean,
    val editable: Boolean,
    val enabled: Boolean,
    val focused: Boolean,
)

data class Blocker(
    val type: BlockerType,
    val message: String,
    val nodeId: String? = null,
)

enum class BlockerType {
    PERMISSION_DIALOG,
    SYSTEM_POPUP,
    KEYBOARD_OBSTRUCTION,
}

data class ScreenObservation(
    val timestampMs: Long,
    val packageName: String?,
    val className: String?,
    val nodes: List<UiNodeSnapshot>,
    val blockers: List<Blocker>,
)

enum class GroundingSource {
    ACCESSIBILITY,
}

enum class TargetAffordance {
    TAP,
    INPUT_TEXT,
}

data class GroundedTarget(
    val id: String,
    val label: String,
    val bounds: Rect,
    val source: GroundingSource,
    val affordances: Set<TargetAffordance>,
    val confidence: Float,
)

sealed interface DeviceAction {
    data class Tap(val target: GroundedTarget) : DeviceAction
    data object Back : DeviceAction
    data class InputText(val target: GroundedTarget, val text: String) : DeviceAction
}

enum class VerificationOutcome {
    SUCCESS,
    NO_EFFECT,
    WRONG_STATE,
    FAILURE,
}

data class VerificationResult(
    val outcome: VerificationOutcome,
    val message: String,
)
