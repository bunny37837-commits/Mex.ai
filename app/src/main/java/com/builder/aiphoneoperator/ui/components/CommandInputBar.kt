package com.builder.aiphoneoperator.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.builder.aiphoneoperator.ui.theme.*

enum class VoiceState { IDLE, RECORDING }

/**
 * Bottom-pinned command input bar with text field + voice button + send button.
 * Parent is responsible for [isRecording] state and callbacks.
 */
@Composable
fun CommandInputBar(
    text        : String,
    voiceState  : VoiceState,
    onTextChange: (String) -> Unit,
    onSend      : () -> Unit,
    onVoiceToggle: () -> Unit,
    modifier    : Modifier = Modifier
) {
    val canSend = text.isNotBlank()

    Surface(
        color     = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp,
        modifier  = modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .navigationBarsPadding()
        ) {
            // ── Text field ────────────────────────────────────────────────
            OutlinedTextField(
                value         = text,
                onValueChange = onTextChange,
                placeholder   = {
                    Text(
                        text  = if (voiceState == VoiceState.RECORDING) "Listening…"
                                else "Give AI a command…",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                shape         = RoundedCornerShape(28.dp),
                singleLine    = false,
                maxLines      = 4,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(onSend = { if (canSend) onSend() }),
                colors        = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor   = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                    focusedContainerColor   = MaterialTheme.colorScheme.surfaceContainer,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainer
                ),
                modifier = Modifier.weight(1f)
            )

            Spacer(Modifier.width(8.dp))

            // ── Voice button ──────────────────────────────────────────────
            VoiceInputButton(
                state    = voiceState,
                onToggle = onVoiceToggle
            )

            Spacer(Modifier.width(8.dp))

            // ── Send button ───────────────────────────────────────────────
            FilledIconButton(
                onClick  = onSend,
                enabled  = canSend,
                modifier = Modifier.size(48.dp),
                colors   = IconButtonDefaults.filledIconButtonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Icon(
                    imageVector        = Icons.Rounded.Send,
                    contentDescription = "Send command",
                    modifier           = Modifier.size(22.dp)
                )
            }
        }
    }
}

// ── Voice Input Button ────────────────────────────────────────────────────────

@Composable
fun VoiceInputButton(
    state    : VoiceState,
    onToggle : () -> Unit,
    modifier : Modifier = Modifier
) {
    val isRecording = state == VoiceState.RECORDING

    val infiniteTransition = rememberInfiniteTransition(label = "voice")
    val scale by if (isRecording) {
        infiniteTransition.animateFloat(
            initialValue = 1f,
            targetValue  = 1.15f,
            animationSpec = infiniteRepeatable(
                tween(600, easing = FastOutSlowInEasing),
                RepeatMode.Reverse
            ),
            label = "voice_scale"
        )
    } else remember { mutableFloatStateOf(1f) }

    val containerColor = if (isRecording)
        MaterialTheme.colorScheme.errorContainer
    else
        MaterialTheme.colorScheme.secondaryContainer

    FilledIconButton(
        onClick  = onToggle,
        modifier = modifier.size(48.dp),
        colors   = IconButtonDefaults.filledIconButtonColors(containerColor = containerColor)
    ) {
        // Using a text stand-in since resource drawables aren't available in this context.
        // Replace with Icon(painterResource(R.drawable.ic_mic), …) in real project.
        Text(
            text  = if (isRecording) "●" else "🎤",
            style = MaterialTheme.typography.labelLarge
        )
    }
}

// ── Preview ───────────────────────────────────────────────────────────────────

@Preview(showBackground = true, backgroundColor = 0xFF0F1117)
@Composable
private fun CommandInputBarPreview() {
    AiPhoneOperatorTheme(darkTheme = true) {
        var text by remember { mutableStateOf("") }
        CommandInputBar(
            text         = text,
            voiceState   = VoiceState.IDLE,
            onTextChange = { text = it },
            onSend       = {},
            onVoiceToggle = {}
        )
    }
}
