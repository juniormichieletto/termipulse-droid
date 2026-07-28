package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val DarkTerminalColorScheme = darkColorScheme(
    primary = TerminalPrimary,
    onPrimary = TerminalBackground,
    primaryContainer = TerminalSurfaceVariant,
    onPrimaryContainer = TerminalPrimary,
    secondary = TerminalSecondary,
    onSecondary = TerminalBackground,
    secondaryContainer = TerminalSurfaceVariant,
    onSecondaryContainer = TerminalSecondary,
    tertiary = TerminalTertiary,
    background = TerminalBackground,
    onBackground = TerminalTextPrimary,
    surface = TerminalSurface,
    onSurface = TerminalTextPrimary,
    surfaceVariant = TerminalSurfaceVariant,
    onSurfaceVariant = TerminalTextSecondary,
    outline = TerminalBorder,
    error = TerminalError,
    onError = TerminalBackground
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = DarkTerminalColorScheme,
        typography = Typography,
        content = content
    )
}
