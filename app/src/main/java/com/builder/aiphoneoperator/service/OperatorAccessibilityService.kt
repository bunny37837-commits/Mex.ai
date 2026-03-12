package com.builder.aiphoneoperator.service

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.graphics.Path
import android.graphics.Rect
import android.os.Bundle
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import com.builder.aiphoneoperator.data.repository.OperatorRepository
import com.builder.aiphoneoperator.domain.agent.BlockerDetector
import com.builder.aiphoneoperator.domain.agent.ScreenObservation
import com.builder.aiphoneoperator.domain.agent.UiNodeSnapshot

class OperatorAccessibilityService : AccessibilityService() {
    override fun onServiceConnected() {
        super.onServiceConnected()
        OperatorAccessibilityBridge.bind(this)
        OperatorRepository.setAccessibilityConnected(true)
        Log.d(TAG, "Accessibility service connected")
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        OperatorRepository.setAccessibilityEvent(event)
        onAccessibilityEventObserved(event)
    }

    private fun onAccessibilityEventObserved(event: AccessibilityEvent?) {
        Log.v(TAG, "Observed accessibility event: ${event?.eventType} from ${event?.packageName}")
    }

    override fun onInterrupt() {
        Log.w(TAG, "Accessibility service interrupted")
    }

    override fun onDestroy() {
        OperatorAccessibilityBridge.unbind(this)
        OperatorRepository.setAccessibilityConnected(false)
        super.onDestroy()
    }

    companion object {
        private const val TAG = "OperatorA11yService"
    }
}

object OperatorAccessibilityBridge {
    @Volatile
    private var service: OperatorAccessibilityService? = null

    fun bind(service: OperatorAccessibilityService) {
        this.service = service
    }

    fun unbind(service: OperatorAccessibilityService) {
        if (this.service === service) this.service = null
    }

    fun captureObservation(): ScreenObservation? {
        val root = service?.rootInActiveWindow ?: return null
        val nodes = mutableListOf<UiNodeSnapshot>()
        collectNodes(root, nodes)
        val observation = ScreenObservation(
            timestampMs = System.currentTimeMillis(),
            packageName = root.packageName?.toString(),
            className = root.className?.toString(),
            nodes = nodes,
            blockers = emptyList(),
        )
        return observation.copy(blockers = BlockerDetector.detect(observation))
    }

    fun tap(nodeId: String): Boolean {
        val root = service?.rootInActiveWindow ?: return false
        val node = findNodeById(root, nodeId) ?: return false
        if (node.isClickable && node.performAction(AccessibilityNodeInfo.ACTION_CLICK)) return true
        val rect = Rect().also { node.getBoundsInScreen(it) }
        val x = rect.centerX().toFloat()
        val y = rect.centerY().toFloat()
        val path = Path().apply { moveTo(x, y) }
        val gesture = GestureDescription.Builder()
            .addStroke(GestureDescription.StrokeDescription(path, 0, 50))
            .build()
        return service?.dispatchGesture(gesture, null, null) == true
    }

    fun inputText(nodeId: String, text: String): Boolean {
        val root = service?.rootInActiveWindow ?: return false
        val node = findNodeById(root, nodeId) ?: return false
        val args = Bundle().apply { putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, text) }
        return node.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, args)
    }

    fun back(): Boolean = service?.performGlobalAction(AccessibilityService.GLOBAL_ACTION_BACK) == true

    private fun collectNodes(node: AccessibilityNodeInfo, out: MutableList<UiNodeSnapshot>) {
        val bounds = Rect().also { node.getBoundsInScreen(it) }
        val id = buildString {
            append(node.viewIdResourceName ?: node.className ?: "node")
            append("#")
            append(bounds.left)
            append(":")
            append(bounds.top)
            append(":")
            append(bounds.right)
            append(":")
            append(bounds.bottom)
        }
        out += UiNodeSnapshot(
            id = id,
            text = node.text?.toString(),
            contentDescription = node.contentDescription?.toString(),
            className = node.className?.toString(),
            packageName = node.packageName?.toString(),
            bounds = bounds,
            clickable = node.isClickable,
            editable = node.isEditable,
            enabled = node.isEnabled,
            focused = node.isFocused,
        )
        for (i in 0 until node.childCount) {
            node.getChild(i)?.let { collectNodes(it, out) }
        }
    }

    private fun findNodeById(node: AccessibilityNodeInfo, targetId: String): AccessibilityNodeInfo? {
        val bounds = Rect().also { node.getBoundsInScreen(it) }
        val currentId = buildString {
            append(node.viewIdResourceName ?: node.className ?: "node")
            append("#")
            append(bounds.left)
            append(":")
            append(bounds.top)
            append(":")
            append(bounds.right)
            append(":")
            append(bounds.bottom)
        }
        if (currentId == targetId) return node
        for (i in 0 until node.childCount) {
            val child = node.getChild(i) ?: continue
            val found = findNodeById(child, targetId)
            if (found != null) return found
        }
        return null
    }
}
