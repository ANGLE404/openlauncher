package com.openlauncher.app.ui.screen

import android.content.res.Configuration
import androidx.compose.animation.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.FormatAlignLeft
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.zIndex
import kotlin.math.roundToInt
import com.openlauncher.app.data.AppSettings
import com.openlauncher.app.data.ClockStyle
import com.openlauncher.app.data.computeWidgetMove
import com.openlauncher.app.data.GRID_COLS
import com.openlauncher.app.data.GRID_ROWS
import com.openlauncher.app.data.WidgetConfig
import com.openlauncher.app.model.NowPlayingState
import com.openlauncher.app.model.WeatherState
import com.openlauncher.app.ui.theme.LauncherChipShape
import com.openlauncher.app.ui.theme.LauncherControlShape
import com.openlauncher.app.ui.theme.LauncherDialogShape
import com.openlauncher.app.ui.theme.LauncherLargeShape
import com.openlauncher.app.ui.theme.LocalDayMode
import com.openlauncher.app.ui.widget.*
import java.util.Calendar
import com.openlauncher.app.util.LocationData

private val WIDGET_RADIUS = LauncherLargeShape

private fun widgetDisplayName(id: String): String = when (id) {
    "CLOCK" -> "时钟"
    "WEATHER" -> "天气"
    "NOW_PLAYING" -> "正在播放"
    "TELEMETRY" -> "指南针"
    "ALTIMETER" -> "高度计"
    "SPEEDOMETER" -> "速度表"
    "VITALS" -> "车机状态"
    "TRIP_TRACKER" -> "行程"
    "SOUNDBOARD" -> "音效板"
    else -> "组件"
}

private data class WidgetTypeInfo(
    val id: String,
    val label: String,
    val icon: ImageVector,
    val description: String
)

private val ALL_WIDGET_TYPES = listOf(
    WidgetTypeInfo("CLOCK",       "时钟",         Icons.Default.AccessTime,  "时间与日期"),
    WidgetTypeInfo("WEATHER",     "天气",         Icons.Default.Cloud,       "当前天气"),
    WidgetTypeInfo("NOW_PLAYING", "正在播放", Icons.Default.MusicNote,   "媒体控制"),
    WidgetTypeInfo("TELEMETRY",   "指南针",       Icons.Default.Explore,     "速度与航向"),
    WidgetTypeInfo("ALTIMETER",   "高度计",       Icons.Default.FlightTakeoff, "横滚、俯仰与高度"),
    WidgetTypeInfo("SPEEDOMETER", "速度表",       Icons.Default.Speed,         "GPS 速度"),
    WidgetTypeInfo("VITALS",      "车况",         Icons.Default.Dns,           "车机健康状态"),
    WidgetTypeInfo("TRIP_TRACKER", "行程追踪", Icons.Default.Map,          "行程记录与统计"),
    WidgetTypeInfo("SOUNDBOARD",  "音效板",       Icons.Default.Piano,         "自定义音效按钮")
)

private fun canAddWidget(settings: com.openlauncher.app.data.AppSettings): Boolean {
    val visibleIds = buildSet {
        if (settings.showClock) add("CLOCK")
        if (settings.showWeather) add("WEATHER")
        if (settings.showNowPlaying) add("NOW_PLAYING")
        if (settings.showTelemetry) add("TELEMETRY")
        if (settings.showAltimeter) add("ALTIMETER")
        if (settings.showSpeedometer) add("SPEEDOMETER")
        if (settings.showVitals) add("VITALS")
        if (settings.showTripTracker) add("TRIP_TRACKER")
        if (settings.showSoundboard) add("SOUNDBOARD")
    }
    val activeWidgets = settings.widgetLayout.filter { it.enabled && it.id in visibleIds }
    val occupied = buildSet<Pair<Int, Int>> {
        activeWidgets.forEach { w ->
            for (dx in 0 until w.spanX) for (dy in 0 until w.spanY) add(w.gridX + dx to w.gridY + dy)
        }
    }
    val hasFreeCell = (0 until com.openlauncher.app.data.GRID_ROWS).any { r ->
        (0 until com.openlauncher.app.data.GRID_COLS).any { c -> (c to r) !in occupied }
    }
    // Also true if any active widget spans >1 cell and can be shrunk to make room
    val hasShrinkable = activeWidgets.any { it.spanX * it.spanY > 1 }
    return hasFreeCell || hasShrinkable
}

