@file:OptIn(ExperimentalMaterial3Api::class)

package com.builder.aiphoneoperator.ui.screen.runningtask

import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.builder.aiphoneoperator.runtime.OperatorAppState
import com.builder.aiphoneoperator.runtime.RuntimeControlState
import com.builder.aiphoneoperator.ui.components.BackTextButton
import com.builder.aiphoneoperator.ui.components.OperatorScaffold
import com.builder.aiphoneoperator.ui.components.StepTimeline
import com.builder.aiphoneoperator.ui.theme.AiPhoneOperatorTheme

@Composable
fun RunningTaskScreen(
    state: RunningTaskUiState,
    appState: OperatorAppState,
    onPause: () -> Unit,
    onStop: () -> Unit,
    onCancel: () -> Unit,
    onBack: () -> Unit,
) {
    val runtimeTask = appState.runningTask
    OperatorScaffold(
        title = state.title,
        navigationIcon = { BackTextButton(onClick = onBack) },
    ) {
        Text(state.summary)
        Text("Command: ${runtimeTask.command}")
        Text("Control state: ${runtimeTask.controlState.name.lowercase()}")
        Text("Task state: ${runtimeTask.taskState.name.lowercase()}")
        StepTimeline(steps = runtimeTask.steps)
        Button(onClick = onPause, enabled = runtimeTask.controlState == RuntimeControlState.RUNNING) { Text("Pause") }
        Button(onClick = onStop) { Text("Stop") }
        Button(onClick = onCancel) { Text("Cancel") }
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
            onBack = {},
        )
    }
}
