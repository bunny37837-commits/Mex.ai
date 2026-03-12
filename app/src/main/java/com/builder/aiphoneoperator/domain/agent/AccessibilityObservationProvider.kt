package com.builder.aiphoneoperator.domain.agent

import com.builder.aiphoneoperator.service.OperatorAccessibilityBridge

object AccessibilityObservationProvider {
    fun capture(): ScreenObservation? = OperatorAccessibilityBridge.captureObservation()
}
