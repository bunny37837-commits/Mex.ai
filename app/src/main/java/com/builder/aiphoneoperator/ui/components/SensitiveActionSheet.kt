package com.builder.aiphoneoperator.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.builder.aiphoneoperator.model.SensitiveActionType
import com.builder.aiphoneoperator.ui.theme.*

data class SensitiveActionConfig(
    val type           : SensitiveActionType,
    val title          : String,
    val badgeLabel     : String,
    val previewContent : String,        // message text / setting change / etc.
    val riskLabel      : String,
    val autoConfirmSec : Int            // 0 = manual only
)

fun SensitiveActionType.badgeStatus() = when (this) {
    SensitiveActionType.SEND_MESSAGE    -> ChipStatus.ACTIVE
    SensitiveActionType.PLACE_CALL      -> ChipStatus.WARNING
    SensitiveActionType.CHANGE_SETTING  -> ChipStatus.WARNING
    SensitiveActionType.SHARE_SCREENSHOT-> ChipStatus.INACTIVE
    SensitiveActionType.DESTRUCTIVE     -> ChipStatus.ERROR
}

fun SensitiveActionType.emoji() = when (this) {
    SensitiveActionType.SEND_MESSAGE    -> "✉️"
    SensitiveActionType.PLACE_CALL      -> "📞"
    SensitiveActionType.CHANGE_SETTING  -> "⚙️"
    SensitiveActionType.SHARE_SCREENSHOT-> "📤"
    SensitiveActionType.DESTRUCTIVE     -> "🗑"
}

/**
 * Non-swipe-dismissible bottom sheet for reviewing sensitive AI actions.
 * Physical back = Stop Task Here.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SensitiveActionSheet(
    config       : SensitiveActionConfig,
    onAllow      : () -> Unit,
    onStop       : () -> Unit,
    modifier     : Modifier = Modifier
) {
    var confirmedDestructive by remember { mutableStateOf(false) }
    var secondsLeft by remember { mutableIntStateOf(config.autoConfirmSec) }
    var autoConfirmActive by remember { mutableStateOf(config.autoConfirmSec > 0) }

    val isDestructive = config.type == SensitiveActionType.DESTRUCTIVE
    val isCall        = config.type == SensitiveActionType.PLACE_CALL
    // Calls and destructive: never auto-confirm
    val effectiveAutoConfirm = if (isDestructive || isCall) 0 else config.autoConfirmSec

    // Countdown effect
    LaunchedEffect(autoConfirmActive) {
        if (autoConfirmActive && effectiveAutoConfirm > 0) {
            while (secondsLeft > 0) {
                kotlinx.coroutines.delay(1000)
                secondsLeft--
            }
            if (secondsLeft == 0 && autoConfirmActive) onAllow()
        }
    }

    val progress = if (effectiveAutoConfirm > 0)
        1f - (secondsLeft.toFloat() / effectiveAutoConfirm.toFloat())
    else 0f

    Column(
        modifier          = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Drag handle (decorative — swipe disabled at call-site via sheetSwipeEnabled = false)
        Surface(
            shape = RoundedCornerShape(50),
            color = MaterialTheme.colorScheme.outlineVariant,
            modifier = Modifier.size(width = 32.dp, height = 4.dp)
        ) {}

        Spacer(Modifier.height(20.dp))

        // Icon + badge row
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(config.type.emoji(), style = MaterialTheme.typography.headlineSmall)
            Spacer(Modifier.width(12.dp))
            Column {
                Text(
                    text  = "Review This Action",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.height(4.dp))
                SeverityBadge(
                    label  = config.badgeLabel,
                    status = config.type.badgeStatus()
                )
            }
        }

        Spacer(Modifier.height(20.dp))

        // Action preview box
        Surface(
            shape  = MaterialTheme.shapes.medium,
            color  = MaterialTheme.colorScheme.surfaceContainer,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            Column(Modifier.padding(16.dp).fillMaxWidth()) {
                Text(
                    text  = "WHAT WILL HAPPEN",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text  = config.previewContent,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        Spacer(Modifier.height(12.dp))

        // Risk label
        Text(
            text      = config.riskLabel,
            style     = MaterialTheme.typography.bodySmall,
            color     = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(16.dp))

        // Destructive checkbox
        if (isDestructive) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Checkbox(
                    checked         = confirmedDestructive,
                    onCheckedChange = { confirmedDestructive = it },
                    colors = CheckboxDefaults.colors(
                        checkedColor = MaterialTheme.colorScheme.error
                    )
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text  = "I understand this cannot be undone",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Spacer(Modifier.height(8.dp))
        }

        // Auto-confirm countdown
        if (effectiveAutoConfirm > 0 && autoConfirmActive) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.weight(1f).height(4.dp),
                    color    = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.outlineVariant
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text  = "${secondsLeft}s",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.width(8.dp))
                TextButton(
                    onClick = { autoConfirmActive = false },
                    contentPadding = PaddingValues(horizontal = 4.dp)
                ) {
                    Text("Cancel auto", style = MaterialTheme.typography.labelSmall)
                }
            }
            Spacer(Modifier.height(12.dp))
        }

        // Allow button
        val allowEnabled = !isDestructive || confirmedDestructive
        Button(
            onClick  = onAllow,
            enabled  = allowEnabled,
            modifier = Modifier.fillMaxWidth().height(52.dp),
            colors   = ButtonDefaults.buttonColors(
                containerColor = if (isDestructive)
                    MaterialTheme.colorScheme.error
                else MaterialTheme.colorScheme.primary
            )
        ) {
            Text(
                text  = if (isDestructive) "Delete" else "✓  Allow This Action",
                style = MaterialTheme.typography.labelLarge
            )
        }

        Spacer(Modifier.height(8.dp))

        // Stop button
        OutlinedButton(
            onClick  = onStop,
            modifier = Modifier.fillMaxWidth().height(52.dp),
            border   = BorderStroke(1.dp, MaterialTheme.colorScheme.error),
            colors   = ButtonDefaults.outlinedButtonColors(
                contentColor = MaterialTheme.colorScheme.error
            )
        ) {
            Text("✕  Stop Task Here", style = MaterialTheme.typography.labelLarge)
        }

        Spacer(Modifier.height(24.dp))
    }
}

// ── Preview ───────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true, backgroundColor = 0xFF0F1117)
@Composable
private fun SensitiveActionSheetPreview() {
    AiPhoneOperatorTheme(darkTheme = true) {
        SensitiveActionSheet(
            config = SensitiveActionConfig(
                type           = SensitiveActionType.SEND_MESSAGE,
                title          = "Review This Action",
                badgeLabel     = "SEND MESSAGE",
                previewContent = "To: Mom\nMessage: \"On my way\"",
                riskLabel      = "This message will be sent immediately and cannot be recalled.",
                autoConfirmSec = 10
            ),
            onAllow = {},
            onStop  = {}
        )
    }
}
