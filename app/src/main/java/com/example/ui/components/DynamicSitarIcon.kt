package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.model.Track

/**
 * Dynamic Sitar Instrument Badge whose background dynamically adapts to the current track's artwork colors.
 * When playing, generates a subtle breathing gradient and acoustic pulse aura.
 */
@Composable
fun DynamicSitarIcon(
    track: Track? = null,
    isPlaying: Boolean = false,
    size: Dp = 40.dp,
    shape: Shape = RoundedCornerShape(12.dp),
    showPulse: Boolean = true,
    modifier: Modifier = Modifier
) {
    // Extract dynamic artwork-derived accent palette
    val artHash = track?.let { (it.title + it.artist + it.album).hashCode() } ?: 0
    val dynamicPrimary = if (track != null && track.id.isNotEmpty()) {
        val hue = (artHash and 0x7FFFFFFF) % 360
        Color.hsl(hue = hue.toFloat(), saturation = 0.72f, lightness = 0.42f)
    } else {
        MaterialTheme.colorScheme.primary
    }

    val dynamicSecondary = if (track != null && track.id.isNotEmpty()) {
        val hue2 = ((artHash + 60) and 0x7FFFFFFF) % 360
        Color.hsl(hue = hue2.toFloat(), saturation = 0.80f, lightness = 0.28f)
    } else {
        MaterialTheme.colorScheme.tertiary
    }

    val animatedColor1 by animateColorAsState(
        targetValue = dynamicPrimary,
        animationSpec = tween(durationMillis = 800),
        label = "dynamicSitarColor1"
    )
    val animatedColor2 by animateColorAsState(
        targetValue = dynamicSecondary,
        animationSpec = tween(durationMillis = 800),
        label = "dynamicSitarColor2"
    )

    val infiniteTransition = rememberInfiniteTransition(label = "sitarPulseTransition")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (isPlaying && showPulse) 1.08f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "sitarPulseScale"
    )
    val gradientShift by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = if (isPlaying) 1f else 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "sitarGradientShift"
    )

    Box(
        modifier = modifier
            .size(size)
            .scale(if (isPlaying && showPulse) pulseScale else 1f),
        contentAlignment = Alignment.Center
    ) {
        // Dynamic Ambient Glow
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(shape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            animatedColor1.copy(alpha = if (isPlaying) 0.5f else 0.25f),
                            Color.Transparent
                        )
                    )
                )
        )

        // Main Icon Surface with Dynamic Artwork Gradient Background
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(shape)
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            animatedColor1,
                            animatedColor2,
                            Color(0xFF160A04)
                        ),
                        start = androidx.compose.ui.geometry.Offset(0f, 0f),
                        end = androidx.compose.ui.geometry.Offset(size.value * 2 * (1f + gradientShift * 0.2f), size.value * 2)
                    )
                )
                .border(
                    width = 1.dp,
                    color = Color.White.copy(alpha = 0.22f),
                    shape = shape
                )
                .padding(size * 0.12f),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_sitar_instrument),
                contentDescription = "Sitar",
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}
