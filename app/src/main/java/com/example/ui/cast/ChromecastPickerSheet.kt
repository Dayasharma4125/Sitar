package com.example.ui.cast

import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cast
import androidx.compose.material.icons.filled.CastConnected
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Router
import androidx.compose.material.icons.filled.Speaker
import androidx.compose.material.icons.filled.SpeakerGroup
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material.icons.filled.VolumeDown
import androidx.compose.material.icons.filled.VolumeMute
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cast.CastDevice
import com.example.cast.CastDeviceType
import com.example.cast.CastState
import com.example.cast.CastStreamQuality
import com.example.cast.ChromecastUiState
import com.example.model.Track
import com.example.ui.theme.ExpressiveShapes
import com.example.ui.theme.springPressPhysics

/**
 * Chromecast & Wireless Audio Output Bottom Sheet.
 * Allows users to discover, connect to, and control playback on Google Home/Nest speakers,
 * Chromecast Audio, Android TV, and local Wi-Fi casting devices.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChromecastPickerSheet(
    uiState: ChromecastUiState,
    currentTrack: Track,
    onDismiss: () -> Unit,
    onStartScan: () -> Unit,
    onConnectDevice: (CastDevice) -> Unit,
    onDisconnect: () -> Unit,
    onVolumeChange: (Float) -> Unit,
    onQualityChange: (CastStreamQuality) -> Unit,
    isAmoledBlack: Boolean = false
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        onStartScan()
    }

    val primaryColor = MaterialTheme.colorScheme.primary
    val backgroundColor = if (isAmoledBlack) Color(0xFF0C0F14) else MaterialTheme.colorScheme.surfaceContainerHigh
    val cardBackground = if (isAmoledBlack) Color(0xFF141922) else MaterialTheme.colorScheme.surfaceContainer
    val cardBorder = BorderStroke(
        1.dp,
        if (isAmoledBlack) Color(0xFF262E3B) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
    )
    val textColor = MaterialTheme.colorScheme.onSurface
    val textMuted = MaterialTheme.colorScheme.onSurfaceVariant

    val isScanning = uiState.state == CastState.DISCOVERING
    val isConnected = uiState.activeDevice != null && uiState.state == CastState.CONNECTED

    // Infinite rotation for scan icon
    val infiniteTransition = rememberInfiniteTransition(label = "castScannerTransition")
    val scanRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "scanRotation"
    )

    // Pulse animation for radar
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.92f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = backgroundColor,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp)
        ) {
            // Header Row: Title, Scan Button, Close
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(if (isConnected) primaryColor.copy(alpha = 0.18f) else MaterialTheme.colorScheme.surfaceContainerHighest)
                            .border(1.dp, if (isConnected) primaryColor.copy(alpha = 0.45f) else Color.Transparent, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isConnected) Icons.Default.CastConnected else Icons.Default.Cast,
                            contentDescription = "Chromecast",
                            tint = if (isConnected) primaryColor else textColor,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Chromecast & Wireless Audio",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = textColor
                            )
                        }
                        Text(
                            text = if (isScanning) "Searching for local Google Cast devices..." else "Stream bit-perfect audio over Wi-Fi",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (isScanning) primaryColor else textMuted,
                            fontSize = 12.sp
                        )
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    IconButton(
                        onClick = onStartScan,
                        modifier = Modifier.size(38.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Rescan",
                            tint = if (isScanning) primaryColor else textMuted,
                            modifier = Modifier
                                .size(20.dp)
                                .rotate(if (isScanning) scanRotation else 0f)
                        )
                    }
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(38.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = textMuted,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            // 1. ACTIVE CAST SESSION CARD (When connected)
            if (uiState.activeDevice != null) {
                val active = uiState.activeDevice
                Surface(
                    shape = ExpressiveShapes.Medium,
                    color = primaryColor.copy(alpha = 0.12f),
                    border = BorderStroke(1.dp, primaryColor.copy(alpha = 0.5f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 18.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(42.dp)
                                        .clip(ExpressiveShapes.Small)
                                        .background(primaryColor),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = getDeviceIcon(active.type),
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onPrimary,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = active.name,
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                        color = textColor,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = "Casting: ${currentTrack.title.ifBlank { "Live Stream" }} • ${active.ipAddress}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = primaryColor,
                                        fontSize = 12.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }

                            Button(
                                onClick = onDisconnect,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.errorContainer,
                                    contentColor = MaterialTheme.colorScheme.onErrorContainer
                                ),
                                shape = ExpressiveShapes.Pill,
                                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                                modifier = Modifier.height(34.dp)
                            ) {
                                Text("Disconnect", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Cast Volume Slider
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (uiState.volume == 0f) Icons.Default.VolumeMute else Icons.Default.VolumeDown,
                                contentDescription = "Volume",
                                tint = primaryColor,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Slider(
                                value = uiState.volume,
                                onValueChange = onVolumeChange,
                                colors = SliderDefaults.colors(
                                    thumbColor = primaryColor,
                                    activeTrackColor = primaryColor,
                                    inactiveTrackColor = primaryColor.copy(alpha = 0.25f)
                                ),
                                modifier = Modifier.weight(1f)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "${(uiState.volume * 100).toInt()}%",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = textColor,
                                modifier = Modifier.width(36.dp)
                            )
                        }
                    }
                }
            }

            // 2. AVAILABLE DEVICES LIST
            Text(
                text = "Available Receivers (${uiState.availableDevices.size})",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                color = textMuted,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            if (uiState.availableDevices.isEmpty()) {
                Surface(
                    shape = ExpressiveShapes.Medium,
                    color = cardBackground,
                    border = cardBorder,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(primaryColor.copy(alpha = 0.12f))
                                .graphicsLayer {
                                    if (isScanning) {
                                        scaleX = pulseScale
                                        scaleY = pulseScale
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Wifi,
                                contentDescription = null,
                                tint = primaryColor,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = if (isScanning) "Searching for Chromecast receivers..." else "No Cast devices discovered",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = textColor
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Make sure your Google Nest, Chromecast, or Smart TV is powered on and connected to the same Wi-Fi network.",
                            style = MaterialTheme.typography.bodySmall,
                            color = textMuted,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            fontSize = 12.sp
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        OutlinedButton(
                            onClick = onStartScan,
                            shape = ExpressiveShapes.Pill
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Scan Again")
                        }
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(
                        items = uiState.availableDevices,
                        key = { it.id }
                    ) { device ->
                        val isThisActive = uiState.activeDevice?.id == device.id
                        CastDeviceListItem(
                            device = device,
                            isActive = isThisActive,
                            primaryColor = primaryColor,
                            cardBackground = cardBackground,
                            cardBorder = cardBorder,
                            textColor = textColor,
                            textMuted = textMuted,
                            onClick = {
                                if (!isThisActive) {
                                    onConnectDevice(device)
                                }
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 3. CAST STREAM QUALITY SELECTOR
            Text(
                text = "Stream Quality Preset",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                color = textMuted,
                modifier = Modifier.padding(bottom = 6.dp)
            )

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(CastStreamQuality.values()) { quality ->
                    val isSelected = uiState.streamQuality == quality
                    Surface(
                        shape = ExpressiveShapes.Pill,
                        color = if (isSelected) primaryColor else cardBackground,
                        border = if (isSelected) null else cardBorder,
                        modifier = Modifier
                            .clip(ExpressiveShapes.Pill)
                            .springPressPhysics { onQualityChange(quality) }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                        ) {
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                            }
                            Text(
                                text = quality.label,
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = if (isSelected) MaterialTheme.colorScheme.onPrimary else textColor,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 4. SYSTEM OUTPUT SWITCHER SHORTCUT
            OutlinedButton(
                onClick = { openSystemMediaOutput(context) },
                shape = ExpressiveShapes.Medium,
                border = cardBorder,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .springPressPhysics()
            ) {
                Icon(
                    imageVector = Icons.Default.Router,
                    contentDescription = null,
                    tint = primaryColor,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Open System Media Output Switcher",
                    color = textColor,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun CastDeviceListItem(
    device: CastDevice,
    isActive: Boolean,
    primaryColor: Color,
    cardBackground: Color,
    cardBorder: BorderStroke,
    textColor: Color,
    textMuted: Color,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = ExpressiveShapes.Small,
        color = if (isActive) primaryColor.copy(alpha = 0.14f) else cardBackground,
        border = if (isActive) BorderStroke(1.dp, primaryColor.copy(alpha = 0.6f)) else cardBorder,
        modifier = Modifier
            .fillMaxWidth()
            .springPressPhysics { onClick() }
            .testTag("cast_device_${device.id}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(ExpressiveShapes.Small)
                    .background(if (isActive) primaryColor else primaryColor.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = getDeviceIcon(device.type),
                    contentDescription = null,
                    tint = if (isActive) MaterialTheme.colorScheme.onPrimary else primaryColor,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = device.name,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = if (isActive) primaryColor else textColor,
                    fontSize = 14.5.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "${device.modelName} • ${device.ipAddress}",
                        style = MaterialTheme.typography.bodySmall,
                        color = textMuted,
                        fontSize = 11.5.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            if (isActive) {
                Surface(
                    shape = ExpressiveShapes.Pill,
                    color = primaryColor,
                    modifier = Modifier.padding(start = 8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.GraphicEq,
                            contentDescription = "Active",
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Connected",
                            color = MaterialTheme.colorScheme.onPrimary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            } else {
                Surface(
                    shape = ExpressiveShapes.Pill,
                    color = primaryColor.copy(alpha = 0.12f),
                    modifier = Modifier.padding(start = 8.dp)
                ) {
                    Text(
                        text = "Connect",
                        color = primaryColor,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                    )
                }
            }
        }
    }
}

private fun getDeviceIcon(type: CastDeviceType): ImageVector {
    return when (type) {
        CastDeviceType.NEST_SPEAKER -> Icons.Default.Speaker
        CastDeviceType.CHROMECAST -> Icons.Default.Cast
        CastDeviceType.ANDROID_TV, CastDeviceType.SMART_DISPLAY -> Icons.Default.Tv
        CastDeviceType.CAST_GROUP -> Icons.Default.SpeakerGroup
        CastDeviceType.GENERIC_WIRELESS -> Icons.Default.Router
    }
}

private fun openSystemMediaOutput(context: Context) {
    try {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            val intent = Intent(Settings.Panel.ACTION_VOLUME).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        } else {
            val intent = Intent(Settings.ACTION_SOUND_SETTINGS).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        }
    } catch (_: Exception) {
        try {
            val intent = Intent(Settings.ACTION_WIRELESS_SETTINGS).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        } catch (_: Exception) {}
    }
}
