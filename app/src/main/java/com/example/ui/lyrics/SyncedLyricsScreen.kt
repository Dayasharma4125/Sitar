package com.example.ui.lyrics

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.DragInteraction
import androidx.compose.foundation.interaction.MutableInteractionSource
import kotlinx.coroutines.Job
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Lyrics
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineBreak
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.LyricLine
import com.example.model.LyricsBackdropStyle
import com.example.model.LyricsScreenConfig
import com.example.model.LyricsTextAlignment
import com.example.model.LyricsUiState
import com.example.model.Track
import com.example.ui.components.AnimatedFavoriteButton
import com.example.ui.components.AnimatedM3IconButton
import com.example.ui.components.AnimatedPlayPauseHeroButton
import com.example.ui.components.AnimatedSkipButton
import com.example.ui.components.ArtworkImageOrFallback
import com.example.ui.components.GlassmorphicBackground
import com.example.ui.theme.OutfitFontFamily
import com.example.ui.theme.getFontFamilyForMusic
import com.example.ui.theme.springPressPhysics
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Locale

/**
 * Premium Full-Screen Synced Lyrics Screen.
 *
 * Immersive, distraction-free edge-to-edge typography presentation featuring:
 * 1. 80% default contrast for inactive lines for crystal-clear readability.
 * 2. Vibrant active line highlight (100% white + luminous bloom shadow).
 * 3. Decluttered interface: clean minimal floating back chevron and track pill.
 * 4. Dynamic Gaussian blur mesh backdrop pulling tints from album artwork.
 * 5. Full customizability via LyricsScreenConfig (text size, alignment, backdrop, mini deck).
 * 6. Word-by-word karaoke illumination & staggered slide-in entrance animations.
 */
