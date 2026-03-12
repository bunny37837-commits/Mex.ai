package com.builder.aiphoneoperator.data.repository

import android.content.Context
import android.view.accessibility.AccessibilityEvent
import com.builder.aiphoneoperator.domain.agent.ScreenObservation
import com.builder.aiphoneoperator.model.RequirementStatus
import com.builder.aiphoneoperator.runtime.OperatorAppState
import kotlinx.coroutines.flow.StateFlow

interface AppStateRepository {
    val appState: StateFlow<OperatorAppState>
    val repairStatus: StateFlow<List<RequirementStatus>>

    suspend fun initialize(context: Context)
    suspend fun refreshStatuses()

    fun setAccessibilityConnected(connected: Boolean)
    fun setAccessibilityEvent(event: AccessibilityEvent?)
    fun setForegroundRunning(running: Boolean)

    fun captureObservation(): ScreenObservation?
    fun tapTargetByText(query: String): Boolean
    fun inputTextIntoFocusedField(text: String): Boolean
    fun performBack(): Boolean

    fun submitCommand(command: String)
    fun pauseTask()
    fun stopTask()
    fun cancelTask()
}
