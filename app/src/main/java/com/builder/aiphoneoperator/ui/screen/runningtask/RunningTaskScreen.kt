@file:OptIn(ExperimentalMaterial3Api::class)

package com.builder.aiphoneoperator.ui.screen.runningtask

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.builder.aiphoneoperator.domain.agent.ExecutionAction
import com.builder.aiphoneoperator.runtime.OperatorAppState
import com.builder.aiphoneoperator.ui.components.BackTextButton
import com.builder.aiphoneoperator.ui.components.OperatorScaffold
import com.builder.aiphoneoperator.ui.theme.AiPhoneOperatorTheme

@Composable
fun RunningTaskScreen(
    state: RunningTaskUiState,
    appState: OperatorAppState,
    onPause: () -> Unit,
    onStop: () -> Unit,
    onCancel: () -> Unit,
    onCaptureObservation: () -> Unit,
    onPerformBack: () -> Unit,
    onTapTargetByText: (String) -> Unit,
    onInputTextIntoFocusedField: (String) -> Unit,
    onBack: () -> Unit,
) {
    val session = appState.activeSession
    val observation = appState.currentObservation
    var targetQuery by rememberSaveable { mutableStateOf("") }
    var inputText by rememberSaveable { mutableStateOf("") }

    OperatorScaffold(
        title = state.title,
        navigationIcon = { BackTextButton(onClick = onBack) },
    ) {
        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            item {
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(state.summary, style = MaterialTheme.typography.titleMedium)
                        Text("Executor: open app, tap visible target, back, input into focused field")
                    }
                }
            }
            item {
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (session == null) {
                            Text("No active session.")
                        } else {
                            Text("Command: ${session.commandText}", style = MaterialTheme.typography.titleMedium)
                            Text("Status: ${session.status.name.lowercase().replace('_', ' ')}")
                            Text("Message: ${session.message}")
                            when (val action = session.action) {
                                is ExecutionAction.OpenApp -> {
                                    Text("Action: open app")
                                    Text("Query: ${action.query}")
                                    if (action.resolvedLabel != null) Text("Resolved label: ${action.resolvedLabel}")
                                    if (action.resolvedPackageName != null) Text("Resolved package: ${action.resolvedPackageName}")
                                }
                                null -> Text("Action: not resolved yet")
                            }
                            if (session.error != null) Text("Error: ${session.error}")
                        }
                    }
                }
            }
            item {
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Current screen observation", style = MaterialTheme.typography.titleMedium)
                        if (observation == null) {
                            Text("No observation captured yet.")
                        } else {
                            Text("Package: ${observation.packageName ?: "unknown"}")
                            Text("Class: ${observation.className ?: "unknown"}")
                            Text("Visible nodes: ${observation.nodes.size}")
                            Text("Blockers: ${if (observation.blockers.isEmpty()) "none" else observation.blockers.joinToString { it.type.name.lowercase() }}")
                        }
                        OutlinedButton(onClick = onCaptureObservation, modifier = Modifier.fillMaxWidth()) {
                            Text("Capture observation")
                        }
                    }
                }
            }
            item {
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("UI navigation controls", style = MaterialTheme.typography.titleMedium)
                        OutlinedTextField(
                            value = targetQuery,
                            onValueChange = { targetQuery = it },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text("Visible target label") },
                            singleLine = true,
                        )
                        Button(
                            onClick = { if (targetQuery.isNotBlank()) onTapTargetByText(targetQuery.trim()) },
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Text("Tap target by visible text")
                        }
                        OutlinedTextField(
                            value = inputText,
                            onValueChange = { inputText = it },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text("Text for focused field") },
                        )
                        OutlinedButton(
                            onClick = { if (inputText.isNotBlank()) onInputTextIntoFocusedField(inputText) },
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Text("Input text into focused field")
                        }
                        OutlinedButton(onClick = onPerformBack, modifier = Modifier.fillMaxWidth()) {
                            Text("Perform back")
                        }
                    }
                }
            }
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = onPause, modifier = Modifier.fillMaxWidth()) { Text("Pause session") }
                    OutlinedButton(onClick = onStop, modifier = Modifier.fillMaxWidth()) { Text("Stop session") }
                    OutlinedButton(onClick = onCancel, modifier = Modifier.fillMaxWidth()) { Text("Cancel session") }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun RunningTaskScreenPreview() {
    AiPhoneOperatorTheme {
        RunningTaskScreen(
            state = RunningTaskUiState(),
            appState = OperatorAppState(),
            onPause = {},
            onStop = {},
            onCancel = {},
            onCaptureObservation = {},
            onPerformBack = {},
            onTapTargetByText = {},
            onInputTextIntoFocusedField = {},
            onBack = {},
        )
    }
}
