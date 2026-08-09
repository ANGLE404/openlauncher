package com.openlauncher.app.ui.widget

import android.content.Context
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.edit
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.openlauncher.app.util.LocationData
import com.openlauncher.app.util.activeFreshSpeedMpsOrNull
import com.openlauncher.app.util.tripAverageSpeedMps
import kotlinx.coroutines.delay

@Composable
fun TripTrackerWidget(
    location: LocationData?,
    isMetric: Boolean,
    accent: Color,
    isDayMode: Boolean = false,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val tripPreferences = remember {
        context.getSharedPreferences("trip_tracker", Context.MODE_PRIVATE)
    }
    val colorScheme = MaterialTheme.colorScheme
    val displayColor = colorScheme.onSurface
    val dimDisplayColor = colorScheme.onSurface.copy(alpha = 0.08f)
    val lcdBg = Color.Transparent
    val lcdBorder = colorScheme.outline.copy(alpha = 0.65f)
    val labelColor = colorScheme.onSurfaceVariant
    val activeAccent = accent
    val teRed = colorScheme.error
    val teGrey = colorScheme.outline

    val lifecycleOwner = LocalLifecycleOwner.current
    val lifecycleState by lifecycleOwner.lifecycle.currentStateFlow.collectAsState()
    val isTrackingActive = lifecycleState.isAtLeast(Lifecycle.State.STARTED)

    var isRunning by rememberSaveable { mutableStateOf(tripPreferences.getBoolean("running", false)) }
    var driveTimeSeconds by rememberSaveable { mutableLongStateOf(tripPreferences.getLong("drive_seconds", 0L)) }
    var idleTimeSeconds by rememberSaveable { mutableLongStateOf(tripPreferences.getLong("idle_seconds", 0L)) }
    var movingDurationSeconds by rememberSaveable {
        mutableDoubleStateOf(
            tripPreferences.getString("moving_duration_seconds", null)?.toDoubleOrNull()
                ?: tripPreferences.getLong("moving_seconds", 0L).toDouble()
        )
    }
    var tripDistanceMeters by rememberSaveable { mutableDoubleStateOf(tripPreferences.getString("distance_meters", "0.0")?.toDoubleOrNull() ?: 0.0) }
    var driveTimeFraction by remember { mutableDoubleStateOf(0.0) }
    var idleTimeFraction by remember { mutableDoubleStateOf(0.0) }

    fun persistTrip() {
        tripPreferences.edit {
            putBoolean("running", isRunning)
            putLong("drive_seconds", driveTimeSeconds)
            putLong("idle_seconds", idleTimeSeconds)
            remove("speed_sum")
            putString("moving_duration_seconds", movingDurationSeconds.toString())
            putLong("moving_seconds", movingDurationSeconds.toLong())
            putString("distance_meters", tripDistanceMeters.toString())
        }
    }

    // The trip loop is keyed on isRunning only, so it must read location through
    // rememberUpdatedState — a plain parameter capture would freeze the GPS fix
    // at the moment tracking started and the trip would record nothing.
    val currentLocation by rememberUpdatedState(location)
    val currentTrackingActive by rememberUpdatedState(isTrackingActive)

    // Trip update loop
    LaunchedEffect(isRunning) {
        var lastTickMs = android.os.SystemClock.elapsedRealtime()
        while (isRunning) {
            delay(1000)
            val now = android.os.SystemClock.elapsedRealtime()
            // Measure the real interval instead of assuming exactly 1 s per tick
            val dtSeconds = ((now - lastTickMs) / 1000.0).coerceIn(0.0, 5.0)
            lastTickMs = now
            val currentSpeed = currentLocation?.let { fix ->
                activeFreshSpeedMpsOrNull(
                    isTrackingActive = currentTrackingActive,
                    speedMps = fix.speedMps,
                    capturedAtElapsedRealtimeMs = fix.capturedAtElapsedRealtimeMs,
                    nowElapsedRealtimeMs = now
                )
            } ?: continue
            if (currentSpeed > 0.5f) {
                val elapsed = driveTimeFraction + dtSeconds
                driveTimeSeconds += elapsed.toLong()
                driveTimeFraction = elapsed % 1.0
                movingDurationSeconds += dtSeconds
                tripDistanceMeters += currentSpeed * dtSeconds
            } else {
                val elapsed = idleTimeFraction + dtSeconds
                idleTimeSeconds += elapsed.toLong()
                idleTimeFraction = elapsed % 1.0
            }
            persistTrip()
        }
    }

    // Calculations
    val averageSpeedMps = tripAverageSpeedMps(tripDistanceMeters, movingDurationSeconds)
    val avgSpeedDisplay = if (isMetric) averageSpeedMps * 3.6 else averageSpeedMps * 2.23694
    val speedUnit = if (isMetric) "KM/H" else "MPH"

    val distanceDisplay = if (isMetric) tripDistanceMeters / 1000.0 else tripDistanceMeters / 1609.34
    val distUnit = if (isMetric) "KM" else "MI"

    fun formatTime(seconds: Long): String {
        val h = seconds / 3600
        val m = (seconds % 3600) / 60
        val s = seconds % 60
        return "%02d:%02d:%02d".format(h, m, s)
    }

    // Flat, borderless Column that lets the launcher's card boundary frame the content
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(start = 14.dp, end = 14.dp, top = 22.dp, bottom = 8.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // 1. DYNAMIC MONOCHROME FLAT LCD PANEL
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .clip(RoundedCornerShape(12.dp))
                .background(lcdBg)
                .border(1.dp, lcdBorder, RoundedCornerShape(12.dp))
                .drawBehind {
                    val dotColor = displayColor.copy(alpha = 0.02f)
                    val dotSize = 1.dp.toPx()
                    val gap = 5.dp.toPx()
                    var x = 3.dp.toPx()
                    while (x < size.width) {
                        var y = 3.dp.toPx()
                        while (y < size.height) {
                            drawCircle(
                                color = dotColor,
                                radius = dotSize / 2,
                                center = Offset(x, y)
                            )
                            y += gap
                        }
                        x += gap
                    }
                }
                .padding(horizontal = 10.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Panel Column 1: Distance Readout
                Column(
                    modifier = Modifier.weight(0.38f),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "距离 // 距离",
                        color = labelColor,
                        fontSize = 6.5.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                    Row(
                        verticalAlignment = Alignment.Bottom,
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Box(contentAlignment = Alignment.BottomStart) {
                            Text(
                                text = "88.88",
                                color = dimDisplayColor,
                                fontSize = 24.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "%05.2f".format(distanceDisplay),
                                color = displayColor,
                                fontSize = 24.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Text(
                            text = distUnit,
                            color = displayColor,
                            fontSize = 9.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 2.dp)
                        )
                    }
                    
                    // Hired/Time-Off Indicators
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "[记录中]",
                            color = if (isRunning) activeAccent else dimDisplayColor,
                            fontSize = 6.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = "[已暂停]",
                            color = if (!isRunning && (driveTimeSeconds > 0 || idleTimeSeconds > 0)) teRed else dimDisplayColor,
                            fontSize = 6.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                // Panel Column 2: Drive & Idle Timers
                Column(
                    modifier = Modifier
                        .weight(0.30f)
                        .padding(horizontal = 2.dp),
                    verticalArrangement = Arrangement.SpaceBetween,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("驾驶［时间］", color = labelColor, fontSize = 6.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                        Box {
                            Text("88:88:88", color = dimDisplayColor, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                            Text(formatTime(driveTimeSeconds), color = displayColor, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                        }
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("怠速［时间］", color = labelColor, fontSize = 6.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                        Box {
                            Text("88:88:88", color = dimDisplayColor, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                            Text(formatTime(idleTimeSeconds), color = displayColor, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                        }
                    }
                }

                // Panel Column 3: Average Speed
                Column(
                    modifier = Modifier.weight(0.32f),
                    verticalArrangement = Arrangement.SpaceBetween,
                    horizontalAlignment = Alignment.End
                ) {
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "平均速度 // 速度",
                            color = labelColor,
                            fontSize = 6.5.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )
                        Row(
                            verticalAlignment = Alignment.Bottom,
                            horizontalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Box(contentAlignment = Alignment.BottomEnd) {
                                Text(
                                    text = "888.8",
                                    color = dimDisplayColor,
                                    fontSize = 15.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "%05.1f".format(avgSpeedDisplay),
                                    color = displayColor,
                                    fontSize = 15.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Text(
                                text = speedUnit,
                                color = displayColor,
                                fontSize = 7.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(bottom = 1.dp)
                            )
                        }
                    }
                    
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text("系统状态", color = labelColor, fontSize = 6.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                        Text(
                            text = if (isRunning) "A" else "I",
                            color = if (isRunning) activeAccent else displayColor,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                    }
            }
        }

        Spacer(Modifier.height(8.dp))

        // 2. FLAT MINIMALIST TACTILE BUTTONS
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Button 1: OPR / RUN styled as a flat dynamic circular cap
            TeTactileButton(
                label = if (isRunning) "暂停" else "开始",
                keyColor = activeAccent,
                active = isRunning,
                onClick = {
                    isRunning = !isRunning
                    persistTrip()
                }
            )

            // Button 2: RST / RESET
            val canReset = !isRunning && (driveTimeSeconds > 0 || idleTimeSeconds > 0)
            TeTactileButton(
                label = "重置",
                keyColor = teRed,
                active = false,
                enabled = canReset,
                onClick = {
                    driveTimeSeconds = 0L
                    idleTimeSeconds = 0L
                    movingDurationSeconds = 0.0
                    tripDistanceMeters = 0.0
                    driveTimeFraction = 0.0
                    idleTimeFraction = 0.0
                    persistTrip()
                }
            )

            TeTactileButton(
                label = "实测",
                keyColor = activeAccent,
                active = true,
                enabled = false,
                onClick = {}
            )

            // Button 4: SET
            TeTactileButton(
                label = "设置",
                keyColor = teGrey,
                active = false,
                enabled = false,
                onClick = {}
            )
        }
    }
}

@Composable
private fun TeTactileButton(
    label: String,
    keyColor: Color,
    active: Boolean,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    val printedLabelColor = colorScheme.onSurfaceVariant
    val buttonBg = if (!enabled) {
        Color.Transparent
    } else if (active) {
        keyColor
    } else {
        colorScheme.surfaceVariant
    }

    val buttonBorder = colorScheme.outline
    val dotColor = if (active) {
        colorScheme.onPrimary
    } else {
        if (enabled) keyColor else keyColor.copy(alpha = 0.2f)
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        // Monospace printed label above key
        Text(
            text = label,
            color = printedLabelColor,
            fontSize = 7.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.5.sp
        )

        // Flat, elegant minimalist circular keycap
        Box(
            modifier = Modifier
                .size(30.dp)
                .clip(CircleShape)
                .background(buttonBg)
                .border(1.dp, buttonBorder, CircleShape)
                .semantics { contentDescription = label }
                .clickable(enabled = enabled, role = Role.Button) { onClick() },
            contentAlignment = Alignment.Center
        ) {
            // Failsafe flat center indicator
            Box(
                modifier = Modifier
                    .size(5.dp)
                    .clip(CircleShape)
                    .background(dotColor)
            )
        }
    }
}
