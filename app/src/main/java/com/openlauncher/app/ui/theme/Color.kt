package com.openlauncher.app.ui.theme

import androidx.compose.ui.graphics.Color

val Black       = Color(0xFF000000)
val White       = Color(0xFFFFFFFF)
val DimSurface  = Color(0xFF0D0D0D)
val CardSurface = Color(0xFF141414)
val DividerGray = Color(0xFF2A2A2A)
val TextMuted   = Color(0xFF888888)

// Preset accent colours
val AccentBlack = Color(0xFF000000)
val AccentWhite = Color(0xFFFFFFFF)
val AccentBlue  = Color(0xFF2979FF)
val AccentGreen = Color(0xFF00E676)
val AccentAmber = Color(0xFFFFAB00)
val AccentRed   = Color(0xFFFF1744)
val AccentIceBlue = Color(0xFF61DAFB)
val AccentTrackOrange = Color(0xFFFF9F43)
val AccentAlertRed = Color(0xFFFF5C70)
val AccentAuroraGreen = Color(0xFF46E6A5)
val AccentNeonPurple = Color(0xFFB58CFF)

data class ThemePalette(val accent: Color, val glow: Color, val surface: Color)

fun com.openlauncher.app.data.DashboardTheme.palette(): ThemePalette = when (this) {
    com.openlauncher.app.data.DashboardTheme.ICE_BLUE -> ThemePalette(AccentIceBlue, Color(0xFF2D9CDB), Color(0xFF111A20))
    com.openlauncher.app.data.DashboardTheme.TRACK_ORANGE -> ThemePalette(AccentTrackOrange, Color(0xFFE76F00), Color(0xFF1E1711))
    com.openlauncher.app.data.DashboardTheme.ALERT_RED -> ThemePalette(AccentAlertRed, Color(0xFFC62845), Color(0xFF201216))
    com.openlauncher.app.data.DashboardTheme.AURORA_GREEN -> ThemePalette(AccentAuroraGreen, Color(0xFF1BA673), Color(0xFF10201B))
    com.openlauncher.app.data.DashboardTheme.NEON_PURPLE -> ThemePalette(AccentNeonPurple, Color(0xFF7146D9), Color(0xFF191321))
}

val accentPresets      = listOf(AccentBlack, AccentWhite, AccentBlue, AccentGreen, AccentAmber, AccentRed)
val accentPresetLabels = listOf("Black", "White", "Blue", "Green", "Amber", "Red")
