package com.builder.aiphoneoperator.ui.screen.repairmode

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.FilledTonalButton
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
import com.builder.aiphoneoperator.model.RequirementStatus
import com.builder.aiphoneoperator.model.RepairRequirement
import com.builder.aiphoneoperator.model.SeverityLevel
import com.builder.aiphoneoperator.runtime.OperatorAppState
import com.builder.aiphoneoperator.ui.components.ChipStatus
import com.builder.aiphoneoperator.ui.components.RequirementCard
import com.builder.aiphoneoperator.ui.components.SeverityBadge
import com.builder.aiphoneoperator.ui.components.SeverityGroupHeader
import com.builder.aiphoneoperator.ui.theme.AiPhoneOperatorTheme
import com.builder.aiphoneoperator.ui.theme.StatusAmberDim
import com.builder.aiphoneoperator.ui.theme.StatusGreenDim
import com.builder.aiphoneoperator.ui.theme.StatusRedDim

@Composable
fun RepairModeScreen(
    state: RepairModeUiState,
    appState: OperatorAppState,
    requirements: List<RequirementStatus> = appState.toRepairRequirements(),
    onRefresh: () -> Unit = {},
    onFixRequirement: (RequirementStatus) -> Unit = {},
    onAcknowledgeAutostart: () -> Unit = {},
    onBack: () -> Unit,
) {
    val critical = requirements.filter { it.severity == SeverityLevel.CRITICAL && !it.isOk }
    val warning = requirements.filter { it.severity == SeverityLevel.WARNING && !it.isOk }
    val optional = requirements.filter { it.severity == SeverityLevel.OPTIONAL && !it.isOk }
    val allOk = critical.isEmpty() && warning.isEmpty() && optional.isEmpty()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(state.title, style = MaterialTheme.typography.titleLarge) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Rounded.ArrowBack, "Back") } },
                actions = { IconButton(onClick = onRefresh) { Icon(Icons.Rounded.Refresh, "Re-check") } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(top = innerPadding.calculateTopPadding() + 8.dp, bottom = innerPadding.calculateBottomPadding() + 24.dp, start = 16.dp, end = 16.dp)
        ) {
            item { SeverityAlertBanner(critical.size, warning.size, optional.size, allOk) }
            if (critical.isNotEmpty()) {
                item { SeverityGroupHeader("CRITICAL", "Fix these first — blocks all execution", critical.size, SeverityLevel.CRITICAL) }
                items(requirements.filter { it.severity == SeverityLevel.CRITICAL }) { req -> RequirementCard(status = req, onFix = { onFixRequirement(req) }) }
            }
            if (warning.isNotEmpty()) {
                item { SeverityGroupHeader("WARNING", "May cause task failures", warning.size, SeverityLevel.WARNING) }
                items(requirements.filter { it.severity == SeverityLevel.WARNING }) { req -> RequirementCard(status = req, onFix = { onFixRequirement(req) }) }
            }
            if (optional.isNotEmpty()) {
                item { SeverityGroupHeader("OPTIONAL", "Recommended for best experience", optional.size, SeverityLevel.OPTIONAL) }
                items(requirements.filter { it.severity == SeverityLevel.OPTIONAL }) { req -> RequirementCard(status = req, onFix = { onFixRequirement(req) }) }
            }
            if (requirements.any { it.requirement == RepairRequirement.AUTOSTART && !it.isOk }) {
                item {
                    FilledTonalButton(onClick = onAcknowledgeAutostart, modifier = Modifier.fillMaxWidth()) {
                        Text("Mark HyperOS autostart as acknowledged")
                    }
                }
            }
            item {
                FilledTonalButton(onClick = onRefresh, modifier = Modifier.fillMaxWidth().height(56.dp)) {
                    Text("Re-check All Systems", style = MaterialTheme.typography.labelLarge)
                }
            }
        }
    }
}

@Composable
private fun SeverityAlertBanner(criticalCount: Int, warningCount: Int, optionalCount: Int, allOk: Boolean) {
    val bgColor = when {
        allOk -> StatusGreenDim
        criticalCount > 0 -> StatusRedDim
        warningCount > 0 -> StatusAmberDim
        else -> MaterialTheme.colorScheme.surfaceVariant
    }
    val bodyText = when {
        allOk -> "All systems operational."
        criticalCount > 0 -> "AI is OFFLINE — fix Critical items to run tasks."
        warningCount > 0 -> "AI is running with reduced reliability."
        else -> "Optional improvements available."
    }
    Surface(shape = MaterialTheme.shapes.medium, color = bgColor) {
        androidx.compose.foundation.layout.Column(Modifier.fillMaxWidth().padding(16.dp)) {
            androidx.compose.foundation.layout.Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                if (criticalCount > 0) SeverityBadge("$criticalCount Critical", ChipStatus.ERROR)
                if (warningCount > 0) SeverityBadge("$warningCount Warning", ChipStatus.WARNING)
                if (optionalCount > 0) SeverityBadge("$optionalCount Optional", ChipStatus.INACTIVE)
                if (allOk) SeverityBadge("All clear", ChipStatus.ACTIVE)
            }
            androidx.compose.foundation.layout.Spacer(Modifier.height(8.dp))
            Text(bodyText, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0F1117)
@Composable
private fun RepairModeScreenPreview() {
    AiPhoneOperatorTheme(darkTheme = true) {
        RepairModeScreen(state = RepairModeUiState(), appState = OperatorAppState(), onBack = {})
    }
}
