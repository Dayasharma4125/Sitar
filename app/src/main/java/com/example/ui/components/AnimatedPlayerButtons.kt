package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.ExpressiveShapes
import com.example.ui.theme.springPressPhysics
import kotlinx.coroutines.delay

/**
 * Animated Hero Play/Pause button with:
 * 1. Tactile spring squash-and-stretch on click.
 * 2. Expanding ambient aura pulse ring while playing.
 * 3. Smooth icon rotation and scale pop on toggle.
 */
@Composable
fun AnimatedPlayPauseHeroButton(
    isPlaying: Boolean,
    primaryColor: Color,
    onPrimaryColor: Color = Color.White,
    size: Dp = 72.dp,
    iconSize: Dp = 38.dp,
    shape: Shape = CircleShape,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    testTag: String = "play_pause_button"
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.86f else 1.0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "heroButtonScale"
    )

    // Animated Aura Pulse when playing
    val infiniteTransition = rememberInfiniteTransition(label = "pulseRing")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = if (isPlaying) 0.6f else 0f,
        targetValue = if (isPlaying) 0.0f else 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulseAlpha"
    )
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = if (isPlaying) 1.0f else 1.0f,
        targetValue = if (isPlaying) 1.55f else 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulseScale"
    )

    // Icon rotation & scale on toggle
    val iconRotation by animateFloatAsState(
        targetValue = if (isPlaying) 0f else 90f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessMediumLow),
        label = "iconRotation"
    )

    Box(
        modifier = modifier.size(size * 1.6f),
        contentAlignment = Alignment.Center
    ) {
        // Outer Expanding Dynamic Pulse Ring
        if (isPlaying) {
            Box(
                modifier = Modifier
                    .size(size)
                    .graphicsLayer {
                        scaleX = pulseScale
                        scaleY = pulseScale
                        alpha = pulseAlpha
                    }
                    .clip(shape)
                    .background(primaryColor.copy(alpha = 0.45f))
            )
        }

        // Main Hero Button with Spring Scale
        Surface(
            onClick = onClick,
            interactionSource = interactionSource,
            shape = shape,
            color = primaryColor,
            shadowElevation = if (isPressed) 2.dp else 12.dp,
            tonalElevation = 8.dp,
            modifier = Modifier
                .size(size)
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                }
                .testTag(testTag)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                Icon(
                    imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = if (isPlaying) "Pause" else "Play",
                    tint = onPrimaryColor,
                    modifier = Modifier
                        .size(iconSize)
                        .graphicsLayer {
                            rotationZ = iconRotation
                        }
                )
            }
        }
    }
}

/**
 * Animated Skip Next / Skip Previous button with horizontal spring shove.
 */
@Composable
fun AnimatedSkipButton(
    isNext: Boolean,
    tint: Color = Color.White,
    size: Dp = 52.dp,
    iconSize: Dp = 32.dp,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    testTag: String = if (isNext) "skip_next_button" else "skip_previous_button"
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.84f else 1.0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
        label = "skipScale"
    )

    val translationX by animateFloatAsState(
        targetValue = if (isPressed) (if (isNext) 8f else -8f) else 0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
        label = "skipNudge"
    )

    Surface(
        onClick = onClick,
        interactionSource = interactionSource,
        shape = CircleShape,
        color = Color.Transparent,
        modifier = modifier
            .size(size)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
                this.translationX = translationX
            }
            .testTag(testTag)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = if (isNext) Icons.Default.SkipNext else Icons.Default.SkipPrevious,
                contentDescription = if (isNext) "Skip Next" else "Skip Previous",
                tint = tint,
                modifier = Modifier.size(iconSize)
            )
        }
    }
}

/**
 * Animated Favorite Star/Heart button with spring explosion bounce.
 */
@Composable
fun AnimatedFavoriteButton(
    isFavorite: Boolean,
    activeColor: Color = Color(0xFFFFD700),
    inactiveColor: Color = Color.White.copy(alpha = 0.70f),
    size: Dp = 44.dp,
    iconSize: Dp = 26.dp,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
    testTag: String = "favorite_button"
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    var burstAnim by remember { mutableStateOf(false) }
    LaunchedEffect(isFavorite) {
        burstAnim = true
        delay(400)
        burstAnim = false
    }

    val bounceScale by animateFloatAsState(
        targetValue = if (burstAnim && isFavorite) 1.38f else if (isPressed) 0.85f else 1.0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioHighBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "favoriteBounce"
    )

    val rotationZ by animateFloatAsState(
        targetValue = if (burstAnim && isFavorite) 15f else 0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
        label = "favoriteRotation"
    )

    Surface(
        onClick = onToggle,
        interactionSource = interactionSource,
        shape = CircleShape,
        color = Color.Transparent,
        modifier = modifier
            .size(size)
            .graphicsLayer {
                scaleX = bounceScale
                scaleY = bounceScale
                this.rotationZ = rotationZ
            }
            .testTag(testTag)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = if (isFavorite) Icons.Filled.Star else Icons.Outlined.StarOutline,
                contentDescription = if (isFavorite) "Remove from Favorites" else "Add to Favorites",
                tint = if (isFavorite) activeColor else inactiveColor,
                modifier = Modifier.size(iconSize)
            )
        }
    }
}

