package com.builder.aiphoneoperator.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.builder.aiphoneoperator.model.*
import com.builder.aiphoneoperator.ui.theme.*

// ── RequirementCard ───────────────────────────────────────────────────────────

fun SeverityLevel.chipStatus() = when (this) {
    SeverityLevel.CRITICAL -> ChipStatus.ERROR
    SeverityLevel.WARNING  -> ChipStatus.WARNING
    SeverityLevel.OPTIONAL -> ChipStatus.INACTIVE
}

fun SeverityLevel.accentColor() = when (this) {
    SeverityLevel.CRITICAL -> StatusRed
    SeverityLevel.WARNING  -> StatusAmber
    SeverityLevel.OPTIONAL -> StatusNeutral
}

/**
 * Full-width card showing the status of a system requirement.
 * Left accent bar = severity color. Status dot = ok/broken.
 */
@Composable
fun RequirementCard(
    status   : RequirementStatus,
    onFix    : () -> Unit,
    modifier : Modifier = Modifier
) {
    val accentColor = if (status.isOk)
        StatusGreen
    else
        status.severity.accentColor()

    val statusIcon  = if (status.isOk) "✓" else "✕"
    val statusColor = if (status.isOk) StatusGreen else accentColor

    ElevatedCard(
        shape    = MaterialTheme.shapes.medium,
        modifier = modifier.fillMaxWidth(),
        colors   = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            // Left accent bar
            Surface(
                color    = accentColor,
                modifier = Modifier
                    .width(4.dp)
                    .defaultMinSize(minHeight = 72.dp)
            ) {}

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp, vertical = 14.dp)
            ) {
                // Status circle
                Surface(
                    shape    = androidx.compose.foundation.shape.CircleShape,
                    color    = statusColor.copy(alpha = 0.15f),
                    modifier = Modifier.size(36.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text  = statusIcon,
                            style = MaterialTheme.typography.titleSmall,
                            color = statusColor
                        )
                    }
                }

                Spacer(Modifier.width(12.dp))

                // Text block
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text  = status.title,
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text  = status.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(4.dp))
                    // Impact line
                    Text(
                        text  = status.impactLine,
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                        ),
                        color = if (status.isOk)
                            MaterialTheme.colorScheme.onSurfaceVariant
                        else
                            accentColor
                    )
                }

                // Fix button (only when broken)
                if (!status.isOk) {
                    Spacer(Modifier.width(8.dp))
                    FilledTonalButton(
                        onClick  = onFix,
                        modifier = Modifier.wrapContentWidth(),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        colors   = ButtonDefaults.filledTonalButtonColors(
                            containerColor = accentColor.copy(alpha = 0.15f),
                            contentColor   = accentColor
                        )
                    ) {
                        Text("Fix →", style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        }
    }
}

// ── SeverityGroupHeader ───────────────────────────────────────────────────────

@Composable
fun SeverityGroupHeader(
    label      : String,
    subtitle   : String,
    count      : Int,
    severity   : SeverityLevel,
    modifier   : Modifier = Modifier
) {
    val color = severity.accentColor()

    ElevatedCard(
        shape  = MaterialTheme.shapes.small,
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            Surface(
                color    = color,
                modifier = Modifier.width(4.dp).defaultMinSize(minHeight = 52.dp)
            ) {}
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.weight(1f).padding(horizontal = 12.dp, vertical = 10.dp)
            ) {
                Column {
                    Text(
                        text  = label,
                        style = MaterialTheme.typography.labelLarge,
                        color = color
                    )
                    Text(
                        text  = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                SeverityBadge(
                    label  = "$count issue${if (count != 1) "s" else ""}",
                    status = severity.chipStatus()
                )
            }
        }
    }
}

// ── LogItem ───────────────────────────────────────────────────────────────────

fun LogType.color() = when (this) {
    LogType.NAV       -> IceBlue
    LogType.SENSITIVE -> StatusAmber
    LogType.SUCCESS   -> StatusGreen
    LogType.ERROR     -> StatusRed
    LogType.INFO      -> StatusNeutral
}

@Composable
fun LogItem(
    entry    : LogEntry,
    modifier : Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 2.dp)
    ) {
        Text(
            text  = entry.timestamp,
            style = MaterialTheme.typography.labelSmall.copy(
                fontFamily = FontFamily.Monospace
            ),
            color = entry.type.color()
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text  = "→ ${entry.message}",
            style = MaterialTheme.typography.labelSmall.copy(
                fontFamily = FontFamily.Monospace
            ),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

// ── Previews ──────────────────────────────────────────────────────────────────

@Preview(showBackground = true, backgroundColor = 0xFF0F1117)
@Composable
private fun RequirementCardPreview() {
    AiPhoneOperatorTheme(darkTheme = true) {
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(16.dp)
        ) {
            SampleData.requirements.take(3).forEach { req ->
                RequirementCard(status = req, onFix = {})
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0F1117)
@Composable
private fun SeverityGroupHeaderPreview() {
    AiPhoneOperatorTheme(darkTheme = true) {
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(16.dp)
        ) {
            SeverityGroupHeader("CRITICAL", "Fix these first", 2, SeverityLevel.CRITICAL)
            SeverityGroupHeader("WARNING",  "May cause failures", 1, SeverityLevel.WARNING)
            SeverityGroupHeader("OPTIONAL", "Recommended", 1, SeverityLevel.OPTIONAL)
        }
    }
}
