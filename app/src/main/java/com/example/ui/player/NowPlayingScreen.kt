package com.example.ui.player

import kotlin.math.PI
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.Crossfade
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode as AnimRepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.ui.graphics.graphicsLayer
import kotlinx.coroutines.delay
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.Edit
import com.example.ui.components.AudioWaveformVisualizer
import com.example.ui.components.AnimatedPlayPauseHeroButton
import com.example.ui.components.AnimatedSkipButton
import com.example.ui.components.AnimatedFavoriteButton
import com.example.ui.components.AnimatedTransportButton
import com.example.ui.components.AnimatedM3IconButton
import com.example.ui.components.AnimatedActionPill
import com.example.ui.theme.getFontFamilyForMusic
import com.example.ui.util.HapticType
import com.example.ui.util.rememberHapticController
import androidx.compose.material.icons.filled.Equalizer
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.Lyrics
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.QueueMusic
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.RepeatOne
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.Speaker
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineBreak
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AudioDeviceType
import com.example.model.LyricsScreenConfig
import com.example.model.LyricsUiState
import com.example.model.M3ExpressivePlayerConfig
import com.example.model.PlayerLayoutType
import com.example.model.PlayerUiState
import com.example.ui.components.ArtworkImageOrFallback
import com.example.ui.components.GlassmorphicBackground
import com.example.ui.components.M3ExpressiveProgressBar
import com.example.ui.components.PlaybackSpeedBottomSheet
import com.example.ui.components.rememberAlbumArtGlassPalette
import com.example.ui.lyrics.SyncedLyricsScreen
import com.example.ui.player.layouts.AppleMusicLyricsPlayerScreen
import com.example.ui.player.layouts.CardSplitPlayerLayout
import com.example.ui.player.layouts.GlassmorphicPlayerLayout
import com.example.ui.player.layouts.M3ExpressivePlayerLayout
import com.example.ui.player.layouts.WaveformPlayerLayout
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

/**
 * NowPlayingScreen with support for multiple layouts:
 * 1. Apple Music Glassmorphic
 * 2. Material You Expressive Split-Card
 * 3. Circular Orbit Studio Player with Full System Theme & Dynamic Color support
 */
