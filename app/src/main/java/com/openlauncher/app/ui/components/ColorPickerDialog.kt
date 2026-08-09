package com.openlauncher.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import com.openlauncher.app.ui.theme.accentPresetLabels
import com.openlauncher.app.ui.theme.accentPresets

@Composable
fun ColorPickerDialog(
    title: String,
    initialColor: Color,
    onColorSelected: (Color) -> Unit,
    onDismiss: () -> Unit
) {
    // Seed HSV state from the initial color synchronously — initializing in a
    // LaunchedEffect made the sliders flash wrong positions on the first frame
    val initialHsv = remember(initialColor) {
        FloatArray(3).also { android.graphics.Color.colorToHSV(initialColor.toArgb(), it) }
    }
    var selectedColor by remember { mutableStateOf(initialColor) }
    var hue   by remember { mutableStateOf(initialHsv[0]) }
    var sat   by remember { mutableStateOf(initialHsv[1]) }
    var value by remember { mutableStateOf(initialHsv[2]) }

    fun rebuildColor() {
        selectedColor = Color(android.graphics.Color.HSVToColor(floatArrayOf(hue, sat, value)))
    }

    fun syncFrom(color: Color) {
        selectedColor = color
        val hsv = FloatArray(3)
        android.graphics.Color.colorToHSV(color.toArgb(), hsv)
        hue = hsv[0]; sat = hsv[1]; value = hsv[2]
    }

    val surfaceColor = MaterialTheme.colorScheme.surfaceVariant
    val contentColor = MaterialTheme.colorScheme.onSurface
    val secondaryColor = MaterialTheme.colorScheme.onSurfaceVariant
    val outlineColor = MaterialTheme.colorScheme.outline

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor    = surfaceColor,
        titleContentColor = contentColor,
        textContentColor  = secondaryColor,
        title = { Text(title) },
        text  = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                // Preset swatches
                Text("预设", style = MaterialTheme.typography.labelMedium, color = secondaryColor)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    accentPresets.forEachIndexed { i, color ->
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(color)
                                    .border(
                                        width = 1.dp,
                                        color = if (selectedColor == color) MaterialTheme.colorScheme.primary else outlineColor,
                                        shape = CircleShape
                                    )
                                    .clickable { syncFrom(color) }
                            )
                            Spacer(Modifier.height(2.dp))
                            Text(accentPresetLabels[i], style = MaterialTheme.typography.labelSmall, color = secondaryColor, fontSize = androidx.compose.ui.unit.TextUnit(8f, androidx.compose.ui.unit.TextUnitType.Sp))
                        }
                    }
                }

                HorizontalDivider(color = outlineColor.copy(alpha = 0.45f))

                // Custom HSV sliders
                Text("自定义", style = MaterialTheme.typography.labelMedium, color = secondaryColor)

                // Hue slider
                Text("色相", style = MaterialTheme.typography.labelSmall, color = secondaryColor)
                Slider(
                    value = hue / 360f,
                    onValueChange = { hue = it * 360f; rebuildColor() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(24.dp)
                        .clip(MaterialTheme.shapes.small)
                        .background(Brush.horizontalGradient(
                            colors = (0..6).map { i ->
                                Color(android.graphics.Color.HSVToColor(floatArrayOf(i * 60f, 1f, 1f)))
                            }
                        )),
                    colors = SliderDefaults.colors(
                        thumbColor = contentColor,
                        activeTrackColor = Color.Transparent,
                        inactiveTrackColor = Color.Transparent
                    )
                )

                // Saturation slider
                Text("饱和度", style = MaterialTheme.typography.labelSmall, color = secondaryColor)
                Slider(
                    value = sat,
                    onValueChange = { sat = it; rebuildColor() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(24.dp)
                        .clip(MaterialTheme.shapes.small)
                        .background(Brush.horizontalGradient(listOf(
                            Color(android.graphics.Color.HSVToColor(floatArrayOf(hue, 0f, value))),
                            Color(android.graphics.Color.HSVToColor(floatArrayOf(hue, 1f, value)))
                        ))),
                    colors = SliderDefaults.colors(
                        thumbColor = contentColor,
                        activeTrackColor = Color.Transparent,
                        inactiveTrackColor = Color.Transparent
                    )
                )

                // Brightness slider
                Text("亮度", style = MaterialTheme.typography.labelSmall, color = secondaryColor)
                Slider(
                    value = value,
                    onValueChange = { value = it; rebuildColor() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(24.dp)
                        .clip(MaterialTheme.shapes.small)
                        .background(Brush.horizontalGradient(listOf(
                            Color.Black,
                            Color(android.graphics.Color.HSVToColor(floatArrayOf(hue, sat, 1f)))
                        ))),
                    colors = SliderDefaults.colors(
                        thumbColor = contentColor,
                        activeTrackColor = Color.Transparent,
                        inactiveTrackColor = Color.Transparent
                    )
                )

                // Preview swatch
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(36.dp)
                        .clip(MaterialTheme.shapes.extraSmall)
                        .background(selectedColor)
                )
            }
        },
        confirmButton = {
            // Filled with the chosen color + auto-contrast label, so Apply stays
            // visible no matter how dark or light the selection is
            Button(
                onClick = { onColorSelected(selectedColor); onDismiss() },
                colors = ButtonDefaults.buttonColors(
                    containerColor = selectedColor,
                    contentColor   = if (selectedColor.luminance() > 0.5f) Color.Black else Color.White
                )
            ) {
                Text("应用")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("取消", color = secondaryColor) }
        }
    )
}
