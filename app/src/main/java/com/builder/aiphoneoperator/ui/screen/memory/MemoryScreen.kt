package com.builder.aiphoneoperator.ui.screen.memory

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.builder.aiphoneoperator.model.SampleData
import com.builder.aiphoneoperator.ui.components.MacroCard
import com.builder.aiphoneoperator.ui.components.PatternCard
import com.builder.aiphoneoperator.ui.theme.AiPhoneOperatorTheme

@Composable
fun MemoryScreen(state: MemoryUiState, onBack: () -> Unit) {
    var selectedTab by remember { mutableIntStateOf(0) }
    var expandedMacroId by remember { androidx.compose.runtime.mutableStateOf<String?>(null) }
    val tabs = listOf("Macros", "Patterns", "Device", "Context")
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("AI Memory", style = MaterialTheme.typography.titleLarge) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Rounded.ArrowBack, "Back") } },
                actions = { IconButton(onClick = {}) { Icon(Icons.Rounded.MoreVert, "More options") } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        LazyColumn(contentPadding = PaddingValues(top = innerPadding.calculateTopPadding(), bottom = innerPadding.calculateBottomPadding() + 24.dp)) {
            item {
                ScrollableTabRow(selectedTabIndex = selectedTab, containerColor = MaterialTheme.colorScheme.surface, edgePadding = 16.dp) {
                    tabs.forEachIndexed { index, label ->
                        Tab(selected = selectedTab == index, onClick = { selectedTab = index }, text = { Text(label, style = MaterialTheme.typography.labelLarge) })
                    }
                }
                Spacer(Modifier.height(8.dp))
            }
            when (selectedTab) {
                0 -> items(SampleData.macros, key = { it.id }) { macro -> MacroCard(macro = macro, expanded = expandedMacroId == macro.id, onToggle = { expandedMacroId = if (expandedMacroId == macro.id) null else macro.id }, onRun = {}, onEdit = {}, onDelete = {}, modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) }
                1 -> items(SampleData.patterns) { pattern -> PatternCard(pattern = pattern, modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) }
                else -> item { Text("${tabs[selectedTab]} view", modifier = Modifier.padding(16.dp)) }
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0F1117)
@Composable private fun MemoryPreview() { AiPhoneOperatorTheme(darkTheme = true) { MemoryScreen(state = MemoryUiState(), onBack = {}) } }
