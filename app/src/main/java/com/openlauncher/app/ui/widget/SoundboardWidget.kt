package com.openlauncher.app.ui.widget

import android.content.Intent
import android.media.MediaPlayer
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AudioFile
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.net.toUri
import com.openlauncher.app.R
import com.openlauncher.app.data.SoundPadConfig

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SoundboardWidget(
    pads: List<SoundPadConfig>,
    accent: Color,
    isDayMode: Boolean = false,
    isEditing: Boolean = false,
    onUpdatePad: (index: Int, pad: SoundPadConfig) -> Unit,
    modifier: Modifier = Modifier
) {
    val context      = LocalContext.current
    val contentColor = MaterialTheme.colorScheme.onSurface
    val dimColor = MaterialTheme.colorScheme.onSurfaceVariant
    val borderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.75f)

    var activePadIndex by remember { mutableStateOf<Int?>(null) }
    var assigningIndex by remember { mutableStateOf<Int?>(null) }

    val safePads = remember(pads) {
        if (pads.size >= 6) pads.take(6)
        else pads + List(6 - pads.size) { SoundPadConfig("+", synthType = "") }
    }

    // Outer grid Column with top padding = 22.dp to leave room for card header label "SOUNDBOARD"
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(start = 10.dp, end = 10.dp, top = 22.dp, bottom = 8.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        repeat(2) { row ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                repeat(3) { col ->
                    val idx = row * 3 + col
                    val pad = safePads[idx]
                    val isActive = activePadIndex == idx
                    val hasCustomAudio = pad.audioUri.isNotEmpty()

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .border(
                                1.dp,
                                if (isActive) accent else borderColor,
                                MaterialTheme.shapes.extraSmall
                            )
                            .clip(MaterialTheme.shapes.extraSmall)
                            .background(if (isActive) accent.copy(alpha = 0.12f) else Color.Transparent)
                            .then(
                                if (!isEditing) Modifier.combinedClickable(
                                    onClick = {
                                        if (pad.label == "+" || (pad.audioUri.isEmpty() && pad.synthType.isEmpty())) {
                                            assigningIndex = idx
                                        } else {
                                            activePadIndex = idx
                                            playSoundPad(
                                                context = context,
                                                pad = pad,
                                                onDone = { activePadIndex = null }
                                            )
                                        }
                                    },
                                    onLongClick = { assigningIndex = idx }
                                ) else Modifier
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        val isPlus = pad.label == "+"
                        Text(
                            text = pad.label,
                            color = if (isActive) accent else if (isPlus) dimColor else contentColor,
                            fontSize = if (isPlus) 16.sp else 9.sp,
                            fontFamily = com.openlauncher.app.ui.theme.PixelNumeric,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }

    assigningIndex?.let { idx ->
        PadAssignDialog(
            pad = safePads[idx],
            accent = accent,
            isDayMode = isDayMode,
            onDismiss = { assigningIndex = null },
            onSave = { updated ->
                onUpdatePad(idx, updated)
                assigningIndex = null
            }
        )
    }
}

@Composable
private fun PadAssignDialog(
    pad: SoundPadConfig,
    accent: Color,
    isDayMode: Boolean,
    onDismiss: () -> Unit,
    onSave: (SoundPadConfig) -> Unit
) {
    val context    = LocalContext.current
    val menuBg = MaterialTheme.colorScheme.surfaceVariant
    val menuBorder = MaterialTheme.colorScheme.outline
    val contentColor = MaterialTheme.colorScheme.onSurface
    val dimColor = MaterialTheme.colorScheme.onSurfaceVariant
    val fieldBorder = MaterialTheme.colorScheme.outline.copy(alpha = 0.75f)

    var labelText   by remember { mutableStateOf(pad.label) }
    var synthType   by remember { mutableStateOf(pad.synthType) }
    var audioUri    by remember { mutableStateOf(pad.audioUri) }

    val filePicker = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            runCatching {
                context.contentResolver.takePersistableUriPermission(
                    uri, Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            }
            audioUri = uri.toString()
            val rawName = uri.path?.substringAfterLast('/') ?: "自定义音效"
            labelText = rawName.substringAfterLast(':').substringBeforeLast('.')
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .clip(MaterialTheme.shapes.medium)
                .background(menuBg)
                .border(1.dp, menuBorder, MaterialTheme.shapes.medium)
                .padding(18.dp)
                .width(340.dp), // Fixed size: increased from 220dp to 340dp for landscape headunit displays
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                "指定音效",
                color = contentColor,
                fontSize = 10.sp,
                fontFamily = com.openlauncher.app.ui.theme.PixelNumeric,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp
            )

            // Label field
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("音效名称", color = dimColor, fontSize = 7.sp, fontFamily = com.openlauncher.app.ui.theme.PixelNumeric, letterSpacing = 1.5.sp)
                BasicTextField(
                    value = labelText,
                    onValueChange = { if (it.length <= 12) labelText = it },
                    singleLine = true,
                    textStyle = TextStyle(
                        color = contentColor,
                        fontSize = 11.sp,
                        fontFamily = com.openlauncher.app.ui.theme.PixelNumeric
                    ),
                    cursorBrush = SolidColor(accent),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, fieldBorder, MaterialTheme.shapes.extraSmall)
                        .padding(horizontal = 8.dp, vertical = 6.dp)
                )
            }

            // Preloaded Audio Selector (replaces old raw waveform synth generation)
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("预置音频", color = dimColor, fontSize = 7.sp, fontFamily = com.openlauncher.app.ui.theme.PixelNumeric, letterSpacing = 1.5.sp)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val preloadedSounds = listOf(
                        "mario_jump" to "跳跃",
                        "mario_coin" to "金币",
                        "boom" to "爆炸",
                        "loud_fart" to "搞笑"
                    )
                    preloadedSounds.forEach { (type, chipLabel) ->
                        val active = synthType == type && audioUri.isEmpty()
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(28.dp)
                                .border(1.dp, if (active) accent else fieldBorder, MaterialTheme.shapes.extraSmall)
                                .clip(MaterialTheme.shapes.extraSmall)
                                .background(if (active) accent.copy(alpha = 0.12f) else Color.Transparent)
                                .clickable { 
                                    synthType = type
                                    audioUri = ""
                                    labelText = chipLabel
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                chipLabel,
                                color = if (active) accent else dimColor,
                                fontSize = 8.sp,
                                fontFamily = com.openlauncher.app.ui.theme.PixelNumeric,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // Custom audio file picker
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("自定义音频文件", color = dimColor, fontSize = 7.sp, fontFamily = com.openlauncher.app.ui.theme.PixelNumeric, letterSpacing = 1.5.sp)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = { filePicker.launch(arrayOf("audio/*")) },
                        modifier = Modifier.weight(1f).height(30.dp),
                        shape = MaterialTheme.shapes.extraSmall,
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = accent),
                        border = androidx.compose.foundation.BorderStroke(1.dp, if (audioUri.isNotEmpty()) accent else fieldBorder),
                        contentPadding = PaddingValues(horizontal = 8.dp)
                    ) {
                        Icon(Icons.Default.AudioFile, null, tint = accent, modifier = Modifier.size(12.dp))
                        Spacer(Modifier.width(4.dp))
                        Text(
                            if (audioUri.isNotEmpty()) "已指定自定义文件" else "选择音频文件",
                            color = accent,
                            fontSize = 7.sp,
                            fontFamily = com.openlauncher.app.ui.theme.PixelNumeric,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    if (audioUri.isNotEmpty()) {
                        IconButton(
                            onClick = { audioUri = "" },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(Icons.Default.Clear, "清除已选音频", tint = dimColor, modifier = Modifier.size(14.dp))
                        }
                    }
                }
                if (audioUri.isNotEmpty()) {
                    Text(
                        audioUri.substringAfterLast('/').take(36),
                        color = dimColor.copy(alpha = 0.6f),
                        fontSize = 6.5.sp,
                        fontFamily = com.openlauncher.app.ui.theme.PixelNumeric,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            if (pad.label != "+" || pad.audioUri.isNotEmpty() || pad.synthType.isNotEmpty()) {
                OutlinedButton(
                    onClick = {
                        onSave(SoundPadConfig(
                            label     = "+",
                            audioUri  = "",
                            synthType = ""
                        ))
                    },
                    modifier = Modifier.fillMaxWidth().height(32.dp),
                    shape = MaterialTheme.shapes.extraSmall,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.error),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text("清除音效", color = MaterialTheme.colorScheme.error, fontSize = 7.5.sp, fontFamily = com.openlauncher.app.ui.theme.PixelNumeric, fontWeight = FontWeight.Bold)
                }
            }

            // Save / Cancel row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f).height(32.dp),
                    shape = MaterialTheme.shapes.extraSmall,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = dimColor),
                    border = androidx.compose.foundation.BorderStroke(1.dp, fieldBorder),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text("取消", color = dimColor, fontSize = 7.5.sp, fontFamily = com.openlauncher.app.ui.theme.PixelNumeric, fontWeight = FontWeight.Bold)
                }
                Button(
                    onClick = {
                        onSave(SoundPadConfig(
                            label     = labelText.trim().ifEmpty { pad.label },
                            audioUri  = audioUri,
                            synthType = synthType
                        ))
                    },
                    modifier = Modifier.weight(1f).height(32.dp),
                    shape = MaterialTheme.shapes.extraSmall,
                    colors = ButtonDefaults.buttonColors(containerColor = accent),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text("保存音效", color = MaterialTheme.colorScheme.onPrimary, fontSize = 7.5.sp, fontFamily = com.openlauncher.app.ui.theme.PixelNumeric, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

private fun playSoundPad(context: android.content.Context, pad: SoundPadConfig, onDone: () -> Unit) {
    val mainHandler = android.os.Handler(android.os.Looper.getMainLooper())
    mainHandler.post {
        try {
            if (pad.audioUri.isNotEmpty()) {
                val player = MediaPlayer()
                try {
                    player.setDataSource(context, pad.audioUri.toUri())
                    player.setOnCompletionListener { mp ->
                        mp.release()
                        onDone()
                    }
                    // Async playback errors (revoked SAF grant, deleted file) must
                    // also release the player and un-highlight the pad
                    player.setOnErrorListener { mp, _, _ ->
                        mp.release()
                        onDone()
                        true
                    }
                    // prepareAsync: a blocking prepare() on the main thread is an
                    // ANR risk for content URIs on slow head-unit storage
                    player.setOnPreparedListener { it.start() }
                    player.prepareAsync()
                } catch (e: Exception) {
                    // Release on synchronous failure — this leaked a native player per failed tap
                    runCatching { player.release() }
                    throw e
                }
            } else {
                val player = MediaPlayer.create(context, soundResourceId(pad.synthType))
                if (player != null) {
                    player.setOnCompletionListener { mp ->
                        mp.release()
                        onDone()
                    }
                    player.setOnErrorListener { mp, _, _ ->
                        mp.release()
                        onDone()
                        true
                    }
                    player.start()
                } else {
                    onDone()
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("Soundboard", "播放音效失败", e)
            onDone()
        }
    }
}

private fun soundResourceId(synthType: String): Int = when (synthType.lowercase().trim()) {
    "mario_coin", "kick", "snare", "bass" -> R.raw.mario_coin
    "boom" -> R.raw.boom
    "loud_fart" -> R.raw.loud_fart
    else -> R.raw.mario_jump
}