@Composable
fun SyncedLyricsScreen(
    uiState: LyricsUiState,
    currentTrack: Track,
    onLyricClick: (Long) -> Unit,
    onBackClick: () -> Unit,
    onFavoriteToggle: () -> Unit,
    onPlayPauseClick: () -> Unit,
    onPreviousClick: () -> Unit,
    onNextClick: () -> Unit,
    onEditLyricsClick: () -> Unit = {},
    onMenuClick: () -> Unit = {},
    config: LyricsScreenConfig = LyricsScreenConfig(),
    blurIntensity: Float = 0.85f,
    isAmoledBlack: Boolean = false,
    modifier: Modifier = Modifier
) {
    BackHandler(onBack = onBackClick)

    val track = currentTrack

    val primaryAccent = remember(track.primaryAmbientColor) {
        val c = track.primaryAmbientColor
        if (c != Color.Unspecified && c.value != 0UL) Color(c.red, c.green, c.blue, 1f)
        else Color(0xFF64B5F6)
    }

    val secondaryAccent = remember(track.secondaryAmbientColor, primaryAccent) {
        val c = track.secondaryAmbientColor
        if (c != Color.Unspecified && c.value != 0UL) Color(c.red, c.green, c.blue, 1f)
        else Color(
            red = (primaryAccent.red * 0.7f + 0.3f).coerceIn(0f, 1f),
            green = (primaryAccent.green * 0.7f + 0.3f).coerceIn(0f, 1f),
            blue = (primaryAccent.blue * 0.7f + 0.3f).coerceIn(0f, 1f),
            alpha = 1f
        )
    }

    val animatedAccent by animateColorAsState(
        targetValue = primaryAccent,
        animationSpec = tween(durationMillis = 800),
        label = "lyricsAccentColor"
    )

    val animatedSecondary by animateColorAsState(
        targetValue = secondaryAccent,
        animationSpec = tween(durationMillis = 800),
        label = "lyricsSecondaryColor"
    )

    // Harmonic atmospheric aura transition
    val infiniteTransition = rememberInfiniteTransition(label = "lyricsAtmosphere")
    val harmonicPhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 6.28318f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 16000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "harmonicPhase"
    )

    val musicFontFamily = remember(track) {
        getFontFamilyForMusic(track)
    }

    val activeFontSize = config.textSize.activeSp
    val inactiveFontSize = config.textSize.inactiveSp

    Box(
        modifier = modifier
            .fillMaxSize()
            .testTag("synced_lyrics_screen")
    ) {
        // 1. Dynamic Full-Screen Backdrop Layer (Configurable Style)
        when (config.backdropStyle) {
            LyricsBackdropStyle.DYNAMIC_GAUSSIAN_BLUR -> {
                DynamicGaussianLyricsBackdrop(
                    track = track,
                    primaryAccent = animatedAccent,
                    secondaryAccent = animatedSecondary,
                    blurIntensity = config.blurIntensity * blurIntensity,
                    isAmoledBlack = isAmoledBlack,
                    harmonicPhase = harmonicPhase
                )
            }
            LyricsBackdropStyle.ORBITAL_AURA -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    animatedAccent.copy(alpha = if (isAmoledBlack) 0.35f else 0.55f),
                                    animatedSecondary.copy(alpha = if (isAmoledBlack) 0.20f else 0.35f),
                                    Color.Black
                                ),
                                center = Offset(300f, 400f),
                                radius = 900f
                            )
                        )
                )
            }
            LyricsBackdropStyle.MINIMAL_GRADIENT -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    animatedAccent.copy(alpha = if (isAmoledBlack) 0.20f else 0.35f),
                                    Color.Black.copy(alpha = 0.85f),
                                    Color.Black
                                )
                            )
                        )
                )
            }
            LyricsBackdropStyle.PURE_AMOLED_BLACK -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black)
                )
            }
        }

        // 2. Main Full-Screen Lyrics Content Area
        when (uiState) {
            is LyricsUiState.Loading -> {
                LyricsLoadingState(primaryAccent = animatedAccent)
            }
            is LyricsUiState.NotFound -> {
                LyricsNotFoundState(
                    message = uiState.message,
                    primaryAccent = animatedAccent,
                    onEditLyricsClick = onEditLyricsClick
                )
            }
            is LyricsUiState.Success -> {
                if (uiState.isSynced && uiState.lines.isNotEmpty()) {
                    SyncedLyricsKaraokeList(
                        lines = uiState.lines,
                        currentPlaybackPositionMs = uiState.currentPlaybackPositionMs,
                        isAutoScrollEnabled = uiState.isAutoScrollEnabled && config.autoScrollEnabled,
                        primaryAccent = animatedAccent,
                        activeFontSize = activeFontSize,
                        inactiveFontSize = inactiveFontSize,
                        inactiveContrast = config.inactiveTextContrast,
                        textAlignment = config.textAlignment,
                        karaokeWordFillEnabled = config.karaokeWordFillEnabled,
                        lyricGlowBloom = config.lyricGlowBloom,
                        staggeredSlideIn = config.staggeredSlideIn,
                        tapToSeekEnabled = config.tapToSeekEnabled,
                        fontFamily = musicFontFamily,
                        hasBottomDeck = config.showMiniTransportDeck,
                        onLyricClick = onLyricClick
                    )
                } else {
                    UnsyncedLyricsView(
                        plainLyrics = uiState.plainLyrics,
                        onEditLyricsClick = onEditLyricsClick,
                        primaryAccent = animatedAccent,
                        inactiveContrast = config.inactiveTextContrast,
                        textAlignment = config.textAlignment,
                        fontFamily = musicFontFamily
                    )
                }
            }
        }

        // 3. Subtle Infinity Vertical Gradient Fade Masks for smooth cinematic reading
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(110.dp)
                .align(Alignment.TopCenter)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = if (isAmoledBlack) 0.92f else 0.80f),
                            Color.Black.copy(alpha = 0.30f),
                            Color.Transparent
                        )
                    )
                )
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(130.dp)
                .align(Alignment.BottomCenter)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.40f),
                            Color.Black.copy(alpha = if (isAmoledBlack) 0.95f else 0.85f)
                        )
                    )
                )
        )

        // 4. Decluttered Minimalist Top Header (Clean Back Button + Song Pill)
        MinimalistLyricsHeader(
            track = track,
            isFavorite = (uiState as? LyricsUiState.Success)?.isFavorite ?: false,
            primaryAccent = animatedAccent,
            fontFamily = musicFontFamily,
            onFavoriteToggle = onFavoriteToggle,
            onCloseClick = onBackClick,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 6.dp)
        )

        // 5. Sleek Mini Floating Transport Deck (Optional via config)
        if (config.showMiniTransportDeck) {
            val isPlaying = (uiState as? LyricsUiState.Success)?.isPlaying ?: false
            val currentMs = (uiState as? LyricsUiState.Success)?.currentPlaybackPositionMs ?: 0L
            val durationMs = track.durationMs

            FloatingLyricsControls(
                isPlaying = isPlaying,
                currentPlaybackPositionMs = currentMs,
                durationMs = durationMs,
                primaryAccent = animatedAccent,
                onPlayPauseClick = onPlayPauseClick,
                onPreviousClick = onPreviousClick,
                onNextClick = onNextClick,
                onSeek = onLyricClick,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 8.dp)
            )
        }
    }
}

/**
 * Dynamic Gaussian Blur Backdrop layer pulling tint from artwork primary/secondary colors.
 */
