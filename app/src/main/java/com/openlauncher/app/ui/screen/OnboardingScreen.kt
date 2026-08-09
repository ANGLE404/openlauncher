package com.openlauncher.app.ui.screen

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.openlauncher.app.BuildConfig
import com.openlauncher.app.data.AppSettings
import com.openlauncher.app.service.MediaListenerService

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun OnboardingScreen(
    accent: Color,
    onComplete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var currentStep by rememberSaveable { mutableStateOf(0) }
    var locationGranted by remember { mutableStateOf(false) }
    var mediaGranted by remember { mutableStateOf(false) }

    val checkPermissions = {
        locationGranted = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        mediaGranted = MediaListenerService.hasNotificationAccess(context)
        if (mediaGranted) MediaListenerService.requestRefresh(context)
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                checkPermissions()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    // Trigger initial check
    LaunchedEffect(Unit) {
        checkPermissions()
    }

    val locationLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        val granted = results[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                results[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        locationGranted = granted
        if (granted) {
            currentStep = 2 // Auto-advance to next step
        }
    }

    val colorScheme = MaterialTheme.colorScheme

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(colorScheme.background)
    ) {
        Row(
            modifier = Modifier.fillMaxSize()
        ) {
            // ── Left branding pane ──────────────────────────────────────────
            Column(
                modifier = Modifier
                    .weight(0.4f)
                    .fillMaxHeight()
                    .background(colorScheme.surface)
                    .padding(32.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(
                        imageVector = Icons.Default.DirectionsCar,
                        contentDescription = null,
                        tint = accent,
                        modifier = Modifier.size(44.dp)
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "开放启动器",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = colorScheme.onSurface,
                        letterSpacing = 2.sp,
                        fontSize = 15.sp
                    )
                    Text(
                        text = "为车载仪表盘而设计",
                        color = colorScheme.onSurfaceVariant,
                        fontSize = 11.sp,
                        letterSpacing = 0.5.sp
                    )
                }

                // Step indicator list
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    StepItem(0, "介绍", currentStep)
                    StepItem(1, "定位服务", currentStep)
                    StepItem(2, "媒体集成", currentStep)
                    StepItem(3, "准备出发", currentStep)
                }

                Text(
                    text = "v${BuildConfig.VERSION_NAME}",
                    color = colorScheme.outline,
                    fontSize = 9.sp,
                    letterSpacing = 1.sp
                )
            }

            // Vertical separator line
            Box(
                modifier = Modifier
                    .width(1.dp)
                    .fillMaxHeight()
                    .background(colorScheme.outline.copy(alpha = 0.7f))
            )

            // ── Right content wizard ────────────────────────────────────────
            Column(
                modifier = Modifier
                    .weight(0.6f)
                    .fillMaxHeight()
                    .padding(48.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Active Step Content
                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.CenterStart
                ) {
                    AnimatedContent(
                        targetState = currentStep,
                        transitionSpec = {
                            fadeIn() + slideInHorizontally { it / 5 } togetherWith
                            fadeOut() + slideOutHorizontally { -it / 5 }
                        },
                        label = "step_transition"
                    ) { step ->
                        when (step) {
                            0 -> IntroStep(accent)
                            1 -> LocationStep(accent, locationGranted, onGrant = {
                                locationLauncher.launch(
                                    arrayOf(
                                        Manifest.permission.ACCESS_FINE_LOCATION,
                                        Manifest.permission.ACCESS_COARSE_LOCATION
                                    )
                                )
                            })
                            2 -> MediaStep(accent, mediaGranted, onGrant = {
                                runCatching {
                                    val notificationSettings = Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS")
                                    context.startActivity(
                                        notificationSettings.takeIf { it.resolveActivity(context.packageManager) != null }
                                            ?: Intent(Settings.ACTION_SETTINGS)
                                    )
                                }
                            })
                            3 -> FinalStep(accent, onSetDefault = {
                                runCatching {
                                    context.startActivity(Intent(Settings.ACTION_HOME_SETTINGS))
                                }
                            })
                        }
                    }
                }

                Spacer(Modifier.height(24.dp))

                // UNIFIED WIZARD FOOTER
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Back button (left aligned)
                    if (currentStep > 0) {
                        TextButton(
                            onClick = { currentStep-- },
                            shape = MaterialTheme.shapes.small,
                            modifier = Modifier.height(44.dp)
                        ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("返回", color = colorScheme.onSurfaceVariant, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                        }
                    } else {
                        Spacer(Modifier.width(1.dp))
                    }

                    // Next / Finish button (right aligned)
                    val isPrimary = when (currentStep) {
                        0 -> true
                        1 -> locationGranted
                        2 -> mediaGranted
                        3 -> true
                        else -> true
                    }

                    val nextButtonLabel = when (currentStep) {
                        0 -> "开始使用"
                        1 -> if (locationGranted) "继续" else "暂时跳过"
                        2 -> if (mediaGranted) "继续" else "暂时跳过"
                        3 -> "完成设置"
                        else -> "继续"
                    }

                    val nextButtonIcon = if (currentStep == 3) Icons.Default.Check else Icons.AutoMirrored.Filled.ArrowForward

                    if (isPrimary) {
                        Button(
                            onClick = {
                                if (currentStep < 3) {
                                    currentStep++
                                } else {
                                    onComplete()
                                }
                            },
                            shape = MaterialTheme.shapes.small,
                            colors = ButtonDefaults.buttonColors(containerColor = accent, contentColor = colorScheme.onPrimary),
                            modifier = Modifier.height(44.dp)
                        ) {
                            Text(nextButtonLabel, color = colorScheme.onPrimary, fontWeight = FontWeight.Bold, letterSpacing = 1.sp, fontSize = 12.sp)
                            Spacer(Modifier.width(8.dp))
                            Icon(nextButtonIcon, null, tint = colorScheme.onPrimary, modifier = Modifier.size(16.dp))
                        }
                    } else {
                        OutlinedButton(
                            onClick = {
                                currentStep++
                            },
                            shape = MaterialTheme.shapes.small,
                            border = androidx.compose.foundation.BorderStroke(1.dp, accent.copy(alpha = 0.5f)),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = colorScheme.onSurface),
                            modifier = Modifier.height(44.dp)
                        ) {
                            Text(nextButtonLabel, color = colorScheme.onSurface, fontWeight = FontWeight.Bold, letterSpacing = 1.sp, fontSize = 12.sp)
                            Spacer(Modifier.width(8.dp))
                            Icon(nextButtonIcon, null, tint = colorScheme.onSurface, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StepItem(stepIndex: Int, title: String, currentStep: Int) {
    val active = stepIndex == currentStep
    val completed = stepIndex < currentStep
    val tint = when {
        active -> MaterialTheme.colorScheme.primary
        completed -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.outline
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(16.dp)
                .clip(MaterialTheme.shapes.extraSmall)
                .background(tint.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(MaterialTheme.shapes.extraSmall)
                    .background(tint)
            )
        }
        Text(
            text = title,
            fontSize = 11.sp,
            color = if (active) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = if (active) FontWeight.Bold else FontWeight.Normal,
            letterSpacing = 0.5.sp
        )
    }
}

@Composable
private fun IntroStep(accent: Color) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(
            text = "欢迎使用开放启动器",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = accent,
            letterSpacing = 2.sp,
            fontSize = 20.sp
        )
        Text(
            text = "简洁现代的横屏仪表盘，为你的车机屏幕打造。",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 13.sp,
            lineHeight = 20.sp
        )

        Spacer(Modifier.height(8.dp))

        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            BulletItem(Icons.Default.CloudOff, "完全离线运行", "无需移动信号或网络连接。速度、指南针和高度计均可离线工作。")
            BulletItem(Icons.Default.Palette, "高度可定制的仪表盘", "自定义强调色、背景渐变、字体、单位，并拖动排列卡片。")
            BulletItem(Icons.AutoMirrored.Filled.VolumeUp, "音效板与媒体快捷方式", "触发自定义音效，管理 CarPlay、Android Auto 快捷方式并控制媒体播放器。")
        }
    }
}

@Composable
private fun LocationStep(accent: Color, isGranted: Boolean, onGrant: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(
            text = "遥测与天气",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = accent,
            letterSpacing = 2.sp,
            fontSize = 20.sp
        )
        Text(
            text = "为计算实时速度、航向和高度并更新天气，开放启动器需要高精度 GPS。",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 13.sp,
            lineHeight = 20.sp
        )

        Spacer(Modifier.height(16.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(MaterialTheme.shapes.small)
                .background(if (isGranted) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f) else MaterialTheme.colorScheme.error.copy(alpha = 0.12f))
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = if (isGranted) Icons.Default.CheckCircle else Icons.Default.Cancel,
                    contentDescription = null,
                    tint = if (isGranted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(24.dp)
                )
                Column {
                    Text(
                        text = if (isGranted) "权限已授予" else "需要权限",
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = if (isGranted) "GPS 遥测已启用" else "遥测当前已禁用",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 11.sp
                    )
                }
            }
        }

        if (!isGranted) {
            Spacer(Modifier.height(16.dp))
            Button(
                onClick = onGrant,
                shape = MaterialTheme.shapes.small,
                colors = ButtonDefaults.buttonColors(containerColor = accent, contentColor = MaterialTheme.colorScheme.onPrimary),
                modifier = Modifier.height(44.dp)
            ) {
                Icon(Icons.Default.LocationOn, null, tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(8.dp))
                Text("授予权限", color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Bold, letterSpacing = 1.sp, fontSize = 12.sp)
            }
        }
    }
}

@Composable
private fun MediaStep(accent: Color, isGranted: Boolean, onGrant: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(
            text = "媒体集成",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = accent,
            letterSpacing = 2.sp,
            fontSize = 20.sp
        )
        Text(
            text = "为显示专辑封面、曲目信息、进度并提供播放控制，开放启动器会监听媒体通知。",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 13.sp,
            lineHeight = 20.sp
        )

        Spacer(Modifier.height(16.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(MaterialTheme.shapes.small)
                .background(if (isGranted) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f) else MaterialTheme.colorScheme.error.copy(alpha = 0.12f))
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = if (isGranted) Icons.Default.CheckCircle else Icons.Default.Cancel,
                    contentDescription = null,
                    tint = if (isGranted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(24.dp)
                )
                Column {
                    Text(
                        text = if (isGranted) "通知访问已授予" else "需要通知访问",
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = if (isGranted) "音乐播放器组件已连接" else "正在播放组件将保持未激活",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 11.sp
                    )
                }
            }
        }

        if (!isGranted) {
            Spacer(Modifier.height(16.dp))
            Button(
                onClick = onGrant,
                shape = MaterialTheme.shapes.small,
                colors = ButtonDefaults.buttonColors(containerColor = accent, contentColor = MaterialTheme.colorScheme.onPrimary),
                modifier = Modifier.height(44.dp)
            ) {
                Icon(Icons.AutoMirrored.Filled.VolumeUp, null, tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(8.dp))
                Text("启用媒体监听", color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Bold, letterSpacing = 1.sp, fontSize = 12.sp)
            }
        }
    }
}

@Composable
private fun FinalStep(accent: Color, onSetDefault: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(
            text = "准备上路！",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = accent,
            letterSpacing = 2.sp,
            fontSize = 20.sp
        )
        Text(
            text = "设置完成。可将开放启动器设为默认主页，车辆启动时自动打开。",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 13.sp,
            lineHeight = 20.sp
        )

        Spacer(Modifier.height(20.dp))

        Button(
            onClick = onSetDefault,
            shape = MaterialTheme.shapes.small,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                contentColor = MaterialTheme.colorScheme.onSurface
            ),
            modifier = Modifier.height(44.dp)
        ) {
            Icon(Icons.Default.Home, null, tint = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(8.dp))
            Text("设为默认", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold, letterSpacing = 1.sp, fontSize = 12.sp)
        }
    }
}

@Composable
private fun BulletItem(icon: ImageVector, title: String, desc: String) {
    Row(
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(icon, null, tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f), modifier = Modifier.size(18.dp).padding(top = 2.dp))
        Column {
            Text(title, color = MaterialTheme.colorScheme.onSurface, fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp)
            Text(desc, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp, lineHeight = 16.sp)
        }
    }
}