@Composable
fun NowPlayingScreen(
    uiState: PlayerUiState,
    layoutType: PlayerLayoutType = PlayerLayoutType.CIRCULAR_ORBIT,
    lyricsUiState: LyricsUiState,
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
    onShuffleToggle: () -> Unit,
    onRepeatToggle: () -> Unit,
    onSleepTimerClick: () -> Unit = {},
    onPlaybackSpeedClick: () -> Unit = {},
    onPlaybackSpeedChange: (Float) -> Unit = {},
    onOpenSettings: () -> Unit = {},
    onTrackDetailsClick: () -> Unit = {},
    onEditLyricsClick: () -> Unit = onLyricsToggle,
    blurIntensity: Float = 0.5f,
    isAmoledBlack: Boolean = false,
    showAudioDetails: Boolean = false,
    isGlassmorphicEffectEnabled: Boolean = true,
    snowballEffectEnabled: Boolean = true,
    liveMovingGradientEnabled: Boolean = true,
    showMusicVisualizer: Boolean = true,
    waveformVisualizerStyle: String = "NEON_BARS",
    onWaveformVisualizerStyleChange: ((String) -> Unit)? = null,
    m3Config: M3ExpressivePlayerConfig = M3ExpressivePlayerConfig(),
    onM3ConfigChange: (M3ExpressivePlayerConfig) -> Unit = {},
    lyricsConfig: LyricsScreenConfig = LyricsScreenConfig(),
    changeFontsWithMusic: Boolean = true,
    defaultFontFamily: String = "DEFAULT",
    modifier: Modifier = Modifier
) {
    var showSpeedSheet by remember { mutableStateOf(false) }

    val handleSpeedClick = {
        showSpeedSheet = true
        onPlaybackSpeedClick()
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (uiState.isLyricsVisible) {
            SyncedLyricsScreen(
                uiState = lyricsUiState,
                currentTrack = uiState.currentTrack,
                onLyricClick = onSeekTo,
                onBackClick = onLyricsToggle,
                onFavoriteToggle = onFavoriteToggle,
                onPlayPauseClick = onPlayPauseClick,
                onPreviousClick = onPreviousClick,
                onNextClick = onNextClick,
                onEditLyricsClick = onEditLyricsClick,
                config = lyricsConfig,
                blurIntensity = lyricsConfig.blurIntensity,
                isAmoledBlack = lyricsConfig.backdropStyle == com.example.model.LyricsBackdropStyle.PURE_AMOLED_BLACK || isAmoledBlack,
                modifier = modifier
            )
        } else {
            when (layoutType) {
            PlayerLayoutType.APPLE_MUSIC -> {
                GlassmorphicPlayerLayout(
                    uiState = uiState,
                    lyricsUiState = lyricsUiState,
                    onPlayPauseClick = onPlayPauseClick,
                    onPreviousClick = onPreviousClick,
                    onNextClick = onNextClick,
                    onSeekTo = onSeekTo,
                    onFavoriteToggle = onFavoriteToggle,
                    onVolumeChange = onVolumeChange,
                    onLyricsToggle = onLyricsToggle,
                    onCollapseClick = onCollapseClick,
                    onDevicePickerClick = onDevicePickerClick,
                    onEqualizerClick = onEqualizerClick,
                    onQueueClick = onQueueClick,
                    onSleepTimerClick = onSleepTimerClick,
                    onPlaybackSpeedClick = handleSpeedClick,
                    onOpenSettings = onOpenSettings,
                    onMenuClick = onEqualizerClick,
                    blurIntensity = blurIntensity,
                    isAmoledBlack = isAmoledBlack,
                    isGlassmorphicEffectEnabled = isGlassmorphicEffectEnabled,
                    snowballEffectEnabled = snowballEffectEnabled,
                    liveMovingGradientEnabled = liveMovingGradientEnabled,
                    showMusicVisualizer = showMusicVisualizer,
                    showAudioDetails = showAudioDetails,
                    waveformVisualizerStyle = waveformVisualizerStyle,
                    onWaveformVisualizerStyleChange = onWaveformVisualizerStyleChange,
                    modifier = modifier
                )
            }
            PlayerLayoutType.MATERIAL_YOU_EXPRESSIVE -> {
                CardSplitPlayerLayout(
                    uiState = uiState,
                    lyricsUiState = lyricsUiState,
                    onPlayPauseClick = onPlayPauseClick,
                    onPreviousClick = onPreviousClick,
                    onNextClick = onNextClick,
                    onSeekTo = onSeekTo,
                    onFavoriteToggle = onFavoriteToggle,
                    onVolumeChange = onVolumeChange,
                    onLyricsToggle = onLyricsToggle,
                    onAlbumArtClick = onLyricsToggle,
                    onCollapseClick = onCollapseClick,
                    onDevicePickerClick = onDevicePickerClick,
                    onEqualizerClick = onEqualizerClick,
                    onQueueClick = onQueueClick,
                    onShuffleToggle = onShuffleToggle,
                    onRepeatToggle = onRepeatToggle,
                    onSleepTimerClick = onSleepTimerClick,
                    onPlaybackSpeedClick = handleSpeedClick,
                    onOpenSettings = onOpenSettings,
                    onMenuClick = onEqualizerClick,
                    blurIntensity = blurIntensity,
                    isAmoledBlack = isAmoledBlack,
                    showAudioDetails = showAudioDetails,
                    isGlassmorphicEffectEnabled = isGlassmorphicEffectEnabled,
                    showMusicVisualizer = showMusicVisualizer,
                    waveformVisualizerStyle = waveformVisualizerStyle,
                    onWaveformVisualizerStyleChange = onWaveformVisualizerStyleChange,
                    modifier = modifier
                )
            }
            PlayerLayoutType.M3_EXPRESSIVE -> {
                M3ExpressivePlayerLayout(
                    uiState = uiState,
                    lyricsUiState = lyricsUiState,
                    config = m3Config,
                    onConfigChange = onM3ConfigChange,
                    onPlayPauseClick = onPlayPauseClick,
                    onPreviousClick = onPreviousClick,
                    onNextClick = onNextClick,
                    onSeekTo = onSeekTo,
                    onFavoriteToggle = onFavoriteToggle,
                    onVolumeChange = onVolumeChange,
                    onLyricsToggle = onLyricsToggle,
                    onCollapseClick = onCollapseClick,
                    onDevicePickerClick = onDevicePickerClick,
                    onEqualizerClick = onEqualizerClick,
                    onQueueClick = onQueueClick,
                    onShuffleToggle = onShuffleToggle,
                    onRepeatToggle = onRepeatToggle,
                    onSleepTimerClick = onSleepTimerClick,
                    onPlaybackSpeedClick = handleSpeedClick,
                    onOpenSettings = onOpenSettings,
                    onMenuClick = onEqualizerClick,
                    blurIntensity = blurIntensity,
                    isAmoledBlack = isAmoledBlack,
                    showAudioDetails = showAudioDetails,
                    isGlassmorphicEffectEnabled = isGlassmorphicEffectEnabled,
                    showMusicVisualizer = showMusicVisualizer,
                    waveformVisualizerStyle = waveformVisualizerStyle,
                    onWaveformVisualizerStyleChange = onWaveformVisualizerStyleChange,
                    lyricsConfig = lyricsConfig,
                    modifier = modifier
                )
            }
            PlayerLayoutType.CIRCULAR_ORBIT -> {
                CircularOrbitPlayerScreen(
                    uiState = uiState,
                    lyricsUiState = lyricsUiState,
                    onPlayPauseClick = onPlayPauseClick,
                    onPreviousClick = onPreviousClick,
                    onNextClick = onNextClick,
                    onSeekTo = onSeekTo,
                    onFavoriteToggle = onFavoriteToggle,
                    onVolumeChange = onVolumeChange,
                    onLyricsToggle = onLyricsToggle,
                    onCollapseClick = onCollapseClick,
                    onDevicePickerClick = onDevicePickerClick,
                    onEqualizerClick = onEqualizerClick,
                    onQueueClick = onQueueClick,
                    onShuffleToggle = onShuffleToggle,
                    onRepeatToggle = onRepeatToggle,
                    onSleepTimerClick = onSleepTimerClick,
                    onPlaybackSpeedClick = handleSpeedClick,
                    onOpenSettings = onOpenSettings,
                    onTrackDetailsClick = onTrackDetailsClick,
                    blurIntensity = blurIntensity,
                    isAmoledBlack = isAmoledBlack,
                    isGlassmorphicEffectEnabled = isGlassmorphicEffectEnabled,
                    showMusicVisualizer = showMusicVisualizer,
                    showAudioDetails = showAudioDetails,
                    waveformVisualizerStyle = waveformVisualizerStyle,
                    onWaveformVisualizerStyleChange = onWaveformVisualizerStyleChange,
                    changeFontsWithMusic = changeFontsWithMusic,
                    defaultFontFamily = defaultFontFamily,
                    modifier = modifier
                )
            }
            PlayerLayoutType.WAVEFORM_CYBER_STUDIO -> {
                WaveformPlayerLayout(
                    uiState = uiState,
                    lyricsUiState = lyricsUiState,
                    onPlayPauseClick = onPlayPauseClick,
                    onPreviousClick = onPreviousClick,
                    onNextClick = onNextClick,
                    onSeekTo = onSeekTo,
                    onFavoriteToggle = onFavoriteToggle,
                    onVolumeChange = onVolumeChange,
                    onLyricsToggle = onLyricsToggle,
                    onCollapseClick = onCollapseClick,
                    onDevicePickerClick = onDevicePickerClick,
                    onEqualizerClick = onEqualizerClick,
                    onQueueClick = onQueueClick,
                    onShuffleToggle = onShuffleToggle,
                    onRepeatToggle = onRepeatToggle,
                    onSleepTimerClick = onSleepTimerClick,
                    onPlaybackSpeedClick = handleSpeedClick,
                    onOpenSettings = onOpenSettings,
                    onEditLyricsClick = onEditLyricsClick,
                    isAmoledBlack = isAmoledBlack,
                    showAudioDetails = showAudioDetails,
                    modifier = modifier
                )
            }
            PlayerLayoutType.VINYL_TURNTABLE -> {
                VinylTurntablePlayerLayout(
                    uiState = uiState,
                    lyricsUiState = lyricsUiState,
                    onPlayPauseClick = onPlayPauseClick,
                    onPreviousClick = onPreviousClick,
                    onNextClick = onNextClick,
                    onSeekTo = onSeekTo,
                    onFavoriteToggle = onFavoriteToggle,
                    onCollapseClick = onCollapseClick,
                    onLyricsToggle = onLyricsToggle,
                    onEqualizerClick = onEqualizerClick,
                    onQueueClick = onQueueClick,
                    onShuffleToggle = onShuffleToggle,
                    onRepeatToggle = onRepeatToggle,
                    onPlaybackSpeedClick = handleSpeedClick,
                    onOpenSettings = onOpenSettings,
                    isAmoledBlack = isAmoledBlack,
                    modifier = modifier
                )
            }
        }
    }

        if (showSpeedSheet) {
            PlaybackSpeedBottomSheet(
                currentSpeed = uiState.playbackSpeed,
                onSpeedSelected = { speed ->
                    onPlaybackSpeedChange(speed)
                },
                onDismiss = { showSpeedSheet = false },
                isAmoledBlack = isAmoledBlack
            )
        }
    }
}

/**
 * Layout 3: Dynamic Theme-Aware Circular Orbit Player.
 * Full system theme reactivity for Light, Dark, Material You wallpaper colors, and AMOLED Black.
 */
