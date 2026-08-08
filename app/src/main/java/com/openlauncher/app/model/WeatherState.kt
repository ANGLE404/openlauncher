package com.openlauncher.app.model

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