@Composable
fun HomeScreen(
    settings: AppSettings,
    weather: WeatherState?,
    weatherIsCached: Boolean = false,
    weatherCacheSavedAtMillis: Long? = null,
    nowPlaying: NowPlayingState?,
    location: LocationData?,
    bearing: Float,
    isWifi: Boolean,
    isData: Boolean,
    isDayMode: Boolean = false,
    onPlayPause: () -> Unit,
    onNext: () -> Unit,
    onPrev: () -> Unit,
    onLaunchCarPlay: () -> Unit,
    onLaunchAndroidAuto: () -> Unit,
    onAssignCarPlay: () -> Unit,
    onAssignAndroidAuto: () -> Unit,
    onClearCarPlay: () -> Unit,
    onClearAndroidAuto: () -> Unit,
    onAssignPip: () -> Unit,
    onClearPip: () -> Unit,
    onLaunchPip: () -> Unit,
    onTapNowPlaying: () -> Unit,
    onUpdateWidget: (id: String, spanX: Int, spanY: Int) -> Unit,
    onMoveWidget: (id: String, gridX: Int, gridY: Int) -> Unit,
    onAddWidget: (id: String) -> Unit,
    onRemoveWidget: (id: String) -> Unit,
    onSetClockStyle: (ClockStyle) -> Unit,
    onSetVitalsAsBars: (Boolean) -> Unit = {},
    onSetSpeedometerDigitalOnly: (Boolean) -> Unit = {},
    onUpdateSoundPad: (index: Int, pad: com.openlauncher.app.data.SoundPadConfig) -> Unit = { _, _ -> },
    hardwareRadio: com.openlauncher.app.viewmodel.LauncherViewModel.HardwareRadioState? = null,
    onLaunchHardwareRadio: () -> Unit = {},
    onStopHardwareRadio: () -> Unit = {},
    onRadioSeekUp: () -> Unit = {},
    onRadioSeekDown: () -> Unit = {},
    onRadioCycleFm: () -> Unit = {},
    onRadioSwitchAm: () -> Unit = {},
    onRadioTune: (band: String, freq: Float) -> Unit = { _, _ -> },
    onAssignRadio: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val accent       = MaterialTheme.colorScheme.primary
    val gap          = 6.dp
    val hasWallpaper = settings.wallpaperUri.isNotEmpty()
    val widgetBg = if (hasWallpaper) {
        MaterialTheme.colorScheme.surface.copy(alpha = 0.88f)
    } else {
        MaterialTheme.colorScheme.surface
    }
    val widgetBorder = MaterialTheme.colorScheme.outline
    val headerTextColor = MaterialTheme.colorScheme.onBackground
    val statusIconColor = MaterialTheme.colorScheme.onSurfaceVariant
    val controlIconColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.82f)

    var resizingId    by remember { mutableStateOf<String?>(null) }
    var contextMenuId by remember { mutableStateOf<String?>(null) }

    val configuration    = LocalConfiguration.current
    val isLandscape      = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    var editMode         by remember { mutableStateOf(false) }
    var widgetLibraryOpen by remember { mutableStateOf(false) }

    Column(modifier = modifier.fillMaxSize()) {

        // ── Header ──────────────────────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .padding(horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text          = settings.vehicleName.uppercase(),
                style         = MaterialTheme.typography.titleLarge,
                color         = headerTextColor,
                letterSpacing = 3.sp,
                fontSize      = 14.sp
            )
            Spacer(Modifier.weight(1f))
            AnimatedVisibility(visible = isWifi, enter = fadeIn(), exit = fadeOut()) {
                Icon(Icons.Default.Wifi, "无线网络", tint = statusIconColor, modifier = Modifier.size(16.dp))
            }
            if (isWifi) Spacer(Modifier.width(6.dp))
            AnimatedVisibility(visible = isData, enter = fadeIn(), exit = fadeOut()) {
                Icon(Icons.Default.SignalCellularAlt, "移动数据", tint = statusIconColor, modifier = Modifier.size(16.dp))
            }
            if (isLandscape) {
                Spacer(Modifier.width(8.dp))
                if (editMode) {
                    IconButton(
                        onClick  = { widgetLibraryOpen = true },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector        = Icons.Default.Dashboard,
                            contentDescription = "组件库",
                            tint               = controlIconColor,
                            modifier           = Modifier.size(15.dp)
                        )
                    }
                    Spacer(Modifier.width(2.dp))
                }
                IconButton(
                    onClick  = { editMode = !editMode },
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector        = Icons.Default.Edit,
                        contentDescription = "编辑组件",
                        tint               = if (editMode) accent else controlIconColor,
                        modifier           = Modifier.size(15.dp)
                    )
                }
            }
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.55f))

        // ── Widget Grid ─────────────────────────────────────────────────────
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(gap)
        ) {
            val cellW = (maxWidth  - gap * (GRID_COLS - 1)) / GRID_COLS
            val cellH = (maxHeight - gap * (GRID_ROWS - 1)) / GRID_ROWS
            val density = LocalDensity.current
            val cellStepXPx = with(density) { (cellW + gap).toPx() }
            val cellStepYPx = with(density) { (cellH + gap).toPx() }

            // WEATHER stays in the set even with no data: the commit path
            // (LauncherViewModel.moveWidgetConfig) computes against settings flags
            // only, so dropping it here would make the drop ghost and the committed
            // layout disagree. With no data the cell renders fully transparent.
            val visibleIds = buildSet {
                if (settings.showClock) add("CLOCK")
                if (settings.showWeather) add("WEATHER")
                if (settings.showNowPlaying) add("NOW_PLAYING")
                if (settings.showTelemetry) add("TELEMETRY")
                if (settings.showAltimeter) add("ALTIMETER")
                if (settings.showSpeedometer) add("SPEEDOMETER")
                if (settings.showVitals) add("VITALS")
                if (settings.showTripTracker) add("TRIP_TRACKER")
                if (settings.showSoundboard) add("SOUNDBOARD")
            }

            // Keep only visible widgets exactly as configured in settings, allowing explicit resizing to dictate layout
            val visible = settings.widgetLayout.filter { it.enabled && it.id in visibleIds }
            val rendered = visible

            // ── Drag state ───────────────────────────────────────────────────
            var draggingId   by remember { mutableStateOf<String?>(null) }
            var dragOffsetPx by remember { mutableStateOf(Offset.Zero) }

            // Compute snap target for the widget being dragged (uses original spanX)
            val draggingOriginal = if (draggingId != null) visible.find { it.id == draggingId } else null
            val targetGridX = draggingOriginal?.let {
                (it.gridX + (dragOffsetPx.x / cellStepXPx).roundToInt()).coerceIn(0, GRID_COLS - it.spanX)
            }
            val targetGridY = draggingOriginal?.let {
                (it.gridY + (dragOffsetPx.y / cellStepYPx).roundToInt()).coerceIn(0, GRID_ROWS - it.spanY)
            }

            // Compute proposed layout (push preview) while dragging
            val proposedLayout = if (draggingOriginal != null && targetGridX != null && targetGridY != null)
                computeWidgetMove(visible, draggingOriginal.id, targetGridX, targetGridY)
            else null

            // Drop ghost — rendered before widgets so it appears beneath them
            if (draggingOriginal != null && targetGridX != null && targetGridY != null) {
                val gX = (cellW + gap) * targetGridX
                val gY = (cellH + gap) * targetGridY
                val gW = cellW * draggingOriginal.spanX + gap * (draggingOriginal.spanX - 1)
                val gH = cellH * draggingOriginal.spanY + gap * (draggingOriginal.spanY - 1)
                Box(
                    modifier = Modifier
                        .absoluteOffset(x = gX, y = gY)
                        .size(gW, gH)
                        .background(accent.copy(alpha = 0.08f))
                        .border(1.dp, accent.copy(alpha = 0.5f), WIDGET_RADIUS)
                )
            }

            // Displacement ghosts — show where pushed widgets will land
            if (proposedLayout != null && draggingOriginal != null) {
                proposedLayout
                    .filter { it.id != draggingOriginal.id }
                    .forEach { proposed ->
                        val original = visible.find { it.id == proposed.id } ?: return@forEach
                        if (proposed.gridX != original.gridX || proposed.gridY != original.gridY) {
                            val dX = (cellW + gap) * proposed.gridX
                            val dY = (cellH + gap) * proposed.gridY
                            val dW = cellW * proposed.spanX + gap * (proposed.spanX - 1)
                            val dH = cellH * proposed.spanY + gap * (proposed.spanY - 1)
                            Box(
                                modifier = Modifier
                                    .absoluteOffset(x = dX, y = dY)
                                    .size(dW, dH)
                                    .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.65f), WIDGET_RADIUS)
                            )
                        }
                    }
            }

            rendered.forEach { w ->
                val xOff   = (cellW + gap) * w.gridX
                val yOff   = (cellH + gap) * w.gridY
                val width  = cellW * w.spanX + gap * (w.spanX - 1)
                val height = cellH * w.spanY + gap * (w.spanY - 1)

                val label = if (w.id == "CLOCK") clockTimeLabel(Calendar.getInstance()) else widgetDisplayName(w.id)

                // Original (pre-auto-expand) spanX needed for drag boundary clamping
                val origSpanX  = visible.find { it.id == w.id }?.spanX ?: 1
                val isDragging = draggingId == w.id
                val isGhost    = false
                val dragDpX    = if (isDragging) with(density) { dragOffsetPx.x.toDp() } else 0.dp
                val dragDpY    = if (isDragging) with(density) { dragOffsetPx.y.toDp() } else 0.dp

                @OptIn(ExperimentalFoundationApi::class)
                Box(
                    modifier = Modifier
                        .absoluteOffset(x = xOff + dragDpX, y = yOff + dragDpY)
                        .size(width, height)
                        .zIndex(if (isDragging) 1f else 0f)
                        .clip(WIDGET_RADIUS)
                        .background(if (isGhost) Color.Transparent else widgetBg)
                        .border(
                            width = if (editMode) 1.5.dp else 1.dp,
                            color = when {
                                editMode -> accent.copy(alpha = 0.45f)
                                isGhost  -> Color.Transparent
                                else     -> widgetBorder
                            },
                            shape = WIDGET_RADIUS
                        )
                        .combinedClickable(
                            indication        = null,
                            interactionSource = remember { MutableInteractionSource() },
                            onClick           = { if (editMode) contextMenuId = w.id },
                            onLongClick       = { if (!editMode) contextMenuId = w.id }
                        )
                        .then(
                            if (editMode) Modifier.pointerInput(editMode, w.id, w.gridX, w.gridY) {
                                var hasSignificantDrag = false
                                // Touch-slop gate: without it, sub-pixel jitter during a
                                // long-press counts as a drag and the context menu never opens
                                val slop = viewConfiguration.touchSlop
                                detectDragGesturesAfterLongPress(
                                    onDragStart = { _ ->
                                        draggingId         = w.id
                                        dragOffsetPx       = Offset.Zero
                                        hasSignificantDrag = false
                                    },
                                    onDrag = { change, dragAmount ->
                                        change.consume()
                                        dragOffsetPx      += dragAmount
                                        if (!hasSignificantDrag && dragOffsetPx.getDistance() > slop) {
                                            hasSignificantDrag = true
                                        }
                                    },
                                    onDragEnd = {
                                        if (hasSignificantDrag) {
                                            val newX = (w.gridX + (dragOffsetPx.x / cellStepXPx).roundToInt())
                                                .coerceIn(0, GRID_COLS - origSpanX)
                                            val newY = (w.gridY + (dragOffsetPx.y / cellStepYPx).roundToInt())
                                                .coerceIn(0, GRID_ROWS - w.spanY)
                                            onMoveWidget(w.id, newX, newY)
                                        } else {
                                            contextMenuId = w.id
                                        }
                                        draggingId   = null
                                        dragOffsetPx = Offset.Zero
                                    },
                                    onDragCancel = {
                                        draggingId   = null
                                        dragOffsetPx = Offset.Zero
                                    }
                                )
                            } else Modifier
                        )
                ) {
                    when (w.id) {
                        "CLOCK" -> ClockWidget(
                            style      = settings.clockStyle,
                            accent     = accent,
                            isDayMode  = isDayMode,
                            modifier   = Modifier.fillMaxSize()
                        )
                        "WEATHER" -> WeatherWidget(
                            state      = weather,
                            accent     = accent,
                            metric     = settings.unitSystem.name == "METRIC",
                            isCached  = weatherIsCached,
                            cacheSavedAtMillis = weatherCacheSavedAtMillis,
                            isDayMode  = isDayMode,
                            modifier   = Modifier.fillMaxSize()
                        )
                        "NOW_PLAYING" -> NowPlayingWidget(
                            state               = nowPlaying,
                            accent              = accent,
                            carPlayPackage      = settings.carPlayPackage,
                            androidAutoPackage  = settings.androidAutoPackage,
                            onPlayPause         = onPlayPause,
                            onNext              = onNext,
                            onPrev              = onPrev,
                            onLaunchCarPlay     = onLaunchCarPlay,
                            onLaunchAndroidAuto = onLaunchAndroidAuto,
                            onTapToOpenApp      = onTapNowPlaying,
                            modifier            = Modifier.fillMaxSize(),
                            isEditing           = editMode,
                            isDayMode           = isDayMode,
                            hardwareRadio         = hardwareRadio,
                            onLaunchHardwareRadio = onLaunchHardwareRadio,
                            onStopHardwareRadio   = onStopHardwareRadio,
                            onRadioSeekUp         = onRadioSeekUp,
                            onRadioSeekDown       = onRadioSeekDown,
                            onRadioCycleFm        = onRadioCycleFm,
                            onRadioSwitchAm       = onRadioSwitchAm,
                            onRadioTune           = onRadioTune,
                            onAssignRadio         = onAssignRadio
                        )
                        "TELEMETRY" -> TelemetryWidget(
                            location  = location,
                            bearing   = (bearing + settings.compassOffset + 360f) % 360f,
                            accent    = accent,
                            isDayMode = isDayMode,
                            modifier  = Modifier.fillMaxSize()
                        )
                        "ALTIMETER" -> AltimeterWidget(
                            location  = location,
                            isMetric  = settings.unitSystem == com.openlauncher.app.data.UnitSystem.METRIC,
                            accent    = accent,
                            isDayMode = isDayMode,
                            modifier  = Modifier.fillMaxSize()
                        )
                        "SPEEDOMETER" -> SpeedometerWidget(
                            location  = location,
                            isMetric  = settings.unitSystem == com.openlauncher.app.data.UnitSystem.METRIC,
                            accent    = accent,
                            isDayMode = isDayMode,
                            digitalOnly = settings.speedometerDigitalOnly,
                            modifier  = Modifier.fillMaxSize()
                        )
                        "VITALS" -> VitalsWidget(
                            accent    = accent,
                            isDayMode = isDayMode,
                            asBars    = settings.vitalsAsBars,
                            modifier  = Modifier.fillMaxSize()
                        )
                        "TRIP_TRACKER" -> TripTrackerWidget(
                            location  = location,
                            isMetric  = settings.unitSystem == com.openlauncher.app.data.UnitSystem.METRIC,
                            accent    = accent,
                            isDayMode = isDayMode,
                            modifier  = Modifier.fillMaxSize()
                        )
                        "SOUNDBOARD" -> SoundboardWidget(
                            pads      = settings.soundboardPads,
                            accent    = accent,
                            isDayMode = isDayMode,
                            isEditing = editMode,
                            onUpdatePad = onUpdateSoundPad,
                            modifier  = Modifier.fillMaxSize()
                        )
                    }

                    // Label — hide when album art fills the widget background
                    val labelColor = when {
                        isGhost -> Color.Transparent
                        w.id == "NOW_PLAYING" && nowPlaying?.albumArt != null && nowPlaying.title.isNotEmpty() -> Color.Transparent
                        else -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.72f)
                    }
                    Text(
                        text          = label,
                        style         = MaterialTheme.typography.labelSmall,
                        color         = labelColor,
                        letterSpacing = 2.sp,
                        fontSize      = 8.sp,
                        modifier      = Modifier
                            .align(Alignment.TopStart)
                            .padding(start = 10.dp, top = 7.dp)
                    )
                }
            }
        }
    }

    // ── Widget context menu (long-press any cell) ────────────────────────────
    contextMenuId?.let { id ->
        WidgetContextMenu(
            widgetId            = id,
            accent              = accent,
            clockStyle          = settings.clockStyle,
            vitalsAsBars        = settings.vitalsAsBars,
            speedometerDigitalOnly = settings.speedometerDigitalOnly,
            carPlayPackage      = settings.carPlayPackage,
            androidAutoPackage  = settings.androidAutoPackage,
            pipAppPackage       = settings.pipAppPackage,
            isDayMode           = isDayMode,
            onResize            = { contextMenuId = null; resizingId = id },
            onAssignCarPlay     = { contextMenuId = null; onAssignCarPlay() },
            onAssignAndroidAuto = { contextMenuId = null; onAssignAndroidAuto() },
            onClearCarPlay      = { contextMenuId = null; onClearCarPlay() },
            onClearAndroidAuto  = { contextMenuId = null; onClearAndroidAuto() },
            onAssignPip         = { contextMenuId = null; onAssignPip() },
            onClearPip          = { contextMenuId = null; onClearPip() },
            onSetClockStyle     = { onSetClockStyle(it) },
            onSetVitalsAsBars   = { onSetVitalsAsBars(it) },
            onSetSpeedometerDigitalOnly = { onSetSpeedometerDigitalOnly(it) },
            onDismiss           = { contextMenuId = null }
        )
    }

    // ── Resize dialog ────────────────────────────────────────────────────────
    resizingId?.let { id ->
        val config = settings.widgetLayout.find { it.id == id }
        if (config != null) {
            WidgetResizeDialog(
                config    = config,
                accent    = accent,
                isDayMode = isDayMode,
                onDismiss = { resizingId = null },
                onConfirm = { sx, sy ->
                    onUpdateWidget(id, sx, sy)
                    resizingId = null
                }
            )
        }
    }

    // ── Widget library ────────────────────────────────────────────────────────
    if (widgetLibraryOpen) {
        WidgetLibraryDialog(
            settings  = settings,
            accent    = accent,
            isDayMode = isDayMode,
            onAdd     = { id -> onAddWidget(id) },
            onRemove  = { id -> onRemoveWidget(id) },
            onDismiss = { widgetLibraryOpen = false }
        )
    }
}

