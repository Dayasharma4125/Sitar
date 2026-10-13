package com.example.ui.player.layouts

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.ui.graphics.graphicsLayer
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.DragInteraction
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.Job
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.QueueMusic
import androidx.compose.material.icons.automirrored.filled.VolumeMute
import androidx.compose.material.icons.filled.Bedtime
import com.example.ui.components.AudioWaveformVisualizer
import com.example.ui.components.AnimatedPlayPauseHeroButton
import com.example.ui.components.AnimatedSkipButton
import com.example.ui.components.AnimatedFavoriteButton
import com.example.ui.components.AnimatedTransportButton
import com.example.ui.theme.getFontFamilyForMusic
import com.example.ui.theme.springPressPhysics
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.Equalizer
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.FastRewind
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Lyrics
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Speaker
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineBreak
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.LyricsUiState
import com.example.model.PlayerUiState
import com.example.ui.components.ArtworkImageOrFallback
import com.example.ui.components.GlassmorphicBackground
import com.example.ui.components.rememberAlbumArtGlassPalette
import com.example.ui.components.M3ExpressiveProgressBar
import com.example.ui.util.HapticType
import com.example.ui.util.rememberHapticController

/**
 * Layout 1: Apple Music Glassmorphic Player
 * Features dynamic blurred backdrop, synchronized scrolling lyrics overlay with tap-to-seek,
 * rounded artwork card, M3 wave progress bar, settings in 3-dot menu, and floating translucent control surfaces.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GlassmorphicPlayerLayout(
    uiState: PlayerUiState,
    lyricsUiState: LyricsUiState = LyricsUiState.NotFound(),
    onPlayPauseClick: () -> Unit,
    onPreviousClick: () -> Unit,
    onNextClick: () -> Unit,
    onSeekTo: (Long) -> Unit,
    onFavoriteToggle: () -> Unit,
    onVolumeChange: (Float) -> Unit,
    onLyricsToggle: () -> Unit,
    onAlbumArtClick: () -> Unit = onLyricsToggle,
    onCollapseClick: () -> Unit,
    onDevicePickerClick: () -> Unit,
    onEqualizerClick: () -> Unit,
    onQueueClick: () -> Unit,
    onSleepTimerClick: () -> Unit = {},
    onPlaybackSpeedClick: () -> Unit = {},
    onOpenSettings: () -> Unit = {},
    onMenuClick: () -> Unit = {},
    blurIntensity: Float,
    isAmoledBlack: Boolean,
    isGlassmorphicEffectEnabled: Boolean = true,
    snowballEffectEnabled: Boolean = true,
    liveMovingGradientEnabled: Boolean = true,
    showMusicVisualizer: Boolean = true,
    showAudioDetails: Boolean = false,
    waveformVisualizerStyle: String = "NEON_BARS",
    onWaveformVisualizerStyleChange: ((String) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val track = uiState.currentTrack
    var isMenuExpanded by remember { mutableStateOf(false) }
    val hapticController = rememberHapticController()
    val glassPalette = rememberAlbumArtGlassPalette(track = track, isAmoledBlack = isAmoledBlack)
    val isGlassmorphic = isGlassmorphicEffectEnabled
    val dynamicTextColor = if (isGlassmorphic || isAmoledBlack) Color.White else MaterialTheme.colorScheme.onBackground
    val dynamicTextSecondary = if (isGlassmorphic || isAmoledBlack) Color.White.copy(alpha = 0.72f) else MaterialTheme.colorScheme.onSurfaceVariant
    val dynamicPillColor = if (isGlassmorphic) glassPalette.glassSurface else MaterialTheme.colorScheme.surfaceContainerHighest
    val dynamicPlayContainer = if (isGlassmorphic) Color.White else MaterialTheme.colorScheme.primary
    val dynamicPlayContent = if (isGlassmorphic) Color(0xFF0F151D) else MaterialTheme.colorScheme.onPrimary
    val dynamicActivePill = if (isGlassmorphic) glassPalette.activePill else MaterialTheme.colorScheme.primaryContainer
    val dynamicActivePillContent = if (isGlassmorphic) glassPalette.activePillContent else MaterialTheme.colorScheme.onPrimaryContainer
    val dynamicBorder = if (isGlassmorphic) {
        BorderStroke(1.dp, glassPalette.glassBorder)
    } else {
        BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))
    }

    val albumScale by animateFloatAsState(
        targetValue = if (uiState.isPlaying) 1.0f else 0.90f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "albumScale"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .testTag("glassmorphic_player_layout")
    ) {
        if (isGlassmorphicEffectEnabled) {
            // Blurred Dynamic Ambient Layer adapting to album art palette
            GlassmorphicBackground(
                track = track,
                blurIntensity = blurIntensity,
                isAmoledBlack = isAmoledBlack,
                isPlaying = uiState.isPlaying,
                snowballEffectEnabled = snowballEffectEnabled,
                liveMovingGradientEnabled = liveMovingGradientEnabled
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
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Drag Handle
            Box(
                modifier = Modifier
                    .padding(top = 8.dp)
                    .size(width = 36.dp, height = 4.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.4f))
                    .clickable { onCollapseClick() }
            )

            // Header Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                FilledTonalIconButton(
                    onClick = onCollapseClick,
                    shape = CircleShape,
                    colors = IconButtonDefaults.filledTonalIconButtonColors(
                        containerColor = dynamicPillColor,
                        contentColor = dynamicTextColor
                    ),
                    modifier = Modifier
                        .size(38.dp)
                        .springPressPhysics()
                        .testTag("now_playing_collapse_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = "Collapse to Library",
                        modifier = Modifier.size(24.dp)
                    )
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 12.dp)
                ) {
                    Text(
                        text = "PLAYING FROM LIBRARY",
                        style = MaterialTheme.typography.labelSmall.copy(
                            letterSpacing = 1.2.sp,
                            fontSize = 10.sp
                        ),
                        color = dynamicTextSecondary
                    )
                    Text(
                        text = track.album.ifBlank { "Local Audio" },
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                        color = dynamicTextColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                FilledTonalIconButton(
                    onClick = onQueueClick,
                    shape = CircleShape,
                    colors = IconButtonDefaults.filledTonalIconButtonColors(
                        containerColor = dynamicPillColor,
                        contentColor = dynamicTextColor
                    ),
                    modifier = Modifier
                        .size(38.dp)
                        .springPressPhysics()
                        .testTag("now_playing_top_queue_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.QueueMusic,
                        contentDescription = "Queue",
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Center Display: Elevated Album Artwork Card OR Apple Music Synchronized Lyrics
            AnimatedContent(
                targetState = uiState.isLyricsVisible,
                transitionSpec = {
                    fadeIn(animationSpec = spring(stiffness = Spring.StiffnessMedium)) togetherWith
                        fadeOut(animationSpec = spring(stiffness = Spring.StiffnessMedium))
                },
                label = "AppleMusicCenterContent",
                modifier = Modifier
                    .fillMaxWidth(0.92f)
                    .weight(1f, fill = false)
                    .aspectRatio(1f)
            ) { isLyrics ->
                if (isLyrics) {
                    GlassmorphicLyricsCard(
                        lyricsUiState = lyricsUiState,
                        currentPositionMs = uiState.currentPositionMs,
                        onSeekTo = onSeekTo,
                        onDismiss = onLyricsToggle,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Card(
                        shape = RoundedCornerShape(26.dp),
                        elevation = CardDefaults.elevatedCardElevation(
                            defaultElevation = if (uiState.isPlaying) 20.dp else 8.dp
                        ),
                        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                        modifier = Modifier
                            .fillMaxSize()
                            .scale(albumScale)
                            .clickable { onAlbumArtClick() }
                            .testTag("album_art_card")
                    ) {
                        ArtworkImageOrFallback(
                            track = track,
                            shape = RoundedCornerShape(26.dp),
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            val musicFontFamily = remember(track) {
                getFontFamilyForMusic(track)
            }

            // Track Information & Actions Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 16.dp)
                ) {
                    Text(
                        text = track.title.ifBlank { "Local Track" },
                        fontFamily = musicFontFamily,
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = (-0.3).sp
                        ),
                        color = dynamicTextColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.testTag("track_title_text")
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = track.artist.ifBlank { "Unknown Artist" },
                        fontFamily = musicFontFamily,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Normal
                        ),
                        color = dynamicTextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.testTag("track_artist_text")
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    AnimatedFavoriteButton(
                        isFavorite = uiState.isFavorite,
                        activeColor = Color(0xFFFFD700),
                        inactiveColor = dynamicTextColor.copy(alpha = 0.7f),
                        onToggle = {
                            hapticController.performHaptic(HapticType.DOUBLE_CLICK)
                            onFavoriteToggle()
                        }
                    )

                    Box {
                        FilledTonalIconButton(
                            onClick = {
                                isMenuExpanded = !isMenuExpanded
                                onMenuClick()
                            },
                            colors = IconButtonDefaults.filledTonalIconButtonColors(
                                containerColor = dynamicPillColor,
                                contentColor = dynamicTextColor
                            ),
                            shape = CircleShape,
                            modifier = Modifier
                                .size(42.dp)
                                .springPressPhysics()
                                .testTag("overflow_menu_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.MoreHoriz,
                                contentDescription = "More Options",
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        DropdownMenu(
                            expanded = isMenuExpanded,
                            onDismissRequest = { isMenuExpanded = false },
                            modifier = Modifier.background(Color(0xFF1E2638))
                        ) {
                            DropdownMenuItem(
                                text = { Text("Settings", color = Color.White) },
                                leadingIcon = { Icon(Icons.Default.Settings, contentDescription = null, tint = Color.White) },
                                onClick = {
                                    isMenuExpanded = false
                                    onOpenSettings()
                                }
                            )
                            val speedLabel = if (kotlin.math.abs(uiState.playbackSpeed - 1.0f) < 0.02f) "Playback Speed (1.0x)" else "Playback Speed (${uiState.playbackSpeed}x)"
                            DropdownMenuItem(
                                text = { Text(speedLabel, color = Color.White) },
                                leadingIcon = { Icon(Icons.Default.Speed, contentDescription = null, tint = Color.White) },
                                onClick = {
                                    isMenuExpanded = false
                                    onPlaybackSpeedClick()
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Sleep Timer", color = Color.White) },
                                leadingIcon = { Icon(Icons.Default.Bedtime, contentDescription = null, tint = Color.White) },
                                onClick = {
                                    isMenuExpanded = false
                                    onSleepTimerClick()
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Equalizer & Sound FX", color = Color.White) },
                                leadingIcon = { Icon(Icons.Default.Equalizer, contentDescription = null, tint = Color.White) },
                                onClick = {
                                    isMenuExpanded = false
                                    onEqualizerClick()
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Synchronized Lyrics", color = Color.White) },
                                leadingIcon = { Icon(Icons.Default.Lyrics, contentDescription = null, tint = Color.White) },
                                onClick = {
                                    isMenuExpanded = false
                                    onLyricsToggle()
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Up Next Queue", color = Color.White) },
                                leadingIcon = { Icon(Icons.AutoMirrored.Filled.QueueMusic, contentDescription = null, tint = Color.White) },
                                onClick = {
                                    isMenuExpanded = false
                                    onQueueClick()
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // M3 Expressive Scrubber & Progress
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("progress_scrubber_section"),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                M3ExpressiveProgressBar(
                    currentPositionMs = uiState.currentPositionMs,
                    durationMs = track.durationMs,
                    onSeekTo = onSeekTo,
                    activeColor = track.primaryAmbientColor.takeIf { it != Color.Unspecified } ?: MaterialTheme.colorScheme.primary,
                    modifier = Modifier.fillMaxWidth()
                )

                // Extended Audio Quality Badge: Format, Bit Depth, Sample Rate, Bitrate
                if (showAudioDetails) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Surface(
                        color = Color.White.copy(alpha = 0.12f),
                        shape = RoundedCornerShape(100.dp),
                        modifier = Modifier.testTag("audio_quality_badge")
                    ) {
                        val sampleRateKhz = track.sampleRateHz / 1000.0
                        Text(
                            text = "𝄢  ${track.formatName} • ${track.bitDepth}-bit / ${String.format("%.1f", sampleRateKhz)}kHz • ${track.bitrateKbps} kbps",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.SemiBold,
                                letterSpacing = 0.3.sp
                            ),
                            color = Color.White.copy(alpha = 0.90f),
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Playback Controls Row with Spring Physics & Expanding Aura
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                AnimatedSkipButton(
                    isNext = false,
                    tint = dynamicTextColor,
                    size = 56.dp,
                    iconSize = 36.dp,
                    onClick = {
                        hapticController.performHaptic(HapticType.LIGHT_CLICK)
                        onPreviousClick()
                    },
                    testTag = "previous_track_button"
                )

                AnimatedPlayPauseHeroButton(
                    isPlaying = uiState.isPlaying,
                    primaryColor = dynamicPlayContainer,
                    onPrimaryColor = dynamicPlayContent,
                    size = 76.dp,
                    iconSize = 40.dp,
                    onClick = {
                        hapticController.performHaptic(HapticType.MEDIUM_CLICK)
                        onPlayPauseClick()
                    },
                    testTag = "play_pause_button"
                )

                AnimatedSkipButton(
                    isNext = true,
                    tint = dynamicTextColor,
                    size = 56.dp,
                    iconSize = 36.dp,
                    onClick = {
                        hapticController.performHaptic(HapticType.LIGHT_CLICK)
                        onNextClick()
                    },
                    testTag = "next_track_button"
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Bottom Utilities Bar: Volume Scrubber + Action Pills
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth(0.95f)
                        .padding(horizontal = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.VolumeMute,
                        contentDescription = "Volume Low",
                        tint = dynamicTextSecondary,
                        modifier = Modifier.size(18.dp)
                    )

                    Slider(
                        value = uiState.volume,
                        onValueChange = onVolumeChange,
                        valueRange = 0f..1f,
                        colors = SliderDefaults.colors(
                            thumbColor = dynamicTextColor,
                            activeTrackColor = dynamicTextColor.copy(alpha = 0.9f),
                            inactiveTrackColor = dynamicTextColor.copy(alpha = 0.22f)
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .height(20.dp)
                            .testTag("volume_slider")
                    )

                    Icon(
                        imageVector = Icons.Filled.VolumeUp,
                        contentDescription = "Volume High",
                        tint = dynamicTextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }

                if (showMusicVisualizer) {
                    Spacer(modifier = Modifier.height(4.dp))

                    val activeStyle = try {
                        com.example.ui.components.WaveformVisualizerStyle.valueOf(waveformVisualizerStyle)
                    } catch (_: Exception) {
                        com.example.ui.components.WaveformVisualizerStyle.NEON_BARS
                    }

                    // Animated Real-Time Audio Waveform Visualizer
                    AudioWaveformVisualizer(
                        isPlaying = uiState.isPlaying,
                        playbackSpeed = uiState.playbackSpeed,
                        volume = uiState.volume,
                        primaryColor = Color(0xFF7FA7F8),
                        secondaryColor = Color(0xFFA78BFA),
                        accentColor = Color(0xFF67E8F9),
                        height = 36.dp,
                        initialStyle = activeStyle,
                        onStyleChange = { onWaveformVisualizerStyleChange?.invoke(it.name) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 4.dp)
                    )

                    Spacer(modifier = Modifier.height(4.dp))
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FilledTonalIconButton(
                        onClick = {
                            hapticController.performHaptic(HapticType.LIGHT_CLICK)
                            onLyricsToggle()
                        },
                        shape = CircleShape,
                        colors = IconButtonDefaults.filledTonalIconButtonColors(
                            containerColor = if (uiState.isLyricsVisible) dynamicActivePill else dynamicPillColor,
                            contentColor = if (uiState.isLyricsVisible) dynamicActivePillContent else dynamicTextColor
                        ),
                        modifier = Modifier
                            .size(46.dp)
                            .springPressPhysics()
                            .testTag("lyrics_toggle_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lyrics,
                            contentDescription = "Synchronized Lyrics",
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Surface(
                        onClick = {
                            hapticController.performHaptic(HapticType.LIGHT_CLICK)
                            onDevicePickerClick()
                        },
                        color = dynamicPillColor,
                        border = dynamicBorder,
                        shape = RoundedCornerShape(100.dp),
                        modifier = Modifier
                            .springPressPhysics()
                            .testTag("device_picker_pill")
                            .minimumInteractiveComponentSize()
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp)
                        ) {
                            val deviceIcon = when (uiState.activeDevice.type) {
                                com.example.model.AudioDeviceType.BLUETOOTH -> Icons.Default.Bluetooth
                                com.example.model.AudioDeviceType.WIRED_HEADPHONES -> Icons.Default.Headphones
                                com.example.model.AudioDeviceType.USB_DAC -> Icons.Default.Headphones
                                else -> Icons.Default.Speaker
                            }
                            Icon(
                                imageVector = deviceIcon,
                                contentDescription = "Playback Device",
                                tint = dynamicTextSecondary,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(7.dp))
                            Text(
                                text = uiState.activeDevice.name,
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Medium
                                ),
                                color = dynamicTextColor,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    FilledTonalIconButton(
                        onClick = {
                            hapticController.performHaptic(HapticType.LIGHT_CLICK)
                            onPlaybackSpeedClick()
                        },
                        shape = CircleShape,
                        colors = IconButtonDefaults.filledTonalIconButtonColors(
                            containerColor = if (kotlin.math.abs(uiState.playbackSpeed - 1.0f) >= 0.05f) dynamicActivePill else dynamicPillColor,
                            contentColor = if (kotlin.math.abs(uiState.playbackSpeed - 1.0f) >= 0.05f) dynamicActivePillContent else dynamicTextColor
                        ),
                        modifier = Modifier
                            .size(46.dp)
                            .springPressPhysics()
                            .testTag("speed_toggle_button")
                    ) {
                        if (kotlin.math.abs(uiState.playbackSpeed - 1.0f) >= 0.05f) {
                            Text(
                                text = "${String.format(java.util.Locale.US, "%.1f", uiState.playbackSpeed)}x",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = Color.White
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Speed,
                                contentDescription = "Playback Speed",
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }

                    FilledTonalIconButton(
                        onClick = {
                            hapticController.performHaptic(HapticType.LIGHT_CLICK)
                            onEqualizerClick()
                        },
                        shape = CircleShape,
                        colors = IconButtonDefaults.filledTonalIconButtonColors(
                            containerColor = glassPalette.glassSurface,
                            contentColor = Color.White
                        ),
                        modifier = Modifier
                            .size(46.dp)
                            .springPressPhysics()
                            .testTag("equalizer_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Equalizer,
                            contentDescription = "Equalizer",
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    FilledTonalIconButton(
                        onClick = {
                            hapticController.performHaptic(HapticType.LIGHT_CLICK)
                            onQueueClick()
                        },
                        shape = CircleShape,
                        colors = IconButtonDefaults.filledTonalIconButtonColors(
                            containerColor = glassPalette.glassSurface,
                            contentColor = Color.White
                        ),
                        modifier = Modifier
                            .size(46.dp)
                            .springPressPhysics()
                            .testTag("queue_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.QueueMusic,
                            contentDescription = "Up Next Queue",
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Apple Music Style Synced Lyrics Display with translucent glassmorphism background.
 */
