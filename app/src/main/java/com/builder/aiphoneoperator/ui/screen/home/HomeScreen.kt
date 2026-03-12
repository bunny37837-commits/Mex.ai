@file:OptIn(ExperimentalMaterial3Api::class)

package com.builder.aiphoneoperator.ui.screen.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
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
    var commandText by rememberSaveable { mutableStateOf("") }
    var voiceState by rememberSaveable { mutableStateOf(VoiceState.IDLE) }
    val session = appState.activeSession
    val system = appState.toSystemStatusUiState()

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
                    val trimmed = commandText.trim()
                    if (trimmed.isNotEmpty()) {
                        onSubmitCommand(trimmed)
                        commandText = ""
                        onOpenRunningTask()
                    }
                },
                onVoiceToggle = {
                    voiceState = if (voiceState == VoiceState.IDLE) VoiceState.RECORDING else VoiceState.IDLE
                }
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = 16.dp,
                end = 16.dp,
                top = innerPadding.calculateTopPadding() + 12.dp,
                bottom = innerPadding.calculateBottomPadding() + 16.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                AgentStatusCard(
                    title = state.summary,
                    capability = "Open installed apps by name",
                    accessibility = if (system.accessibilityOn) "Connected" else "Disconnected",
                    service = system.serviceState.name.lowercase().replaceFirstChar { it.uppercase() },
                )
            }
            item {
                SessionCard(
                    title = if (session == null) "No active session" else "Current session",
                    body = when {
                        session == null -> "Try commands like ‘open whatsapp’ or ‘launch camera’."
                        session.error != null -> "${session.message}\n${session.error}"
                        else -> session.message
                    },
                    footer = session?.status?.name?.lowercase()?.replace('_', ' ') ?: "idle",
                    onOpen = onOpenRunningTask,
                )
            }
            item {
                QuickActionsSection(
                    onOpenCapabilities = onOpenCapabilities,
                    onOpenHistory = onOpenHistory,
                    onOpenSettings = onOpenSettings,
                    onOpenServiceStatus = onOpenServiceStatus,
                    onOpenRepairMode = onOpenRepairMode,
                    onOpenEmergencyControls = onOpenEmergencyControls,
                )
            }
            item {
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Current truth", style = MaterialTheme.typography.titleMedium)
                        Text("• API integration is not implemented yet")
                        Text("• Local model loading is not implemented yet")
                        Text("• Memory screen is not yet backed by real knowledge storage")
                        OutlinedButton(onClick = onOpenMemory, modifier = Modifier.fillMaxWidth()) {
                            Text("Open memory surface")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AgentStatusCard(
    title: String,
    capability: String,
    accessibility: String,
    service: String,
) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Text("Capability: $capability")
            Text("Accessibility: $accessibility")
            Text("Service: $service")
        }
    }
}

@Composable
private fun SessionCard(
    title: String,
    body: String,
    footer: String,
    onOpen: () -> Unit,
) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Text(body, style = MaterialTheme.typography.bodyMedium)
            Text("State: $footer", style = MaterialTheme.typography.labelMedium)
            Button(onClick = onOpen, modifier = Modifier.fillMaxWidth()) {
                Text("Open session")
            }
        }
    }
}

@Composable
private fun QuickActionsSection(
    onOpenCapabilities: () -> Unit,
    onOpenHistory: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenServiceStatus: () -> Unit,
    onOpenRepairMode: () -> Unit,
    onOpenEmergencyControls: () -> Unit,
) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Agent tools", style = MaterialTheme.typography.titleMedium)
            OutlinedButton(onClick = onOpenCapabilities, modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)) { Text("Capabilities") }
            OutlinedButton(onClick = onOpenHistory, modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)) { Text("History") }
            OutlinedButton(onClick = onOpenSettings, modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)) { Text("Settings") }
            OutlinedButton(onClick = onOpenServiceStatus, modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)) { Text("Service status") }
            OutlinedButton(onClick = onOpenRepairMode, modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)) { Text("Repair mode") }
            OutlinedButton(onClick = onOpenEmergencyControls, modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)) { Text("Emergency controls") }
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
