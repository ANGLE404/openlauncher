package com.openlauncher.app.ui.screen

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.FormatAlignRight
import androidx.compose.material.icons.automirrored.filled.ShortText
import androidx.compose.material.icons.automirrored.filled.TrendingFlat
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.material3.LocalTextStyle
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import com.openlauncher.app.data.AppFont
import com.openlauncher.app.data.AppSettings
import com.openlauncher.app.data.DashboardTheme
import com.openlauncher.app.data.DashboardStyle
import com.openlauncher.app.data.DayNightMode
import com.openlauncher.app.data.SidebarPosition
import com.openlauncher.app.data.ShortcutConfig
import com.openlauncher.app.data.GradientDirection
import com.openlauncher.app.data.UnitSystem
import com.openlauncher.app.ui.theme.LocalDayMode
import com.openlauncher.app.util.SunriseSunset
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import com.openlauncher.app.ui.components.ColorPickerDialog
import com.openlauncher.app.ui.components.ConfirmDialog
import com.openlauncher.app.ui.theme.accent
import com.openlauncher.app.ui.theme.defaultThemeColors
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.openlauncher.app.BuildConfig

// Resolved at call site via LocalDayMode — see SettingsDivider / SettingsSection

@Composable
fun SettingsScreen(
    settings: AppSettings,
    accent: Color,
    onUpdate: (AppSettings.() -> AppSettings) -> Unit,
    onReset: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var showResetDialog       by remember { mutableStateOf(false) }
    var showAccentPicker      by remember { mutableStateOf(false) }
    var showBgPicker          by remember { mutableStateOf(false) }
    var showGradientEndPicker by remember { mutableStateOf(false) }
    var showFontColorPicker   by remember { mutableStateOf(false) }
    var showSurfaceColorPicker by remember { mutableStateOf(false) }
    var showOverlayColorPicker by remember { mutableStateOf(false) }
    var showBorderColorPicker by remember { mutableStateOf(false) }
    var showSecondaryTextColorPicker by remember { mutableStateOf(false) }

    // OpenDocument (not GetContent): only SAF document URIs carry a persistable
    // grant, so this is what actually keeps the wallpaper readable after reboot
    val wallpaperPicker = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let {
            runCatching {
                context.contentResolver.takePersistableUriPermission(
                    it, android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            }
            onUpdate { copy(wallpaperUri = it.toString()) }
        }
    }

    val isDayMode = LocalDayMode.current
    val screenBg  = MaterialTheme.colorScheme.background

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(screenBg)
    ) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // ── Title ────────────────────────────────────────────────────────────
        Text(
            text          = "设置",
            style         = MaterialTheme.typography.titleLarge,
            color         = MaterialTheme.colorScheme.onBackground,
            letterSpacing = 3.sp,
            fontSize      = 14.sp
        )

        Spacer(Modifier.height(4.dp))

        // ── Permissions ──────────────────────────────────────────────────────
        SettingsSection("权限") {
            val isMediaConnected by com.openlauncher.app.service.MediaListenerService.isConnected.collectAsState()

            // Bumped on ON_RESUME so statuses refresh when the user returns from
            // system settings (recomposition alone doesn't re-run these checks)
            var permissionRefresh by remember { mutableIntStateOf(0) }
            val lifecycleOwner = LocalLifecycleOwner.current
            DisposableEffect(lifecycleOwner) {
                val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
                    if (event == androidx.lifecycle.Lifecycle.Event.ON_RESUME) permissionRefresh++
                }
                lifecycleOwner.lifecycle.addObserver(observer)
                onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
            }

            // canDrawOverlays requires API 23 — on Android 5.x the permission
            // model doesn't exist, so treat it as granted
            val canDrawOverlays = remember(permissionRefresh) {
                android.os.Build.VERSION.SDK_INT < 23 || Settings.canDrawOverlays(context)
            }
            val hasLocation = remember(permissionRefresh) {
                val hasFine = androidx.core.content.ContextCompat.checkSelfPermission(
                    context, android.Manifest.permission.ACCESS_FINE_LOCATION
                ) == android.content.pm.PackageManager.PERMISSION_GRANTED
                val hasCoarse = androidx.core.content.ContextCompat.checkSelfPermission(
                    context, android.Manifest.permission.ACCESS_COARSE_LOCATION
                ) == android.content.pm.PackageManager.PERMISSION_GRANTED
                hasFine || hasCoarse
            }
            val isDefaultLauncher = remember(permissionRefresh) {
                val home = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME)
                context.packageManager.resolveActivity(
                    home, android.content.pm.PackageManager.MATCH_DEFAULT_ONLY
                )?.activityInfo?.packageName == context.packageName
            }

            val homeRoleLauncher = rememberLauncherForActivityResult(
                ActivityResultContracts.StartActivityForResult()
            ) { permissionRefresh++ }

            val locationPermissionLauncher = rememberLauncherForActivityResult(
                ActivityResultContracts.RequestMultiplePermissions()
            ) { permissionRefresh++ }

            SettingsButton(
                label    = "设为默认启动器",
                sublabel = if (isDefaultLauncher) "已启用，开放启动器是主页应用"
                           else "车机启动时进入开放启动器所需",
                icon     = Icons.Default.Home,
                accent   = if (isDefaultLauncher) accent else MaterialTheme.colorScheme.error,
                onClick  = {
                    // Preferred: the system home-role dialog (API 29+). Vendor ROMs
                    // sometimes ship without it, so fall through to the home-settings
                    // screen, then the default-apps screen.
                    var launched = false
                    if (android.os.Build.VERSION.SDK_INT >= 29) {
                        val rm = context.getSystemService(android.app.role.RoleManager::class.java)
                        if (rm != null && rm.isRoleAvailable(android.app.role.RoleManager.ROLE_HOME) &&
                            !rm.isRoleHeld(android.app.role.RoleManager.ROLE_HOME)
                        ) {
                            launched = runCatching {
                                homeRoleLauncher.launch(rm.createRequestRoleIntent(android.app.role.RoleManager.ROLE_HOME))
                            }.isSuccess
                        }
                    }
                    if (!launched) {
                        launched = runCatching {
                            context.startActivity(
                                Intent(Settings.ACTION_HOME_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            )
                        }.isSuccess
                    }
                    if (!launched) {
                        runCatching {
                            context.startActivity(
                                Intent("android.settings.MANAGE_DEFAULT_APPS_SETTINGS")
                                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            )
                        }
                    }
                }
            )
            SettingsDivider()
            SettingsButton(
                label    = "通知访问",
                sublabel = if (isMediaConnected) "已授予，媒体控制已启用" else "正在播放组件所需",
                icon     = if (isMediaConnected) Icons.Default.NotificationsActive else Icons.Default.NotificationsOff,
                accent   = if (isMediaConnected) accent else MaterialTheme.colorScheme.error,
                onClick  = {
                    val notificationSettings = Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS")
                    val intent = notificationSettings.takeIf { it.resolveActivity(context.packageManager) != null }
                        ?: Intent(Settings.ACTION_SETTINGS)
                    context.startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                }
            )
            SettingsDivider()
            SettingsButton(
                label    = "显示在其他应用上层",
                sublabel = if (canDrawOverlays) "已授予，画中画浮窗已启用" else "画中画浮窗所需",
                icon     = if (canDrawOverlays) Icons.Default.Layers else Icons.Default.LayersClear,
                accent   = if (canDrawOverlays) accent else MaterialTheme.colorScheme.error,
                onClick  = {
                    if (android.os.Build.VERSION.SDK_INT >= 23) {
                        runCatching {
                            context.startActivity(
                                Intent(
                                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                    "package:${context.packageName}".toUri()
                                ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            )
                        }
                    }
                }
            )
            SettingsDivider()
            SettingsButton(
                label    = "定位访问",
                sublabel = if (hasLocation) "已授予，GPS、指南针和天气已启用" else "指南针、速度和天气所需",
                icon     = if (hasLocation) Icons.Default.LocationOn else Icons.Default.LocationOff,
                accent   = if (hasLocation) accent else MaterialTheme.colorScheme.error,
                onClick  = {
                    if (!hasLocation) {
                        // Ask in-app first — previously the only grant path was the
                        // onboarding flow; skipping it left GPS features dead forever
                        locationPermissionLauncher.launch(arrayOf(
                            android.Manifest.permission.ACCESS_FINE_LOCATION,
                            android.Manifest.permission.ACCESS_COARSE_LOCATION
                        ))
                    } else {
                        runCatching {
                            context.startActivity(
                                Intent(
                                    Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                    "package:${context.packageName}".toUri()
                                ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            )
                        }
                    }
                }
            )
        }

        // ── Vehicle Name ─────────────────────────────────────────────────────
        SettingsSection("车辆") {
            var nameInput by remember(settings.vehicleName) { mutableStateOf(settings.vehicleName) }
            SettingsRow(
                label    = "车辆名称",
                sublabel = "",
                icon     = Icons.Default.DirectionsCar
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    OutlinedTextField(
                        value         = nameInput,
                        onValueChange = { nameInput = it },
                        placeholder   = { Text("我的爱车", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp) },
                        singleLine    = true,
                        textStyle     = LocalTextStyle.current.copy(fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface),
                        colors        = outlinedFieldColors(accent),
                        modifier      = Modifier.width(140.dp)
                    )
                    if (nameInput != settings.vehicleName) {
                        IconButton(onClick = { onUpdate { copy(vehicleName = nameInput) } }, modifier = Modifier.size(32.dp)) {
                            Icon(Icons.Default.Check, "保存", tint = accent, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }

            SettingsDivider()

            SettingsRow(
                label    = "侧边栏位置",
                sublabel = when (settings.sidebarPosition) {
                    SidebarPosition.LEFT   -> "左侧"
                    SidebarPosition.RIGHT  -> "右侧"
                    SidebarPosition.BOTTOM -> "底部"
                },
                icon     = Icons.Default.SwapHoriz
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    SidebarPosition.entries.forEach { pos ->
                        FilterChip(
                            selected = settings.sidebarPosition == pos,
                            onClick  = { onUpdate { copy(sidebarPosition = pos) } },
                            label    = {
                                Text(
                                    when (pos) {
                                        SidebarPosition.LEFT   -> "左侧"
                                        SidebarPosition.RIGHT  -> "右侧"
                                        SidebarPosition.BOTTOM -> "底部"
                                    },
                                    fontSize = 9.sp,
                                    letterSpacing = 0.5.sp
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = accent,
                                selectedLabelColor     = MaterialTheme.colorScheme.onPrimary
                            )
                        )
                    }
                }
            }

            if (settings.sidebarPosition == SidebarPosition.BOTTOM) {
                SettingsDivider()
                SettingsRow(
                    label    = "快捷方式位置",
                    sublabel = if (settings.bottomBarShortcutsRight) "右侧，导航按钮在左" else "左侧，导航按钮在右",
                    icon     = Icons.AutoMirrored.Filled.FormatAlignRight
                ) {
                    Switch(
                        checked         = settings.bottomBarShortcutsRight,
                        onCheckedChange = { onUpdate { copy(bottomBarShortcutsRight = it) } },
                        colors          = switchColors(accent)
                    )
                }
            }

            SettingsDivider()

            SettingsRow(label = "单位系统", sublabel = if (settings.unitSystem == UnitSystem.METRIC) "公制（°C、公里）" else "英制（°F、英里）", icon = Icons.Default.Straighten) {
                Row {
                    FilterChip(
                        selected = settings.unitSystem == UnitSystem.METRIC,
                        onClick  = { onUpdate { copy(unitSystem = UnitSystem.METRIC) } },
                        label    = { Text("公制", fontSize = 11.sp) },
                        colors   = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = accent,
                            selectedLabelColor     = MaterialTheme.colorScheme.onPrimary
                        )
                    )
                    Spacer(Modifier.width(6.dp))
                    FilterChip(
                        selected = settings.unitSystem == UnitSystem.IMPERIAL,
                        onClick  = { onUpdate { copy(unitSystem = UnitSystem.IMPERIAL) } },
                        label    = { Text("英制", fontSize = 11.sp) },
                        colors   = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = accent,
                            selectedLabelColor     = MaterialTheme.colorScheme.onPrimary
                        )
                    )
                }
            }
        }

        // ── Sidebar Shortcuts ─────────────────────────────────────────────────
        SettingsSection("侧边栏") {
            settings.shortcuts.forEachIndexed { index, shortcut ->
                if (index > 0) SettingsDivider()
                SettingsRow(
                    label    = "插槽 ${index + 1}",
                    sublabel = when {
                        shortcut.label.isNotEmpty()       -> shortcut.label
                        shortcut.packageName.isNotEmpty() -> shortcut.packageName
                        else                              -> "空"
                    },
                    icon     = Icons.Default.Apps
                ) {
                    if (settings.shortcuts.size > 1) {
                        IconButton(
                            onClick  = {
                                onUpdate {
                                    copy(shortcuts = shortcuts.toMutableList().also { it.removeAt(index) })
                                }
                            },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(Icons.Default.Close, "删除快捷方式", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(14.dp))
                        }
                    }
                }
            }

            SettingsDivider()

            SettingsButton(
                label    = "添加插槽",
                sublabel = "向侧边栏添加空快捷方式",
                icon     = Icons.Default.Add,
                accent   = accent,
                onClick  = { onUpdate { copy(shortcuts = shortcuts + ShortcutConfig()) } }
            )
        }

        // ── Appearance ───────────────────────────────────────────────────────
        SettingsSection("外观") {
            // Display Mode
            SettingsRow(
                label    = "显示模式",
                sublabel = when (settings.dayNightMode) {
                    DayNightMode.DARK   -> "始终深色"
                    DayNightMode.LIGHT  -> "始终浅色"
                    DayNightMode.AUTO   -> "日出 / 日落"
                    DayNightMode.SYSTEM -> "跟随系统主题"
                },
                icon = when (settings.dayNightMode) {
                    DayNightMode.DARK   -> Icons.Default.NightlightRound
                    DayNightMode.LIGHT  -> Icons.Default.LightMode
                    DayNightMode.AUTO   -> Icons.Default.Brightness4
                    DayNightMode.SYSTEM -> Icons.Default.PhoneAndroid
                }
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    DayNightMode.entries.forEach { mode ->
                        FilterChip(
                            selected = settings.dayNightMode == mode,
                            onClick  = { onUpdate { copy(dayNightMode = mode) } },
                            label    = {
                                Text(
                                    text      = when (mode) {
                                        DayNightMode.DARK   -> "深色"
                                        DayNightMode.LIGHT  -> "浅色"
                                        DayNightMode.AUTO   -> "日落"
                                        DayNightMode.SYSTEM -> "系统"
                                    },
                                    fontSize  = 9.sp,
                                    letterSpacing = 0.5.sp
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = accent,
                                selectedLabelColor     = MaterialTheme.colorScheme.onPrimary
                            )
                        )
                    }
                }
            }

            SettingsDivider()

            SettingsRow(
                label = "主题风格",
                sublabel = when (settings.dashboardStyle) {
                    DashboardStyle.OEM -> "精致 OEM"
                    DashboardStyle.CYBER -> "赛博科技"
                    DashboardStyle.GLASS -> "极简玻璃"
                },
                icon = Icons.Default.AutoAwesome
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    DashboardStyle.entries.forEach { style ->
                        FilterChip(
                            selected = settings.dashboardStyle == style,
                            onClick = { onUpdate { withThemeDefaults(isDayMode = isDayMode, style = style) } },
                            label = {
                                Text(
                                    when (style) {
                                        DashboardStyle.OEM -> "OEM"
                                        DashboardStyle.CYBER -> "赛博"
                                        DashboardStyle.GLASS -> "玻璃"
                                    },
                                    fontSize = 9.sp,
                                    letterSpacing = 0.5.sp
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = accent,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                            )
                        )
                    }
                }
            }

            SettingsDivider()

            // Accent color
            SettingsRow(
                label    = "强调色",
                sublabel = "界面高亮颜色",
                icon     = Icons.Default.Palette
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(settings.accentColor))
                        .clickable { showAccentPicker = true }
                )
            }

            SettingsDivider()

            // Background color + gradient
            SettingsRow(
                label    = "背景",
                sublabel = if (settings.useGradient) "渐变" else "纯色",
                icon     = Icons.Default.FormatColorFill
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment     = Alignment.CenterVertically
                ) {
                    // Start color swatch
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(settings.backgroundColor))
                            .clickable { showBgPicker = true }
                    )
                    if (settings.useGradient) {
                        androidx.compose.material3.Icon(
                                Icons.AutoMirrored.Filled.ArrowForward, null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(14.dp)
                        )
                        // End color swatch
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(settings.gradientEndColor))
                                .clickable { showGradientEndPicker = true }
                        )
                    }
                    if (settings.useCustomBackgroundColor) {
                        TextButton(
                            onClick = {
                                onUpdate {
                                    val defaultBackground = dashboardStyle.defaultThemeColors(
                                        Color(accentColor),
                                        isDayMode
                                    ).background
                                    copy(
                                        useCustomBackgroundColor = false,
                                        backgroundColor = defaultBackground.toArgb(),
                                        useGradient = false
                                    )
                                }
                            },
                            contentPadding = PaddingValues(horizontal = 6.dp),
                            modifier = Modifier.height(28.dp)
                        ) {
                            Text("默认", color = accent, fontSize = 9.sp, letterSpacing = 1.sp)
                        }
                    }
                }
            }

            SettingsDivider()

            SettingsRow(
                label = "快捷主题色",
                sublabel = when (settings.dashboardTheme) {
                    DashboardTheme.ICE_BLUE -> "冰蓝科技"
                    DashboardTheme.TRACK_ORANGE -> "赛道橙"
                    DashboardTheme.ALERT_RED -> "警示红"
                    DashboardTheme.AURORA_GREEN -> "极光绿"
                    DashboardTheme.NEON_PURPLE -> "紫色霓虹"
                },
                icon = Icons.Default.AutoAwesome
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    DashboardTheme.entries.forEach { theme ->
                        val color = when (theme) {
                            DashboardTheme.ICE_BLUE -> Color(0xFF61DAFB)
                            DashboardTheme.TRACK_ORANGE -> Color(0xFFFF9F43)
                            DashboardTheme.ALERT_RED -> Color(0xFFFF5C70)
                            DashboardTheme.AURORA_GREEN -> Color(0xFF46E6A5)
                            DashboardTheme.NEON_PURPLE -> Color(0xFFB58CFF)
                        }
                        Box(
                            modifier = Modifier
                                .size(if (settings.dashboardTheme == theme) 34.dp else 28.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(color)
                                .clickable {
                                onUpdate { withThemeDefaults(isDayMode = isDayMode, theme = theme) }
                                }
                        )
                    }
                }
            }

            SettingsDivider()

            // Font Color row
            SettingsRow(
                label    = "字体颜色",
                sublabel = "深色模式下的自定义文字颜色",
                icon     = Icons.Default.FormatSize
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(settings.fontColor))
                        .clickable { showFontColorPicker = true }
                )
            }

            SettingsDivider()

            SettingsRow(
                label = "卡片颜色",
                sublabel = "组件与主卡片表面",
                icon = Icons.Default.ViewAgenda
            ) {
                ThemeColorSwatch(Color(settings.surfaceColor)) { showSurfaceColorPicker = true }
            }

            SettingsDivider()

            SettingsRow(
                label = "浮层颜色",
                sublabel = "菜单、输入框与次级表面",
                icon = Icons.Default.Layers
            ) {
                ThemeColorSwatch(Color(settings.overlayColor)) { showOverlayColorPicker = true }
            }

            SettingsDivider()

            SettingsRow(
                label = "边框颜色",
                sublabel = "卡片、控件与分割线",
                icon = Icons.Default.CropSquare
            ) {
                ThemeColorSwatch(Color(settings.borderColor)) { showBorderColorPicker = true }
            }

            SettingsDivider()

            SettingsRow(
                label = "次级文字颜色",
                sublabel = "说明、标签与辅助信息",
                        icon = Icons.AutoMirrored.Filled.ShortText
            ) {
                ThemeColorSwatch(Color(settings.secondaryTextColor)) { showSecondaryTextColorPicker = true }
            }

            SettingsDivider()

            if (settings.useCustomThemeColors || settings.useCustomBackgroundColor) {
                TextButton(
                    onClick = { onUpdate { withThemeDefaults(isDayMode = isDayMode) } },
                    contentPadding = PaddingValues(horizontal = 6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("恢复主题默认颜色", color = accent, fontSize = 10.sp, letterSpacing = 0.5.sp)
                }
                SettingsDivider()
            }

            SettingsRow(label = "使用渐变", sublabel = "混合两种颜色作为背景", icon = Icons.Default.Gradient) {
                Switch(checked = settings.useGradient,
                    onCheckedChange = { onUpdate { copy(useGradient = it) } },
                    colors = switchColors(accent))
            }

            if (settings.useGradient) {
                SettingsDivider()
                SettingsRow(
                    label    = "渐变方向",
                    sublabel = when (settings.gradientDirection) {
                        GradientDirection.TOP_TO_BOTTOM -> "从上到下"
                        GradientDirection.LEFT_TO_RIGHT -> "从左到右"
                        GradientDirection.DIAGONAL      -> "对角（线性）"
                        GradientDirection.RADIAL        -> "径向（圆形）"
                    },
                    icon     = Icons.AutoMirrored.Filled.TrendingFlat
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        GradientDirection.entries.forEach { dir ->
                            FilterChip(
                                selected = settings.gradientDirection == dir,
                                onClick  = { onUpdate { copy(gradientDirection = dir) } },
                                label    = {
                                    Text(
                                        text      = when (dir) {
                                            GradientDirection.TOP_TO_BOTTOM -> "垂直"
                                            GradientDirection.LEFT_TO_RIGHT -> "水平"
                                            GradientDirection.DIAGONAL      -> "对角"
                                            GradientDirection.RADIAL        -> "径向"
                                        },
                                        fontSize  = 9.sp,
                                        letterSpacing = 0.5.sp
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = accent,
                                    selectedLabelColor     = MaterialTheme.colorScheme.onPrimary
                                )
                            )
                        }
                    }
                }
            }

            SettingsDivider()

            // Wallpaper
            SettingsButton(
                label    = "设置壁纸",
                sublabel = if (settings.wallpaperUri.isNotEmpty()) "自定义壁纸已启用" else "从图库选择图片",
                icon     = Icons.Default.Wallpaper,
                accent   = accent,
                onClick  = { wallpaperPicker.launch(arrayOf("image/*")) }
            )
            if (settings.wallpaperUri.isNotEmpty()) {
                Column {
                    SettingsRow(
                        label    = "壁纸暗度",
                        sublabel = "${"%.0f".format(settings.wallpaperDim * 100)}%",
                        icon     = Icons.Default.BrightnessLow
                    ) {}
                    Slider(
                        value         = settings.wallpaperDim,
                        onValueChange = { onUpdate { copy(wallpaperDim = it) } },
                        valueRange    = 0f..0.95f,
                        steps         = 18,
                        colors        = sliderColors(accent),
                        modifier      = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
                    )
                    Spacer(Modifier.height(4.dp))
                    TextButton(
                        onClick  = { onUpdate { copy(wallpaperUri = "") } },
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Text("移除壁纸", color = MaterialTheme.colorScheme.error, fontSize = 9.sp, letterSpacing = 1.sp)
                    }
                }
            }
        }

        // ── Typography ───────────────────────────────────────────────────────
        SettingsSection("字体") {
            SettingsRow(label = "字体", sublabel = fontDisplayName(settings.appFont), icon = Icons.Default.FontDownload) {
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    com.openlauncher.app.data.AppFont.entries.forEach { font ->
                        FilterChip(
                            selected = settings.appFont == font,
                            onClick  = { onUpdate { copy(appFont = font) } },
                            label    = { Text(fontDisplayName(font), fontSize = 9.sp, letterSpacing = 0.5.sp) },
                            colors   = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = accent,
                                selectedLabelColor     = MaterialTheme.colorScheme.onPrimary
                            )
                        )
                    }
                }
            }

            SettingsDivider()

            SettingsRow(label = "粗体", sublabel = "让所有文字更粗", icon = Icons.Default.FormatBold) {
                Switch(
                    checked         = settings.fontBold,
                    onCheckedChange = { onUpdate { copy(fontBold = it) } },
                    colors          = switchColors(accent)
                )
            }

            SettingsDivider()

            Column {
                SettingsRow(
                    label    = "文字缩放",
                    sublabel = "${"%.0f".format(settings.textScale * 100)}%",
                    icon     = Icons.Default.TextFields
                ) {}
                Slider(
                    value         = settings.textScale,
                    onValueChange = { onUpdate { copy(textScale = it) } },
                    valueRange    = 0.8f..1.4f,
                    steps         = 5,
                    colors        = sliderColors(accent),
                    modifier      = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
                )
            }

            SettingsDivider()

            Column {
                SettingsRow(
                    label    = "界面缩放",
                    sublabel = "${"%.0f".format(settings.uiScale * 100)}%  ·  缩放所有界面元素",
                    icon     = Icons.Default.ZoomIn
                ) {}
                Slider(
                    value         = settings.uiScale,
                    onValueChange = { onUpdate { copy(uiScale = it) } },
                    valueRange    = 0.7f..1.5f,
                    steps         = 7,
                    colors        = sliderColors(accent),
                    modifier      = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
                )
            }
        }

        // ── GPS & Calibration ───────────────────────────────────────────────
        SettingsSection("GPS 与校准") {
            var calibrationStatus by remember { mutableStateOf<String?>(null) }
            val coroutineScope = rememberCoroutineScope()
            var isCalibratingCompass by remember { mutableStateOf(false) }
            var compassCountdown by remember { mutableIntStateOf(0) }

            // 1. Reset A-GPS Button
            SettingsButton(
                label    = "重置 A-GPS 辅助数据",
                sublabel = calibrationStatus ?: "强制冷启动并离线下载最新卫星轨道",
                icon     = Icons.Default.MyLocation,
                accent   = accent,
                onClick  = {
                    calibrationStatus = "正在清理 A-GPS 缓存…"
                    val lm = context.getSystemService(android.content.Context.LOCATION_SERVICE) as android.location.LocationManager
                    var success = false
                    try {
                        // "delete_aiding_data" is the command AOSP's GPS provider
                        // actually recognizes (requires ACCESS_LOCATION_EXTRA_COMMANDS)
                        success = lm.sendExtraCommand(android.location.LocationManager.GPS_PROVIDER, "delete_aiding_data", android.os.Bundle())
                        lm.sendExtraCommand(android.location.LocationManager.GPS_PROVIDER, "force_xtra_injection", null)
                        lm.sendExtraCommand(android.location.LocationManager.GPS_PROVIDER, "force_time_injection", null)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }

                    calibrationStatus = if (success) {
                        "已强制冷启动，请到室外获取新的卫星定位（约 2–3 分钟）"
                    } else {
                        "此设备的 GPS 驱动不支持该操作，未清除数据"
                    }
                }
            )
            
            SettingsDivider()
            
            // 2. Drive-in-circles magnetometer sweep. Android's sensor stack
            // self-calibrates the magnetometer continuously — the circles feed it
            // diverse readings. The timer guides the sweep; it does not (and
            // cannot) apply offsets itself, so the message must not claim it did.
            SettingsButton(
                label    = "磁力计校准（停车场）",
                sublabel = if (isCalibratingCompass) {
                    "校准进行中：缓慢绕两圈 360°…剩余 ${compassCountdown} 秒"
                } else {
                    "引导校准，绕圈行驶时 Android 会自动校准指南针"
                },
                icon     = Icons.Default.Navigation,
                accent   = if (isCalibratingCompass) Color.Green else accent,
                onClick  = {
                    if (!isCalibratingCompass) {
                        isCalibratingCompass = true
                        compassCountdown = 30
                        coroutineScope.launch {
                            while (compassCountdown > 0) {
                                delay(1000)
                                compassCountdown--
                            }
                            isCalibratingCompass = false
                            calibrationStatus = "校准完成，请检查指南针组件；若航向仍有偏差，请使用下方手动偏移"
                        }
                    }
                }
            )

            SettingsDivider()

            // 4. Manual Compass Heading Offset Slider
            Column(modifier = Modifier.padding(bottom = 8.dp)) {
                SettingsRow(
                    label    = "指南针航向偏移",
                    sublabel = "手动校准：${if (settings.compassOffset >= 0) "+" else ""}${settings.compassOffset.toInt()}°  — 与车头方向对齐",
                    icon     = Icons.Default.Explore
                ) {}
                Slider(
                    value         = settings.compassOffset,
                    onValueChange = { onUpdate { copy(compassOffset = it) } },
                    valueRange    = -180f..180f,
                    steps         = 71, // 5 degree steps: 360 / 5 - 1 = 71 steps
                    colors        = sliderColors(accent),
                    modifier      = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
                )
            }
        }

        // ── Updates ──────────────────────────────────────────────────────────
        SettingsSection("更新") {
            SettingsButton(
                label    = "检查更新",
                sublabel = "在 GitHub 查看版本发布",
                icon     = Icons.Default.SystemUpdate,
                accent   = accent,
                onClick  = {
                    val intent = Intent(Intent.ACTION_VIEW, "https://github.com/ANGLE404/openlauncher/releases".toUri())
                    context.startActivity(intent)
                }
            )
        }

        // ── Maintenance ──────────────────────────────────────────────────────
        SettingsSection("维护") {
            Spacer(Modifier.height(8.dp))
            Button(
                onClick  = { showResetDialog = true },
                shape    = RoundedCornerShape(10.dp),
                colors   = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                modifier = Modifier.fillMaxWidth().height(44.dp)
            ) {
                Icon(Icons.Default.RestartAlt, null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(8.dp))
                Text("恢复默认设置", color = MaterialTheme.colorScheme.error, fontSize = 13.sp, letterSpacing = 1.sp)
            }
            Spacer(Modifier.height(8.dp))
        }

        Spacer(Modifier.height(32.dp))

        Text(
            text          = "v${BuildConfig.VERSION_NAME}  ·  ayc404 制作  ·  2026",
            color         = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.62f),
            fontSize      = 10.sp,
            letterSpacing = 1.sp,
            modifier      = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(bottom = 16.dp)
        )
    }
    } // end Box

    // ── Dialogs ──────────────────────────────────────────────────────────────
    if (showResetDialog) {
        ConfirmDialog(
            title        = "重置设置",
            message      = "确定要将所有设置恢复默认吗？此操作无法撤销。",
            confirmLabel = "确认重置",
            onConfirm    = { onReset(); showResetDialog = false },
            onDismiss    = { showResetDialog = false }
        )
    }

    if (showAccentPicker) {
        ColorPickerDialog(
            title           = "强调色",
            initialColor    = Color(settings.accentColor),
            onColorSelected = { c -> onUpdate { withThemeOverrides(isDayMode).copy(accentColor = c.toArgb()) } },
            onDismiss       = { showAccentPicker = false }
        )
    }

    if (showBgPicker) {
        ColorPickerDialog(
            title           = "背景颜色",
            initialColor    = Color(settings.backgroundColor),
            onColorSelected = { c -> 
                onUpdate { 
                    withThemeOverrides(isDayMode).copy(
                        backgroundColor = c.toArgb(),
                        useCustomBackgroundColor = true
                    ) 
                } 
            },
            onDismiss       = { showBgPicker = false }
        )
    }

    if (showGradientEndPicker) {
        ColorPickerDialog(
            title           = "渐变结束颜色",
            initialColor    = Color(settings.gradientEndColor),
            onColorSelected = { c -> 
                onUpdate { 
                    withThemeOverrides(isDayMode).copy(
                        gradientEndColor = c.toArgb(),
                        useCustomBackgroundColor = true
                    ) 
                } 
            },
            onDismiss       = { showGradientEndPicker = false }
        )
    }

    if (showFontColorPicker) {
        ColorPickerDialog(
            title           = "字体颜色",
            initialColor    = Color(settings.fontColor),
            onColorSelected = { c -> onUpdate { withThemeOverrides(isDayMode).copy(fontColor = c.toArgb()) } },
            onDismiss       = { showFontColorPicker = false }
        )
    }

    if (showSurfaceColorPicker) {
        ColorPickerDialog(
            title = "卡片颜色",
            initialColor = Color(settings.surfaceColor),
            onColorSelected = { c -> onUpdate { withThemeOverrides(isDayMode).copy(surfaceColor = c.toArgb()) } },
            onDismiss = { showSurfaceColorPicker = false }
        )
    }

    if (showOverlayColorPicker) {
        ColorPickerDialog(
            title = "浮层颜色",
            initialColor = Color(settings.overlayColor),
            onColorSelected = { c -> onUpdate { withThemeOverrides(isDayMode).copy(overlayColor = c.toArgb()) } },
            onDismiss = { showOverlayColorPicker = false }
        )
    }

    if (showBorderColorPicker) {
        ColorPickerDialog(
            title = "边框颜色",
            initialColor = Color(settings.borderColor),
            onColorSelected = { c -> onUpdate { withThemeOverrides(isDayMode).copy(borderColor = c.toArgb()) } },
            onDismiss = { showBorderColorPicker = false }
        )
    }

    if (showSecondaryTextColorPicker) {
        ColorPickerDialog(
            title = "次级文字颜色",
            initialColor = Color(settings.secondaryTextColor),
            onColorSelected = { c -> onUpdate { withThemeOverrides(isDayMode).copy(secondaryTextColor = c.toArgb()) } },
            onDismiss = { showSecondaryTextColorPicker = false }
        )
    }
}

