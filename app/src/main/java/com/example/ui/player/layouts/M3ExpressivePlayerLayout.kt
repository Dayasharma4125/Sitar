package com.example.ui.player.layouts

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.DragInteraction
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import kotlinx.coroutines.Job
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
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
import androidx.compose.material.icons.automirrored.filled.VolumeDown
import androidx.compose.material.icons.automirrored.filled.VolumeMute
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DashboardCustomize
import androidx.compose.material.icons.filled.Equalizer
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.HighQuality
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Lyrics
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.QueueMusic
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.RepeatOne
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.Speaker
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import com.example.ui.theme.ensureVibrantDarkAccent
import com.example.ui.theme.springPressPhysics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AudioDeviceType
import com.example.model.LyricsUiState
import com.example.model.M3ArtworkShape
import com.example.model.M3ArtworkSize
import com.example.model.M3BackgroundStyle
import com.example.model.M3ControlButtonStyle
import com.example.model.M3ElementPlacement
import com.example.model.M3ExpressivePlayerConfig
import com.example.model.M3HeroButtonStyle
import com.example.model.M3InfoAlignment
import com.example.model.M3MotionProfile
import com.example.model.M3PlayerStyle
import com.example.model.M3ScrubberStyle
import com.example.model.PlayerUiState
import com.example.ui.components.ArtworkImageOrFallback
import com.example.ui.components.AudioWaveformVisualizer
import com.example.ui.components.GlassmorphicBackground
import com.example.ui.components.M3ExpressiveProgressBar
import com.example.ui.components.rememberAlbumArtGlassPalette
import com.example.ui.theme.ExpressiveShapes
import com.example.ui.theme.harmonicBreathing
import com.example.ui.theme.springPressPhysics
import com.example.ui.theme.springWobble
import com.example.ui.util.HapticType
import com.example.ui.util.rememberHapticController
import java.util.Locale
import kotlin.math.PI
import kotlin.math.sin

/**
 * Material 3 Expressive Player Screen Layout.
 *
 * Implements Google's latest Material 3 Expressive design paradigm:
 * - Fluid, playful shapes (dynamic squircle, blossom petal, arch dome, vinyl)
 * - Wavy expressive progress scrubber responding dynamically to music playback
 * - Morphing expressive play/pause hero button with tactile spring feedback
 * - Expressive pill action rows and output routing capsules
 * - Live real-time audio waveform spectrum
 * - Lossless Hi-Res audio badges
 * - Modular architecture: every element is fully customizable by the user
 * - Quick live-customizer sheet to toggle and adjust components directly from the screen
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun M3ExpressivePlayerLayout(
    uiState: PlayerUiState,
    lyricsUiState: LyricsUiState,
    config: M3ExpressivePlayerConfig,
    onConfigChange: (M3ExpressivePlayerConfig) -> Unit,
    onPlayPauseClick: () -> Unit,
    onPreviousClick: () -> Unit,
    onNextClick: () -> Unit,
    onSeekTo: (Long) -> Unit,
    onFavoriteToggle: () -> Unit,
    onVolumeChange: (Float) -> Unit,
    onLyricsToggle: () -> Unit,
    onCollapseClick: () -> Unit,
    onDevicePickerClick: () -> Unit,
    onEqualizerClick: () -> Unit,
    onQueueClick: () -> Unit,
    onShuffleToggle: () -> Unit = {},
    onRepeatToggle: () -> Unit = {},
    onSleepTimerClick: () -> Unit = {},
    onPlaybackSpeedClick: () -> Unit = {},
    onOpenSettings: () -> Unit = {},
    onMenuClick: () -> Unit = {},
    blurIntensity: Float = 0.85f,
    isAmoledBlack: Boolean = false,
    showAudioDetails: Boolean = false,
    isGlassmorphicEffectEnabled: Boolean = true,
    showMusicVisualizer: Boolean = true,
    waveformVisualizerStyle: String = "NEON_BARS",
    onWaveformVisualizerStyleChange: ((String) -> Unit)? = null,
    lyricsConfig: com.example.model.LyricsScreenConfig = com.example.model.LyricsScreenConfig(),
    modifier: Modifier = Modifier
) {
    val track = uiState.currentTrack
    val hapticController = rememberHapticController()
    var isMenuExpanded by remember { mutableStateOf(false) }
    var showCustomizerSheet by remember { mutableStateOf(false) }

    // Dynamic color theming derived from artwork & Material Theme with dark-artwork visibility protection
    val glassPalette = rememberAlbumArtGlassPalette(track = track, isAmoledBlack = isAmoledBlack)
    val accentPrimary = remember(track.primaryAmbientColor) {
        ensureVibrantDarkAccent(track.primaryAmbientColor, fallback = Color(0xFFA8C8FF))
    }
    val accentSecondary = remember(track.secondaryAmbientColor) {
        ensureVibrantDarkAccent(track.secondaryAmbientColor, fallback = Color(0xFFBDC7DC))
    }

    val isDarkAmoled = isAmoledBlack || config.backgroundStyle == M3BackgroundStyle.AMOLED_BLACK

    val containerBg = when (config.backgroundStyle) {
        M3BackgroundStyle.AMOLED_BLACK -> Color(0xFF000000)
        M3BackgroundStyle.DYNAMIC_TONAL -> MaterialTheme.colorScheme.surface
        M3BackgroundStyle.AMBIENT_AURA -> Color(0xFF0F111A)
        M3BackgroundStyle.GLASSMORPHIC_FROST -> Color.Transparent
        M3BackgroundStyle.ORBITAL_NEBULA -> Color(0xFF070B14)
        M3BackgroundStyle.MESH_AURORA_GLOW -> Color(0xFF050811)
        M3BackgroundStyle.METALLIC_BRUSHED -> Color(0xFF14171E)
    }

    val textColor = when {
        isDarkAmoled || config.backgroundStyle == M3BackgroundStyle.GLASSMORPHIC_FROST ||
                config.backgroundStyle == M3BackgroundStyle.AMBIENT_AURA ||
                config.backgroundStyle == M3BackgroundStyle.ORBITAL_NEBULA ||
                config.backgroundStyle == M3BackgroundStyle.MESH_AURORA_GLOW ||
                config.backgroundStyle == M3BackgroundStyle.METALLIC_BRUSHED -> Color.White
        else -> MaterialTheme.colorScheme.onSurface
    }

    val textSecondary = textColor.copy(alpha = 0.65f)

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .testTag("m3_expressive_player_screen")
    ) {
        val screenHeight = maxHeight
        val scrollState = rememberScrollState()
        // 1. Background Layer
        when (config.backgroundStyle) {
            M3BackgroundStyle.GLASSMORPHIC_FROST -> {
                GlassmorphicBackground(
                    track = track,
                    blurIntensity = blurIntensity,
                    isAmoledBlack = isAmoledBlack,
                    modifier = Modifier.fillMaxSize()
                )
            }
            M3BackgroundStyle.AMBIENT_AURA -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFF08090E))
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    accentPrimary.copy(alpha = 0.45f * config.auraGlowStrength),
                                    accentSecondary.copy(alpha = 0.20f * config.auraGlowStrength),
                                    Color.Transparent
                                ),
                                center = Offset(size.width * 0.5f, size.height * 0.35f),
                                radius = size.width * 0.85f
                            )
                        )
                    }
                }
            }
            M3BackgroundStyle.ORBITAL_NEBULA -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFF07070B))
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        drawCircle(
                            brush = Brush.radialGradient(
                                listOf(accentPrimary.copy(alpha = 0.50f * config.auraGlowStrength), Color.Transparent),
                                center = Offset(size.width * 0.25f, size.height * 0.25f),
                                radius = size.width * 0.70f
                            )
                        )
                        drawCircle(
                            brush = Brush.radialGradient(
                                listOf(accentSecondary.copy(alpha = 0.40f * config.auraGlowStrength), Color.Transparent),
                                center = Offset(size.width * 0.75f, size.height * 0.65f),
                                radius = size.width * 0.75f
                            )
                        )
                    }
                }
            }
            M3BackgroundStyle.MESH_AURORA_GLOW -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.linearGradient(
                                listOf(
                                    accentPrimary.copy(alpha = 0.35f * config.auraGlowStrength),
                                    Color(0xFF1E1B4B),
                                    accentSecondary.copy(alpha = 0.25f * config.auraGlowStrength),
                                    Color(0xFF0F172A)
                                )
                            )
                        )
                )
            }
            M3BackgroundStyle.METALLIC_BRUSHED -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    Color(0xFF27272A),
                                    Color(0xFF18181B),
                                    Color(0xFF09090B)
                                )
                            )
                        )
                )
            }
            M3BackgroundStyle.AMOLED_BLACK -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black)
                )
            }
            M3BackgroundStyle.DYNAMIC_TONAL -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    accentPrimary.copy(alpha = 0.22f),
                                    MaterialTheme.colorScheme.surface,
                                    MaterialTheme.colorScheme.surfaceContainerLowest
                                )
                            )
                        )
                )
            }
        }

        // 2. Main Content Layout (Scrollable on overflow to prevent screen destruction)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // TOP BAR
            M3ExpressiveTopBar(
                activeDeviceName = uiState.activeDevice.name,
                deviceType = uiState.activeDevice.type,
                showDeviceCapsule = config.showDeviceCapsule,
                onCollapseClick = {
                    if (config.expressiveHaptics) hapticController.performHaptic(HapticType.LIGHT_CLICK)
                    onCollapseClick()
                },
                onDevicePickerClick = {
                    if (config.expressiveHaptics) hapticController.performHaptic(HapticType.LIGHT_CLICK)
                    onDevicePickerClick()
                },
                onCustomizeClick = {
                    if (config.expressiveHaptics) hapticController.performHaptic(HapticType.MEDIUM_CLICK)
                    showCustomizerSheet = true
                },
                onMenuClick = { isMenuExpanded = true },
                isMenuExpanded = isMenuExpanded,
                onDismissMenu = { isMenuExpanded = false },
                onEqualizerClick = onEqualizerClick,
                onSleepTimerClick = onSleepTimerClick,
                onSpeedClick = onPlaybackSpeedClick,
                onOpenSettings = onOpenSettings,
                accentColor = accentPrimary,
                textColor = textColor
            )

            Spacer(modifier = Modifier.height(10.dp))

            // REUSABLE SECTION COMPOSABLES FOR CUSTOMIZABLE ELEMENT PLACEMENT
            val renderArtwork: @Composable () -> Unit = {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (uiState.isLyricsVisible) {
                        val lyricsHeight = when {
                            screenHeight < 680.dp -> 260.dp
                            screenHeight < 800.dp -> 310.dp
                            else -> 350.dp
                        }
                        M3ExpressiveInPlaceLyrics(
                            lyricsUiState = lyricsUiState,
                            currentPositionMs = uiState.currentPositionMs,
                            onSeekTo = onSeekTo,
                            onCloseLyrics = onLyricsToggle,
                            accentColor = accentPrimary,
                            textColor = textColor,
                            lyricsConfig = lyricsConfig,
                            haptics = config.expressiveHaptics,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(lyricsHeight)
                        )
                    } else {
                        M3ExpressiveArtwork(
                            track = track,
                            shapeType = config.artworkShape,
                            sizeType = config.artworkSize,
                            isPlaying = uiState.isPlaying,
                            accentColor = accentPrimary,
                            maxAvailableHeight = screenHeight,
                            onClick = {
                                if (config.expressiveHaptics) hapticController.performHaptic(HapticType.LIGHT_CLICK)
                                onLyricsToggle()
                            }
                        )
                    }
                }
            }

            val renderHeader: @Composable () -> Unit = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (config.showAudioSpecs && track.durationMs > 0) {
                        M3ExpressiveAudioBadge(
                            track = track,
                            accentColor = accentPrimary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                    M3ExpressiveTrackHeader(
                        track = track,
                        isFavorite = uiState.isFavorite,
                        showFavorite = config.showFavoriteButton,
                        showDetails = config.showTrackDetails,
                        infoAlignment = config.infoAlignment,
                        onFavoriteToggle = {
                            if (config.expressiveHaptics) hapticController.performHaptic(HapticType.MEDIUM_CLICK)
                            onFavoriteToggle()
                        },
                        textColor = textColor,
                        textSecondary = textSecondary,
                        accentColor = accentPrimary
                    )
                }
            }

            val renderVisualizer: @Composable () -> Unit = {
                if (config.showVisualizer && showMusicVisualizer) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(34.dp)
                            .padding(horizontal = 16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        val vizStyle = try {
                            com.example.ui.components.WaveformVisualizerStyle.valueOf(waveformVisualizerStyle)
                        } catch (e: Exception) {
                            com.example.ui.components.WaveformVisualizerStyle.NEON_BARS
                        }
                        AudioWaveformVisualizer(
                            isPlaying = uiState.isPlaying,
                            volume = uiState.volume,
                            primaryColor = accentPrimary,
                            secondaryColor = accentSecondary,
                            initialStyle = vizStyle
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                }
            }

            val renderScrubber: @Composable () -> Unit = {
                M3ExpressiveScrubber(
                    currentPositionMs = uiState.currentPositionMs,
                    durationMs = uiState.durationMs,
                    isPlaying = uiState.isPlaying,
                    style = config.scrubberStyle,
                    accentColor = accentPrimary,
                    textColor = textColor,
                    onSeekTo = { posMs ->
                        if (config.expressiveHaptics) hapticController.performHaptic(HapticType.LIGHT_CLICK)
                        onSeekTo(posMs)
                    }
                )
            }

            val renderTransport: @Composable () -> Unit = {
                M3ExpressiveTransportControls(
                    isPlaying = uiState.isPlaying,
                    isShuffle = uiState.isShuffle,
                    isRepeat = uiState.isRepeat,
                    heroButtonStyle = config.heroButtonStyle,
                    controlButtonStyle = config.controlButtonStyle,
                    buttonGlowEffect = config.buttonGlowEffect,
                    showShuffleRepeat = config.showShuffleRepeat,
                    onPlayPauseClick = {
                        if (config.expressiveHaptics) hapticController.performHaptic(HapticType.MEDIUM_CLICK)
                        onPlayPauseClick()
                    },
                    onPreviousClick = {
                        if (config.expressiveHaptics) hapticController.performHaptic(HapticType.LIGHT_CLICK)
                        onPreviousClick()
                    },
                    onNextClick = {
                        if (config.expressiveHaptics) hapticController.performHaptic(HapticType.LIGHT_CLICK)
                        onNextClick()
                    },
                    onShuffleToggle = {
                        if (config.expressiveHaptics) hapticController.performHaptic(HapticType.LIGHT_CLICK)
                        onShuffleToggle()
                    },
                    onRepeatToggle = {
                        if (config.expressiveHaptics) hapticController.performHaptic(HapticType.LIGHT_CLICK)
                        onRepeatToggle()
                    },
                    accentColor = accentPrimary,
                    textColor = textColor
                )
            }

            val renderVolume: @Composable () -> Unit = {
                if (config.showVolumeSlider) {
                    Spacer(modifier = Modifier.height(10.dp))
                    M3ExpressiveVolumeBar(
                        volume = uiState.volume,
                        onVolumeChange = { vol ->
                            onVolumeChange(vol)
                        },
                        accentColor = accentPrimary,
                        textColor = textColor
                    )
                }
            }

            val renderPills: @Composable () -> Unit = {
                if (config.showActionPills) {
                    Spacer(modifier = Modifier.height(12.dp))
                    M3ExpressiveActionPillsRow(
                        queueCount = uiState.queueCount,
                        isLyricsActive = uiState.isLyricsVisible,
                        onQueueClick = onQueueClick,
                        onLyricsClick = onLyricsToggle,
                        onSleepTimerClick = onSleepTimerClick,
                        onSpeedClick = onPlaybackSpeedClick,
                        onEqualizerClick = onEqualizerClick,
                        accentColor = accentPrimary,
                        textColor = textColor
                    )
                }
            }

            val renderLyricsPeek: @Composable () -> Unit = {
                if (config.showLyricsCard && !uiState.isLyricsVisible && lyricsUiState is LyricsUiState.Success) {
                    Spacer(modifier = Modifier.height(10.dp))
                    M3ExpressiveLyricsPeekCard(
                        lyricsState = lyricsUiState,
                        currentPositionMs = uiState.currentPositionMs,
                        onCardClick = onLyricsToggle,
                        accentColor = accentPrimary,
                        textColor = textColor
                    )
                }
            }

            // ORDER SECTIONS ACCORDING TO USER'S SELECTED M3 ELEMENT PLACEMENT
            when (config.elementPlacement) {
                com.example.model.M3ElementPlacement.BALANCED_DEFAULT -> {
                    renderArtwork()
                    Spacer(modifier = Modifier.height(12.dp))
                    renderVisualizer()
                    renderHeader()
                    Spacer(modifier = Modifier.height(12.dp))
                    renderScrubber()
                    Spacer(modifier = Modifier.height(14.dp))
                    renderTransport()
                    renderVolume()
                    renderPills()
                    renderLyricsPeek()
                }

                com.example.model.M3ElementPlacement.THUMB_FRIENDLY_BOTTOM -> {
                    renderArtwork()
                    Spacer(modifier = Modifier.height(10.dp))
                    renderHeader()
                    renderVisualizer()
                    Spacer(modifier = Modifier.height(8.dp))
                    renderScrubber()
                    renderPills()
                    renderVolume()
                    Spacer(modifier = Modifier.height(14.dp))
                    renderTransport()
                    renderLyricsPeek()
                }

                com.example.model.M3ElementPlacement.CONTROLS_FIRST_TOP -> {
                    renderHeader()
                    Spacer(modifier = Modifier.height(10.dp))
                    renderScrubber()
                    Spacer(modifier = Modifier.height(12.dp))
                    renderTransport()
                    Spacer(modifier = Modifier.height(14.dp))
                    renderArtwork()
                    renderVisualizer()
                    renderVolume()
                    renderPills()
                    renderLyricsPeek()
                }

                com.example.model.M3ElementPlacement.COMPACT_SPLIT_CARDS -> {
                    Surface(
                        shape = RoundedCornerShape(26.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.55f),
                        border = BorderStroke(1.dp, textColor.copy(alpha = 0.12f)),
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            renderArtwork()
                            Spacer(modifier = Modifier.height(8.dp))
                            renderHeader()
                            renderVisualizer()
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Surface(
                        shape = RoundedCornerShape(26.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.55f),
                        border = BorderStroke(1.dp, textColor.copy(alpha = 0.12f)),
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            renderScrubber()
                            Spacer(modifier = Modifier.height(12.dp))
                            renderTransport()
                            renderVolume()
                            renderPills()
                            renderLyricsPeek()
                        }
                    }
                }

                com.example.model.M3ElementPlacement.EXPANDED_LYRICS_HERO -> {
                    if (lyricsUiState is LyricsUiState.Success) {
                        M3ExpressiveInPlaceLyrics(
                            lyricsUiState = lyricsUiState,
                            currentPositionMs = uiState.currentPositionMs,
                            onSeekTo = onSeekTo,
                            onCloseLyrics = onLyricsToggle,
                            accentColor = accentPrimary,
                            textColor = textColor,
                            lyricsConfig = lyricsConfig,
                            haptics = config.expressiveHaptics,
                            modifier = Modifier.fillMaxWidth().height(280.dp)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                    } else {
                        renderArtwork()
                        Spacer(modifier = Modifier.height(10.dp))
                    }
                    renderHeader()
                    renderVisualizer()
                    Spacer(modifier = Modifier.height(8.dp))
                    renderScrubber()
                    Spacer(modifier = Modifier.height(12.dp))
                    renderTransport()
                    renderVolume()
                    renderPills()
                }
            }

            Spacer(modifier = Modifier.height(28.dp))
        }

        // 3. LIVE CUSTOMIZER MODAL BOTTOM SHEET
        if (showCustomizerSheet) {
            M3ExpressiveCustomizerSheet(
                config = config,
                onConfigChange = onConfigChange,
                onDismiss = { showCustomizerSheet = false },
                accentColor = accentPrimary
            )
        }
    }
}

// -------------------------------------------------------------------------------------------------
// SUB-COMPONENTS
// -------------------------------------------------------------------------------------------------

/**
 * Top Navigation Bar with Back, Audio Device Pill, Customize Button, and Overflow Menu.
 */
