package com.builder.aiphoneoperator.domain.agent

import com.builder.aiphoneoperator.service.OperatorAccessibilityBridge

object AccessibilityActionExecutor {
    fun execute(action: DeviceAction): Boolean {
        return when (action) {
            is DeviceAction.Tap -> OperatorAccessibilityBridge.tap(action.target.id)
            DeviceAction.Back -> OperatorAccessibilityBridge.back()
            is DeviceAction.InputText -> OperatorAccessibilityBridge.inputText(action.target.id, action.text)
        }
    }
}
