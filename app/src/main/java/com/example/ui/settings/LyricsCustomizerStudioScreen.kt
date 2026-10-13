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
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Animation
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BlurOn
import androidx.compose.material.icons.filled.Brightness6
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Contrast
import androidx.compose.material.icons.filled.FormatAlignCenter
import androidx.compose.material.icons.filled.FormatAlignLeft
import androidx.compose.material.icons.filled.FormatAlignRight
import androidx.compose.material.icons.filled.FormatSize
import androidx.compose.material.icons.filled.Lyrics
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AppSettings
import com.example.model.LyricsBackdropStyle
import com.example.model.LyricsScreenConfig
import com.example.model.LyricsTextAlignment
import com.example.model.LyricsTextSize
import com.example.model.Track
import com.example.ui.components.AnimatedM3IconButton
import com.example.ui.theme.ExpressiveShapes
import com.example.ui.theme.getFontFamilyForMusic
import com.example.ui.theme.springPressPhysics
import kotlinx.coroutines.delay

/**
 * Dedicated Full-Screen Lyrics & Typography Customization Studio.
 * Offers real-time live preview, text size scaling, alignment selection,
 * contrast fine-tuning (80% default), backdrop style controls, and karaoke animations.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LyricsCustomizerStudioScreen(
    settings: AppSettings,
    onConfigChange: (LyricsScreenConfig) -> Unit,
    onBackClick: () -> Unit,
    sampleTrack: Track? = null,
    modifier: Modifier = Modifier
) {
    BackHandler(onBack = onBackClick)

    var config by remember(settings.lyricsConfig) { mutableStateOf(settings.lyricsConfig) }

    val updateConfig: (LyricsScreenConfig) -> Unit = { newConfig ->
        config = newConfig
        onConfigChange(newConfig)
    }

    val isAmoled = settings.themeMode == com.example.model.AppThemeMode.PURE_BLACK || settings.amoledBlack
    val backgroundColor = if (isAmoled) Color(0xFF000000) else MaterialTheme.colorScheme.background
    val primaryColor = MaterialTheme.colorScheme.primary
    val textColor = MaterialTheme.colorScheme.onBackground
    val textSecondary = MaterialTheme.colorScheme.onSurfaceVariant
    val cardColor = if (isAmoled) Color(0xFF10141C) else MaterialTheme.colorScheme.surfaceContainer
    val cardBorderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)

    val activeTrack = sampleTrack ?: Track(
        id = "preview_lyrics_track",
        title = "Clair de Lune (Aura Master)",
        artist = "Claude Debussy",
        album = "Suite Bergamasque Hi-Res",
        durationMs = 310000L,
        primaryAmbientColor = Color(0xFF3B82F6),
        secondaryAmbientColor = Color(0xFF8B5CF6)
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(backgroundColor)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        // TOP APP BAR
        TopAppBar(
            title = {
                Column {
                    Text(
                        text = "Lyrics & Typography Studio",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = textColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "Appearance, contrast & animations",
                        style = MaterialTheme.typography.bodySmall,
                        color = textSecondary,
                        fontSize = 11.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            },
            navigationIcon = {
                AnimatedM3IconButton(
                    icon = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = textColor,
                    onClick = onBackClick,
                    testTag = "lyrics_studio_back"
                )
            },
            actions = {
                IconButton(
                    onClick = {
                        val defaultConfig = LyricsScreenConfig()
                        updateConfig(defaultConfig)
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Reset to Defaults",
                        tint = primaryColor
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = backgroundColor,
                titleContentColor = textColor
            )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 18.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. LIVE INTERACTIVE LYRICS PREVIEW CARD
            LyricsLivePreviewCard(
                config = config,
                track = activeTrack,
                isAmoled = isAmoled
            )

            // 2. TEXT SIZE & SCALE CARD
            LyricsStudioSectionCard(
                title = "Typography & Text Size",
                subtitle = "Select font scaling for full-screen lyrics viewing",
                icon = Icons.Default.FormatSize,
                cardColor = cardColor,
                cardBorderColor = cardBorderColor,
                primaryColor = primaryColor
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    LyricsTextSize.entries.forEach { sizeOption ->
                        val isSelected = config.textSize == sizeOption
                        Surface(
                            onClick = { updateConfig(config.copy(textSize = sizeOption)) },
                            shape = RoundedCornerShape(16.dp),
                            color = if (isSelected) primaryColor.copy(alpha = 0.22f) else MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.5f),
                            border = if (isSelected) BorderStroke(1.5.dp, primaryColor) else null,
                            modifier = Modifier
                                .weight(1f)
                                .springPressPhysics()
                        ) {
                            Column(
                                modifier = Modifier.padding(vertical = 12.dp, horizontal = 6.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = sizeOption.displayName,
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                    color = if (isSelected) primaryColor else textColor,
                                    fontSize = 12.sp,
                                    maxLines = 1
                                )
                                Spacer(modifier = Modifier.height(3.dp))
                                Text(
                                    text = "${sizeOption.activeSp.toInt()}sp",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (isSelected) primaryColor else textSecondary,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                }
            }

            // 3. TEXT ALIGNMENT CARD
            LyricsStudioSectionCard(
                title = "Text Alignment",
                subtitle = "Align lyric lines to match your reading style",
                icon = Icons.Default.FormatAlignLeft,
                cardColor = cardColor,
                cardBorderColor = cardBorderColor,
                primaryColor = primaryColor
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val alignments = listOf(
                        Triple(LyricsTextAlignment.LEFT, "Left", Icons.Default.FormatAlignLeft),
                        Triple(LyricsTextAlignment.CENTER, "Center", Icons.Default.FormatAlignCenter),
                        Triple(LyricsTextAlignment.RIGHT, "Right", Icons.Default.FormatAlignRight)
                    )

                    alignments.forEach { (align, label, iconVector) ->
                        val isSelected = config.textAlignment == align
                        Surface(
                            onClick = { updateConfig(config.copy(textAlignment = align)) },
                            shape = RoundedCornerShape(16.dp),
                            color = if (isSelected) primaryColor.copy(alpha = 0.22f) else MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.5f),
                            border = if (isSelected) BorderStroke(1.5.dp, primaryColor) else null,
                            modifier = Modifier
                                .weight(1f)
                                .springPressPhysics()
                        ) {
                            Row(
                                modifier = Modifier.padding(vertical = 12.dp, horizontal = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = iconVector,
                                    contentDescription = null,
                                    tint = if (isSelected) primaryColor else textSecondary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                    color = if (isSelected) primaryColor else textColor
                                )
                            }
                        }
                    }
                }
            }

            // 4. INACTIVE TEXT CONTRAST (80% Default & Custom Slider)
            LyricsStudioSectionCard(
                title = "Text Contrast & Readability",
                subtitle = "Adjust inactive lyrics opacity against vibrant artwork (Default: 80%)",
                icon = Icons.Default.Contrast,
                cardColor = cardColor,
                cardBorderColor = cardBorderColor,
                primaryColor = primaryColor
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Inactive Lines Contrast",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = textColor
                        )
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = primaryColor.copy(alpha = 0.18f),
                            border = BorderStroke(1.dp, primaryColor.copy(alpha = 0.4f))
                        ) {
                            Text(
                                text = "${(config.inactiveTextContrast * 100).toInt()}%",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.ExtraBold),
                                color = primaryColor,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }

                    Slider(
                        value = config.inactiveTextContrast,
                        onValueChange = { updateConfig(config.copy(inactiveTextContrast = it)) },
                        valueRange = 0.40f..0.98f,
                        colors = SliderDefaults.colors(
                            thumbColor = primaryColor,
                            activeTrackColor = primaryColor,
                            inactiveTrackColor = MaterialTheme.colorScheme.surfaceContainerHighest
                        )
                    )

                    // Quick Contrast Preset Chips
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val presets = listOf(
                            0.60f to "60% Soft",
                            0.80f to "80% Optimal",
                            0.90f to "90% Crisp",
                            0.96f to "96% Ultra"
                        )

                        presets.forEach { (contrastVal, label) ->
                            val isSelected = kotlin.math.abs(config.inactiveTextContrast - contrastVal) < 0.04f
                            Surface(
                                onClick = { updateConfig(config.copy(inactiveTextContrast = contrastVal)) },
                                shape = RoundedCornerShape(12.dp),
                                color = if (isSelected) primaryColor else MaterialTheme.colorScheme.surfaceContainerHighest,
                                modifier = Modifier
                                    .weight(1f)
                                    .springPressPhysics()
                            ) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier.padding(vertical = 8.dp)
                                ) {
                                    Text(
                                        text = label,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                        ),
                                        color = if (isSelected) Color.White else textColor,
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 5. ATMOSPHERIC BACKDROP STYLE CARD
            LyricsStudioSectionCard(
                title = "Atmospheric Backdrop Effect",
                subtitle = "Choose background aura behind scrolling lyrics",
                icon = Icons.Default.BlurOn,
                cardColor = cardColor,
                cardBorderColor = cardBorderColor,
                primaryColor = primaryColor
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    LyricsBackdropStyle.entries.forEach { style ->
                        val isSelected = config.backdropStyle == style
                        Surface(
                            onClick = { updateConfig(config.copy(backdropStyle = style)) },
                            shape = RoundedCornerShape(16.dp),
                            color = if (isSelected) primaryColor.copy(alpha = 0.16f) else MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.45f),
                            border = if (isSelected) BorderStroke(1.5.dp, primaryColor) else null,
                            modifier = Modifier
                                .fillMaxWidth()
                                .springPressPhysics()
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(22.dp)
                                        .clip(CircleShape)
                                        .background(if (isSelected) primaryColor else Color.Transparent)
                                        .then(
                                            if (!isSelected) Modifier.background(Color.White.copy(alpha = 0.15f)) else Modifier
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (isSelected) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = style.displayName,
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                        color = if (isSelected) primaryColor else textColor
                                    )
                                    Text(
                                        text = style.description,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = textSecondary,
                                        fontSize = 11.5.sp
                                    )
                                }
                            }
                        }
                    }

                    if (config.backdropStyle == LyricsBackdropStyle.DYNAMIC_GAUSSIAN_BLUR) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Blur Mesh Intensity",
                                style = MaterialTheme.typography.bodyMedium,
                                color = textColor
                            )
                            Text(
                                text = "${String.format(java.util.Locale.US, "%.2f", config.blurIntensity)}x",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = primaryColor
                            )
                        }
                        Slider(
                            value = config.blurIntensity,
                            onValueChange = { updateConfig(config.copy(blurIntensity = it)) },
                            valueRange = 0.4f..1.5f,
                            colors = SliderDefaults.colors(
                                thumbColor = primaryColor,
                                activeTrackColor = primaryColor
                            )
                        )
                    }
                }
            }

            // 6. VISUAL ANIMATIONS & INTERACTIVE CONTROLS
            LyricsStudioSectionCard(
                title = "Animations & Interactive Controls",
                subtitle = "Toggle karaoke motion effects and screen widgets",
                icon = Icons.Default.Animation,
                cardColor = cardColor,
                cardBorderColor = cardBorderColor,
                primaryColor = primaryColor
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    LyricsCustomizerToggle(
                        title = "Word-by-Word Karaoke Fill",
                        description = "Smooth progressive glowing text highlight synchronized to active singing",
                        checked = config.karaokeWordFillEnabled,
                        onCheckedChange = { updateConfig(config.copy(karaokeWordFillEnabled = it)) },
                        primaryColor = primaryColor,
                        textColor = textColor,
                        textSecondary = textSecondary
                    )

                    LyricsCustomizerToggle(
                        title = "Staggered Slide-In Entrance",
                        description = "Smooth vertical slide-in and spring settling for each lyric line on entry",
                        checked = config.staggeredSlideIn,
                        onCheckedChange = { updateConfig(config.copy(staggeredSlideIn = it)) },
                        primaryColor = primaryColor,
                        textColor = textColor,
                        textSecondary = textSecondary
                    )

                    LyricsCustomizerToggle(
                        title = "Luminous Bloom Text Glow",
                        description = "Vibrant artwork-tinted halo shadow behind the currently active lyric line",
                        checked = config.lyricGlowBloom,
                        onCheckedChange = { updateConfig(config.copy(lyricGlowBloom = it)) },
                        primaryColor = primaryColor,
                        textColor = textColor,
                        textSecondary = textSecondary
                    )

                    LyricsCustomizerToggle(
                        title = "Mini Floating Transport Deck",
                        description = "Compact play/pause, skip, and mini scrubber bar floating cleanly at the bottom",
                        checked = config.showMiniTransportDeck,
                        onCheckedChange = { updateConfig(config.copy(showMiniTransportDeck = it)) },
                        primaryColor = primaryColor,
                        textColor = textColor,
                        textSecondary = textSecondary
                    )

                    LyricsCustomizerToggle(
                        title = "Keep Lyrics in Focus (Center Active Line)",
                        description = "Automatically locks and smoothly glides the active lyric line into the exact middle of the screen",
                        checked = config.autoScrollEnabled,
                        onCheckedChange = { updateConfig(config.copy(autoScrollEnabled = it)) },
                        primaryColor = primaryColor,
                        textColor = textColor,
                        textSecondary = textSecondary
                    )

                    LyricsCustomizerToggle(
                        title = "Interactive Tap-to-Seek",
                        description = "Instantly seek music playback directly by tapping on any lyric timestamp",
                        checked = config.tapToSeekEnabled,
                        onCheckedChange = { updateConfig(config.copy(tapToSeekEnabled = it)) },
                        primaryColor = primaryColor,
                        textColor = textColor,
                        textSecondary = textSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}

/**
 * Live interactive preview card of full-screen lyrics with dynamic text size, contrast, and alignment.
 */