@Composable
private fun M3ExpressiveTopBar(
    activeDeviceName: String,
    deviceType: AudioDeviceType,
    showDeviceCapsule: Boolean,
    onCollapseClick: () -> Unit,
    onDevicePickerClick: () -> Unit,
    onCustomizeClick: () -> Unit,
    onMenuClick: () -> Unit,
    isMenuExpanded: Boolean,
    onDismissMenu: () -> Unit,
    onEqualizerClick: () -> Unit,
    onSleepTimerClick: () -> Unit,
    onSpeedClick: () -> Unit,
    onOpenSettings: () -> Unit,
    accentColor: Color,
    textColor: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Collapse Icon Button
        Surface(
            onClick = onCollapseClick,
            shape = CircleShape,
            color = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.6f),
            modifier = Modifier.size(44.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = "Collapse Player",
                    tint = textColor,
                    modifier = Modifier.size(26.dp)
                )
            }
        }

        // Device Capsule or Title
        if (showDeviceCapsule) {
            Surface(
                onClick = onDevicePickerClick,
                shape = CircleShape,
                color = accentColor.copy(alpha = 0.16f),
                border = BorderStroke(1.dp, accentColor.copy(alpha = 0.35f)),
                modifier = Modifier
                    .height(36.dp)
                    .testTag("device_picker_capsule")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val deviceIcon = when (deviceType) {
                        AudioDeviceType.BLUETOOTH -> Icons.Default.Bluetooth
                        AudioDeviceType.WIRED_HEADPHONES,
                        AudioDeviceType.USB_DAC -> Icons.Default.Headphones
                        else -> Icons.Default.Speaker
                    }
                    Icon(
                        imageVector = deviceIcon,
                        contentDescription = "Audio Route",
                        tint = accentColor,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = activeDeviceName,
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = textColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        } else {
            Text(
                text = "NOW PLAYING",
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp
                ),
                color = textColor.copy(alpha = 0.7f)
            )
        }

        // Actions: Customize Button + Menu
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // Live Player Customizer Trigger
            Surface(
                onClick = onCustomizeClick,
                shape = CircleShape,
                color = accentColor.copy(alpha = 0.22f),
                border = BorderStroke(1.dp, accentColor.copy(alpha = 0.45f)),
                modifier = Modifier
                    .size(40.dp)
                    .testTag("customize_player_button")
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.DashboardCustomize,
                        contentDescription = "Customize M3 Expressive Player",
                        tint = accentColor,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // Overflow Menu Button
            Box {
                Surface(
                    onClick = onMenuClick,
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.6f),
                    modifier = Modifier.size(40.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "More Options",
                            tint = textColor,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                DropdownMenu(
                    expanded = isMenuExpanded,
                    onDismissRequest = onDismissMenu
                ) {
                    DropdownMenuItem(
                        text = { Text("Equalizer") },
                        leadingIcon = { Icon(Icons.Default.Equalizer, contentDescription = null) },
                        onClick = {
                            onDismissMenu()
                            onEqualizerClick()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Sleep Timer") },
                        leadingIcon = { Icon(Icons.Default.Bedtime, contentDescription = null) },
                        onClick = {
                            onDismissMenu()
                            onSleepTimerClick()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Playback Speed") },
                        leadingIcon = { Icon(Icons.Default.Speed, contentDescription = null) },
                        onClick = {
                            onDismissMenu()
                            onSpeedClick()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Player & App Settings") },
                        leadingIcon = { Icon(Icons.Default.Settings, contentDescription = null) },
                        onClick = {
                            onDismissMenu()
                            onOpenSettings()
                        }
                    )
                }
            }
        }
    }
}

/**
 * Expressive Artwork Canvas supporting squircle, blossom petal, arch dome, and vinyl disc.
 */
@Composable
private fun M3ExpressiveArtwork(
    track: com.example.model.Track,
    shapeType: M3ArtworkShape,
    sizeType: M3ArtworkSize,
    isPlaying: Boolean,
    accentColor: Color,
    maxAvailableHeight: Dp = 800.dp,
    onClick: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "vinylSpin")
    val rotationAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 18000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "vinylAngle"
    )

    val targetSizeDp: Dp = when (sizeType) {
        M3ArtworkSize.EXPANSIVE_HERO -> if (maxAvailableHeight < 700.dp) 240.dp else 290.dp
        M3ArtworkSize.BALANCED -> if (maxAvailableHeight < 700.dp) 210.dp else 250.dp
        M3ArtworkSize.COMPACT -> if (maxAvailableHeight < 700.dp) 175.dp else 205.dp
    }

    val artworkShape: Shape = when (shapeType) {
        M3ArtworkShape.EXPRESSIVE_SQUIRCLE -> ExpressiveShapes.HeroSquircle
        M3ArtworkShape.SUPER_ELLIPSE -> ExpressiveShapes.SuperellipseCard
        M3ArtworkShape.ORGANIC_CLOVER -> ExpressiveShapes.CloverShape(0.38f)
        M3ArtworkShape.HEXAGON_HIRES -> ExpressiveShapes.HexagonalBadgeShape()
        M3ArtworkShape.OCTAGON_FACET -> ExpressiveShapes.OctagonBadgeShape()
        M3ArtworkShape.CURVED_ARCH -> RoundedCornerShape(topStart = 40.dp, topEnd = 40.dp, bottomStart = 16.dp, bottomEnd = 16.dp)
        M3ArtworkShape.TICKET_NOTCH -> ExpressiveShapes.TicketShape(16f)
        M3ArtworkShape.DIAMOND_CUT -> ExpressiveShapes.DiamondCutCard
        M3ArtworkShape.ROUNDED_SQUARE -> RoundedCornerShape(20.dp)
        M3ArtworkShape.VINYL_RECORD -> CircleShape
        M3ArtworkShape.STARBURST_12 -> ExpressiveShapes.StarburstShape()
        M3ArtworkShape.STADIUM_PILL -> ExpressiveShapes.Stadium
        M3ArtworkShape.ARCH_PORTAL -> ExpressiveShapes.ArchPortalShape()
        M3ArtworkShape.TEARDROP_FLUID -> ExpressiveShapes.TeardropFluidShape()
        M3ArtworkShape.SHIELD_HERALD -> ExpressiveShapes.ShieldCrestShape()
    }

    val scaleFactor by animateFloatAsState(
        targetValue = if (isPlaying) 1.0f else 0.94f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "artworkScale"
    )

    Box(
        modifier = Modifier
            .size(targetSizeDp)
            .scale(scaleFactor)
            .shadow(
                elevation = if (isPlaying) 20.dp else 10.dp,
                shape = artworkShape,
                spotColor = accentColor.copy(alpha = 0.5f),
                ambientColor = accentColor.copy(alpha = 0.25f)
            )
            .clip(artworkShape)
            .clickable(onClick = onClick)
            .testTag("expressive_artwork_card"),
        contentAlignment = Alignment.Center
    ) {
        ArtworkImageOrFallback(
            track = track,
            modifier = Modifier
                .fillMaxSize()
                .then(
                    if (shapeType == M3ArtworkShape.VINYL_RECORD && isPlaying) {
                        Modifier.rotate(rotationAngle)
                    } else Modifier
                )
        )

        // Subtle gloss rim overlay
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.25f)
                        )
                    )
                )
        )

        // Center vinyl hole detail if vinyl disc shape
        if (shapeType == M3ArtworkShape.VINYL_RECORD) {
            Surface(
                shape = CircleShape,
                color = Color.Black.copy(alpha = 0.90f),
                border = BorderStroke(2.dp, accentColor),
                modifier = Modifier.size(36.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(Color.White)
                    )
                }
            }
        }
    }
}

