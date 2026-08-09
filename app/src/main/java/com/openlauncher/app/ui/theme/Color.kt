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

data class ThemeColors(
    val accent: Color,
    val background: Color,
    val surface: Color,
    val elevatedSurface: Color,
    val border: Color,
    val primaryText: Color,
    val secondaryText: Color,
    val glow: Color
)

fun com.openlauncher.app.data.DashboardTheme.accent(): Color = when (this) {
    com.openlauncher.app.data.DashboardTheme.ICE_BLUE -> AccentIceBlue
    com.openlauncher.app.data.DashboardTheme.TRACK_ORANGE -> AccentTrackOrange
    com.openlauncher.app.data.DashboardTheme.ALERT_RED -> AccentAlertRed
    com.openlauncher.app.data.DashboardTheme.AURORA_GREEN -> AccentAuroraGreen
    com.openlauncher.app.data.DashboardTheme.NEON_PURPLE -> AccentNeonPurple
}

fun com.openlauncher.app.data.DashboardStyle.defaultThemeColors(
    accent: Color,
    isDayMode: Boolean = false
): ThemeColors = if (isDayMode) {
    when (this) {
        com.openlauncher.app.data.DashboardStyle.OEM -> ThemeColors(
            accent, Color(0xFFEEF2F5), Color(0xFFFFFFFF), Color(0xFFE3E9ED),
            Color(0xFFB8C4CC), Color(0xFF182027), Color(0xFF5C6870), accent
        )
        com.openlauncher.app.data.DashboardStyle.CYBER -> ThemeColors(
            accent, Color(0xFFEAF0F8), Color(0xFFF8FBFF), Color(0xFFDFE8F5),
            Color(0xFF9FB2CB), Color(0xFF111A28), Color(0xFF52657D), accent
        )
        com.openlauncher.app.data.DashboardStyle.GLASS -> ThemeColors(
            accent, Color(0xFFEAF3F4), Color(0xE6FFFFFF), Color(0xE6F3FAFB),
            Color(0xFF9DB8BE), Color(0xFF142125), Color(0xFF526B72), accent
        )
    }
} else {
    when (this) {
        com.openlauncher.app.data.DashboardStyle.OEM -> ThemeColors(
            accent, Color(0xFF0E1216), Color(0xFF171C21), Color(0xFF20272E),
            Color(0xFF35424C), Color(0xFFF4F7F8), Color(0xFFADB8C0), accent
        )
        com.openlauncher.app.data.DashboardStyle.CYBER -> ThemeColors(
            accent, Color(0xFF080C13), Color(0xFF101827), Color(0xFF172238),
            Color(0xFF425A7B), Color(0xFFF1F6FF), Color(0xFFA9B8CE), accent
        )
        com.openlauncher.app.data.DashboardStyle.GLASS -> ThemeColors(
            accent, Color(0xFF101518), Color(0xD9212B31), Color(0xE02B373F),
            Color(0xFF607887), Color(0xFFF4F8F9), Color(0xFFB6C4C9), accent
        )
    }
}

val accentPresets      = listOf(AccentBlack, AccentWhite, AccentBlue, AccentGreen, AccentAmber, AccentRed)
val accentPresetLabels = listOf("黑色", "白色", "蓝色", "绿色", "琥珀色", "红色")
