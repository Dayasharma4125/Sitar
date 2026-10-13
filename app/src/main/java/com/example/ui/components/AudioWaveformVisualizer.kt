package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import kotlinx.coroutines.isActive
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

/**
 * Visual styles for the animated bottom audio waveform visualizer.
 */
enum class WaveformVisualizerStyle(val title: String) {
    NEON_BARS("Neon Equalizer Bars"),
    FLUID_SINE_WAVE("Fluid Sound Waves"),
    GRADIENT_SPECTRUM("Harmonic Spectrum")
}

/**
 * An expressive, real-time reactive animated audio waveform visualizer.
 * Reacts smoothly to playback status, volume level, playback speed, and track colors.
 */
@Composable
fun AudioWaveformVisualizer(
    isPlaying: Boolean,
    modifier: Modifier = Modifier,
    playbackSpeed: Float = 1.0f,
    volume: Float = 1.0f,
    primaryColor: Color = MaterialTheme.colorScheme.primary,
    secondaryColor: Color = MaterialTheme.colorScheme.tertiary,
    accentColor: Color = MaterialTheme.colorScheme.secondary,
    height: Dp = 48.dp,
    barCount: Int = 38,
    initialStyle: WaveformVisualizerStyle = WaveformVisualizerStyle.NEON_BARS,
    onStyleChange: ((WaveformVisualizerStyle) -> Unit)? = null
) {
    var currentStyle by remember(initialStyle) { mutableStateOf(initialStyle) }

    // Smooth amplitude transition when playing starts or pauses
    val amplitudeFactor = remember { Animatable(if (isPlaying) 1.0f else 0.15f) }
    LaunchedEffect(isPlaying) {
        amplitudeFactor.animateTo(
            targetValue = if (isPlaying) 1.0f else 0.15f,
            animationSpec = tween(durationMillis = 600)
        )
    }

    var phase by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(isPlaying, playbackSpeed) {
        var lastNanos = withFrameNanos { it }
        while (isActive) {
            if (!isPlaying) {
                // When paused, update at a gentle 30fps to conserve battery and CPU
                kotlinx.coroutines.delay(32)
            }
            withFrameNanos { currentNanos ->
                val dtSeconds = ((currentNanos - lastNanos) / 1_000_000_000f).coerceIn(0f, 0.05f)
                lastNanos = currentNanos
                val speedFactor = if (isPlaying) playbackSpeed.coerceIn(0.5f, 2.5f) * 2.2f else 0.65f
                val nextPhase = phase + dtSeconds * speedFactor
                // Period of 20 * PI guarantees all harmonic multiples (1.2, 0.8, 1.6, 1.4, 1.0, 0.4, 0.5, 1.8, 2.4)
                // are exact integer multiples of 2 * PI, making the modulo 100% mathematically continuous with 0 jump.
                val period = 20f * PI.toFloat()
                phase = if (nextPhase >= period) nextPhase - period else nextPhase
            }
        }
    }

    val pulse = 1.0f + 0.15f * kotlin.math.sin(phase * 0.6f)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                val nextStyle = when (currentStyle) {
                    WaveformVisualizerStyle.NEON_BARS -> WaveformVisualizerStyle.FLUID_SINE_WAVE
                    WaveformVisualizerStyle.FLUID_SINE_WAVE -> WaveformVisualizerStyle.GRADIENT_SPECTRUM
                    WaveformVisualizerStyle.GRADIENT_SPECTRUM -> WaveformVisualizerStyle.NEON_BARS
                }
                currentStyle = nextStyle
                onStyleChange?.invoke(nextStyle)
            }
            .testTag("audio_waveform_visualizer"),
        contentAlignment = Alignment.Center
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(height)
        ) {
            val canvasWidth = size.width
            val canvasHeight = size.height
            val amp = amplitudeFactor.value * (0.45f + volume.coerceIn(0f, 1f) * 0.55f)

            when (currentStyle) {
                WaveformVisualizerStyle.NEON_BARS -> {
                    drawNeonBars(
                        width = canvasWidth,
                        height = canvasHeight,
                        barCount = barCount,
                        phase = phase,
                        pulse = pulse,
                        amp = amp,
                        isPlaying = isPlaying,
                        primaryColor = primaryColor,
                        secondaryColor = secondaryColor,
                        accentColor = accentColor
                    )
                }

                WaveformVisualizerStyle.FLUID_SINE_WAVE -> {
                    drawFluidWaves(
                        width = canvasWidth,
                        height = canvasHeight,
                        phase = phase,
                        amp = amp,
                        isPlaying = isPlaying,
                        primaryColor = primaryColor,
                        secondaryColor = secondaryColor,
                        accentColor = accentColor
                    )
                }

                WaveformVisualizerStyle.GRADIENT_SPECTRUM -> {
                    drawHarmonicSpectrum(
                        width = canvasWidth,
                        height = canvasHeight,
                        barCount = barCount,
                        phase = phase,
                        pulse = pulse,
                        amp = amp,
                        isPlaying = isPlaying,
                        primaryColor = primaryColor,
                        secondaryColor = secondaryColor,
                        accentColor = accentColor
                    )
                }
            }
        }
    }
}

