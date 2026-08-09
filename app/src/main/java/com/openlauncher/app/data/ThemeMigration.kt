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

fun shouldUseCustomBackground(
    isDayMode: Boolean,
    useFullCustomTheme: Boolean,
    backgroundLuminance: Float
): Boolean = !isDayMode || useFullCustomTheme || backgroundLuminance >= 0.35f

fun shouldUseCustomGradient(
    applyCustomBackground: Boolean,
    useGradient: Boolean
): Boolean = applyCustomBackground && useGradient