@Composable
private fun DynamicGaussianLyricsBackdrop(
    track: Track,
    primaryAccent: Color,
    secondaryAccent: Color,
    blurIntensity: Float,
    isAmoledBlack: Boolean,
    harmonicPhase: Float
) {
    Box(modifier = Modifier.fillMaxSize()) {
        // Base Glassmorphic Artwork Layer
        GlassmorphicBackground(
            track = track,
            blurIntensity = blurIntensity,
            isAmoledBlack = isAmoledBlack
        )

        // Dynamic Gaussian Blurred Chroma Mesh Layer
        Box(
            modifier = Modifier
                .fillMaxSize()
                .blur(radius = (48.dp * blurIntensity.coerceAtLeast(0.5f)).coerceIn(24.dp, 64.dp))
                .drawBehind {
                    val w = size.width
                    val h = size.height

                    val orb1X = w * (0.35f + kotlin.math.sin(harmonicPhase.toDouble()).toFloat() * 0.16f)
                    val orb1Y = h * (0.24f + kotlin.math.cos(harmonicPhase.toDouble()).toFloat() * 0.12f)
                    val orb2X = w * (0.72f - kotlin.math.cos(harmonicPhase.toDouble()).toFloat() * 0.14f)
                    val orb2Y = h * (0.58f + kotlin.math.sin(harmonicPhase.toDouble()).toFloat() * 0.14f)
                    val orb3X = w * (0.42f + kotlin.math.cos((harmonicPhase * 0.7).toDouble()).toFloat() * 0.12f)
                    val orb3Y = h * (0.82f - kotlin.math.sin((harmonicPhase * 0.7).toDouble()).toFloat() * 0.10f)

                    val alphaFactor = if (isAmoledBlack) 0.44f else 0.65f

                    // Primary Radiant Gaussian Orb (Artwork Primary Color)
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                primaryAccent.copy(alpha = alphaFactor * 0.95f),
                                primaryAccent.copy(alpha = alphaFactor * 0.35f),
                                Color.Transparent
                            ),
                            center = Offset(orb1X, orb1Y),
                            radius = w * 0.95f
                        )
                    )

                    // Secondary Complementary Gaussian Orb (Artwork Secondary Color)
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                secondaryAccent.copy(alpha = alphaFactor * 0.85f),
                                secondaryAccent.copy(alpha = alphaFactor * 0.25f),
                                Color.Transparent
                            ),
                            center = Offset(orb2X, orb2Y),
                            radius = w * 0.88f
                        )
                    )

                    // Ambient Depth Gaussian Orb
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                primaryAccent.copy(alpha = alphaFactor * 0.50f),
                                Color.Transparent
                            ),
                            center = Offset(orb3X, orb3Y),
                            radius = w * 0.75f
                        )
                    )
                }
        )

        // Frosted Glass Scrim ensuring optimal high contrast for lyrics reading
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    if (isAmoledBlack) Color.Black.copy(alpha = 0.76f)
                    else Color.Black.copy(alpha = 0.52f)
                )
        )
    }
}

/**
 * Loading state with pulsating audio wave indicators and glowing typography.
 */
@Composable
private fun LyricsLoadingState(primaryAccent: Color) {
    val infiniteTransition = rememberInfiniteTransition(label = "lyricsLoadingTransition")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 100.dp, bottom = 140.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Surface(
                shape = CircleShape,
                color = primaryAccent.copy(alpha = 0.20f),
                border = BorderStroke(1.5.dp, primaryAccent.copy(alpha = 0.60f)),
                modifier = Modifier
                    .size(76.dp)
                    .graphicsLayer {
                        scaleX = pulseScale
                        scaleY = pulseScale
                    }
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.GraphicEq,
                        contentDescription = null,
                        tint = primaryAccent,
                        modifier = Modifier.size(38.dp)
                    )
                }
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Syncing Lyrics...",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-0.3).sp
                    ),
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Matching timestamps and audio telemetry",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.70f)
                )
            }
        }
    }
}

/**
 * Not-found state with friendly actionable card to write/import lyrics.
 */
