package com.builder.aiphoneoperator.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.builder.aiphoneoperator.model.ServiceState
import com.builder.aiphoneoperator.ui.theme.*

// ── Status dot states ─────────────────────────────────────────────────────────

enum class ChipStatus { ACTIVE, WARNING, ERROR, INACTIVE, LOADING }

fun ChipStatus.dotColor() = when (this) {
    ChipStatus.ACTIVE   -> StatusGreen
    ChipStatus.WARNING  -> StatusAmber
    ChipStatus.ERROR    -> StatusRed
    ChipStatus.INACTIVE -> StatusNeutral
    ChipStatus.LOADING  -> IceBlue
}

fun ServiceState.toChipStatus() = when (this) {
    ServiceState.ACTIVE   -> ChipStatus.ACTIVE
    ServiceState.INACTIVE -> ChipStatus.INACTIVE
    ServiceState.LOADING  -> ChipStatus.LOADING
    ServiceState.ERROR    -> ChipStatus.ERROR
}

// ── StatusChip ────────────────────────────────────────────────────────────────

/**
 * Compact pill chip with a colored dot + text label.
 * Optionally pulsing when [status] == ACTIVE or LOADING.
 */
@Composable
fun StatusChip(
    label     : String,
    status    : ChipStatus,
    modifier  : Modifier = Modifier,
    onClick   : (() -> Unit)? = null
) {
    val dotColor = status.dotColor()
    val pulse    = status == ChipStatus.ACTIVE || status == ChipStatus.LOADING

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val alpha by if (pulse) {
        infiniteTransition.animateFloat(
            initialValue   = 1f,
            targetValue    = 0.4f,
            animationSpec  = infiniteRepeatable(
                animation  = tween(900, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "dot_alpha"
        )
    } else {
        remember { mutableFloatStateOf(1f) }
    }

    val containerColor = MaterialTheme.colorScheme.surfaceVariant
    val textColor      = MaterialTheme.colorScheme.onSurfaceVariant

    Surface(
        shape  = RoundedCornerShape(50),
        color  = containerColor,
        modifier = modifier
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
    ) {
        Row(
            verticalAlignment    = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(dotColor.copy(alpha = alpha))
            )
            Text(
                text  = label,
                style = MaterialTheme.typography.labelSmall,
                color = textColor
            )
        }
    }
}

// ── LargeStatusPill ───────────────────────────────────────────────────────────

/**
 * Larger pill used in onboarding and repair mode to show requirement state.
 */
@Composable
fun LargeStatusPill(
    label    : String,
    status   : ChipStatus,
    modifier : Modifier = Modifier
) {
    val bg   = when (status) {
        ChipStatus.ACTIVE   -> StatusGreenDim
        ChipStatus.WARNING  -> StatusAmberDim
        ChipStatus.ERROR    -> StatusRedDim
        ChipStatus.INACTIVE -> MaterialTheme.colorScheme.surfaceVariant
        ChipStatus.LOADING  -> IceBlueDim
    }
    val textColor = status.dotColor()

    Surface(
        shape    = RoundedCornerShape(50),
        color    = bg,
        modifier = modifier
    ) {
        Row(
            verticalAlignment    = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(textColor)
            )
            Text(
                text  = label,
                style = MaterialTheme.typography.labelLarge,
                color = textColor
            )
        }
    }
}

// ── SeverityBadge ─────────────────────────────────────────────────────────────

@Composable
fun SeverityBadge(label: String, status: ChipStatus, modifier: Modifier = Modifier) {
    val bg        = status.dotColor().copy(alpha = 0.15f)
    val textColor = status.dotColor()
    Surface(
        shape    = RoundedCornerShape(4.dp),
        color    = bg,
        modifier = modifier
    ) {
        Text(
            text     = label,
            style    = MaterialTheme.typography.labelSmall,
            color    = textColor,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
        )
    }
}

// ── Previews ──────────────────────────────────────────────────────────────────

@Preview(showBackground = true, backgroundColor = 0xFF0F1117)
@Composable
private fun StatusChipPreview() {
    AiPhoneOperatorTheme(darkTheme = true) {
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(16.dp)
        ) {
            StatusChip("Active",    ChipStatus.ACTIVE)
            StatusChip("Warning",   ChipStatus.WARNING)
            StatusChip("Error",     ChipStatus.ERROR)
            StatusChip("Inactive",  ChipStatus.INACTIVE)
            StatusChip("Loading…",  ChipStatus.LOADING)
            LargeStatusPill("Accessibility: Enabled", ChipStatus.ACTIVE)
            LargeStatusPill("Accessibility: Disabled", ChipStatus.ERROR)
        }
    }
}
