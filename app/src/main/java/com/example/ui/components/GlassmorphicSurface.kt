package com.example.ui.components

import android.graphics.RenderEffect
import android.graphics.Shader
import android.os.Build
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.model.Track
import com.example.ui.theme.ensureVibrantDarkAccent

/**
 * Adaptive Glassmorphic Palette derived and animated from Album Artwork colors.
 */
@Immutable
data class AlbumArtGlassPalette(
    val primary: Color,
    val secondary: Color,
    val accent: Color,
    val glassSurface: Color,
    val glassSurfaceSubtle: Color,
    val glassBorder: Brush,
    val glassHighlight: Brush,
    val activePill: Color,
    val activePillContent: Color
)

/**
 * Remembers and animates an adaptive glassmorphic palette based on the current track's album art colors.
 */
@Composable
fun rememberAlbumArtGlassPalette(
    track: Track,
    isAmoledBlack: Boolean = false
): AlbumArtGlassPalette {
    val rawPrimary = if (track.primaryAmbientColor != Color.Unspecified && track.primaryAmbientColor != Color(0xFF263859)) {
        track.primaryAmbientColor
    } else {
        val hash = (track.title + track.artist).hashCode()
        com.example.data.MediaRepository.getHarmonicColor(hash)
    }

    val rawSecondary = if (track.secondaryAmbientColor != Color.Unspecified && track.secondaryAmbientColor != Color(0xFF0B132B)) {
        track.secondaryAmbientColor
    } else {
        com.example.data.MediaRepository.getDarkerVariant(rawPrimary)
    }

    val targetPrimary = ensureVibrantDarkAccent(rawPrimary, fallback = Color(0xFFA8C8FF))
    val targetSecondary = ensureVibrantDarkAccent(rawSecondary, fallback = Color(0xFFBDC7DC))

    val animatedPrimary by animateColorAsState(
        targetValue = targetPrimary,
        animationSpec = tween(durationMillis = 650, easing = FastOutSlowInEasing),
        label = "animatedGlassPrimary"
    )

    val animatedSecondary by animateColorAsState(
        targetValue = targetSecondary,
        animationSpec = tween(durationMillis = 650, easing = FastOutSlowInEasing),
        label = "animatedGlassSecondary"
    )

    return remember(animatedPrimary, animatedSecondary, isAmoledBlack) {
        val accentColor = Color(
            red = (animatedPrimary.red * 0.6f + animatedSecondary.green * 0.4f).coerceIn(0f, 1f),
            green = (animatedPrimary.green * 0.4f + animatedSecondary.blue * 0.6f).coerceIn(0f, 1f),
            blue = (animatedPrimary.blue * 0.5f + animatedSecondary.red * 0.5f).coerceIn(0f, 1f),
            alpha = 1f
        )

        val surfaceColor = if (isAmoledBlack) {
            Color(0x33101216)
        } else {
            // Semi-transparent surface delicately tinted with the album palette
            Color(
                red = (animatedPrimary.red * 0.35f + 0.65f).coerceIn(0f, 1f),
                green = (animatedPrimary.green * 0.35f + 0.65f).coerceIn(0f, 1f),
                blue = (animatedPrimary.blue * 0.35f + 0.65f).coerceIn(0f, 1f),
                alpha = 0.14f
            )
        }

        val subtleSurface = if (isAmoledBlack) {
            Color(0x22181C24)
        } else {
            Color(0x18FFFFFF)
        }

        val borderBrush = Brush.linearGradient(
            colors = listOf(
                Color.White.copy(alpha = if (isAmoledBlack) 0.22f else 0.35f),
                animatedPrimary.copy(alpha = if (isAmoledBlack) 0.15f else 0.20f),
                Color.White.copy(alpha = if (isAmoledBlack) 0.05f else 0.08f)
            )
        )

        val highlightBrush = Brush.verticalGradient(
            colors = listOf(
                Color.White.copy(alpha = 0.18f),
                Color.Transparent
            )
        )

        AlbumArtGlassPalette(
            primary = animatedPrimary,
            secondary = animatedSecondary,
            accent = accentColor,
            glassSurface = surfaceColor,
            glassSurfaceSubtle = subtleSurface,
            glassBorder = borderBrush,
            glassHighlight = highlightBrush,
            activePill = animatedPrimary.copy(alpha = if (isAmoledBlack) 0.40f else 0.32f),
            activePillContent = Color.White
        )
    }
}

/**
 * Semi-transparent glassmorphic container surface with frosted specular border and blur effect.
 */
@Composable
fun GlassmorphicSurface(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(24.dp),
    backgroundColor: Color = Color.White.copy(alpha = 0.12f),
    borderBrush: Brush = Brush.verticalGradient(
        listOf(
            Color.White.copy(alpha = 0.35f),
            Color.White.copy(alpha = 0.08f)
        )
    ),
    borderWidth: Dp = 1.dp,
    onClick: (() -> Unit)? = null,
    enableBlur: Boolean = true,
    content: @Composable BoxScope.() -> Unit
) {
    val clickModifier = if (onClick != null) {
        Modifier.clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = ripple(color = Color.White.copy(alpha = 0.3f)),
            onClick = onClick
        )
    } else Modifier

    val blurModifier = if (enableBlur && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        Modifier.graphicsLayer {
            renderEffect = RenderEffect.createBlurEffect(
                20f,
                20f,
                Shader.TileMode.CLAMP
            ).asComposeRenderEffect()
        }
    } else Modifier

    Box(
        modifier = modifier
            .clip(shape)
            .border(borderWidth, borderBrush, shape)
            .background(backgroundColor, shape)
            .then(blurModifier)
            .then(clickModifier)
    ) {
        content()
    }
}

/**
 * Custom Modifier extension achieving the glassmorphic aesthetic for UI containers.
 * Applies rounded corners, semi-transparent frosted background, specular gradient border, and blur.
 */
fun Modifier.glassmorphic(
    shape: Shape = RoundedCornerShape(24.dp),
    backgroundColor: Color = Color.White.copy(alpha = 0.12f),
    borderColor: Color = Color.White.copy(alpha = 0.28f),
    borderWidth: Dp = 1.dp,
    enableBlur: Boolean = true
): Modifier = this
    .clip(shape)
    .border(
        width = borderWidth,
        brush = Brush.linearGradient(
            colors = listOf(
                borderColor,
                borderColor.copy(alpha = 0.08f),
                borderColor.copy(alpha = 0.22f)
            )
        ),
        shape = shape
    )
    .background(backgroundColor, shape)
    .then(
        if (enableBlur && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            Modifier.graphicsLayer {
                renderEffect = RenderEffect.createBlurEffect(
                    22f,
                    22f,
                    Shader.TileMode.CLAMP
                ).asComposeRenderEffect()
            }
        } else Modifier
    )