@Composable
private fun LyricsNotFoundState(
    message: String,
    primaryAccent: Color,
    onEditLyricsClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
            .padding(top = 100.dp, bottom = 130.dp),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = Color.Black.copy(alpha = 0.60f),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.15f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(28.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Surface(
                    shape = CircleShape,
                    color = primaryAccent.copy(alpha = 0.18f),
                    border = BorderStroke(1.dp, primaryAccent.copy(alpha = 0.40f)),
                    modifier = Modifier.size(68.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Lyrics,
                            contentDescription = null,
                            tint = primaryAccent,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }

                Text(
                    text = message,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = Color.White,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "No synchronized lyrics found in local audio metadata or LRCLIB database.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.80f),
                    textAlign = TextAlign.Center,
                    lineHeight = 20.sp
                )

                Spacer(modifier = Modifier.height(6.dp))

                FilledTonalButton(
                    onClick = onEditLyricsClick,
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = primaryAccent,
                        contentColor = Color.White
                    ),
                    contentPadding = PaddingValues(horizontal = 24.dp, vertical = 14.dp),
                    modifier = Modifier.springPressPhysics()
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Search Online / Add Lyrics",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }
        }
    }
}

/**
 * Unsynced plain lyrics presentation with smooth vertical scrolling.
 */
@Composable
private fun UnsyncedLyricsView(
    plainLyrics: String?,
    onEditLyricsClick: () -> Unit,
    primaryAccent: Color,
    inactiveContrast: Float,
    textAlignment: LyricsTextAlignment,
    fontFamily: FontFamily
) {
    val scrollState = rememberScrollState()
    val align = when (textAlignment) {
        LyricsTextAlignment.LEFT -> TextAlign.Start
        LyricsTextAlignment.CENTER -> TextAlign.Center
        LyricsTextAlignment.RIGHT -> TextAlign.End
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 28.dp)
            .padding(top = 90.dp, bottom = 120.dp)
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "UNSYNCED LYRICS",
            fontFamily = fontFamily,
            style = MaterialTheme.typography.labelSmall.copy(
                letterSpacing = 1.5.sp,
                fontWeight = FontWeight.Bold
            ),
            color = primaryAccent.copy(alpha = 0.90f),
            textAlign = align,
            modifier = Modifier.fillMaxWidth()
        )

        Text(
            text = plainLyrics ?: "No lyric text content available",
            fontFamily = fontFamily,
            style = MaterialTheme.typography.headlineSmall.copy(
                lineHeight = 36.sp,
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold,
                textAlign = align
            ),
            color = Color.White.copy(alpha = inactiveContrast),
            modifier = Modifier.fillMaxWidth()
        )
    }
}

/**
 * Synchronized Karaoke List with Staggered Slide-In Entrance Animation,
 * auto-scroll with drag detection, and floating sync pill.
 */