@Composable
private fun GlassmorphicLyricsCard(
    lyricsUiState: LyricsUiState,
    currentPositionMs: Long,
    onSeekTo: (Long) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(26.dp),
        color = Color(0x440A101C),
        border = BorderStroke(1.dp, Color(0x28FFFFFF)),
        modifier = modifier
            .clip(RoundedCornerShape(26.dp))
            .clickable { onDismiss() }
            .testTag("apple_music_lyrics_card")
    ) {
        when (lyricsUiState) {
            is LyricsUiState.Success -> {
                if (lyricsUiState.lines.isNotEmpty()) {
                    val listState = rememberLazyListState()
                    val coroutineScope = rememberCoroutineScope()
                    val activeIndex = lyricsUiState.lines.indexOfLast { it.timestampMs <= currentPositionMs }
                        .coerceAtLeast(0)

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
                            listState.animateScrollToItem(activeIndex, 0)
                        }
                    }

                    var entranceReady by remember(lyricsUiState.lines) { mutableStateOf(false) }
                    LaunchedEffect(lyricsUiState.lines) {
                        entranceReady = false
                        delay(30)
                        entranceReady = true
                    }

                    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                        val density = androidx.compose.ui.platform.LocalDensity.current
                        val halfHeightDp = with(density) { (maxHeight / 2f) }
                        val verticalPad = (halfHeightDp - 28.dp).coerceAtLeast(36.dp)

                        LazyColumn(
                            state = listState,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 18.dp),
                            contentPadding = PaddingValues(vertical = verticalPad),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            itemsIndexed(lyricsUiState.lines) { idx, line ->
                                val isActive = idx == activeIndex
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
                                    label = "glassLyricsEntrance"
                                )

                                val slideOffsetY = (1f - entranceProgress) * 28f

                                CompositionLocalProvider(
                                    LocalLayoutDirection provides if (isRtl) LayoutDirection.Rtl else LayoutDirection.Ltr
                                ) {
                                    Text(
                                        text = line.text,
                                        color = if (isActive) Color.White else Color.White.copy(alpha = 0.45f),
                                        fontSize = if (isActive) 17.sp else 16.sp,
                                        fontWeight = if (isActive) FontWeight.ExtraBold else FontWeight.Medium,
                                        textAlign = TextAlign.Start,
                                        style = MaterialTheme.typography.bodyLarge.copy(
                                            lineBreak = LineBreak.Paragraph
                                        ),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .graphicsLayer {
                                                alpha = (if (isActive) 1f else 0.45f) * entranceProgress
                                                translationY = slideOffsetY
                                            }
                                            .clickable {
                                                isUserScrolling = false
                                                onSeekTo(line.timestampMs)
                                            }
                                    )
                                }
                            }
                        }
                    }
                } else if (!lyricsUiState.plainLyrics.isNullOrBlank()) {
                    Text(
                        text = lyricsUiState.plainLyrics,
                        color = Color.White.copy(alpha = 0.85f),
                        fontSize = 15.sp,
                        textAlign = TextAlign.Start,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            lineBreak = LineBreak.Paragraph
                        ),
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(20.dp)
                    )
                } else {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            text = "No lyrics found for this track\n(Tap to return)",
                            color = Color.White.copy(alpha = 0.65f),
                            fontSize = 14.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
            is LyricsUiState.Loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        CircularProgressIndicator(
                            color = Color.White.copy(alpha = 0.85f),
                            modifier = Modifier.size(32.dp)
                        )
                        Text(
                            text = "Loading lyrics...",
                            color = Color.White.copy(alpha = 0.75f),
                            fontSize = 13.sp
                        )
                    }
                }
            }
            else -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = "No synchronized lyrics available\n(Tap to view album artwork)",
                        color = Color.White.copy(alpha = 0.65f),
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center
                    )
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

