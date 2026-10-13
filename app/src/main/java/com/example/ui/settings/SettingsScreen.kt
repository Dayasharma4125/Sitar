package com.example.ui.settings

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Equalizer
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.Cast
import androidx.compose.material.icons.filled.CastConnected
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.History
import com.example.cast.CastState
import com.example.cast.CastStreamQuality
import com.example.cast.ChromecastUiState
import com.example.data.BackupSummary
import com.example.ui.theme.OutfitFontFamily
import com.example.ui.theme.PlayfairDisplayFontFamily
import com.example.ui.theme.SpaceGroteskFontFamily
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.audio.AudioEngineStatus
import com.example.audio.AudioRoutingMode
import com.example.model.AppColorPalette
import com.example.model.AppSettings
import com.example.model.AppThemeMode
import com.example.model.M3ArtworkShape
import com.example.model.M3ArtworkSize
import com.example.model.M3BackgroundStyle
import com.example.model.M3ExpressivePlayerConfig
import com.example.model.M3HeroButtonStyle
import com.example.model.M3ScrubberStyle
import com.example.model.PlayerLayoutType
import com.example.model.SleepTimerState
import androidx.compose.material.icons.filled.DashboardCustomize
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults

import com.example.model.LyricsScreenConfig
import androidx.compose.material.icons.filled.Lyrics

/**
 * Categorization tabs for Settings.
 * "All Settings" shows every single feature sequentially.
 * Individual tabs allow quick filtering.
 */
enum class SettingsCategoryTab(val label: String, val icon: ImageVector) {
    ALL("All Settings", Icons.Default.Tune),
    APPEARANCE("Appearance", Icons.Default.Palette),
    AUDIO("Audio Engine", Icons.Default.GraphicEq),
    CAST("Chromecast", Icons.Default.Cast),
    BACKUP("Backup & Restore", Icons.Default.Backup),
    LIBRARY("Library & Folders", Icons.Default.Folder),
    TOOLS("Playback & Tools", Icons.Default.AutoAwesome)
}

