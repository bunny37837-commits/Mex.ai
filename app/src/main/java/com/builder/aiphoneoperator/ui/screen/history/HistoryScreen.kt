@file:OptIn(ExperimentalMaterial3Api::class)

package com.builder.aiphoneoperator.ui.screen.history

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
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
import com.builder.aiphoneoperator.ui.components.HistoryTaskItem
import com.builder.aiphoneoperator.ui.theme.AiPhoneOperatorTheme

private val filterOptions = listOf("All", "Success", "Failed", "Today")

@Composable
fun HistoryScreen(state: HistoryUiState, onBack: () -> Unit) {
    var selectedFilter by remember { mutableIntStateOf(0) }
    val history = SampleData.taskHistory
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("History", style = MaterialTheme.typography.titleLarge) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Rounded.ArrowBack, "Back") } },
                actions = { IconButton(onClick = {}) { Icon(Icons.Rounded.Search, "Search") } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        LazyColumn(contentPadding = PaddingValues(top = innerPadding.calculateTopPadding(), bottom = innerPadding.calculateBottomPadding() + 24.dp)) {
            item {
                LazyRow(contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(filterOptions.size) { index ->
                        FilterChip(selected = selectedFilter == index, onClick = { selectedFilter = index }, label = { Text(filterOptions[index]) })
                    }
                }
            }
            items(history, key = { it.id }) { task ->
                HistoryTaskItem(item = task, onClick = {}, onRetry = {})
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0F1117)
@Composable private fun HistoryPreview() { AiPhoneOperatorTheme(darkTheme = true) { HistoryScreen(state = HistoryUiState(), onBack = {}) } }
