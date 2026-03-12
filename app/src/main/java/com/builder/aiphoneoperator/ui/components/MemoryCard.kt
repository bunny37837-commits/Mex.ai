package com.builder.aiphoneoperator.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.builder.aiphoneoperator.model.MemoryMacro
import com.builder.aiphoneoperator.model.MemoryPattern
import com.builder.aiphoneoperator.model.SampleData
import com.builder.aiphoneoperator.ui.theme.*

// ── MacroCard ─────────────────────────────────────────────────────────────────

@Composable
fun MacroCard(
    macro    : MemoryMacro,
    expanded : Boolean,
    onToggle : () -> Unit,
    onRun    : () -> Unit,
    onEdit   : () -> Unit,
    onDelete : () -> Unit,
    modifier : Modifier = Modifier
) {
    ElevatedCard(
        shape    = MaterialTheme.shapes.medium,
        modifier = modifier.fillMaxWidth(),
        colors   = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(Modifier.padding(16.dp)) {
            // Header row
            Row(
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier              = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("⚡", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text  = macro.name,
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                TextButton(
                    onClick = onToggle,
                    contentPadding = PaddingValues(4.dp)
                ) {
                    Text(
                        text  = if (expanded) "▲" else "▾",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            // Steps preview (always visible, collapsed)
            Spacer(Modifier.height(4.dp))
            Text(
                text  = macro.steps.joinToString(" → "),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = if (expanded) Int.MAX_VALUE else 1,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
            )

            // Expanded detail
            if (expanded) {
                Spacer(Modifier.height(12.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                    MetaLabel("Created", "Mar 10, 2026")
                    MetaLabel("Last run", macro.lastRun)
                    MetaLabel("Runs", macro.runCount.toString())
                }
                Spacer(Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("✓", color = StatusGreen, style = MaterialTheme.typography.labelSmall)
                    Spacer(Modifier.width(4.dp))
                    Text("Stores: command sequence only",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("✕", color = StatusRed, style = MaterialTheme.typography.labelSmall)
                    Spacer(Modifier.width(4.dp))
                    Text("Does NOT store: content viewed during steps",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }

                Spacer(Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilledTonalButton(onClick = onRun,   modifier = Modifier.weight(1f)) {
                        Text("▶ Run",   style = MaterialTheme.typography.labelSmall)
                    }
                    OutlinedButton(onClick = onEdit,  modifier = Modifier.weight(1f)) {
                        Text("✏ Edit", style = MaterialTheme.typography.labelSmall)
                    }
                    OutlinedButton(
                        onClick = onDelete,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Text("🗑 Del", style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        }
    }
}

// ── PatternCard ───────────────────────────────────────────────────────────────

@Composable
fun PatternCard(
    pattern  : MemoryPattern,
    modifier : Modifier = Modifier
) {
    ElevatedCard(
        shape    = MaterialTheme.shapes.medium,
        modifier = modifier.fillMaxWidth(),
        colors   = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(16.dp)
        ) {
            Text("📊", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text  = "\"${pattern.command}\"",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text  = "Used ${pattern.usageCount}× · ${pattern.successRate}% success · ~${pattern.avgDurationMs / 1000}s avg",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

// ── MetaLabel helper ──────────────────────────────────────────────────────────

@Composable
private fun MetaLabel(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface)
    }
}

// ── Preview ───────────────────────────────────────────────────────────────────

@Preview(showBackground = true, backgroundColor = 0xFF0F1117)
@Composable
private fun MacroCardPreview() {
    AiPhoneOperatorTheme(darkTheme = true) {
        var expanded by remember { mutableStateOf(true) }
        Column(modifier = Modifier.padding(16.dp)) {
            MacroCard(
                macro    = SampleData.macros.first(),
                expanded = expanded,
                onToggle = { expanded = !expanded },
                onRun    = {},
                onEdit   = {},
                onDelete = {}
            )
        }
    }
}