// ── Helpers ─────────────────────────────────────────────────────────────────

@Composable
private fun ThemeColorSwatch(color: Color, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(32.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(color)
            .clickable(onClick = onClick)
    )
}

private fun AppSettings.withThemeDefaults(
    isDayMode: Boolean,
    style: DashboardStyle = dashboardStyle,
    theme: DashboardTheme = dashboardTheme
): AppSettings {
    val colors = style.defaultThemeColors(theme.accent(), isDayMode)
    return copy(
        dashboardStyle = style,
        dashboardTheme = theme,
        accentColor = colors.accent.toArgb(),
        backgroundColor = colors.background.toArgb(),
        fontColor = colors.primaryText.toArgb(),
        surfaceColor = colors.surface.toArgb(),
        overlayColor = colors.elevatedSurface.toArgb(),
        borderColor = colors.border.toArgb(),
        secondaryTextColor = colors.secondaryText.toArgb(),
        useCustomThemeColors = false,
        useCustomBackgroundColor = false,
        useGradient = false
    )
}

private fun AppSettings.withThemeOverrides(isDayMode: Boolean): AppSettings {
    if (useCustomThemeColors) return this

    val colors = dashboardStyle.defaultThemeColors(Color(accentColor), isDayMode)
    return copy(
        accentColor = colors.accent.toArgb(),
        backgroundColor = if (useCustomBackgroundColor) backgroundColor else colors.background.toArgb(),
        fontColor = colors.primaryText.toArgb(),
        surfaceColor = colors.surface.toArgb(),
        overlayColor = colors.elevatedSurface.toArgb(),
        borderColor = colors.border.toArgb(),
        secondaryTextColor = colors.secondaryText.toArgb(),
        useCustomThemeColors = true
    )
}

