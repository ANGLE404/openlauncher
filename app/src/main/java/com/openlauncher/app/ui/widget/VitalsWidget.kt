package com.openlauncher.app.ui.widget

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.openlauncher.app.ui.theme.LauncherPixelShape
import kotlinx.coroutines.delay
import java.io.File

private data class TemperatureReading(val value: Float?, val label: String)

@Composable
fun VitalsWidget(
    accent: Color,
    isDayMode: Boolean = false,
    asBars: Boolean = false,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val labelColor = MaterialTheme.colorScheme.onSurfaceVariant

    var cpuUsage by remember { mutableStateOf<Float?>(null) }
    var ramUsedPercent by remember { mutableStateOf<Float?>(null) }
    var ramDisplayGb by remember { mutableStateOf("0.0G") }
    var temperature by remember { mutableStateOf(TemperatureReading(null, "温度")) }

    // CPU Stat Tracking variables
    var lastCpuTime by remember { mutableLongStateOf(0L) }
    var lastIdleTime by remember { mutableLongStateOf(0L) }

    // Temperature tracking helper (Thermal files -> Battery fallback)
    val getTemperatureReading = {
        val paths = listOf(
            "/sys/class/thermal/thermal_zone0/temp",
            "/sys/class/thermal/thermal_zone1/temp",
            "/sys/devices/virtual/thermal/thermal_zone0/temp",
            "/sys/class/hwmon/hwmon0/device/temp1_input"
        )
        var foundTemp = -1f
        for (path in paths) {
            try {
                val file = File(path)
                if (file.exists() && file.canRead()) {
                    val tempStr = file.readText().trim()
                    var temp = tempStr.toFloatOrNull() ?: continue
                    if (temp > 1000f) temp /= 1000f
                    if (temp in 10f..150f) {
                        foundTemp = temp
                        break
                    }
                }
            } catch (_: Exception) {}
        }
        
        if (foundTemp != -1f) {
            TemperatureReading(foundTemp, "设备温度")
        } else {
            val batteryTemperature = runCatching {
                val intent = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
                val rawTemp = intent?.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0) ?: 0
                if (rawTemp > 0) rawTemp / 10f else null
            }.getOrNull()
            TemperatureReading(batteryTemperature, if (batteryTemperature != null) "电池温度" else "温度不可用")
        }
    }

    // Process RAM telemetry
    val updateRam = {
        try {
            val am = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            val memInfo = ActivityManager.MemoryInfo()
            am.getMemoryInfo(memInfo)
            
            val availGb = memInfo.availMem.toDouble() / (1024.0 * 1024.0 * 1024.0)
            val totalGb = memInfo.totalMem.toDouble() / (1024.0 * 1024.0 * 1024.0)
            val usedGb = totalGb - availGb
            
            ramUsedPercent = ((usedGb / totalGb) * 100f).toFloat().coerceIn(0f, 100f)
            ramDisplayGb = "%.1fG".format(usedGb)
        } catch (_: Exception) {
            ramUsedPercent = null
            ramDisplayGb = "—"
        }
    }

    // Process CPU telemetry
    val updateCpu = {
        try {
            var updated = false
            val file = File("/proc/stat")
            if (file.exists() && file.canRead()) {
                val line = file.useLines { it.firstOrNull() }
                if (line != null && line.startsWith("cpu ")) {
                    val parts = line.split("\\s+".toRegex())
                    if (parts.size >= 5) {
                        val user = parts[1].toLong()
                        val nice = parts[2].toLong()
                        val system = parts[3].toLong()
                        val idle = parts[4].toLong()
                        val ioWait = if (parts.size > 5) parts[5].toLong() else 0L
                        val irq = if (parts.size > 6) parts[6].toLong() else 0L
                        val softIrq = if (parts.size > 7) parts[7].toLong() else 0L
 
                        val active = user + nice + system + ioWait + irq + softIrq
                        val total = active + idle

                        val deltaTotal = total - (lastCpuTime + lastIdleTime)
                        val deltaIdle = idle - lastIdleTime

                        lastCpuTime = active
                        lastIdleTime = idle

                        if (deltaTotal > 0) {
                            cpuUsage = (((deltaTotal - deltaIdle).toFloat() / deltaTotal.toFloat()) * 100f).coerceIn(0f, 100f)
                            updated = true
                        }
                    }
                }
            }
            if (!updated) cpuUsage = null
        } catch (_: Exception) {
            cpuUsage = null
        }
    }

    // Periodic polling loop — the /proc and /sys reads are file I/O, so they
    // run on the IO dispatcher instead of blocking the main thread every tick
    LaunchedEffect(Unit) {
        while (true) {
            kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                updateCpu()
                updateRam()
                temperature = getTemperatureReading()
            }
            delay(2500)
        }
    }

    // Warning colors for diagnostics
    val cpuValue = cpuUsage ?: 0f
    val ramValue = ramUsedPercent ?: 0f
    val temperatureValue = temperature.value ?: 0f
    val cpuColor = if (cpuValue > 85f) Color(0xFFDD5555) else if (cpuValue > 65f) Color(0xFFE6A23C) else accent
    val ramColor = if (ramValue > 90f) Color(0xFFDD5555) else if (ramValue > 75f) Color(0xFFE6A23C) else accent
    val tempColor = if (temperatureValue > 75f) Color(0xFFDD5555) else if (temperatureValue > 60f) Color(0xFFE6A23C) else accent

    Column(
        modifier = modifier.padding(start = 14.dp, end = 14.dp, top = 22.dp, bottom = 8.dp),
        verticalArrangement = Arrangement.Center
    ) {
        if (asBars) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically)
            ) {
                BarGauge(
                    value = cpuValue,
                    label = "处理器",
                    displayValue = cpuUsage?.let { "%.0f%%".format(it) } ?: "不可用",
                    activeColor = cpuColor,
                    modifier = Modifier.fillMaxWidth()
                )
                BarGauge(
                    value = ramValue,
                    label = "内存",
                    displayValue = ramDisplayGb,
                    activeColor = ramColor,
                    modifier = Modifier.fillMaxWidth()
                )
                BarGauge(
                    value = temperatureValue,
                    label = temperature.label,
                    displayValue = temperature.value?.let { "%.0f°".format(it) } ?: "不可用",
                    activeColor = tempColor,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        } else {
            Row(
                modifier = Modifier.fillMaxWidth().weight(1f),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                DialGauge(
                    value = cpuValue,
                    label = "处理器",
                    displayValue = cpuUsage?.let { "%.0f%%".format(it) } ?: "不可用",
                    activeColor = cpuColor,
                    modifier = Modifier.weight(1f).fillMaxHeight()
                )

                DialGauge(
                    value = ramValue,
                    label = "内存",
                    displayValue = ramDisplayGb,
                    activeColor = ramColor,
                    modifier = Modifier.weight(1f).fillMaxHeight()
                )

                DialGauge(
                    value = temperatureValue,
                    label = temperature.label,
                    displayValue = temperature.value?.let { "%.0f°".format(it) } ?: "不可用",
                    activeColor = tempColor,
                    modifier = Modifier.weight(1f).fillMaxHeight()
                )
            }
        }
    }
}