@Composable
private fun CircularOrbitPlayerScreen(
    uiState: PlayerUiState,
    lyricsUiState: LyricsUiState,
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
    onShuffleToggle: () -> Unit,
    onRepeatToggle: () -> Unit,
    onSleepTimerClick: () -> Unit = {},
    onPlaybackSpeedClick: () -> Unit = {},
    onOpenSettings: () -> Unit = {},
    onTrackDetailsClick: () -> Unit = {},
    blurIntensity: Float = 0.85f,
    isAmoledBlack: Boolean = false,
    isGlassmorphicEffectEnabled: Boolean = true,
    showMusicVisualizer: Boolean = true,
    showAudioDetails: Boolean = false,
    waveformVisualizerStyle: String = "NEON_BARS",
    onWaveformVisualizerStyleChange: ((String) -> Unit)? = null,
    changeFontsWithMusic: Boolean = true,
    defaultFontFamily: String = "DEFAULT",
    modifier: Modifier = Modifier
) {
    val track = uiState.currentTrack
    val glassPalette = rememberAlbumArtGlassPalette(track = track, isAmoledBlack = isAmoledBlack)
    val hapticController = rememberHapticController()

    // Dynamic Material 3 theme colors adapting to album art glass palette & glassmorphism toggle
    val isGlassmorphic = isGlassmorphicEffectEnabled
    val dynamicPrimary = if (isGlassmorphic) glassPalette.primary else MaterialTheme.colorScheme.primary
    val dynamicOnPrimary = if (isGlassmorphic) Color.White else MaterialTheme.colorScheme.onPrimary
    val dynamicTextColor = if (isGlassmorphic || isAmoledBlack) Color.White else MaterialTheme.colorScheme.onBackground
    val dynamicTextSecondary = if (isGlassmorphic || isAmoledBlack) Color.White.copy(alpha = 0.72f) else MaterialTheme.colorScheme.onSurfaceVariant
    val dynamicCardBg = if (isGlassmorphic) glassPalette.glassSurface else MaterialTheme.colorScheme.surfaceContainer
    val dynamicPillBg = if (isGlassmorphic) glassPalette.glassSurface else MaterialTheme.colorScheme.surfaceContainerHighest
    val dynamicBorder = if (isGlassmorphic) {
        BorderStroke(1.dp, glassPalette.glassBorder)
    } else {
        BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))
    }

    var showLyricsInPlace by remember { mutableStateOf(false) }
    var isMenuExpanded by remember { mutableStateOf(false) }
    val musicFontFamily = remember(uiState.currentTrack, changeFontsWithMusic, defaultFontFamily) {
        getFontFamilyForMusic(uiState.currentTrack, changeFontsWithMusic, defaultFontFamily)
    }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .testTag("circular_orbit_player_layout")
    ) {
        val screenHeight = maxHeight
        val scrollState = rememberScrollState()

        if (isGlassmorphicEffectEnabled) {
            // Adaptive Glassmorphic Background with album art palette & blur
            GlassmorphicBackground(
                track = track,
                blurIntensity = blurIntensity,
                isAmoledBlack = isAmoledBlack,
                isPlaying = uiState.isPlaying
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(if (isAmoledBlack) Color.Black else MaterialTheme.colorScheme.background)
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp, vertical = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 1. TOP BAR ("Your Music", Back Arrow, Favorite Heart + 3-dot Menu)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp, bottom = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                AnimatedM3IconButton(
                    icon = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = dynamicTextColor,
                    onClick = {
                        hapticController.performHaptic(HapticType.LIGHT_CLICK)
                        onCollapseClick()
                    },
                    modifier = Modifier.size(44.dp),
                    testTag = "orbit_back_button"
                )

                Text(
                    text = "Your Music",
                    color = dynamicTextColor,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    AnimatedFavoriteButton(
                        isFavorite = uiState.isFavorite,
                        activeColor = dynamicPrimary,
                        inactiveColor = dynamicTextColor.copy(alpha = 0.7f),
                        onToggle = {
                            hapticController.performHaptic(HapticType.DOUBLE_CLICK)
                            onFavoriteToggle()
                        }
                    )

                    Box {
                        AnimatedM3IconButton(
                            icon = Icons.Default.MoreVert,
                            contentDescription = "More Options",
                            tint = dynamicTextColor.copy(alpha = 0.85f),
                            onClick = {
                                hapticController.performHaptic(HapticType.LIGHT_CLICK)
                                isMenuExpanded = !isMenuExpanded
                            },
                            modifier = Modifier.size(44.dp),
                            testTag = "orbit_overflow_menu"
                        )

                    DropdownMenu(
                        expanded = isMenuExpanded,
                        onDismissRequest = { isMenuExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Settings") },
                            leadingIcon = { Icon(Icons.Default.Settings, null) },
                            onClick = {
                                isMenuExpanded = false
                                onOpenSettings()
                            }
                        )
                        val speedLabel = if (abs(uiState.playbackSpeed - 1.0f) < 0.02f) "Playback Speed (1.0x)" else "Playback Speed (${uiState.playbackSpeed}x)"
                        DropdownMenuItem(
                            text = { Text(speedLabel) },
                            leadingIcon = { Icon(Icons.Default.Speed, null) },
                            onClick = {
                                isMenuExpanded = false
                                onPlaybackSpeedClick()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Sleep Timer") },
                            leadingIcon = { Icon(Icons.Default.Bedtime, null) },
                            onClick = {
                                isMenuExpanded = false
                                onSleepTimerClick()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Equalizer & Sound Effects") },
                            leadingIcon = { Icon(Icons.Default.Equalizer, null) },
                            onClick = {
                                isMenuExpanded = false
                                onEqualizerClick()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Synchronized Lyrics") },
                            leadingIcon = { Icon(Icons.Default.Lyrics, null) },
                            onClick = {
                                isMenuExpanded = false
                                onLyricsToggle()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Playing Queue") },
                            leadingIcon = { Icon(Icons.Default.QueueMusic, null) },
                            onClick = {
                                isMenuExpanded = false
                                onQueueClick()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Music Details & Telemetry") },
                            leadingIcon = { Icon(Icons.Default.Headphones, null) },
                            onClick = {
                                isMenuExpanded = false
                                onTrackDetailsClick()
                            }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 2. CENTER ARTWORK & ORBITAL VISUALIZER (OR IN-PLACE LYRICS)
        val centerOrbitSize = when {
            screenHeight < 680.dp -> 210.dp
            screenHeight < 800.dp -> 255.dp
            else -> 290.dp
        }
        Box(
            modifier = Modifier
                .size(centerOrbitSize)
                .padding(vertical = 4.dp),
            contentAlignment = Alignment.Center
        ) {
            Crossfade(
                targetState = showLyricsInPlace,
                animationSpec = tween(350),
                label = "LyricsOrVisualizerCrossfade"
            ) { inLyricsMode ->
                if (inLyricsMode) {
                    // In-Place Synced Lyrics View
                    InPlaceLyricsView(
                        lyricsUiState = lyricsUiState,
                        currentPositionMs = uiState.currentPositionMs,
                        cardBackground = dynamicCardBg,
                        primaryColor = dynamicPrimary,
                        textColor = dynamicTextColor,
                        textSecondary = dynamicTextSecondary,
                        borderStroke = dynamicBorder,
                        onSeekTo = onSeekTo,
                        onDismiss = { showLyricsInPlace = false },
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    // Center Artwork with Pulsing Orbital Radial Visualizer
                    OrbitalVisualizerArtwork(
                        uiState = uiState,
                        primaryColor = dynamicPrimary,
                        secondaryBarColor = dynamicTextColor.copy(alpha = 0.45f),
                        artworkContainerColor = dynamicPillBg,
                        showMusicVisualizer = showMusicVisualizer,
                        onClick = { showLyricsInPlace = true }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 3. TRACK INFO (Title, Artist, Hi-Res Badge)
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = uiState.currentTrack.title.ifBlank { "Local Track" },
                fontFamily = musicFontFamily,
                color = dynamicTextColor,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = uiState.currentTrack.artist.ifBlank { "Unknown Artist" },
                fontFamily = musicFontFamily,
                color = dynamicTextSecondary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center
            )

            // Audiophile Tag Pill (respects showAudioDetails setting)
            if (showAudioDetails) {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    shape = RoundedCornerShape(50),
                    color = dynamicPillBg,
                    border = dynamicBorder,
                    modifier = Modifier.testTag("audio_quality_badge_orbit")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(dynamicPrimary)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "${uiState.currentTrack.formatName.ifBlank { "FLAC" }} • ${uiState.currentTrack.sampleRateHz / 1000}kHz / ${uiState.currentTrack.bitDepth}-bit",
                            fontFamily = musicFontFamily,
                            color = dynamicTextColor.copy(alpha = 0.9f),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // 4. DYNAMIC WAVEFORM SCRUBBER WITH FLOATING TIMESTAMP BADGE
        WaveformScrubber(
            currentPositionMs = uiState.currentPositionMs,
            durationMs = uiState.durationMs,
            primaryColor = dynamicPrimary,
            playedBarColor = dynamicPrimary,
            unplayedBarColor = dynamicTextColor.copy(alpha = 0.22f),
            badgeContainerColor = dynamicCardBg,
            textSecondaryColor = dynamicTextSecondary,
            borderStroke = dynamicBorder,
            onSeekTo = onSeekTo,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(14.dp))

        // 5. PLAYBACK CONTROLS (Shuffle, Prev, Animated Play/Pause Hero, Next, Repeat)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            AnimatedTransportButton(
                icon = Icons.Default.Shuffle,
                contentDescription = "Shuffle",
                isActive = uiState.isShuffle,
                activeColor = dynamicPrimary,
                inactiveColor = dynamicTextColor.copy(alpha = 0.55f),
                onClick = {
                    hapticController.performHaptic(HapticType.LIGHT_CLICK)
                    onShuffleToggle()
                },
                testTag = "shuffle_button"
            )

            AnimatedSkipButton(
                isNext = false,
                tint = dynamicTextColor,
                onClick = {
                    hapticController.performHaptic(HapticType.LIGHT_CLICK)
                    onPreviousClick()
                }
            )

            // Prominent Animated Play/Pause Hero Button with live aura pulse & spring squash
            AnimatedPlayPauseHeroButton(
                isPlaying = uiState.isPlaying,
                primaryColor = dynamicPrimary,
                onPrimaryColor = dynamicOnPrimary,
                onClick = {
                    hapticController.performHaptic(HapticType.MEDIUM_CLICK)
                    onPlayPauseClick()
                }
            )

            AnimatedSkipButton(
                isNext = true,
                tint = dynamicTextColor,
                onClick = {
                    hapticController.performHaptic(HapticType.LIGHT_CLICK)
                    onNextClick()
                }
            )

            AnimatedTransportButton(
                icon = if (uiState.isRepeat) Icons.Default.RepeatOne else Icons.Default.Repeat,
                contentDescription = "Repeat",
                isActive = uiState.isRepeat,
                activeColor = dynamicPrimary,
                inactiveColor = dynamicTextColor.copy(alpha = 0.55f),
                onClick = {
                    hapticController.performHaptic(HapticType.LIGHT_CLICK)
                    onRepeatToggle()
                },
                testTag = "repeat_button"
            )
        }

        if (showMusicVisualizer) {
            Spacer(modifier = Modifier.height(8.dp))

            val activeStyle = try {
                com.example.ui.components.WaveformVisualizerStyle.valueOf(waveformVisualizerStyle)
            } catch (_: Exception) {
                com.example.ui.components.WaveformVisualizerStyle.NEON_BARS
            }

            // 5.5 ANIMATED REAL-TIME AUDIO WAVEFORM VISUALIZER
            AudioWaveformVisualizer(
                isPlaying = uiState.isPlaying,
                playbackSpeed = uiState.playbackSpeed,
                volume = uiState.volume,
                primaryColor = dynamicPrimary,
                secondaryColor = MaterialTheme.colorScheme.tertiary,
                accentColor = MaterialTheme.colorScheme.secondary,
                height = 36.dp,
                initialStyle = activeStyle,
                onStyleChange = { onWaveformVisualizerStyleChange?.invoke(it.name) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))
        } else {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            )
        }

        // 6. BOTTOM ACTION BAR (In-place Lyrics Toggle, Output Device, Equalizer, Queue) - Horizontally Scrollable
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Lyrics In-Place Toggle Pill
            Surface(
                shape = RoundedCornerShape(50),
                color = if (showLyricsInPlace) dynamicPrimary else dynamicPillBg,
                border = dynamicBorder,
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .clickable {
                        hapticController.performHaptic(HapticType.LIGHT_CLICK)
                        showLyricsInPlace = !showLyricsInPlace
                    }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Lyrics,
                        contentDescription = "Lyrics",
                        tint = if (showLyricsInPlace) dynamicOnPrimary else dynamicTextColor,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Lyrics",
                        color = if (showLyricsInPlace) dynamicOnPrimary else dynamicTextColor,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Output Device Shortcut Pill
            Surface(
                shape = RoundedCornerShape(50),
                color = dynamicPillBg,
                border = dynamicBorder,
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .clickable {
                        hapticController.performHaptic(HapticType.LIGHT_CLICK)
                        onDevicePickerClick()
                    }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val deviceIcon = when (uiState.activeDevice.type) {
                        AudioDeviceType.BLUETOOTH -> Icons.Default.Bluetooth
                        AudioDeviceType.WIRED_HEADPHONES,
                        AudioDeviceType.USB_DAC -> Icons.Default.Headphones
                        AudioDeviceType.SPEAKER -> Icons.Default.Speaker
                        else -> Icons.Default.Speaker
                    }
                    Icon(
                        imageVector = deviceIcon,
                        contentDescription = "Output Device",
                        tint = dynamicPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = uiState.activeDevice.name.take(16),
                        color = dynamicTextColor.copy(alpha = 0.9f),
                        fontSize = 12.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Playback Speed Toggle Pill
            val isSpeedActive = abs(uiState.playbackSpeed - 1.0f) >= 0.05f
            AnimatedActionPill(
                icon = Icons.Default.Speed,
                label = "${String.format(java.util.Locale.US, "%.2f", uiState.playbackSpeed).trimEnd('0').trimEnd('.')}x",
                isActive = isSpeedActive,
                activeColor = dynamicPrimary,
                inactiveColor = dynamicTextColor,
                backgroundColor = dynamicPillBg,
                onClick = {
                    hapticController.performHaptic(HapticType.LIGHT_CLICK)
                    onPlaybackSpeedClick()
                },
                testTag = "orbit_speed_pill"
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                AnimatedM3IconButton(
                    icon = Icons.Default.Bedtime,
                    contentDescription = "Sleep Timer",
                    tint = dynamicTextColor.copy(alpha = 0.85f),
                    size = 40.dp,
                    iconSize = 20.dp,
                    onClick = {
                        hapticController.performHaptic(HapticType.LIGHT_CLICK)
                        onSleepTimerClick()
                    },
                    testTag = "orbit_sleep_timer"
                )

                AnimatedM3IconButton(
                    icon = Icons.Default.Equalizer,
                    contentDescription = "Equalizer",
                    tint = dynamicTextColor.copy(alpha = 0.85f),
                    size = 40.dp,
                    iconSize = 20.dp,
                    onClick = {
                        hapticController.performHaptic(HapticType.LIGHT_CLICK)
                        onEqualizerClick()
                    },
                    testTag = "orbit_equalizer"
                )

                AnimatedM3IconButton(
                    icon = Icons.Default.QueueMusic,
                    contentDescription = "Queue",
                    tint = dynamicTextColor.copy(alpha = 0.85f),
                    size = 40.dp,
                    iconSize = 20.dp,
                    onClick = {
                        hapticController.performHaptic(HapticType.LIGHT_CLICK)
                        onQueueClick()
                    },
                    testTag = "orbit_queue"
                )
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
    }
    }
}

/**
 * Center circular album artwork surrounded by pulsing radial visualizer bars on Canvas.
 */
@Composable
private fun OrbitalVisualizerArtwork(
    uiState: PlayerUiState,
    primaryColor: Color,
    secondaryBarColor: Color,
    artworkContainerColor: Color,
    showMusicVisualizer: Boolean = true,
    onClick: () -> Unit
) {
    val isPlaying = uiState.isPlaying

    val infiniteTransition = rememberInfiniteTransition(label = "OrbitalPulseTransition")
    val pulsePhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2f * PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3000, easing = LinearEasing),
            repeatMode = AnimRepeatMode.Restart
        ),
        label = "pulsePhase"
    )

    Box(
        modifier = Modifier
            .size(280.dp)
            .clip(CircleShape)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        if (showMusicVisualizer) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val center = Offset(size.width / 2f, size.height / 2f)
                val innerRadius = 100.dp.toPx()
                val barCount = 54

                for (i in 0 until barCount) {
                    val angleRad = (i.toFloat() / barCount) * 2f * PI.toFloat()
                    val dirX = cos(angleRad)
                    val dirY = sin(angleRad)

                    val waveFactor = if (isPlaying) {
                        0.5f + 0.5f * sin(angleRad * 4f + pulsePhase)
                    } else {
                        0.3f
                    }
                    val rawHeight = (8.dp.toPx() + waveFactor * 20.dp.toPx())
                    val start = Offset(center.x + dirX * innerRadius, center.y + dirY * innerRadius)
                    val end = Offset(center.x + dirX * (innerRadius + rawHeight), center.y + dirY * (innerRadius + rawHeight))

                    val barColor = if (i % 4 == 0) primaryColor else secondaryBarColor

                    drawLine(
                        color = barColor,
                        start = start,
                        end = end,
                        strokeWidth = 2.5.dp.toPx(),
                        cap = StrokeCap.Round
                    )
                }
            }
        }

        Box(
            modifier = Modifier
                .size(190.dp)
                .clip(CircleShape)
                .background(artworkContainerColor),
            contentAlignment = Alignment.Center
        ) {
            ArtworkImageOrFallback(
                track = uiState.currentTrack,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

/**
 * Interactive Waveform scrubber supporting dynamic theme colors.
 */
@Composable
private fun WaveformScrubber(
    currentPositionMs: Long,
    durationMs: Long,
    primaryColor: Color,
    playedBarColor: Color,
    unplayedBarColor: Color,
    badgeContainerColor: Color,
    textSecondaryColor: Color,
    borderStroke: BorderStroke,
    onSeekTo: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val totalMs = durationMs.coerceAtLeast(1L)
    val progressFraction = (currentPositionMs.toFloat() / totalMs).coerceIn(0f, 1f)

    var isDragging by remember { mutableStateOf(false) }
    var dragFraction by remember { mutableFloatStateOf(0f) }

    val activeFraction = if (isDragging) dragFraction else progressFraction
    val activeMs = (activeFraction * totalMs).toLong()

    val barHeights = remember {
        listOf(
            0.3f, 0.45f, 0.6f, 0.8f, 0.5f, 0.35f, 0.7f, 0.9f, 0.65f, 0.4f,
            0.75f, 0.85f, 1.0f, 0.9f, 0.7f, 0.5f, 0.65f, 0.8f, 0.95f, 0.6f,
            0.4f, 0.7f, 0.85f, 1.0f, 0.8f, 0.6f, 0.75f, 0.9f, 0.65f, 0.45f,
            0.35f, 0.55f, 0.7f, 0.85f, 0.6f, 0.4f, 0.65f, 0.8f, 0.5f, 0.35f,
            0.45f, 0.3f
        )
    }

    Column(modifier = modifier) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .height(68.dp)
        ) {
            val totalWidthPx = constraints.maxWidth.toFloat()
            val clampedThumbX = (activeFraction * totalWidthPx).coerceIn(0f, totalWidthPx)

            val badgeWidthDp = 52.dp
            val badgeWidthPx = badgeWidthDp.value * (totalWidthPx / maxWidth.value)
            val badgeOffsetXDp = (clampedThumbX / (totalWidthPx / maxWidth.value) - (badgeWidthDp.value / 2f))
                .coerceIn(0f, (maxWidth.value - badgeWidthDp.value))

            Box(
                modifier = Modifier
                    .offset(x = badgeOffsetXDp.dp, y = 0.dp)
                    .width(badgeWidthDp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = badgeContainerColor,
                        border = borderStroke
                    ) {
                        Text(
                            text = formatTime(activeMs),
                            color = primaryColor,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    Canvas(modifier = Modifier.size(6.dp, 4.dp)) {
                        val path = Path().apply {
                            moveTo(0f, 0f)
                            lineTo(size.width, 0f)
                            lineTo(size.width / 2f, size.height)
                            close()
                        }
                        drawPath(path, color = badgeContainerColor)
                    }
                }
            }

            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(36.dp)
                    .align(Alignment.BottomCenter)
                    .pointerInput(Unit) {
                        detectTapGestures { offset ->
                            val fraction = (offset.x / size.width).coerceIn(0f, 1f)
                            onSeekTo((fraction * totalMs).toLong())
                        }
                    }
                    .pointerInput(Unit) {
                        detectDragGestures(
                            onDragStart = { offset ->
                                isDragging = true
                                dragFraction = (offset.x / size.width).coerceIn(0f, 1f)
                            },
                            onDragEnd = {
                                onSeekTo((dragFraction * totalMs).toLong())
                                isDragging = false
                            },
                            onDragCancel = {
                                isDragging = false
                            },
                            onDrag = { change, _ ->
                                dragFraction = (change.position.x / size.width).coerceIn(0f, 1f)
                            }
                        )
                    }
            ) {
                val barCount = barHeights.size
                val spacingPx = 4.dp.toPx()
                val totalSpacing = spacingPx * (barCount - 1)
                val barWidth = (size.width - totalSpacing) / barCount

                for (i in 0 until barCount) {
                    val x = i * (barWidth + spacingPx)
                    val barFraction = i.toFloat() / barCount
                    val isPlayed = barFraction <= activeFraction

                    val h = barHeights[i] * size.height
                    val topY = (size.height - h) / 2f
                    val bottomY = topY + h

                    val color = if (isPlayed) playedBarColor else unplayedBarColor

                    drawLine(
                        color = color,
                        start = Offset(x + barWidth / 2f, topY),
                        end = Offset(x + barWidth / 2f, bottomY),
                        strokeWidth = barWidth.coerceAtLeast(2.dp.toPx()),
                        cap = StrokeCap.Round
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = formatTime(currentPositionMs),
                color = textSecondaryColor,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )

            val remainingMs = (durationMs - currentPositionMs).coerceAtLeast(0L)
            Text(
                text = "-${formatTime(remainingMs)}",
                color = textSecondaryColor,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

/**
 * In-place synced lyrics view supporting multi-language scripts and dynamic theming.
 */
@Composable
private fun InPlaceLyricsView(
    lyricsUiState: LyricsUiState,
    currentPositionMs: Long,
    cardBackground: Color,
    primaryColor: Color,
    textColor: Color,
    textSecondary: Color,
    borderStroke: BorderStroke,
    onSeekTo: (Long) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(24.dp),
        color = cardBackground,
        border = borderStroke,
        modifier = modifier
            .clip(RoundedCornerShape(24.dp))
            .clickable { onDismiss() }
    ) {
        when (lyricsUiState) {
            is LyricsUiState.Success -> {
                if (lyricsUiState.lines.isNotEmpty()) {
                    val listState = rememberLazyListState()
                    val activeIndex = lyricsUiState.lines.indexOfLast { it.timestampMs <= currentPositionMs }
                        .coerceAtLeast(0)

                    LaunchedEffect(activeIndex) {
                        listState.animateScrollToItem(
                            index = (activeIndex - 1).coerceAtLeast(0),
                            scrollOffset = -24
                        )
                    }

                    var entranceReady by remember(lyricsUiState.lines) { mutableStateOf(false) }
                    LaunchedEffect(lyricsUiState.lines) {
                        entranceReady = false
                        delay(30)
                        entranceReady = true
                    }

                    LazyColumn(
                        state = listState,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        itemsIndexed(lyricsUiState.lines, key = { index, line -> "${line.timestampMs}_$index" }) { idx, line ->
                            val isActive = idx == activeIndex
                            val distance = kotlin.math.abs(idx - activeIndex)
                            val isRtl = isRtlString(line.text)

                            var lineAppeared by remember(line) { mutableStateOf(false) }
                            LaunchedEffect(entranceReady, line) {
                                if (entranceReady) {
                                    delay((idx * 30).coerceAtMost(500).toLong())
                                    lineAppeared = true
                                } else {
                                    lineAppeared = false
                                }
                            }

                            val entranceProgress by animateFloatAsState(
                                targetValue = if (lineAppeared) 1f else 0f,
                                animationSpec = spring(
                                    dampingRatio = Spring.DampingRatioLowBouncy,
                                    stiffness = Spring.StiffnessLow
                                ),
                                label = "inplaceEntrance"
                            )

                            val slideOffsetY = (1f - entranceProgress) * 28f

                            val alpha by animateFloatAsState(
                                targetValue = if (isActive) 1.0f else if (distance == 1) 0.65f else 0.35f,
                                animationSpec = tween(durationMillis = 350, easing = FastOutSlowInEasing),
                                label = "inplaceAlpha"
                            )
                            val scale = 1.0f

                            CompositionLocalProvider(
                                LocalLayoutDirection provides if (isRtl) LayoutDirection.Rtl else LayoutDirection.Ltr
                            ) {
                                Text(
                                    text = line.text,
                                    color = if (isActive) primaryColor else textColor,
                                    fontSize = if (isActive) 17.sp else 16.sp,
                                    fontWeight = if (isActive) FontWeight.Bold else FontWeight.Medium,
                                    textAlign = TextAlign.Center,
                                    style = MaterialTheme.typography.bodyLarge.copy(
                                        lineBreak = LineBreak.Paragraph
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .graphicsLayer {
                                            scaleX = scale
                                            scaleY = scale
                                            this.alpha = alpha * entranceProgress
                                            translationY = slideOffsetY
                                        }
                                        .clickable { onSeekTo(line.timestampMs) }
                                )
                            }
                        }
                    }
                } else if (!lyricsUiState.plainLyrics.isNullOrBlank()) {
                    Text(
                        text = lyricsUiState.plainLyrics,
                        color = textColor.copy(alpha = 0.85f),
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            lineBreak = LineBreak.Paragraph
                        ),
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                    )
                } else {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            text = "No lyrics found for this track",
                            color = textSecondary,
                            fontSize = 14.sp
                        )
                    }
                }
            }
            is LyricsUiState.Loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(text = "Searching multi-language lyrics...", color = primaryColor, fontSize = 14.sp)
                }
            }
            else -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(text = "No lyrics available\n(Tap to return)", color = textSecondary, fontSize = 14.sp, textAlign = TextAlign.Center)
                }
            }
        }
    }
}

private fun isRtlString(text: String): Boolean {
    val trimmed = text.trim()
    if (trimmed.isEmpty()) return false
    val firstChar = trimmed.first()
    val direction = Character.getDirectionality(firstChar)
    return direction == Character.DIRECTIONALITY_RIGHT_TO_LEFT ||
            direction == Character.DIRECTIONALITY_RIGHT_TO_LEFT_ARABIC
}

private fun formatTime(ms: Long): String {
    val totalSeconds = (ms / 1000).coerceAtLeast(0)
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format("%d:%02d", minutes, seconds)
}

@Composable
private fun VinylTurntablePlayerLayout(
    uiState: PlayerUiState,
    lyricsUiState: LyricsUiState,
    onPlayPauseClick: () -> Unit,
    onPreviousClick: () -> Unit,
    onNextClick: () -> Unit,
    onSeekTo: (Long) -> Unit,
    onFavoriteToggle: () -> Unit,
    onCollapseClick: () -> Unit,
    onLyricsToggle: () -> Unit,
    onEqualizerClick: () -> Unit,
    onQueueClick: () -> Unit,
    onShuffleToggle: () -> Unit,
    onRepeatToggle: () -> Unit,
    onPlaybackSpeedClick: () -> Unit,
    onOpenSettings: () -> Unit,
    isAmoledBlack: Boolean,
    modifier: Modifier = Modifier
) {
    val haptic = rememberHapticController()
    val primaryColor = MaterialTheme.colorScheme.primary
    val backgroundColor = if (isAmoledBlack) Color.Black else Color(0xFF121418)
    val plinthColor = if (isAmoledBlack) Color(0xFF181A20) else Color(0xFF1E222B)
    val plinthBorder = Color(0xFF384050)
    val textColor = Color.White
    val textSecondary = Color(0xFFA0AAB8)

    val infiniteTransition = rememberInfiniteTransition(label = "vinyl_spin")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = LinearEasing),
            repeatMode = AnimRepeatMode.Restart
        ),
        label = "vinyl_deg"
    )

    val tonearmAngle by animateFloatAsState(
        targetValue = if (uiState.isPlaying) 28f else 0f,
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "tonearm"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(backgroundColor)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 20.dp, vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Top Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            AnimatedM3IconButton(
                icon = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = textColor,
                onClick = onCollapseClick
            )
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "HI-FI TURNTABLE",
                    style = MaterialTheme.typography.labelSmall.copy(
                        letterSpacing = 1.8.sp,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                    ),
                    color = primaryColor
                )
                Text(
                    text = "DIRECT DRIVE • 33⅓ RPM",
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
                    color = textSecondary
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                AnimatedTransportButton(
                    icon = Icons.Default.Lyrics,
                    contentDescription = "Lyrics",
                    isActive = uiState.isLyricsVisible,
                    activeColor = primaryColor,
                    inactiveColor = textSecondary,
                    onClick = onLyricsToggle
                )
                AnimatedM3IconButton(
                    icon = Icons.Default.Settings,
                    contentDescription = "Settings",
                    tint = textSecondary,
                    onClick = onOpenSettings
                )
            }
        }

        // Turntable Plinth Deck with Vinyl Record & Tonearm OR Vinyl Liner Notes Lyrics
        AnimatedContent(
            targetState = uiState.isLyricsVisible,
            transitionSpec = { fadeIn(tween(400)) togetherWith fadeOut(tween(300)) },
            label = "turntable_deck_lyrics"
        ) { isLyrics ->
            if (isLyrics) {
                VinylLinerNotesLyricsCard(
                    lyricsUiState = lyricsUiState,
                    currentPositionMs = uiState.currentPositionMs,
                    plinthColor = plinthColor,
                    primaryColor = primaryColor,
                    textColor = textColor,
                    textSecondary = textSecondary,
                    onSeekTo = onSeekTo,
                    onDismiss = onLyricsToggle,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                        .clip(RoundedCornerShape(28.dp))
                        .background(plinthColor)
                        .border(BorderStroke(2.dp, plinthBorder), RoundedCornerShape(28.dp))
                        .clickable { onLyricsToggle() },
                    contentAlignment = Alignment.Center
                ) {
                    // Turntable metallic platter under vinyl
                    Canvas(modifier = Modifier.size(260.dp)) {
                        drawCircle(color = Color(0xFF282D38))
                        drawCircle(color = Color(0xFF1E222B), radius = size.minDimension / 2.05f)
                    }

                    // Spinning Vinyl Disc
                    Box(
                        modifier = Modifier
                            .size(240.dp)
                            .graphicsLayer {
                                rotationZ = if (uiState.isPlaying) rotation else 0f
                            }
                            .clip(CircleShape)
                            .background(Color(0xFF0D0E11)),
                        contentAlignment = Alignment.Center
                    ) {
                        // Microgroove rings
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val center = androidx.compose.ui.geometry.Offset(size.width / 2f, size.height / 2f)
                            val maxR = size.minDimension / 2f
                            for (i in 1..8) {
                                drawCircle(
                                    color = Color(0xFF20242D).copy(alpha = 0.6f),
                                    radius = maxR * (0.42f + i * 0.065f),
                                    center = center,
                                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.5f)
                                )
                            }
                        }

                        // Center Album Label
                        Box(
                            modifier = Modifier
                                .size(86.dp)
                                .clip(CircleShape)
                                .border(BorderStroke(2.dp, Color(0xFFD4AF37)), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            ArtworkImageOrFallback(
                                track = uiState.currentTrack,
                                contentDescription = "Vinyl Label",
                                modifier = Modifier.fillMaxSize()
                            )
                            // Spindle hole
                            Box(
                                modifier = Modifier
                                    .size(16.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF1E222B))
                                    .border(BorderStroke(1.dp, Color.LightGray), CircleShape)
                            )
                        }
                    }

                    // Pivot Tonearm overlay in top-right
                    Canvas(
                        modifier = Modifier
                            .size(120.dp)
                            .align(Alignment.TopEnd)
                            .graphicsLayer {
                                rotationZ = tonearmAngle
                                transformOrigin = androidx.compose.ui.graphics.TransformOrigin(0.85f, 0.15f)
                            }
                    ) {
                        val pivotX = size.width * 0.85f
                        val pivotY = size.height * 0.15f
                        drawCircle(color = Color(0xFFC0C0C0), radius = 14f, center = androidx.compose.ui.geometry.Offset(pivotX, pivotY))
                        drawCircle(color = Color(0xFF606060), radius = 6f, center = androidx.compose.ui.geometry.Offset(pivotX, pivotY))
                        // Arm
                        drawLine(
                            color = Color(0xFFE0E0E0),
                            start = androidx.compose.ui.geometry.Offset(pivotX, pivotY),
                            end = androidx.compose.ui.geometry.Offset(size.width * 0.15f, size.height * 0.85f),
                            strokeWidth = 5f
                        )
                        // Cartridge / Stylus
                        drawRect(
                            color = Color(0xFFFF4081),
                            topLeft = androidx.compose.ui.geometry.Offset(size.width * 0.10f, size.height * 0.80f),
                            size = androidx.compose.ui.geometry.Size(20f, 14f)
                        )
                    }
                }
            }
        }

        // Track Info
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = uiState.currentTrack.title.ifBlank { "Analog Audio" },
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.Bold),
                color = textColor,
                maxLines = 1,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = uiState.currentTrack.artist.ifBlank { "Unknown Artist" },
                style = MaterialTheme.typography.bodyMedium,
                color = textSecondary,
                maxLines = 1,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
            )
        }

        // Progress Scrubber
        Column(modifier = Modifier.fillMaxWidth()) {
            val progress = if (uiState.durationMs > 0) {
                (uiState.currentPositionMs.toFloat() / uiState.durationMs).coerceIn(0f, 1f)
            } else 0f

            androidx.compose.material3.Slider(
                value = progress,
                onValueChange = { frac ->
                    val targetMs = (frac * uiState.durationMs).toLong()
                    onSeekTo(targetMs)
                },
                colors = androidx.compose.material3.SliderDefaults.colors(
                    thumbColor = primaryColor,
                    activeTrackColor = primaryColor,
                    inactiveTrackColor = Color(0xFF282D38)
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(formatTime(uiState.currentPositionMs), style = MaterialTheme.typography.labelSmall, color = textSecondary)
                Text(formatTime(uiState.durationMs), style = MaterialTheme.typography.labelSmall, color = textSecondary)
            }
        }

        // Transport Controls
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            AnimatedTransportButton(
                icon = Icons.Default.Shuffle,
                contentDescription = "Shuffle",
                isActive = uiState.isShuffle,
                activeColor = primaryColor,
                inactiveColor = textSecondary,
                onClick = onShuffleToggle
            )

            AnimatedSkipButton(
                isNext = false,
                tint = textColor,
                size = 52.dp,
                iconSize = 32.dp,
                onClick = {
                    haptic.performHaptic(HapticType.LIGHT_CLICK)
                    onPreviousClick()
                }
            )

            AnimatedPlayPauseHeroButton(
                isPlaying = uiState.isPlaying,
                primaryColor = primaryColor,
                onPrimaryColor = MaterialTheme.colorScheme.onPrimary,
                size = 68.dp,
                iconSize = 34.dp,
                onClick = {
                    haptic.performHaptic(HapticType.MEDIUM_CLICK)
                    onPlayPauseClick()
                }
            )

            AnimatedSkipButton(
                isNext = true,
                tint = textColor,
                size = 52.dp,
                iconSize = 32.dp,
                onClick = {
                    haptic.performHaptic(HapticType.LIGHT_CLICK)
                    onNextClick()
                }
            )

            AnimatedFavoriteButton(
                isFavorite = uiState.isFavorite,
                activeColor = Color(0xFFE91E63),
                inactiveColor = textSecondary,
                onToggle = onFavoriteToggle
            )
        }

        // Bottom utility icons - Scrollable
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            AnimatedTransportButton(
                icon = if (uiState.isRepeat) Icons.Default.RepeatOne else Icons.Default.Repeat,
                contentDescription = "Repeat",
                isActive = uiState.isRepeat,
                activeColor = primaryColor,
                inactiveColor = textSecondary,
                onClick = onRepeatToggle
            )
            AnimatedM3IconButton(
                icon = Icons.Default.Equalizer,
                contentDescription = "Equalizer",
                tint = textSecondary,
                onClick = onEqualizerClick
            )
            AnimatedM3IconButton(
                icon = Icons.Default.QueueMusic,
                contentDescription = "Queue",
                tint = textSecondary,
                onClick = onQueueClick
            )
            AnimatedM3IconButton(
                icon = Icons.Default.Speed,
                contentDescription = "Speed",
                tint = textSecondary,
                onClick = onPlaybackSpeedClick
            )
        }
    }
}

