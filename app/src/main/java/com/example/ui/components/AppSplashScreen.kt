package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

/**
 * High-craft startup splash animation for Pixel Player.
 * Features pulsing acoustic wave rings, dynamic theme-reactive gradients,
 * smooth spring entrance, and seamless transition to the main interface.
 */
@Composable
fun AppSplashScreen(
    visible: Boolean,
    isAmoledBlack: Boolean = false,
    onAnimationFinished: () -> Unit
) {
    val scaleAnim = remember { Animatable(0.7f) }
    val alphaAnim = remember { Animatable(0f) }

    val infiniteTransition = rememberInfiniteTransition(label = "pulseTransition")
    val pulseRing by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.35f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulseRing"
    )
    val ringAlpha by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ringAlpha"
    )

    LaunchedEffect(Unit) {
        // Entrance animation
        alphaAnim.animateTo(1f, tween(450, easing = FastOutSlowInEasing))
        scaleAnim.animateTo(1f, tween(550, easing = FastOutSlowInEasing))

        // Hold and display
        delay(1100)

        // Trigger finish
        onAnimationFinished()
    }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(200)),
        exit = fadeOut(tween(400, easing = FastOutSlowInEasing)) + scaleOut(targetScale = 1.08f, animationSpec = tween(400))
    ) {
        val backgroundColor = if (isAmoledBlack) Color.Black else MaterialTheme.colorScheme.background
        val primaryColor = MaterialTheme.colorScheme.primary
        val secondaryColor = MaterialTheme.colorScheme.tertiary
        val textColor = MaterialTheme.colorScheme.onBackground
        val mutedText = MaterialTheme.colorScheme.onSurfaceVariant

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundColor),
            contentAlignment = Alignment.Center
        ) {
            // Background ambient radial glow
            Canvas(modifier = Modifier.size(320.dp)) {
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            primaryColor.copy(alpha = 0.25f * alphaAnim.value),
                            secondaryColor.copy(alpha = 0.1f * alphaAnim.value),
                            Color.Transparent
                        ),
                        center = center,
                        radius = size.minDimension / 1.5f
                    )
                )

                // Pulsing acoustic rings
                drawCircle(
                    color = primaryColor.copy(alpha = ringAlpha * alphaAnim.value),
                    radius = (size.minDimension / 3.2f) * pulseRing,
                    style = Stroke(width = 2.dp.toPx())
                )
                drawCircle(
                    color = secondaryColor.copy(alpha = (ringAlpha * 0.7f) * alphaAnim.value),
                    radius = (size.minDimension / 2.6f) * pulseRing,
                    style = Stroke(width = 1.5.dp.toPx())
                )
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier
                    .scale(scaleAnim.value)
                    .alpha(alphaAnim.value)
            ) {
                // Vinyl Record / Core Sound Center
                Box(
                    modifier = Modifier.size(96.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val centerOffset = Offset(size.width / 2, size.height / 2)
                        // Vinyl disk background
                        drawCircle(
                            color = primaryColor.copy(alpha = 0.18f),
                            radius = size.minDimension / 2
                        )
                        // Outer rim
                        drawCircle(
                            color = primaryColor,
                            radius = size.minDimension / 2,
                            style = Stroke(width = 2.5.dp.toPx())
                        )
                        // Inner sound concentric groove 1
                        drawCircle(
                            color = primaryColor.copy(alpha = 0.4f),
                            radius = size.minDimension / 2.8f,
                            style = Stroke(width = 1.dp.toPx())
                        )
                        // Inner sound concentric groove 2
                        drawCircle(
                            color = primaryColor.copy(alpha = 0.25f),
                            radius = size.minDimension / 4f,
                            style = Stroke(width = 1.dp.toPx())
                        )
                    }

                    Icon(
                        imageVector = Icons.Default.MusicNote,
                        contentDescription = "Pixel Player",
                        tint = primaryColor,
                        modifier = Modifier.size(44.dp)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "Pixel Player",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.5.sp
                    ),
                    color = textColor,
                    fontFamily = FontFamily.SansSerif
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "HI-RES AUDIO ENGINE",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 2.5.sp
                    ),
                    color = mutedText
                )
            }
        }
    }
}
