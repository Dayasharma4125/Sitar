package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RadialGradientShader
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.unit.dp
import kotlin.math.sin
import kotlin.random.Random

private data class SnowballParticle(
    var xFraction: Float,
    var yFraction: Float,
    val radiusPx: Float,
    val alpha: Float,
    val fallSpeed: Float,
    val swayPhase: Float,
    val swayFrequency: Float,
    val swayAmplitude: Float,
    val isSoftGlow: Boolean
)

/**
 * Animated Snowball Effect Canvas.
 * Creates an immersive live snowfall particle system over the glassmorphic player UI.
 * Features falling snowflakes of varying sizes, opacities, gentle horizontal sine sway,
 * and soft glowing snowballs that enhance the visual depth.
 */
@Composable
fun SnowballEffectCanvas(
    modifier: Modifier = Modifier,
    particleCount: Int = 50,
    snowColor: Color = Color.White,
    isPlaying: Boolean = true
) {
    val random = remember { Random(System.currentTimeMillis()) }

    // Initialize particle pool
    val particles = remember(particleCount) {
        List(particleCount) {
            SnowballParticle(
                xFraction = random.nextFloat(),
                yFraction = random.nextFloat(),
                radiusPx = (3f + random.nextFloat() * 12f),
                alpha = 0.25f + random.nextFloat() * 0.65f,
                fallSpeed = 0.0006f + random.nextFloat() * 0.0018f,
                swayPhase = random.nextFloat() * 6.28318f,
                swayFrequency = 1.2f + random.nextFloat() * 2.5f,
                swayAmplitude = 0.0015f + random.nextFloat() * 0.004f,
                isSoftGlow = random.nextFloat() < 0.35f
            )
        }
    }

    // Animation frame tick trigger
    val tickState = remember { mutableFloatStateOf(0f) }

    LaunchedEffect(isPlaying) {
        var lastTime = 0L
        while (true) {
            withFrameNanos { frameTimeNanos ->
                if (lastTime != 0L) {
                    val dt = ((frameTimeNanos - lastTime) / 1_000_000_000f).coerceIn(0.005f, 0.05f)
                    val speedMultiplier = if (isPlaying) 1.2f else 0.6f

                    particles.forEach { p ->
                        p.yFraction += p.fallSpeed * speedMultiplier * (dt * 60f)
                        if (p.yFraction > 1.05f) {
                            p.yFraction = -0.05f
                            p.xFraction = random.nextFloat()
                        }
                    }
                    tickState.floatValue += dt
                }
                lastTime = frameTimeNanos
            }
        }
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height
        val totalTime = tickState.floatValue

        particles.forEach { p ->
            // Sine-wave horizontal sway
            val xSway = sin(totalTime * p.swayFrequency + p.swayPhase) * p.swayAmplitude
            val currentX = ((p.xFraction + xSway).mod(1.0f)) * width
            val currentY = p.yFraction * height

            val center = Offset(currentX, currentY)

            if (p.isSoftGlow) {
                // Soft blur glow rendered with concentric alpha layers for zero object allocations
                val glowRadius = p.radiusPx * 2.0f
                drawCircle(
                    color = snowColor.copy(alpha = p.alpha * 0.22f),
                    radius = glowRadius,
                    center = center
                )
                drawCircle(
                    color = snowColor.copy(alpha = p.alpha * 0.65f),
                    radius = p.radiusPx * 1.3f,
                    center = center
                )
                drawCircle(
                    color = snowColor.copy(alpha = p.alpha),
                    radius = p.radiusPx,
                    center = center
                )
            } else {
                drawCircle(
                    color = snowColor.copy(alpha = p.alpha),
                    radius = p.radiusPx,
                    center = center
                )
            }
        }
    }
}