@Composable
private fun BarGauge(
    value: Float,
    label: String,
    displayValue: String,
    activeColor: Color,
    modifier: Modifier = Modifier
) {
    val trackColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.28f)
    val contentColor = MaterialTheme.colorScheme.onSurface
    val labelColor = MaterialTheme.colorScheme.onSurfaceVariant

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.Center
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                color = labelColor,
                fontSize = 8.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp
            )
            Text(
                text = displayValue,
                color = contentColor,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(Modifier.height(3.dp))
        val barBorder = Modifier.border(
            0.5.dp,
            MaterialTheme.colorScheme.outline.copy(alpha = 0.55f),
            LauncherPixelShape
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .clip(LauncherPixelShape)
                .background(trackColor)
                .then(barBorder)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(fraction = (value / 100f).coerceIn(0f, 1f))
                    .background(activeColor)
            )
        }
    }
}

@Composable
private fun DialGauge(
    value: Float,
    label: String,
    displayValue: String,
    activeColor: Color,
    modifier: Modifier = Modifier
) {
    val trackColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.28f)
    val contentColor = MaterialTheme.colorScheme.onSurface
    val labelColor = MaterialTheme.colorScheme.onSurfaceVariant

    BoxWithConstraints(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        val sizePx = minOf(maxWidth, maxHeight)
        val strokeWidth = 4.5.dp

        Box(
            modifier = Modifier.size(sizePx),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize().padding(strokeWidth / 2)) {
                val sw = strokeWidth.toPx()

                drawArc(
                    color = trackColor,
                    startAngle = 150f,
                    sweepAngle = 240f,
                    useCenter = false,
                    style = Stroke(width = sw, cap = StrokeCap.Round)
                )

                drawArc(
                    color = activeColor,
                    startAngle = 150f,
                    sweepAngle = 240f * (value / 100f).coerceIn(0f, 1f),
                    useCenter = false,
                    style = Stroke(width = sw, cap = StrokeCap.Round)
                )
            }
            
            // Centered text indicators
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = displayValue,
                    color = contentColor,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(1.dp))
                Text(
                    text = label,
                    color = labelColor,
                    fontSize = 7.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
