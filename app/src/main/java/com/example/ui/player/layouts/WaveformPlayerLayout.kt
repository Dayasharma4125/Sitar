package com.example.ui.player.layouts

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.QueueMusic
import androidx.compose.material.icons.automirrored.filled.VolumeMute
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Equalizer
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.FastRewind
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Lyrics
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.outlined.StarOutline
import com.example.ui.components.AnimatedPlayPauseHeroButton
import com.example.ui.components.AnimatedSkipButton
import com.example.ui.components.AnimatedFavoriteButton
import com.example.ui.theme.getFontFamilyForMusic
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.style.LineBreak
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import com.example.model.LyricsUiState
import com.example.model.PlayerUiState
import com.example.ui.components.ArtworkImageOrFallback
import com.example.ui.lyrics.isRtlString
import kotlin.math.sin

/**
 * Layout 3: Waveform Studio Player
 * Features live audio frequency waveform visualization integrated directly into the progress scrubber,
 * neon electric accents, digital telemetry badges, and futuristic transport controls.
 */
@Composable
fun WaveformPlayerLayout(
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
    onShuffleToggle: () -> Unit = {},
    onRepeatToggle: () -> Unit = {},
    onSleepTimerClick: () -> Unit = {},
    onPlaybackSpeedClick: () -> Unit = {},
    onOpenSettings: () -> Unit = {},
    onMenuClick: () -> Unit = onEqualizerClick,
    onEditLyricsClick: () -> Unit = {},
    isAmoledBlack: Boolean = false,
    showAudioDetails: Boolean = false,
    modifier: Modifier = Modifier
) {
    val track = uiState.currentTrack
    var isMenuExpanded by remember { mutableStateOf(false) }

    val baseBgColor = if (isAmoledBlack) Color.Black else Color(0xFF070B12)

    // Dynamic music-driven cyber color palette
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

    val accentNeon by animateColorAsState(
        targetValue = rawPrimary,
        animationSpec = tween(durationMillis = 650, easing = FastOutSlowInEasing),
        label = "cyberWavePrimary"
    )
    val secondaryNeon by animateColorAsState(
        targetValue = rawSecondary,
        animationSpec = tween(durationMillis = 650, easing = FastOutSlowInEasing),
        label = "cyberWaveSecondary"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(baseBgColor)
            .testTag("waveform_player_layout")
    ) {
        // Dynamic cyber glow backdrop
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            accentNeon.copy(alpha = if (isAmoledBlack) 0.18f else 0.32f),
                            secondaryNeon.copy(alpha = if (isAmoledBlack) 0.08f else 0.16f),
                            Color.Transparent
                        ),
                        radius = 1200f
                    )
                )
        )

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
                    .background(accentNeon.copy(alpha = 0.5f))
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
                        containerColor = accentNeon.copy(alpha = 0.16f),
                        contentColor = accentNeon
                    ),
                    modifier = Modifier.size(38.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = "Collapse",
                        modifier = Modifier.size(24.dp)
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.GraphicEq,
                        contentDescription = null,
                        tint = accentNeon,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "WAVEFORM FREQUENCY CONSOLE",
                        style = MaterialTheme.typography.labelSmall.copy(
                            letterSpacing = 1.4.sp,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp
                        ),
                        color = accentNeon
                    )
                }

                FilledTonalIconButton(
                    onClick = onQueueClick,
                    shape = CircleShape,
                    colors = IconButtonDefaults.filledTonalIconButtonColors(
                        containerColor = accentNeon.copy(alpha = 0.16f),
                        contentColor = accentNeon
                    ),
                    modifier = Modifier.size(38.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.QueueMusic,
                        contentDescription = "Queue",
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Artwork Centerpiece with Neon Border & Shadow or Cyber Matrix Lyrics Console
            AnimatedContent(
                targetState = uiState.isLyricsVisible,
                transitionSpec = {
                    fadeIn(animationSpec = spring(stiffness = Spring.StiffnessMedium)) togetherWith
                        fadeOut(animationSpec = spring(stiffness = Spring.StiffnessMedium))
                },
                label = "cyberMatrixLyricsToggle",
                modifier = Modifier
                    .fillMaxWidth(0.88f)
                    .aspectRatio(1f)
            ) { showLyrics ->
                if (showLyrics) {
                    CyberMatrixLyricsCard(
                        lyricsUiState = lyricsUiState,
                        currentPositionMs = uiState.currentPositionMs,
                        accentNeon = accentNeon,
                        onLyricsToggle = onLyricsToggle,
                        onSeekTo = onSeekTo,
                        onEditLyrics = onEditLyricsClick
                    )
                } else {
                    Card(
                        shape = RoundedCornerShape(topStart = 28.dp, bottomEnd = 28.dp, topEnd = 16.dp, bottomStart = 16.dp),
                        border = androidx.compose.foundation.BorderStroke(1.5.dp, accentNeon.copy(alpha = 0.45f)),
                        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.65f)),
                        modifier = Modifier
                            .fillMaxSize()
                            .clickable { onAlbumArtClick() }
                            .testTag("album_art_card")
                    ) {
                        ArtworkImageOrFallback(
                            track = track,
                            shape = RoundedCornerShape(topStart = 28.dp, bottomEnd = 28.dp, topEnd = 16.dp, bottomStart = 16.dp),
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Track Meta & Actions Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = track.title,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Black,
                            letterSpacing = (-0.2).sp
                        ),
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = track.artist,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Medium
                        ),
                        color = accentNeon.copy(alpha = 0.85f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    FilledTonalIconButton(
                        onClick = onFavoriteToggle,
                        colors = IconButtonDefaults.filledTonalIconButtonColors(
                            containerColor = if (uiState.isFavorite) Color(0x33FFD700) else Color(0x14FFFFFF),
                            contentColor = if (uiState.isFavorite) Color(0xFFFFD700) else Color.White
                        ),
                        shape = CircleShape,
                        modifier = Modifier.size(38.dp)
                    ) {
                        Icon(
                            imageVector = if (uiState.isFavorite) Icons.Filled.Star else Icons.Outlined.StarOutline,
                            contentDescription = "Favorite",
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Box {
                        FilledTonalIconButton(
                            onClick = {
                                isMenuExpanded = !isMenuExpanded
                                onMenuClick()
                            },
                            colors = IconButtonDefaults.filledTonalIconButtonColors(
                                containerColor = Color(0x14FFFFFF),
                                contentColor = Color.White
                            ),
                            shape = CircleShape,
                            modifier = Modifier.size(38.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.MoreHoriz,
                                contentDescription = "More",
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        DropdownMenu(
                            expanded = isMenuExpanded,
                            onDismissRequest = { isMenuExpanded = false },
                            modifier = Modifier.background(Color(0xFF10192A))
                        ) {
                            DropdownMenuItem(
                                text = { Text("Edit Lyrics", color = Color.White) },
                                leadingIcon = {
                                    Icon(Icons.Default.Edit, contentDescription = null, tint = accentNeon, modifier = Modifier.size(18.dp))
                                },
                                onClick = {
                                    isMenuExpanded = false
                                    onEditLyricsClick()
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("View Album", color = Color.White) },
                                onClick = { isMenuExpanded = false }
                            )
                            DropdownMenuItem(
                                text = { Text("View Artist", color = Color.White) },
                                onClick = { isMenuExpanded = false }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // LIVE AUDIO WAVEFORM PROGRESS SCRUBBER
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("waveform_scrubber_section"),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                InteractiveWaveformScrubber(
                    currentPositionMs = uiState.currentPositionMs,
                    durationMs = track.durationMs,
                    isPlaying = uiState.isPlaying,
                    onSeekTo = onSeekTo,
                    accentColor = accentNeon,
                    secondaryColor = secondaryNeon,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                )

                // Studio Telemetry Badge: Format, Bit Depth, Sample Rate, Bitrate
                if (showAudioDetails) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Surface(
                        color = accentNeon.copy(alpha = 0.16f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, accentNeon.copy(alpha = 0.35f)),
                        shape = RoundedCornerShape(100.dp)
                    ) {
                        val sampleRateKhz = track.sampleRateHz / 1000.0
                        Text(
                            text = "STUDIO MASTER • ${track.formatName} • ${track.bitDepth}BIT / ${String.format("%.1f", sampleRateKhz)}KHZ • ${track.bitrateKbps} KBPS",
                            style = MaterialTheme.typography.labelSmall.copy(
                                letterSpacing = 1.1.sp,
                                fontWeight = FontWeight.Bold,
                                fontSize = 9.sp
                            ),
                            color = accentNeon,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Cyber Transport Controls (Material 3 Expressive Squircle Hero Button)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onPreviousClick,
                    modifier = Modifier.size(54.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.FastRewind,
                        contentDescription = "Previous",
                        tint = Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                }

                // Primary Neon Glow Play/Pause Button with M3 Expressive Squircle Form
                FilledIconButton(
                    onClick = onPlayPauseClick,
                    shape = RoundedCornerShape(26.dp),
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = accentNeon,
                        contentColor = if (accentNeon.luminance() > 0.5f) Color(0xFF04101A) else Color.White
                    ),
                    modifier = Modifier
                        .size(76.dp)
                        .shadow(elevation = 16.dp, shape = RoundedCornerShape(26.dp), spotColor = accentNeon)
                        .testTag("play_pause_button")
                ) {
                    AnimatedContent(
                        targetState = uiState.isPlaying,
                        transitionSpec = {
                            fadeIn(animationSpec = spring(stiffness = Spring.StiffnessMedium)) togetherWith
                                fadeOut(animationSpec = spring(stiffness = Spring.StiffnessMedium))
                        },
                        label = "waveformPlayPause"
                    ) { isPlaying ->
                        Icon(
                            imageVector = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                            contentDescription = if (isPlaying) "Pause" else "Play",
                            modifier = Modifier.size(40.dp)
                        )
                    }
                }

                IconButton(
                    onClick = onNextClick,
                    modifier = Modifier.size(54.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.FastForward,
                        contentDescription = "Next",
                        tint = Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                }
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
                        contentDescription = null,
                        tint = accentNeon.copy(alpha = 0.7f),
                        modifier = Modifier.size(18.dp)
                    )

                    Slider(
                        value = uiState.volume,
                        onValueChange = onVolumeChange,
                        valueRange = 0f..1f,
                        colors = SliderDefaults.colors(
                            thumbColor = accentNeon,
                            activeTrackColor = accentNeon,
                            inactiveTrackColor = Color(0x22FFFFFF)
                        ),
                        modifier = Modifier.weight(1f)
                    )

                    Icon(
                        imageVector = Icons.Filled.VolumeUp,
                        contentDescription = null,
                        tint = accentNeon.copy(alpha = 0.7f),
                        modifier = Modifier.size(18.dp)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FilledTonalIconButton(
                        onClick = onLyricsToggle,
                        shape = CircleShape,
                        colors = IconButtonDefaults.filledTonalIconButtonColors(
                            containerColor = if (uiState.isLyricsVisible) accentNeon else accentNeon.copy(alpha = 0.16f),
                            contentColor = if (uiState.isLyricsVisible) Color.Black else accentNeon
                        ),
                        modifier = Modifier.size(44.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lyrics,
                            contentDescription = "Cyber Lyrics",
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    FilledTonalIconButton(
                        onClick = onEditLyricsClick,
                        shape = CircleShape,
                        colors = IconButtonDefaults.filledTonalIconButtonColors(
                            containerColor = accentNeon.copy(alpha = 0.16f),
                            contentColor = accentNeon
                        ),
                        modifier = Modifier.size(44.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit Lyrics",
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Surface(
                        onClick = onDevicePickerClick,
                        color = accentNeon.copy(alpha = 0.16f),
                        shape = RoundedCornerShape(100.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, accentNeon.copy(alpha = 0.3f))
                    ) {
                        Text(
                            text = "⚡  ${uiState.activeDevice.name}",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = accentNeon,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                        )
                    }

                    FilledTonalIconButton(
                        onClick = onEqualizerClick,
                        shape = CircleShape,
                        colors = IconButtonDefaults.filledTonalIconButtonColors(
                            containerColor = accentNeon.copy(alpha = 0.16f),
                            contentColor = accentNeon
                        ),
                        modifier = Modifier.size(44.dp)
                    ) {
                        Icon(Icons.Default.Equalizer, "Equalizer", modifier = Modifier.size(20.dp))
                    }

                    FilledTonalIconButton(
                        onClick = onQueueClick,
                        shape = CircleShape,
                        colors = IconButtonDefaults.filledTonalIconButtonColors(
                            containerColor = accentNeon.copy(alpha = 0.16f),
                            contentColor = accentNeon
                        ),
                        modifier = Modifier.size(44.dp)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.QueueMusic, "Queue", modifier = Modifier.size(20.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun CyberMatrixLyricsCard(
    lyricsUiState: LyricsUiState,
    currentPositionMs: Long,
    accentNeon: Color,
    onLyricsToggle: () -> Unit,
    onSeekTo: (Long) -> Unit,
    onEditLyrics: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()
    val lines = (lyricsUiState as? LyricsUiState.Success)?.lines ?: emptyList()
    val isLoading = lyricsUiState is LyricsUiState.Loading
    val activeIndex = if (lines.isNotEmpty()) {
        lines.indexOfLast { it.timestampMs <= currentPositionMs }.coerceAtLeast(0)
    } else -1

    LaunchedEffect(activeIndex) {
        if (activeIndex >= 0) {
            listState.animateScrollToItem((activeIndex - 1).coerceAtLeast(0))
        }
    }

    Card(
        shape = RoundedCornerShape(22.dp),
        border = androidx.compose.foundation.BorderStroke(1.5.dp, accentNeon.copy(alpha = 0.5f)),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 14.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0C1322)),
        modifier = modifier
            .fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { onLyricsToggle() }
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(accentNeon)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "MATRIX TELEMETRY • VOCAL STREAM",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 9.sp,
                            letterSpacing = 1.2.sp
                        ),
                        color = accentNeon
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = accentNeon.copy(alpha = 0.15f),
                        border = BorderStroke(1.dp, accentNeon.copy(alpha = 0.4f)),
                        modifier = Modifier.clickable { onEditLyrics() }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Edit",
                                tint = accentNeon,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "EDIT",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = accentNeon
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    Text(
                        text = "RETURN",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontFamily = FontFamily.Monospace,
                            fontSize = 8.sp
                        ),
                        color = Color(0xFF64748B),
                        modifier = Modifier.clickable { onLyricsToggle() }
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = accentNeon, modifier = Modifier.size(28.dp))
                }
            } else if (lines.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = "[ NO SYNCHRONIZED STREAM DETECTED ]",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Medium
                        ),
                        color = Color(0xFF64748B)
                    )
                }
            } else {
                LazyColumn(
                    state = listState,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    itemsIndexed(lines) { index, line ->
                        val isCurrent = index == activeIndex
                        val isPast = index < activeIndex
                        val isRtl = isRtlString(line.text)

                        CompositionLocalProvider(
                            LocalLayoutDirection provides if (isRtl) LayoutDirection.Rtl else LayoutDirection.Ltr
                        ) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isCurrent) Color(0x1F00E5FF) else Color.Transparent,
                                border = if (isCurrent) {
                                    androidx.compose.foundation.BorderStroke(1.dp, accentNeon.copy(alpha = 0.6f))
                                } else null,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onSeekTo(line.timestampMs) }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    if (isCurrent) {
                                        Text(
                                            text = "▶",
                                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                            color = accentNeon,
                                            modifier = Modifier.padding(end = 6.dp)
                                        )
                                    }
                                    Text(
                                        text = line.text,
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontSize = if (isCurrent) 14.sp else 12.sp,
                                            fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                                            fontFamily = if (isCurrent) FontFamily.Monospace else FontFamily.Default,
                                            lineBreak = LineBreak.Paragraph
                                        ),
                                        color = when {
                                            isCurrent -> Color.White
                                            isPast -> Color(0xFF94A3B8)
                                            else -> Color(0xFF475569)
                                        },
                                        textAlign = if (isRtl) TextAlign.Right else TextAlign.Left,
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Interactive Audio Frequency Waveform Visualizer Scrubber.
 * Renders dynamic frequency bars with animated playback pulse and tap-to-scrub.
 */
@Composable
private fun InteractiveWaveformScrubber(
    currentPositionMs: Long,
    durationMs: Long,
    isPlaying: Boolean,
    onSeekTo: (Long) -> Unit,
    accentColor: Color,
    secondaryColor: Color,
    modifier: Modifier = Modifier
) {
    val totalDuration = durationMs.coerceAtLeast(1L)
    val progressFraction = (currentPositionMs.toFloat() / totalDuration.toFloat()).coerceIn(0f, 1f)

    val infiniteTransition = rememberInfiniteTransition(label = "waveformPulse")
    val pulsePhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 6.28f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "waveformPulsePhase"
    )

    val barCount = 48
    // Deterministic waveform profile representing music dynamics
    val staticAmplitudes = remember {
        val list = mutableListOf<Float>()
        for (i in 0 until barCount) {
            val normalized = i.toFloat() / barCount
            val amp = 0.25f + 0.45f * sin(normalized * 3.14f * 2.2f).let { (it + 1f) / 2f } +
                0.20f * sin(normalized * 3.14f * 5.8f).let { (it + 1f) / 2f }
            list.add(amp.coerceIn(0.15f, 0.95f))
        }
        list
    }

    Box(
        modifier = modifier
            .pointerInput(totalDuration) {
                detectTapGestures { offset ->
                    val tappedFraction = (offset.x / size.width.toFloat()).coerceIn(0f, 1f)
                    onSeekTo((tappedFraction * totalDuration).toLong())
                }
            }
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val totalWidth = size.width
            val totalHeight = size.height
            val barWidth = (totalWidth / barCount) * 0.65f
            val spacing = (totalWidth / barCount) * 0.35f

            for (i in 0 until barCount) {
                val barFraction = i.toFloat() / barCount
                val isPlayed = barFraction <= progressFraction

                // Dynamic live pulse when playing
                val dynamicBounce = if (isPlaying) {
                    0.12f * sin(pulsePhase + i * 0.4f)
                } else 0f

                val barHeight = ((staticAmplitudes[i] + dynamicBounce).coerceIn(0.12f, 1f)) * totalHeight
                val x = i * (barWidth + spacing)
                val y = (totalHeight - barHeight) / 2f

                val barColor = if (isPlayed) {
                    accentColor
                } else {
                    Color.White.copy(alpha = 0.18f)
                }

                drawRoundRect(
                    color = barColor,
                    topLeft = Offset(x, y),
                    size = Size(barWidth, barHeight),
                    cornerRadius = CornerRadius(barWidth / 2, barWidth / 2)
                )
            }

            // Playhead Cursor Line
            val cursorX = progressFraction * totalWidth
            drawLine(
                color = Color.White,
                start = Offset(cursorX, 0f),
                end = Offset(cursorX, totalHeight),
                strokeWidth = 2.5f
            )
            drawCircle(
                color = accentColor,
                radius = 5.dp.toPx(),
                center = Offset(cursorX, totalHeight / 2)
            )
        }
    }
}
