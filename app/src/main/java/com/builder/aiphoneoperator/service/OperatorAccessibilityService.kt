package com.builder.aiphoneoperator.service

import android.accessibilityservice.AccessibilityService
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import com.builder.aiphoneoperator.data.repository.OperatorRepository

class OperatorAccessibilityService : AccessibilityService() {
    override fun onServiceConnected() {
        super.onServiceConnected()
        OperatorRepository.setAccessibilityConnected(true)
        Log.d(TAG, "Accessibility service connected")
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        OperatorRepository.setAccessibilityEvent(event)
        onAccessibilityEventObserved(event)
    }

    private fun onAccessibilityEventObserved(event: AccessibilityEvent?) {
        // Event listener skeleton only. No automation or task execution here yet.
        Log.v(TAG, "Observed accessibility event: ${event?.eventType} from ${event?.packageName}")
    }

    override fun onInterrupt() {
        Log.w(TAG, "Accessibility service interrupted")
    }

    override fun onDestroy() {
        OperatorRepository.setAccessibilityConnected(false)
        super.onDestroy()
    }

    companion object {
        private const val TAG = "OperatorA11yService"
    }
}
