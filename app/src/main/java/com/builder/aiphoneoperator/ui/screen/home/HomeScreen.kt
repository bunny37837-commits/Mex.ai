@file:OptIn(ExperimentalMaterial3Api::class)

package com.builder.aiphoneoperator.ui.screen.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.builder.aiphoneoperator.runtime.OperatorAppState
import com.builder.aiphoneoperator.ui.components.CommandInputBar
import com.builder.aiphoneoperator.ui.components.VoiceState
import com.builder.aiphoneoperator.ui.theme.AiPhoneOperatorTheme

@Composable
fun HomeScreen(
    state: HomeUiState,
    appState: OperatorAppState,
    onSubmitCommand: (String) -> Unit,
    onOpenRunningTask: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenServiceStatus: () -> Unit,
    onOpenHistory: () -> Unit,
    onOpenMemory: () -> Unit,
    onOpenCapabilities: () -> Unit,
    onOpenEmergencyControls: () -> Unit,
    onOpenRepairMode: () -> Unit,
) {
    var commandText by remember { mutableStateOf("") }
    var voiceState by remember { mutableStateOf(VoiceState.IDLE) }
    val activeSession = appState.activeSession
    val systemStatus = appState.toSystemStatusUiState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(state.title) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        bottomBar = {
            CommandInputBar(
                text = commandText,
                voiceState = voiceState,
                onTextChange = { commandText = it },
                onSend = {
                    if (commandText.isNotBlank()) {
                        onSubmitCommand(commandText)
                        commandText = ""
                        onOpenRunningTask()
                    }
                },
                onVoiceToggle = {
                    voiceState = if (voiceState == VoiceState.IDLE) VoiceState.RECORDING else VoiceState.IDLE
                }
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(top = innerPadding.calculateTopPadding() + 16.dp, bottom = innerPadding.calculateBottomPadding() + 24.dp, start = 16.dp, end = 16.dp)
        ) {
            item { Text(state.summary) }
            item { Text("Real capability: open installed apps by name") }
            item { Text("Accessibility: ${if (systemStatus.accessibilityOn) "connected" else "not connected"}") }
            item { Text("Service: ${systemStatus.serviceState.name.lowercase()}") }
            item { Text("Privacy mode: ${if (appState.settings.localAiOnly) "Local-only" else "Mixed future mode"}") }
            item { Text("Conversation memory: ${appState.settings.conversationMemoryEnabled}") }
            item { Text("Debug mode: ${appState.settings.debugModeEnabled}") }
            item {
                Button(onClick = onOpenCapabilities, modifier = Modifier.fillMaxWidth().height(48.dp)) {
                    Text("View current capabilities")
                }
            }
            item {
                Button(onClick = onOpenHistory, modifier = Modifier.fillMaxWidth().height(48.dp)) {
                    Text("View real session history")
                }
            }
            item {
                Button(onClick = onOpenSettings, modifier = Modifier.fillMaxWidth().height(48.dp)) {
                    Text("Open settings")
                }
            }
            item {
                Text(
                    text = if (activeSession == null) "No active session. Try: open whatsapp" else "Active session: ${activeSession.status.name.lowercase()} · ${activeSession.message}",
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun HomeScreenPreview() {
    AiPhoneOperatorTheme {
        HomeScreen(
            state = HomeUiState(),
            appState = OperatorAppState(),
            onSubmitCommand = {},
            onOpenRunningTask = {},
            onOpenSettings = {},
            onOpenServiceStatus = {},
            onOpenHistory = {},
            onOpenMemory = {},
            onOpenCapabilities = {},
            onOpenEmergencyControls = {},
            onOpenRepairMode = {},
        )
    }
}