/**
 * Zen Mode Tranquil Synced Lyrics Experience
 * Serene typography, breathing ambient active line aura, direct seeking.
 */
@Composable
private fun ZenTranquilLyricsCard(
    lyricsUiState: LyricsUiState,
    currentPositionMs: Long,
    dynamicPrimary: Color,
    textColor: Color,
    textSecondary: Color,
    onSeekTo: (Long) -> Unit,
    onDismiss: () -> Unit,
    onEditLyrics: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(26.dp),
        color = Color(0x1A000000),
        border = BorderStroke(1.5.dp, dynamicPrimary.copy(alpha = 0.35f)),
        modifier = modifier.clickable { onDismiss() }
    ) {
        when (lyricsUiState) {
            is LyricsUiState.Success -> {
                if (lyricsUiState.lines.isNotEmpty()) {
                    val activeIndex = lyricsUiState.lines.indexOfLast { it.timestampMs <= currentPositionMs }
                        .coerceAtLeast(0)
                    val listState = rememberLazyListState()

                    LaunchedEffect(activeIndex) {
                        listState.animateScrollToItem((activeIndex - 1).coerceAtLeast(0))
                    }

                    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "ZEN • HARMONIC LYRICS",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    letterSpacing = 1.6.sp,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = dynamicPrimary
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = dynamicPrimary.copy(alpha = 0.16f),
                                    modifier = Modifier.clickable { onEditLyrics() }
                                ) {
                                    Text(
                                        text = "EDIT",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold
                                        ),
                                        color = dynamicPrimary,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "CLOSE",
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                    color = textSecondary,
                                    modifier = Modifier.clickable { onDismiss() }
                                )
                            }
                        }

                        LazyColumn(
                            state = listState,
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            itemsIndexed(lyricsUiState.lines) { idx, line ->
                                val isActive = idx == activeIndex
                                val isRtl = isRtlString(line.text)

                                CompositionLocalProvider(
                                    LocalLayoutDirection provides if (isRtl) LayoutDirection.Rtl else LayoutDirection.Ltr
                                ) {
                                    Text(
                                        text = line.text,
                                        color = if (isActive) textColor else textSecondary.copy(alpha = 0.5f),
                                        fontSize = if (isActive) 16.sp else 15.sp,
                                        fontWeight = if (isActive) FontWeight.ExtraBold else FontWeight.Medium,
                                        style = MaterialTheme.typography.bodyLarge.copy(
                                            lineBreak = LineBreak.Paragraph
                                        ),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(if (isActive) dynamicPrimary.copy(alpha = 0.15f) else Color.Transparent)
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                            .clickable { onSeekTo(line.timestampMs) }
                                    )
                                }
                            }
                        }
                    }
                } else if (!lyricsUiState.plainLyrics.isNullOrBlank()) {
                    Text(
                        text = lyricsUiState.plainLyrics,
                        color = textColor.copy(alpha = 0.9f),
                        fontSize = 14.sp,
                        style = MaterialTheme.typography.bodyMedium.copy(lineBreak = LineBreak.Paragraph),
                        modifier = Modifier.fillMaxSize().padding(20.dp)
                    )
                } else {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            text = "No tranquil lyrics found for this track\n(Tap to return)",
                            color = textSecondary,
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
            is LyricsUiState.Loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        CircularProgressIndicator(color = dynamicPrimary, modifier = Modifier.size(28.dp))
                        Text(text = "Harmonizing lyrics...", color = textSecondary, fontSize = 12.sp)
                    }
                }
            }
            else -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = "No synchronized lyrics available\n(Tap to return)",
                        color = textSecondary,
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

