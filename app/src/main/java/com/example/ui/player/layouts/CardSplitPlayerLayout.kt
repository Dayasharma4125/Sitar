package com.example.ui.player.layouts

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.NavigateBefore
import androidx.compose.material.icons.automirrored.filled.QueueMusic
import androidx.compose.material.icons.automirrored.filled.VolumeDown
import androidx.compose.material.icons.automirrored.filled.VolumeMute
import androidx.compose.material.icons.filled.Bedtime
import com.example.ui.components.AudioWaveformVisualizer
import com.example.ui.components.AnimatedFavoriteButton
import com.example.ui.theme.getFontFamilyForMusic
import androidx.compose.material.icons.filled.Cast
import androidx.compose.material.icons.filled.Equalizer
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.Lyrics
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.RepeatOne
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.Speaker
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AudioDeviceType
import com.example.model.LyricsUiState
import com.example.model.PlayerUiState
import com.example.ui.components.ArtworkImageOrFallback
import com.example.ui.components.GlassmorphicBackground
import com.example.ui.components.rememberAlbumArtGlassPalette
import com.example.ui.util.HapticType
import com.example.ui.util.rememberHapticController

/**
 * Material You Expressive Player Layout (as per user UI design):
 * - Top Bar: Back button, center active audio device capsule (e.g. "🎧 realme Buds T310"), Cast & Overflow menu
 * - Center Dynamic Zone: In-place smooth switch between Album Artwork squircle card and Synced Lyrics view
 * - Track Info: Prominent bold title, artist • format/album subtitle (respects showAudioDetails setting)
 * - Scrubber: Sleek progress bar with current & total duration timestamps
 * - Playback Cluster: 3-column expressive squircle buttons (Prev, Center Hero Play/Pause in accent container, Next)
 * - Volume & Secondary Row: Left capsule volume slider + Right capsule (Shuffle, Repeat, Favorite Heart)
 * - Bottom Action Row: Quick pills for Playing queue, In-place Lyrics (accent active state), Sleep timer
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CardSplitPlayerLayout(
    uiState: PlayerUiState,
    lyricsUiState: LyricsUiState,
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
    modifier: Modifier = Modifier
) {
    val track = uiState.currentTrack
    var isMenuExpanded by remember { mutableStateOf(false) }
    val hapticController = rememberHapticController()

    // Dynamic Glassmorphic Palette adapting to Album Artwork & AMOLED Support
    val isAmoled = isAmoledBlack
    val glassPalette = rememberAlbumArtGlassPalette(track = track, isAmoledBlack = isAmoled)
    val isGlassmorphic = isGlassmorphicEffectEnabled

    val textColor = if (isGlassmorphic || isAmoled) Color.White else MaterialTheme.colorScheme.onBackground
    val textSecondary = if (isGlassmorphic || isAmoled) Color.White.copy(alpha = 0.72f) else MaterialTheme.colorScheme.onSurfaceVariant
    val surfacePillColor = if (isGlassmorphic) glassPalette.glassSurface else MaterialTheme.colorScheme.surfaceContainerHighest
    val surfaceControlColor = if (isGlassmorphic) glassPalette.glassSurface else MaterialTheme.colorScheme.surfaceContainer
    val heroPlayContainerColor = if (isGlassmorphic) glassPalette.activePill else MaterialTheme.colorScheme.primary
    val heroPlayContentColor = if (isGlassmorphic) glassPalette.activePillContent else MaterialTheme.colorScheme.onPrimary
    val accentColor = if (isGlassmorphic) glassPalette.primary else MaterialTheme.colorScheme.primary
    val borderBrush = if (isGlassmorphic) {
        glassPalette.glassBorder
    } else {
        Brush.linearGradient(
            listOf(
                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f),
                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f)
            )
        )
    }

    var isSeeking by remember { mutableStateOf(false) }
    var seekProgress by remember { mutableFloatStateOf(0f) }

    val currentPosition = if (isSeeking) {
        (seekProgress * uiState.durationMs).toLong()
    } else {
        uiState.currentPositionMs
    }

    val sliderProgress = if (uiState.durationMs > 0) {
        (currentPosition.toFloat() / uiState.durationMs.toFloat()).coerceIn(0f, 1f)
    } else 0f

    // Device icon
    val deviceIcon = when (uiState.activeDevice.type) {
        AudioDeviceType.BLUETOOTH -> Icons.Default.Headphones
        AudioDeviceType.WIRED_HEADPHONES -> Icons.Default.Headphones
        AudioDeviceType.USB_DAC -> Icons.Default.Equalizer
        AudioDeviceType.SPEAKER -> Icons.Default.Speaker
        else -> Icons.Default.Speaker
    }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .testTag("card_split_player_layout")
    ) {
        val screenHeight = maxHeight
        val screenWidth = maxWidth
        val scrollState = rememberScrollState()

        val horizontalPadding = if (screenWidth < 360.dp) 16.dp else 20.dp
        val interSectionSpacing = when {
            screenHeight < 720.dp -> 8.dp
            screenHeight < 820.dp -> 12.dp
            else -> 16.dp
        }
        val contentMaxWidth = 560.dp
        val effectiveWidth = minOf(screenWidth, contentMaxWidth)
        val maxAvailableArtWidth = (effectiveWidth - (horizontalPadding * 2)).coerceAtLeast(220.dp)
        val availableArtHeight = (screenHeight - 440.dp).coerceAtLeast(240.dp)
        val centerArtSize = when {
            screenHeight < 680.dp -> 240.dp
            screenHeight < 740.dp -> 280.dp
            screenHeight < 820.dp -> minOf(maxAvailableArtWidth, 340.dp)
            else -> minOf(maxAvailableArtWidth, availableArtHeight, 375.dp)
        }

        if (isGlassmorphicEffectEnabled) {
            // Glassmorphic background blur adapting to album art color palette
            GlassmorphicBackground(
                track = track,
                blurIntensity = blurIntensity,
                isAmoledBlack = isAmoled,
                isPlaying = uiState.isPlaying
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(if (isAmoled) Color.Black else MaterialTheme.colorScheme.background)
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .verticalScroll(scrollState)
                .widthIn(max = contentMaxWidth)
                .padding(horizontal = horizontalPadding, vertical = if (screenHeight < 720.dp) 6.dp else 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // ================= 1. TOP APP BAR =================
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Back / Collapse Button
                IconButton(
                    onClick = onCollapseClick,
                    modifier = Modifier
                        .size(42.dp)
                        .testTag("player_collapse_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.NavigateBefore,
                        contentDescription = "Back",
                        tint = textColor,
                        modifier = Modifier.size(30.dp)
                    )
                }

                // Center Output Device Capsule (e.g. "🎧 realme Buds T310")
                Surface(
                    onClick = onDevicePickerClick,
                    shape = RoundedCornerShape(50.dp),
                    color = surfacePillColor,
                    border = BorderStroke(1.dp, borderBrush),
                    modifier = Modifier
                        .height(38.dp)
                        .testTag("active_device_pill")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = deviceIcon,
                            contentDescription = "Active Device",
                            tint = accentColor,
                            modifier = Modifier.size(17.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = uiState.activeDevice.name.ifBlank { "Device Speaker" },
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.sp
                            ),
                            color = textColor,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // Right Actions: Cast & Overflow Menu
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onDevicePickerClick,
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Cast,
                            contentDescription = "Cast & Audio Routing",
                            tint = textColor,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Box {
                        IconButton(
                            onClick = { isMenuExpanded = true },
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "Options Menu",
                                tint = textColor,
                                modifier = Modifier.size(22.dp)
                            )
                        }

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
                            val speedLabel = if (kotlin.math.abs(uiState.playbackSpeed - 1.0f) < 0.02f) "Playback Speed (1.0x)" else "Playback Speed (${uiState.playbackSpeed}x)"
                            DropdownMenuItem(
                                text = { Text(speedLabel) },
                                leadingIcon = { Icon(Icons.Default.Speed, null) },
                                onClick = {
                                    isMenuExpanded = false
                                    onPlaybackSpeedClick()
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
                                text = { Text("Sleep Timer") },
                                leadingIcon = { Icon(Icons.Default.Bedtime, null) },
                                onClick = {
                                    isMenuExpanded = false
                                    onSleepTimerClick()
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(interSectionSpacing))

            // ================= 2. DYNAMIC CENTER: IN-PLACE ARTWORK OR LYRICS =================
            Box(
                modifier = Modifier
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                AnimatedContent(
                    targetState = uiState.isLyricsVisible,
                    transitionSpec = {
                        fadeIn(animationSpec = tween(260)) togetherWith fadeOut(animationSpec = tween(260))
                    },
                    label = "InPlaceLyricsAnimation",
                    modifier = Modifier.fillMaxWidth()
                ) { isLyrics ->
                    if (isLyrics) {
                        InPlaceLyricsView(
                            lyricsUiState = lyricsUiState,
                            currentPositionMs = currentPosition,
                            textColor = textColor,
                            textSecondary = textSecondary,
                            onSeekTo = onSeekTo,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(centerArtSize)
                        )
                    } else {
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            Surface(
                                shape = RoundedCornerShape(32.dp),
                                color = surfacePillColor.copy(alpha = 0.35f),
                                border = BorderStroke(1.2.dp, borderBrush),
                                shadowElevation = if (isAmoled) 0.dp else 8.dp,
                                modifier = Modifier
                                    .size(centerArtSize)
                                    .clip(RoundedCornerShape(32.dp))
                                    .clickable { onAlbumArtClick() }
                                    .testTag("player_album_art")
                            ) {
                                ArtworkImageOrFallback(
                                    track = track,
                                    modifier = Modifier.fillMaxSize(),
                                    shape = RoundedCornerShape(32.dp)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(interSectionSpacing + 2.dp))

            val musicFontFamily = remember(track) {
                getFontFamilyForMusic(track)
            }

            // ================= 3. TRACK TITLE & ARTIST =================
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = track.title,
                    fontFamily = musicFontFamily,
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp
                    ),
                    color = textColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = track.artist.ifBlank { "Unknown Artist" },
                        fontFamily = musicFontFamily,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp
                        ),
                        color = textSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = " • ",
                        color = textSecondary.copy(alpha = 0.5f),
                        fontWeight = FontWeight.Bold
                    )
                    val albumLabel = track.album.ifBlank { "TELEGRAM" }.uppercase()
                    Text(
                        text = albumLabel,
                        fontFamily = musicFontFamily,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            letterSpacing = 0.8.sp
                        ),
                        color = textSecondary.copy(alpha = 0.75f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    if (showAudioDetails && track.audioQuality.isNotBlank()) {
                        Text(
                            text = " • ",
                            color = textSecondary.copy(alpha = 0.5f),
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = track.audioQuality,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                color = accentColor
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(interSectionSpacing))

            // ================= 4. SCRUBBER & TIMESTAMPS =================
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                Slider(
                    value = if (isSeeking) seekProgress else sliderProgress,
                    onValueChange = {
                        isSeeking = true
                        seekProgress = it
                    },
                    onValueChangeFinished = {
                        val seekTarget = (seekProgress * uiState.durationMs).toLong()
                        onSeekTo(seekTarget)
                        isSeeking = false
                    },
                    colors = SliderDefaults.colors(
                        thumbColor = textColor,
                        activeTrackColor = textColor,
                        inactiveTrackColor = textColor.copy(alpha = 0.22f)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(26.dp)
                        .testTag("player_progress_slider")
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = formatTime(currentPosition),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Medium,
                            fontSize = 12.sp
                        ),
                        color = textSecondary
                    )
                    Text(
                        text = formatTime(uiState.durationMs),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Medium,
                            fontSize = 12.sp
                        ),
                        color = textSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(interSectionSpacing))

            // ================= 5. EXPRESSIVE PLAYBACK CONTROLS =================
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Previous Track Squircle
                Surface(
                    onClick = {
                        hapticController.performHaptic(HapticType.LIGHT_CLICK)
                        onPreviousClick()
                    },
                    shape = RoundedCornerShape(26.dp),
                    color = surfaceControlColor,
                    border = BorderStroke(1.dp, borderBrush),
                    modifier = Modifier
                        .weight(1f)
                        .height(72.dp)
                        .testTag("player_previous_button")
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.SkipPrevious,
                            contentDescription = "Previous",
                            tint = textColor,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }

                // Center Hero Play/Pause Squircle in M3 Accent Container
                Surface(
                    onClick = {
                        hapticController.performHaptic(HapticType.MEDIUM_CLICK)
                        onPlayPauseClick()
                    },
                    shape = RoundedCornerShape(30.dp),
                    color = heroPlayContainerColor,
                    border = BorderStroke(1.5.dp, androidx.compose.ui.graphics.Brush.linearGradient(listOf(Color.White.copy(alpha = 0.55f), accentColor.copy(alpha = 0.35f)))),
                    modifier = Modifier
                        .weight(1.35f)
                        .height(78.dp)
                        .testTag("player_play_pause_button")
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = if (uiState.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (uiState.isPlaying) "Pause" else "Play",
                            tint = heroPlayContentColor,
                            modifier = Modifier.size(38.dp)
                        )
                    }
                }

                // Next Track Squircle
                Surface(
                    onClick = {
                        hapticController.performHaptic(HapticType.LIGHT_CLICK)
                        onNextClick()
                    },
                    shape = RoundedCornerShape(26.dp),
                    color = surfaceControlColor,
                    border = BorderStroke(1.dp, borderBrush),
                    modifier = Modifier
                        .weight(1f)
                        .height(72.dp)
                        .testTag("player_next_button")
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.SkipNext,
                            contentDescription = "Next",
                            tint = textColor,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(interSectionSpacing))

            // ================= 6. VOLUME & SECONDARY UTILITY ROW =================
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Volume Slider Capsule (Left)
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = surfacePillColor,
                    border = BorderStroke(1.dp, borderBrush),
                    modifier = Modifier
                        .weight(1.15f)
                        .height(48.dp)
                        .testTag("volume_control_capsule")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Slider(
                            value = uiState.volume,
                            onValueChange = onVolumeChange,
                            colors = SliderDefaults.colors(
                                thumbColor = textColor,
                                activeTrackColor = textColor,
                                inactiveTrackColor = textColor.copy(alpha = 0.2f)
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .height(24.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            imageVector = if (uiState.volume <= 0.01f) Icons.AutoMirrored.Filled.VolumeMute else Icons.AutoMirrored.Filled.VolumeDown,
                            contentDescription = "Volume",
                            tint = textSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                // Utility Capsule (Right: Shuffle, Repeat, Favorite)
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = surfacePillColor,
                    border = BorderStroke(1.dp, borderBrush),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Shuffle
                        IconButton(
                            onClick = {
                                hapticController.performHaptic(HapticType.LIGHT_CLICK)
                                onShuffleToggle()
                            },
                            modifier = Modifier
                                .size(36.dp)
                                .testTag("player_shuffle_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Shuffle,
                                contentDescription = "Shuffle",
                                tint = if (uiState.isShuffle) accentColor else textSecondary,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        // Repeat
                        IconButton(
                            onClick = {
                                hapticController.performHaptic(HapticType.LIGHT_CLICK)
                                onRepeatToggle()
                            },
                            modifier = Modifier
                                .size(36.dp)
                                .testTag("player_repeat_button")
                        ) {
                            Icon(
                                imageVector = if (uiState.isRepeat) Icons.Default.RepeatOne else Icons.Default.Repeat,
                                contentDescription = "Repeat",
                                tint = if (uiState.isRepeat) accentColor else textSecondary,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        // Favorite Heart with Spring Explosion
                        AnimatedFavoriteButton(
                            isFavorite = uiState.isFavorite,
                            activeColor = Color(0xFFFF4B4B),
                            inactiveColor = textSecondary,
                            size = 36.dp,
                            iconSize = 20.dp,
                            onToggle = {
                                hapticController.performHaptic(HapticType.DOUBLE_CLICK)
                                onFavoriteToggle()
                            },
                            testTag = "player_favorite_button"
                        )
                    }
                }
            }

            if (showMusicVisualizer) {
                Spacer(modifier = Modifier.height((interSectionSpacing - 4.dp).coerceAtLeast(6.dp)))

                val activeStyle = try {
                    com.example.ui.components.WaveformVisualizerStyle.valueOf(waveformVisualizerStyle)
                } catch (_: Exception) {
                    com.example.ui.components.WaveformVisualizerStyle.NEON_BARS
                }

                // ================= 6.5 ANIMATED REAL-TIME AUDIO WAVEFORM VISUALIZER =================
                AudioWaveformVisualizer(
                    isPlaying = uiState.isPlaying,
                    playbackSpeed = uiState.playbackSpeed,
                    volume = uiState.volume,
                    primaryColor = accentColor,
                    secondaryColor = MaterialTheme.colorScheme.tertiary,
                    accentColor = MaterialTheme.colorScheme.secondary,
                    height = 36.dp,
                    initialStyle = activeStyle,
                    onStyleChange = { onWaveformVisualizerStyleChange?.invoke(it.name) },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(interSectionSpacing))
            } else {
                Spacer(modifier = Modifier.height(interSectionSpacing))
            }

            // ================= 7. BOTTOM ACTION PILLS (Scrollable) =================
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Queue Pill
                Surface(
                    onClick = {
                        hapticController.performHaptic(HapticType.LIGHT_CLICK)
                        onQueueClick()
                    },
                    shape = RoundedCornerShape(50.dp),
                    color = surfacePillColor,
                    border = BorderStroke(1.dp, borderBrush),
                    modifier = Modifier
                        .height(42.dp)
                        .testTag("action_pill_queue")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.QueueMusic,
                            contentDescription = null,
                            tint = textColor,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Playing queue",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 11.5.sp
                            ),
                            color = textColor,
                            maxLines = 1
                        )
                    }
                }

                // Lyrics Pill (Highlighted active state in-place!)
                Surface(
                    onClick = {
                        hapticController.performHaptic(HapticType.LIGHT_CLICK)
                        onLyricsToggle()
                    },
                    shape = RoundedCornerShape(50.dp),
                    color = if (uiState.isLyricsVisible) heroPlayContainerColor else surfacePillColor,
                    border = BorderStroke(1.dp, borderBrush),
                    modifier = Modifier
                        .height(42.dp)
                        .testTag("action_pill_lyrics")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lyrics,
                            contentDescription = null,
                            tint = if (uiState.isLyricsVisible) heroPlayContentColor else textColor,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Lyrics",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 11.5.sp
                            ),
                            color = if (uiState.isLyricsVisible) heroPlayContentColor else textColor,
                            maxLines = 1
                        )
                    }
                }

                // Sleep Timer Pill
                Surface(
                    onClick = {
                        hapticController.performHaptic(HapticType.LIGHT_CLICK)
                        onSleepTimerClick()
                    },
                    shape = RoundedCornerShape(50.dp),
                    color = surfacePillColor,
                    border = BorderStroke(1.dp, borderBrush),
                    modifier = Modifier
                        .height(42.dp)
                        .testTag("action_pill_sleeptimer")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Bedtime,
                            contentDescription = null,
                            tint = textColor,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Sleep timer",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 11.5.sp
                            ),
                            color = textColor,
                            maxLines = 1
                        )
                    }
                }

                // Speed Pill
                Surface(
                    onClick = {
                        hapticController.performHaptic(HapticType.LIGHT_CLICK)
                        onPlaybackSpeedClick()
                    },
                    shape = RoundedCornerShape(50.dp),
                    color = if (kotlin.math.abs(uiState.playbackSpeed - 1.0f) >= 0.05f) heroPlayContainerColor else surfacePillColor,
                    border = BorderStroke(1.dp, borderBrush),
                    modifier = Modifier
                        .height(42.dp)
                        .testTag("action_pill_speed")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Speed,
                            contentDescription = null,
                            tint = if (kotlin.math.abs(uiState.playbackSpeed - 1.0f) >= 0.05f) heroPlayContentColor else textColor,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${String.format(java.util.Locale.US, "%.2f", uiState.playbackSpeed).trimEnd('0').trimEnd('.')}x",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 11.5.sp
                            ),
                            color = if (kotlin.math.abs(uiState.playbackSpeed - 1.0f) >= 0.05f) heroPlayContentColor else textColor,
                            maxLines = 1
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun InPlaceLyricsView(
    lyricsUiState: LyricsUiState,
    currentPositionMs: Long,
    textColor: Color,
    textSecondary: Color,
    onSeekTo: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    when (lyricsUiState) {
        is LyricsUiState.Success -> {
            val lines = lyricsUiState.lines
            if (lines.isNotEmpty()) {
                val activeIndex = remember(lines, currentPositionMs) {
                    val idx = lines.indexOfLast { it.timestampMs <= currentPositionMs }
                    if (idx >= 0) idx else 0
                }
                val listState = rememberLazyListState()

                LaunchedEffect(activeIndex) {
                    if (activeIndex >= 0 && activeIndex < lines.size) {
                        val target = (activeIndex - 1).coerceAtLeast(0)
                        listState.animateScrollToItem(index = target, scrollOffset = -24)
                    }
                }

                LazyColumn(
                    state = listState,
                    contentPadding = PaddingValues(vertical = 24.dp, horizontal = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = modifier
                        .fillMaxSize()
                        .testTag("inplace_lyrics_list")
                ) {
                    itemsIndexed(lines, key = { index, line -> "${line.timestampMs}_$index" }) { index, line ->
                        val isActive = index == activeIndex
                        val distance = kotlin.math.abs(index - activeIndex)

                        val alpha by animateFloatAsState(
                            targetValue = if (isActive) 1.0f else if (distance == 1) 0.65f else 0.35f,
                            animationSpec = tween(durationMillis = 350, easing = FastOutSlowInEasing),
                            label = "inplaceCardAlpha"
                        )
                        val scale by animateFloatAsState(
                            targetValue = if (isActive) 1.05f else 1.0f,
                            animationSpec = spring(stiffness = Spring.StiffnessLow),
                            label = "inplaceCardScale"
                        )

                        Text(
                            text = line.text,
                            style = if (isActive) {
                                MaterialTheme.typography.headlineSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 24.sp,
                                    lineHeight = 32.sp
                                )
                            } else {
                                MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 19.sp,
                                    lineHeight = 28.sp
                                )
                            },
                            color = if (isActive) textColor else textSecondary,
                            modifier = Modifier
                                .fillMaxWidth()
                                .graphicsLayer {
                                    scaleX = scale
                                    scaleY = scale
                                    this.alpha = alpha
                                }
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { onSeekTo(line.timestampMs) }
                                .padding(vertical = 4.dp, horizontal = 4.dp)
                        )
                    }
                }
            } else if (!lyricsUiState.plainLyrics.isNullOrBlank()) {
                LazyColumn(
                    contentPadding = PaddingValues(vertical = 24.dp, horizontal = 12.dp),
                    modifier = modifier.fillMaxSize()
                ) {
                    item {
                        Text(
                            text = lyricsUiState.plainLyrics,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Medium,
                                fontSize = 20.sp,
                                lineHeight = 30.sp
                            ),
                            color = textColor.copy(alpha = 0.85f),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            } else {
                LyricsEmptyState(textSecondary = textSecondary, modifier = modifier)
            }
        }
        is LyricsUiState.Loading -> {
            Box(modifier = modifier, contentAlignment = Alignment.Center) {
                CircularProgressIndicator(
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(36.dp)
                )
            }
        }
        is LyricsUiState.NotFound -> {
            LyricsEmptyState(textSecondary = textSecondary, modifier = modifier)
        }
    }
}

@Composable
private fun LyricsEmptyState(
    textSecondary: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                modifier = Modifier.size(64.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.MusicNote,
                        contentDescription = null,
                        tint = textSecondary,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(14.dp))
            Text(
                text = "No Lyrics Found",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = textSecondary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Add an .lrc file in the track's folder to enable synced lyrics",
                style = MaterialTheme.typography.bodySmall,
                color = textSecondary.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )
        }
    }
}

private fun formatTime(millis: Long): String {
    val totalSeconds = (millis / 1000).coerceAtLeast(0)
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format("%02d:%02d", minutes, seconds)
}
