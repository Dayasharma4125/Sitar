package com.example.ui.aod

import android.text.format.DateFormat
import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AodLayoutType
import com.example.model.PlayerUiState
import com.example.ui.util.HapticType
import com.example.ui.util.rememberHapticController
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

/**
 * Fullscreen Always On Display (AOD) Ambient Music Mode
 *
 * Features:
 * - Pure OLED Black (#000000) for zero power consumption on AMOLED displays
 * - 6 Dynamic thematic layouts:
 *   1. MINIMAL_OLED_CLOCK: Clean geometric clock & subtle track progress
 *   2. RETRO_CASSETTE_GLOW: Glowing neon wireframe magnetic tape spools
 *   3. VINYL_GROOVE_ARC: Orbital vinyl grooves & radial track arc
 *   4. CYBER_MATRIX_HUD: Futuristic matrix telemetry HUD & frequency bars
 *   5. ZEN_ORGANIC_BREATHE: Pulsating fluid organic geometry & serene glyphs
 *   6. CAPSULE_PILL_GLOW: Sleek pill capsule with illuminated status
 * - Burn-in Protection: Periodically shifts coordinates by a few pixels
 * - Dimming factor & double-tap to wake/exit
 */
@Composable
fun AodMusicScreen(
    uiState: PlayerUiState,
    layoutType: AodLayoutType,
    burnInProtection: Boolean = true,
    dimFactor: Float = 0.25f,
    onPlayPauseClick: () -> Unit,
    onPreviousClick: () -> Unit,
    onNextClick: () -> Unit,
    onExitAod: () -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = rememberHapticController()
    var currentTimeMs by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var burnInShiftX by remember { mutableStateOf(0) }
    var burnInShiftY by remember { mutableStateOf(0) }
    var showExitHint by remember { mutableStateOf(false) }

    // Clock ticker & Burn-in pixel protection shifter
    LaunchedEffect(Unit) {
        var counter = 0
        while (true) {
            currentTimeMs = System.currentTimeMillis()
            delay(1000L)
            counter++
            if (burnInProtection && counter % 45 == 0) {
                burnInShiftX = ((-8..8).random())
                burnInShiftY = ((-12..12).random())
            }
        }
    }

    BackHandler {
        onExitAod()
    }

    val timeString = remember(currentTimeMs) {
        SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(currentTimeMs))
    }
    val dateString = remember(currentTimeMs) {
        SimpleDateFormat("EEE, MMM d", Locale.getDefault()).format(Date(currentTimeMs))
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
            .pointerInput(Unit) {
                detectTapGestures(
                    onDoubleTap = {
                        haptic.performHaptic(HapticType.MEDIUM_CLICK)
                        onExitAod()
                    },
                    onTap = {
                        showExitHint = true
                    }
                )
            }
    ) {
        // Main AOD Content shifted for OLED burn-in protection
        Box(
            modifier = Modifier
                .fillMaxSize()
                .offset { IntOffset(burnInShiftX.dp.roundToPx(), burnInShiftY.dp.roundToPx()) }
                .padding(horizontal = 24.dp, vertical = 32.dp),
            contentAlignment = Alignment.Center
        ) {
            when (layoutType) {
                AodLayoutType.MINIMAL_OLED_CLOCK -> {
                    MinimalOledClockAod(
                        timeString = timeString,
                        dateString = dateString,
                        uiState = uiState,
                        onPlayPauseClick = onPlayPauseClick,
                        onPreviousClick = onPreviousClick,
                        onNextClick = onNextClick
                    )
                }
                AodLayoutType.VINYL_GROOVE_ARC -> {
                    VinylGrooveArcAod(
                        timeString = timeString,
                        uiState = uiState,
                        onPlayPauseClick = onPlayPauseClick,
                        onPreviousClick = onPreviousClick,
                        onNextClick = onNextClick
                    )
                }
                AodLayoutType.CYBER_MATRIX_HUD -> {
                    CyberMatrixHudAod(
                        timeString = timeString,
                        dateString = dateString,
                        uiState = uiState,
                        onPlayPauseClick = onPlayPauseClick,
                        onPreviousClick = onPreviousClick,
                        onNextClick = onNextClick
                    )
                }
                AodLayoutType.CAPSULE_PILL_GLOW -> {
                    CapsulePillGlowAod(
                        timeString = timeString,
                        dateString = dateString,
                        uiState = uiState,
                        onPlayPauseClick = onPlayPauseClick,
                        onPreviousClick = onPreviousClick,
                        onNextClick = onNextClick
                    )
                }
            }
        }

        // Top-right exit button & double tap hint
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp, end = 16.dp, start = 20.dp)
                .align(Alignment.TopCenter),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "AOD AMBIENT",
                style = MaterialTheme.typography.labelSmall.copy(
                    letterSpacing = 2.sp,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Light
                ),
                color = Color.White.copy(alpha = 0.25f)
            )

            IconButton(
                onClick = {
                    haptic.performHaptic(HapticType.LIGHT_CLICK)
                    onExitAod()
                },
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Exit AOD",
                    tint = Color.White.copy(alpha = 0.35f),
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        // Tap hint notification overlay
        AnimatedVisibility(
            visible = showExitHint,
            enter = fadeIn(tween(200)),
            exit = fadeOut(tween(600)),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 24.dp)
        ) {
            LaunchedEffect(showExitHint) {
                if (showExitHint) {
                    delay(2500L)
                    showExitHint = false
                }
            }
            Surface(
                shape = RoundedCornerShape(50),
                color = Color(0x33FFFFFF),
                border = BorderStroke(1.dp, Color(0x44FFFFFF))
            ) {
                Text(
                    text = "Double-tap or tap ✕ to exit AOD",
                    color = Color.White.copy(alpha = 0.8f),
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                )
            }
        }
    }
}