@Composable
private fun WidgetContextMenu(
    widgetId: String,
    accent: Color,
    clockStyle: ClockStyle,
    vitalsAsBars: Boolean,
    speedometerDigitalOnly: Boolean,
    carPlayPackage: String = "",
    androidAutoPackage: String = "",
    pipAppPackage: String = "",
    isDayMode: Boolean,
    onResize: () -> Unit,
    onAssignCarPlay: () -> Unit,
    onAssignAndroidAuto: () -> Unit,
    onClearCarPlay: () -> Unit,
    onClearAndroidAuto: () -> Unit,
    onAssignPip: () -> Unit,
    onClearPip: () -> Unit,
    onSetClockStyle: (ClockStyle) -> Unit,
    onSetVitalsAsBars: (Boolean) -> Unit,
    onSetSpeedometerDigitalOnly: (Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    val menuBg = MaterialTheme.colorScheme.surfaceVariant
    val menuBorder = MaterialTheme.colorScheme.outline
    val menuDivider = MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)
    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .clip(LauncherControlShape)
                .background(menuBg)
                .border(1.dp, menuBorder, LauncherControlShape)
                .padding(vertical = 4.dp)
                .width(200.dp)
        ) {
            val inactiveMenuTint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.62f)
            ContextRow("调整大小", Icons.Default.OpenWith, accent, onResize, isDayMode = isDayMode)
            if (widgetId == "CLOCK") {
                HorizontalDivider(color = menuDivider)
                ContextRow(
                    label   = "数字",
                    icon    = Icons.Default.Schedule,
                    tint    = if (clockStyle == ClockStyle.DIGITAL) accent else inactiveMenuTint,
                    onClick = { onSetClockStyle(ClockStyle.DIGITAL); onDismiss() },
                    isDayMode = isDayMode
                )
                HorizontalDivider(color = menuDivider)
                ContextRow(
                    label   = "指针",
                    icon    = Icons.Default.Watch,
                    tint    = if (clockStyle == ClockStyle.ANALOG) accent else inactiveMenuTint,
                    onClick = { onSetClockStyle(ClockStyle.ANALOG); onDismiss() },
                    isDayMode = isDayMode
                )
            }
            if (widgetId == "VITALS") {
                HorizontalDivider(color = menuDivider)
                ContextRow(
                    label   = "仪表盘视图",
                    icon    = Icons.Default.Adjust,
                    tint    = if (!vitalsAsBars) accent else inactiveMenuTint,
                    onClick = { onSetVitalsAsBars(false); onDismiss() },
                    isDayMode = isDayMode
                )
                HorizontalDivider(color = menuDivider)
                ContextRow(
                    label   = "条形视图",
                    icon    = Icons.AutoMirrored.Filled.FormatAlignLeft,
                    tint    = if (vitalsAsBars) accent else inactiveMenuTint,
                    onClick = { onSetVitalsAsBars(true); onDismiss() },
                    isDayMode = isDayMode
                )
            }
            if (widgetId == "SPEEDOMETER") {
                HorizontalDivider(color = menuDivider)
                ContextRow(
                    label   = "仪表盘轨迹",
                    icon    = Icons.Default.Speed,
                    tint    = if (!speedometerDigitalOnly) accent else inactiveMenuTint,
                    onClick = { onSetSpeedometerDigitalOnly(false); onDismiss() },
                    isDayMode = isDayMode
                )
                HorizontalDivider(color = menuDivider)
                ContextRow(
                    label   = "仅数字",
                    icon    = Icons.Default.Dialpad,
                    tint    = if (speedometerDigitalOnly) accent else inactiveMenuTint,
                    onClick = { onSetSpeedometerDigitalOnly(true); onDismiss() },
                    isDayMode = isDayMode
                )
            }
            if (widgetId == "NOW_PLAYING") {
                HorizontalDivider(color = menuDivider)
                ContextRow("指定 CarPlay 应用",      Icons.Default.PhoneAndroid,  accent, onAssignCarPlay, isDayMode = isDayMode)
                if (carPlayPackage.isNotEmpty()) {
                    HorizontalDivider(color = menuDivider)
                    ContextRow("清除 CarPlay 应用", Icons.Default.PhoneAndroid, MaterialTheme.colorScheme.error, onClearCarPlay, isDayMode = isDayMode)
                }
                HorizontalDivider(color = menuDivider)
                ContextRow("指定 Android Auto 应用", Icons.Default.DirectionsCar, accent, onAssignAndroidAuto, isDayMode = isDayMode)
                if (androidAutoPackage.isNotEmpty()) {
                    HorizontalDivider(color = menuDivider)
                    ContextRow("清除 Android Auto 应用", Icons.Default.DirectionsCar, MaterialTheme.colorScheme.error, onClearAndroidAuto, isDayMode = isDayMode)
                }
            }

        }
    }
}