/**
 * Style 1: Modern rounded neon equalizer bars with dynamic symmetric energy.
 */
private fun DrawScope.drawNeonBars(
    width: Float,
    height: Float,
    barCount: Int,
    phase: Float,
    pulse: Float,
    amp: Float,
    isPlaying: Boolean,
    primaryColor: Color,
    secondaryColor: Color,
    accentColor: Color
) {
    val totalSpacingRatio = 0.45f
    val barWidth = (width / barCount) * (1f - totalSpacingRatio)
    val spacing = (width - (barWidth * barCount)) / (barCount - 1).coerceAtLeast(1)
    val centerY = height / 2f
    val maxBarHeight = height * 0.90f

    for (i in 0 until barCount) {
        val normalizedIndex = i.toFloat() / barCount.toFloat()
        val x = i * (barWidth + spacing) + (barWidth / 2f)

        // Generate synthetic harmonic motion based on position and phase
        val harmonic1 = sin(phase * 1.8f + normalizedIndex * PI.toFloat() * 3.5f)
        val harmonic2 = cos(phase * 2.4f - normalizedIndex * PI.toFloat() * 5.2f)
        val harmonic3 = sin(phase * 0.9f + normalizedIndex * PI.toFloat() * 8.0f)

        // Center-weighted envelope so middle bars dance with higher dynamic range
        val envelope = sin(normalizedIndex * PI.toFloat()).coerceIn(0.2f, 1.0f)

        val rawEnergy = if (isPlaying) {
            abs((harmonic1 * 0.45f + harmonic2 * 0.35f + harmonic3 * 0.20f) * pulse)
        } else {
            // Calm subtle resting ripple when paused
            (sin(normalizedIndex * PI.toFloat() * 2f + phase * 0.5f) * 0.15f + 0.2f).coerceAtLeast(0.08f)
        }

        val barHeight = (rawEnergy * maxBarHeight * envelope * amp).coerceIn(4f, maxBarHeight)
        val topY = (centerY - barHeight / 2f).coerceAtLeast(1f)
        val bottomY = (centerY + barHeight / 2f).coerceAtMost(height - 1f)

        val barBrush = Brush.verticalGradient(
            colors = listOf(
                accentColor.copy(alpha = 0.95f),
                primaryColor.copy(alpha = 0.85f),
                secondaryColor.copy(alpha = 0.70f)
            ),
            startY = topY,
            endY = bottomY
        )

        drawLine(
            brush = barBrush,
            start = Offset(x, topY),
            end = Offset(x, bottomY),
            strokeWidth = barWidth.coerceAtLeast(2.5f),
            cap = StrokeCap.Round
        )
    }
}

/**
 * Style 2: Fluid multi-layer sine waves with glowing gradient fill underneath.
 */
