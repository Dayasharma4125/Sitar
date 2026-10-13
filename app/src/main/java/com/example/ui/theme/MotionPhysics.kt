package com.example.ui.theme

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.SpringSpec
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Comprehensive Material 3 Expressive Motion Physics System.
 *
 * Implements real physical harmonic oscillation, mass-spring-damper dynamics,
 * tactile touch physics, harmonic breathing, and fluid momentum across all UI layers.
 */
object MotionPhysics {

    /**
     * Bouncy spring with high elasticity and responsive return.
     * Perfect for hero buttons, play/pause toggles, and modal popups.
     */
    val ExpressiveBouncy: SpringSpec<Float> = spring(
        dampingRatio = Spring.DampingRatioMediumBouncy,
        stiffness = Spring.StiffnessMediumLow
    )

    /**
     * Extra bouncy spring with pronounced overshoot and oscillation.
     * Ideal for heart favorite buttons, badge popups, and celebration bursts.
     */
    val HighOvershoot: SpringSpec<Float> = spring(
        dampingRatio = Spring.DampingRatioHighBouncy,
        stiffness = Spring.StiffnessLow
    )

    /**
     * Gentle fluid spring with low bounce and silky settling motion.
     * Ideal for screen transitions, bottom sheets, and playlist reordering.
     */
    val GentleSettling: SpringSpec<Float> = spring(
        dampingRatio = Spring.DampingRatioLowBouncy,
        stiffness = Spring.StiffnessLow
    )

    /**
     * Crisp, snappy spring with zero overshoot and rapid stabilization.
     * Ideal for sliders, scrubbers, and seekbar thumb tracking.
     */
    val SnappyTrack: SpringSpec<Float> = spring(
        dampingRatio = Spring.DampingRatioNoBouncy,
        stiffness = Spring.StiffnessMedium
    )

    /**
     * Tactile micro-spring for interactive button press scale physics.
     */
    val TactilePressSpring: SpringSpec<Float> = spring(
        dampingRatio = 0.62f,
        stiffness = 700f
    )

    /**
     * Standard Material 3 Expressive motion physics specs for layout transitions.
     */
    fun <T> expressiveSpring(
        dampingRatio: Float = Spring.DampingRatioLowBouncy,
        stiffness: Float = Spring.StiffnessMediumLow
    ): SpringSpec<T> = spring(dampingRatio = dampingRatio, stiffness = stiffness)

    // =========================================================================
    // SCREEN & COMPONENT ENTER / EXIT TRANSITIONS WITH MOTION PHYSICS
    // =========================================================================

    val SheetEnterTransition: EnterTransition = slideInVertically(
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessLow
        ),
        initialOffsetY = { it }
    ) + fadeIn(
        animationSpec = spring(stiffness = Spring.StiffnessLow)
    )

    val SheetExitTransition: ExitTransition = slideOutVertically(
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        targetOffsetY = { it }
    ) + fadeOut(
        animationSpec = spring(stiffness = Spring.StiffnessMedium)
    )

    val CardExpandEnter: EnterTransition = scaleIn(
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        initialScale = 0.88f
    ) + fadeIn(animationSpec = spring(stiffness = Spring.StiffnessMediumLow))

    val CardCollapseExit: ExitTransition = scaleOut(
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        targetScale = 0.92f
    ) + fadeOut(animationSpec = spring(stiffness = Spring.StiffnessMedium))

    val SharedAxisSlideEnter: EnterTransition = slideInVertically(
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        initialOffsetY = { it / 3 }
    ) + scaleIn(
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        initialScale = 0.94f
    ) + fadeIn(animationSpec = spring(stiffness = Spring.StiffnessMediumLow))

    val SharedAxisSlideExit: ExitTransition = slideOutVertically(
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        targetOffsetY = { -it / 6 }
    ) + scaleOut(
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        targetScale = 0.96f
    ) + fadeOut(animationSpec = spring(stiffness = Spring.StiffnessMedium))
}

/**
 * Modifier that adds tactile physics-based squish and spring-back bounce on touch press.
 *
 * @param pressedScale Scale down factor when finger is pressed down (default: 0.93f)
 * @param onClick Optional click listener invoked on release
 */