/**
 * Complete, feature-rich Settings Screen.
 * Includes all audio, theme, glassmorphic visual, library filter, and playback features.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    settings: AppSettings,
    onThemeModeChange: (AppThemeMode) -> Unit = {},
    onColorPaletteChange: (AppColorPalette) -> Unit = {},
    onUseDynamicColorsChange: (Boolean) -> Unit = {},
    onDarkThemeChange: (Boolean) -> Unit = {},
    onAmoledBlackChange: (Boolean) -> Unit = {},
    onDynamicColorChange: (Boolean) -> Unit = {},
    onBlurIntensityChange: (Float) -> Unit = {},
    onPlayerLayoutTypeChange: (PlayerLayoutType) -> Unit = {},
    onM3ExpressiveConfigChange: (M3ExpressivePlayerConfig) -> Unit = {},
    onLyricsConfigChange: (LyricsScreenConfig) -> Unit = {},
    onNotificationPatternChange: (com.example.model.NotificationPattern) -> Unit = {},
    onSilentNotificationChange: (Boolean) -> Unit = {},
    onForceHighRefreshRateChange: (Boolean) -> Unit = {},
    onMinDurationChange: (Int) -> Unit = {},
    onAddBlacklistFolder: (String) -> Unit = {},
    onRemoveBlacklistFolder: (String) -> Unit = {},
    onAddWhitelistFolder: (String) -> Unit = {},
    onRemoveWhitelistFolder: (String) -> Unit = {},
    onUseWhitelistOnlyChange: (Boolean) -> Unit = {},
    onGaplessPlaybackChange: (Boolean) -> Unit = {},
    onCrossfadeEnabledChange: (Boolean) -> Unit = {},
    onCrossfadeDurationSecChange: (Int) -> Unit = {},
    onAlwaysShuffleChange: (Boolean) -> Unit = {},
    onShowAudioDetailsChange: (Boolean) -> Unit = {},
    onLockScreenPlayerChange: (Boolean) -> Unit = {},
    onMusicDynamicThemingChange: (Boolean) -> Unit = {},
    onChangeFontsWithMusicChange: (Boolean) -> Unit = {},
    onDefaultFontFamilyChange: (String) -> Unit = {},
    onBackdropBlurEnabledChange: (Boolean) -> Unit = {},
    onGlassmorphicEffectEnabledChange: (Boolean) -> Unit = {},
    onSnowballEffectEnabledChange: (Boolean) -> Unit = {},
    onLiveMovingGradientChange: (Boolean) -> Unit = {},
    onHomeScreenEffectsChange: (Boolean) -> Unit = {},
    onHomeScreenEffectStyleChange: (String) -> Unit = {},
    onPlaybackVisualizerEnabledChange: (Boolean) -> Unit = {},
    onWaveformVisualizerStyleChange: (String) -> Unit = {},
    onAodEnabledChange: (Boolean) -> Unit = {},
    onAodLayoutTypeChange: (com.example.model.AodLayoutType) -> Unit = {},
    onAodKeepScreenAwakeChange: (Boolean) -> Unit = {},
    onAodBurnInProtectionChange: (Boolean) -> Unit = {},
    onAodBrightnessDimChange: (Float) -> Unit = {},
    onPreviewAodClick: () -> Unit = {},
    onCacheImagesClick: () -> Unit = {},
    isCachingImages: Boolean = false,
    cachingProgress: String? = null,
    onOpenSystemEqualizer: () -> Unit = {},
    onOpenBuiltInEqualizer: () -> Unit = {},
    onUseSystemEqualizerChange: (Boolean) -> Unit = {},
    onRescanClick: () -> Unit = {},
    sleepTimerState: SleepTimerState = SleepTimerState(),
    onOpenSleepTimer: () -> Unit = {},
    chromecastUiState: ChromecastUiState = ChromecastUiState(),
    onOpenChromecast: () -> Unit = {},
    onDisconnectChromecast: () -> Unit = {},
    onSetCastQuality: (CastStreamQuality) -> Unit = {},
    onScanCastDevices: () -> Unit = {},
    localSnapshotSummary: BackupSummary? = null,
    backupStatusMessage: String? = null,
    onClearBackupStatus: () -> Unit = {},
    onExportBackupToUri: (Uri) -> Unit = {},
    onRestoreBackupFromUri: (Uri, Boolean) -> Unit = { _, _ -> },
    onCreateLocalSnapshot: () -> Unit = {},
    onRestoreLocalSnapshot: (Boolean) -> Unit = {},
    onLoadSnapshotInfo: () -> Unit = {},
    audioEngineStatus: AudioEngineStatus? = null,
    onAudioRoutingModeChange: (AudioRoutingMode) -> Unit = {},
    hasStoragePermission: Boolean = false,
    hasMicrophonePermission: Boolean = false,
    hasNearbyDevicesPermission: Boolean = false,
    onRequestStoragePermission: () -> Unit = {},
    onRequestMicrophonePermission: () -> Unit = {},
    onRequestNearbyDevicesPermission: () -> Unit = {},
    onOpenTracker: () -> Unit = {},
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf(SettingsCategoryTab.ALL) }
    var activeSubPage by remember { mutableStateOf<String?>(null) }
    var newBlacklistFolderInput by remember { mutableStateOf("") }
    var newWhitelistFolderInput by remember { mutableStateOf("") }

    if (activeSubPage == "m3_studio") {
        M3ExpressiveStudioScreen(
            settings = settings,
            onConfigChange = onM3ExpressiveConfigChange,
            onBackClick = { activeSubPage = null }
        )
        return
    }

    if (activeSubPage == "lyrics_studio") {
        LyricsCustomizerStudioScreen(
            settings = settings,
            onConfigChange = onLyricsConfigChange,
            onBackClick = { activeSubPage = null }
        )
        return
    }

    val isAmoled = settings.themeMode == AppThemeMode.PURE_BLACK || settings.amoledBlack
    val backgroundColor = if (isAmoled) Color(0xFF000000) else MaterialTheme.colorScheme.background
    val primaryColor = MaterialTheme.colorScheme.primary
    val onPrimaryColor = MaterialTheme.colorScheme.onPrimary
    val textColor = MaterialTheme.colorScheme.onBackground
    val textSecondary = MaterialTheme.colorScheme.onSurfaceVariant
    val cardColor = if (isAmoled) Color(0xFF10141C) else MaterialTheme.colorScheme.surfaceContainer
    val cardBorderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
    val pillBackground = if (isAmoled) Color(0xFF181B22) else MaterialTheme.colorScheme.surfaceContainerHighest
    val context = LocalContext.current

    // SAF Directory & File Launchers
    val blacklistFolderPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocumentTree()
    ) { uri: Uri? ->
        uri?.let {
            val folder = resolveFolderName(it)
            if (folder.isNotBlank()) {
                onAddBlacklistFolder(folder)
            }
        }
    }

    val blacklistFilePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenMultipleDocuments()
    ) { uris: List<Uri>? ->
        uris?.forEach { uri ->
            val fileName = resolveDisplayName(context, uri)
            if (fileName.isNotBlank()) {
                onAddBlacklistFolder(fileName)
            }
        }
    }

    val whitelistFolderPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocumentTree()
    ) { uri: Uri? ->
        uri?.let {
            val folder = resolveFolderName(it)
            if (folder.isNotBlank()) {
                onAddWhitelistFolder(folder)
            }
        }
    }

    val whitelistFilePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenMultipleDocuments()
    ) { uris: List<Uri>? ->
        uris?.forEach { uri ->
            val fileName = resolveDisplayName(context, uri)
            if (fileName.isNotBlank()) {
                onAddWhitelistFolder(fileName)
            }
        }
    }

    // Backup & Restore State & Launchers
    var pendingRestoreUri by remember { mutableStateOf<Uri?>(null) }
    var showRestoreConfirmDialog by remember { mutableStateOf(false) }
    var showLocalRestoreConfirmDialog by remember { mutableStateOf(false) }
    var restoreMergeMode by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        onLoadSnapshotInfo()
    }

    val createBackupLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri: Uri? ->
        uri?.let { onExportBackupToUri(it) }
    }

    val openBackupLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            pendingRestoreUri = uri
            showRestoreConfirmDialog = true
        }
    }

    // Restore Backup File Confirmation Dialog
    if (showRestoreConfirmDialog && pendingRestoreUri != null) {
        AlertDialog(
            onDismissRequest = {
                showRestoreConfirmDialog = false
                pendingRestoreUri = null
            },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.FileUpload,
                        contentDescription = null,
                        tint = primaryColor,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Restore Music Data",
                        fontWeight = FontWeight.Bold,
                        color = textColor
                    )
                }
            },
            text = {
                Column {
                    Text(
                        text = "This will restore your listening metrics, top-played history, favorite songs, custom synced lyrics, and settings profile.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = textSecondary
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Surface(
                        color = primaryColor.copy(alpha = 0.12f),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { restoreMergeMode = !restoreMergeMode }
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = restoreMergeMode,
                                onCheckedChange = { restoreMergeMode = it }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "Merge with Current Data",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                    color = textColor
                                )
                                Text(
                                    text = if (restoreMergeMode) "Combines play counts & favorites safely" else "Replaces all current stats & favorites",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = textSecondary
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val uri = pendingRestoreUri
                        showRestoreConfirmDialog = false
                        pendingRestoreUri = null
                        if (uri != null) {
                            onRestoreBackupFromUri(uri, restoreMergeMode)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = primaryColor)
                ) {
                    Text("Restore Now", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showRestoreConfirmDialog = false
                        pendingRestoreUri = null
                    }
                ) {
                    Text("Cancel", color = textSecondary)
                }
            }
        )
    }

    // Restore Local Snapshot Dialog
    if (showLocalRestoreConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showLocalRestoreConfirmDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.History,
                        contentDescription = null,
                        tint = primaryColor,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Restore Local Snapshot",
                        fontWeight = FontWeight.Bold,
                        color = textColor
                    )
                }
            },
            text = {
                Column {
                    val info = localSnapshotSummary
                    if (info != null) {
                        Text(
                            text = "Restore snapshot from ${info.formattedDate} containing ${info.statsCount} listening records, ${info.favoritesCount} favorites, and ${info.lyricsCount} custom lyrics?",
                            style = MaterialTheme.typography.bodyMedium,
                            color = textColor
                        )
                    } else {
                        Text(
                            text = "Restore the latest saved internal snapshot of your listening stats and settings?",
                            style = MaterialTheme.typography.bodyMedium,
                            color = textSecondary
                        )
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                    Surface(
                        color = primaryColor.copy(alpha = 0.12f),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { restoreMergeMode = !restoreMergeMode }
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = restoreMergeMode,
                                onCheckedChange = { restoreMergeMode = it }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "Merge with Current Data",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                    color = textColor
                                )
                                Text(
                                    text = if (restoreMergeMode) "Combines play counts & favorites safely" else "Replaces all current stats & favorites",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = textSecondary
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showLocalRestoreConfirmDialog = false
                        onRestoreLocalSnapshot(restoreMergeMode)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = primaryColor)
                ) {
                    Text("Restore Snapshot", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLocalRestoreConfirmDialog = false }) {
                    Text("Cancel", color = textSecondary)
                }
            }
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(backgroundColor)
            .statusBarsPadding()
    ) {
        // TOP NAVIGATION BAR
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBackClick,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(pillBackground)
                    .testTag("settings_back_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = textColor
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column {
                Text(
                    text = "SETTINGS & PREFERENCES",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = primaryColor,
                    letterSpacing = 1.2.sp
                )
                Text(
                    text = "Customization & Engine",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = textColor
                )
            }
        }

        // CATEGORY TAB ROW (Quick filter pill list)
        LazyRow(
            modifier = Modifier.padding(vertical = 6.dp),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(SettingsCategoryTab.entries.toTypedArray()) { tab ->
                val isSelected = selectedTab == tab
                Surface(
                    shape = RoundedCornerShape(50),
                    color = if (isSelected) primaryColor else pillBackground,
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .clickable { selectedTab = tab }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = tab.icon,
                            contentDescription = null,
                            tint = if (isSelected) onPrimaryColor else textSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = tab.label,
                            color = if (isSelected) onPrimaryColor else textColor,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 12.5.sp
                        )
                    }
                }
            }
        }

        HorizontalDivider(
            color = cardBorderColor,
            modifier = Modifier.padding(top = 6.dp, bottom = 12.dp)
        )

        // CONTENT AREA (Displays all settings or filtered by selected tab)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState())
                .padding(bottom = 120.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // TAB / SECTION 1: APPEARANCE & STYLING
            if (selectedTab == SettingsCategoryTab.ALL || selectedTab == SettingsCategoryTab.APPEARANCE) {
                // Now Playing Screen Style
                SettingsCard(
                    title = "Now Playing Screen Style",
                    icon = Icons.Default.MusicNote,
                    cardColor = cardColor,
                    cardBorderColor = cardBorderColor,
                    primaryColor = primaryColor
                ) {
                    Text(
                        text = "Choose your full-screen player layout and aesthetic:",
                        style = MaterialTheme.typography.bodySmall,
                        color = textSecondary
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    PlayerLayoutType.entries.forEach { layoutType ->
                        val isSelected = settings.playerLayoutType == layoutType
                        val (title, description) = when (layoutType) {
                            PlayerLayoutType.M3_EXPRESSIVE -> "Material 3 Expressive Studio" to "Google's flagship expressive design: interactive shapes, motion physics, fluid wavy slider & dynamic sound visualizer"
                            PlayerLayoutType.MATERIAL_YOU_EXPRESSIVE -> "Material You (Card Split)" to "Dynamic cards, active device capsule, in-place lyrics & volume slider"
                            PlayerLayoutType.APPLE_MUSIC -> "Glassmorphic (Apple Music Style)" to "Full-bleed dynamic blur mesh, frosted glass panels, synced lyrics & volume slider"
                            PlayerLayoutType.CIRCULAR_ORBIT -> "Circular Orbit (Vinyl Turntable)" to "Spinning vinyl record disc with circular playback progress ring and retro deck"
                            PlayerLayoutType.WAVEFORM_CYBER_STUDIO -> "Cyber Waveform Studio" to "Live audio waveform visualizer inside scrubber, cyber telemetry badges & neon accents"
                            PlayerLayoutType.VINYL_TURNTABLE -> "Hi-Fi Vinyl Turntable" to "Realistic 33⅓ RPM rotating vinyl record with moving tonearm stylus & analog plinth deck"
                        }

                        Surface(
                            onClick = { onPlayerLayoutTypeChange(layoutType) },
                            shape = RoundedCornerShape(16.dp),
                            color = if (isSelected) primaryColor.copy(alpha = 0.15f) else pillBackground,
                            border = BorderStroke(
                                width = if (isSelected) 1.5.dp else 1.dp,
                                color = if (isSelected) primaryColor else cardBorderColor
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = isSelected,
                                    onClick = { onPlayerLayoutTypeChange(layoutType) },
                                    colors = RadioButtonDefaults.colors(
                                        selectedColor = primaryColor,
                                        unselectedColor = textSecondary
                                    )
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = title,
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                        color = if (isSelected) primaryColor else textColor
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = description,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = textSecondary,
                                        fontSize = 11.5.sp,
                                        lineHeight = 15.sp
                                    )
                                }
                            }
                        }
                    }
                }

                // DEDICATED MATERIAL 3 EXPRESSIVE STUDIO PAGE ENTRY BANNER
                SettingsCard(
                    title = "Material 3 Expressive Studio",
                    icon = Icons.Default.DashboardCustomize,
                    cardColor = cardColor,
                    cardBorderColor = primaryColor.copy(alpha = 0.45f),
                    primaryColor = primaryColor
                ) {
                    Text(
                        text = "Fine-tune interactive shapes, fluid motion physics profiles, live audio visualizers, and screen modules on a dedicated designer page:",
                        style = MaterialTheme.typography.bodySmall,
                        color = textSecondary
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Surface(
                        onClick = {
                            if (settings.playerLayoutType != PlayerLayoutType.M3_EXPRESSIVE) {
                                onPlayerLayoutTypeChange(PlayerLayoutType.M3_EXPRESSIVE)
                            }
                            activeSubPage = "m3_studio"
                        },
                        shape = RoundedCornerShape(20.dp),
                        color = primaryColor.copy(alpha = 0.12f),
                        border = BorderStroke(1.5.dp, primaryColor.copy(alpha = 0.4f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.AutoAwesome,
                                        contentDescription = null,
                                        tint = primaryColor,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Open Expressive Studio",
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                        color = primaryColor
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Current: ${settings.m3ExpressiveConfig.artworkShape.displayName} • ${settings.m3ExpressiveConfig.scrubberStyle.displayName} • ${settings.m3ExpressiveConfig.motionProfile.displayName}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = textColor.copy(alpha = 0.85f),
                                    fontSize = 11.5.sp
                                )
                            }

                            FilledTonalButton(
                                onClick = {
                                    if (settings.playerLayoutType != PlayerLayoutType.M3_EXPRESSIVE) {
                                        onPlayerLayoutTypeChange(PlayerLayoutType.M3_EXPRESSIVE)
                                    }
                                    activeSubPage = "m3_studio"
                                },
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.filledTonalButtonColors(
                                    containerColor = primaryColor,
                                    contentColor = Color.White
                                )
                            ) {
                                Text(
                                    text = "Launch",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                                )
                            }
                        }
                    }
                }

                // DEDICATED FULL-SCREEN LYRICS & TYPOGRAPHY STUDIO BANNER
                SettingsCard(
                    title = "Lyrics & Typography Studio",
                    icon = Icons.Default.Lyrics,
                    cardColor = cardColor,
                    cardBorderColor = primaryColor.copy(alpha = 0.45f),
                    primaryColor = primaryColor
                ) {
                    Text(
                        text = "Customize full-screen lyrics layout, contrast (80% default), text size, alignment, backdrop styles, and karaoke animations:",
                        style = MaterialTheme.typography.bodySmall,
                        color = textSecondary
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Surface(
                        onClick = {
                            activeSubPage = "lyrics_studio"
                        },
                        shape = RoundedCornerShape(20.dp),
                        color = primaryColor.copy(alpha = 0.12f),
                        border = BorderStroke(1.5.dp, primaryColor.copy(alpha = 0.4f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Lyrics,
                                        contentDescription = null,
                                        tint = primaryColor,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Open Lyrics Customizer",
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                        color = primaryColor
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Current: ${settings.lyricsConfig.textSize.displayName} • ${settings.lyricsConfig.textAlignment.displayName} • ${(settings.lyricsConfig.inactiveTextContrast * 100).toInt()}% Contrast • ${settings.lyricsConfig.backdropStyle.displayName}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = textColor.copy(alpha = 0.85f),
                                    fontSize = 11.5.sp
                                )
                            }

                            FilledTonalButton(
                                onClick = {
                                    activeSubPage = "lyrics_studio"
                                },
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.filledTonalButtonColors(
                                    containerColor = primaryColor,
                                    contentColor = Color.White
                                )
                            ) {
                                Text(
                                    text = "Launch",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                                )
                            }
                        }
                    }
                }

                // HOME SCREEN ATMOSPHERE & AMBIENT EFFECTS
                SettingsCard(
                    title = "Home Screen Visual Atmosphere & Effects",
                    icon = Icons.Default.AutoAwesome,
                    cardColor = cardColor,
                    cardBorderColor = cardBorderColor,
                    primaryColor = primaryColor
                ) {
                    Text(
                        text = "Dynamic ambient lighting, glowing radial aura spheres, and particle shimmer across the main home library canvas:",
                        style = MaterialTheme.typography.bodySmall,
                        color = textSecondary
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    // Master Toggle Switch for Home Screen Effects
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(pillBackground)
                            .clickable { onHomeScreenEffectsChange(!settings.homeScreenEffectsEnabled) }
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Home Screen Ambient Effects",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                color = textColor
                            )
                            Text(
                                text = if (settings.homeScreenEffectsEnabled) "Active • Dynamic aura & music color reflections enabled" else "Disabled • Static flat background",
                                style = MaterialTheme.typography.bodySmall,
                                color = if (settings.homeScreenEffectsEnabled) primaryColor else textSecondary,
                                fontSize = 11.5.sp
                            )
                        }
                        Switch(
                            checked = settings.homeScreenEffectsEnabled,
                            onCheckedChange = onHomeScreenEffectsChange,
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = primaryColor
                            )
                        )
                    }

                    if (settings.homeScreenEffectsEnabled) {
                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = "Effect Style & Particle Physics:",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = primaryColor
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        val effectStyles = listOf(
                            Triple("AMBIENT_AURA", "Ambient Radial Aura", "Harmonic moving glow spheres with dynamic artwork colors"),
                            Triple("LUMINOUS_SHIMMER", "Luminous Particle Shimmer", "Gleaming orbital floating starlets & ambient mesh"),
                            Triple("FLUID_GRADIENT", "Fluid Soft Gradient", "Gentle sinusoidal breathing background mesh")
                        )

                        effectStyles.forEach { (styleKey, styleTitle, styleDesc) ->
                            val isSelected = settings.homeScreenEffectStyle == styleKey
                            Surface(
                                onClick = { onHomeScreenEffectStyleChange(styleKey) },
                                shape = RoundedCornerShape(14.dp),
                                color = if (isSelected) primaryColor.copy(alpha = 0.15f) else pillBackground,
                                border = BorderStroke(
                                    width = if (isSelected) 1.5.dp else 1.dp,
                                    color = if (isSelected) primaryColor else cardBorderColor
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    RadioButton(
                                        selected = isSelected,
                                        onClick = { onHomeScreenEffectStyleChange(styleKey) },
                                        colors = RadioButtonDefaults.colors(
                                            selectedColor = primaryColor,
                                            unselectedColor = textSecondary
                                        )
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = styleTitle,
                                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                            color = if (isSelected) primaryColor else textColor
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = styleDesc,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = textSecondary,
                                            fontSize = 11.5.sp,
                                            lineHeight = 15.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // ALWAYS ON DISPLAY (AOD) MUSIC MODE
                SettingsCard(
                    title = "Always-On Display (AOD) Music Mode",
                    icon = Icons.Default.Bedtime,
                    cardColor = cardColor,
                    cardBorderColor = cardBorderColor,
                    primaryColor = primaryColor
                ) {
                    Text(
                        text = "AMOLED battery-saving ambient display while music is playing:",
                        style = MaterialTheme.typography.bodySmall,
                        color = textSecondary
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    // Main AOD Master Toggle Switch
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(pillBackground)
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Enable AOD Screen",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                color = textColor
                            )
                            Text(
                                text = "Displays pure OLED black ambient screen with music telemetry & clock",
                                style = MaterialTheme.typography.bodySmall,
                                color = textSecondary,
                                fontSize = 11.5.sp
                            )
                        }
                        Switch(
                            checked = settings.aodEnabled,
                            onCheckedChange = onAodEnabledChange,
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = primaryColor
                            )
                        )
                    }

                    if (settings.aodEnabled) {
                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = "Choose AOD Layout Theme:",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = primaryColor
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        com.example.model.AodLayoutType.entries.forEach { aodType ->
                            val isSelected = settings.aodLayoutType == aodType
                            val (title, description) = when (aodType) {
                                com.example.model.AodLayoutType.MINIMAL_OLED_CLOCK -> "Minimal OLED Clock & Track" to "Ultra-efficient deep black geometric clock with track info and subtle progress line"
                                com.example.model.AodLayoutType.VINYL_GROOVE_ARC -> "Vinyl Arc & Needle Groove" to "Minimal turntable ring with glowing cyan stylus arc and track duration"
                                com.example.model.AodLayoutType.CYBER_MATRIX_HUD -> "Cyber Matrix HUD" to "Futuristic neon matrix digital HUD with live frequency meters and telemetry"
                                com.example.model.AodLayoutType.CAPSULE_PILL_GLOW -> "Minimal Capsule & Progress Pill" to "Sleek purple glowing progress capsule with elapsed time and tactile glyphs"
                            }

                            Surface(
                                onClick = { onAodLayoutTypeChange(aodType) },
                                shape = RoundedCornerShape(14.dp),
                                color = if (isSelected) primaryColor.copy(alpha = 0.15f) else pillBackground,
                                border = BorderStroke(
                                    width = if (isSelected) 1.5.dp else 1.dp,
                                    color = if (isSelected) primaryColor else cardBorderColor
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    RadioButton(
                                        selected = isSelected,
                                        onClick = { onAodLayoutTypeChange(aodType) },
                                        colors = RadioButtonDefaults.colors(
                                            selectedColor = primaryColor,
                                            unselectedColor = textSecondary
                                        )
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = title,
                                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                            color = if (isSelected) primaryColor else textColor
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = description,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = textSecondary,
                                            fontSize = 11.5.sp,
                                            lineHeight = 15.sp
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // AOD Advanced Controls: Burn-in protection, Keep Screen Awake, and Quick Preview
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(pillBackground)
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "OLED Burn-in Pixel Shifter",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                    color = textColor
                                )
                                Text(
                                    text = "Subtly moves pixel coordinates to prevent AMOLED screen retention",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = textSecondary,
                                    fontSize = 11.sp
                                )
                            }
                            Switch(
                                checked = settings.aodBurnInProtection,
                                onCheckedChange = onAodBurnInProtectionChange,
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = primaryColor
                                )
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(pillBackground)
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Keep Screen Awake (AOD)",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                    color = textColor
                                )
                                Text(
                                    text = "Keeps the AOD screen active continuously while playback continues",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = textSecondary,
                                    fontSize = 11.sp
                                )
                            }
                            Switch(
                                checked = settings.aodKeepScreenAwake,
                                onCheckedChange = onAodKeepScreenAwakeChange,
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = primaryColor
                                )
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        FilledTonalButton(
                            onClick = onPreviewAodClick,
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.filledTonalButtonColors(
                                containerColor = primaryColor.copy(alpha = 0.2f),
                                contentColor = primaryColor
                            )
                        ) {
                            Icon(Icons.Default.Bedtime, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Launch / Test AOD Screen Now", fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // Theme Mode
                SettingsCard(
                    title = "Theme Mode",
                    icon = Icons.Default.Palette,
                    cardColor = cardColor,
                    cardBorderColor = cardBorderColor,
                    primaryColor = primaryColor
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        AppThemeMode.entries.forEach { mode ->
                            val isSelected = settings.themeMode == mode
                            Surface(
                                onClick = { onThemeModeChange(mode) },
                                shape = RoundedCornerShape(12.dp),
                                color = if (isSelected) primaryColor else pillBackground,
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    modifier = Modifier.padding(vertical = 10.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = when (mode) {
                                            AppThemeMode.SYSTEM -> "System"
                                            AppThemeMode.LIGHT -> "Light"
                                            AppThemeMode.DARK -> "Dark"
                                            AppThemeMode.PURE_BLACK -> "OLED"
                                        },
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = if (isSelected) onPrimaryColor else textColor
                                    )
                                }
                            }
                        }
                    }
                }

                // All 9 App Accent Palettes
                SettingsCard(
                    title = "App Accent Palette (9 Themes)",
                    icon = Icons.Default.Palette,
                    cardColor = cardColor,
                    cardBorderColor = cardBorderColor,
                    primaryColor = primaryColor
                ) {
                    Text(
                        text = "Select an expressive color theme for the entire app:",
                        style = MaterialTheme.typography.bodySmall,
                        color = textSecondary
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    AppColorPalette.entries.chunked(3).forEach { rowPalettes ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            rowPalettes.forEach { palette ->
                                val isSelected = settings.colorPalette == palette
                                val pColor = Color(palette.primaryHex)
                                val sColor = Color(palette.secondaryHex)

                                Surface(
                                    onClick = { onColorPaletteChange(palette) },
                                    shape = RoundedCornerShape(14.dp),
                                    color = if (isSelected) primaryColor.copy(alpha = 0.18f) else pillBackground,
                                    border = BorderStroke(
                                        width = if (isSelected) 1.5.dp else 1.dp,
                                        color = if (isSelected) primaryColor else cardBorderColor
                                    ),
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(vertical = 4.dp)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 10.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(12.dp)
                                                    .clip(CircleShape)
                                                    .background(pColor)
                                            )
                                            Box(
                                                modifier = Modifier
                                                    .size(12.dp)
                                                    .clip(CircleShape)
                                                    .background(sColor)
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(
                                            text = palette.displayName,
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                            ),
                                            color = if (isSelected) primaryColor else textColor,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            fontSize = 10.5.sp
                                        )
                                    }
                                }
                            }
                            if (rowPalettes.size < 3) {
                                repeat(3 - rowPalettes.size) {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    SettingsSwitchRow(
                        title = "Dynamic Colors (Material You)",
                        subtitle = "Sync theme with Android 12+ wallpaper palette",
                        checked = settings.useDynamicColors,
                        onCheckedChange = onUseDynamicColorsChange,
                        textColor = textColor,
                        textSecondary = textSecondary,
                        primaryColor = primaryColor
                    )

                    SettingsSwitchRow(
                        title = "Music-Adaptive Theming",
                        subtitle = "Dynamically extract dominant palette from current album artwork",
                        checked = settings.musicDynamicTheming,
                        onCheckedChange = onMusicDynamicThemingChange,
                        textColor = textColor,
                        textSecondary = textSecondary,
                        primaryColor = primaryColor
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    SettingsSwitchRow(
                        title = "Change Fonts with Music",
                        subtitle = "Dynamically adapt typography (Serif, Cyber Monospace, Modern Sans) based on music genre and vibe",
                        checked = settings.changeFontsWithMusic,
                        onCheckedChange = onChangeFontsWithMusicChange,
                        textColor = textColor,
                        textSecondary = textSecondary,
                        primaryColor = primaryColor
                    )

                    if (settings.changeFontsWithMusic) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = primaryColor.copy(alpha = 0.08f),
                            border = BorderStroke(1.dp, primaryColor.copy(alpha = 0.2f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = "Dynamic Font Adapters Active",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                    color = primaryColor
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text(
                                            text = "🎻 Classical / Acoustic",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = textSecondary
                                        )
                                        Text(
                                            text = "Playfair Serif",
                                            fontFamily = PlayfairDisplayFontFamily,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = textColor
                                        )
                                    }
                                    Column {
                                        Text(
                                            text = "⚡ Cyber / Synth / EDM",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = textSecondary
                                        )
                                        Text(
                                            text = "Space Grotesk",
                                            fontFamily = SpaceGroteskFontFamily,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = textColor
                                        )
                                    }
                                    Column {
                                        Text(
                                            text = "🎵 Pop / Modern",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = textSecondary
                                        )
                                        Text(
                                            text = "Outfit Sans",
                                            fontFamily = OutfitFontFamily,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = textColor
                                        )
                                    }
                                }
                            }
                        }
                    } else {
                        // Static Font Picker when dynamic fonts are turned off
                        Column(modifier = Modifier.padding(top = 8.dp)) {
                            Text(
                                text = "App Font Family (Fixed)",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = textSecondary
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                val currentFont = settings.defaultFontFamily.uppercase()
                                val fonts = listOf(
                                    Triple("DEFAULT", "Outfit Sans", OutfitFontFamily),
                                    Triple("SERIF", "Playfair Serif", PlayfairDisplayFontFamily),
                                    Triple("MONOSPACE", "Space Grotesk", SpaceGroteskFontFamily)
                                )

                                fonts.forEach { (key, label, family) ->
                                    val isSelected = currentFont == key || (key == "DEFAULT" && currentFont != "SERIF" && currentFont != "MONOSPACE")
                                    Surface(
                                        onClick = { onDefaultFontFamilyChange(key) },
                                        shape = RoundedCornerShape(12.dp),
                                        color = if (isSelected) primaryColor else pillBackground,
                                        border = if (isSelected) null else BorderStroke(1.dp, cardBorderColor),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(vertical = 10.dp, horizontal = 6.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Text(
                                                text = "Aa",
                                                fontFamily = family,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 16.sp,
                                                color = if (isSelected) onPrimaryColor else textColor
                                            )
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(
                                                text = label,
                                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                                                color = if (isSelected) onPrimaryColor.copy(alpha = 0.9f) else textSecondary,
                                                maxLines = 1,
                                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Glassmorphic & Live Effects
                SettingsCard(
                    title = "Glassmorphic & Live Effects",
                    icon = Icons.Default.AutoAwesome,
                    cardColor = cardColor,
                    cardBorderColor = cardBorderColor,
                    primaryColor = primaryColor
                ) {
                    SettingsSwitchRow(
                        title = "Frosted Glassmorphism UI",
                        subtitle = "Translucent frosted-glass panels, glowing specular borders & real-time blur",
                        checked = settings.glassmorphicEffectEnabled,
                        onCheckedChange = { enabled ->
                            onGlassmorphicEffectEnabledChange(enabled)
                            onBackdropBlurEnabledChange(enabled)
                        },
                        textColor = textColor,
                        textSecondary = textSecondary,
                        primaryColor = primaryColor
                    )

                    SettingsSwitchRow(
                        title = "Live Moving Gradient",
                        subtitle = "Organic background gradient animation derived from album artwork",
                        checked = settings.liveMovingGradientEnabled,
                        onCheckedChange = onLiveMovingGradientChange,
                        textColor = textColor,
                        textSecondary = textSecondary,
                        primaryColor = primaryColor
                    )

                    SettingsSwitchRow(
                        title = "Snowball Particle Effect",
                        subtitle = "Gentle floating snowfall particle canvas over glass backdrop",
                        checked = settings.snowballEffectEnabled,
                        onCheckedChange = onSnowballEffectEnabledChange,
                        textColor = textColor,
                        textSecondary = textSecondary,
                        primaryColor = primaryColor
                    )

                    SettingsSwitchRow(
                        title = "Glassmorphic Ambient Backdrop",
                        subtitle = "Hardware-accelerated blur render effect on Home & Player screens",
                        checked = settings.backdropBlurEnabled,
                        onCheckedChange = onBackdropBlurEnabledChange,
                        textColor = textColor,
                        textSecondary = textSecondary,
                        primaryColor = primaryColor
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Artwork Glass Blur Intensity (${(settings.blurIntensity * 100).toInt()}%)",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = textColor
                    )
                    Slider(
                        value = settings.blurIntensity,
                        onValueChange = onBlurIntensityChange,
                        valueRange = 0.2f..1.8f,
                        colors = SliderDefaults.colors(
                            thumbColor = primaryColor,
                            activeTrackColor = primaryColor
                        )
                    )
                }

                // Real-time Audio Visualizer
                SettingsCard(
                    title = "Playback Audio Visualizer",
                    icon = Icons.Default.GraphicEq,
                    cardColor = cardColor,
                    cardBorderColor = cardBorderColor,
                    primaryColor = primaryColor
                ) {
                    SettingsSwitchRow(
                        title = "Real-Time Audio Visualizer",
                        subtitle = "Frequency-reactive equalizer bars and waveforms in player & hero card",
                        checked = settings.playbackVisualizerEnabled,
                        onCheckedChange = onPlaybackVisualizerEnabledChange,
                        textColor = textColor,
                        textSecondary = textSecondary,
                        primaryColor = primaryColor
                    )

                    if (settings.playbackVisualizerEnabled) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Visualizer Waveform Style",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            color = textColor
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf(
                                "NEON_BARS" to "Neon Bars",
                                "FLUID_SINE_WAVE" to "Fluid Waves",
                                "GRADIENT_SPECTRUM" to "Spectrum"
                            ).forEach { (styleKey, styleTitle) ->
                                val isSelected = settings.waveformVisualizerStyle == styleKey
                                Surface(
                                    onClick = { onWaveformVisualizerStyleChange(styleKey) },
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (isSelected) primaryColor else pillBackground,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Box(
                                        modifier = Modifier.padding(vertical = 10.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = styleTitle,
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                            color = if (isSelected) onPrimaryColor else textColor
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    SettingsSwitchRow(
                        title = "Force High Refresh Rate (120Hz)",
                        subtitle = "Smooth 120fps animations on compatible high refresh rate displays",
                        checked = settings.forceHighRefreshRate,
                        onCheckedChange = onForceHighRefreshRateChange,
                        textColor = textColor,
                        textSecondary = textSecondary,
                        primaryColor = primaryColor
                    )
                }
            }

            // TAB / SECTION 2: AUDIO ENGINE & DSP
            if (selectedTab == SettingsCategoryTab.ALL || selectedTab == SettingsCategoryTab.AUDIO || selectedTab == SettingsCategoryTab.TOOLS) {
                SettingsCard(
                    title = "Hardware & Device Permissions",
                    icon = Icons.Default.Security,
                    cardColor = cardColor,
                    cardBorderColor = cardBorderColor,
                    primaryColor = primaryColor
                ) {
                    Text(
                        text = "Manage system permissions for connected audio gear and sound processing.",
                        style = MaterialTheme.typography.bodySmall,
                        color = textSecondary
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    // 1. Nearby Devices (Bluetooth)
                    PermissionRowItem(
                        title = "Nearby Devices (Bluetooth)",
                        subtitle = "Detects Bluetooth headphones, speakers & shows device names",
                        isGranted = hasNearbyDevicesPermission,
                        onGrant = onRequestNearbyDevicesPermission,
                        icon = Icons.Default.Bluetooth,
                        textColor = textColor,
                        textSecondary = textSecondary,
                        primaryColor = primaryColor
                    )

                    HorizontalDivider(
                        color = cardBorderColor.copy(alpha = 0.5f),
                        modifier = Modifier.padding(vertical = 10.dp)
                    )

                    // 2. Microphone Access
                    PermissionRowItem(
                        title = "Microphone Access",
                        subtitle = "Powers real-time reactive audio visualizer spectrum & acoustic response",
                        isGranted = hasMicrophonePermission,
                        onGrant = onRequestMicrophonePermission,
                        icon = Icons.Default.Mic,
                        textColor = textColor,
                        textSecondary = textSecondary,
                        primaryColor = primaryColor
                    )

                    HorizontalDivider(
                        color = cardBorderColor.copy(alpha = 0.5f),
                        modifier = Modifier.padding(vertical = 10.dp)
                    )

                    // 3. Media & Storage Files
                    PermissionRowItem(
                        title = "Audio & Storage Files",
                        subtitle = "Scans and plays local high-resolution audio files from device storage",
                        isGranted = hasStoragePermission,
                        onGrant = onRequestStoragePermission,
                        icon = Icons.Default.Folder,
                        textColor = textColor,
                        textSecondary = textSecondary,
                        primaryColor = primaryColor
                    )
                }
            }

            // CHROMECAST & WIRELESS AUDIO STREAMING SECTION
            if (selectedTab == SettingsCategoryTab.ALL || selectedTab == SettingsCategoryTab.CAST || selectedTab == SettingsCategoryTab.AUDIO || selectedTab == SettingsCategoryTab.TOOLS) {
                val isCastConnected = chromecastUiState.activeDevice != null
                val activeDevice = chromecastUiState.activeDevice

                SettingsCard(
                    title = "Chromecast & Wireless Audio",
                    icon = if (isCastConnected) Icons.Default.CastConnected else Icons.Default.Cast,
                    cardColor = cardColor,
                    cardBorderColor = cardBorderColor,
                    primaryColor = primaryColor
                ) {
                    Text(
                        text = "Stream lossless audio wirelessly to Google Nest, Chromecast Audio, Smart TVs, and local Wi-Fi speakers.",
                        style = MaterialTheme.typography.bodySmall,
                        color = textSecondary
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    // Active Cast Receiver Status Banner
                    Surface(
                        color = if (isCastConnected) primaryColor.copy(alpha = 0.15f) else Color(0x18FFFFFF),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, if (isCastConnected) primaryColor.copy(alpha = 0.5f) else cardBorderColor),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(if (isCastConnected) primaryColor.copy(alpha = 0.25f) else Color(0x22FFFFFF)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (isCastConnected) Icons.Default.CastConnected else Icons.Default.Cast,
                                    contentDescription = null,
                                    tint = if (isCastConnected) primaryColor else textSecondary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (isCastConnected) (activeDevice?.name ?: "Connected") else "No Cast Device Connected",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                    color = textColor
                                )
                                Text(
                                    text = if (isCastConnected) "${activeDevice?.modelName ?: "Google Cast"} • ${activeDevice?.ipAddress ?: ""}" else "Tap below to discover & cast music",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = textSecondary
                                )
                            }
                            if (isCastConnected) {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = primaryColor
                                ) {
                                    Text(
                                        text = "LIVE",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onPrimary,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Action Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        FilledTonalButton(
                            onClick = onOpenChromecast,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Cast, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Open Cast Center", fontWeight = FontWeight.SemiBold)
                        }

                        FilledTonalIconButton(
                            onClick = onScanCastDevices,
                            modifier = Modifier.size(40.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Scan for Cast Devices",
                                tint = textColor
                            )
                        }
                    }

                    if (isCastConnected) {
                        Spacer(modifier = Modifier.height(8.dp))
                        FilledTonalButton(
                            onClick = onDisconnectChromecast,
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.filledTonalButtonColors(
                                containerColor = Color(0x33EF4444),
                                contentColor = Color(0xFFF87171)
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Disconnect Cast Output", fontWeight = FontWeight.SemiBold)
                        }
                    }

                    HorizontalDivider(
                        color = cardBorderColor.copy(alpha = 0.5f),
                        modifier = Modifier.padding(vertical = 12.dp)
                    )

                    // Cast Stream Quality Options
                    Text(
                        text = "Cast Stream Audio Quality",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = textColor
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    CastStreamQuality.entries.forEach { quality ->
                        val isSelected = chromecastUiState.streamQuality == quality
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .clickable { onSetCastQuality(quality) }
                                .padding(vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = isSelected,
                                onClick = { onSetCastQuality(quality) },
                                colors = RadioButtonDefaults.colors(selectedColor = primaryColor)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = quality.label,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    ),
                                    color = if (isSelected) primaryColor else textColor
                                )
                                Text(
                                    text = quality.description,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = textSecondary
                                )
                            }
                        }
                    }
                }
            }

            if (selectedTab == SettingsCategoryTab.ALL || selectedTab == SettingsCategoryTab.AUDIO) {
                SettingsCard(
                    title = "Audiophile Audio Pipeline",
                    icon = Icons.Default.GraphicEq,
                    cardColor = cardColor,
                    cardBorderColor = cardBorderColor,
                    primaryColor = primaryColor
                ) {
                    Text(
                        text = "Audio Pipeline Routing Mode",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = textColor
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    AudioRoutingMode.entries.forEach { mode ->
                        val isSelected = audioEngineStatus?.currentMode == mode
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .clickable { onAudioRoutingModeChange(mode) }
                                .padding(vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = isSelected,
                                onClick = { onAudioRoutingModeChange(mode) },
                                colors = RadioButtonDefaults.colors(selectedColor = primaryColor)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = when (mode) {
                                        AudioRoutingMode.HI_RES_DIRECT -> "Direct Bit-Perfect Hi-Res Engine (96kHz/24-bit)"
                                        AudioRoutingMode.AAUDIO_OPENSL -> "Low-Latency AAudio / OpenSL ES"
                                        AudioRoutingMode.AUDIOTRACK_STANDARD -> "Standard Android AudioTrack"
                                    },
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                    color = textColor
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Buttons to Equalizer screens
                    FilledTonalButton(
                        onClick = onOpenBuiltInEqualizer,
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = primaryColor,
                            contentColor = onPrimaryColor
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.Equalizer,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Open Built-In 10-Band Graphic Equalizer", fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    FilledTonalButton(
                        onClick = onOpenSystemEqualizer,
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = pillBackground,
                            contentColor = textColor
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Open Android System Equalizer", fontWeight = FontWeight.SemiBold)
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    SettingsSwitchRow(
                        title = "Prefer System Equalizer by Default",
                        subtitle = "Route EQ button to external system audio effects panel",
                        checked = settings.useSystemEqualizer,
                        onCheckedChange = onUseSystemEqualizerChange,
                        textColor = textColor,
                        textSecondary = textSecondary,
                        primaryColor = primaryColor
                    )
                }

                SettingsCard(
                    title = "Transitions & Audio Fidelity",
                    icon = Icons.Default.MusicNote,
                    cardColor = cardColor,
                    cardBorderColor = cardBorderColor,
                    primaryColor = primaryColor
                ) {
                    SettingsSwitchRow(
                        title = "Gapless Playback",
                        subtitle = "Zero-delay track transitions for live recordings and seamless albums",
                        checked = settings.gaplessPlayback,
                        onCheckedChange = onGaplessPlaybackChange,
                        textColor = textColor,
                        textSecondary = textSecondary,
                        primaryColor = primaryColor
                    )

                    SettingsSwitchRow(
                        title = "Crossfade Tracks",
                        subtitle = "Smooth volume crossfade between successive tracks",
                        checked = settings.crossfadeEnabled,
                        onCheckedChange = onCrossfadeEnabledChange,
                        textColor = textColor,
                        textSecondary = textSecondary,
                        primaryColor = primaryColor
                    )

                    if (settings.crossfadeEnabled) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Crossfade Duration (${settings.crossfadeDurationSec}s)",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            color = textColor
                        )
                        Slider(
                            value = settings.crossfadeDurationSec.toFloat(),
                            onValueChange = { onCrossfadeDurationSecChange(it.toInt()) },
                            valueRange = 1f..12f,
                            steps = 10,
                            colors = SliderDefaults.colors(
                                thumbColor = primaryColor,
                                activeTrackColor = primaryColor
                            )
                        )
                    }

                    SettingsSwitchRow(
                        title = "Show Audio Format Details",
                        subtitle = "Display FLAC/Hi-Res sample rate and bit depth badges",
                        checked = settings.showAudioDetails,
                        onCheckedChange = onShowAudioDetailsChange,
                        textColor = textColor,
                        textSecondary = textSecondary,
                        primaryColor = primaryColor
                    )
                }
            }

            // TAB / SECTION 3: LIBRARY & FOLDER MANAGEMENT
            if (selectedTab == SettingsCategoryTab.ALL || selectedTab == SettingsCategoryTab.LIBRARY) {
                // Folder Whitelist
                SettingsCard(
                    title = "Storage Whitelist (Include Folders)",
                    icon = Icons.Default.Folder,
                    cardColor = cardColor,
                    cardBorderColor = cardBorderColor,
                    primaryColor = primaryColor
                ) {
                    SettingsSwitchRow(
                        title = "Use Whitelisted Folders Only",
                        subtitle = "Ignore all other storage locations except folders listed below",
                        checked = settings.useWhitelistOnly,
                        onCheckedChange = onUseWhitelistOnlyChange,
                        textColor = textColor,
                        textSecondary = textSecondary,
                        primaryColor = primaryColor
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Whitelisted Folders (${settings.whitelistedFolders.size})",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = textColor
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = newWhitelistFolderInput,
                            onValueChange = { newWhitelistFolderInput = it },
                            placeholder = { Text("e.g. /Music or song name", fontSize = 12.sp) },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = primaryColor,
                                unfocusedBorderColor = cardBorderColor
                            ),
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        FilledTonalIconButton(
                            onClick = {
                                if (newWhitelistFolderInput.isNotBlank()) {
                                    onAddWhitelistFolder(newWhitelistFolderInput)
                                    newWhitelistFolderInput = ""
                                } else {
                                    whitelistFolderPickerLauncher.launch(null)
                                }
                            },
                            colors = IconButtonDefaults.filledTonalIconButtonColors(
                                containerColor = primaryColor,
                                contentColor = onPrimaryColor
                            )
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Add Folder")
                        }
                    }

                    // Direct Action Buttons: Add Folder & Select Music Files
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp, bottom = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilledTonalButton(
                            onClick = { whitelistFolderPickerLauncher.launch(null) },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.filledTonalButtonColors(
                                containerColor = pillBackground,
                                contentColor = textColor
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Folder, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Add Folder", fontSize = 12.sp)
                        }

                        FilledTonalButton(
                            onClick = { whitelistFilePickerLauncher.launch(arrayOf("audio/*", "application/ogg")) },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.filledTonalButtonColors(
                                containerColor = pillBackground,
                                contentColor = textColor
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.MusicNote, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Select Files", fontSize = 12.sp)
                        }
                    }

                    settings.whitelistedFolders.forEach { folderPath ->
                        val isAudioFile = folderPath.endsWith(".mp3", ignoreCase = true) ||
                                          folderPath.endsWith(".flac", ignoreCase = true) ||
                                          folderPath.endsWith(".wav", ignoreCase = true) ||
                                          folderPath.endsWith(".m4a", ignoreCase = true) ||
                                          folderPath.endsWith(".aac", ignoreCase = true) ||
                                          folderPath.endsWith(".ogg", ignoreCase = true)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = if (isAudioFile) Icons.Default.MusicNote else Icons.Default.Folder,
                                    contentDescription = null,
                                    tint = primaryColor,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = folderPath,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = textSecondary
                                )
                            }
                            IconButton(
                                onClick = { onRemoveWhitelistFolder(folderPath) },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    Icons.Default.Close,
                                    contentDescription = "Remove",
                                    tint = primaryColor,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }

                // Folder Blacklist
                SettingsCard(
                    title = "Storage Blacklist (Exclude Folders)",
                    icon = Icons.Default.Folder,
                    cardColor = cardColor,
                    cardBorderColor = cardBorderColor,
                    primaryColor = primaryColor
                ) {
                    Text(
                        text = "Exclude WhatsApp, voice notes, and ringtones from the music library:",
                        style = MaterialTheme.typography.bodySmall,
                        color = textSecondary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Blacklisted Folders (${settings.blacklistedFolders.size})",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = textColor
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = newBlacklistFolderInput,
                            onValueChange = { newBlacklistFolderInput = it },
                            placeholder = { Text("e.g. /Storage/Ringtones or voice note", fontSize = 12.sp) },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = primaryColor,
                                unfocusedBorderColor = cardBorderColor
                            ),
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        FilledTonalIconButton(
                            onClick = {
                                if (newBlacklistFolderInput.isNotBlank()) {
                                    onAddBlacklistFolder(newBlacklistFolderInput)
                                    newBlacklistFolderInput = ""
                                } else {
                                    blacklistFolderPickerLauncher.launch(null)
                                }
                            },
                            colors = IconButtonDefaults.filledTonalIconButtonColors(
                                containerColor = primaryColor,
                                contentColor = onPrimaryColor
                            )
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Add Folder")
                        }
                    }

                    // Direct Action Buttons: Add Folder & Select Music Files
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp, bottom = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilledTonalButton(
                            onClick = { blacklistFolderPickerLauncher.launch(null) },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.filledTonalButtonColors(
                                containerColor = pillBackground,
                                contentColor = textColor
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Folder, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Add Folder", fontSize = 12.sp)
                        }

                        FilledTonalButton(
                            onClick = { blacklistFilePickerLauncher.launch(arrayOf("audio/*", "application/ogg")) },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.filledTonalButtonColors(
                                containerColor = pillBackground,
                                contentColor = textColor
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.MusicNote, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Select Files", fontSize = 12.sp)
                        }
                    }

                    settings.blacklistedFolders.forEach { folderPath ->
                        val isAudioFile = folderPath.endsWith(".mp3", ignoreCase = true) ||
                                          folderPath.endsWith(".flac", ignoreCase = true) ||
                                          folderPath.endsWith(".wav", ignoreCase = true) ||
                                          folderPath.endsWith(".m4a", ignoreCase = true) ||
                                          folderPath.endsWith(".aac", ignoreCase = true) ||
                                          folderPath.endsWith(".ogg", ignoreCase = true)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = if (isAudioFile) Icons.Default.MusicNote else Icons.Default.Folder,
                                    contentDescription = null,
                                    tint = primaryColor,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = folderPath,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = textSecondary
                                )
                            }
                            IconButton(
                                onClick = { onRemoveBlacklistFolder(folderPath) },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    Icons.Default.Close,
                                    contentDescription = "Remove",
                                    tint = primaryColor,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }

                // Duration Filter, Artwork Cache & Rescan
                SettingsCard(
                    title = "Duration Filter, Cache & Rescan",
                    icon = Icons.Default.CloudDownload,
                    cardColor = cardColor,
                    cardBorderColor = cardBorderColor,
                    primaryColor = primaryColor
                ) {
                    Text(
                        text = "Ignore Audio Files Under (${settings.minDurationSec} seconds)",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = textColor
                    )
                    Slider(
                        value = settings.minDurationSec.toFloat(),
                        onValueChange = { onMinDurationChange(it.toInt()) },
                        valueRange = 0f..60f,
                        steps = 11,
                        colors = SliderDefaults.colors(
                            thumbColor = primaryColor,
                            activeTrackColor = primaryColor
                        )
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    FilledTonalButton(
                        onClick = onCacheImagesClick,
                        enabled = !isCachingImages,
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = primaryColor,
                            contentColor = onPrimaryColor
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.CloudDownload,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isCachingImages) cachingProgress ?: "Pre-caching Artwork..." else "Pre-cache High-Res Artwork",
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    FilledTonalButton(
                        onClick = onRescanClick,
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = pillBackground,
                            contentColor = textColor
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Rescan Local Storage Now", fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            // TAB / SECTION 4: PLAYBACK, TOOLS & ANALYTICS
            if (selectedTab == SettingsCategoryTab.ALL || selectedTab == SettingsCategoryTab.TOOLS) {
                // Listening Analytics & Tracker
                SettingsCard(
                    title = "Listening Analytics & Tracker",
                    icon = Icons.Default.Whatshot,
                    cardColor = cardColor,
                    cardBorderColor = cardBorderColor,
                    primaryColor = primaryColor
                ) {
                    Text(
                        text = "Music Listening Tracker",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = textColor
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "View detailed statistics on your top listened tracks, artists, and playback hours.",
                        style = MaterialTheme.typography.bodySmall,
                        color = textSecondary
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    FilledTonalButton(
                        onClick = onOpenTracker,
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = primaryColor,
                            contentColor = onPrimaryColor
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.Whatshot,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Open Music Listening Analytics", fontWeight = FontWeight.Bold)
                    }
                }

                // Sleep Timer & Lockscreen
                SettingsCard(
                    title = "Sleep Timer & Lockscreen Controls",
                    icon = Icons.Default.Bedtime,
                    cardColor = cardColor,
                    cardBorderColor = cardBorderColor,
                    primaryColor = primaryColor
                ) {
                    val timerFormatted = remember(sleepTimerState) {
                        val mins = sleepTimerState.remainingSeconds / 60
                        val secs = sleepTimerState.remainingSeconds % 60
                        String.format("%02d:%02d", mins, secs)
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Sleep Timer",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                color = textColor
                            )
                            Text(
                                text = if (sleepTimerState.isActive) "Active: $timerFormatted remaining" else "Automatically stop audio playback after a set duration",
                                style = MaterialTheme.typography.bodySmall,
                                color = textSecondary
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        FilledTonalButton(
                            onClick = onOpenSleepTimer,
                            colors = ButtonDefaults.filledTonalButtonColors(
                                containerColor = primaryColor,
                                contentColor = onPrimaryColor
                            ),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Schedule,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Set Timer")
                        }
                    }

                    SettingsSwitchRow(
                        title = "Lockscreen Player Integration",
                        subtitle = "Show album art and playback controls on device lockscreen",
                        checked = settings.lockScreenPlayer,
                        onCheckedChange = onLockScreenPlayerChange,
                        textColor = textColor,
                        textSecondary = textSecondary,
                        primaryColor = primaryColor
                    )

                    SettingsSwitchRow(
                        title = "Always Shuffle Queue on Launch",
                        subtitle = "Automatically shuffle playback queue when starting app",
                        checked = settings.alwaysShuffle,
                        onCheckedChange = onAlwaysShuffleChange,
                        textColor = textColor,
                        textSecondary = textSecondary,
                        primaryColor = primaryColor
                    )
                }

                // Backup & Restore Data Card
                SettingsCard(
                    title = "Backup & Restore Data",
                    icon = Icons.Default.Backup,
                    cardColor = cardColor,
                    cardBorderColor = cardBorderColor,
                    primaryColor = primaryColor
                ) {
                    Text(
                        text = "Safely export and restore your listening statistics, top-played history, favorite songs, customized lyrics, and all player preferences as a portable JSON file.",
                        style = MaterialTheme.typography.bodySmall,
                        color = textSecondary
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    // Live Status Banner
                    if (!backupStatusMessage.isNullOrBlank()) {
                        Surface(
                            color = primaryColor.copy(alpha = 0.16f),
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, primaryColor.copy(alpha = 0.45f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 12.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = primaryColor,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = backupStatusMessage,
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                                    color = textColor,
                                    modifier = Modifier.weight(1f)
                                )
                                IconButton(
                                    onClick = onClearBackupStatus,
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Dismiss",
                                        tint = textSecondary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }

                    // Data Coverage Overview Grid
                    Surface(
                        color = pillBackground,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "Included in Backup:",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = textColor
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("• Listening Stats & Play Counts", style = MaterialTheme.typography.labelSmall, color = textSecondary)
                                    Text("• Liked & Favorite Songs", style = MaterialTheme.typography.labelSmall, color = textSecondary)
                                }
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("• Custom Synced Lyrics", style = MaterialTheme.typography.labelSmall, color = textSecondary)
                                    Text("• Player & Theme Settings", style = MaterialTheme.typography.labelSmall, color = textSecondary)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Local Device Snapshot Box
                    Surface(
                        color = if (localSnapshotSummary != null) Color(0x18FFFFFF) else Color(0x0EFFFFFF),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, cardBorderColor),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Storage,
                                    contentDescription = null,
                                    tint = primaryColor,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Local Device Snapshot",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                    color = textColor,
                                    modifier = Modifier.weight(1f)
                                )
                                if (localSnapshotSummary != null && localSnapshotSummary.fileSizeFormatted.isNotBlank()) {
                                    Text(
                                        text = localSnapshotSummary.fileSizeFormatted,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = textSecondary
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = if (localSnapshotSummary != null) {
                                    "Saved: ${localSnapshotSummary.formattedDate} • ${localSnapshotSummary.statsCount} stats, ${localSnapshotSummary.favoritesCount} favs, ${localSnapshotSummary.lyricsCount} lyrics"
                                } else {
                                    "Save an instant snapshot in app storage for quick one-tap recovery."
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = textSecondary
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                FilledTonalButton(
                                    onClick = onCreateLocalSnapshot,
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Save Snapshot", fontSize = 12.5.sp)
                                }
                                if (localSnapshotSummary != null) {
                                    FilledTonalButton(
                                        onClick = { showLocalRestoreConfirmDialog = true },
                                        modifier = Modifier.weight(1f),
                                        colors = ButtonDefaults.filledTonalButtonColors(
                                            containerColor = primaryColor.copy(alpha = 0.22f),
                                            contentColor = primaryColor
                                        ),
                                        shape = RoundedCornerShape(10.dp)
                                    ) {
                                        Icon(Icons.Default.History, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Restore", fontSize = 12.5.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // File Export & Import Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        FilledTonalButton(
                            onClick = {
                                val timestamp = java.text.SimpleDateFormat("yyyyMMdd_HHmmss", java.util.Locale.getDefault()).format(java.util.Date())
                                createBackupLauncher.launch("sitar_backup_$timestamp.json")
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.filledTonalButtonColors(
                                containerColor = primaryColor,
                                contentColor = onPrimaryColor
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.FileDownload, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Export (.json)", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }

                        FilledTonalButton(
                            onClick = {
                                openBackupLauncher.launch(arrayOf("application/json", "*/*"))
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.filledTonalButtonColors(
                                containerColor = pillBackground,
                                contentColor = textColor
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.FileUpload, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Import & Restore", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                        }
                    }
                }
            }
        }
    }
}

