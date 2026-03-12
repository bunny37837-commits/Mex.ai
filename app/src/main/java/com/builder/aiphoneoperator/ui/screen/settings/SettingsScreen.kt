@file:OptIn(ExperimentalMaterial3Api::class)

package com.builder.aiphoneoperator.ui.screen.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.weight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.builder.aiphoneoperator.runtime.OperatorAppState
import com.builder.aiphoneoperator.ui.theme.AiPhoneOperatorTheme

@Composable
fun SettingsScreen(
    state: SettingsUiState,
    appState: OperatorAppState = OperatorAppState(),
    onSetLocalAiOnly: (Boolean) -> Unit = {},
    onSetConversationMemoryEnabled: (Boolean) -> Unit = {},
    onSetDebugModeEnabled: (Boolean) -> Unit = {},
    onBack: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(state.title, style = MaterialTheme.typography.titleLarge) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Rounded.ArrowBack, "Back") } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        LazyColumn(contentPadding = PaddingValues(top = innerPadding.calculateTopPadding(), bottom = innerPadding.calculateBottomPadding() + 24.dp)) {
            item { SettingsSectionLabel("AGENT") }
            item {
                SettingsCard {
                    SettingsToggleRow("Local AI Only", appState.settings.localAiOnly, onSetLocalAiOnly)
                    SettingsDivider()
                    SettingsToggleRow("Conversation Memory", appState.settings.conversationMemoryEnabled, onSetConversationMemoryEnabled)
                    SettingsDivider()
                    SettingsToggleRow("Debug Mode", appState.settings.debugModeEnabled, onSetDebugModeEnabled)
                }
            }
            item { SettingsSectionLabel("CURRENT TRUTH") }
            item {
                SettingsCard {
                    SettingsInfoRow("API integration", "Not implemented yet")
                    SettingsDivider()
                    SettingsInfoRow("Local model loading", "Not implemented yet")
                    SettingsDivider()
                    SettingsInfoRow("Current executor", "Open app by name")
                }
            }
        }
    }
}

@Composable private fun SettingsSectionLabel(label: String) { Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(start = 28.dp, top = 20.dp, bottom = 4.dp)) }
@Composable private fun SettingsCard(content: @Composable ColumnScope.() -> Unit) { ElevatedCard(shape = MaterialTheme.shapes.medium, modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp), colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) { Column(content = content) } }
@Composable private fun SettingsInfoRow(label: String, value: String) { Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp)) { Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.weight(1f)); Text(value, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) } }
@Composable private fun SettingsToggleRow(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) { Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)) { Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.weight(1f)); Switch(checked = checked, onCheckedChange = onCheckedChange) } }
@Composable private fun SettingsDivider() { HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, modifier = Modifier.padding(horizontal = 16.dp)) }

@Preview(showBackground = true)
@Composable private fun SettingsPreview() { AiPhoneOperatorTheme { SettingsScreen(state = SettingsUiState(), onBack = {}) } }
