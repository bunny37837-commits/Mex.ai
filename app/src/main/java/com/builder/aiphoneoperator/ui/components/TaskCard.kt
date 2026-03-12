package com.builder.aiphoneoperator.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.builder.aiphoneoperator.model.SampleData
import com.builder.aiphoneoperator.model.TaskHistoryItem
import com.builder.aiphoneoperator.model.TaskState
import com.builder.aiphoneoperator.ui.theme.*

// ── TaskState helpers ─────────────────────────────────────────────────────────

fun TaskState.statusColor() = when (this) {
    TaskState.COMPLETED -> StatusGreen
    TaskState.RUNNING   -> IceBlue
    TaskState.PAUSED    -> StatusAmber
    TaskState.FAILED    -> StatusRed
    TaskState.CANCELLED -> StatusNeutral
    TaskState.PARTIAL   -> StatusAmber
}

fun TaskState.label() = when (this) {
    TaskState.COMPLETED -> "Done"
    TaskState.RUNNING   -> "Running"
    TaskState.PAUSED    -> "Paused"
    TaskState.FAILED    -> "Failed"
    TaskState.CANCELLED -> "Cancelled"
    TaskState.PARTIAL   -> "Partial"
}

// ── RecentTaskCard (compact horizontal card) ──────────────────────────────────

@Composable
fun RecentTaskCard(
    item     : TaskHistoryItem,
    onClick  : () -> Unit,
    modifier : Modifier = Modifier
) {
    ElevatedCard(
        shape    = MaterialTheme.shapes.medium,
        modifier = modifier
            .width(160.dp)
            .height(80.dp)
            .clickable(onClick = onClick),
        colors   = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Box(Modifier.fillMaxSize().padding(12.dp)) {
            // Status dot
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .align(Alignment.TopEnd)
            ) {
                Surface(
                    shape = CircleShape,
                    color = item.state.statusColor(),
                    modifier = Modifier.fillMaxSize()
                ) {}
            }
            // Content
            Column(
                verticalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxHeight()
            ) {
                Text(
                    text     = item.command,
                    style    = MaterialTheme.typography.bodyMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    color    = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text  = item.timestamp,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

// ── HistoryTaskItem (full-width list row) ─────────────────────────────────────

@Composable
fun HistoryTaskItem(
    item       : TaskHistoryItem,
    onClick    : () -> Unit,
    onRetry    : () -> Unit,
    modifier   : Modifier = Modifier
) {
    Surface(
        color    = MaterialTheme.colorScheme.surface,
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            // Status dot
            Surface(
                shape    = CircleShape,
                color    = item.state.statusColor(),
                modifier = Modifier.size(12.dp)
            ) {}

            Spacer(Modifier.width(12.dp))

            // Command text + meta
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text     = item.command,
                    style    = MaterialTheme.typography.bodyLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color    = MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text  = "${item.stepCount} steps · ${item.elapsedMs / 1000}s · ${item.state.label()}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(Modifier.width(8.dp))

            // Retry
            TextButton(
                onClick  = onRetry,
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text  = "▶ Retry",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
    }
}

// ── QuickActionButton ─────────────────────────────────────────────────────────

data class QuickAction(
    val label   : String,
    val emoji   : String        // placeholder for icon; replace with painter in real app
)

val defaultQuickActions = listOf(
    QuickAction("Open App",       "📱"),
    QuickAction("Message",        "💬"),
    QuickAction("Settings",       "⚙️"),
    QuickAction("Notifications",  "🔔"),
    QuickAction("Screenshot",     "📷"),
    QuickAction("Custom",         "＋")
)

@Composable
fun QuickActionButton(
    action   : QuickAction,
    onClick  : () -> Unit,
    modifier : Modifier = Modifier
) {
    FilledTonalButton(
        onClick  = onClick,
        shape    = MaterialTheme.shapes.extraLarge,
        modifier = modifier.height(40.dp),
        contentPadding = PaddingValues(horizontal = 14.dp)
    ) {
        Text(
            text  = "${action.emoji} ${action.label}",
            style = MaterialTheme.typography.labelLarge
        )
    }
}

// ── SectionHeader ─────────────────────────────────────────────────────────────

@Composable
fun SectionHeader(
    title    : String,
    modifier : Modifier = Modifier,
    action   : String?  = null,
    onAction : () -> Unit = {}
) {
    Row(
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(
            text  = title,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        if (action != null) {
            TextButton(onClick = onAction, contentPadding = PaddingValues(4.dp)) {
                Text(
                    text  = action,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

// ── Previews ──────────────────────────────────────────────────────────────────

@Preview(showBackground = true, backgroundColor = 0xFF0F1117)
@Composable
private fun RecentTaskCardPreview() {
    AiPhoneOperatorTheme(darkTheme = true) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(16.dp)
        ) {
            SampleData.taskHistory.take(2).forEach { item ->
                RecentTaskCard(item = item, onClick = {})
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0F1117)
@Composable
private fun HistoryTaskItemPreview() {
    AiPhoneOperatorTheme(darkTheme = true) {
        Column {
            SampleData.taskHistory.take(3).forEach { item ->
                HistoryTaskItem(item = item, onClick = {}, onRetry = {})
            }
        }
    }
}
