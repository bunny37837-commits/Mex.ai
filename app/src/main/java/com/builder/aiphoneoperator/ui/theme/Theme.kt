package com.builder.aiphoneoperator.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

// ── Dark color scheme (primary) ───────────────────────────────────────────────
private val DarkColorScheme = darkColorScheme(
    primary              = IceBlue,
    onPrimary            = Color(0xFF003354),
    primaryContainer     = IceBlueDim,
    onPrimaryContainer   = Color(0xFFB3E5FC),

    secondary            = TealAccent,
    onSecondary          = Color(0xFF003733),
    secondaryContainer   = Color(0xFF00504A),
    onSecondaryContainer = Color(0xFFB2DFDB),

    tertiary             = StatusAmber,
    onTertiary           = Color(0xFF3A2A00),
    tertiaryContainer    = StatusAmberDim,
    onTertiaryContainer  = Color(0xFFFFE0B2),

    error                = StatusRed,
    onError              = Color(0xFF5F0000),
    errorContainer       = StatusRedDim,
    onErrorContainer     = Color(0xFFFFCDD2),

    background           = Surface00,
    onBackground         = TextPrimary,

    surface              = Surface00,
    onSurface            = TextPrimary,
    surfaceVariant       = Surface01,
    onSurfaceVariant     = TextSecondary,

    surfaceContainer         = Surface02,
    surfaceContainerHigh     = Surface03,
    surfaceContainerHighest  = Surface03,

    outline              = OutlineDefault,
    outlineVariant       = OutlineSubtle,

    inverseSurface       = TextPrimary,
    inverseOnSurface     = Surface00,
    inversePrimary       = LightPrimary,
    scrim                = Color(0x80000000)
)

// ── Light color scheme (system-adaptive) ─────────────────────────────────────
private val LightColorScheme = lightColorScheme(
    primary              = LightPrimary,
    onPrimary            = Color.White,
    primaryContainer     = Color(0xFFCDE5F7),
    onPrimaryContainer   = Color(0xFF001E30),

    background           = LightBackground,
    onBackground         = LightOnSurface,
    surface              = LightSurface,
    onSurface            = LightOnSurface,
    surfaceVariant       = Color(0xFFDFE3F0),
    onSurfaceVariant     = Color(0xFF4A5060),
    outline              = Color(0xFF8B91A4),
    error                = Color(0xFFB71C1C),
)

@Composable
fun AiPhoneOperatorTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,        // keep brand colors; skip dynamic
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context)
            else           dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else      -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography  = AppTypography,
        shapes      = AppShapes,
        content     = content
    )
}
