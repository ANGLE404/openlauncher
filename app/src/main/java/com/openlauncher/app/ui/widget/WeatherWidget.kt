package com.openlauncher.app.ui.widget

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.openlauncher.app.model.WeatherState
import com.openlauncher.app.model.cachedWeatherAgeLabel

@Composable
fun WeatherWidget(
    state: WeatherState?,
    accent: Color,
    metric: Boolean,
    isCached: Boolean = false,
    cacheSavedAtMillis: Long? = null,
    isDayMode: Boolean = false,
    modifier: Modifier = Modifier
) {
    val contentColor = MaterialTheme.colorScheme.onSurface
    val subColor     = MaterialTheme.colorScheme.onSurfaceVariant

    Box(modifier = modifier) {
        if (state != null) {
            Column(
                modifier            = Modifier.fillMaxSize().padding(12.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text     = state.conditionIcon,
                    fontSize = 30.sp
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text       = state.temperatureDisplay(metric),
                    color      = contentColor,
                    fontSize   = 38.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.sp
                )
                Text(
                    text = if (isCached) {
                        val age = cacheSavedAtMillis?.let(::cachedWeatherAgeLabel)
                        if (age != null) "${state.conditionLabel}（${age}缓存）"
                        else "${state.conditionLabel}（离线缓存）"
                    } else state.conditionLabel,
                    color         = subColor,
                    fontSize      = 9.sp,
                    letterSpacing = 0.sp,
                    textAlign     = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        } else {
            Column(
                modifier = Modifier.fillMaxSize().padding(12.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("天气", color = subColor, fontSize = 12.sp, letterSpacing = 0.sp)
                Spacer(Modifier.height(4.dp))
                Text("等待定位或网络", color = contentColor, fontSize = 14.sp)
                Text("暂时不可用", color = subColor, fontSize = 9.sp, letterSpacing = 0.sp)
            }
        }
    }
}