@Composable
private fun ContextRow(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    tint: Color,
    onClick: () -> Unit,
    isDayMode: Boolean = false
) {
    val finalTint = tint
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(icon, null, tint = finalTint, modifier = Modifier.size(16.dp))
        Text(label, color = finalTint, fontSize = 10.sp, letterSpacing = 1.sp)
    }
}

@Composable
private fun WidgetResizeDialog(
    config: WidgetConfig,
    accent: Color,
    isDayMode: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (spanX: Int, spanY: Int) -> Unit
) {
    var spanX by remember { mutableStateOf(config.spanX) }
    var spanY by remember { mutableStateOf(config.spanY) }

    val maxSpanX = GRID_COLS - config.gridX
    val maxSpanY = GRID_ROWS - config.gridY

    val dialogBg = MaterialTheme.colorScheme.surfaceVariant
    val dialogText = MaterialTheme.colorScheme.onSurface
    val cancelColor = MaterialTheme.colorScheme.onSurfaceVariant
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text          = widgetDisplayName(config.id),
                color         = dialogText,
                fontSize      = 11.sp,
                letterSpacing = 2.sp
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
                SpanRow(label = "宽度",  value = spanX, min = 1, max = maxSpanX, accent = accent, isDayMode = isDayMode) { spanX = it }
                SpanRow(label = "高度", value = spanY, min = 1, max = maxSpanY, accent = accent, isDayMode = isDayMode) { spanY = it }
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(spanX, spanY) }) {
                Text("应用", color = accent, fontSize = 11.sp, letterSpacing = 1.sp)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消", color = cancelColor, fontSize = 11.sp, letterSpacing = 1.sp)
            }
        },
        containerColor    = dialogBg,
        titleContentColor = dialogText,
        textContentColor  = dialogText
    )
}

