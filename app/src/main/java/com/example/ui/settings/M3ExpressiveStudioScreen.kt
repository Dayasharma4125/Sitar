package com.example.ui.settings

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
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Equalizer
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.MotionPhotosOn
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AppSettings
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
import com.example.model.Track
import com.example.ui.components.ArtworkImageOrFallback
import com.example.ui.components.DynamicSitarIcon
import com.example.ui.components.M3ExpressiveProgressBar
import com.example.ui.theme.ExpressiveShapes
import com.example.ui.theme.harmonicBreathing
import com.example.ui.theme.springPressPhysics
import com.example.ui.theme.springWobble
import com.example.ui.theme.staggeredSpringEnter

/**
 * Material 3 Expressive Studio & Customization Screen.
 * A dedicated, highly-customizable editable screen offering live preview, shape library geometry,
 * motion physics profiles, atmosphere glow, and transport control modules.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun M3ExpressiveStudioScreen(
    settings: AppSettings,
    onConfigChange: (M3ExpressivePlayerConfig) -> Unit,
    onBackClick: () -> Unit,
    playerUiState: PlayerUiState? = null,
    modifier: Modifier = Modifier
) {
    M3ExpressiveStudioScreen(
        currentConfig = settings.m3ExpressiveConfig,
        onConfigChange = onConfigChange,
        playerUiState = playerUiState,
        onBackClick = onBackClick,
        isAmoledBlack = settings.themeMode == com.example.model.AppThemeMode.PURE_BLACK || settings.amoledBlack,
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun M3ExpressiveStudioScreen(
    currentConfig: M3ExpressivePlayerConfig,
    onConfigChange: (M3ExpressivePlayerConfig) -> Unit,
    playerUiState: PlayerUiState? = null,
    onBackClick: () -> Unit,
    isAmoledBlack: Boolean = false,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    modifier: Modifier = Modifier
) {
    BackHandler(onBack = onBackClick)

    var config by remember(currentConfig) { mutableStateOf(currentConfig) }
    var isPreviewPlaying by remember { mutableStateOf(true) }
    var previewSeekPosition by remember { mutableStateOf(0.42f) }

    val updateConfig: (M3ExpressivePlayerConfig) -> Unit = { newConfig ->
        config = newConfig
        onConfigChange(newConfig)
    }

    val backgroundColor = if (isAmoledBlack) Color(0xFF000000) else MaterialTheme.colorScheme.background
    val sampleTrack = playerUiState?.currentTrack?.takeIf { it.id.isNotBlank() } ?: Track(
        id = "preview_track",
        title = "Raga Yaman (Aura Expressive)",
        artist = "Pandit Nikhil Banerjee",
        album = "Morning Symphony Master",
        durationMs = 284000L,
        audioQuality = "FLAC 24-bit / 96kHz Lossless",
        primaryAmbientColor = Color(0xFF2E4057),
        secondaryAmbientColor = Color(0xFF048A81)
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(backgroundColor)
            .statusBarsPadding()
            .testTag("m3_expressive_studio_screen")
    ) {
        // Top App Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                FilledTonalIconButton(
                    onClick = onBackClick,
                    shape = ExpressiveShapes.PillDock,
                    colors = IconButtonDefaults.filledTonalIconButtonColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                        contentColor = MaterialTheme.colorScheme.onSurface
                    ),
                    modifier = Modifier
                        .size(42.dp)
                        .testTag("studio_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column(modifier = Modifier.weight(1f, fill = false)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "M3 Expressive Studio",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onBackground,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Text(
                        text = "Shapes, physics & stage customization",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 11.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            FilledTonalButton(
                onClick = {
                    val defaultConfig = M3ExpressivePlayerConfig()
                    updateConfig(defaultConfig)
                },
                shape = ExpressiveShapes.PillDock,
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                    contentColor = MaterialTheme.colorScheme.primary
                ),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                modifier = Modifier
                    .springPressPhysics()
                    .testTag("studio_reset_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Reset",
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("Reset", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
            }
        }

        val scrollState = rememberScrollState()
        val bottomInset = contentPadding.calculateBottomPadding()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 18.dp)
                .padding(bottom = bottomInset + 32.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            // Live Interactive Preview Deck
            LiveM3StudioPreview(
                track = sampleTrack,
                config = config,
                isPlaying = isPreviewPlaying,
                seekPosition = previewSeekPosition,
                onTogglePlay = { isPreviewPlaying = !isPreviewPlaying },
                onSeek = { previewSeekPosition = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .staggeredSpringEnter(0)
            )

            // Category 0: Player Stage Style & Paradigm
            StudioSectionCard(
                title = "Player Stage Style & Architecture",
                icon = Icons.Default.Category,
                description = "Switch between full turntable decks, cassette machines, 3D card stacks & minimal stages"
            ) {
                val playerStyles = M3PlayerStyle.entries
                val scroll = rememberScrollState()
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(scroll),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    playerStyles.forEach { style ->
                        val isSelected = config.playerStyle == style
                        FilterChip(
                            selected = isSelected,
                            onClick = { updateConfig(config.copy(playerStyle = style)) },
                            label = { Text(style.displayName, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                            leadingIcon = if (isSelected) {
                                { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                            } else null,
                            shape = ExpressiveShapes.PillDock,
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                                labelColor = MaterialTheme.colorScheme.onSurface
                            ),
                            modifier = Modifier.springPressPhysics { updateConfig(config.copy(playerStyle = style)) }
                        )
                    }
                }
            }

            // Category 1: Expressive Shape Library Geometry
            StudioSectionCard(
                title = "Shape Library & Geometry",
                icon = Icons.Default.Category,
                description = "Choose geometric curvature and contour contours for album artwork and containers"
            ) {
                Text(
                    text = "Album Artwork Shape",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(10.dp))

                // Horizontal Flow Chips of all Expressive Shapes
                val shapes = M3ArtworkShape.entries
                val scroll = rememberScrollState()
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(scroll),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    shapes.forEach { shape ->
                        val isSelected = config.artworkShape == shape
                        FilterChip(
                            selected = isSelected,
                            onClick = { updateConfig(config.copy(artworkShape = shape)) },
                            label = { Text(shape.displayName, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                            leadingIcon = if (isSelected) {
                                { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                            } else null,
                            shape = ExpressiveShapes.PillDock,
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                                labelColor = MaterialTheme.colorScheme.onSurface
                            ),
                            modifier = Modifier.springPressPhysics { updateConfig(config.copy(artworkShape = shape)) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Artwork Proportions",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    M3ArtworkSize.entries.forEach { size ->
                        val isSelected = config.artworkSize == size
                        Surface(
                            onClick = { updateConfig(config.copy(artworkSize = size)) },
                            shape = ExpressiveShapes.Small,
                            color = if (isSelected) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surfaceContainerHigh,
                            border = if (isSelected) BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary) else null,
                            modifier = Modifier
                                .weight(1f)
                                .springPressPhysics { updateConfig(config.copy(artworkSize = size)) }
                        ) {
                            Column(
                                modifier = Modifier.padding(vertical = 10.dp, horizontal = 8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = size.displayName,
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                    color = if (isSelected) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }

            // Category 1.5: Placement of Elements & Hierarchy
            StudioSectionCard(
                title = "Placement of Elements & Alignment",
                icon = Icons.Default.Tune,
                description = "Configure the vertical ordering and thumb reach zones of controls on the player stage"
            ) {
                Text(
                    text = "Element Vertical Hierarchy",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))

                M3ElementPlacement.entries.forEach { placement ->
                    val isSelected = config.elementPlacement == placement
                    Surface(
                        onClick = { updateConfig(config.copy(elementPlacement = placement)) },
                        shape = ExpressiveShapes.Medium,
                        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerHigh,
                        border = if (isSelected) BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary) else null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .springPressPhysics { updateConfig(config.copy(elementPlacement = placement)) }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = placement.displayName,
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                    color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = placement.description,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = "Track Information Alignment",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    M3InfoAlignment.entries.forEach { align ->
                        val isSelected = config.infoAlignment == align
                        Surface(
                            onClick = { updateConfig(config.copy(infoAlignment = align)) },
                            shape = ExpressiveShapes.Small,
                            color = if (isSelected) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surfaceContainerHigh,
                            border = if (isSelected) BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary) else null,
                            modifier = Modifier
                                .weight(1f)
                                .springPressPhysics { updateConfig(config.copy(infoAlignment = align)) }
                        ) {
                            Column(
                                modifier = Modifier.padding(vertical = 10.dp, horizontal = 6.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = align.displayName,
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = if (isSelected) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onSurface,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
            }

            // Category 2: Expressive Motion Physics
            StudioSectionCard(
                title = "Motion Physics & Dynamics",
                icon = Icons.Default.MotionPhotosOn,
                description = "Harmonic mass-spring-damper dynamics, tactile elastic squish, and continuous rhythm"
            ) {
                Text(
                    text = "Spring Motion Profile",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))

                M3MotionProfile.entries.forEach { profile ->
                    val isSelected = config.motionProfile == profile
                    Surface(
                        onClick = { updateConfig(config.copy(motionProfile = profile)) },
                        shape = ExpressiveShapes.Medium,
                        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerHigh,
                        border = if (isSelected) BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary) else null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .springPressPhysics { updateConfig(config.copy(motionProfile = profile)) }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = profile.displayName,
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                    color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = profile.description,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                StudioToggleRow(
                    title = "Live Harmonic Breathing",
                    subtitle = "Artwork softly expands & contracts in rhythm with playback",
                    icon = Icons.Default.Speed,
                    checked = config.harmonicBreathingEnabled,
                    onCheckedChange = { updateConfig(config.copy(harmonicBreathingEnabled = it)) }
                )

                StudioToggleRow(
                    title = "Tactile Spring Haptics",
                    subtitle = "Physical micro-vibration pulses on interactive controls",
                    icon = Icons.Default.Vibration,
                    checked = config.expressiveHaptics,
                    onCheckedChange = { updateConfig(config.copy(expressiveHaptics = it)) }
                )
            }

            // Category 3: Scrubber & Progress Bar Style
            StudioSectionCard(
                title = "Scrubber & Playback Wave",
                icon = Icons.Default.GraphicEq,
                description = "Choose fluid wave styles, chunky tactile sliders, or real-time spectrum tracking"
            ) {
                val scrubberScroll = rememberScrollState()
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(scrubberScroll),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    M3ScrubberStyle.entries.forEach { scrubberStyle ->
                        val isSelected = config.scrubberStyle == scrubberStyle
                        FilterChip(
                            selected = isSelected,
                            onClick = { updateConfig(config.copy(scrubberStyle = scrubberStyle)) },
                            label = { Text(scrubberStyle.displayName, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                            leadingIcon = if (isSelected) {
                                { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                            } else null,
                            shape = ExpressiveShapes.PillDock,
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onSecondaryContainer,
                                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                                labelColor = MaterialTheme.colorScheme.onSurface
                            ),
                            modifier = Modifier.springPressPhysics { updateConfig(config.copy(scrubberStyle = scrubberStyle)) }
                        )
                    }
                }
            }

            // Category 4: Hero Transport Control Geometry & Button Styles
            StudioSectionCard(
                title = "Hero & Transport Button Styles",
                icon = Icons.Default.PlayArrow,
                description = "Choose hero play/pause button geometry, halo glow rings, and secondary button styling"
            ) {
                Text(
                    text = "Hero Play/Pause Button Style",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(10.dp))

                val heroBtnScroll = rememberScrollState()
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(heroBtnScroll),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    M3HeroButtonStyle.entries.forEach { buttonStyle ->
                        val isSelected = config.heroButtonStyle == buttonStyle
                        FilterChip(
                            selected = isSelected,
                            onClick = { updateConfig(config.copy(heroButtonStyle = buttonStyle)) },
                            label = { Text(buttonStyle.displayName, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                            leadingIcon = if (isSelected) {
                                { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                            } else null,
                            shape = ExpressiveShapes.PillDock,
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                                labelColor = MaterialTheme.colorScheme.onSurface
                            ),
                            modifier = Modifier.springPressPhysics { updateConfig(config.copy(heroButtonStyle = buttonStyle)) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Secondary Controls Style (Prev/Next/Shuffle/Repeat)",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))

                val ctrlBtnScroll = rememberScrollState()
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(ctrlBtnScroll),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    M3ControlButtonStyle.entries.forEach { ctrlStyle ->
                        val isSelected = config.controlButtonStyle == ctrlStyle
                        FilterChip(
                            selected = isSelected,
                            onClick = { updateConfig(config.copy(controlButtonStyle = ctrlStyle)) },
                            label = { Text(ctrlStyle.displayName, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                            leadingIcon = if (isSelected) {
                                { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                            } else null,
                            shape = ExpressiveShapes.PillDock,
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.tertiaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onTertiaryContainer,
                                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                                labelColor = MaterialTheme.colorScheme.onSurface
                            ),
                            modifier = Modifier.springPressPhysics { updateConfig(config.copy(controlButtonStyle = ctrlStyle)) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
                StudioToggleRow(
                    title = "Button Halo Glow Effect",
                    subtitle = "Luminous ambient bloom around transport buttons",
                    icon = Icons.Default.AutoAwesome,
                    checked = config.buttonGlowEffect,
                    onCheckedChange = { updateConfig(config.copy(buttonGlowEffect = it)) }
                )
            }

            // Category 5: Stage Background & Ambient Aura
            StudioSectionCard(
                title = "Atmosphere & Ambient Aura",
                icon = Icons.Default.Palette,
                description = "Tune dynamic ambient color aura glow and surface reflection"
            ) {
                M3BackgroundStyle.entries.forEach { bgStyle ->
                    val isSelected = config.backgroundStyle == bgStyle
                    Surface(
                        onClick = { updateConfig(config.copy(backgroundStyle = bgStyle)) },
                        shape = ExpressiveShapes.Medium,
                        color = if (isSelected) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surfaceContainerHigh,
                        border = if (isSelected) BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary) else null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .springPressPhysics { updateConfig(config.copy(backgroundStyle = bgStyle)) }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = bgStyle.displayName,
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                    color = if (isSelected) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = bgStyle.description,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Aura Glow Intensity: ${(config.auraGlowStrength * 100).toInt()}%",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Slider(
                    value = config.auraGlowStrength,
                    onValueChange = { updateConfig(config.copy(auraGlowStrength = it)) },
                    valueRange = 0.2f..1.0f,
                    colors = SliderDefaults.colors(
                        thumbColor = MaterialTheme.colorScheme.primary,
                        activeTrackColor = MaterialTheme.colorScheme.primary,
                        inactiveTrackColor = MaterialTheme.colorScheme.surfaceContainerHighest
                    )
                )
            }

            // Category 6: Interactive Modules & Widgets
            StudioSectionCard(
                title = "Player Screen Modules & Widgets",
                icon = Icons.Default.Widgets,
                description = "Toggle visible components on the Now Playing stage"
            ) {
                StudioToggleRow(
                    title = "Live Audio Visualizer",
                    subtitle = "Animated frequency spectrum responding to sound",
                    icon = Icons.Default.Equalizer,
                    checked = config.showVisualizer,
                    onCheckedChange = { updateConfig(config.copy(showVisualizer = it)) }
                )
                StudioToggleRow(
                    title = "Hi-Res Audio Specs Badge",
                    subtitle = "Display FLAC/Lossless bitrate & sample rate badge",
                    icon = Icons.Default.GraphicEq,
                    checked = config.showAudioSpecs,
                    onCheckedChange = { updateConfig(config.copy(showAudioSpecs = it)) }
                )
                StudioToggleRow(
                    title = "Tactile Volume Slider",
                    subtitle = "Hardware volume slider integrated in player stage",
                    icon = Icons.Default.VolumeUp,
                    checked = config.showVolumeSlider,
                    onCheckedChange = { updateConfig(config.copy(showVolumeSlider = it)) }
                )
                StudioToggleRow(
                    title = "Active Output Device Capsule",
                    subtitle = "Connected speaker / Bluetooth indicator pill",
                    icon = Icons.Default.Tune,
                    checked = config.showDeviceCapsule,
                    onCheckedChange = { updateConfig(config.copy(showDeviceCapsule = it)) }
                )
                StudioToggleRow(
                    title = "In-Place Lyrics Card",
                    subtitle = "Expandable synced lyrics panel below transport controls",
                    icon = Icons.Default.MusicNote,
                    checked = config.showLyricsCard,
                    onCheckedChange = { updateConfig(config.copy(showLyricsCard = it)) }
                )
            }
        }
    }
}

/**
 * Live Interactive Preview Deck of the M3 Expressive Player.
 */
