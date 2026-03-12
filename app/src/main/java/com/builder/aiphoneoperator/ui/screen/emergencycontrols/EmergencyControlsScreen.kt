@file:OptIn(ExperimentalMaterial3Api::class)

package com.builder.aiphoneoperator.ui.screen.emergencycontrols

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
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
import com.builder.aiphoneoperator.runtime.OperatorAppState
import com.builder.aiphoneoperator.ui.theme.AiPhoneOperatorTheme
import com.builder.aiphoneoperator.ui.theme.StatusAmber
import com.builder.aiphoneoperator.ui.theme.StatusAmberDim
import com.builder.aiphoneoperator.ui.theme.StatusRedDim

@Composable
fun EmergencyControlsScreen(
    state: EmergencyControlsUiState,
    appState: OperatorAppState,
    onStopAll: () -> Unit,
    onPauseCurrent: () -> Unit,
    onCancelCurrent: () -> Unit,
    onSetSafetyLock: (Boolean) -> Unit = {},
    onBack: () -> Unit,
) {
    var showDisableDialog by remember { mutableStateOf(false) }
    val runningTask = appState.runningTask
    val safetyLock = appState.persisted.safetyLockEnabled
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(state.title, style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.error) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Rounded.ArrowBack, "Back") } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.errorContainer)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(modifier = Modifier.fillMaxSize().padding(top = innerPadding.calculateTopPadding() + 16.dp, bottom = innerPadding.calculateBottomPadding() + 16.dp, start = 16.dp, end = 16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Surface(shape = MaterialTheme.shapes.medium, color = StatusAmberDim, modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp)) {
                    Text("⚠ Runtime Control", style = MaterialTheme.typography.titleSmall, color = StatusAmber)
                    Text("Command: ${runningTask.command}", style = MaterialTheme.typography.bodyMedium)
                    Text("State: ${runningTask.controlState.name.lowercase()}", style = MaterialTheme.typography.bodySmall)
                }
            }
            Button(onClick = onStopAll, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)) { Text("⏹ STOP ALL TASKS") }
            ElevatedCard(shape = MaterialTheme.shapes.medium) {
                Column {
                    EmergencyListItem("⏸", "Pause Current Task", true, onPauseCurrent)
                    EmergencyListItem("✕", "Cancel Current Task", true, onCancelCurrent)
                    EmergencyListItem("🔒", if (safetyLock) "Disable Safety Lock" else "Enable Safety Lock", true) { onSetSafetyLock(!safetyLock) }
                    EmergencyListItem("🚫", "Disable AI Operator", true) { showDisableDialog = true }
                }
            }
            ElevatedCard(shape = MaterialTheme.shapes.medium, containerColor = if (safetyLock) StatusRedDim else MaterialTheme.colorScheme.surfaceVariant) {
                androidx.compose.foundation.layout.Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                    Text(if (safetyLock) "🔒" else "🔓")
                    Text("Safety Lock", modifier = Modifier.weight(1f).padding(start = 8.dp))
                    Switch(checked = safetyLock, onCheckedChange = onSetSafetyLock)
                }
            }
        }
    }
    if (showDisableDialog) {
        AlertDialog(onDismissRequest = { showDisableDialog = false }, title = { Text("Disable AI Operator?") }, text = { Text("This will stop the foreground service and accessibility binding.") }, confirmButton = { Button(onClick = { showDisableDialog = false }, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)) { Text("Disable") } }, dismissButton = { OutlinedButton(onClick = { showDisableDialog = false }) { Text("Cancel") } })
    }
}

@Composable private fun EmergencyListItem(emoji: String, label: String, enabled: Boolean, onClick: () -> Unit) { androidx.compose.foundation.layout.Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().then(if (enabled) Modifier.clickable(onClick = onClick) else Modifier).padding(horizontal = 16.dp, vertical = 16.dp)) { Text(emoji); Text(label, modifier = Modifier.weight(1f).padding(start = 12.dp)); Text("›") } }

@Preview(showBackground = true, backgroundColor = 0xFF0F1117)
@Composable private fun EmergencyPreview() { AiPhoneOperatorTheme(darkTheme = true) { EmergencyControlsScreen(state = EmergencyControlsUiState(), appState = OperatorAppState(), onStopAll = {}, onPauseCurrent = {}, onCancelCurrent = {}, onBack = {}) } }
