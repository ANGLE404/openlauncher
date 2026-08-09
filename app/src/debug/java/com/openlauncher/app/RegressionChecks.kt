package com.openlauncher.app

import com.openlauncher.app.data.shouldEnableLegacyCustomTheme
import com.openlauncher.app.data.shouldUseLegacyFontColor
import com.openlauncher.app.data.shouldUseCustomBackground
import com.openlauncher.app.data.shouldUseCustomGradient
import com.openlauncher.app.model.cachedWeatherAgeLabel
import com.openlauncher.app.model.isWeatherCacheUsable
import com.openlauncher.app.model.shouldRefreshWeather
import com.openlauncher.app.model.weatherCacheRemainingMillis
import com.openlauncher.app.model.weatherDistanceKm
import com.openlauncher.app.util.activeFreshSpeedMpsOrNull
import com.openlauncher.app.util.freshSpeedMps
import com.openlauncher.app.util.freshSpeedMpsOrNull
import com.openlauncher.app.util.tripAverageSpeedMps

object RegressionChecks {
    @JvmStatic
    fun main(args: Array<String>) {
        check(!shouldEnableLegacyCustomTheme(null, null))
        check(!shouldEnableLegacyCustomTheme(true, null))
        check(!shouldEnableLegacyCustomTheme(false, 2))
        check(shouldEnableLegacyCustomTheme(true, 2))
        check(!shouldUseLegacyFontColor(2, 21, 20))
        check(!shouldUseLegacyFontColor(null, 20, 20))
        check(shouldUseLegacyFontColor(null, 21, 20))
        check(!shouldUseCustomBackground(isDayMode = true, useFullCustomTheme = false, backgroundLuminance = 0.01f))
        check(shouldUseCustomBackground(isDayMode = true, useFullCustomTheme = true, backgroundLuminance = 0.01f))
        check(shouldUseCustomBackground(isDayMode = true, useFullCustomTheme = false, backgroundLuminance = 0.8f))
        check(shouldUseCustomBackground(isDayMode = false, useFullCustomTheme = false, backgroundLuminance = 0.01f))
        check(!shouldUseCustomGradient(applyCustomBackground = false, useGradient = true))
        check(shouldUseCustomGradient(applyCustomBackground = true, useGradient = true))

        check(freshSpeedMps(12f, 10_000L, 17_999L) == 12f)
        check(freshSpeedMps(12f, 10_000L, 18_001L) == 0f)
        check(freshSpeedMpsOrNull(12f, 10_000L, 18_001L) == null)
        check(freshSpeedMps(-1f, 10_000L, 10_500L) == 0f)
        check(freshSpeedMps(12f, 11_000L, 10_000L) == 0f)
        check(activeFreshSpeedMpsOrNull(false, 12f, 10_000L, 10_500L) == null)
        check(activeFreshSpeedMpsOrNull(true, 12f, 10_000L, 10_500L) == 12f)
        check(tripAverageSpeedMps(165.0, 15.0) == 11.0)
        check(tripAverageSpeedMps(165.0, 0.0) == 0.0)

        val now = 1_000_000_000L
        check(isWeatherCacheUsable(now - 6L * 60 * 60 * 1_000, now))
        check(!isWeatherCacheUsable(now - 13L * 60 * 60 * 1_000, now))
        check(weatherCacheRemainingMillis(now - 11L * 60 * 60 * 1_000, now) == 60L * 60 * 1_000)
        check(weatherCacheRemainingMillis(now - 13L * 60 * 60 * 1_000, now) == 0L)
        check(cachedWeatherAgeLabel(now - 65L * 60 * 1_000, now) == "1 小时前")
        check(weatherDistanceKm(0.0, 0.0, 0.0, 1.0) in 111.0..112.0)
        check(!shouldRefreshWeather(now - 10L * 60 * 1_000, now, 0.0, 0.0, 0.01, 0.01))
        check(shouldRefreshWeather(now - 10L * 60 * 1_000, now, 0.0, 0.0, 0.0, 1.0))
        check(shouldRefreshWeather(now - 31L * 60 * 1_000, now, 0.0, 0.0, 0.01, 0.01))
    }
}