/**
 * Generic Animated Action Button with spring touch feedback and active glowing background pill.
 */
@Composable
fun AnimatedTransportButton(
    icon: ImageVector,
    contentDescription: String?,
    isActive: Boolean = false,
    activeColor: Color = MaterialTheme.colorScheme.primary,
    inactiveColor: Color = Color.White.copy(alpha = 0.75f),
    size: Dp = 44.dp,
    iconSize: Dp = 24.dp,
    shape: Shape = CircleShape,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    testTag: String? = null
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.86f else if (isActive) 1.06f else 1.0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "transportBtnScale"
    )

    val bgColor by animateColorAsState(
        targetValue = if (isActive) activeColor.copy(alpha = 0.22f) else Color.Transparent,
        animationSpec = tween(300),
        label = "transportBtnBg"
    )

    val iconTint by animateColorAsState(
        targetValue = if (isActive) activeColor else inactiveColor,
        animationSpec = tween(300),
        label = "transportBtnTint"
    )

    Surface(
        onClick = onClick,
        interactionSource = interactionSource,
        shape = shape,
        color = bgColor,
        border = if (isActive) BorderStroke(1.dp, activeColor.copy(alpha = 0.45f)) else null,
        modifier = modifier
            .size(size)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .then(if (testTag != null) Modifier.testTag(testTag) else Modifier)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                tint = iconTint,
                modifier = Modifier.size(iconSize)
            )
        }
    }
}

/**
 * General-purpose Animated M3 Icon Button with spring press physics, tactile feedback,
 * and subtle tonal highlight.
 */
@Composable
fun AnimatedM3IconButton(
    icon: ImageVector,
    contentDescription: String?,
    onClick: () -> Unit,
    tint: Color = Color.White,
    size: Dp = 44.dp,
    iconSize: Dp = 24.dp,
    shape: Shape = CircleShape,
    backgroundColor: Color = Color.Transparent,
    borderColor: Color? = null,
    modifier: Modifier = Modifier,
    testTag: String? = null
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.85f else 1.0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = 900f
        ),
        label = "m3IconBtnScale"
    )

    Surface(
        onClick = onClick,
        interactionSource = interactionSource,
        shape = shape,
        color = backgroundColor,
        border = borderColor?.let { BorderStroke(1.dp, it) },
        modifier = modifier
            .size(size)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .then(if (testTag != null) Modifier.testTag(testTag) else Modifier)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                tint = tint,
                modifier = Modifier.size(iconSize)
            )
        }
    }
}

/**
 * Animated Action Pill / Chip with tactile spring scale and dynamic active glow.
 */
@Composable
fun AnimatedActionPill(
    icon: ImageVector,
    label: String,
    isActive: Boolean = false,
    activeColor: Color = MaterialTheme.colorScheme.primary,
    inactiveColor: Color = Color.White.copy(alpha = 0.85f),
    backgroundColor: Color = Color.White.copy(alpha = 0.08f),
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    testTag: String? = null
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.90f else if (isActive) 1.03f else 1.0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = 800f
        ),
        label = "actionPillScale"
    )

    val currentBg by animateColorAsState(
        targetValue = if (isActive) activeColor.copy(alpha = 0.28f) else backgroundColor,
        animationSpec = tween(250),
        label = "actionPillBg"
    )

    val currentContentColor by animateColorAsState(
        targetValue = if (isActive) activeColor else inactiveColor,
        animationSpec = tween(250),
        label = "actionPillColor"
    )

    Surface(
        onClick = onClick,
        interactionSource = interactionSource,
        shape = ExpressiveShapes.Pill,
        color = currentBg,
        border = if (isActive) BorderStroke(1.dp, activeColor.copy(alpha = 0.5f)) else null,
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .then(if (testTag != null) Modifier.testTag(testTag) else Modifier)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = currentContentColor,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = label,
                color = currentContentColor,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}