@Composable
private fun LiveM3StudioPreview(
    track: Track,
    config: M3ExpressivePlayerConfig,
    isPlaying: Boolean,
    seekPosition: Float,
    onTogglePlay: () -> Unit,
    onSeek: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    val artworkShape: Shape = when (config.artworkShape) {
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

    val heroButtonShape: Shape = when (config.heroButtonStyle) {
        M3HeroButtonStyle.EXPRESSIVE_SQUIRCLE -> ExpressiveShapes.Squircle
        M3HeroButtonStyle.PILL_FAB -> ExpressiveShapes.PillDock
        M3HeroButtonStyle.CIRCLE_HERO -> CircleShape
        M3HeroButtonStyle.ASYMMETRIC_DIAMOND -> ExpressiveShapes.CutCornerCard
        M3HeroButtonStyle.NEUMORPHIC_CONTOUR -> RoundedCornerShape(26.dp)
        M3HeroButtonStyle.DOUBLE_RING_ORBIT -> CircleShape
        M3HeroButtonStyle.ORGANIC_BLOSSOM -> ExpressiveShapes.CloverShape(0.40f)
        M3HeroButtonStyle.HEXAGON_PRISM -> ExpressiveShapes.HexagonalBadgeShape()
        M3HeroButtonStyle.CAPSULE_SEGMENTED -> ExpressiveShapes.PillDock
        M3HeroButtonStyle.MINIMAL_OUTLINE_GLOW -> CircleShape
        M3HeroButtonStyle.STARBURST_HERO -> ExpressiveShapes.ScallopedBadgeShape(8)
        M3HeroButtonStyle.STADIUM_BADGE -> RoundedCornerShape(24.dp)
    }

    val primaryAccent = MaterialTheme.colorScheme.primary

    Card(
        shape = ExpressiveShapes.Large,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
        border = BorderStroke(1.5.dp, primaryAccent.copy(alpha = 0.4f)),
        modifier = modifier
            .fillMaxWidth()
            .shadow(16.dp, ExpressiveShapes.Large, spotColor = primaryAccent.copy(alpha = 0.25f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Live Preview Header Tag
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = ExpressiveShapes.PillDock,
                    color = primaryAccent.copy(alpha = 0.15f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(if (isPlaying) Color(0xFF10B981) else Color(0xFFF59E0B))
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "LIVE INTERACTIVE PREVIEW",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = primaryAccent
                        )
                    }
                }

                Text(
                    text = config.artworkShape.displayName,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Artwork container with real-time breathing & shape
            Box(
                modifier = Modifier
                    .size(160.dp)
                    .clip(artworkShape)
                    .background(MaterialTheme.colorScheme.surfaceContainerHighest)
                    .then(
                        if (config.harmonicBreathingEnabled && isPlaying) {
                            Modifier.harmonicBreathing(isPlaying = true, minScale = 0.96f, maxScale = 1.04f, durationMs = 2000)
                        } else Modifier
                    ),
                contentAlignment = Alignment.Center
            ) {
                ArtworkImageOrFallback(
                    track = track,
                    modifier = Modifier.fillMaxSize()
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Track info
            Text(
                text = track.title,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "${track.artist} • ${track.album}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Progress Bar
            val totalMockDurationMs = 214000L
            val currentMockPositionMs = (seekPosition * totalMockDurationMs).toLong()
            M3ExpressiveProgressBar(
                currentPositionMs = currentMockPositionMs,
                durationMs = totalMockDurationMs,
                onSeekTo = { newMs ->
                    onSeek((newMs.toFloat() / totalMockDurationMs.toFloat()).coerceIn(0f, 1f))
                },
                activeColor = primaryAccent,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Transport Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = {},
                    modifier = Modifier
                        .size(42.dp)
                        .springPressPhysics {}
                ) {
                    Icon(
                        imageVector = Icons.Default.SkipPrevious,
                        contentDescription = "Previous",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }

                // Hero Button
                Surface(
                    onClick = onTogglePlay,
                    shape = heroButtonShape,
                    color = MaterialTheme.colorScheme.primaryContainer,
                    tonalElevation = 6.dp,
                    modifier = Modifier
                        .size(56.dp)
                        .springWobble(isPlaying)
                        .springPressPhysics { onTogglePlay() }
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (isPlaying) "Pause" else "Play",
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }

                IconButton(
                    onClick = {},
                    modifier = Modifier
                        .size(42.dp)
                        .springPressPhysics {}
                ) {
                    Icon(
                        imageVector = Icons.Default.SkipNext,
                        contentDescription = "Next",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

@Composable
private fun StudioSectionCard(
    title: String,
    icon: ImageVector,
    description: String,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Card(
        shape = ExpressiveShapes.Medium,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = ExpressiveShapes.Squircle,
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                    modifier = Modifier.size(36.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f))
            Spacer(modifier = Modifier.height(14.dp))

            content()
        }
    }
}

@Composable
private fun StudioToggleRow(
    title: String,
    subtitle: String,
    icon: ImageVector,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (checked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                checkedTrackColor = MaterialTheme.colorScheme.primary
            ),
            modifier = Modifier.testTag("switch_${title.lowercase().replace(" ", "_")}")
        )
    }
}
