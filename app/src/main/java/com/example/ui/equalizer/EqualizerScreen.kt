package com.example.ui.equalizer

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.SurroundSound
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Waves
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.EqualizerBandState
import com.example.model.EqualizerUiState
import com.example.ui.util.HapticType
import com.example.ui.util.rememberHapticController

/**
 * Material Expressive 3 Equalizer Screen.
 * Features an interactive Bezier frequency response spline curve, expressive chunky sliders,
 * dynamic M3 color tokens, sound preset chips carousel, and dual audio effects bento cards.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EqualizerScreen(
    uiState: EqualizerUiState,
    onToggleEqualizer: (Boolean) -> Unit,
    onBandGainChange: (Short, Short) -> Unit,
    onPresetSelect: (Short) -> Unit,
    onBassBoostChange: (Short) -> Unit,
    onVirtualizerChange: (Short) -> Unit,
    onReverbPresetChange: (Short) -> Unit = {},
    onLoudnessGainChange: (Int) -> Unit = {},
    onResetToFlat: () -> Unit = {},
    onOpenSystemEqualizer: () -> Unit,
    onBackClick: () -> Unit,
    isAmoledBlack: Boolean = false,
    modifier: Modifier = Modifier
) {
    val haptic = rememberHapticController()

    // Expressive Material 3 Dynamic Tokens
    val primaryColor = MaterialTheme.colorScheme.primary
    val onPrimaryColor = MaterialTheme.colorScheme.onPrimary
    val primaryContainer = MaterialTheme.colorScheme.primaryContainer
    val onPrimaryContainer = MaterialTheme.colorScheme.onPrimaryContainer
    val secondaryColor = MaterialTheme.colorScheme.secondary
    val tertiaryColor = MaterialTheme.colorScheme.tertiary
    val surfaceColor = if (isAmoledBlack) Color.Black else MaterialTheme.colorScheme.background
    val cardColor = if (isAmoledBlack) Color(0xFF10141C) else MaterialTheme.colorScheme.surfaceContainer
    val highCardColor = if (isAmoledBlack) Color(0xFF161B26) else MaterialTheme.colorScheme.surfaceContainerHigh
    val cardBorder = BorderStroke(
        width = 1.dp,
        color = if (isAmoledBlack) Color(0x33FFFFFF) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
    )
    val textColor = MaterialTheme.colorScheme.onBackground
    val textSecondary = MaterialTheme.colorScheme.onSurfaceVariant

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(surfaceColor)
            .testTag("equalizer_screen")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            // 1. TOP APP BAR
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                FilledTonalIconButton(
                    onClick = {
                        haptic.performHaptic(HapticType.LIGHT_CLICK)
                        onBackClick()
                    },
                    shape = CircleShape,
                    colors = IconButtonDefaults.filledTonalIconButtonColors(
                        containerColor = highCardColor,
                        contentColor = textColor
                    ),
                    modifier = Modifier.testTag("equalizer_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back"
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Acoustic Equalizer",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = (-0.5).sp
                        ),
                        color = textColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "Material Expressive 3 Audio Engine",
                        style = MaterialTheme.typography.labelSmall,
                        color = textSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Quick Reset Action Button ("Flat")
                FilledTonalButton(
                    onClick = {
                        haptic.performHaptic(HapticType.DOUBLE_CLICK)
                        onResetToFlat()
                    },
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = highCardColor,
                        contentColor = primaryColor
                    ),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier.testTag("equalizer_reset_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.RestartAlt,
                        contentDescription = "Reset to Flat",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Flat",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }

            // SCROLLABLE CONTENT BODY
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 2. HERO MASTER PROCESSING CARD WITH LIVE SPLINE RESPONSE CURVE
                Card(
                    shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.cardColors(containerColor = cardColor),
                    border = cardBorder,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Header Row with Switch & Status Pill
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Tune,
                                        contentDescription = null,
                                        tint = primaryColor,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Hardware DSP",
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                        color = textColor
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Surface(
                                    shape = CircleShape,
                                    color = if (uiState.isEnabled) primaryColor.copy(alpha = 0.16f) else textSecondary.copy(alpha = 0.12f)
                                ) {
                                    Text(
                                        text = if (uiState.isEnabled) "ACTIVE • 32-BIT AUDIO FX" else "BYPASSED • PASS-THROUGH",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = if (uiState.isEnabled) primaryColor else textSecondary,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
                                    )
                                }
                            }

                            Switch(
                                checked = uiState.isEnabled,
                                onCheckedChange = {
                                    haptic.performHaptic(HapticType.LIGHT_CLICK)
                                    onToggleEqualizer(it)
                                },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = onPrimaryColor,
                                    checkedTrackColor = primaryColor,
                                    uncheckedThumbColor = textSecondary,
                                    uncheckedTrackColor = highCardColor
                                ),
                                modifier = Modifier.testTag("equalizer_power_switch")
                            )
                        }

                        // Real-time Dynamic Frequency Response Spline Curve Canvas
                        FrequencyResponseCurveCanvas(
                            bands = uiState.bands,
                            isEnabled = uiState.isEnabled,
                            primaryColor = primaryColor,
                            secondaryColor = secondaryColor,
                            gridColor = textSecondary.copy(alpha = 0.25f),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(130.dp)
                                .clip(RoundedCornerShape(20.dp))
                                .background(if (isAmoledBlack) Color(0xFF080B10) else highCardColor)
                        )
                    }
                }

                // 3. SOUND PRESETS CAROUSEL (EXPRESSIVE FILTER CHIPS)
                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = cardColor),
                    border = cardBorder,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = primaryColor,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "SOUND PRESETS",
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        letterSpacing = 1.2.sp,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = textSecondary
                                )
                            }

                            if (uiState.currentPresetIndex >= 0 && uiState.currentPresetIndex < uiState.presets.size) {
                                Text(
                                    text = uiState.presets[uiState.currentPresetIndex.toInt()],
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = primaryColor
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            itemsIndexed(uiState.presets) { index, presetName ->
                                val isSelected = uiState.currentPresetIndex == index.toShort()
                                FilterChip(
                                    selected = isSelected,
                                    onClick = {
                                        haptic.performHaptic(HapticType.LIGHT_CLICK)
                                        onPresetSelect(index.toShort())
                                    },
                                    label = {
                                        Text(
                                            text = presetName,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                        )
                                    },
                                    leadingIcon = {
                                        if (isSelected) {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = null,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        } else {
                                            Icon(
                                                imageVector = Icons.Default.MusicNote,
                                                contentDescription = null,
                                                modifier = Modifier.size(14.dp)
                                            )
                                        }
                                    },
                                    enabled = uiState.isEnabled,
                                    shape = RoundedCornerShape(16.dp),
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = primaryColor,
                                        selectedLabelColor = onPrimaryColor,
                                        selectedLeadingIconColor = onPrimaryColor,
                                        containerColor = highCardColor,
                                        labelColor = textColor
                                    ),
                                    border = FilterChipDefaults.filterChipBorder(
                                        enabled = uiState.isEnabled,
                                        selected = isSelected,
                                        borderColor = if (isSelected) primaryColor else cardBorder.brush?.let { primaryColor } ?: cardBorder.width.let { textSecondary.copy(alpha = 0.2f) }
                                    )
                                )
                            }
                        }
                    }
                }

                // 4. FREQUENCY BANDS CONSOLE (EXPRESSIVE TACTILE FADERS)
                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = cardColor),
                    border = cardBorder,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.GraphicEq,
                                    contentDescription = null,
                                    tint = primaryColor,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "FREQUENCY BANDS",
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        letterSpacing = 1.2.sp,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = textSecondary
                                )
                            }

                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = highCardColor
                            ) {
                                Text(
                                    text = "±12 dB RANGE",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = textSecondary,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                        }

                        if (uiState.bands.isEmpty()) {
                            Text(
                                text = "Default 5-band equalizer will be initialized on first audio playback.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = textSecondary,
                                modifier = Modifier.padding(vertical = 12.dp)
                            )
                        } else {
                            uiState.bands.forEach { band ->
                                ExpressiveBandFaderRow(
                                    band = band,
                                    isEnabled = uiState.isEnabled,
                                    primaryColor = primaryColor,
                                    highCardColor = highCardColor,
                                    textColor = textColor,
                                    textSecondary = textSecondary,
                                    onGainChange = { gainMb ->
                                        onBandGainChange(band.bandIndex, gainMb)
                                    }
                                )
                            }
                        }
                    }
                }

                // 5. AUDIO EFFECTS BENTO GRID (BASS BOOST & 3D SPATIAL VIRTUALIZER)
                Text(
                    text = "AUDIO ENHANCEMENT FX",
                    style = MaterialTheme.typography.labelMedium.copy(
                        letterSpacing = 1.2.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = textSecondary,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Bass Boost Card
                    Card(
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = cardColor),
                        border = cardBorder,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("bass_boost_card")
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = primaryColor.copy(alpha = 0.16f),
                                    modifier = Modifier.size(34.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Default.Waves,
                                            contentDescription = null,
                                            tint = primaryColor,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }

                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = highCardColor
                                ) {
                                    Text(
                                        text = "${uiState.bassBoostStrength / 10}%",
                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                        color = if (uiState.bassBoostStrength > 0) primaryColor else textSecondary,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }
                            }

                            Column {
                                Text(
                                    text = "Bass Boost",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = textColor
                                )
                                Text(
                                    text = "Sub-bass resonance",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = textSecondary,
                                    fontSize = 11.sp
                                )
                            }

                            Slider(
                                value = uiState.bassBoostStrength.toFloat(),
                                onValueChange = {
                                    haptic.performHaptic(HapticType.LIGHT_CLICK)
                                    onBassBoostChange(it.toInt().toShort())
                                },
                                valueRange = 0f..1000f,
                                enabled = uiState.isEnabled,
                                colors = SliderDefaults.colors(
                                    thumbColor = primaryColor,
                                    activeTrackColor = primaryColor,
                                    inactiveTrackColor = highCardColor
                                ),
                                modifier = Modifier.testTag("bass_boost_slider")
                            )
                        }
                    }

                    // 3D Spatial Virtualizer Card
                    Card(
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = cardColor),
                        border = cardBorder,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("virtualizer_card")
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = tertiaryColor.copy(alpha = 0.16f),
                                    modifier = Modifier.size(34.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Default.SurroundSound,
                                            contentDescription = null,
                                            tint = tertiaryColor,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }

                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = highCardColor
                                ) {
                                    Text(
                                        text = "${uiState.virtualizerStrength / 10}%",
                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                        color = if (uiState.virtualizerStrength > 0) tertiaryColor else textSecondary,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }
                            }

                            Column {
                                Text(
                                    text = "3D Virtualizer",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = textColor
                                )
                                Text(
                                    text = "Soundstage expansion",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = textSecondary,
                                    fontSize = 11.sp
                                )
                            }

                            Slider(
                                value = uiState.virtualizerStrength.toFloat(),
                                onValueChange = {
                                    haptic.performHaptic(HapticType.LIGHT_CLICK)
                                    onVirtualizerChange(it.toInt().toShort())
                                },
                                valueRange = 0f..1000f,
                                enabled = uiState.isEnabled,
                                colors = SliderDefaults.colors(
                                    thumbColor = tertiaryColor,
                                    activeTrackColor = tertiaryColor,
                                    inactiveTrackColor = highCardColor
                                ),
                                modifier = Modifier.testTag("virtualizer_slider")
                            )
                        }
                    }
                }

                // Environmental Reverb Studio Card
                val reverbPresets = listOf(
                    "Off" to 0.toShort(),
                    "Small Room" to 1.toShort(),
                    "Medium Room" to 2.toShort(),
                    "Large Room" to 3.toShort(),
                    "Medium Hall" to 4.toShort(),
                    "Large Hall" to 5.toShort(),
                    "Plate" to 6.toShort()
                )

                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = cardColor),
                    border = cardBorder,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("reverb_studio_card")
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    shape = CircleShape,
                                    color = secondaryColor.copy(alpha = 0.16f),
                                    modifier = Modifier.size(34.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Default.SurroundSound,
                                            contentDescription = null,
                                            tint = secondaryColor,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "Acoustic Environment & Reverb",
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                        color = textColor
                                    )
                                    Text(
                                        text = "Room reflection simulation",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = textSecondary,
                                        fontSize = 11.sp
                                    )
                                }
                            }

                            val activeReverbLabel = reverbPresets.firstOrNull { it.second == uiState.reverbPreset }?.first ?: "Off"
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (uiState.reverbPreset > 0 && uiState.isEnabled) secondaryColor.copy(alpha = 0.18f) else highCardColor
                            ) {
                                Text(
                                    text = activeReverbLabel,
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                    color = if (uiState.reverbPreset > 0 && uiState.isEnabled) secondaryColor else textSecondary,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }

                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            itemsIndexed(reverbPresets) { _, (name, presetVal) ->
                                val isSelected = uiState.reverbPreset == presetVal
                                FilterChip(
                                    selected = isSelected,
                                    onClick = {
                                        haptic.performHaptic(HapticType.MEDIUM_CLICK)
                                        onReverbPresetChange(presetVal)
                                    },
                                    label = {
                                        Text(
                                            text = name,
                                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold)
                                        )
                                    },
                                    enabled = uiState.isEnabled,
                                    shape = RoundedCornerShape(14.dp),
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = secondaryColor,
                                        selectedLabelColor = onPrimaryColor,
                                        containerColor = highCardColor,
                                        labelColor = textColor
                                    ),
                                    border = FilterChipDefaults.filterChipBorder(
                                        enabled = uiState.isEnabled,
                                        selected = isSelected,
                                        borderColor = if (isSelected) secondaryColor else textSecondary.copy(alpha = 0.2f)
                                    )
                                )
                            }
                        }
                    }
                }

                // Loudness Enhancer Card
                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = cardColor),
                    border = cardBorder,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("loudness_enhancer_card")
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    shape = CircleShape,
                                    color = primaryColor.copy(alpha = 0.16f),
                                    modifier = Modifier.size(34.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Default.VolumeUp,
                                            contentDescription = null,
                                            tint = primaryColor,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "Loudness Enhancer",
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                        color = textColor
                                    )
                                    Text(
                                        text = "Hardware dynamic gain boost",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = textSecondary,
                                        fontSize = 11.sp
                                    )
                                }
                            }

                            val gainDb = uiState.loudnessGainMb / 100
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (gainDb > 0 && uiState.isEnabled) primaryColor.copy(alpha = 0.18f) else highCardColor
                            ) {
                                Text(
                                    text = "+$gainDb dB",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                    color = if (gainDb > 0 && uiState.isEnabled) primaryColor else textSecondary,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }

                        Slider(
                            value = uiState.loudnessGainMb.toFloat(),
                            onValueChange = {
                                haptic.performHaptic(HapticType.LIGHT_CLICK)
                                onLoudnessGainChange(it.toInt())
                            },
                            valueRange = 0f..1200f,
                            steps = 11,
                            enabled = uiState.isEnabled,
                            colors = SliderDefaults.colors(
                                thumbColor = primaryColor,
                                activeTrackColor = primaryColor,
                                inactiveTrackColor = highCardColor
                            ),
                            modifier = Modifier.testTag("loudness_slider")
                        )
                    }
                }

                // 6. SYSTEM EQUALIZER INTEGRATION
                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = cardColor),
                    border = cardBorder,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = secondaryColor.copy(alpha = 0.15f),
                                modifier = Modifier.size(36.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.Tune,
                                        contentDescription = null,
                                        tint = secondaryColor,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "System Equalizer Panel",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = textColor
                                )
                                Text(
                                    text = "Bridge to Wavelet, Dolby Atmos, Dirac, or OEM Audio Tuner",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = textSecondary
                                )
                            }
                        }

                        FilledTonalButton(
                            onClick = {
                                haptic.performHaptic(HapticType.LIGHT_CLICK)
                                onOpenSystemEqualizer()
                            },
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.filledTonalButtonColors(
                                containerColor = highCardColor,
                                contentColor = textColor
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("open_system_equalizer_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.OpenInNew,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Open Device Sound Effects", fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(48.dp))
            }
        }
    }
}

/**
 * Dynamic Bezier Spline Frequency Response Curve.
 * Reacts continuously to band gains with smooth cubic curves and an expressive luminous gradient fill.
 */