/**
 * Hi-Res Lossless Audio Specs Badge.
 */
@Composable
private fun M3ExpressiveAudioBadge(
    track: com.example.model.Track,
    accentColor: Color
) {
    val qualityString = buildString {
        append(track.formatName.ifBlank { "FLAC" })
        if (track.bitDepth > 0) append(" • ${track.bitDepth}-bit")
        if (track.sampleRateHz > 0) append(" / ${track.sampleRateHz / 1000}kHz")
        if (track.bitrateKbps > 0) append(" • ${track.bitrateKbps}kbps")
    }

    Surface(
        shape = CircleShape,
        color = accentColor.copy(alpha = 0.14f),
        border = BorderStroke(0.8.dp, accentColor.copy(alpha = 0.35f)),
        modifier = Modifier.testTag("audio_specs_badge")
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = Icons.Default.HighQuality,
                contentDescription = "Hi-Res Audio",
                tint = accentColor,
                modifier = Modifier.size(15.dp)
            )
            Text(
                text = qualityString,
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = accentColor
            )
        }
    }
}

/**
 * Track Header with Title, Artist & Album, and Favorite Heart button with configurable alignment.
 */
@Composable
private fun M3ExpressiveTrackHeader(
    track: com.example.model.Track,
    isFavorite: Boolean,
    showFavorite: Boolean,
    showDetails: Boolean,
    infoAlignment: com.example.model.M3InfoAlignment = com.example.model.M3InfoAlignment.LEFT,
    onFavoriteToggle: () -> Unit,
    textColor: Color,
    textSecondary: Color,
    accentColor: Color
) {
    when (infoAlignment) {
        com.example.model.M3InfoAlignment.CENTER -> {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = track.title.ifBlank { "No Track Selected" },
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = (-0.5).sp
                        ),
                        color = textColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.Center
                    )
                    if (showFavorite) {
                        Spacer(modifier = Modifier.width(6.dp))
                        IconButton(
                            onClick = onFavoriteToggle,
                            modifier = Modifier.size(36.dp).testTag("favorite_button")
                        ) {
                            Icon(
                                imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Outlined.FavoriteBorder,
                                contentDescription = if (isFavorite) "Remove from favorites" else "Add to favorites",
                                tint = if (isFavorite) Color(0xFFEF4444) else textSecondary,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                }

                Text(
                    text = if (showDetails && track.album.isNotBlank()) {
                        "${track.artist.ifBlank { "Unknown Artist" }} • ${track.album}"
                    } else {
                        track.artist.ifBlank { "Unknown Artist" }
                    },
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                    color = textSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center
                )
            }
        }
        com.example.model.M3InfoAlignment.SPLIT_ACCENT -> {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = track.title.ifBlank { "No Track Selected" },
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = (-0.5).sp
                            ),
                            color = textColor,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = accentColor.copy(alpha = 0.16f),
                            modifier = Modifier.padding(vertical = 2.dp)
                        ) {
                            Text(
                                text = track.formatName.ifBlank { "AUDIO" },
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = accentColor,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                        Text(
                            text = track.artist.ifBlank { "Unknown Artist" },
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                            color = textSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                if (showFavorite) {
                    IconButton(
                        onClick = onFavoriteToggle,
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("favorite_button")
                    ) {
                        Icon(
                            imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Outlined.FavoriteBorder,
                            contentDescription = if (isFavorite) "Remove from favorites" else "Add to favorites",
                            tint = if (isFavorite) Color(0xFFEF4444) else textSecondary,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
            }
        }
        com.example.model.M3InfoAlignment.LEFT -> {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = track.title.ifBlank { "No Track Selected" },
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = (-0.5).sp
                        ),
                        color = textColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Text(
                        text = if (showDetails && track.album.isNotBlank()) {
                            "${track.artist.ifBlank { "Unknown Artist" }} • ${track.album}"
                        } else {
                            track.artist.ifBlank { "Unknown Artist" }
                        },
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                        color = textSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                if (showFavorite) {
                    IconButton(
                        onClick = onFavoriteToggle,
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("favorite_button")
                    ) {
                        Icon(
                            imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Outlined.FavoriteBorder,
                            contentDescription = if (isFavorite) "Remove from favorites" else "Add to favorites",
                            tint = if (isFavorite) Color(0xFFEF4444) else textSecondary,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Progress Scrubber supporting Wavy Wave, Chunky Pill, Segmented, or Minimal Sleek.
 */
@Composable
private fun M3ExpressiveScrubber(
    currentPositionMs: Long,
    durationMs: Long,
    isPlaying: Boolean,
    style: M3ScrubberStyle,
    accentColor: Color,
    textColor: Color,
    onSeekTo: (Long) -> Unit
) {
    when (style) {
        M3ScrubberStyle.WAVY -> {
            M3WavyProgressScrubber(
                currentPositionMs = currentPositionMs,
                durationMs = durationMs,
                isPlaying = isPlaying,
                accentColor = accentColor,
                textColor = textColor,
                onSeekTo = onSeekTo
            )
        }
        M3ScrubberStyle.CHUNKY_PILL -> {
            M3ExpressiveProgressBar(
                currentPositionMs = currentPositionMs,
                durationMs = durationMs,
                onSeekTo = onSeekTo,
                activeColor = accentColor,
                inactiveColor = textColor.copy(alpha = 0.15f)
            )
        }
        M3ScrubberStyle.SEGMENTED_BEATS -> {
            M3SegmentedProgressScrubber(
                currentPositionMs = currentPositionMs,
                durationMs = durationMs,
                accentColor = accentColor,
                textColor = textColor,
                onSeekTo = onSeekTo
            )
        }
        M3ScrubberStyle.MINIMAL_SLIM -> {
            M3MinimalProgressScrubber(
                currentPositionMs = currentPositionMs,
                durationMs = durationMs,
                accentColor = accentColor,
                textColor = textColor,
                onSeekTo = onSeekTo
            )
        }
        M3ScrubberStyle.DYNAMIC_SPECTRUM -> {
            M3DynamicSpectrumScrubber(
                currentPositionMs = currentPositionMs,
                durationMs = durationMs,
                isPlaying = isPlaying,
                accentColor = accentColor,
                textColor = textColor,
                onSeekTo = onSeekTo
            )
        }
        M3ScrubberStyle.NEON_BEAM_GLOW -> {
            M3NeonBeamScrubber(
                currentPositionMs = currentPositionMs,
                durationMs = durationMs,
                isPlaying = isPlaying,
                accentColor = accentColor,
                textColor = textColor,
                onSeekTo = onSeekTo
            )
        }
        M3ScrubberStyle.VU_STEREO_TICKS -> {
            M3VuMeterScrubber(
                currentPositionMs = currentPositionMs,
                durationMs = durationMs,
                isPlaying = isPlaying,
                accentColor = accentColor,
                textColor = textColor,
                onSeekTo = onSeekTo
            )
        }
        M3ScrubberStyle.DOT_MATRIX_BEADS -> {
            M3DotMatrixScrubber(
                currentPositionMs = currentPositionMs,
                durationMs = durationMs,
                accentColor = accentColor,
                textColor = textColor,
                onSeekTo = onSeekTo
            )
        }
        M3ScrubberStyle.GRADIENT_RIVER -> {
            M3GradientRiverScrubber(
                currentPositionMs = currentPositionMs,
                durationMs = durationMs,
                isPlaying = isPlaying,
                accentColor = accentColor,
                textColor = textColor,
                onSeekTo = onSeekTo
            )
        }
    }
}

/**
 * Live audio frequency spectrum scrubber that visually reacts to playback and supports drag seeking.
 */
@Composable
private fun M3DynamicSpectrumScrubber(
    currentPositionMs: Long,
    durationMs: Long,
    isPlaying: Boolean,
    accentColor: Color,
    textColor: Color,
    onSeekTo: (Long) -> Unit
) {
    val totalMs = durationMs.coerceAtLeast(1L)
    val fraction = (currentPositionMs.toFloat() / totalMs.toFloat()).coerceIn(0f, 1f)
    var dragFraction by remember { mutableFloatStateOf(-1f) }
    val displayFraction = if (dragFraction >= 0f) dragFraction else fraction

    val infiniteTransition = rememberInfiniteTransition(label = "spectrumPhysics")
    val pulsePhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1400, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulsePhase"
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(38.dp)
                .pointerInput(totalMs) {
                    detectTapGestures { offset ->
                        val newFraction = (offset.x / size.width.toFloat()).coerceIn(0f, 1f)
                        onSeekTo((newFraction * totalMs).toLong())
                    }
                }
                .pointerInput(totalMs) {
                    detectHorizontalDragGestures(
                        onDragStart = { offset ->
                            dragFraction = (offset.x / size.width.toFloat()).coerceIn(0f, 1f)
                        },
                        onDragEnd = {
                            if (dragFraction >= 0f) {
                                onSeekTo((dragFraction * totalMs).toLong())
                                dragFraction = -1f
                            }
                        },
                        onDragCancel = {
                            dragFraction = -1f
                        },
                        onHorizontalDrag = { change, _ ->
                            dragFraction = (change.position.x / size.width.toFloat()).coerceIn(0f, 1f)
                        }
                    )
                },
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val barCount = 36
                val totalWidth = size.width
                val barWidth = totalWidth / (barCount * 1.5f)
                val spacing = barWidth * 0.5f
                val midY = size.height / 2f

                for (i in 0 until barCount) {
                    val x = i * (barWidth + spacing) + barWidth / 2f
                    val barFraction = (i.toFloat() / (barCount - 1).toFloat()).coerceIn(0f, 1f)
                    val isPast = barFraction <= displayFraction

                    // Simulated live frequency harmonics
                    val sineVal = kotlin.math.sin((i * 0.45f + (if (isPlaying) pulsePhase * 6.28f else 0f)).toDouble()).toFloat()
                    val cosVal = kotlin.math.cos((i * 0.25f - (if (isPlaying) pulsePhase * 4.14f else 0f)).toDouble()).toFloat()
                    val rawAmp = (kotlin.math.abs(sineVal * 0.6f + cosVal * 0.4f)).coerceIn(0.15f, 1f)
                    val barHeight = size.height * (if (isPlaying) rawAmp * 0.85f + 0.15f else 0.25f)

                    val color = if (isPast) {
                        accentColor
                    } else {
                        textColor.copy(alpha = 0.25f)
                    }

                    drawLine(
                        color = color,
                        start = Offset(x, midY - barHeight / 2f),
                        end = Offset(x, midY + barHeight / 2f),
                        strokeWidth = barWidth,
                        cap = StrokeCap.Round
                    )
                }

                // Thumb indicator at displayFraction
                val thumbX = displayFraction * size.width
                drawCircle(
                    color = accentColor,
                    radius = if (dragFraction >= 0f) 8.dp.toPx() else 5.dp.toPx(),
                    center = Offset(thumbX, midY)
                )
                drawCircle(
                    color = Color.White,
                    radius = if (dragFraction >= 0f) 4.dp.toPx() else 2.5.dp.toPx(),
                    center = Offset(thumbX, midY)
                )
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 2.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            val currentMsToFormat = if (dragFraction >= 0f) (dragFraction * totalMs).toLong() else currentPositionMs
            Text(
                text = formatDuration(currentMsToFormat),
                style = MaterialTheme.typography.labelSmall,
                color = textColor.copy(alpha = 0.7f)
            )
            Text(
                text = formatDuration(durationMs),
                style = MaterialTheme.typography.labelSmall,
                color = textColor.copy(alpha = 0.7f)
            )
        }
    }
}

/**
 * Android 13/14/15/16 Wavy Media Progress Bar with animated sinusoidal curve.
 */
@Composable
private fun M3WavyProgressScrubber(
    currentPositionMs: Long,
    durationMs: Long,
    isPlaying: Boolean,
    accentColor: Color,
    textColor: Color,
    onSeekTo: (Long) -> Unit
) {
    val totalMs = durationMs.coerceAtLeast(1L)
    val fraction = (currentPositionMs.toFloat() / totalMs.toFloat()).coerceIn(0f, 1f)
    var dragFraction by remember { mutableFloatStateOf(-1f) }
    val displayFraction = if (dragFraction >= 0f) dragFraction else fraction

    val infiniteTransition = rememberInfiniteTransition(label = "waveOffset")
    val wavePhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2f * PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase"
    )

    val waveAmplitude by animateFloatAsState(
        targetValue = if (isPlaying) 6f else 0f,
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "waveAmp"
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("wavy_scrubber")
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(36.dp)
                .pointerInput(totalMs) {
                    detectTapGestures { offset ->
                        val newFrac = (offset.x / size.width.toFloat()).coerceIn(0f, 1f)
                        onSeekTo((newFrac * totalMs).toLong())
                    }
                }
                .pointerInput(totalMs) {
                    detectHorizontalDragGestures(
                        onDragStart = { offset ->
                            dragFraction = (offset.x / size.width.toFloat()).coerceIn(0f, 1f)
                        },
                        onDragEnd = {
                            if (dragFraction >= 0f) {
                                onSeekTo((dragFraction * totalMs).toLong())
                                dragFraction = -1f
                            }
                        },
                        onDragCancel = {
                            dragFraction = -1f
                        },
                        onHorizontalDrag = { change, _ ->
                            change.consume()
                            dragFraction = (change.position.x / size.width.toFloat()).coerceIn(0f, 1f)
                        }
                    )
                },
            contentAlignment = Alignment.CenterStart
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val width = size.width
                val height = size.height
                val centerY = height / 2f
                val thumbX = width * displayFraction

                // 1. Draw inactive line (after thumb)
                drawLine(
                    color = textColor.copy(alpha = 0.18f),
                    start = Offset(thumbX, centerY),
                    end = Offset(width, centerY),
                    strokeWidth = 4.dp.toPx(),
                    cap = StrokeCap.Round
                )

                // 2. Draw active wavy line (before thumb)
                val wavePath = Path()
                wavePath.moveTo(0f, centerY)

                val step = 4f
                var x = 0f
                val wavelength = 36.dp.toPx()

                while (x <= thumbX) {
                    val angle = (x / wavelength) * (2f * PI.toFloat()) + wavePhase
                    val y = centerY + sin(angle) * waveAmplitude.dp.toPx()
                    wavePath.lineTo(x, y)
                    x += step
                }
                wavePath.lineTo(thumbX, centerY)

                drawPath(
                    path = wavePath,
                    color = accentColor,
                    style = Stroke(width = 4.5.dp.toPx(), cap = StrokeCap.Round)
                )

                // 3. Draw tactile thumb
                drawCircle(
                    color = Color.White,
                    radius = 8.dp.toPx(),
                    center = Offset(thumbX, centerY)
                )
                drawCircle(
                    color = accentColor,
                    radius = 5.dp.toPx(),
                    center = Offset(thumbX, centerY)
                )
            }
        }

        // Timestamp row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = formatDuration(currentPositionMs),
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                color = textColor.copy(alpha = 0.7f)
            )
            val remainingMs = (durationMs - currentPositionMs).coerceAtLeast(0L)
            Text(
                text = "-${formatDuration(remainingMs)}",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                color = textColor.copy(alpha = 0.5f)
            )
        }
    }
}

/**
 * Segmented Beats Progress Scrubber.
 */
@Composable
private fun M3SegmentedProgressScrubber(
    currentPositionMs: Long,
    durationMs: Long,
    accentColor: Color,
    textColor: Color,
    onSeekTo: (Long) -> Unit
) {
    val totalMs = durationMs.coerceAtLeast(1L)
    val fraction = (currentPositionMs.toFloat() / totalMs.toFloat()).coerceIn(0f, 1f)

    Column(modifier = Modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(30.dp)
                .pointerInput(totalMs) {
                    detectTapGestures { offset ->
                        val newFrac = (offset.x / size.width.toFloat()).coerceIn(0f, 1f)
                        onSeekTo((newFrac * totalMs).toLong())
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxWidth().height(10.dp)) {
                val totalSegments = 24
                val segSpacing = 3.dp.toPx()
                val totalSpacing = segSpacing * (totalSegments - 1)
                val segWidth = (size.width - totalSpacing) / totalSegments

                for (i in 0 until totalSegments) {
                    val segFrac = (i + 1).toFloat() / totalSegments.toFloat()
                    val isActive = fraction >= segFrac
                    val startX = i * (segWidth + segSpacing)
                    drawRoundRect(
                        color = if (isActive) accentColor else textColor.copy(alpha = 0.15f),
                        topLeft = Offset(startX, 0f),
                        size = Size(segWidth, size.height),
                        cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
                    )
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = formatDuration(currentPositionMs),
                style = MaterialTheme.typography.labelSmall,
                color = textColor.copy(alpha = 0.7f)
            )
            Text(
                text = formatDuration(durationMs),
                style = MaterialTheme.typography.labelSmall,
                color = textColor.copy(alpha = 0.5f)
            )
        }
    }
}

/**
 * Minimal Sleek Hairline Scrubber.
 */
@Composable
private fun M3MinimalProgressScrubber(
    currentPositionMs: Long,
    durationMs: Long,
    accentColor: Color,
    textColor: Color,
    onSeekTo: (Long) -> Unit
) {
    val totalMs = durationMs.coerceAtLeast(1L)
    val fraction = (currentPositionMs.toFloat() / totalMs.toFloat()).coerceIn(0f, 1f)

    Column(modifier = Modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(28.dp)
                .pointerInput(totalMs) {
                    detectTapGestures { offset ->
                        val newFrac = (offset.x / size.width.toFloat()).coerceIn(0f, 1f)
                        onSeekTo((newFrac * totalMs).toLong())
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxWidth().height(3.dp)) {
                drawLine(
                    color = textColor.copy(alpha = 0.15f),
                    start = Offset(0f, size.height / 2),
                    end = Offset(size.width, size.height / 2),
                    strokeWidth = 3.dp.toPx(),
                    cap = StrokeCap.Round
                )
                drawLine(
                    color = accentColor,
                    start = Offset(0f, size.height / 2),
                    end = Offset(size.width * fraction, size.height / 2),
                    strokeWidth = 3.dp.toPx(),
                    cap = StrokeCap.Round
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = formatDuration(currentPositionMs),
                style = MaterialTheme.typography.labelSmall,
                color = textColor.copy(alpha = 0.7f)
            )
            Text(
                text = formatDuration(durationMs),
                style = MaterialTheme.typography.labelSmall,
                color = textColor.copy(alpha = 0.5f)
            )
        }
    }
}

/**
 * Neon Beam Scrubber with laser particle glowing head.
 */
@Composable
private fun M3NeonBeamScrubber(
    currentPositionMs: Long,
    durationMs: Long,
    isPlaying: Boolean,
    accentColor: Color,
    textColor: Color,
    onSeekTo: (Long) -> Unit
) {
    val totalMs = durationMs.coerceAtLeast(1L)
    val fraction = (currentPositionMs.toFloat() / totalMs.toFloat()).coerceIn(0f, 1f)

    Column(modifier = Modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(32.dp)
                .pointerInput(totalMs) {
                    detectTapGestures { offset ->
                        val newFrac = (offset.x / size.width.toFloat()).coerceIn(0f, 1f)
                        onSeekTo((newFrac * totalMs).toLong())
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxWidth().height(8.dp)) {
                val midY = size.height / 2f
                val headX = size.width * fraction

                drawLine(
                    color = textColor.copy(alpha = 0.15f),
                    start = Offset(0f, midY),
                    end = Offset(size.width, midY),
                    strokeWidth = 4.dp.toPx(),
                    cap = StrokeCap.Round
                )

                if (headX > 0f) {
                    drawLine(
                        brush = Brush.horizontalGradient(
                            colors = listOf(accentColor.copy(alpha = 0.3f), accentColor, Color.White)
                        ),
                        start = Offset(0f, midY),
                        end = Offset(headX, midY),
                        strokeWidth = 5.dp.toPx(),
                        cap = StrokeCap.Round
                    )
                    drawCircle(
                        color = accentColor.copy(alpha = 0.5f),
                        radius = 9.dp.toPx(),
                        center = Offset(headX, midY)
                    )
                    drawCircle(
                        color = Color.White,
                        radius = 4.dp.toPx(),
                        center = Offset(headX, midY)
                    )
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = formatDuration(currentPositionMs), style = MaterialTheme.typography.labelSmall, color = textColor.copy(alpha = 0.7f))
            Text(text = formatDuration(durationMs), style = MaterialTheme.typography.labelSmall, color = textColor.copy(alpha = 0.5f))
        }
    }
}

/**
 * VU Meter Ticks Scrubber with green/amber/red audio LED level styling.
 */
@Composable
private fun M3VuMeterScrubber(
    currentPositionMs: Long,
    durationMs: Long,
    isPlaying: Boolean,
    accentColor: Color,
    textColor: Color,
    onSeekTo: (Long) -> Unit
) {
    val totalMs = durationMs.coerceAtLeast(1L)
    val fraction = (currentPositionMs.toFloat() / totalMs.toFloat()).coerceIn(0f, 1f)

    Column(modifier = Modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(32.dp)
                .pointerInput(totalMs) {
                    detectTapGestures { offset ->
                        val newFrac = (offset.x / size.width.toFloat()).coerceIn(0f, 1f)
                        onSeekTo((newFrac * totalMs).toLong())
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxWidth().height(14.dp)) {
                val totalTicks = 32
                val spacing = 2.5.dp.toPx()
                val tickWidth = (size.width - (spacing * (totalTicks - 1))) / totalTicks

                for (i in 0 until totalTicks) {
                    val tickFrac = (i + 1).toFloat() / totalTicks.toFloat()
                    val isActive = fraction >= tickFrac
                    val x = i * (tickWidth + spacing)

                    val tickColor = when {
                        !isActive -> textColor.copy(alpha = 0.12f)
                        i > totalTicks * 0.85f -> Color(0xFFEF4444)
                        i > totalTicks * 0.65f -> Color(0xFFF59E0B)
                        else -> accentColor
                    }

                    drawRoundRect(
                        color = tickColor,
                        topLeft = Offset(x, 0f),
                        size = Size(tickWidth, size.height),
                        cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx())
                    )
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = formatDuration(currentPositionMs), style = MaterialTheme.typography.labelSmall, color = textColor.copy(alpha = 0.7f))
            Text(text = formatDuration(durationMs), style = MaterialTheme.typography.labelSmall, color = textColor.copy(alpha = 0.5f))
        }
    }
}

/**
 * Dot Matrix Beads Scrubber.
 */
@Composable
private fun M3DotMatrixScrubber(
    currentPositionMs: Long,
    durationMs: Long,
    accentColor: Color,
    textColor: Color,
    onSeekTo: (Long) -> Unit
) {
    val totalMs = durationMs.coerceAtLeast(1L)
    val fraction = (currentPositionMs.toFloat() / totalMs.toFloat()).coerceIn(0f, 1f)

    Column(modifier = Modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(30.dp)
                .pointerInput(totalMs) {
                    detectTapGestures { offset ->
                        val newFrac = (offset.x / size.width.toFloat()).coerceIn(0f, 1f)
                        onSeekTo((newFrac * totalMs).toLong())
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxWidth().height(10.dp)) {
                val dots = 30
                val midY = size.height / 2f
                val dotRadius = 3.dp.toPx()
                val step = size.width / (dots - 1)

                for (i in 0 until dots) {
                    val x = i * step
                    val dotFrac = i.toFloat() / (dots - 1).toFloat()
                    val isActive = fraction >= dotFrac

                    drawCircle(
                        color = if (isActive) accentColor else textColor.copy(alpha = 0.16f),
                        radius = if (isActive) dotRadius else dotRadius * 0.7f,
                        center = Offset(x, midY)
                    )
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = formatDuration(currentPositionMs), style = MaterialTheme.typography.labelSmall, color = textColor.copy(alpha = 0.7f))
            Text(text = formatDuration(durationMs), style = MaterialTheme.typography.labelSmall, color = textColor.copy(alpha = 0.5f))
        }
    }
}

/**
 * Chroma Fluid River Scrubber with multi-color dynamic gradient.
 */
@Composable
private fun M3GradientRiverScrubber(
    currentPositionMs: Long,
    durationMs: Long,
    isPlaying: Boolean,
    accentColor: Color,
    textColor: Color,
    onSeekTo: (Long) -> Unit
) {
    val totalMs = durationMs.coerceAtLeast(1L)
    val fraction = (currentPositionMs.toFloat() / totalMs.toFloat()).coerceIn(0f, 1f)

    Column(modifier = Modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(32.dp)
                .pointerInput(totalMs) {
                    detectTapGestures { offset ->
                        val newFrac = (offset.x / size.width.toFloat()).coerceIn(0f, 1f)
                        onSeekTo((newFrac * totalMs).toLong())
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxWidth().height(6.dp)) {
                val midY = size.height / 2f
                val headX = size.width * fraction

                drawLine(
                    color = textColor.copy(alpha = 0.15f),
                    start = Offset(0f, midY),
                    end = Offset(size.width, midY),
                    strokeWidth = 6.dp.toPx(),
                    cap = StrokeCap.Round
                )

                if (headX > 0f) {
                    drawLine(
                        brush = Brush.horizontalGradient(
                            listOf(Color(0xFF8B5CF6), Color(0xFF06B6D4), accentColor, Color(0xFF10B981))
                        ),
                        start = Offset(0f, midY),
                        end = Offset(headX, midY),
                        strokeWidth = 6.dp.toPx(),
                        cap = StrokeCap.Round
                    )
                    drawCircle(
                        color = Color.White,
                        radius = 6.dp.toPx(),
                        center = Offset(headX, midY)
                    )
                    drawCircle(
                        color = accentColor,
                        radius = 4.dp.toPx(),
                        center = Offset(headX, midY)
                    )
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = formatDuration(currentPositionMs), style = MaterialTheme.typography.labelSmall, color = textColor.copy(alpha = 0.7f))
            Text(text = formatDuration(durationMs), style = MaterialTheme.typography.labelSmall, color = textColor.copy(alpha = 0.5f))
        }
    }
}

/**
 * Expressive Transport Controls with customizable hero button and control button styles.
 */
@Composable
private fun M3ExpressiveTransportControls(
    isPlaying: Boolean,
    isShuffle: Boolean,
    isRepeat: Boolean,
    heroButtonStyle: M3HeroButtonStyle,
    controlButtonStyle: M3ControlButtonStyle = M3ControlButtonStyle.EXPRESSIVE_TONAL_SQUIRCLE,
    buttonGlowEffect: Boolean = true,
    showShuffleRepeat: Boolean = true,
    onPlayPauseClick: () -> Unit,
    onPreviousClick: () -> Unit,
    onNextClick: () -> Unit,
    onShuffleToggle: () -> Unit,
    onRepeatToggle: () -> Unit,
    accentColor: Color,
    textColor: Color
) {
    val controlShape: Shape = when (controlButtonStyle) {
        M3ControlButtonStyle.EXPRESSIVE_TONAL_SQUIRCLE -> ExpressiveShapes.Squircle
        M3ControlButtonStyle.PILL_SEGMENTED_DOCK -> ExpressiveShapes.PillDock
        M3ControlButtonStyle.MINIMAL_FLOATING_ICONS -> CircleShape
        M3ControlButtonStyle.GLOWING_CIRCULAR_PADS -> CircleShape
        M3ControlButtonStyle.FACETED_DIAMOND_TILES -> ExpressiveShapes.CutCornerCard
    }

    val controlContainerColor = when (controlButtonStyle) {
        M3ControlButtonStyle.MINIMAL_FLOATING_ICONS -> Color.Transparent
        M3ControlButtonStyle.GLOWING_CIRCULAR_PADS -> MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.6f)
        M3ControlButtonStyle.PILL_SEGMENTED_DOCK -> MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.5f)
        M3ControlButtonStyle.FACETED_DIAMOND_TILES -> MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.45f)
        M3ControlButtonStyle.EXPRESSIVE_TONAL_SQUIRCLE -> MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.45f)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Shuffle Pill
        if (showShuffleRepeat) {
            Surface(
                onClick = onShuffleToggle,
                shape = controlShape,
                color = if (isShuffle) accentColor.copy(alpha = 0.22f) else controlContainerColor,
                border = BorderStroke(
                    width = 1.dp,
                    color = if (isShuffle) accentColor else textColor.copy(alpha = 0.08f)
                ),
                modifier = Modifier.size(44.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Shuffle,
                        contentDescription = "Toggle Shuffle",
                        tint = if (isShuffle) accentColor else textColor.copy(alpha = 0.65f),
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        } else {
            Spacer(modifier = Modifier.size(44.dp))
        }

        // Previous Button
        Surface(
            onClick = onPreviousClick,
            shape = controlShape,
            color = controlContainerColor,
            border = BorderStroke(1.dp, textColor.copy(alpha = 0.08f)),
            modifier = Modifier.size(52.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Default.SkipPrevious,
                    contentDescription = "Previous Track",
                    tint = textColor,
                    modifier = Modifier.size(30.dp)
                )
            }
        }

        // HERO PLAY/PAUSE BUTTON
        M3ExpressiveHeroButton(
            isPlaying = isPlaying,
            style = heroButtonStyle,
            buttonGlowEffect = buttonGlowEffect,
            accentColor = accentColor,
            onClick = onPlayPauseClick
        )

        // Next Button
        Surface(
            onClick = onNextClick,
            shape = controlShape,
            color = controlContainerColor,
            border = BorderStroke(1.dp, textColor.copy(alpha = 0.08f)),
            modifier = Modifier.size(52.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Default.SkipNext,
                    contentDescription = "Next Track",
                    tint = textColor,
                    modifier = Modifier.size(30.dp)
                )
            }
        }

        // Repeat Pill
        if (showShuffleRepeat) {
            Surface(
                onClick = onRepeatToggle,
                shape = controlShape,
                color = if (isRepeat) accentColor.copy(alpha = 0.22f) else controlContainerColor,
                border = BorderStroke(
                    width = 1.dp,
                    color = if (isRepeat) accentColor else textColor.copy(alpha = 0.08f)
                ),
                modifier = Modifier.size(44.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Repeat,
                        contentDescription = "Toggle Repeat",
                        tint = if (isRepeat) accentColor else textColor.copy(alpha = 0.65f),
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        } else {
            Spacer(modifier = Modifier.size(44.dp))
        }
    }
}

/**
 * Customizable M3 Expressive Hero Button with all 12 styles and halo glow support.
 */
@Composable
private fun M3ExpressiveHeroButton(
    isPlaying: Boolean,
    style: M3HeroButtonStyle,
    buttonGlowEffect: Boolean = true,
    accentColor: Color,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.88f else 1.0f,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "heroScale"
    )

    val haloModifier = if (buttonGlowEffect) {
        Modifier.shadow(
            elevation = if (isPlaying) 16.dp else 8.dp,
            shape = CircleShape,
            spotColor = accentColor.copy(alpha = 0.6f),
            ambientColor = accentColor.copy(alpha = 0.35f)
        )
    } else Modifier

    when (style) {
        M3HeroButtonStyle.EXPRESSIVE_SQUIRCLE -> {
            Surface(
                onClick = onClick,
                interactionSource = interactionSource,
                shape = RoundedCornerShape(26.dp),
                color = accentColor,
                shadowElevation = if (buttonGlowEffect) 12.dp else 6.dp,
                modifier = Modifier
                    .size(72.dp)
                    .scale(scale)
                    .then(haloModifier)
                    .testTag("hero_play_pause_button")
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Crossfade(targetState = isPlaying, label = "playPauseCrossfade") { playing ->
                        Icon(
                            imageVector = if (playing) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (playing) "Pause" else "Play",
                            tint = Color.White,
                            modifier = Modifier.size(38.dp)
                        )
                    }
                }
            }
        }
        M3HeroButtonStyle.PILL_FAB -> {
            Surface(
                onClick = onClick,
                interactionSource = interactionSource,
                shape = RoundedCornerShape(32.dp),
                color = accentColor,
                shadowElevation = if (buttonGlowEffect) 12.dp else 6.dp,
                modifier = Modifier
                    .size(width = 96.dp, height = 64.dp)
                    .scale(scale)
                    .then(haloModifier)
                    .testTag("hero_play_pause_button")
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Crossfade(targetState = isPlaying, label = "playPausePill") { playing ->
                        Icon(
                            imageVector = if (playing) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (playing) "Pause" else "Play",
                            tint = Color.White,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                }
            }
        }
        M3HeroButtonStyle.CIRCLE_HERO -> {
            Surface(
                onClick = onClick,
                interactionSource = interactionSource,
                shape = CircleShape,
                color = accentColor,
                shadowElevation = if (buttonGlowEffect) 12.dp else 6.dp,
                modifier = Modifier
                    .size(72.dp)
                    .scale(scale)
                    .then(haloModifier)
                    .testTag("hero_play_pause_button")
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Crossfade(targetState = isPlaying, label = "playPauseCircle") { playing ->
                        Icon(
                            imageVector = if (playing) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (playing) "Pause" else "Play",
                            tint = Color.White,
                            modifier = Modifier.size(38.dp)
                        )
                    }
                }
            }
        }
        M3HeroButtonStyle.ASYMMETRIC_DIAMOND -> {
            Surface(
                onClick = onClick,
                interactionSource = interactionSource,
                shape = RoundedCornerShape(18.dp),
                color = accentColor,
                shadowElevation = if (buttonGlowEffect) 12.dp else 6.dp,
                modifier = Modifier
                    .size(70.dp)
                    .scale(scale)
                    .rotate(45f)
                    .then(haloModifier)
                    .testTag("hero_play_pause_button")
            ) {
                Box(
                    modifier = Modifier.rotate(-45f),
                    contentAlignment = Alignment.Center
                ) {
                    Crossfade(targetState = isPlaying, label = "playPauseDiamond") { playing ->
                        Icon(
                            imageVector = if (playing) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (playing) "Pause" else "Play",
                            tint = Color.White,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                }
            }
        }
        M3HeroButtonStyle.NEUMORPHIC_CONTOUR -> {
            Surface(
                onClick = onClick,
                interactionSource = interactionSource,
                shape = RoundedCornerShape(26.dp),
                color = MaterialTheme.colorScheme.surfaceContainerHighest,
                border = BorderStroke(2.dp, Brush.linearGradient(listOf(accentColor, Color.White.copy(alpha = 0.6f)))),
                shadowElevation = 10.dp,
                modifier = Modifier
                    .size(72.dp)
                    .scale(scale)
                    .then(haloModifier)
                    .testTag("hero_play_pause_button")
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.radialGradient(
                                listOf(accentColor.copy(alpha = 0.85f), accentColor.copy(alpha = 0.5f))
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Crossfade(targetState = isPlaying, label = "playPauseNeumorphic") { playing ->
                        Icon(
                            imageVector = if (playing) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (playing) "Pause" else "Play",
                            tint = Color.White,
                            modifier = Modifier.size(38.dp)
                        )
                    }
                }
            }
        }
        M3HeroButtonStyle.DOUBLE_RING_ORBIT -> {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .scale(scale),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    drawCircle(
                        color = accentColor.copy(alpha = if (isPlaying) 0.35f else 0.15f),
                        radius = size.width / 2f - 2.dp.toPx(),
                        style = Stroke(width = 2.dp.toPx())
                    )
                    drawCircle(
                        color = accentColor.copy(alpha = if (isPlaying) 0.65f else 0.30f),
                        radius = size.width / 2f - 8.dp.toPx(),
                        style = Stroke(width = 1.5.dp.toPx())
                    )
                }
                Surface(
                    onClick = onClick,
                    interactionSource = interactionSource,
                    shape = CircleShape,
                    color = accentColor,
                    modifier = Modifier.size(60.dp).testTag("hero_play_pause_button")
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Crossfade(targetState = isPlaying, label = "playPauseOrbit") { playing ->
                            Icon(
                                imageVector = if (playing) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = if (playing) "Pause" else "Play",
                                tint = Color.White,
                                modifier = Modifier.size(34.dp)
                            )
                        }
                    }
                }
            }
        }
        M3HeroButtonStyle.ORGANIC_BLOSSOM -> {
            val blossomShape = ExpressiveShapes.CloverShape(0.40f)
            Surface(
                onClick = onClick,
                interactionSource = interactionSource,
                shape = blossomShape,
                color = accentColor,
                shadowElevation = 8.dp,
                modifier = Modifier
                    .size(72.dp)
                    .scale(scale)
                    .then(haloModifier)
                    .testTag("hero_play_pause_button")
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Crossfade(targetState = isPlaying, label = "playPauseBlossom") { playing ->
                        Icon(
                            imageVector = if (playing) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (playing) "Pause" else "Play",
                            tint = Color.White,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                }
            }
        }
        M3HeroButtonStyle.HEXAGON_PRISM -> {
            val hexShape = ExpressiveShapes.HexagonalBadgeShape()
            Surface(
                onClick = onClick,
                interactionSource = interactionSource,
                shape = hexShape,
                color = accentColor,
                border = BorderStroke(1.5.dp, Color.White.copy(alpha = 0.5f)),
                shadowElevation = 8.dp,
                modifier = Modifier
                    .size(72.dp)
                    .scale(scale)
                    .then(haloModifier)
                    .testTag("hero_play_pause_button")
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Crossfade(targetState = isPlaying, label = "playPauseHex") { playing ->
                        Icon(
                            imageVector = if (playing) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (playing) "Pause" else "Play",
                            tint = Color.White,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                }
            }
        }
        M3HeroButtonStyle.CAPSULE_SEGMENTED -> {
            Surface(
                onClick = onClick,
                interactionSource = interactionSource,
                shape = ExpressiveShapes.PillDock,
                color = accentColor,
                border = BorderStroke(2.dp, Color.White.copy(alpha = 0.35f)),
                shadowElevation = 8.dp,
                modifier = Modifier
                    .size(width = 88.dp, height = 62.dp)
                    .scale(scale)
                    .then(haloModifier)
                    .testTag("hero_play_pause_button")
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Crossfade(targetState = isPlaying, label = "playPauseCapsule") { playing ->
                        Icon(
                            imageVector = if (playing) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (playing) "Pause" else "Play",
                            tint = Color.White,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                }
            }
        }
        M3HeroButtonStyle.MINIMAL_OUTLINE_GLOW -> {
            Surface(
                onClick = onClick,
                interactionSource = interactionSource,
                shape = CircleShape,
                color = accentColor.copy(alpha = 0.20f),
                border = BorderStroke(2.5.dp, accentColor),
                modifier = Modifier
                    .size(72.dp)
                    .scale(scale)
                    .then(haloModifier)
                    .testTag("hero_play_pause_button")
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Crossfade(targetState = isPlaying, label = "playPauseOutline") { playing ->
                        Icon(
                            imageVector = if (playing) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (playing) "Pause" else "Play",
                            tint = Color.White,
                            modifier = Modifier.size(38.dp)
                        )
                    }
                }
            }
        }
        M3HeroButtonStyle.STARBURST_HERO -> {
            val starShape = ExpressiveShapes.ScallopedBadgeShape(8, 0.85f)
            Surface(
                onClick = onClick,
                interactionSource = interactionSource,
                shape = starShape,
                color = accentColor,
                shadowElevation = 8.dp,
                modifier = Modifier
                    .size(74.dp)
                    .scale(scale)
                    .then(haloModifier)
                    .testTag("hero_play_pause_button")
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Crossfade(targetState = isPlaying, label = "playPauseStar") { playing ->
                        Icon(
                            imageVector = if (playing) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (playing) "Pause" else "Play",
                            tint = Color.White,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                }
            }
        }
        M3HeroButtonStyle.STADIUM_BADGE -> {
            Surface(
                onClick = onClick,
                interactionSource = interactionSource,
                shape = RoundedCornerShape(24.dp),
                color = accentColor,
                border = BorderStroke(1.5.dp, Color.White.copy(alpha = 0.5f)),
                shadowElevation = 8.dp,
                modifier = Modifier
                    .size(width = 84.dp, height = 64.dp)
                    .scale(scale)
                    .then(haloModifier)
                    .testTag("hero_play_pause_button")
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.linearGradient(
                                listOf(accentColor, accentColor.copy(alpha = 0.7f), Color(0xFF6366F1))
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Crossfade(targetState = isPlaying, label = "playPauseStadium") { playing ->
                        Icon(
                            imageVector = if (playing) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (playing) "Pause" else "Play",
                            tint = Color.White,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Expressive Volume Bar with sound icons and smooth slider.
 */
@Composable
private fun M3ExpressiveVolumeBar(
    volume: Float,
    onVolumeChange: (Float) -> Unit,
    accentColor: Color,
    textColor: Color
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.35f),
        border = BorderStroke(1.dp, textColor.copy(alpha = 0.08f)),
        modifier = Modifier
            .fillMaxWidth()
            .height(44.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = if (volume <= 0.05f) Icons.AutoMirrored.Filled.VolumeMute else Icons.AutoMirrored.Filled.VolumeDown,
                contentDescription = "Mute",
                tint = textColor.copy(alpha = 0.6f),
                modifier = Modifier.size(18.dp)
            )

            Slider(
                value = volume,
                onValueChange = onVolumeChange,
                modifier = Modifier.weight(1f),
                colors = SliderDefaults.colors(
                    thumbColor = accentColor,
                    activeTrackColor = accentColor,
                    inactiveTrackColor = textColor.copy(alpha = 0.12f)
                )
            )

            Icon(
                imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                contentDescription = "Max Volume",
                tint = textColor.copy(alpha = 0.6f),
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

/**
 * Expressive Action Pills Row (Queue, Lyrics, Sleep Timer, Speed, Equalizer).
 */
@Composable
private fun M3ExpressiveActionPillsRow(
    queueCount: Int,
    isLyricsActive: Boolean,
    onQueueClick: () -> Unit,
    onLyricsClick: () -> Unit,
    onSleepTimerClick: () -> Unit,
    onSpeedClick: () -> Unit,
    onEqualizerClick: () -> Unit,
    accentColor: Color,
    textColor: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Queue Pill
        M3ToolPill(
            icon = Icons.Default.QueueMusic,
            label = if (queueCount > 0) "$queueCount" else "Queue",
            isSelected = false,
            accentColor = accentColor,
            textColor = textColor,
            onClick = onQueueClick
        )

        // Lyrics Pill
        M3ToolPill(
            icon = Icons.Default.Lyrics,
            label = "Lyrics",
            isSelected = isLyricsActive,
            accentColor = accentColor,
            textColor = textColor,
            onClick = onLyricsClick
        )

        // Sleep Timer Pill
        M3ToolPill(
            icon = Icons.Default.Bedtime,
            label = "Timer",
            isSelected = false,
            accentColor = accentColor,
            textColor = textColor,
            onClick = onSleepTimerClick
        )

        // Speed Pill
        M3ToolPill(
            icon = Icons.Default.Speed,
            label = "Speed",
            isSelected = false,
            accentColor = accentColor,
            textColor = textColor,
            onClick = onSpeedClick
        )

        // Equalizer Pill
        M3ToolPill(
            icon = Icons.Default.Equalizer,
            label = "EQ",
            isSelected = false,
            accentColor = accentColor,
            textColor = textColor,
            onClick = onEqualizerClick
        )
    }
}

@Composable
private fun M3ToolPill(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    isSelected: Boolean,
    accentColor: Color,
    textColor: Color,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = CircleShape,
        color = if (isSelected) accentColor else MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.5f),
        border = BorderStroke(
            1.dp,
            if (isSelected) accentColor else textColor.copy(alpha = 0.10f)
        ),
        modifier = Modifier.height(36.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (isSelected) Color.White else textColor.copy(alpha = 0.8f),
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                color = if (isSelected) Color.White else textColor.copy(alpha = 0.8f)
            )
        }
    }
}

/**
 * Synchronized Lyrics Quick Peek Card.
 */
@Composable
private fun M3ExpressiveLyricsPeekCard(
    lyricsState: LyricsUiState.Success,
    currentPositionMs: Long,
    onCardClick: () -> Unit,
    accentColor: Color,
    textColor: Color
) {
    val activeLine = lyricsState.lines.lastOrNull { it.timestampMs <= currentPositionMs }?.text
        ?: lyricsState.lines.firstOrNull()?.text
        ?: "Tap to view synchronized lyrics"

    Surface(
        onClick = onCardClick,
        shape = RoundedCornerShape(16.dp),
        color = accentColor.copy(alpha = 0.12f),
        border = BorderStroke(1.dp, accentColor.copy(alpha = 0.28f)),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("lyrics_peek_card")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Lyrics,
                contentDescription = null,
                tint = accentColor,
                modifier = Modifier.size(18.dp)
            )
            Text(
                text = activeLine,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                color = textColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

/**
 * In-Place Synced Lyrics View with smooth auto-scroll and full customization support.
 */
@Composable
private fun M3ExpressiveInPlaceLyrics(
    lyricsUiState: LyricsUiState,
    currentPositionMs: Long,
    onSeekTo: (Long) -> Unit,
    onCloseLyrics: () -> Unit,
    accentColor: Color,
    textColor: Color,
    lyricsConfig: com.example.model.LyricsScreenConfig = com.example.model.LyricsScreenConfig(),
    haptics: Boolean,
    modifier: Modifier = Modifier.fillMaxWidth().height(320.dp)
) {
    val hapticController = rememberHapticController()
    val activeFontSize = lyricsConfig.textSize.activeSp.sp
    val inactiveFontSize = lyricsConfig.textSize.inactiveSp.sp
    val inactiveContrast = lyricsConfig.inactiveTextContrast
    val textAlign = when (lyricsConfig.textAlignment) {
        com.example.model.LyricsTextAlignment.LEFT -> TextAlign.Start
        com.example.model.LyricsTextAlignment.CENTER -> TextAlign.Center
        com.example.model.LyricsTextAlignment.RIGHT -> TextAlign.End
    }

    Surface(
        shape = RoundedCornerShape(28.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.75f),
        border = BorderStroke(1.dp, textColor.copy(alpha = 0.12f)),
        modifier = modifier
            .padding(vertical = 4.dp)
            .testTag("m3_inplace_lyrics")
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            when (lyricsUiState) {
                is LyricsUiState.Loading -> {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator(color = accentColor)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Loading Lyrics...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = textColor.copy(alpha = 0.7f)
                        )
                    }
                }
                is LyricsUiState.Success -> {
                    val listState = rememberLazyListState()
                    val coroutineScope = rememberCoroutineScope()
                    val activeIndex = lyricsUiState.lines.indexOfLast { it.timestampMs <= currentPositionMs }

                    var isUserScrolling by remember { mutableStateOf(false) }
                    var resumeTimerJob by remember { mutableStateOf<Job?>(null) }

                    LaunchedEffect(listState.interactionSource) {
                        listState.interactionSource.interactions.collect { interaction ->
                            when (interaction) {
                                is DragInteraction.Start -> {
                                    resumeTimerJob?.cancel()
                                    isUserScrolling = true
                                }
                                is DragInteraction.Stop, is DragInteraction.Cancel -> {
                                    resumeTimerJob?.cancel()
                                    resumeTimerJob = coroutineScope.launch {
                                        delay(3000)
                                        isUserScrolling = false
                                    }
                                }
                            }
                        }
                    }

                    LaunchedEffect(activeIndex, isUserScrolling) {
                        if (activeIndex >= 0 && !isUserScrolling) {
                            listState.animateScrollToItem(
                                index = activeIndex,
                                scrollOffset = 0
                            )
                        }
                    }

                    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                        val density = androidx.compose.ui.platform.LocalDensity.current
                        val halfHeightDp = with(density) { (maxHeight / 2f) }
                        val verticalPad = (halfHeightDp - 28.dp).coerceAtLeast(40.dp)

                        LazyColumn(
                            state = listState,
                            contentPadding = PaddingValues(horizontal = 20.dp, vertical = verticalPad),
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            itemsIndexed(lyricsUiState.lines) { index, line ->
                                val isActive = index == activeIndex
                                val fontSize = if (isActive) activeFontSize else inactiveFontSize
                                Text(
                                    text = line.text,
                                    textAlign = textAlign,
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = if (isActive) FontWeight.Bold else FontWeight.Medium,
                                        fontSize = fontSize,
                                        lineHeight = (fontSize.value * 1.34f).sp,
                                        shadow = if (isActive && lyricsConfig.lyricGlowBloom) androidx.compose.ui.graphics.Shadow(
                                            color = accentColor.copy(alpha = 0.8f),
                                            offset = Offset.Zero,
                                            blurRadius = 18f
                                        ) else null
                                    ),
                                    color = if (isActive) Color.White else textColor.copy(alpha = inactiveContrast),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            if (haptics) hapticController.performHaptic(HapticType.LIGHT_CLICK)
                                            isUserScrolling = false
                                            onSeekTo(line.timestampMs)
                                        }
                                )
                            }
                        }
                    }
                }
                is LyricsUiState.NotFound -> {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.MusicNote,
                            contentDescription = null,
                            tint = textColor.copy(alpha = 0.4f),
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "No Synchronized Lyrics Found",
                            style = MaterialTheme.typography.bodyMedium,
                            color = textColor.copy(alpha = 0.6f)
                        )
                    }
                }
            }

            // Close button top-right
            IconButton(
                onClick = onCloseLyrics,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close Lyrics",
                    tint = textColor
                )
            }
        }
    }
}

// -------------------------------------------------------------------------------------------------
// LIVE CUSTOMIZER MODAL BOTTOM SHEET
// -------------------------------------------------------------------------------------------------

/**
 * Modal BottomSheet for full Material 3 Expressive player customization.
 * Allows the user to choose their artwork shape, scrubber style, hero button style,
 * background treatment, and toggle every module/widget on screen with physics-based springs.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun M3ExpressiveCustomizerSheet(
    config: M3ExpressivePlayerConfig,
    onConfigChange: (M3ExpressivePlayerConfig) -> Unit,
    onDismiss: () -> Unit,
    accentColor: Color
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        scrimColor = Color.Black.copy(alpha = 0.65f),
        modifier = Modifier.testTag("m3_customizer_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp, vertical = 8.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Header with Reset Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = accentColor.copy(alpha = 0.2f),
                        modifier = Modifier.size(38.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.DashboardCustomize,
                                contentDescription = null,
                                tint = accentColor,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                    Column {
                        Text(
                            text = "Customize Player Screen",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "Material 3 Expressive Modular Controls",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Surface(
                    onClick = {
                        onConfigChange(M3ExpressivePlayerConfig())
                    },
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surfaceContainerHighest,
                    modifier = Modifier.springPressPhysics()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.RestartAlt,
                            contentDescription = "Reset Defaults",
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Reset",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
            Spacer(modifier = Modifier.height(14.dp))

            // 0. PLAYER STAGE STYLE
            CustomizerSectionTitle("Player Stage Style & Layout")
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                com.example.model.M3PlayerStyle.entries.forEach { pStyle ->
                    val isSelected = config.playerStyle == pStyle
                    FilterChip(
                        selected = isSelected,
                        onClick = { onConfigChange(config.copy(playerStyle = pStyle)) },
                        label = { Text(pStyle.displayName, style = MaterialTheme.typography.labelMedium) },
                        leadingIcon = if (isSelected) {
                            { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                        } else null,
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = accentColor.copy(alpha = 0.22f),
                            selectedLabelColor = accentColor,
                            selectedLeadingIconColor = accentColor
                        ),
                        modifier = Modifier.springPressPhysics()
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 0.5. ELEMENT PLACEMENT
            CustomizerSectionTitle("Element Placement & Order")
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                com.example.model.M3ElementPlacement.entries.forEach { placement ->
                    val isSelected = config.elementPlacement == placement
                    FilterChip(
                        selected = isSelected,
                        onClick = { onConfigChange(config.copy(elementPlacement = placement)) },
                        label = { Text(placement.displayName, style = MaterialTheme.typography.labelMedium) },
                        leadingIcon = if (isSelected) {
                            { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                        } else null,
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = accentColor.copy(alpha = 0.22f),
                            selectedLabelColor = accentColor,
                            selectedLeadingIconColor = accentColor
                        ),
                        modifier = Modifier.springPressPhysics()
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 1. ARTWORK SHAPE
            CustomizerSectionTitle("Artwork Shape & Aesthetic")
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                M3ArtworkShape.entries.forEach { shape ->
                    val isSelected = config.artworkShape == shape
                    val scale by animateFloatAsState(
                        targetValue = if (isSelected) 1.04f else 1.0f,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessMediumLow
                        ),
                        label = "shape_chip_scale"
                    )
                    FilterChip(
                        selected = isSelected,
                        onClick = { onConfigChange(config.copy(artworkShape = shape)) },
                        label = { Text(shape.displayName, style = MaterialTheme.typography.labelMedium) },
                        leadingIcon = if (isSelected) {
                            {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        } else null,
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = accentColor.copy(alpha = 0.22f),
                            selectedLabelColor = accentColor,
                            selectedLeadingIconColor = accentColor
                        ),
                        modifier = Modifier
                            .graphicsLayer {
                                scaleX = scale
                                scaleY = scale
                            }
                            .springPressPhysics()
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 2. ARTWORK CANVAS SIZE
            CustomizerSectionTitle("Artwork Canvas Size")
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                M3ArtworkSize.entries.forEach { size ->
                    val isSelected = config.artworkSize == size
                    val scale by animateFloatAsState(
                        targetValue = if (isSelected) 1.04f else 1.0f,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessMediumLow
                        ),
                        label = "size_chip_scale"
                    )
                    FilterChip(
                        selected = isSelected,
                        onClick = { onConfigChange(config.copy(artworkSize = size)) },
                        label = { Text(size.displayName, style = MaterialTheme.typography.labelMedium) },
                        leadingIcon = if (isSelected) {
                            {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        } else null,
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = accentColor.copy(alpha = 0.22f),
                            selectedLabelColor = accentColor,
                            selectedLeadingIconColor = accentColor
                        ),
                        modifier = Modifier
                            .graphicsLayer {
                                scaleX = scale
                                scaleY = scale
                            }
                            .springPressPhysics()
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 3. SCRUBBER STYLE
            CustomizerSectionTitle("Scrubber / Progress Bar Style")
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                M3ScrubberStyle.entries.forEach { scrubber ->
                    val isSelected = config.scrubberStyle == scrubber
                    val scale by animateFloatAsState(
                        targetValue = if (isSelected) 1.04f else 1.0f,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessMediumLow
                        ),
                        label = "scrubber_chip_scale"
                    )
                    FilterChip(
                        selected = isSelected,
                        onClick = { onConfigChange(config.copy(scrubberStyle = scrubber)) },
                        label = { Text(scrubber.displayName, style = MaterialTheme.typography.labelMedium) },
                        leadingIcon = if (isSelected) {
                            {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        } else null,
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = accentColor.copy(alpha = 0.22f),
                            selectedLabelColor = accentColor,
                            selectedLeadingIconColor = accentColor
                        ),
                        modifier = Modifier
                            .graphicsLayer {
                                scaleX = scale
                                scaleY = scale
                            }
                            .springPressPhysics()
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 4. HERO BUTTON SHAPE
            CustomizerSectionTitle("Hero Play/Pause Button Shape")
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                M3HeroButtonStyle.entries.forEach { btnStyle ->
                    val isSelected = config.heroButtonStyle == btnStyle
                    val scale by animateFloatAsState(
                        targetValue = if (isSelected) 1.04f else 1.0f,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessMediumLow
                        ),
                        label = "hero_chip_scale"
                    )
                    FilterChip(
                        selected = isSelected,
                        onClick = { onConfigChange(config.copy(heroButtonStyle = btnStyle)) },
                        label = { Text(btnStyle.displayName, style = MaterialTheme.typography.labelMedium) },
                        leadingIcon = if (isSelected) {
                            {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        } else null,
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = accentColor.copy(alpha = 0.22f),
                            selectedLabelColor = accentColor,
                            selectedLeadingIconColor = accentColor
                        ),
                        modifier = Modifier
                            .graphicsLayer {
                                scaleX = scale
                                scaleY = scale
                            }
                            .springPressPhysics()
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 4.5. SECONDARY ACTION BUTTON STYLE
            CustomizerSectionTitle("Secondary Control Buttons Style")
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                com.example.model.M3ControlButtonStyle.entries.forEach { ctrlStyle ->
                    val isSelected = config.controlButtonStyle == ctrlStyle
                    FilterChip(
                        selected = isSelected,
                        onClick = { onConfigChange(config.copy(controlButtonStyle = ctrlStyle)) },
                        label = { Text(ctrlStyle.displayName, style = MaterialTheme.typography.labelMedium) },
                        leadingIcon = if (isSelected) {
                            { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                        } else null,
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = accentColor.copy(alpha = 0.22f),
                            selectedLabelColor = accentColor,
                            selectedLeadingIconColor = accentColor
                        ),
                        modifier = Modifier.springPressPhysics()
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 4.6. INFO ALIGNMENT
            CustomizerSectionTitle("Track Title & Metadata Alignment")
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                com.example.model.M3InfoAlignment.entries.forEach { infoAlign ->
                    val isSelected = config.infoAlignment == infoAlign
                    FilterChip(
                        selected = isSelected,
                        onClick = { onConfigChange(config.copy(infoAlignment = infoAlign)) },
                        label = { Text(infoAlign.displayName, style = MaterialTheme.typography.labelMedium) },
                        leadingIcon = if (isSelected) {
                            { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                        } else null,
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = accentColor.copy(alpha = 0.22f),
                            selectedLabelColor = accentColor,
                            selectedLeadingIconColor = accentColor
                        ),
                        modifier = Modifier.springPressPhysics()
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 4.7. MOTION PHYSICS PROFILE
            CustomizerSectionTitle("Motion Physics Profile")
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                com.example.model.M3MotionProfile.entries.forEach { motion ->
                    val isSelected = config.motionProfile == motion
                    FilterChip(
                        selected = isSelected,
                        onClick = { onConfigChange(config.copy(motionProfile = motion)) },
                        label = { Text(motion.displayName, style = MaterialTheme.typography.labelMedium) },
                        leadingIcon = if (isSelected) {
                            { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                        } else null,
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = accentColor.copy(alpha = 0.22f),
                            selectedLabelColor = accentColor,
                            selectedLeadingIconColor = accentColor
                        ),
                        modifier = Modifier.springPressPhysics()
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 5. BACKGROUND TREATMENT
            CustomizerSectionTitle("Background Treatment")
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                M3BackgroundStyle.entries.forEach { bgStyle ->
                    val isSelected = config.backgroundStyle == bgStyle
                    val scale by animateFloatAsState(
                        targetValue = if (isSelected) 1.04f else 1.0f,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessMediumLow
                        ),
                        label = "bg_chip_scale"
                    )
                    FilterChip(
                        selected = isSelected,
                        onClick = { onConfigChange(config.copy(backgroundStyle = bgStyle)) },
                        label = { Text(bgStyle.displayName, style = MaterialTheme.typography.labelMedium) },
                        leadingIcon = if (isSelected) {
                            {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        } else null,
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = accentColor.copy(alpha = 0.22f),
                            selectedLabelColor = accentColor,
                            selectedLeadingIconColor = accentColor
                        ),
                        modifier = Modifier
                            .graphicsLayer {
                                scaleX = scale
                                scaleY = scale
                            }
                            .springPressPhysics()
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
            Spacer(modifier = Modifier.height(16.dp))

            // 6. MODULAR COMPONENT TOGGLES CARD
            CustomizerSectionTitle("Modules & Widgets: Choose What You Wanna Have")
            Spacer(modifier = Modifier.height(8.dp))

            Surface(
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.7f),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(vertical = 6.dp, horizontal = 12.dp)) {
                    CustomizerToggleItem(
                        title = "Live Audio Visualizer",
                        subtitle = "Waveform and spectrum frequency bars",
                        checked = config.showVisualizer,
                        onCheckedChange = { onConfigChange(config.copy(showVisualizer = it)) },
                        accentColor = accentColor
                    )

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f), modifier = Modifier.padding(horizontal = 4.dp))

                    CustomizerToggleItem(
                        title = "Hi-Res Audio Specs Badge",
                        subtitle = "Lossless FLAC, bit depth, kHz & bitrate badge",
                        checked = config.showAudioSpecs,
                        onCheckedChange = { onConfigChange(config.copy(showAudioSpecs = it)) },
                        accentColor = accentColor
                    )

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f), modifier = Modifier.padding(horizontal = 4.dp))

                    CustomizerToggleItem(
                        title = "Volume Bar & Sound Slider",
                        subtitle = "Inline sound level adjustment bar",
                        checked = config.showVolumeSlider,
                        onCheckedChange = { onConfigChange(config.copy(showVolumeSlider = it)) },
                        accentColor = accentColor
                    )

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f), modifier = Modifier.padding(horizontal = 4.dp))

                    CustomizerToggleItem(
                        title = "Output Device Routing Capsule",
                        subtitle = "Quick Bluetooth / Speaker switch pill in top bar",
                        checked = config.showDeviceCapsule,
                        onCheckedChange = { onConfigChange(config.copy(showDeviceCapsule = it)) },
                        accentColor = accentColor
                    )

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f), modifier = Modifier.padding(horizontal = 4.dp))

                    CustomizerToggleItem(
                        title = "Quick Action Pills Toolbar",
                        subtitle = "Queue, In-Place Lyrics, Sleep Timer, Speed, Equalizer",
                        checked = config.showActionPills,
                        onCheckedChange = { onConfigChange(config.copy(showActionPills = it)) },
                        accentColor = accentColor
                    )

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f), modifier = Modifier.padding(horizontal = 4.dp))

                    CustomizerToggleItem(
                        title = "Shuffle & Repeat Controls",
                        subtitle = "Side transport mode buttons",
                        checked = config.showShuffleRepeat,
                        onCheckedChange = { onConfigChange(config.copy(showShuffleRepeat = it)) },
                        accentColor = accentColor
                    )

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f), modifier = Modifier.padding(horizontal = 4.dp))

                    CustomizerToggleItem(
                        title = "Favorite Heart Button",
                        subtitle = "Quick heart action in title header",
                        checked = config.showFavoriteButton,
                        onCheckedChange = { onConfigChange(config.copy(showFavoriteButton = it)) },
                        accentColor = accentColor
                    )

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f), modifier = Modifier.padding(horizontal = 4.dp))

                    CustomizerToggleItem(
                        title = "Album & Format Subtitle",
                        subtitle = "Extended metadata beside artist name",
                        checked = config.showTrackDetails,
                        onCheckedChange = { onConfigChange(config.copy(showTrackDetails = it)) },
                        accentColor = accentColor
                    )

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f), modifier = Modifier.padding(horizontal = 4.dp))

                    CustomizerToggleItem(
                        title = "Lyrics Quick Peek Drawer",
                        subtitle = "Mini card previewing active synced lyric line",
                        checked = config.showLyricsCard,
                        onCheckedChange = { onConfigChange(config.copy(showLyricsCard = it)) },
                        accentColor = accentColor
                    )

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f), modifier = Modifier.padding(horizontal = 4.dp))

                    CustomizerToggleItem(
                        title = "Expressive Tactile Spring Haptics",
                        subtitle = "Vibration response on play, seek, and actions",
                        checked = config.expressiveHaptics,
                        onCheckedChange = { onConfigChange(config.copy(expressiveHaptics = it)) },
                        accentColor = accentColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))
        }
    }
}

@Composable
private fun CustomizerSectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
        color = MaterialTheme.colorScheme.onSurface
    )
    Spacer(modifier = Modifier.height(8.dp))
}

@Composable
fun CustomizerToggleItem(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    accentColor: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable { onCheckedChange(!checked) }
            .padding(vertical = 10.dp, horizontal = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 12.sp
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = accentColor
            ),
            modifier = Modifier.springPressPhysics()
        )
    }
}

private fun formatDuration(ms: Long): String {
    val totalSec = (ms / 1000L).coerceAtLeast(0L)
    val m = totalSec / 60L
    val s = totalSec % 60L
    return String.format(Locale.getDefault(), "%d:%02d", m, s)
}

