package com.example.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.example.model.PlayerUiState
import com.example.ui.theme.ExpressiveShapes
import com.example.ui.theme.ensureVibrantDarkAccent
import com.example.ui.theme.springPressPhysics
import com.example.ui.util.HapticType
import com.example.ui.util.rememberHapticController
import androidx.compose.foundation.BorderStroke
import kotlin.math.roundToInt

/**
 * Material 3 Split-Pill Floating Mini-Player.
 *
 * Requirements:
 * 1. Layout: A horizontal Row positioned above the navigation bar with Arrangement.spacedBy(8.dp).
 * 2. Primary Track Container (Left):
 *    - Wide pill-shaped M3 Surface (shape = MaterialTheme.shapes.extraLarge) taking majority width (weight(1f)).
 *    - Color: M3 MaterialTheme.colorScheme.primaryContainer or surfaceContainerHigh, dynamically tinted by album artwork.
 *    - Contents: Circular album art, vertical column for Song Title (M3 titleMedium) and Artist (M3 bodyMedium),
 *      and an M3 IconButton for "Skip Next".
 * 3. Independent Action Container (Right):
 *    - A separate M3 Surface (rounded-square / extra-large shape).
 *    - Color: M3 MaterialTheme.colorScheme.tertiaryContainer for high contrast.
 *    - Contains a single prominent M3 Icon for Play/Pause.
 */
@Composable
fun FloatingMiniPlayer(
    uiState: PlayerUiState,
    onMiniPlayerClick: () -> Unit,
    onPlayPauseClick: () -> Unit,
    onSkipNextClick: () -> Unit,
    onSkipPreviousClick: () -> Unit = {},
    isAmoledBlack: Boolean = false,
    modifier: Modifier = Modifier
) {
    val track = uiState.currentTrack

    // Dynamic tint derived from artwork with contrast protection
    val vibrantAccent = ensureVibrantDarkAccent(track.primaryAmbientColor, fallback = MaterialTheme.colorScheme.primary)
    val primaryContainerColor = if (isAmoledBlack) {
        Color(0xFF131722)
    } else {
        Color(
            red = (vibrantAccent.red * 0.20f + 0.08f).coerceIn(0f, 1f),
            green = (vibrantAccent.green * 0.20f + 0.08f).coerceIn(0f, 1f),
            blue = (vibrantAccent.blue * 0.25f + 0.11f).coerceIn(0f, 1f),
            alpha = 0.96f
        )
    }

    val actionContainerColor = if (isAmoledBlack) {
        vibrantAccent
    } else {
        vibrantAccent
    }

    val onActionContentColor = Color(0xFF041220)
    val hapticController = rememberHapticController()

    var dragOffsetX by remember { mutableFloatStateOf(0f) }
    val animatedDragOffsetX by animateFloatAsState(
        targetValue = dragOffsetX,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "MiniPlayerSwipeOffset"
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .testTag("floating_mini_player"),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 1. Primary Track Container (Left)
        Surface(
            onClick = onMiniPlayerClick,
            shape = ExpressiveShapes.PillDock,
            color = primaryContainerColor,
            border = BorderStroke(1.dp, vibrantAccent.copy(alpha = 0.30f)),
            tonalElevation = 4.dp,
            shadowElevation = 8.dp,
            modifier = Modifier
                .weight(1f)
                .height(64.dp)
                .offset { IntOffset(animatedDragOffsetX.roundToInt(), 0) }
                .springPressPhysics { onMiniPlayerClick() }
                .pointerInput(track.id) {
                    detectHorizontalDragGestures(
                        onDragStart = { },
                        onDragEnd = {
                            if (dragOffsetX < -100f) {
                                hapticController.performHaptic(HapticType.LIGHT_CLICK)
                                onSkipNextClick()
                            } else if (dragOffsetX > 100f) {
                                hapticController.performHaptic(HapticType.LIGHT_CLICK)
                                onSkipPreviousClick()
                            }
                            dragOffsetX = 0f
                        },
                        onDragCancel = {
                            dragOffsetX = 0f
                        },
                        onHorizontalDrag = { _, dragAmount ->
                            dragOffsetX = (dragOffsetX + dragAmount * 0.7f).coerceIn(-250f, 250f)
                        }
                    )
                }
                .testTag("mini_player_primary_pill")
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Circular Album Art Thumbnail
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                ) {
                    ArtworkImageOrFallback(
                        track = track,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Vertical column: Song Title (M3 titleMedium) and Artist (M3 bodyMedium)
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = track.title,
                        style = MaterialTheme.typography.titleMedium,
                        color = if (isAmoledBlack) Color.White else MaterialTheme.colorScheme.onPrimaryContainer,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = track.artist,
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (isAmoledBlack) Color.White.copy(alpha = 0.70f) else MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.75f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // M3 IconButton for "Skip Next"
                IconButton(
                    onClick = {
                        hapticController.performHaptic(HapticType.LIGHT_CLICK)
                        onSkipNextClick()
                    },
                    colors = IconButtonDefaults.iconButtonColors(
                        contentColor = if (isAmoledBlack) Color.White else MaterialTheme.colorScheme.onPrimaryContainer
                    ),
                    modifier = Modifier
                        .size(40.dp)
                        .testTag("mini_player_skip_next")
                ) {
                    Icon(
                        imageVector = Icons.Default.SkipNext,
                        contentDescription = "Skip Next",
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }

        // 2. Independent Action Container (Right)
        Surface(
            onClick = {
                hapticController.performHaptic(HapticType.MEDIUM_CLICK)
                onPlayPauseClick()
            },
            shape = ExpressiveShapes.Squircle,
            color = actionContainerColor,
            tonalElevation = 4.dp,
            shadowElevation = 8.dp,
            modifier = Modifier
                .size(64.dp)
                .springPressPhysics {
                    hapticController.performHaptic(HapticType.MEDIUM_CLICK)
                    onPlayPauseClick()
                }
                .testTag("mini_player_play_pause")
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (uiState.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = if (uiState.isPlaying) "Pause" else "Play",
                    tint = onActionContentColor,
                    modifier = Modifier.size(30.dp)
                )
            }
        }
    }
}
