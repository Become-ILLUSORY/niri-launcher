package dev.niri.launcher.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val NiriDarkScheme = darkColorScheme(
    primary = NoctPrimary,
    onPrimary = NoctBg,
    primaryContainer = NoctSurfaceHi,
    onPrimaryContainer = NoctText,
    secondary = NoctPurple,
    onSecondary = NoctBg,
    secondaryContainer = NoctSurface,
    onSecondaryContainer = NoctText,
    tertiary = NoctCyan,
    onTertiary = NoctBg,
    background = NoctBg,
    onBackground = NoctText,
    surface = NoctSurface,
    onSurface = NoctText,
    surfaceVariant = NoctSurfaceHi,
    onSurfaceVariant = NoctTextDim,
    outline = NoctBorder,
    outlineVariant = NoctTextMuted,
    error = NoctRed,
    onError = NoctBg,
)

@Composable
fun NiriTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = NiriDarkScheme,
        content = content,
    )
}