@Composable
private fun SyncedLyricsKaraokeList(
    lines: List<LyricLine>,
    currentPlaybackPositionMs: Long,
    isAutoScrollEnabled: Boolean,
    primaryAccent: Color,
    activeFontSize: Float,
    inactiveFontSize: Float,
    inactiveContrast: Float,
    textAlignment: LyricsTextAlignment,
    karaokeWordFillEnabled: Boolean,
    lyricGlowBloom: Boolean,
    staggeredSlideIn: Boolean,
    tapToSeekEnabled: Boolean,
    fontFamily: FontFamily,
    hasBottomDeck: Boolean,
    onLyricClick: (Long) -> Unit
) {
    val lazyListState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    // Key to trigger staggered entrance whenever lines change or initial view mounts
    var entranceReady by remember(lines) { mutableStateOf(false) }
    LaunchedEffect(lines) {
        entranceReady = false
        delay(30)
        entranceReady = true
    }

    // Karaoke 0.5s (500ms) lead time offset so lyrics and word illumination stay synchronized and ahead of the song
    val karaokeLeadTimeMs = 500L
    val effectivePositionMs = currentPlaybackPositionMs + karaokeLeadTimeMs

    // Find the currently active lyric line
    val activeIndex by remember(lines, currentPlaybackPositionMs) {
        derivedStateOf {
            val index = lines.indexOfLast { it.timestampMs <= effectivePositionMs }
            if (index >= 0) index else 0
        }
    }

    // Detect user manual drag gesture vs programmatic auto-scroll
    var isUserScrolling by remember { mutableStateOf(false) }
    var resumeTimerJob by remember { mutableStateOf<Job?>(null) }

    LaunchedEffect(lazyListState.interactionSource) {
        lazyListState.interactionSource.interactions.collect { interaction ->
            when (interaction) {
                is DragInteraction.Start -> {
                    resumeTimerJob?.cancel()
                    isUserScrolling = true
                }
                is DragInteraction.Stop, is DragInteraction.Cancel -> {
                    resumeTimerJob?.cancel()
                    resumeTimerJob = coroutineScope.launch {
                        delay(3500)
                        isUserScrolling = false
                    }
                }
            }
        }
    }

    // Auto-scroll to center the active zoomed lyric line directly in the optical middle of the screen
    LaunchedEffect(activeIndex, isAutoScrollEnabled, isUserScrolling) {
        if (isAutoScrollEnabled && !isUserScrolling && lines.isNotEmpty() && activeIndex in lines.indices) {
            lazyListState.animateScrollToItem(
                index = activeIndex,
                scrollOffset = 0
            )
        }
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val density = androidx.compose.ui.platform.LocalDensity.current
        val halfHeightDp = with(density) { (maxHeight / 2f) }
        val topPadding = (halfHeightDp - 40.dp).coerceAtLeast(80.dp)
        val bottomPadding = (halfHeightDp - (if (hasBottomDeck) 64.dp else 24.dp)).coerceAtLeast(80.dp)

        LazyColumn(
            state = lazyListState,
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
                .testTag("lyrics_list"),
            contentPadding = PaddingValues(
                top = topPadding,
                bottom = bottomPadding
            ),
            verticalArrangement = Arrangement.spacedBy(22.dp)
        ) {
            itemsIndexed(
                items = lines,
                key = { index, item -> "${item.timestampMs}_$index" }
            ) { index, line ->
                val isActive = index == activeIndex

                // Calculate duration until next line to determine karaoke fill progress
                val nextLineTimestamp = lines.getOrNull(index + 1)?.timestampMs
                val lineDurationMs = remember(line.timestampMs, nextLineTimestamp) {
                    if (nextLineTimestamp != null && nextLineTimestamp > line.timestampMs) {
                        (nextLineTimestamp - line.timestampMs).coerceIn(800L, 12000L)
                    } else {
                        3500L
                    }
                }

                // Check for instrumental break before this line
                val previousLineTimestamp = lines.getOrNull(index - 1)?.timestampMs
                val instrumentalBreakDurationMs = remember(line.timestampMs, previousLineTimestamp) {
                    if (previousLineTimestamp != null && line.timestampMs - previousLineTimestamp > 7000L) {
                        line.timestampMs - previousLineTimestamp
                    } else 0L
                }

                val elapsedInLineMs = (effectivePositionMs - line.timestampMs).coerceAtLeast(0L)
                val rawProgress = (elapsedInLineMs.toFloat() / lineDurationMs).coerceIn(0f, 1f)
                val distanceFromActive = kotlin.math.abs(index - activeIndex)

                Column {
                    // Show Instrumental Gap Dots if applicable
                    if (instrumentalBreakDurationMs > 0L && index > 0) {
                        val isBreakActive = effectivePositionMs >= (previousLineTimestamp ?: 0L) &&
                                effectivePositionMs < line.timestampMs
                        InstrumentalBreakPill(
                            isActive = isBreakActive,
                            primaryAccent = primaryAccent,
                            modifier = Modifier.padding(vertical = 10.dp)
                        )
                    }

                    KaraokeLyricItem(
                        line = line,
                        index = index,
                        entranceReady = entranceReady,
                        isActive = isActive,
                        distanceFromActive = distanceFromActive,
                        karaokeProgress = if (isActive) rawProgress else 0f,
                        primaryAccent = primaryAccent,
                        activeFontSize = activeFontSize,
                        inactiveFontSize = inactiveFontSize,
                        inactiveContrast = inactiveContrast,
                        textAlignment = textAlignment,
                        karaokeWordFillEnabled = karaokeWordFillEnabled,
                        lyricGlowBloom = lyricGlowBloom,
                        staggeredSlideIn = staggeredSlideIn,
                        fontFamily = fontFamily,
                        onClick = {
                            if (tapToSeekEnabled) {
                                isUserScrolling = false
                                coroutineScope.launch {
                                    lazyListState.animateScrollToItem(index, 0)
                                }
                                onLyricClick(line.timestampMs)
                            }
                        }
                    )
                }
            }
        }

        // Floating "Center Lyrics in Focus" pill if user scrolled away
        AnimatedVisibility(
            visible = isUserScrolling,
            enter = fadeIn() + scaleIn(),
            exit = fadeOut() + scaleOut(),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = if (hasBottomDeck) 110.dp else 24.dp)
        ) {
            Surface(
                onClick = {
                    isUserScrolling = false
                    coroutineScope.launch {
                        lazyListState.animateScrollToItem(activeIndex, 0)
                    }
                },
                shape = RoundedCornerShape(20.dp),
                color = primaryAccent,
                tonalElevation = 8.dp,
                shadowElevation = 8.dp,
                modifier = Modifier.springPressPhysics()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 18.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Navigation,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "Center in Focus",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )
                }
            }
        }
    }
}

/**
 * Instrumental gap indicator with animated pulsing bars.
 */
