package com.shejan.keywe.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkMonochromeColorScheme = darkColorScheme(
    primary = MonochromeWhite,
    onPrimary = PitchBlack,
    secondary = MonochromeMuted,
    background = PitchBlack,
    surface = CharcoalDark,
    onBackground = MonochromeWhite,
    onSurface = MonochromeWhite,
    outline = GraphiteBorder,
    error = SignalRed
)

@Composable
fun KeyweTheme(
    content: @Composable () -> Unit
) {
    val colorScheme = DarkMonochromeColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = PitchBlack.toArgb()
            window.navigationBarColor = PitchBlack.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
            WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = false
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = DotMatrixTypography,
        content = content
    )
}