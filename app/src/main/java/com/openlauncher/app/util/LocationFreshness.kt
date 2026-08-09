package com.openlauncher.app.util

const val LOCATION_SPEED_MAX_AGE_MILLIS = 8_000L

fun freshSpeedMps(
    speedMps: Float,
    capturedAtElapsedRealtimeMs: Long,
    nowElapsedRealtimeMs: Long,
    maxAgeMs: Long = LOCATION_SPEED_MAX_AGE_MILLIS
): Float = freshSpeedMpsOrNull(
    speedMps = speedMps,
    capturedAtElapsedRealtimeMs = capturedAtElapsedRealtimeMs,
    nowElapsedRealtimeMs = nowElapsedRealtimeMs,
    maxAgeMs = maxAgeMs
) ?: 0f

fun freshSpeedMpsOrNull(
    speedMps: Float,
    capturedAtElapsedRealtimeMs: Long,
    nowElapsedRealtimeMs: Long,
    maxAgeMs: Long = LOCATION_SPEED_MAX_AGE_MILLIS
): Float? {
    val ageMs = nowElapsedRealtimeMs - capturedAtElapsedRealtimeMs
    return if (speedMps.isFinite() && speedMps >= 0f && ageMs in 0..maxAgeMs) speedMps else null
}

fun activeFreshSpeedMpsOrNull(
    isTrackingActive: Boolean,
    speedMps: Float,
    capturedAtElapsedRealtimeMs: Long,
    nowElapsedRealtimeMs: Long,
    maxAgeMs: Long = LOCATION_SPEED_MAX_AGE_MILLIS
): Float? = if (isTrackingActive) {
    freshSpeedMpsOrNull(
        speedMps = speedMps,
        capturedAtElapsedRealtimeMs = capturedAtElapsedRealtimeMs,
        nowElapsedRealtimeMs = nowElapsedRealtimeMs,
        maxAgeMs = maxAgeMs
    )
} else {
    null
}

fun tripAverageSpeedMps(distanceMeters: Double, movingDurationSeconds: Double): Double =
    if (distanceMeters >= 0.0 && movingDurationSeconds > 0.0) distanceMeters / movingDurationSeconds else 0.0
