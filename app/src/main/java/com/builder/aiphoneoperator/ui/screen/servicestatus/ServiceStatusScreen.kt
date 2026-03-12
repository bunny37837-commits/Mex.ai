@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.builder.aiphoneoperator.ui.screen.servicestatus

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.builder.aiphoneoperator.model.ServiceState
import com.builder.aiphoneoperator.model.SystemStatusUiState
import com.builder.aiphoneoperator.runtime.OperatorAppState
import com.builder.aiphoneoperator.ui.components.ChipStatus
import com.builder.aiphoneoperator.ui.components.StatusChip
import com.builder.aiphoneoperator.ui.theme.AiPhoneOperatorTheme
import com.builder.aiphoneoperator.ui.theme.StatusGreen
import com.builder.aiphoneoperator.ui.theme.StatusGreenDim
import com.builder.aiphoneoperator.ui.theme.StatusRed
import com.builder.aiphoneoperator.ui.theme.StatusRedDim
import java.text.DateFormat
import java.util.Date

@Composable
fun ServiceStatusScreen(
    state: ServiceStatusUiState,
    appState: OperatorAppState,
    onRefresh: () -> Unit = {},
    onBack: () -> Unit,
) {
    val status = appState.toSystemStatusUiState()
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(state.title, style = MaterialTheme.typography.titleLarge) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Rounded.ArrowBack, "Back") } },
                actions = { IconButton(onClick = onRefresh) { Icon(Icons.Rounded.Refresh, "Refresh") } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp), contentPadding = PaddingValues(top = innerPadding.calculateTopPadding() + 16.dp, bottom = innerPadding.calculateBottomPadding() + 24.dp, start = 16.dp, end = 16.dp)) {
            item { OverallHealthCard(status) }
            item { DeviceInfoCard(status) }
            item { RuntimeMetadataCard(appState) }
            item { Button(onClick = {}, modifier = Modifier.fillMaxWidth().height(52.dp), colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)) { Text("⏹ Emergency Controls", style = MaterialTheme.typography.labelLarge) } }
        }
    }
}

@Composable private fun OverallHealthCard(status: SystemStatusUiState) {
    val isHealthy = status.accessibilityOn && status.serviceState == ServiceState.ACTIVE
    val bgColor = if (isHealthy) StatusGreenDim else StatusRedDim
    val textColor = if (isHealthy) StatusGreen else StatusRed
    Surface(shape = MaterialTheme.shapes.large, color = bgColor, modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(20.dp)) {
            Text(if (isHealthy) "✅" else "⚠", style = MaterialTheme.typography.headlineMedium)
            Spacer(Modifier.padding(horizontal = 8.dp))
            androidx.compose.foundation.layout.Column {
                Text(if (isHealthy) "✓ All Systems Operational" else "✕ Issues Detected", style = MaterialTheme.typography.titleMedium, color = textColor)
                Text("Accessibility: ${if (status.accessibilityOn) "connected" else "disconnected"}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("Service: ${status.serviceState.name.lowercase()}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable private fun DeviceInfoCard(status: SystemStatusUiState) {
    ElevatedCard(shape = MaterialTheme.shapes.medium, colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(16.dp).fillMaxWidth()) {
            androidx.compose.foundation.layout.Column(modifier = Modifier.weight(1f)) {
                Text("Android 15 · API 35", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
                Text("Battery unrestricted: ${status.batteryOk}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("Autostart: ${status.autostartOn}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            StatusChip("HyperOS", ChipStatus.ACTIVE)
        }
    }
}

@Composable private fun RuntimeMetadataCard(appState: OperatorAppState) {
    val persisted = appState.persisted
    val formattedTime = if (persisted.lastMetadataUpdatedAt > 0L) {
        DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT).format(Date(persisted.lastMetadataUpdatedAt))
    } else "Never"
    ElevatedCard(shape = MaterialTheme.shapes.medium, colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        androidx.compose.foundation.layout.Column(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text("Runtime metadata", style = MaterialTheme.typography.titleSmall)
            Text("Last event: ${persisted.lastEventSummary.ifBlank { "None" }}", style = MaterialTheme.typography.bodySmall)
            Text("Last command: ${persisted.lastRuntimeCommand.ifBlank { "None" }}", style = MaterialTheme.typography.bodySmall)
            Text("Last known service running: ${persisted.lastServiceRunning}", style = MaterialTheme.typography.bodySmall)
            Text("Last known accessibility connected: ${persisted.lastAccessibilityConnected}", style = MaterialTheme.typography.bodySmall)
            Text("Last updated: $formattedTime", style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0F1117)
@Composable private fun ServiceStatusPreview() { AiPhoneOperatorTheme(darkTheme = true) { ServiceStatusScreen(state = ServiceStatusUiState(), appState = OperatorAppState(), onBack = {}) } }