@Composable
private fun LyricsLivePreviewCard(
    config: LyricsScreenConfig,
    track: Track,
    isAmoled: Boolean
) {
    val musicFontFamily = remember(track) { getFontFamilyForMusic(track) }
    val primaryAccent = track.primaryAmbientColor.takeIf { it != Color.Unspecified } ?: Color(0xFF3B82F6)
    val secondaryAccent = track.secondaryAmbientColor.takeIf { it != Color.Unspecified } ?: Color(0xFF8B5CF6)

    val infiniteTransition = rememberInfiniteTransition(label = "lyricsPreviewTransition")
    val previewProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "previewProgress"
    )

    Surface(
        shape = RoundedCornerShape(26.dp),
        color = Color(0xFF0C1018),
        border = BorderStroke(1.5.dp, primaryAccent.copy(alpha = 0.50f)),
        modifier = Modifier
            .fillMaxWidth()
            .height(250.dp)
            .clip(RoundedCornerShape(26.dp))
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Dynamic Backdrop
            when (config.backdropStyle) {
                LyricsBackdropStyle.DYNAMIC_GAUSSIAN_BLUR -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .blur(radius = (36.dp * config.blurIntensity).coerceIn(16.dp, 50.dp))
                            .drawBehind {
                                val w = size.width
                                val h = size.height
                                drawCircle(
                                    brush = Brush.radialGradient(
                                        colors = listOf(primaryAccent.copy(alpha = 0.70f), Color.Transparent),
                                        center = Offset(w * 0.35f, h * 0.35f),
                                        radius = w * 0.8f
                                    )
                                )
                                drawCircle(
                                    brush = Brush.radialGradient(
                                        colors = listOf(secondaryAccent.copy(alpha = 0.60f), Color.Transparent),
                                        center = Offset(w * 0.75f, h * 0.65f),
                                        radius = w * 0.75f
                                    )
                                )
                            }
                    )
                }
                LyricsBackdropStyle.ORBITAL_AURA -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(primaryAccent.copy(alpha = 0.40f), Color(0xFF090D14)),
                                    center = Offset(200f, 200f),
                                    radius = 600f
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
                                    colors = listOf(primaryAccent.copy(alpha = 0.25f), Color(0xFF06080E))
                                )
                            )
                    )
                }
                LyricsBackdropStyle.PURE_AMOLED_BLACK -> {
                    Box(modifier = Modifier.fillMaxSize().background(Color.Black))
                }
            }

            // High-Contrast Dimming Scrim
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = if (isAmoled) 0.65f else 0.48f))
            )

            // Header Tag in Preview
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp)
                    .align(Alignment.TopCenter),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = primaryAccent.copy(alpha = 0.25f),
                    border = BorderStroke(1.dp, primaryAccent.copy(alpha = 0.45f))
                ) {
                    Text(
                        text = "LIVE FULL-SCREEN PREVIEW",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 1.sp
                        ),
                        color = primaryAccent,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }

                Text(
                    text = "${(config.inactiveTextContrast * 100).toInt()}% Contrast",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = Color.White.copy(alpha = 0.85f)
                )
            }

            // Preview Lyrics Content
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp, vertical = 40.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = when (config.textAlignment) {
                    LyricsTextAlignment.LEFT -> Alignment.Start
                    LyricsTextAlignment.CENTER -> Alignment.CenterHorizontally
                    LyricsTextAlignment.RIGHT -> Alignment.End
                }
            ) {
                val align = when (config.textAlignment) {
                    LyricsTextAlignment.LEFT -> TextAlign.Start
                    LyricsTextAlignment.CENTER -> TextAlign.Center
                    LyricsTextAlignment.RIGHT -> TextAlign.End
                }

                // Inactive Line 1 (Showing 80% contrast!)
                Text(
                    text = "Dans le calme de la nuit céleste",
                    fontFamily = musicFontFamily,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = config.textSize.inactiveSp.sp,
                        fontWeight = FontWeight.Medium,
                        textAlign = align
                    ),
                    color = Color.White.copy(alpha = config.inactiveTextContrast),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Active Line (Word-by-word fill + Bloom Shadow)
                val karaokeBrush = if (config.karaokeWordFillEnabled) {
                    val stop1 = (previewProgress - 0.05f).coerceAtLeast(0f)
                    val stop2 = (previewProgress + 0.05f).coerceAtMost(1f)
                    Brush.horizontalGradient(
                        colorStops = arrayOf(
                            0.0f to Color.White,
                            stop1 to Color.White,
                            stop2 to Color.White.copy(alpha = 0.50f),
                            1.0f to Color.White.copy(alpha = 0.50f)
                        )
                    )
                } else null

                if (karaokeBrush != null) {
                    Text(
                        text = "Au clair de lune, mon ami Pierrot",
                        fontFamily = musicFontFamily,
                        style = TextStyle(
                            brush = karaokeBrush,
                            fontSize = config.textSize.activeSp.sp,
                            fontWeight = FontWeight.ExtraBold,
                            textAlign = align,
                            shadow = if (config.lyricGlowBloom) Shadow(
                                color = primaryAccent.copy(alpha = 0.90f),
                                blurRadius = 22f
                            ) else null
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                } else {
                    Text(
                        text = "Au clair de lune, mon ami Pierrot",
                        fontFamily = musicFontFamily,
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontSize = config.textSize.activeSp.sp,
                            fontWeight = FontWeight.ExtraBold,
                            textAlign = align,
                            shadow = if (config.lyricGlowBloom) Shadow(
                                color = primaryAccent.copy(alpha = 0.90f),
                                blurRadius = 22f
                            ) else null
                        ),
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Inactive Line 2 (Showing 80% contrast!)
                Text(
                    text = "Prête-moi ta plume pour écrire un mot",
                    fontFamily = musicFontFamily,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = config.textSize.inactiveSp.sp,
                        fontWeight = FontWeight.Medium,
                        textAlign = align
                    ),
                    color = Color.White.copy(alpha = config.inactiveTextContrast),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

/**
 * Reusable Section Card for the Lyrics Customizer Studio.
 */
@Composable
private fun LyricsStudioSectionCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    cardColor: Color,
    cardBorderColor: Color,
    primaryColor: Color,
    content: @Composable () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(24.dp),
        color = cardColor,
        border = BorderStroke(1.dp, cardBorderColor),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = primaryColor.copy(alpha = 0.16f),
                    border = BorderStroke(1.dp, primaryColor.copy(alpha = 0.35f)),
                    modifier = Modifier.size(38.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = primaryColor,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 11.5.sp
                    )
                }
            }

            content()
        }
    }
}

/**
 * Switch Toggle item for lyrics studio options.
 */
@Composable
private fun LyricsCustomizerToggle(
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    primaryColor: Color,
    textColor: Color,
    textSecondary: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable { onCheckedChange(!checked) }
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                color = textColor
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = textSecondary,
                fontSize = 11.5.sp,
                lineHeight = 15.sp
            )
        }

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = primaryColor
            )
        )
    }
}
