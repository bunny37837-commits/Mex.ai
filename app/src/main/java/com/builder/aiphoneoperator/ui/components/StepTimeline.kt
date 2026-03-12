package com.builder.aiphoneoperator.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.builder.aiphoneoperator.model.SampleData
import com.builder.aiphoneoperator.model.StepState
import com.builder.aiphoneoperator.model.TaskStep
import com.builder.aiphoneoperator.ui.theme.*

// ── Node color helpers ────────────────────────────────────────────────────────

fun StepState.nodeColor() = when (this) {
    StepState.COMPLETED -> StatusGreen
    StepState.RUNNING   -> IceBlue
    StepState.FAILED    -> StatusRed
    StepState.PENDING   -> Color.Transparent
}

fun StepState.nodeBorderColor() = when (this) {
    StepState.PENDING -> StatusNeutral
    else              -> Color.Transparent
}

// ── StepTimeline ──────────────────────────────────────────────────────────────

/**
 * Vertical timeline showing task steps.
 * [compact] = shorter spacing for history detail screens.
 */
@Composable
fun StepTimeline(
    steps    : List<TaskStep>,
    compact  : Boolean = false,
    modifier : Modifier = Modifier
) {
    val lineColor = MaterialTheme.colorScheme.outlineVariant

    Column(modifier = modifier.fillMaxWidth()) {
        steps.forEachIndexed { index, step ->
            val isLast = index == steps.lastIndex
            StepTimelineNode(
                step      = step,
                isLast    = isLast,
                compact   = compact,
                lineColor = lineColor
            )
        }
    }
}

@Composable
private fun StepTimelineNode(
    step      : TaskStep,
    isLast    : Boolean,
    compact   : Boolean,
    lineColor : Color
) {
    val nodeSize   = if (compact) 18.dp else 22.dp
    val nodeColor  = step.state.nodeColor()
    val borderColor= step.state.nodeBorderColor()
    val itemHeight = if (compact) 48.dp else 60.dp
    val isCurrent  = step.state == StepState.RUNNING

    // Spinning progress for current step
    val infiniteTransition = rememberInfiniteTransition(label = "step")
    val rotation by if (isCurrent) {
        infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue  = 360f,
            animationSpec = infiniteRepeatable(tween(1200, easing = LinearEasing)),
            label = "spin"
        )
    } else remember { mutableFloatStateOf(0f) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        // ── Left axis: node + line ────────────────────────────────────────
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.width(nodeSize + 8.dp)
        ) {
            // Node circle
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(nodeSize)
                    .clip(CircleShape)
                    .background(
                        if (step.state == StepState.PENDING)
                            MaterialTheme.colorScheme.surfaceVariant
                        else nodeColor
                    )
                    .then(
                        if (step.state == StepState.PENDING)
                            Modifier.drawBehind {
                                drawCircle(
                                    color  = borderColor,
                                    radius = size.minDimension / 2,
                                    style  = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.5.dp.toPx())
                                )
                            }
                        else Modifier
                    )
            ) {
                when (step.state) {
                    StepState.COMPLETED -> Text("✓", style = MaterialTheme.typography.labelSmall,
                        color = Color.White)
                    StepState.FAILED    -> Text("✕", style = MaterialTheme.typography.labelSmall,
                        color = Color.White)
                    StepState.RUNNING   -> CircularProgressIndicator(
                        modifier = Modifier.size(nodeSize - 4.dp),
                        color    = Color.White,
                        strokeWidth = 2.dp
                    )
                    StepState.PENDING   -> {}
                }
            }

            // Connecting line (hidden for last node)
            if (!isLast) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .height(itemHeight - nodeSize)
                        .background(lineColor)
                )
            }
        }

        Spacer(Modifier.width(12.dp))

        // ── Step label + sub-label ────────────────────────────────────────
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(top = 2.dp, bottom = if (isLast) 0.dp else 8.dp)
        ) {
            Text(
                text  = step.label,
                style = if (isCurrent)
                    MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold
                    )
                else MaterialTheme.typography.bodyLarge,
                color = when (step.state) {
                    StepState.PENDING   -> MaterialTheme.colorScheme.onSurfaceVariant
                    StepState.COMPLETED -> MaterialTheme.colorScheme.onSurfaceVariant
                    else                -> MaterialTheme.colorScheme.onSurface
                }
            )
            if (step.subLabel.isNotEmpty()) {
                Spacer(Modifier.height(2.dp))
                Text(
                    text  = step.subLabel,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontStyle = FontStyle.Italic
                    ),
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

// ── Preview ───────────────────────────────────────────────────────────────────

@Preview(showBackground = true, backgroundColor = 0xFF0F1117)
@Composable
private fun StepTimelinePreview() {
    AiPhoneOperatorTheme(darkTheme = true) {
        StepTimeline(
            steps    = SampleData.taskSteps,
            modifier = Modifier.padding(vertical = 16.dp)
        )
    }
}