@Composable
private fun FrequencyResponseCurveCanvas(
    bands: List<EqualizerBandState>,
    isEnabled: Boolean,
    primaryColor: Color,
    secondaryColor: Color,
    gridColor: Color,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        val midY = height / 2f

        // Draw Reference Grid Lines (0 dB Center Line, +12 dB, -12 dB)
        drawLine(
            color = gridColor,
            start = Offset(0f, midY),
            end = Offset(width, midY),
            strokeWidth = 1.dp.toPx(),
            cap = StrokeCap.Round
        )

        val quarterY = height * 0.25f
        val threeQuarterY = height * 0.75f
        drawLine(
            color = gridColor.copy(alpha = gridColor.alpha * 0.5f),
            start = Offset(0f, quarterY),
            end = Offset(width, quarterY),
            strokeWidth = 1.dp.toPx()
        )
        drawLine(
            color = gridColor.copy(alpha = gridColor.alpha * 0.5f),
            start = Offset(0f, threeQuarterY),
            end = Offset(width, threeQuarterY),
            strokeWidth = 1.dp.toPx()
        )

        if (bands.isEmpty() || !isEnabled) {
            // Flat baseline when disabled or empty
            val flatPath = Path().apply {
                moveTo(0f, midY)
                lineTo(width, midY)
            }
            drawPath(
                path = flatPath,
                color = primaryColor.copy(alpha = if (isEnabled) 0.8f else 0.35f),
                style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
            )
            return@Canvas
        }

        // Calculate control points based on band gains (-1500 to +1500 mB typically)
        val stepX = width / (bands.size + 1)
        val points = mutableListOf<Offset>()

        // Anchor at beginning
        val firstGainNorm = bands.first().currentGainMb.toFloat() / 1500f
        val firstY = midY - (firstGainNorm * (height * 0.42f)).coerceIn(-midY + 8f, midY - 8f)
        points.add(Offset(0f, firstY))

        bands.forEachIndexed { i, band ->
            val normGain = band.currentGainMb.toFloat() / 1500f
            val px = (i + 1) * stepX
            val py = midY - (normGain * (height * 0.42f)).coerceIn(-midY + 8f, midY - 8f)
            points.add(Offset(px, py))
        }

        // Anchor at end
        val lastGainNorm = bands.last().currentGainMb.toFloat() / 1500f
        val lastY = midY - (lastGainNorm * (height * 0.42f)).coerceIn(-midY + 8f, midY - 8f)
        points.add(Offset(width, lastY))

        // Build Smooth Cubic Bezier Spline Path
        val splinePath = Path().apply {
            moveTo(points[0].x, points[0].y)
            for (i in 0 until points.size - 1) {
                val p0 = points[i]
                val p1 = points[i + 1]
                val cX = (p0.x + p1.x) / 2f
                cubicTo(cX, p0.y, cX, p1.y, p1.x, p1.y)
            }
        }

        // Fill under the curve with luminous gradient
        val fillPath = Path().apply {
            addPath(splinePath)
            lineTo(width, height)
            lineTo(0f, height)
            close()
        }

        drawPath(
            path = fillPath,
            brush = Brush.verticalGradient(
                colors = listOf(
                    primaryColor.copy(alpha = 0.35f),
                    secondaryColor.copy(alpha = 0.12f),
                    Color.Transparent
                ),
                startY = 0f,
                endY = height
            )
        )

        // Draw Spline Stroke
        drawPath(
            path = splinePath,
            brush = Brush.horizontalGradient(
                colors = listOf(primaryColor, secondaryColor, primaryColor)
            ),
            style = Stroke(width = 3.5.dp.toPx(), cap = StrokeCap.Round)
        )

        // Draw Band Node Circles
        for (i in 1 until points.size - 1) {
            val pt = points[i]
            // Outer glow ring
            drawCircle(
                color = primaryColor.copy(alpha = 0.28f),
                radius = 8.dp.toPx(),
                center = pt
            )
            // Inner solid core
            drawCircle(
                color = Color.White,
                radius = 4.dp.toPx(),
                center = pt
            )
        }
    }
}

