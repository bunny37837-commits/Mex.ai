@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.builder.aiphoneoperator.ui.screen.capabilities

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.builder.aiphoneoperator.model.CapabilityItem
import com.builder.aiphoneoperator.ui.theme.AiPhoneOperatorTheme

private val capabilityCategories = listOf(
    "📱 App Control" to listOf(
        CapabilityItem("apps", "Open App", "Launch any installed app by name", "\"Open WhatsApp\"", "App Control"),
        CapabilityItem("wifi", "Wi-Fi", "Toggle Wi-Fi on or off", "\"Turn on Wi-Fi\"", "Settings")
    )
)

@Composable
fun CapabilitiesScreen(state: CapabilitiesUiState, onBack: () -> Unit) {
    var selectedCapability by remember { mutableStateOf<CapabilityItem?>(null) }
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
        LazyColumn(contentPadding = PaddingValues(top = innerPadding.calculateTopPadding() + 8.dp, bottom = innerPadding.calculateBottomPadding() + 24.dp)) {
            capabilityCategories.forEach { (category, items) ->
                item {
                    Text(category, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 8.dp))
                    LazyRow(contentPadding = PaddingValues(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(items) { cap -> CapabilityTile(cap) { selectedCapability = cap } }
                    }
                }
            }
            item { Spacer(Modifier.padding(vertical = 8.dp)) }
        }
    }
}

@Composable private fun CapabilityTile(item: CapabilityItem, onClick: () -> Unit) { ElevatedCard(shape = MaterialTheme.shapes.medium, modifier = Modifier.clickable(onClick = onClick), colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) { Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(16.dp)) { Text("🤖"); Spacer(Modifier.padding(horizontal = 4.dp)); Text(item.label) } } }

@Preview(showBackground = true, backgroundColor = 0xFF0F1117)
@Composable private fun CapabilitiesPreview() { AiPhoneOperatorTheme(darkTheme = true) { CapabilitiesScreen(state = CapabilitiesUiState(), onBack = {}) } }