@Composable
private fun SettingsSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    val sectionColor = MaterialTheme.colorScheme.onSurfaceVariant
    val dividerColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.55f)
    Column(verticalArrangement = Arrangement.spacedBy(0.dp)) {
        Text(
            text          = title.uppercase(),
            style         = MaterialTheme.typography.labelSmall,
            color         = sectionColor,
            letterSpacing = 2.sp,
            modifier      = Modifier.padding(top = 16.dp, bottom = 6.dp)
        )
        HorizontalDivider(color = dividerColor)
        Column(modifier = Modifier.fillMaxWidth(), content = content)
        HorizontalDivider(color = dividerColor)
    }
}

@Composable
private fun SettingsRow(
    label: String,
    sublabel: String = "",
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    content: @Composable RowScope.() -> Unit
) {
    val labelColor = MaterialTheme.colorScheme.onSurface
    val subColor = MaterialTheme.colorScheme.onSurfaceVariant
    val iconTint = MaterialTheme.colorScheme.primary.copy(alpha = 0.72f)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 0.dp, vertical = 11.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, tint = iconTint, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(label, style = MaterialTheme.typography.bodyMedium, color = labelColor, fontSize = 13.sp)
            if (sublabel.isNotEmpty())
                Text(sublabel, style = MaterialTheme.typography.labelSmall, color = subColor, fontSize = 11.sp)
        }
        content()
    }
}