@Composable
private fun SpanRow(
    label: String,
    value: Int,
    min: Int,
    max: Int,
    accent: Color,
    isDayMode: Boolean,
    onChange: (Int) -> Unit
) {
    val textColor = MaterialTheme.colorScheme.onSurface
    val dimColor = MaterialTheme.colorScheme.onSurfaceVariant
    val disabledC = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f)
    val inactiveBg = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)
    Row(
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text          = label,
            color         = dimColor,
            fontSize      = 10.sp,
            letterSpacing = 1.sp,
            modifier      = Modifier.width(52.dp)
        )
        IconButton(
            onClick  = { if (value > min) onChange(value - 1) },
            modifier = Modifier.size(32.dp)
        ) {
            Icon(
                Icons.Default.Remove, "减小${label}",
                tint     = if (value > min) textColor else disabledC,
                modifier = Modifier.size(16.dp)
            )
        }
        Text(
            text      = "$value",
            color     = textColor,
            fontSize  = 16.sp,
            textAlign = TextAlign.Center,
            modifier  = Modifier.width(24.dp)
        )
        IconButton(
            onClick  = { if (value < max) onChange(value + 1) },
            modifier = Modifier.size(32.dp)
        ) {
            Icon(
                Icons.Default.Add, "增大${label}",
                tint     = if (value < max) accent else disabledC,
                modifier = Modifier.size(16.dp)
            )
        }
        Spacer(Modifier.weight(1f))
        Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
            repeat(max) { i ->
                Box(
                    modifier = Modifier
                        .size(width = 14.dp, height = 10.dp)
                        .background(
                            if (i < value) accent.copy(alpha = 0.7f) else inactiveBg,
                            LauncherChipShape
                        )
                )
            }
        }
    }
}