@Composable
private fun InstrumentalBreakPill(
    isActive: Boolean,
    primaryAccent: Color,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "instrumentalPill")
    val alpha by infiniteTransition.animateFloat(
        initialValue = if (isActive) 0.40f else 0.20f,
        targetValue = if (isActive) 0.95f else 0.40f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1100, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "instrumentalAlpha"
    )

    Surface(
        shape = RoundedCornerShape(100.dp),
        color = Color.White.copy(alpha = 0.08f),
        border = BorderStroke(1.dp, if (isActive) primaryAccent.copy(alpha = 0.5f) else Color.White.copy(alpha = 0.12f)),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            repeat(3) { i ->
                Box(
                    modifier = Modifier
                        .size(5.dp)
                        .clip(CircleShape)
                        .background(
                            if (isActive) primaryAccent.copy(alpha = (alpha - i * 0.15f).coerceIn(0.2f, 1f))
                            else Color.White.copy(alpha = 0.4f)
                        )
                )
            }
            Text(
                text = "Instrumental Break",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                ),
                color = if (isActive) primaryAccent else Color.White.copy(alpha = 0.6f)
            )
        }
    }
}

/**
 * Single Karaoke Lyric line item with:
 * 1. Stable typography (no frame-by-frame font size thrashing) to eliminate scroll jitter.
 * 2. True word-by-word sequential karaoke lighting via FlowRow so multi-line lyrics illuminate in chronological order without whole-line horizontal overlap.
 * 3. 80% default contrast for high-contrast reading against background.
 * 4. Staggered Slide-In Entrance Animation for visual rhythm.
 * 5. Luminous Bloom text shadow matching primary artwork color.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun KaraokeLyricItem(
    line: LyricLine,
    index: Int = 0,
    entranceReady: Boolean = true,
    isActive: Boolean,
    distanceFromActive: Int = 0,
    karaokeProgress: Float,
    primaryAccent: Color = MaterialTheme.colorScheme.primary,
    activeFontSize: Float = 24f,
    inactiveFontSize: Float = 22f,
    inactiveContrast: Float = 0.80f,
    textAlignment: LyricsTextAlignment = LyricsTextAlignment.LEFT,
    karaokeWordFillEnabled: Boolean = true,
    lyricGlowBloom: Boolean = true,
    staggeredSlideIn: Boolean = true,
    track: Track? = null,
    fontFamily: FontFamily? = null,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isRtl = remember(line.text) { isRtlString(line.text) }

    // Staggered Slide-in Entrance Animation
    var lineAppeared by remember(line) { mutableStateOf(!staggeredSlideIn) }
    LaunchedEffect(entranceReady, line, staggeredSlideIn) {
        if (staggeredSlideIn) {
            if (entranceReady) {
                val staggerDelay = (index * 30).coerceAtMost(550).toLong()
                delay(staggerDelay)
                lineAppeared = true
            } else {
                lineAppeared = false
            }
        } else {
            lineAppeared = true
        }
    }

    val entranceProgress by animateFloatAsState(
        targetValue = if (lineAppeared) 1f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "staggeredSlideIn"
    )

    val slideOffsetYDp = if (staggeredSlideIn) (1f - entranceProgress) * 28f else 0f

    val targetAlpha = when {
        isActive -> 1.0f
        distanceFromActive == 1 -> inactiveContrast
        distanceFromActive == 2 -> (inactiveContrast * 0.90f).coerceAtLeast(0.55f)
        else -> (inactiveContrast * 0.80f).coerceAtLeast(0.45f)
    }

    val finalAlpha by animateFloatAsState(
        targetValue = (targetAlpha * entranceProgress).coerceIn(0f, 1f),
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "lyricAlpha"
    )

    val musicFontFamily = remember(track, fontFamily) {
        fontFamily ?: (track?.let { getFontFamilyForMusic(it) } ?: OutfitFontFamily)
    }

    val textAlign = when (textAlignment) {
        LyricsTextAlignment.LEFT -> TextAlign.Start
        LyricsTextAlignment.CENTER -> TextAlign.Center
        LyricsTextAlignment.RIGHT -> TextAlign.End
    }

    val flowArrangement = when (textAlignment) {
        LyricsTextAlignment.LEFT -> Arrangement.Start
        LyricsTextAlignment.CENTER -> Arrangement.Center
        LyricsTextAlignment.RIGHT -> Arrangement.End
    }

    // Split words for sequential word-by-word illumination
    val words = remember(line.text) {
        line.text.trim().split(Regex("\\s+")).filter { it.isNotEmpty() }
    }

    val wordProgressThresholds = remember(words) {
        val totalChars = words.sumOf { it.length }.coerceAtLeast(1)
        var cumulative = 0
        words.map { word ->
            val start = cumulative.toFloat() / totalChars
            cumulative += word.length
            val end = cumulative.toFloat() / totalChars
            start to end
        }
    }

    CompositionLocalProvider(
        LocalLayoutDirection provides if (isRtl) LayoutDirection.Rtl else LayoutDirection.Ltr
    ) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .graphicsLayer {
                    alpha = finalAlpha
                    translationY = slideOffsetYDp
                }
                .clip(RoundedCornerShape(16.dp))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = ripple(color = primaryAccent.copy(alpha = 0.25f)),
                    onClick = onClick
                )
                .padding(vertical = 4.dp, horizontal = 4.dp)
                .testTag("lyric_line_${line.timestampMs}")
        ) {
            if (isActive && karaokeWordFillEnabled && words.isNotEmpty()) {
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = flowArrangement,
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    val currentProgress = karaokeProgress.coerceIn(0f, 1f)
                    words.forEachIndexed { wordIdx, wordText ->
                        val (startProg, endProg) = wordProgressThresholds.getOrElse(wordIdx) { 0f to 1f }
                        val wordSpan = (endProg - startProg).coerceAtLeast(0.001f)
                        val wordLocalProgress = ((currentProgress - startProg) / wordSpan).coerceIn(0f, 1f)

                        val isWordPassed = wordLocalProgress >= 1f
                        val isWordSinging = wordLocalProgress > 0f && wordLocalProgress < 1f

                        val wordBrush = if (isWordSinging) {
                            val stop1 = (wordLocalProgress - 0.08f).coerceAtLeast(0f)
                            val stop2 = (wordLocalProgress + 0.08f).coerceAtMost(1f)
                            Brush.horizontalGradient(
                                colorStops = arrayOf(
                                    0.0f to Color.White,
                                    stop1 to Color.White,
                                    stop2 to Color.White.copy(alpha = inactiveContrast),
                                    1.0f to Color.White.copy(alpha = inactiveContrast)
                                )
                            )
                        } else null

                        val suffix = if (wordIdx < words.lastIndex) " " else ""
                        val displayText = "$wordText$suffix"

                        if (wordBrush != null) {
                            Text(
                                text = displayText,
                                fontFamily = musicFontFamily,
                                style = TextStyle(
                                    brush = wordBrush,
                                    fontSize = activeFontSize.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    lineHeight = (activeFontSize * 1.32f).sp,
                                    letterSpacing = (-0.2).sp,
                                    shadow = if (lyricGlowBloom) Shadow(
                                        color = primaryAccent.copy(alpha = 0.90f),
                                        offset = Offset(0f, 0f),
                                        blurRadius = 24f
                                    ) else null
                                )
                            )
                        } else {
                            Text(
                                text = displayText,
                                fontFamily = musicFontFamily,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontSize = activeFontSize.sp,
                                    fontWeight = if (isWordPassed) FontWeight.ExtraBold else FontWeight.Bold,
                                    lineHeight = (activeFontSize * 1.32f).sp,
                                    letterSpacing = (-0.2).sp,
                                    shadow = if (isWordPassed && lyricGlowBloom) Shadow(
                                        color = primaryAccent.copy(alpha = 0.90f),
                                        offset = Offset(0f, 0f),
                                        blurRadius = 24f
                                    ) else null
                                ),
                                color = if (isWordPassed) Color.White else Color.White.copy(alpha = inactiveContrast)
                            )
                        }
                    }
                }
            } else {
                val currentSize = if (isActive) activeFontSize else inactiveFontSize
                Text(
                    text = line.text,
                    fontFamily = musicFontFamily,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontSize = currentSize.sp,
                        fontWeight = if (isActive) FontWeight.ExtraBold else if (distanceFromActive <= 1) FontWeight.SemiBold else FontWeight.Medium,
                        lineHeight = (currentSize * 1.32f).sp,
                        letterSpacing = (-0.2).sp,
                        lineBreak = LineBreak.Paragraph,
                        textAlign = textAlign,
                        shadow = if (isActive && lyricGlowBloom) Shadow(
                            color = primaryAccent.copy(alpha = 0.90f),
                            offset = Offset(0f, 0f),
                            blurRadius = 24f
                        ) else null
                    ),
                    color = if (isActive) Color.White else Color.White.copy(alpha = targetAlpha),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

fun isRtlString(text: String): Boolean {
    for (char in text) {
        val directionality = Character.getDirectionality(char)
        if (directionality == Character.DIRECTIONALITY_RIGHT_TO_LEFT ||
            directionality == Character.DIRECTIONALITY_RIGHT_TO_LEFT_ARABIC) {
            return true
        } else if (directionality == Character.DIRECTIONALITY_LEFT_TO_RIGHT) {
            return false
        }
    }
    return false
}

/**
 * Decluttered Minimalist Top Header with subtle translucent dismiss button and song info pill.
 */
