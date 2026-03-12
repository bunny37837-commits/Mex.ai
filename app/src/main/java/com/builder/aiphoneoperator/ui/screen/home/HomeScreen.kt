@file:OptIn(ExperimentalMaterial3Api::class)

package com.builder.aiphoneoperator.ui.screen.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.builder.aiphoneoperator.model.AiMode
import com.builder.aiphoneoperator.model.SampleData
import com.builder.aiphoneoperator.model.ServiceState
import com.builder.aiphoneoperator.model.SystemStatusUiState
import com.builder.aiphoneoperator.model.TaskHistoryItem
import com.builder.aiphoneoperator.ui.components.ChipStatus
import com.builder.aiphoneoperator.ui.components.CommandInputBar
import com.builder.aiphoneoperator.ui.components.HistoryTaskItem
import com.builder.aiphoneoperator.ui.components.QuickActionButton
import com.builder.aiphoneoperator.ui.components.SectionHeader
import com.builder.aiphoneoperator.ui.components.StatusChip
import com.builder.aiphoneoperator.ui.components.VoiceState
import com.builder.aiphoneoperator.ui.components.defaultQuickActions
import com.builder.aiphoneoperator.runtime.OperatorAppState
import com.builder.aiphoneoperator.ui.components.RecentTaskCard
import com.builder.aiphoneoperator.ui.theme.AiPhoneOperatorTheme
import com.builder.aiphoneoperator.ui.theme.StatusRed
import com.builder.aiphoneoperator.ui.theme.StatusRedDim

@Composable
fun HomeScreen(
    state: HomeUiState,
    appState: OperatorAppState,
    onOpenRunningTask: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenServiceStatus: () -> Unit,
    onOpenHistory: () -> Unit,
    onOpenMemory: () -> Unit,
    onOpenCapabilities: () -> Unit,
    onOpenEmergencyControls: () -> Unit,
    onOpenRepairMode: () -> Unit,
) {
    HomeScreenContent(
        status = appState.toSystemStatusUiState(),
        recentTasks = SampleData.taskHistory,
        onSendCommand = { onOpenRunningTask() },
        onNavigateHistory = onOpenHistory,
        onNavigateStatus = onOpenServiceStatus,
        onNavigateSettings = onOpenSettings,
        onNavigateEmergency = onOpenEmergencyControls,
        onNavigateRepairMode = onOpenRepairMode,
        onNavigateMemory = onOpenMemory,
        onNavigateCapabilities = onOpenCapabilities,
    )
}