/**
 * Expressive tactile Band Fader Row.
 */
@Composable
private fun ExpressiveBandFaderRow(
    band: EqualizerBandState,
    isEnabled: Boolean,
    primaryColor: Color,
    highCardColor: Color,
    textColor: Color,
    textSecondary: Color,
    onGainChange: (Short) -> Unit
) {
    val haptic = rememberHapticController()
    val gainDb = band.currentGainMb / 100
    val freqLabel = if (band.centerFreqHz >= 1000) {
        val khz = band.centerFreqHz / 1000f
        if (khz % 1f == 0f) "${khz.toInt()} kHz" else "%.1f kHz".format(khz)
    } else {
        "${band.centerFreqHz} Hz"
    }

    val bandTitle = when (band.bandIndex.toInt()) {
        0 -> "SUB-BASS"
        1 -> "BASS"
        2 -> "MIDRANGE"
        3 -> "PRESENCE"
        4 -> "BRILLIANCE"
        else -> "BAND ${band.bandIndex + 1}"
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = freqLabel,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.ExtraBold),
                    color = textColor
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "•  $bandTitle",
                    style = MaterialTheme.typography.labelSmall,
                    color = textSecondary
                )
            }

            Surface(
                shape = RoundedCornerShape(10.dp),
                color = if (gainDb != 0 && isEnabled) primaryColor.copy(alpha = 0.18f) else highCardColor
            ) {
                Text(
                    text = if (gainDb > 0) "+$gainDb dB" else "$gainDb dB",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = if (gainDb != 0 && isEnabled) primaryColor else textSecondary,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
                )
            }
        }

        Slider(
            value = band.currentGainMb.toFloat(),
            onValueChange = {
                onGainChange(it.toInt().toShort())
            },
            valueRange = band.minGainMb.toFloat()..band.maxGainMb.toFloat(),
            enabled = isEnabled,
            colors = SliderDefaults.colors(
                thumbColor = primaryColor,
                activeTrackColor = primaryColor,
                inactiveTrackColor = highCardColor
            ),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("eq_band_${band.bandIndex}")
        )
    }
}