@Composable
private fun ColumnScope.SettingsButton(
    label: String,
    sublabel: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    accent: Color,
    onClick: () -> Unit
) {
    val labelColor = MaterialTheme.colorScheme.onSurface
    val subColor = MaterialTheme.colorScheme.onSurfaceVariant
    val chevronC = MaterialTheme.colorScheme.outline
    val iconTint = MaterialTheme.colorScheme.primary.copy(alpha = 0.72f)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 0.dp, vertical = 11.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, tint = iconTint, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(label, style = MaterialTheme.typography.bodyMedium, color = labelColor, fontSize = 13.sp)
            if (sublabel.isNotEmpty())
                Text(sublabel, style = MaterialTheme.typography.labelSmall, color = subColor, fontSize = 11.sp)
        }
        Icon(Icons.Default.ChevronRight, null, tint = chevronC, modifier = Modifier.size(16.dp))
    }
}

@Composable
private fun ColumnScope.SettingsDivider() {
    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
}

@Composable
private fun outlinedFieldColors(accent: Color): androidx.compose.material3.TextFieldColors {
    val textColor = MaterialTheme.colorScheme.onSurface
    val borderU = MaterialTheme.colorScheme.outline
    return OutlinedTextFieldDefaults.colors(
        focusedBorderColor   = accent,
        unfocusedBorderColor = borderU,
        focusedTextColor     = textColor,
        unfocusedTextColor   = textColor,
        cursorColor          = accent,
        focusedLabelColor    = accent,
        unfocusedLabelColor  = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

@Composable
private fun switchColors(accent: Color): androidx.compose.material3.SwitchColors {
    return SwitchDefaults.colors(
        checkedThumbColor    = MaterialTheme.colorScheme.onPrimary,
        checkedTrackColor    = accent,
        uncheckedThumbColor  = MaterialTheme.colorScheme.onSurfaceVariant,
        uncheckedTrackColor  = MaterialTheme.colorScheme.surfaceVariant,
        uncheckedBorderColor = MaterialTheme.colorScheme.outline
    )
}

@Composable
private fun sliderColors(accent: Color): androidx.compose.material3.SliderColors {
    return SliderDefaults.colors(
        thumbColor         = accent,
        activeTrackColor   = accent,
        inactiveTrackColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.55f)
    )
}


private fun fontDisplayName(font: AppFont): String = when (font) {
    AppFont.SYSTEM          -> "系统字体"
    AppFont.NOTO_SANS_SC    -> "Noto Sans SC"
    AppFont.JETBRAINS_MONO  -> "JetBrains Mono"
    AppFont.SOURCE_CODE_PRO -> "Source Code Pro"
}
