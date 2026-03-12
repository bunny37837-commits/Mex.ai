@file:OptIn(ExperimentalMaterial3Api::class)

package com.builder.aiphoneoperator.ui.screen.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ElevatedCard
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.builder.aiphoneoperator.ui.theme.AiPhoneOperatorTheme

@Composable
fun SettingsScreen(state: SettingsUiState, onBack: () -> Unit) {
    var memoryEnabled by remember { mutableStateOf(true) }
    var localOnly by remember { mutableStateOf(true) }
    var debugMode by remember { mutableStateOf(false) }
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
            item { SettingsSectionLabel("MEMORY & CONTEXT") }
            item {
                SettingsCard {
                    SettingsToggleRow("Enable Conversation Memory", memoryEnabled) { memoryEnabled = it }
                    SettingsDivider()
                    SettingsRow("View Memory Contents", "") {}
                }
            }
            item { SettingsSectionLabel("PRIVACY") }
            item {
                SettingsCard {
                    SettingsToggleRow("Local AI Only", localOnly) { localOnly = it }
                    SettingsDivider()
                    SettingsRow("Data Retention", "30 days →") {}
                }
            }
            item { SettingsSectionLabel("DEBUG & DIAGNOSTICS") }
            item {
                SettingsCard {
                    SettingsToggleRow("Debug Mode", debugMode) { debugMode = it }
                    SettingsDivider()
                    SettingsRow("Run Diagnostics", "Repair Mode →") {}
                }
            }
        }
    }
}

@Composable private fun SettingsSectionLabel(label: String) { Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(start = 28.dp, top = 20.dp, bottom = 4.dp)) }
@Composable private fun SettingsCard(content: @Composable ColumnScope.() -> Unit) { ElevatedCard(shape = MaterialTheme.shapes.medium, modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp), colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) { Column(content = content) } }
@Composable private fun SettingsRow(label: String, value: String, onClick: () -> Unit) { Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(horizontal = 16.dp, vertical = 14.dp)) { Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.weight(1f)); if (value.isNotEmpty()) Text(value, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant); Spacer(Modifier.padding(horizontal = 2.dp)); Icon(Icons.Rounded.ChevronRight, null) } }
@Composable private fun SettingsToggleRow(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) { Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)) { Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.weight(1f)); Switch(checked = checked, onCheckedChange = onCheckedChange) } }
@Composable private fun SettingsDivider() { HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, modifier = Modifier.padding(horizontal = 16.dp)) }

@Preview(showBackground = true, backgroundColor = 0xFF0F1117)
@Composable private fun SettingsPreview() { AiPhoneOperatorTheme(darkTheme = true) { SettingsScreen(state = SettingsUiState(), onBack = {}) } }