/**
 * Vinyl Turntable Liner Notes & Gatefold Lyrics Sheet
 * Vintage record-sleeve finish, tonearm needle stylus point tracking active lyric line.
 */
@Composable
private fun VinylLinerNotesLyricsCard(
    lyricsUiState: LyricsUiState,
    currentPositionMs: Long,
    plinthColor: Color,
    primaryColor: Color,
    textColor: Color,
    textSecondary: Color,
    onSeekTo: (Long) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(26.dp),
        color = plinthColor,
        border = BorderStroke(2.dp, Color(0xFFD4AF37).copy(alpha = 0.4f)),
        modifier = modifier.clickable { onDismiss() }
    ) {
        when (lyricsUiState) {
            is LyricsUiState.Success -> {
                if (lyricsUiState.lines.isNotEmpty()) {
                    val activeIndex = lyricsUiState.lines.indexOfLast { it.timestampMs <= currentPositionMs }
                        .coerceAtLeast(0)
                    val listState = rememberLazyListState()

                    LaunchedEffect(activeIndex) {
                        listState.animateScrollToItem((activeIndex - 1).coerceAtLeast(0))
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color(0xFF14161C))
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "SIDE A • LINER NOTES & LYRICS",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    letterSpacing = 1.8.sp,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = Color(0xFFD4AF37)
                            )
                            Text(
                                text = "NEEDLE DROP SEEK",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                color = textSecondary
                            )
                        }

                        LazyColumn(
                            state = listState,
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            itemsIndexed(lyricsUiState.lines) { idx, line ->
                                val isActive = idx == activeIndex
                                val isRtl = isRtlString(line.text)

                                CompositionLocalProvider(
                                    LocalLayoutDirection provides if (isRtl) LayoutDirection.Rtl else LayoutDirection.Ltr
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(if (isActive) Color(0x2BD4AF37) else Color.Transparent)
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                            .clickable { onSeekTo(line.timestampMs) },
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        if (isActive) {
                                            Text(
                                                text = "▶ ",
                                                color = Color(0xFFD4AF37),
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                        Text(
                                            text = line.text,
                                            color = if (isActive) Color(0xFFFFF6D6) else textSecondary.copy(alpha = 0.55f),
                                            fontSize = if (isActive) 16.sp else 13.sp,
                                            fontWeight = if (isActive) FontWeight.SemiBold else FontWeight.Normal,
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                lineBreak = LineBreak.Paragraph
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }
                } else if (!lyricsUiState.plainLyrics.isNullOrBlank()) {
                    Text(
                        text = lyricsUiState.plainLyrics,
                        color = Color(0xFFFFF6D6),
                        fontSize = 13.sp,
                        style = MaterialTheme.typography.bodyMedium.copy(lineBreak = LineBreak.Paragraph),
                        modifier = Modifier.fillMaxSize().padding(18.dp)
                    )
                } else {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            text = "No liner lyrics found on this vinyl release\n(Tap to return)",
                            color = textSecondary,
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
            is LyricsUiState.Loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        CircularProgressIndicator(color = Color(0xFFD4AF37), modifier = Modifier.size(28.dp))
                        Text(text = "Reading vinyl sleeve...", color = textSecondary, fontSize = 12.sp)
                    }
                }
            }
            else -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = "No synchronized lyrics on record\n(Tap to return)",
                        color = textSecondary,
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}
