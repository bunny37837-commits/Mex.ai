@file:OptIn(ExperimentalMaterial3Api::class)

package com.builder.aiphoneoperator.ui.screen.onboarding

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.builder.aiphoneoperator.model.RequirementStatus
import com.builder.aiphoneoperator.runtime.OperatorAppState
import com.builder.aiphoneoperator.ui.components.RequirementCard
import com.builder.aiphoneoperator.ui.theme.AiPhoneOperatorTheme

@Composable
fun OnboardingScreen(
    state: OnboardingUiState,
    appState: OperatorAppState = OperatorAppState(),
    requirements: List<RequirementStatus> = emptyList(),
    onRefresh: () -> Unit = {},
    onFixRequirement: (RequirementStatus) -> Unit = {},
    onAcknowledgeAutostart: () -> Unit = {},
    onContinue: () -> Unit,
) {
    Scaffold(containerColor = MaterialTheme.colorScheme.background) { innerPadding ->
        Column(Modifier.fillMaxSize().padding(innerPadding)) {
            OnboardingHeader(appState)
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    Text(state.title, style = MaterialTheme.typography.headlineSmall)
                    Spacer(Modifier.height(8.dp))
                    Text(state.summary, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                item {
                    FilledTonalButton(onClick = onRefresh, modifier = Modifier.fillMaxWidth()) {
                        Text("Refresh setup status")
                    }
                }
                if (requirements.isNotEmpty()) {
                    items(requirements, key = { it.requirement.name }) { requirement ->
                        RequirementCard(status = requirement, onFix = { onFixRequirement(requirement) })
                    }
                }
                item {
                    FilledTonalButton(onClick = onAcknowledgeAutostart, modifier = Modifier.fillMaxWidth()) {
                        Text("Mark HyperOS autostart as acknowledged")
                    }
                }
            }
            OnboardingNavRow(
                isComplete = appState.onboardingStatus.isComplete || appState.persisted.onboardingCompletionSnapshot,
                onContinue = onContinue,
            )
        }
    }
}

@Composable
private fun OnboardingHeader(appState: OperatorAppState) {
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 16.dp)) {
        Text("Onboarding status", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.height(8.dp))
        Text("Accessibility: ${if (appState.onboardingStatus.accessibilityEnabled) "enabled" else "disabled"}")
        Text("Notifications: ${if (appState.onboardingStatus.notificationPermissionGranted) "granted" else "missing"}")
        Text("Battery optimization: ${if (appState.onboardingStatus.batteryOptimizationDisabled) "ignored" else "restricted"}")
        Text("HyperOS autostart: ${if (appState.onboardingStatus.hyperOsAutostartEnabled || appState.persisted.hyperOsAutostartAcknowledged) "ready" else "needs review"}")
    }
}

@Composable
private fun OnboardingNavRow(isComplete: Boolean, onContinue: () -> Unit) {
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 16.dp).navigationBarsPadding()
    ) {
        TextButton(onClick = onContinue) { Text("Skip setup", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant) }
        Button(onClick = onContinue, enabled = isComplete, modifier = Modifier.height(48.dp)) {
            Text(if (isComplete) "Start Using AI Operator →" else "Complete setup first", style = MaterialTheme.typography.labelLarge)
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0F1117)
@Composable
private fun OnboardingScreenPreview() {
    AiPhoneOperatorTheme(darkTheme = true) {
        OnboardingScreen(state = OnboardingUiState(), onContinue = {})
    }
}
