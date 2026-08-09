package com.openlauncher.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.animation.core.tween
import androidx.compose.animation.animateColorAsState
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import com.openlauncher.app.data.AppFont

val LocalDayMode = staticCompositionLocalOf { false }
val LocalLauncherColors = staticCompositionLocalOf {
    com.openlauncher.app.data.DashboardStyle.OEM.defaultThemeColors(AccentIceBlue)
}

@Composable
fun OpenLauncherTheme(
    themeColors: ThemeColors = com.openlauncher.app.data.DashboardStyle.OEM.defaultThemeColors(AccentIceBlue),
    fontBold: Boolean = false,
    textScale: Float  = 1.0f,
    appFont: AppFont  = AppFont.PIXEL,
    isDayMode: Boolean = false,
    content: @Composable (ThemeColors) -> Unit
) {
    val transition = tween<Color>(420)
    val animatedAccent by animateColorAsState(themeColors.accent, transition, label = "accent_transition")
    val animatedBackground by animateColorAsState(themeColors.background, transition, label = "background_transition")
    val animatedSurface by animateColorAsState(themeColors.surface, transition, label = "surface_transition")
    val animatedElevatedSurface by animateColorAsState(themeColors.elevatedSurface, transition, label = "overlay_transition")
    val animatedBorder by animateColorAsState(themeColors.border, transition, label = "border_transition")
    val animatedPrimaryText by animateColorAsState(themeColors.primaryText, transition, label = "primary_text_transition")
    val animatedSecondaryText by animateColorAsState(themeColors.secondaryText, transition, label = "secondary_text_transition")
    val animatedGlow by animateColorAsState(themeColors.glow, transition, label = "glow_transition")
    val animatedColors = ThemeColors(
        accent = animatedAccent,
        background = animatedBackground,
        surface = animatedSurface,
        elevatedSurface = animatedElevatedSurface,
        border = animatedBorder,
        primaryText = animatedPrimaryText,
        secondaryText = animatedSecondaryText,
        glow = animatedGlow
    )
    // Contrast-aware: the accent is user-chosen and can be any brightness,
    // so a fixed onPrimary (white) goes invisible on light accents
    val onAccent = if (animatedAccent.luminance() > 0.5f) Color.Black else Color.White
    val colorScheme = if (isDayMode) lightColorScheme(
        primary          = animatedAccent,
        onPrimary        = onAccent,
        secondary        = animatedAccent.copy(alpha = 0.7f),
        onSecondary      = onAccent,
        tertiary         = animatedAccent.copy(alpha = 0.5f),
        background       = animatedColors.background,
        surface          = animatedColors.surface,
        onBackground     = animatedColors.primaryText,
        onSurface        = animatedColors.primaryText,
        surfaceVariant   = animatedColors.elevatedSurface,
        onSurfaceVariant = animatedColors.secondaryText,
        outline          = animatedColors.border
    ) else darkColorScheme(
        primary          = animatedAccent,
        onPrimary        = onAccent,
        secondary        = animatedAccent.copy(alpha = 0.7f),
        onSecondary      = onAccent,
        tertiary         = animatedAccent.copy(alpha = 0.5f),
        background       = animatedColors.background,
        surface          = animatedColors.surface,
        onBackground     = animatedColors.primaryText,
        onSurface        = animatedColors.primaryText,
        surfaceVariant   = animatedColors.elevatedSurface,
        onSurfaceVariant = animatedColors.secondaryText,
        outline          = animatedColors.border
    )
    CompositionLocalProvider(
        LocalDayMode provides isDayMode,
        LocalLauncherColors provides animatedColors
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography  = launcherTypography(
                bold = fontBold,
                scale = textScale,
                fontFamily = appFont.toFontFamily(),
                displayFontFamily = appFont.toFontFamily()
            ),
            shapes      = LauncherShapes,
        ) { content(animatedColors) }
    }
}