/**
 * Clean Card Container for Settings Group
 */
@Composable
private fun SettingsCard(
    title: String,
    icon: ImageVector,
    cardColor: Color,
    cardBorderColor: Color,
    primaryColor: Color,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor),
        border = BorderStroke(1.dp, cardBorderColor),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(primaryColor.copy(alpha = 0.18f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = primaryColor,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = title.uppercase(),
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = primaryColor,
                    letterSpacing = 1.1.sp
                )
            }

            content()
        }
    }
}

/**
 * Standard Switch Row in Settings Card
 */
@Composable
private fun SettingsSwitchRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    textColor: Color,
    textSecondary: Color,
    primaryColor: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                color = textColor
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = textSecondary
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
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

private fun resolveDisplayName(context: Context, uri: Uri): String {
    try {
        context.contentResolver.query(uri, arrayOf(android.provider.OpenableColumns.DISPLAY_NAME), null, null, null)?.use { cursor ->
            val col = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
            if (col != -1 && cursor.moveToFirst()) {
                val name = cursor.getString(col)
                if (!name.isNullOrBlank()) return name
            }
        }
    } catch (_: Exception) {}
    val last = uri.lastPathSegment ?: uri.path ?: uri.toString()
    return Uri.decode(last).substringAfterLast(":").substringAfterLast("/").trim()
}

private fun resolveFolderName(uri: Uri): String {
    val decoded = Uri.decode(uri.toString())
    val afterColon = decoded.substringAfterLast(":")
    return afterColon.trim().trim('/')
}

@Composable
private fun PermissionRowItem(
    title: String,
    subtitle: String,
    isGranted: Boolean,
    onGrant: () -> Unit,
    icon: ImageVector,
    textColor: Color,
    textSecondary: Color,
    primaryColor: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(CircleShape)
                .background(if (isGranted) primaryColor.copy(alpha = 0.15f) else textSecondary.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isGranted) primaryColor else textSecondary,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                color = textColor
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = textSecondary
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        if (isGranted) {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = primaryColor.copy(alpha = 0.15f)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Granted",
                        tint = primaryColor,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = "Granted",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = primaryColor
                    )
                }
            }
        } else {
            FilledTonalButton(
                onClick = onGrant,
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "Grant",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                )
            }
        }
    }
}