@Composable
private fun HomeScreenContent(
    status: SystemStatusUiState,
    recentTasks: List<TaskHistoryItem>,
    onSendCommand: (String) -> Unit,
    onNavigateHistory: () -> Unit,
    onNavigateStatus: () -> Unit,
    onNavigateSettings: () -> Unit,
    onNavigateEmergency: () -> Unit,
    onNavigateRepairMode: () -> Unit,
    onNavigateMemory: () -> Unit,
    onNavigateCapabilities: () -> Unit,
) {
    var commandText by remember { mutableStateOf("") }
    var voiceState by remember { mutableStateOf(VoiceState.IDLE) }
    var showOverflow by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            HomeTopBar(
                status = status,
                showOverflow = showOverflow,
                onOverflow = { showOverflow = true },
                onDismiss = { showOverflow = false },
                onStatusClick = onNavigateStatus,
                onHistory = onNavigateHistory,
                onSettings = onNavigateSettings,
                onEmergency = onNavigateEmergency,
                onMemory = onNavigateMemory,
                onCapabilities = onNavigateCapabilities,
            )
        },
        bottomBar = {
            CommandInputBar(
                text = commandText,
                voiceState = voiceState,
                onTextChange = { commandText = it },
                onSend = {
                    if (commandText.isNotBlank()) {
                        onSendCommand(commandText)
                        commandText = ""
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
            contentPadding = PaddingValues(
                top = innerPadding.calculateTopPadding() + 8.dp,
                bottom = innerPadding.calculateBottomPadding() + 16.dp
            )
        ) {
            item {
                SystemStatusBar(
                    status = status,
                    onClick = onNavigateRepairMode,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                )
                Text(
                    text = "Status: ${if (status.serviceState == ServiceState.ACTIVE && status.accessibilityOn) "AI ready" else "Runtime not ready"}",
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                Text(
                    text = "Accessibility: ${if (status.accessibilityOn) "enabled" else "disabled"}",
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                Text(
                    text = "Service: ${status.serviceState.name.lowercase()}",
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                Spacer(Modifier.height(16.dp))
            }
            item {
                SectionHeader(title = "Quick Actions")
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    modifier = Modifier.padding(bottom = 8.dp)
                ) {
                    items(defaultQuickActions) { action ->
                        QuickActionButton(action = action, onClick = { onSendCommand(action.label) })
                    }
                }
                Spacer(Modifier.height(16.dp))
            }
            item {
                SectionHeader(title = "Recent", action = "View All →", onAction = onNavigateHistory)
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    modifier = Modifier.padding(bottom = 8.dp)
                ) {
                    items(recentTasks.take(6)) { task ->
                        RecentTaskCard(item = task, onClick = onNavigateHistory)
                    }
                }
                Spacer(Modifier.height(16.dp))
            }
            item {
                SectionHeader(title = "History", action = "View All →", onAction = onNavigateHistory)
            }
            if (recentTasks.isEmpty()) {
                item { HomeEmptyState() }
            } else {
                items(recentTasks.take(5)) { task ->
                    HistoryTaskItem(item = task, onClick = onNavigateHistory, onRetry = { onSendCommand(task.command) })
                }
            }
        }
    }
}

@Composable
private fun HomeTopBar(
    status: SystemStatusUiState,
    showOverflow: Boolean,
    onOverflow: () -> Unit,
    onDismiss: () -> Unit,
    onStatusClick: () -> Unit,
    onHistory: () -> Unit,
    onSettings: () -> Unit,
    onEmergency: () -> Unit,
    onMemory: () -> Unit,
    onCapabilities: () -> Unit,
) {
    val serviceChipStatus = when {
        status.serviceState == ServiceState.ACTIVE && status.accessibilityOn -> ChipStatus.ACTIVE
        status.serviceState == ServiceState.LOADING -> ChipStatus.LOADING
        else -> ChipStatus.ERROR
    }

    TopAppBar(
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("🤖", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.padding(horizontal = 4.dp))
                Text("AI Operator", style = MaterialTheme.typography.titleLarge)
            }
        },
        actions = {
            StatusChip(
                label = when (status.aiMode) {
                    AiMode.LOCAL -> "Local"
                    AiMode.API -> "API"
                    AiMode.FALLBACK -> "Fallback"
                    AiMode.OFFLINE -> "Offline"
                },
                status = serviceChipStatus,
                onClick = onStatusClick
            )
            Spacer(Modifier.padding(horizontal = 2.dp))
            IconButton(onClick = onOverflow) {
                Icon(Icons.Rounded.MoreVert, contentDescription = "More options")
            }
            DropdownMenu(expanded = showOverflow, onDismissRequest = onDismiss) {
                DropdownMenuItem(text = { Text("⏹ Emergency Controls", color = MaterialTheme.colorScheme.error) }, onClick = { onDismiss(); onEmergency() })
                HorizontalDivider()
                DropdownMenuItem(text = { Text("History") }, onClick = { onDismiss(); onHistory() })
                DropdownMenuItem(text = { Text("Memory") }, onClick = { onDismiss(); onMemory() })
                DropdownMenuItem(text = { Text("Capabilities") }, onClick = { onDismiss(); onCapabilities() })
                DropdownMenuItem(text = { Text("Settings") }, onClick = { onDismiss(); onSettings() })
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
    )
}

@Composable
private fun SystemStatusBar(status: SystemStatusUiState, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val hasIssue = !status.accessibilityOn || status.serviceState == ServiceState.ERROR || status.serviceState == ServiceState.INACTIVE
    Surface(
        shape = MaterialTheme.shapes.small,
        color = if (hasIssue) StatusRedDim else MaterialTheme.colorScheme.surfaceVariant,
        modifier = modifier.fillMaxWidth()
    ) {
        androidx.compose.foundation.layout.Column {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceEvenly,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                StatusChip(label = if (status.aiMode == AiMode.LOCAL) "Local · ${status.modelName}" else "API · ${status.modelName}", status = if (status.serviceState == ServiceState.ACTIVE) ChipStatus.ACTIVE else ChipStatus.ERROR, onClick = onClick)
                StatusChip(label = if (status.accessibilityOn) "A11y On" else "A11y Off", status = if (status.accessibilityOn) ChipStatus.ACTIVE else ChipStatus.ERROR, onClick = onClick)
                StatusChip(label = if (status.isPrivacyLocal) "Local-only" else "API", status = if (status.isPrivacyLocal) ChipStatus.ACTIVE else ChipStatus.WARNING, onClick = onClick)
            }
            AnimatedVisibility(visible = hasIssue) {
                Surface(color = StatusRedDim, modifier = Modifier.fillMaxWidth()) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text("⚠ Fix Required", style = MaterialTheme.typography.labelSmall, color = StatusRed)
                        Spacer(Modifier.padding(horizontal = 4.dp))
                        TextButton(onClick = onClick, contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp)) {
                            Text("Open Repair Mode →", style = MaterialTheme.typography.labelSmall, color = StatusRed)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HomeEmptyState() {
    androidx.compose.foundation.layout.Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth().padding(vertical = 48.dp)
    ) {
        Text("🤖", style = MaterialTheme.typography.displaySmall)
        Spacer(Modifier.height(16.dp))
        Text("Give your AI a command to get started", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0F1117)
@Composable
private fun HomeScreenPreview() {
    AiPhoneOperatorTheme(darkTheme = true) {
        HomeScreen(
            state = HomeUiState(),
            appState = com.builder.aiphoneoperator.runtime.OperatorAppState(),
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
