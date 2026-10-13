package com.example.ui.components

import android.graphics.RenderEffect
import android.graphics.Shader
import android.os.Build
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.model.Track

/**
 * Dynamic Glassmorphic Blur Background component.
 * Adapts in real-time to the album art color palette:
 * - Fluid hardware-accelerated RenderEffect.createBlurEffect() on Android 12+ (API 31+)
 * - Smooth animated color transitions when tracks change
 * - Ambient multi-orb color mesh that breathes subtly with playback
 * - Translucent frosted glass scrim providing high contrast for foreground controls
 */
@Composable
fun GlassmorphicBackground(
    track: Track,
    blurIntensity: Float = 0.8f,
    isAmoledBlack: Boolean = false,
    isPlaying: Boolean = false,
    snowballEffectEnabled: Boolean = true,
    liveMovingGradientEnabled: Boolean = true,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val blurRadiusPx = (110f * blurIntensity.coerceIn(0.2f, 1.8f))

    // 1. Smoothly animate palette colors when track changes
    val targetPrimary = if (track.primaryAmbientColor != Color.Unspecified && track.primaryAmbientColor != Color(0xFF263859)) {
        track.primaryAmbientColor
    } else {
        Color(0xFF3860B2)
    }

    val targetSecondary = if (track.secondaryAmbientColor != Color.Unspecified && track.secondaryAmbientColor != Color(0xFF0B132B)) {
        track.secondaryAmbientColor
    } else {
        Color(0xFF1E293B)
    }

    val animatedPrimary by animateColorAsState(
        targetValue = targetPrimary,
        animationSpec = tween(durationMillis = 750, easing = FastOutSlowInEasing),
        label = "glassmorphicPrimary"
    )

    val animatedSecondary by animateColorAsState(
        targetValue = targetSecondary,
        animationSpec = tween(durationMillis = 750, easing = FastOutSlowInEasing),
        label = "glassmorphicSecondary"
    )

    val targetAccent = remember(targetPrimary, targetSecondary) {
        Color(
            red = (targetPrimary.red * 0.7f + targetSecondary.green * 0.3f).coerceIn(0f, 1f),
            green = (targetPrimary.green * 0.3f + targetSecondary.blue * 0.7f).coerceIn(0f, 1f),
            blue = (targetPrimary.blue * 0.5f + targetSecondary.red * 0.5f).coerceIn(0f, 1f),
            alpha = 1f
        )
    }

    val animatedAccent by animateColorAsState(
        targetValue = targetAccent,
        animationSpec = tween(durationMillis = 750, easing = FastOutSlowInEasing),
        label = "glassmorphicAccent"
    )

    // 2. Live moving gradient animation and organic mesh drift
    val infiniteTransition = rememberInfiniteTransition(label = "glassmorphicMeshAnimation")
    val pulseScale by animateFloatAsState(
        targetValue = if (isPlaying) 1.05f else 1.0f,
        animationSpec = tween(durationMillis = 600),
        label = "playPulseScale"
    )

    val driftAnim by infiniteTransition.animateFloat(
        initialValue = -35f,
        targetValue = 35f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 6000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "meshDrift"
    )

    val gradientAngleProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 16000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "liveGradientRotation"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(if (isAmoledBlack) Color.Black else Color(0xFF080C14))
    ) {
        // 3. Hardware Blurred Album Artwork Base Layer
        val imageModel = track.coverUrl ?: track.albumArtUri
        val hasResCover = track.coverResId != null

        val blurModifier = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            Modifier.graphicsLayer {
                renderEffect = RenderEffect
                    .createBlurEffect(
                        blurRadiusPx,
                        blurRadiusPx,
                        Shader.TileMode.CLAMP
                    )
                    .asComposeRenderEffect()
            }
        } else {
            // High-quality opacity blend fallback on Android < 12
            Modifier.graphicsLayer { alpha = 0.55f }
        }

        if (hasResCover) {
            Image(
                painter = painterResource(id = track.coverResId!!),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .scale(1.2f) // Prevents edge vignetting under heavy blur
                    .then(blurModifier)
            )
        } else if (imageModel != null) {
            val bgImageRequest = remember(imageModel) {
                ImageRequest.Builder(context)
                    .data(imageModel)
                    .size(256, 256)
                    .dispatcher(kotlinx.coroutines.Dispatchers.IO)
                    .crossfade(true)
                    .build()
            }
            AsyncImage(
                model = bgImageRequest,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .scale(1.2f)
                    .then(blurModifier)
            )
        }

        // 4. Live Moving Fluid Ambient Color Mesh (Drawn in Draw Phase to avoid recomposition)
        if (liveMovingGradientEnabled) {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        scaleX = pulseScale
                        scaleY = pulseScale
                    }
            ) {
                val radians = Math.toRadians(gradientAngleProgress.toDouble())
                val cosVal = Math.cos(radians).toFloat()
                val sinVal = Math.sin(radians).toFloat()

                // Dynamic live moving linear gradient sweep layer
                drawRect(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            animatedPrimary.copy(alpha = if (isAmoledBlack) 0.35f else 0.50f),
                            animatedAccent.copy(alpha = if (isAmoledBlack) 0.25f else 0.40f),
                            animatedSecondary.copy(alpha = if (isAmoledBlack) 0.30f else 0.45f),
                            animatedPrimary.copy(alpha = if (isAmoledBlack) 0.20f else 0.35f)
                        ),
                        start = Offset(500f + cosVal * 400f, 500f + sinVal * 400f),
                        end = Offset(500f - cosVal * 400f, 500f - sinVal * 400f)
                    )
                )

                // Top-Left Primary Orb
                drawRect(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            animatedPrimary.copy(alpha = if (isAmoledBlack) 0.35f else 0.55f),
                            animatedPrimary.copy(alpha = if (isAmoledBlack) 0.15f else 0.25f),
                            Color.Transparent
                        ),
                        center = Offset(200f + driftAnim, 300f - driftAnim),
                        radius = 900f
                    )
                )

                // Center-Right Secondary Orb
                drawRect(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            animatedSecondary.copy(alpha = if (isAmoledBlack) 0.30f else 0.50f),
                            animatedSecondary.copy(alpha = if (isAmoledBlack) 0.10f else 0.20f),
                            Color.Transparent
                        ),
                        center = Offset(900f - driftAnim, 950f + driftAnim),
                        radius = 850f
                    )
                )

                // Bottom-Center Accent Orb
                drawRect(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            animatedAccent.copy(alpha = if (isAmoledBlack) 0.22f else 0.38f),
                            Color.Transparent
                        ),
                        center = Offset(450f, 1500f + driftAnim),
                        radius = 800f
                    )
                )
            }
        }

        // 5. Frosted Glass Atmospheric Vignette & Contrast Scrim Overlay (Cached brush)
        val scrimBrush = remember(isAmoledBlack) {
            val colors = if (isAmoledBlack) {
                listOf(
                    Color.Black.copy(alpha = 0.25f),
                    Color.Black.copy(alpha = 0.55f),
                    Color.Black.copy(alpha = 0.85f),
                    Color.Black.copy(alpha = 0.98f)
                )
            } else {
                listOf(
                    Color(0x33080C14),
                    Color(0x66080C14),
                    Color(0xAA080C14),
                    Color(0xE005080E)
                )
            }
            Brush.verticalGradient(colors)
        }

        val sheenBrush = remember {
            Brush.verticalGradient(
                listOf(
                    Color.White.copy(alpha = 0.08f),
                    Color.Transparent,
                    Color.Transparent
                )
            )
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(scrimBrush)
        )

        // 6. Top Specular Glass Reflection Sheen
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(sheenBrush)
        )
    }
}