// ── Widget Library ────────────────────────────────────────────────────────────

@Composable
private fun WidgetLibraryDialog(
    settings: AppSettings,
    accent: Color,
    isDayMode: Boolean,
    onAdd: (String) -> Unit,
    onRemove: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val dialogBg = MaterialTheme.colorScheme.surfaceVariant
    val dialogBorder = MaterialTheme.colorScheme.outline
    val titleColor = MaterialTheme.colorScheme.onSurfaceVariant
    val closeColor = MaterialTheme.colorScheme.onSurfaceVariant

    val activeIds = buildSet {
        if (settings.showClock) add("CLOCK")
        if (settings.showWeather) add("WEATHER")
        if (settings.showNowPlaying) add("NOW_PLAYING")
        if (settings.showTelemetry) add("TELEMETRY")
        if (settings.showAltimeter) add("ALTIMETER")
        if (settings.showSpeedometer) add("SPEEDOMETER")
        if (settings.showVitals) add("VITALS")
        if (settings.showTripTracker) add("TRIP_TRACKER")
        if (settings.showSoundboard) add("SOUNDBOARD")
    }
    val canAdd = canAddWidget(settings)

    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .clip(LauncherDialogShape)
                .background(dialogBg)
                .border(1.dp, dialogBorder, LauncherDialogShape)
                .padding(16.dp)
                .widthIn(min = 320.dp, max = 520.dp)
        ) {
            Row(
                modifier          = Modifier.fillMaxWidth().padding(bottom = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text          = "组件库",
                    color         = titleColor,
                    fontSize      = 9.sp,
                    letterSpacing = 2.sp
                )
                Spacer(Modifier.weight(1f))
                IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.Default.Close, "关闭组件库", tint = closeColor, modifier = Modifier.size(14.dp))
                }
            }

            LazyVerticalGrid(
                columns               = GridCells.Fixed(4),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement   = Arrangement.spacedBy(6.dp),
                modifier              = Modifier.fillMaxWidth()
            ) {
                items(ALL_WIDGET_TYPES) { info ->
                    val isActive = info.id in activeIds
                    WidgetLibraryCard(
                        info     = info,
                        isActive = isActive,
                        canAdd   = canAdd,
                        accent   = accent,
                        isDayMode = isDayMode,
                        onToggle = { if (isActive) onRemove(info.id) else onAdd(info.id) }
                    )
                }
            }

            if (!canAdd) {
                Spacer(Modifier.height(10.dp))
                Text(
                    text          = "全部 ${GRID_COLS * GRID_ROWS} 个单元已占用，移除组件后才能继续添加",
                    color         = MaterialTheme.colorScheme.error,
                    fontSize      = 8.sp,
                    letterSpacing = 1.sp,
                    modifier      = Modifier.fillMaxWidth(),
                    textAlign     = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun WidgetLibraryCard(
    info: WidgetTypeInfo,
    isActive: Boolean,
    canAdd: Boolean,
    accent: Color,
    isDayMode: Boolean,
    onToggle: () -> Unit
) {
    val enabled    = isActive || canAdd
    val cardBorder = if (isActive) accent else MaterialTheme.colorScheme.outline
    val cardBg = if (isActive) accent.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surface
    val iconTint = if (isActive) accent else MaterialTheme.colorScheme.onSurfaceVariant
    val labelColor = if (isActive) accent else MaterialTheme.colorScheme.onSurface

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .clip(LauncherControlShape)
            .background(cardBg)
            .border(1.dp, cardBorder, LauncherControlShape)
            .clickable(enabled = enabled, onClick = onToggle)
            .padding(6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(info.icon, null, tint = iconTint, modifier = Modifier.size(18.dp))
        Spacer(Modifier.height(5.dp))
        Text(
            text          = info.label,
            color         = labelColor,
            fontSize      = 7.sp,
            letterSpacing = 1.sp,
            textAlign     = TextAlign.Center,
            maxLines      = 2,
            lineHeight    = 9.sp
        )
        Spacer(Modifier.height(3.dp))
        Text(
            text          = when {
                isActive -> "已启用"
                !canAdd  -> "已满"
                else     -> "添加"
            },
            color         = when {
                isActive -> accent.copy(alpha = 0.75f)
                !canAdd  -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f)
                else     -> MaterialTheme.colorScheme.onSurfaceVariant
            },
            fontSize      = 6.sp,
            letterSpacing = 1.sp,
            textAlign     = TextAlign.Center
        )
    }
}