private fun DrawScope.drawFluidWaves(
    width: Float,
    height: Float,
    phase: Float,
    amp: Float,
    isPlaying: Boolean,
    primaryColor: Color,
    secondaryColor: Color,
    accentColor: Color
) {
    val centerY = height / 2f
    val maxAmplitude = height * 0.40f * amp

    // Background Wave (Secondary Color)
    val pathBack = Path().apply {
        moveTo(0f, centerY)
        val steps = 40
        for (i in 0..steps) {
            val progress = i.toFloat() / steps
            val x = progress * width
            val wave = if (isPlaying) {
                sin(progress * 2f * PI.toFloat() * 2.2f + phase * 1.2f) * 0.6f +
                        cos(progress * 2f * PI.toFloat() * 1.1f - phase * 0.8f) * 0.4f
            } else {
                sin(progress * 2f * PI.toFloat() * 1.5f + phase * 0.4f) * 0.2f
            }
            val y = centerY + wave * maxAmplitude * 0.75f
            if (i == 0) moveTo(x, y) else lineTo(x, y)
        }
    }

    drawPath(
        path = pathBack,
        brush = Brush.horizontalGradient(
            listOf(
                secondaryColor.copy(alpha = 0.45f),
                accentColor.copy(alpha = 0.65f),
                secondaryColor.copy(alpha = 0.45f)
            )
        ),
        style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
    )

    // Foreground Wave (Primary Accent Color with fill)
    val pathFront = Path()
    val fillPath = Path()

    val steps = 50
    for (i in 0..steps) {
        val progress = i.toFloat() / steps
        val x = progress * width
        val wave = if (isPlaying) {
            sin(progress * 2f * PI.toFloat() * 3.0f - phase * 1.6f) * 0.7f +
                    sin(progress * 2f * PI.toFloat() * 1.4f + phase * 1.0f) * 0.3f
        } else {
            sin(progress * 2f * PI.toFloat() * 2.0f + phase * 0.5f) * 0.25f
        }
        val y = centerY + wave * maxAmplitude

        if (i == 0) {
            pathFront.moveTo(x, y)
            fillPath.moveTo(x, y)
        } else {
            pathFront.lineTo(x, y)
            fillPath.lineTo(x, y)
        }
    }

    fillPath.lineTo(width, height)
    fillPath.lineTo(0f, height)
    fillPath.close()

    // Gradient fill under the front wave
    drawPath(
        path = fillPath,
        brush = Brush.verticalGradient(
            colors = listOf(
                primaryColor.copy(alpha = 0.28f * amp),
                primaryColor.copy(alpha = 0.02f)
            ),
            startY = centerY - maxAmplitude,
            endY = height
        ),
        style = Fill
    )

    // Crisp crest line
    drawPath(
        path = pathFront,
        brush = Brush.horizontalGradient(
            listOf(
                primaryColor.copy(alpha = 0.75f),
                accentColor.copy(alpha = 1.0f),
                primaryColor.copy(alpha = 0.85f)
            )
        ),
        style = Stroke(width = 3.5.dp.toPx(), cap = StrokeCap.Round)
    )
}

/**
 * Style 3: Harmonic mirrored spectrum with rhythmic frequency peaks.
 */
private fun DrawScope.drawHarmonicSpectrum(
    width: Float,
    height: Float,
    barCount: Int,
    phase: Float,
    pulse: Float,
    amp: Float,
    isPlaying: Boolean,
    primaryColor: Color,
    secondaryColor: Color,
    accentColor: Color
) {
    val barWidth = width / barCount * 0.65f
    val spacing = (width - (barWidth * barCount)) / (barCount - 1).coerceAtLeast(1)
    val baselineY = height * 0.95f
    val maxBarHeight = height * 0.85f

    for (i in 0 until barCount) {
        val norm = i.toFloat() / barCount.toFloat()
        val x = i * (barWidth + spacing) + (barWidth / 2f)

        // Frequency simulation: low frequencies on the left, mids in center, highs on right
        val freqWeight = (1f - (norm * 0.5f))
        val oscillation = if (isPlaying) {
            val bass = sin(phase * 2.2f + norm * 4f) * (1f - norm)
            val mids = cos(phase * 3.5f + norm * 9f) * sin(norm * PI.toFloat())
            val treble = sin(phase * 5.0f - norm * 14f) * norm
            abs(bass * 0.5f + mids * 0.35f + treble * 0.25f) * pulse * freqWeight
        } else {
            0.12f * (sin(norm * 6f + phase) + 1f) * 0.5f
        }

        val barH = (oscillation * maxBarHeight * amp).coerceIn(3f, maxBarHeight)
        val topY = baselineY - barH

        val brush = Brush.verticalGradient(
            colors = listOf(
                accentColor,
                primaryColor,
                secondaryColor.copy(alpha = 0.3f)
            ),
            startY = topY,
            endY = baselineY
        )

        drawRoundRect(
            brush = brush,
            topLeft = Offset(x - barWidth / 2f, topY),
            size = Size(barWidth, barH),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(barWidth / 2f, barWidth / 2f)
        )
    }
}
