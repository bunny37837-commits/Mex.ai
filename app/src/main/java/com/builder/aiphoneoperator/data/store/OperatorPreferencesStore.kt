package com.builder.aiphoneoperator.data.store

import android.content.Context
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.operatorDataStore by preferencesDataStore(name = "operator_prefs")

data class OperatorPreferences(
    val hyperOsAutostartAcknowledged: Boolean = false,
    val onboardingCompletionSnapshot: Boolean = false,
    val safetyLockEnabled: Boolean = false,
    val localAiOnly: Boolean = true,
    val conversationMemoryEnabled: Boolean = false,
    val debugModeEnabled: Boolean = false,
    val lastServiceRunning: Boolean = false,
    val lastAccessibilityConnected: Boolean = false,
    val lastRuntimeCommand: String = "",
    val lastEventSummary: String = "",
    val lastMetadataUpdatedAt: Long = 0L,
)

class OperatorPreferencesStore(private val context: Context) {
    private object Keys {
        val hyperOsAutostartAcknowledged = booleanPreferencesKey("hyperos_autostart_acknowledged")
        val onboardingCompletionSnapshot = booleanPreferencesKey("onboarding_completion_snapshot")
        val safetyLockEnabled = booleanPreferencesKey("safety_lock_enabled")
        val localAiOnly = booleanPreferencesKey("local_ai_only")
        val conversationMemoryEnabled = booleanPreferencesKey("conversation_memory_enabled")
        val debugModeEnabled = booleanPreferencesKey("debug_mode_enabled")
        val lastServiceRunning = booleanPreferencesKey("last_service_running")
        val lastAccessibilityConnected = booleanPreferencesKey("last_accessibility_connected")
        val lastRuntimeCommand = stringPreferencesKey("last_runtime_command")
        val lastEventSummary = stringPreferencesKey("last_event_summary")
        val lastMetadataUpdatedAt = longPreferencesKey("last_metadata_updated_at")
    }

    val preferencesFlow: Flow<OperatorPreferences> = context.operatorDataStore.data.map { prefs ->
        OperatorPreferences(
            hyperOsAutostartAcknowledged = prefs[Keys.hyperOsAutostartAcknowledged] ?: false,
            onboardingCompletionSnapshot = prefs[Keys.onboardingCompletionSnapshot] ?: false,
            safetyLockEnabled = prefs[Keys.safetyLockEnabled] ?: false,
            localAiOnly = prefs[Keys.localAiOnly] ?: true,
            conversationMemoryEnabled = prefs[Keys.conversationMemoryEnabled] ?: false,
            debugModeEnabled = prefs[Keys.debugModeEnabled] ?: false,
            lastServiceRunning = prefs[Keys.lastServiceRunning] ?: false,
            lastAccessibilityConnected = prefs[Keys.lastAccessibilityConnected] ?: false,
            lastRuntimeCommand = prefs[Keys.lastRuntimeCommand] ?: "",
            lastEventSummary = prefs[Keys.lastEventSummary] ?: "",
            lastMetadataUpdatedAt = prefs[Keys.lastMetadataUpdatedAt] ?: 0L,
        )
    }

    suspend fun setHyperOsAutostartAcknowledged(value: Boolean) = mutate { it.copy(hyperOsAutostartAcknowledged = value) }
    suspend fun setOnboardingCompletionSnapshot(value: Boolean) = mutate { it.copy(onboardingCompletionSnapshot = value) }
    suspend fun setSafetyLockEnabled(value: Boolean) = mutate { it.copy(safetyLockEnabled = value) }
    suspend fun setLocalAiOnly(value: Boolean) = mutate { it.copy(localAiOnly = value) }
    suspend fun setConversationMemoryEnabled(value: Boolean) = mutate { it.copy(conversationMemoryEnabled = value) }
    suspend fun setDebugModeEnabled(value: Boolean) = mutate { it.copy(debugModeEnabled = value) }

    suspend fun setRuntimeMetadata(
        serviceRunning: Boolean,
        accessibilityConnected: Boolean,
        runtimeCommand: String,
        eventSummary: String,
    ) = mutate {
        it.copy(
            lastServiceRunning = serviceRunning,
            lastAccessibilityConnected = accessibilityConnected,
            lastRuntimeCommand = runtimeCommand,
            lastEventSummary = eventSummary,
            lastMetadataUpdatedAt = System.currentTimeMillis(),
        )
    }

    private suspend fun mutate(transform: (OperatorPreferences) -> OperatorPreferences) {
        val current = preferencesFlow.first()
        val next = transform(current)
        context.operatorDataStore.edit { prefs -> writePrefs(prefs, next) }
    }

    private fun writePrefs(prefs: MutablePreferences, value: OperatorPreferences) {
        prefs[Keys.hyperOsAutostartAcknowledged] = value.hyperOsAutostartAcknowledged
        prefs[Keys.onboardingCompletionSnapshot] = value.onboardingCompletionSnapshot
        prefs[Keys.safetyLockEnabled] = value.safetyLockEnabled
        prefs[Keys.localAiOnly] = value.localAiOnly
        prefs[Keys.conversationMemoryEnabled] = value.conversationMemoryEnabled
        prefs[Keys.debugModeEnabled] = value.debugModeEnabled
        prefs[Keys.lastServiceRunning] = value.lastServiceRunning
        prefs[Keys.lastAccessibilityConnected] = value.lastAccessibilityConnected
        prefs[Keys.lastRuntimeCommand] = value.lastRuntimeCommand
        prefs[Keys.lastEventSummary] = value.lastEventSummary
        prefs[Keys.lastMetadataUpdatedAt] = value.lastMetadataUpdatedAt
    }
}
