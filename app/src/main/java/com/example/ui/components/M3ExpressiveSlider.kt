package com.example.ui.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsDraggedAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Locale

/**
 * Material 3 Expressive Scrubber & Progress Bar.
 * Features dynamic thumb bounce, expressive height scaling on interaction,
 * and high-contrast time badges.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun M3ExpressiveProgressBar(
    currentPositionMs: Long,
    durationMs: Long,
    onSeekTo: (Long) -> Unit,
    activeColor: Color = MaterialTheme.colorScheme.primary,
    inactiveColor: Color = Color.White.copy(alpha = 0.15f),
    modifier: Modifier = Modifier
) {
    val totalDuration = durationMs.coerceAtLeast(1L)
    val progressFraction = (currentPositionMs.toFloat() / totalDuration.toFloat()).coerceIn(0f, 1f)

    val interactionSource = remember { MutableInteractionSource() }
    val isDragged by interactionSource.collectIsDraggedAsState()
    val isPressed by interactionSource.collectIsPressedAsState()
    val isInteracting = isDragged || isPressed

    val thumbWidth by animateDpAsState(
        targetValue = if (isInteracting) 20.dp else 12.dp,
        animationSpec = spring(stiffness = 400f),
        label = "thumbWidth"
    )

    val thumbHeight by animateDpAsState(
        targetValue = if (isInteracting) 24.dp else 16.dp,
        animationSpec = spring(stiffness = 400f),
        label = "thumbHeight"
    )

    val trackHeight by animateDpAsState(
        targetValue = if (isInteracting) 8.dp else 5.dp,
        animationSpec = spring(stiffness = 400f),
        label = "trackHeight"
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp)
    ) {
        // Scrubber Slider
        Slider(
            value = progressFraction,
            onValueChange = { fraction ->
                val seekMs = (fraction * totalDuration).toLong()
                onSeekTo(seekMs)
            },
            interactionSource = interactionSource,
            modifier = Modifier.fillMaxWidth(),
            thumb = {
                Box(
                    modifier = Modifier
                        .size(width = thumbWidth, height = thumbHeight)
                        .shadow(elevation = if (isInteracting) 8.dp else 3.dp, shape = RoundedCornerShape(8.dp))
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            Brush.verticalGradient(
                                listOf(Color.White, activeColor)
                            )
                        )
                )
            },
            track = { sliderState ->
                val fraction = sliderState.value
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(trackHeight)
                        .clip(CircleShape)
                        .background(inactiveColor)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(fraction)
                            .height(trackHeight)
                            .clip(CircleShape)
                            .background(
                                Brush.horizontalGradient(
                                    listOf(
                                        activeColor.copy(alpha = 0.85f),
                                        activeColor
                                    )
                                )
                            )
                    )
                }
            }
        )

        // Time indicators row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                color = Color.White.copy(alpha = 0.08f),
                shape = RoundedCornerShape(6.dp)
            ) {
                Text(
                    text = formatTimestamp(currentPositionMs),
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 0.5.sp
                    ),
                    color = Color.White.copy(alpha = 0.85f),
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }

            Surface(
                color = Color.White.copy(alpha = 0.08f),
                shape = RoundedCornerShape(6.dp)
            ) {
                val remainingMs = (durationMs - currentPositionMs).coerceAtLeast(0L)
                Text(
                    text = "-${formatTimestamp(remainingMs)}",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Medium,
                        letterSpacing = 0.5.sp
                    ),
                    color = Color.White.copy(alpha = 0.60f),
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
        }
    }
}

private fun formatTimestamp(ms: Long): String {
    val totalSeconds = (ms / 1000L).coerceAtLeast(0L)
    val minutes = totalSeconds / 60L
    val seconds = totalSeconds % 60L
    return String.format(Locale.getDefault(), "%d:%02d", minutes, seconds)
}