fun Modifier.springPressPhysics(
    pressedScale: Float = 0.93f,
    onClick: (() -> Unit)? = null
): Modifier = composed {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) pressedScale else 1f,
        animationSpec = spring(
            dampingRatio = if (isPressed) Spring.DampingRatioNoBouncy else Spring.DampingRatioMediumBouncy,
            stiffness = if (isPressed) 1200f else 650f
        ),
        label = "spring_press_scale"
    )

    this
        .graphicsLayer {
            scaleX = scale
            scaleY = scale
        }
        .then(
            if (onClick != null) {
                Modifier.clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    onClick = onClick
                )
            } else {
                Modifier
            }
        )
}

/**
 * Physics-based spring bounce trigger for tap gestures with dynamic rebound.
 */
fun Modifier.physicsBounceTap(
    pressedScale: Float = 0.88f,
    onTap: () -> Unit
): Modifier = composed {
    var isPressed by remember { mutableStateOf(false) }

    val scale by animateFloatAsState(
        targetValue = if (isPressed) pressedScale else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = 800f
        ),
        label = "physics_bounce_scale"
    )

    this
        .graphicsLayer {
            scaleX = scale
            scaleY = scale
        }
        .pointerInput(Unit) {
            detectTapGestures(
                onPress = {
                    isPressed = true
                    try {
                        awaitRelease()
                    } finally {
                        isPressed = false
                    }
                },
                onTap = {
                    onTap()
                }
            )
        }
}

/**
 * Harmonic breathing motion modifier for album art or hero cards when music is playing.
 */
fun Modifier.harmonicBreathing(
    isPlaying: Boolean,
    minScale: Float = 0.985f,
    maxScale: Float = 1.025f,
    durationMs: Int = 2200
): Modifier = composed {
    if (!isPlaying) return@composed this

    val infiniteTransition = rememberInfiniteTransition(label = "harmonic_breathing")
    val scale by infiniteTransition.animateFloat(
        initialValue = minScale,
        targetValue = maxScale,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = durationMs, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "breathing_scale"
    )

    this.graphicsLayer {
        scaleX = scale
        scaleY = scale
    }
}

/**
 * Dynamic velocity-adaptive entrance animation modifier for LazyColumn items.
 * When scrolling fast / flinging, items load and enter quickly and snappily without latency.
 * When scrolling slowly or browsing leisurely, items float in smoothly and gracefully with rich organic spring physics.
 */
fun Modifier.staggeredSpringEnter(
    index: Int,
    baseDelayMs: Int = 0,
    scrollSpeed: Float = 0f
): Modifier = composed {
    val isFastScroll = scrollSpeed > 0.40f
    var isVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        if (!isFastScroll && index <= 5 && baseDelayMs > 0) {
            delay((index * baseDelayMs).coerceAtMost(80).toLong())
        }
        isVisible = true
    }

    // Adaptive stiffness and damping based on scroll velocity
    val springStiffness = if (isFastScroll) Spring.StiffnessHigh else Spring.StiffnessMediumLow
    val dampingRatio = if (isFastScroll) Spring.DampingRatioNoBouncy else Spring.DampingRatioLowBouncy
    val startAlpha = if (isFastScroll) 0.85f else 0.20f
    val startTranslationY = if (isFastScroll) 4f else 16f

    val alpha by animateFloatAsState(
        targetValue = if (isVisible) 1f else startAlpha,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = springStiffness
        ),
        label = "staggered_alpha"
    )

    val translationY by animateFloatAsState(
        targetValue = if (isVisible) 0f else startTranslationY,
        animationSpec = spring(
            dampingRatio = dampingRatio,
            stiffness = springStiffness
        ),
        label = "staggered_translation_y"
    )

    this.graphicsLayer {
        this.alpha = alpha
        this.translationY = translationY
    }
}

/**
 * Spring wobble modifier on state change (e.g., favorite toggle, shuffle trigger).
 */
fun Modifier.springWobble(
    trigger: Any?
): Modifier = composed {
    var isWobbling by remember { mutableStateOf(false) }

    LaunchedEffect(trigger) {
        if (trigger != null) {
            isWobbling = true
            delay(350)
            isWobbling = false
        }
    }

    val rotationZ by animateFloatAsState(
        targetValue = if (isWobbling) 6f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioHighBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "spring_wobble"
    )

    this.graphicsLayer {
        this.rotationZ = rotationZ
    }
}