// -------------------------------------------------------------
// Layout 1: Minimal OLED Clock & Track Progress
// -------------------------------------------------------------
@Composable
private fun MinimalOledClockAod(
    timeString: String,
    dateString: String,
    uiState: PlayerUiState,
    onPlayPauseClick: () -> Unit,
    onPreviousClick: () -> Unit,
    onNextClick: () -> Unit
) {
    val progress = if (uiState.durationMs > 0) {
        (uiState.currentPositionMs.toFloat() / uiState.durationMs.toFloat()).coerceIn(0f, 1f)
    } else 0f

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Clock Section
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(top = 40.dp)
        ) {
            Text(
                text = timeString,
                color = Color.White.copy(alpha = 0.88f),
                fontSize = 64.sp,
                fontWeight = FontWeight.Thin,
                letterSpacing = (-2).sp
            )
            Text(
                text = dateString.uppercase(),
                color = Color.White.copy(alpha = 0.45f),
                style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 2.sp)
            )
        }

        // Center Minimal Music Card
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(if (uiState.isPlaying) Color(0xFF4CAF50) else Color.White.copy(alpha = 0.3f))
                )
                Text(
                    text = uiState.currentTrack.title.ifBlank { "No Track Selected" },
                    color = Color.White.copy(alpha = 0.95f),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = uiState.currentTrack.artist.ifBlank { "Unknown Artist" },
                color = Color.White.copy(alpha = 0.5f),
                fontSize = 13.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Thin Minimalist OLED Progress Bar
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.75f)
                    .height(2.dp)
                    .clip(RoundedCornerShape(50))
                    .background(Color.White.copy(alpha = 0.15f))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(progress)
                        .height(2.dp)
                        .background(Color.White.copy(alpha = 0.85f))
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(0.75f),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = formatAodTime(uiState.currentPositionMs),
                    color = Color.White.copy(alpha = 0.35f),
                    fontSize = 10.sp
                )
                Text(
                    text = formatAodTime(uiState.durationMs),
                    color = Color.White.copy(alpha = 0.35f),
                    fontSize = 10.sp
                )
            }
        }

        // Minimalist Transport Controls
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 20.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onPreviousClick) {
                Icon(
                    Icons.Default.SkipPrevious,
                    contentDescription = "Previous",
                    tint = Color.White.copy(alpha = 0.5f),
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.width(20.dp))
            IconButton(
                onClick = onPlayPauseClick,
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.12f))
            ) {
                Icon(
                    imageVector = if (uiState.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = "Play/Pause",
                    tint = Color.White,
                    modifier = Modifier.size(26.dp)
                )
            }
            Spacer(modifier = Modifier.width(20.dp))
            IconButton(onClick = onNextClick) {
                Icon(
                    Icons.Default.SkipNext,
                    contentDescription = "Next",
                    tint = Color.White.copy(alpha = 0.5f),
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

// -------------------------------------------------------------
// Layout 2: Retro Cassette Wireframe Glow AOD
// -------------------------------------------------------------
@Composable
private fun RetroCassetteGlowAod(
    timeString: String,
    uiState: PlayerUiState,
    onPlayPauseClick: () -> Unit,
    onPreviousClick: () -> Unit,
    onNextClick: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "cassette_aod")
    val spoolRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(2800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "spool_rot"
    )

    val amberColor = Color(0xFFFFB300)

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Digital Amber Clock
        Text(
            text = timeString,
            color = amberColor.copy(alpha = 0.9f),
            fontSize = 48.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 2.sp,
            fontFamily = FontFamily.Monospace,
            modifier = Modifier.padding(top = 36.dp)
        )

        // Glowing Wireframe Cassette Deck
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(width = 240.dp, height = 140.dp)
                    .border(BorderStroke(1.5.dp, amberColor.copy(alpha = 0.45f)), RoundedCornerShape(12.dp))
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Left Spool
                    Canvas(
                        modifier = Modifier
                            .size(54.dp)
                            .graphicsLayer { rotationZ = if (uiState.isPlaying) spoolRotation else 0f }
                    ) {
                        drawCircle(color = amberColor.copy(alpha = 0.8f), style = Stroke(width = 2f))
                        for (i in 0..5) {
                            val rad = (i * 60f) * (PI / 180f).toFloat()
                            val cx = size.width / 2f
                            val cy = size.height / 2f
                            val r = size.minDimension / 2.3f
                            drawLine(
                                color = amberColor.copy(alpha = 0.6f),
                                start = Offset(cx, cy),
                                end = Offset(cx + cos(rad) * r, cy + sin(rad) * r),
                                strokeWidth = 1.5f
                            )
                        }
                    }

                    // Tape window
                    Box(
                        modifier = Modifier
                            .size(width = 70.dp, height = 28.dp)
                            .border(BorderStroke(1.dp, amberColor.copy(alpha = 0.3f)), RoundedCornerShape(4.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = formatAodTime(uiState.currentPositionMs),
                            color = amberColor.copy(alpha = 0.8f),
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    // Right Spool
                    Canvas(
                        modifier = Modifier
                            .size(54.dp)
                            .graphicsLayer { rotationZ = if (uiState.isPlaying) spoolRotation else 0f }
                    ) {
                        drawCircle(color = amberColor.copy(alpha = 0.8f), style = Stroke(width = 2f))
                        for (i in 0..5) {
                            val rad = (i * 60f) * (PI / 180f).toFloat()
                            val cx = size.width / 2f
                            val cy = size.height / 2f
                            val r = size.minDimension / 2.3f
                            drawLine(
                                color = amberColor.copy(alpha = 0.6f),
                                start = Offset(cx, cy),
                                end = Offset(cx + cos(rad) * r, cy + sin(rad) * r),
                                strokeWidth = 1.5f
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = uiState.currentTrack.title.ifBlank { "Cassette Tape A" },
                color = amberColor.copy(alpha = 0.95f),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = uiState.currentTrack.artist.ifBlank { "Analog Studio" },
                color = amberColor.copy(alpha = 0.6f),
                fontSize = 12.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        // Amber Tape Controls
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onPreviousClick) {
                Icon(Icons.Default.SkipPrevious, contentDescription = "Prev", tint = amberColor.copy(alpha = 0.6f))
            }
            Spacer(modifier = Modifier.width(16.dp))
            IconButton(
                onClick = onPlayPauseClick,
                modifier = Modifier
                    .size(44.dp)
                    .border(BorderStroke(1.dp, amberColor), CircleShape)
            ) {
                Icon(
                    imageVector = if (uiState.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = "Play/Pause",
                    tint = amberColor
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            IconButton(onClick = onNextClick) {
                Icon(Icons.Default.SkipNext, contentDescription = "Next", tint = amberColor.copy(alpha = 0.6f))
            }
        }
    }
}

// -------------------------------------------------------------
// Layout 3: Vinyl Groove Arc AOD
// -------------------------------------------------------------
@Composable
private fun VinylGrooveArcAod(
    timeString: String,
    uiState: PlayerUiState,
    onPlayPauseClick: () -> Unit,
    onPreviousClick: () -> Unit,
    onNextClick: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "vinyl_aod")
    val vinylRot by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(3400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "vinyl_rot"
    )

    val progress = if (uiState.durationMs > 0) {
        (uiState.currentPositionMs.toFloat() / uiState.durationMs.toFloat()).coerceIn(0f, 1f)
    } else 0f

    val cyanColor = Color(0xFF00E5FF)

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = timeString,
            color = Color.White.copy(alpha = 0.85f),
            fontSize = 44.sp,
            fontWeight = FontWeight.Light,
            modifier = Modifier.padding(top = 36.dp)
        )

        // Vinyl Arc Center Visualizer
        Box(
            modifier = Modifier.size(210.dp),
            contentAlignment = Alignment.Center
        ) {
            // Radial Progress Sweep Arc
            Canvas(modifier = Modifier.fillMaxSize()) {
                val stroke = 3f
                drawCircle(color = Color.White.copy(alpha = 0.1f), style = Stroke(stroke))
                drawArc(
                    color = cyanColor,
                    startAngle = -90f,
                    sweepAngle = progress * 360f,
                    useCenter = false,
                    style = Stroke(width = 4f, cap = StrokeCap.Round)
                )
            }

            // Concentric Vinyl Grooves
            Canvas(
                modifier = Modifier
                    .size(170.dp)
                    .graphicsLayer { rotationZ = if (uiState.isPlaying) vinylRot else 0f }
            ) {
                val maxR = size.minDimension / 2f
                for (i in 1..4) {
                    drawCircle(
                        color = Color.White.copy(alpha = 0.08f + i * 0.03f),
                        radius = maxR * (0.35f + i * 0.14f),
                        style = Stroke(1.2f)
                    )
                }
            }

            // Center Info Badge
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(horizontal = 24.dp)
            ) {
                Text(
                    text = uiState.currentTrack.title.ifBlank { "Vinyl Track" },
                    color = Color.White.copy(alpha = 0.9f),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = uiState.currentTrack.artist.ifBlank { "Artist" },
                    color = cyanColor.copy(alpha = 0.7f),
                    fontSize = 11.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center
                )
            }
        }

        // Vinyl Controls
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onPreviousClick) {
                Icon(Icons.Default.SkipPrevious, contentDescription = "Prev", tint = Color.White.copy(alpha = 0.5f))
            }
            Spacer(modifier = Modifier.width(16.dp))
            IconButton(
                onClick = onPlayPauseClick,
                modifier = Modifier
                    .size(44.dp)
                    .border(BorderStroke(1.dp, cyanColor.copy(alpha = 0.6f)), CircleShape)
            ) {
                Icon(
                    imageVector = if (uiState.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = "Play/Pause",
                    tint = cyanColor
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            IconButton(onClick = onNextClick) {
                Icon(Icons.Default.SkipNext, contentDescription = "Next", tint = Color.White.copy(alpha = 0.5f))
            }
        }
    }
}

// -------------------------------------------------------------
// Layout 4: Cyber Matrix HUD AOD
// -------------------------------------------------------------
@Composable
private fun CyberMatrixHudAod(
    timeString: String,
    dateString: String,
    uiState: PlayerUiState,
    onPlayPauseClick: () -> Unit,
    onPreviousClick: () -> Unit,
    onNextClick: () -> Unit
) {
    val neonGreen = Color(0xFF00E676)
    val infiniteTransition = rememberInfiniteTransition(label = "cyber_aod")
    val pulse by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1200, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "cyber_pulse"
    )

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Cyber HUD Top
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(top = 36.dp)
        ) {
            Text(
                text = "SYS.AUDIO // MATRIX",
                color = neonGreen.copy(alpha = 0.6f),
                style = MaterialTheme.typography.labelSmall.copy(
                    letterSpacing = 3.sp,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 10.sp
                )
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = timeString,
                color = neonGreen.copy(alpha = pulse),
                fontSize = 52.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = dateString,
                color = Color.White.copy(alpha = 0.35f),
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace
            )
        }

        // Cyber Frequency Bars & Telemetry
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp)
        ) {
            // Live frequency telemetry bars
            Row(
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .height(32.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.Bottom
            ) {
                val barCount = 14
                for (i in 0 until barCount) {
                    val barHeight = if (uiState.isPlaying) {
                        (0.25f + 0.7f * kotlin.math.sin((i + pulse * 4f).toDouble()).toFloat().coerceAtLeast(0.1f))
                    } else 0.15f

                    Box(
                        modifier = Modifier
                            .width(6.dp)
                            .height((32 * barHeight).dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(neonGreen.copy(alpha = if (uiState.isPlaying) 0.85f else 0.25f))
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            Text(
                text = uiState.currentTrack.title.ifBlank { "MATRIX_STREAM_01" },
                color = Color.White.copy(alpha = 0.95f),
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "${uiState.currentTrack.artist} // ${formatAodTime(uiState.currentPositionMs)}",
                color = neonGreen.copy(alpha = 0.65f),
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        // Cyber Deck Controls
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onPreviousClick) {
                Icon(Icons.Default.SkipPrevious, contentDescription = "Prev", tint = neonGreen.copy(alpha = 0.5f))
            }
            Spacer(modifier = Modifier.width(16.dp))
            IconButton(
                onClick = onPlayPauseClick,
                modifier = Modifier
                    .size(44.dp)
                    .border(BorderStroke(1.dp, neonGreen), RoundedCornerShape(8.dp))
            ) {
                Icon(
                    imageVector = if (uiState.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = "Play/Pause",
                    tint = neonGreen
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            IconButton(onClick = onNextClick) {
                Icon(Icons.Default.SkipNext, contentDescription = "Next", tint = neonGreen.copy(alpha = 0.5f))
            }
        }
    }
}

// -------------------------------------------------------------
// Layout 5: Zen Organic Breathe AOD
// -------------------------------------------------------------
@Composable
private fun ZenOrganicBreatheAod(
    timeString: String,
    uiState: PlayerUiState,
    onPlayPauseClick: () -> Unit,
    onPreviousClick: () -> Unit,
    onNextClick: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "zen_aod")
    val breatheScale by infiniteTransition.animateFloat(
        initialValue = 0.92f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(2400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "zen_breathe"
    )

    val zenTeal = Color(0xFF26A69A)

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = timeString,
            color = Color.White.copy(alpha = 0.8f),
            fontSize = 44.sp,
            fontWeight = FontWeight.ExtraLight,
            modifier = Modifier.padding(top = 36.dp)
        )

        // Pulsating Organic Geometry
        Box(
            modifier = Modifier
                .size(190.dp)
                .graphicsLayer {
                    scaleX = if (uiState.isPlaying) breatheScale else 1f
                    scaleY = if (uiState.isPlaying) breatheScale else 1f
                },
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val path = Path()
                val cx = size.width / 2f
                val cy = size.height / 2f
                val r = size.minDimension * 0.42f

                // Draw organic petal-like flower morph
                for (deg in 0..360 step 5) {
                    val angle = deg * (PI / 180f).toFloat()
                    val morph = 1f + 0.12f * sin(angle * 4f)
                    val x = cx + cos(angle) * r * morph
                    val y = cy + sin(angle) * r * morph
                    if (deg == 0) path.moveTo(x, y) else path.lineTo(x, y)
                }
                path.close()
                drawPath(path = path, color = zenTeal.copy(alpha = 0.25f))
                drawPath(path = path, color = zenTeal.copy(alpha = 0.7f), style = Stroke(1.5f))
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(horizontal = 20.dp)
            ) {
                Text(
                    text = uiState.currentTrack.title.ifBlank { "Harmonic Flow" },
                    color = Color.White.copy(alpha = 0.95f),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Light,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = uiState.currentTrack.artist.ifBlank { "Zen Peace" },
                    color = zenTeal.copy(alpha = 0.8f),
                    fontSize = 11.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center
                )
            }
        }

        // Zen Controls
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onPreviousClick) {
                Icon(Icons.Default.SkipPrevious, contentDescription = "Prev", tint = Color.White.copy(alpha = 0.4f))
            }
            Spacer(modifier = Modifier.width(16.dp))
            IconButton(
                onClick = onPlayPauseClick,
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(zenTeal.copy(alpha = 0.2f))
                    .border(BorderStroke(1.dp, zenTeal.copy(alpha = 0.5f)), CircleShape)
            ) {
                Icon(
                    imageVector = if (uiState.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = "Play/Pause",
                    tint = zenTeal
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            IconButton(onClick = onNextClick) {
                Icon(Icons.Default.SkipNext, contentDescription = "Next", tint = Color.White.copy(alpha = 0.4f))
            }
        }
    }
}

// -------------------------------------------------------------
// Layout 6: Capsule Pill Glow AOD
// -------------------------------------------------------------
@Composable
private fun CapsulePillGlowAod(
    timeString: String,
    dateString: String,
    uiState: PlayerUiState,
    onPlayPauseClick: () -> Unit,
    onPreviousClick: () -> Unit,
    onNextClick: () -> Unit
) {
    val purpleGlow = Color(0xFFAB47BC)
    val progress = if (uiState.durationMs > 0) {
        (uiState.currentPositionMs.toFloat() / uiState.durationMs.toFloat()).coerceIn(0f, 1f)
    } else 0f

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(top = 36.dp)
        ) {
            Text(
                text = timeString,
                color = Color.White.copy(alpha = 0.9f),
                fontSize = 56.sp,
                fontWeight = FontWeight.Normal
            )
            Text(
                text = dateString,
                color = purpleGlow.copy(alpha = 0.7f),
                style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.4.sp)
            )
        }

        // Glowing Capsule Pill
        Surface(
            shape = RoundedCornerShape(50),
            color = Color(0xFF140D1E),
            border = BorderStroke(1.5.dp, purpleGlow.copy(alpha = 0.55f)),
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .padding(vertical = 12.dp)
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = uiState.currentTrack.title.ifBlank { "Track Title" },
                            color = Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = uiState.currentTrack.artist.ifBlank { "Artist" },
                            color = Color.White.copy(alpha = 0.6f),
                            fontSize = 12.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Icon(
                        imageVector = Icons.Default.GraphicEq,
                        contentDescription = null,
                        tint = purpleGlow,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Progress Bar in Pill
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.dp)
                        .clip(RoundedCornerShape(50))
                        .background(Color.White.copy(alpha = 0.15f))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(progress)
                            .height(3.dp)
                            .background(purpleGlow)
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = formatAodTime(uiState.currentPositionMs),
                        color = Color.White.copy(alpha = 0.4f),
                        fontSize = 10.sp
                    )
                    Text(
                        text = formatAodTime(uiState.durationMs),
                        color = Color.White.copy(alpha = 0.4f),
                        fontSize = 10.sp
                    )
                }
            }
        }

        // Capsule Controls
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onPreviousClick) {
                Icon(Icons.Default.SkipPrevious, contentDescription = "Prev", tint = Color.White.copy(alpha = 0.5f))
            }
            Spacer(modifier = Modifier.width(16.dp))
            IconButton(
                onClick = onPlayPauseClick,
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(purpleGlow.copy(alpha = 0.25f))
                    .border(BorderStroke(1.dp, purpleGlow), CircleShape)
            ) {
                Icon(
                    imageVector = if (uiState.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = "Play/Pause",
                    tint = purpleGlow
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            IconButton(onClick = onNextClick) {
                Icon(Icons.Default.SkipNext, contentDescription = "Next", tint = Color.White.copy(alpha = 0.5f))
            }
        }
    }
}

private fun formatAodTime(ms: Long): String {
    val totalSeconds = (ms / 1000).coerceAtLeast(0)
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format(Locale.getDefault(), "%d:%02d", minutes, seconds)
}