@Composable
private fun MinimalistLyricsHeader(
    track: Track,
    isFavorite: Boolean,
    primaryAccent: Color,
    fontFamily: FontFamily,
    onFavoriteToggle: () -> Unit,
    onCloseClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Dismiss / Collapse Chevron Pill
        Surface(
            onClick = onCloseClick,
            shape = CircleShape,
            color = Color.Black.copy(alpha = 0.45f),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.15f)),
            modifier = Modifier
                .size(42.dp)
                .springPressPhysics()
                .testTag("lyrics_header_close")
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = "Collapse Lyrics",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        // Center Track Info Pill
        Surface(
            shape = RoundedCornerShape(100.dp),
            color = Color.Black.copy(alpha = 0.40f),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.12f)),
            modifier = Modifier
                .padding(horizontal = 8.dp)
                .weight(1f, fill = false)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "${track.title.ifBlank { "Now Playing" }} • ${track.artist.ifBlank { "Unknown Artist" }}",
                    fontFamily = fontFamily,
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                    color = Color.White.copy(alpha = 0.90f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        // Favorite Button
        AnimatedFavoriteButton(
            isFavorite = isFavorite,
            activeColor = Color(0xFFFFD700),
            inactiveColor = Color.White.copy(alpha = 0.70f),
            size = 42.dp,
            iconSize = 22.dp,
            onToggle = onFavoriteToggle
        )
    }
}

