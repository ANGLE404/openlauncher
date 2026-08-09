package com.openlauncher.app.data

const val CURRENT_THEME_SCHEMA_VERSION = 2

fun shouldEnableLegacyCustomTheme(
    explicitSelection: Boolean?,
    storedSchemaVersion: Int?
): Boolean = explicitSelection == true && (storedSchemaVersion ?: 0) >= CURRENT_THEME_SCHEMA_VERSION

fun shouldUseLegacyFontColor(
    storedSchemaVersion: Int?,
    storedFontColor: Int?,
    defaultFontColor: Int
): Boolean = (storedSchemaVersion ?: 0) < CURRENT_THEME_SCHEMA_VERSION &&
    storedFontColor?.let { it != defaultFontColor } == true

fun migrateStoredAppFont(storedValue: String?): AppFont? = when (storedValue) {
    AppFont.NOTO_SANS_SC.name -> AppFont.PIXEL
    else -> storedValue?.let { value -> runCatching { AppFont.valueOf(value) }.getOrNull() }
}

fun shouldUseCustomBackground(
    isDayMode: Boolean,
    useFullCustomTheme: Boolean,
    backgroundLuminance: Float
): Boolean = !isDayMode || backgroundLuminance >= 0.35f

fun shouldUseCustomGradient(
    applyCustomBackground: Boolean,
    useGradient: Boolean,
    isDayMode: Boolean = false,
    gradientEndLuminance: Float = 1f
): Boolean = applyCustomBackground && useGradient &&
    (!isDayMode || gradientEndLuminance >= 0.35f)
