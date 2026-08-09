package com.openlauncher.app.model

import kotlin.math.asin
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

data class WeatherState(
    val temperatureCelsius: Double,
    val weatherCode: Int,
    val windspeedKmh: Double,
    val isDay: Boolean
) {
    // roundToInt, not toInt — truncation displayed 20.9° as 20°
    fun temperatureDisplay(metric: Boolean): String =
        if (metric) "${Math.round(temperatureCelsius)}°C"
        else "${Math.round(celsiusToFahrenheit(temperatureCelsius))}°F"

    val conditionLabel: String get() = wmoCodeToLabel(weatherCode)
    val conditionIcon: String get() = wmoCodeToEmoji(weatherCode, isDay)
}

data class CachedWeather(
    val state: WeatherState,
    val savedAtMillis: Long,
    val latitude: Double? = null,
    val longitude: Double? = null
)

const val WEATHER_CACHE_MAX_AGE_MILLIS = 12L * 60 * 60 * 1_000
const val WEATHER_REFRESH_INTERVAL_MILLIS = 30L * 60 * 1_000
const val WEATHER_RELOCATION_DISTANCE_KM = 50.0

fun isWeatherCacheUsable(
    savedAtMillis: Long,
    nowMillis: Long = System.currentTimeMillis(),
    maxAgeMillis: Long = WEATHER_CACHE_MAX_AGE_MILLIS
): Boolean = nowMillis - savedAtMillis in 0..maxAgeMillis

fun weatherCacheRemainingMillis(
    savedAtMillis: Long,
    nowMillis: Long = System.currentTimeMillis(),
    maxAgeMillis: Long = WEATHER_CACHE_MAX_AGE_MILLIS
): Long {
    val ageMillis = nowMillis - savedAtMillis
    return if (ageMillis in 0..maxAgeMillis) maxAgeMillis - ageMillis else 0L
}

fun cachedWeatherAgeLabel(
    savedAtMillis: Long,
    nowMillis: Long = System.currentTimeMillis()
): String {
    val ageMinutes = ((nowMillis - savedAtMillis).coerceAtLeast(0L) / 60_000L)
    return when {
        ageMinutes < 1 -> "刚刚"
        ageMinutes < 60 -> "${ageMinutes} 分钟前"
        ageMinutes < 24 * 60 -> "${ageMinutes / 60} 小时前"
        else -> "${ageMinutes / (24 * 60)} 天前"
    }
}

fun weatherDistanceKm(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
    val earthRadiusKm = 6_371.0
    val latDelta = Math.toRadians(lat2 - lat1)
    val lonDelta = Math.toRadians(lon2 - lon1)
    val startLat = Math.toRadians(lat1)
    val endLat = Math.toRadians(lat2)
    val a = sin(latDelta / 2).pow(2) + cos(startLat) * cos(endLat) * sin(lonDelta / 2).pow(2)
    return 2 * earthRadiusKm * asin(sqrt(a.coerceIn(0.0, 1.0)))
}

fun shouldRefreshWeather(
    lastFetchMillis: Long,
    nowMillis: Long,
    lastLatitude: Double?,
    lastLongitude: Double?,
    currentLatitude: Double,
    currentLongitude: Double,
    refreshIntervalMillis: Long = WEATHER_REFRESH_INTERVAL_MILLIS,
    relocationDistanceKm: Double = WEATHER_RELOCATION_DISTANCE_KM
): Boolean {
    val elapsedMillis = nowMillis - lastFetchMillis
    if (lastFetchMillis <= 0L || elapsedMillis < 0L || elapsedMillis >= refreshIntervalMillis) return true
    if (lastLatitude == null || lastLongitude == null) return true
    return weatherDistanceKm(
        lastLatitude,
        lastLongitude,
        currentLatitude,
        currentLongitude
    ) >= relocationDistanceKm
}

private fun celsiusToFahrenheit(c: Double) = c * 9.0 / 5.0 + 32.0

private fun wmoCodeToLabel(code: Int): String = when (code) {
    0 -> "晴"
    1, 2, 3 -> "多云"
    45, 48 -> "雾"
    51, 53, 55 -> "毛毛雨"
    61, 63, 65 -> "雨"
    71, 73, 75 -> "雪"
    80, 81, 82 -> "阵雨"
    95 -> "雷暴"
    96, 99 -> "冰雹"
    else -> "未知"
}

private fun wmoCodeToEmoji(code: Int, isDay: Boolean): String = when (code) {
    0 -> if (isDay) "☀️" else "🌙"
    1, 2 -> if (isDay) "⛅" else "🌤"
    3 -> "☁️"
    45, 48 -> "🌫️"
    51, 53, 55, 61, 63, 65, 80, 81, 82 -> "🌧️"
    71, 73, 75 -> "❄️"
    95, 96, 99 -> "⛈️"
    else -> "🌡️"
}