/**
 * Sleek, non-intrusive floating glass mini transport deck.
 */
@Composable
private fun FloatingLyricsControls(
    isPlaying: Boolean,
    currentPlaybackPositionMs: Long,
    durationMs: Long,
    primaryAccent: Color,
    onPlayPauseClick: () -> Unit,
    onPreviousClick: () -> Unit,
    onNextClick: () -> Unit,
    onSeek: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(28.dp),
        color = Color.Black.copy(alpha = 0.60f),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.14f)),
        tonalElevation = 8.dp,
        shadowElevation = 10.dp,
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Mini Interactive Scrubber Track
            val safeDuration = durationMs.coerceAtLeast(1L)
            var isDragging by remember { mutableStateOf(false) }
            var dragFraction by remember { mutableFloatStateOf(0f) }

            val progressFraction = if (isDragging) dragFraction else (currentPlaybackPositionMs.toFloat() / safeDuration).coerceIn(0f, 1f)

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = formatTime(if (isDragging) (dragFraction * safeDuration).toLong() else currentPlaybackPositionMs),
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.75f),
                    fontSize = 11.sp
                )

                Slider(
                    value = progressFraction,
                    onValueChange = {
                        isDragging = true
                        dragFraction = it
                    },
                    onValueChangeFinished = {
                        isDragging = false
                        onSeek((dragFraction * safeDuration).toLong())
                    },
                    colors = SliderDefaults.colors(
                        thumbColor = primaryAccent,
                        activeTrackColor = primaryAccent,
                        inactiveTrackColor = Color.White.copy(alpha = 0.20f)
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 8.dp)
                        .height(24.dp)
                )

                Text(
                    text = formatTime(durationMs),
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.75f),
                    fontSize = 11.sp
                )
            }

            // Transport Control Buttons with tactile spring feedback
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                AnimatedSkipButton(
                    isNext = false,
                    tint = Color.White,
                    size = 44.dp,
                    iconSize = 26.dp,
                    onClick = onPreviousClick
                )

                Spacer(modifier = Modifier.width(14.dp))

                AnimatedPlayPauseHeroButton(
                    isPlaying = isPlaying,
                    primaryColor = primaryAccent,
                    onPrimaryColor = Color.White,
                    size = 54.dp,
                    iconSize = 30.dp,
                    onClick = onPlayPauseClick,
                    testTag = "lyrics_play_pause_button"
                )

                Spacer(modifier = Modifier.width(14.dp))

                AnimatedSkipButton(
                    isNext = true,
                    tint = Color.White,
                    size = 44.dp,
                    iconSize = 26.dp,
                    onClick = onNextClick
                )
            }
        }
    }
}

private fun formatTime(ms: Long): String {
    val totalSeconds = (ms / 1000).coerceAtLeast(0)
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format(Locale.getDefault(), "%d:%02d", minutes, seconds)
}
