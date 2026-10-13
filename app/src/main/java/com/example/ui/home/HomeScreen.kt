package com.example.ui.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Cast
import androidx.compose.material.icons.filled.CastConnected
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.HomeUiState
import com.example.model.MusicTrackerSummary
import com.example.model.PlayerUiState
import com.example.model.Track
import com.example.ui.components.ArtworkImageOrFallback
import com.example.ui.components.DynamicSitarIcon
import com.example.ui.components.GlassmorphicBackground
import com.example.ui.components.TrackActionBottomSheet
import com.example.ui.theme.ExpressiveShapes
import com.example.ui.theme.harmonicBreathing
import com.example.ui.theme.springPressPhysics
import com.example.ui.theme.springWobble
import com.example.ui.theme.staggeredSpringEnter

/**
 * Modern High-Fidelity Home Screen with:
 * - Dynamic Greeting & Top Action Bar (Chromecast, Rescan, Settings)
 * - Quick Action Cards: Shuffle All & Playback Tracker
 * - Category Filter Chips ("All Songs", "Favorites", "Most Played", "Hi-Res Audio")
 * - Full Sorting Controls (Title, Artist, Date Added, Duration, Most Played with Ascending/Descending)
 * - Consistent context-aware queue playback so sorted lists are permanently preserved.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    homeUiState: HomeUiState,
    currentTrack: Track,
    isPlaying: Boolean,
    isFavorite: Boolean,
    favoriteTrackIds: Set<String> = emptySet(),
    trackerSummary: MusicTrackerSummary = MusicTrackerSummary(),
    initialSortMethod: String = "TITLE",
    initialSortAscending: Boolean = true,
    onSortChange: (method: String, ascending: Boolean) -> Unit = { _, _ -> },
    onOpenTracker: () -> Unit = {},
    onTrackClick: (Track, List<Track>) -> Unit,
    onPlayPauseClick: () -> Unit,
    onPreviousClick: () -> Unit = {},
    onNextClick: () -> Unit = {},
    onShuffleAllClick: () -> Unit,
    onToggleFavoriteTrack: (String) -> Unit = {},
    onDeleteTrack: (Track) -> Unit = {},
    onEditMetadata: (trackId: String, newTitle: String, newArtist: String, newAlbum: String) -> Unit = { _, _, _, _ -> },
    onPlayNextClick: (Track) -> Unit = {},
    onAddToQueueClick: (Track) -> Unit = {},
    onEditLyricsClick: (Track) -> Unit = {},
    onSettingsClick: () -> Unit,
    onOpenChromecast: () -> Unit = {},
    isChromecastConnected: Boolean = false,
    chromecastDeviceName: String? = null,
    onRequestPermission: () -> Unit,
    onRequestMicrophonePermission: () -> Unit = {},
    onRequestNearbyDevicesPermission: () -> Unit = {},
    onRescanClick: () -> Unit,
    onTrackDetailsClick: (Track) -> Unit = {},
    contentPadding: PaddingValues = PaddingValues(0.dp),
    listState: LazyListState = rememberLazyListState(),
    isAmoledBlack: Boolean = false,
    backdropBlurEnabled: Boolean = true,
    liveMovingGradientEnabled: Boolean = true,
    homeScreenEffectsEnabled: Boolean = true,
    homeScreenEffectStyle: String = "AMBIENT_AURA",
    blurIntensity: Float = 0.8f,
    playbackVisualizerEnabled: Boolean = true,
    waveformVisualizerStyle: String = "NEON_BARS",
    modifier: Modifier = Modifier
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val onPrimaryColor = MaterialTheme.colorScheme.onPrimary
    val backgroundColor = if (isAmoledBlack) Color(0xFF000000) else MaterialTheme.colorScheme.background
    val cardBackground = if (isAmoledBlack) Color(0xFF101216) else MaterialTheme.colorScheme.surfaceContainer
    val pillBackground = if (isAmoledBlack) Color(0xFF181B22) else MaterialTheme.colorScheme.surfaceContainerHighest
    val textColor = MaterialTheme.colorScheme.onBackground
    val textMuted = MaterialTheme.colorScheme.onSurfaceVariant
    val cardBorder = BorderStroke(
        width = 1.dp,
        color = if (isAmoledBlack) Color(0xFF242832) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
    )

    var selectedFilter by remember { mutableStateOf("All Songs") }
    var sortMethod by remember(initialSortMethod) { mutableStateOf(initialSortMethod) }
    var sortAscending by remember(initialSortAscending) { mutableStateOf(initialSortAscending) }
    var showSortSheet by remember { mutableStateOf(false) }
    var selectedTrackForActions by remember { mutableStateOf<Track?>(null) }
    val categories = remember { listOf("All Songs", "Favorites", "Most Played", "Hi-Res Audio") }

    // Filter raw tracks efficiently without redundant iterations
    val filteredByCategory = remember(
        homeUiState.tracks,
        selectedFilter,
        if (selectedFilter == "Favorites") favoriteTrackIds else Unit,
        if (selectedFilter == "Most Played") trackerSummary.mostListened else Unit
    ) {
        when (selectedFilter) {
            "Favorites" -> homeUiState.tracks.filter { it.id in favoriteTrackIds }
            "Most Played" -> {
                val mostListenedIds = trackerSummary.mostListened.map { it.track.id }.toSet()
                homeUiState.tracks.filter { it.id in mostListenedIds }
            }
            "Hi-Res Audio" -> homeUiState.tracks.filter { it.bitDepth >= 24 || it.sampleRateHz >= 48000 || it.formatName == "FLAC" }
            else -> homeUiState.tracks
        }
    }

    // Apply active Sort Order
    val sortedDisplayTracks = remember(
        filteredByCategory,
        sortMethod,
        sortAscending,
        if (sortMethod == "MOST_PLAYED") trackerSummary.mostListened else Unit
    ) {
        if (sortMethod == "TITLE" && sortAscending) {
            filteredByCategory
        } else {
            val sorted = when (sortMethod) {
                "ARTIST" -> filteredByCategory.sortedBy { it.artist.lowercase() }
                "DATE_ADDED" -> filteredByCategory.sortedByDescending { it.dateAdded }
                "DURATION" -> filteredByCategory.sortedByDescending { it.durationMs }
                "MOST_PLAYED" -> {
                    val playCountMap = trackerSummary.mostListened.associate { it.track.id to it.playCount }
                    filteredByCategory.sortedByDescending { playCountMap[it.id] ?: 0 }
                }
                else -> filteredByCategory.sortedBy { it.title.lowercase() }
            }
            if (sortAscending) sorted else sorted.reversed()
        }
    }

    val primaryAccent = remember(currentTrack.primaryAmbientColor, primaryColor) {
        try {
            val color = currentTrack.primaryAmbientColor
            if (color != Color.Unspecified && color.value != 0UL) {
                Color(color.red, color.green, color.blue, 1f)
            } else {
                primaryColor
            }
        } catch (_: Throwable) {
            primaryColor
        }
    }

    val animatedAccent by animateColorAsState(
        targetValue = primaryAccent,
        animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing),
        label = "homeScreenAccent"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(backgroundColor)
    ) {
        // Living Ambient Canvas Effects
        if (backdropBlurEnabled || liveMovingGradientEnabled || homeScreenEffectsEnabled) {
            GlassmorphicBackground(
                track = currentTrack,
                blurIntensity = blurIntensity,
                isAmoledBlack = isAmoledBlack,
                isPlaying = isPlaying,
                snowballEffectEnabled = homeScreenEffectsEnabled && homeScreenEffectStyle == "SNOWBALL",
                liveMovingGradientEnabled = liveMovingGradientEnabled
            )
        }

        LazyColumn(
            state = listState,
            contentPadding = PaddingValues(
                top = 0.dp,
                bottom = contentPadding.calculateBottomPadding() + 96.dp
            ),
            modifier = Modifier.fillMaxSize()
        ) {
            // 1. TOP HEADER & BRANDING
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 20.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        val currentHour = remember { java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY) }
                        val greeting = when (currentHour) {
                            in 5..11 -> "Good Morning"
                            in 12..16 -> "Good Afternoon"
                            in 17..21 -> "Good Evening"
                            else -> "Night Grooves"
                        }
                        Text(
                            text = greeting,
                            style = MaterialTheme.typography.labelMedium,
                            color = textMuted,
                            fontSize = 12.sp
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Sitar",
                                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                                color = textColor,
                                fontSize = 28.sp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            DynamicSitarIcon(
                                track = currentTrack,
                                isPlaying = isPlaying,
                                size = 28.dp,
                                showPulse = true
                            )
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        // Chromecast / Wireless Audio Output button
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(if (isChromecastConnected) primaryAccent.copy(alpha = 0.22f) else pillBackground)
                                .border(
                                    if (isChromecastConnected) BorderStroke(1.5.dp, primaryAccent) else cardBorder,
                                    CircleShape
                                )
                                .clickable { onOpenChromecast() }
                                .testTag("home_chromecast_button"),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (isChromecastConnected) Icons.Default.CastConnected else Icons.Default.Cast,
                                contentDescription = "Chromecast & Wireless Audio",
                                tint = if (isChromecastConnected) primaryAccent else textColor,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        // Rescan button
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(pillBackground)
                                .border(cardBorder, CircleShape)
                                .clickable { onRescanClick() },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Rescan Media",
                                tint = textColor,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        // Settings button
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(pillBackground)
                                .border(cardBorder, CircleShape)
                                .clickable { onSettingsClick() }
                                .testTag("settings_button"),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "Settings",
                                tint = textColor,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }

            // Optional Active Casting Banner
            if (isChromecastConnected && !chromecastDeviceName.isNullOrBlank()) {
                item {
                    Surface(
                        onClick = onOpenChromecast,
                        color = primaryAccent.copy(alpha = 0.14f),
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.dp, primaryAccent.copy(alpha = 0.45f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 4.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.CastConnected,
                                contentDescription = null,
                                tint = primaryAccent,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Casting to $chromecastDeviceName",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                    color = textColor,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "Tap to manage devices or adjust volume",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = textMuted,
                                    maxLines = 1
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = primaryAccent.copy(alpha = 0.25f)
                            ) {
                                Text(
                                    text = "Active",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = primaryAccent,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
            }

            // 2. QUICK ACTIONS ROW (Shuffle All & Playback Tracker)
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Shuffle All Card
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = primaryColor,
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .clickable { onShuffleAllClick() }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(onPrimaryColor.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Shuffle,
                                    contentDescription = "Shuffle",
                                    tint = onPrimaryColor,
                                    modifier = Modifier.size(17.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Shuffle All",
                                    color = onPrimaryColor,
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    fontSize = 13.5.sp
                                )
                                Text(
                                    text = "${homeUiState.tracks.size} Tracks",
                                    color = onPrimaryColor.copy(alpha = 0.85f),
                                    style = MaterialTheme.typography.bodySmall,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }

                    // Analytics / Tracker Card
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = cardBackground,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f)),
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .clickable { onOpenTracker() }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(primaryColor.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Whatshot,
                                    contentDescription = "Tracker",
                                    tint = primaryColor,
                                    modifier = Modifier.size(17.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Analytics",
                                    color = textColor,
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    fontSize = 13.5.sp
                                )
                                Text(
                                    text = "${trackerSummary.totalPlays} Plays",
                                    color = textMuted,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                }
            }

            // 3. CATEGORY CHIPS
            item {
                Column(modifier = Modifier.padding(top = 8.dp, bottom = 2.dp)) {
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 20.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(categories, key = { it }) { filter ->
                            val isSelected = filter == selectedFilter
                            Surface(
                                shape = CircleShape,
                                color = if (isSelected) primaryColor else pillBackground,
                                border = if (isSelected) null else cardBorder,
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .clickable { selectedFilter = filter }
                            ) {
                                Text(
                                    text = filter,
                                    color = if (isSelected) onPrimaryColor else textColor.copy(alpha = 0.85f),
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    fontSize = 12.5.sp,
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                }
            }

            // 4. SONGS SECTION & SORT CONTROLS HEADER
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "$selectedFilter • ${sortedDisplayTracks.size}",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = textColor,
                        fontSize = 14.sp
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Sort Chip Button
                        Surface(
                            onClick = { showSortSheet = true },
                            shape = CircleShape,
                            color = pillBackground,
                            border = cardBorder
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Sort,
                                    contentDescription = "Sort Music",
                                    tint = primaryAccent,
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(5.dp))
                                Text(
                                    text = when (sortMethod) {
                                        "ARTIST" -> "Artist"
                                        "DATE_ADDED" -> "Date Added"
                                        "DURATION" -> "Duration"
                                        "MOST_PLAYED" -> "Most Played"
                                        else -> "Name"
                                    },
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                                    color = textColor,
                                    fontSize = 11.5.sp
                                )
                            }
                        }

                        // Ascending / Descending Toggle Button
                        Surface(
                            onClick = {
                                val nextAsc = !sortAscending
                                sortAscending = nextAsc
                                onSortChange(sortMethod, nextAsc)
                            },
                            shape = CircleShape,
                            color = pillBackground,
                            border = cardBorder,
                            modifier = Modifier.size(30.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = if (sortAscending) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
                                    contentDescription = if (sortAscending) "Ascending Order" else "Descending Order",
                                    tint = primaryAccent,
                                    modifier = Modifier.size(15.dp)
                                )
                            }
                        }
                    }
                }
            }

            // 5. SONG LIST
            if (sortedDisplayTracks.isEmpty() && selectedFilter == "Favorites") {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 32.dp, vertical = 40.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(Color(0x1FFF4B4B)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Favorite,
                                contentDescription = null,
                                tint = Color(0xFFFF4B4B),
                                modifier = Modifier.size(32.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No Liked Songs Yet",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = textColor
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Tap the heart on any song to add it to your favorites. Your liked songs will be permanently saved.",
                            style = MaterialTheme.typography.bodySmall,
                            color = textMuted,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            }

            itemsIndexed(
                items = sortedDisplayTracks,
                key = { index, track -> track.id.takeIf { it.isNotBlank() } ?: "home_track_$index" },
                contentType = { _, _ -> "song_item" }
            ) { _, track ->
                CleanSongListItem(
                    track = track,
                    isPlaying = isPlaying && currentTrack.id == track.id,
                    isFavorite = track.id in favoriteTrackIds,
                    accentColor = primaryColor,
                    textColor = textColor,
                    textMuted = textMuted,
                    pillBackground = pillBackground,
                    onClick = { onTrackClick(track, sortedDisplayTracks) },
                    onActionClick = { selectedTrackForActions = track },
                    onToggleFavorite = { onToggleFavoriteTrack(track.id) }
                )
            }
        }

        // Sort Options Modal Bottom Sheet
        if (showSortSheet) {
            ModalBottomSheet(
                onDismissRequest = { showSortSheet = false },
                sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
                containerColor = cardBackground,
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                        .padding(bottom = 32.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Sort Music By",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = textColor
                        )
                        IconButton(onClick = { showSortSheet = false }) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = textMuted)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    val sortOptions = listOf(
                        "TITLE" to "Song Name / Title",
                        "ARTIST" to "Artist Name",
                        "DATE_ADDED" to "Date Added (Recent First)",
                        "DURATION" to "Duration (Length)",
                        "MOST_PLAYED" to "Most Played (Play Count)"
                    )

                    sortOptions.forEach { (key, label) ->
                        val isSelected = sortMethod == key
                        Surface(
                            onClick = {
                                sortMethod = key
                                onSortChange(key, sortAscending)
                                showSortSheet = false
                            },
                            shape = RoundedCornerShape(14.dp),
                            color = if (isSelected) primaryColor.copy(alpha = 0.15f) else Color.Transparent,
                            border = if (isSelected) BorderStroke(1.dp, primaryColor.copy(alpha = 0.5f)) else null,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.bodyLarge.copy(
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    ),
                                    color = if (isSelected) primaryColor else textColor
                                )
                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Selected",
                                        tint = primaryColor,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Direction Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = {
                                sortAscending = true
                                onSortChange(sortMethod, true)
                                showSortSheet = false
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (sortAscending) primaryColor else pillBackground,
                                contentColor = if (sortAscending) onPrimaryColor else textColor
                            ),
                            shape = ExpressiveShapes.Pill,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.ArrowUpward, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Ascending")
                        }

                        Button(
                            onClick = {
                                sortAscending = false
                                onSortChange(sortMethod, false)
                                showSortSheet = false
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (!sortAscending) primaryColor else pillBackground,
                                contentColor = if (!sortAscending) onPrimaryColor else textColor
                            ),
                            shape = ExpressiveShapes.Pill,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.ArrowDownward, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Descending")
                        }
                    }
                }
            }
        }

        // Long-Press Track Action Bottom Sheet (Delete, Edit Tags, Queue, Favorite, Lyrics)
        selectedTrackForActions?.let { actionTrack ->
            TrackActionBottomSheet(
                track = actionTrack,
                isFavorite = actionTrack.id in favoriteTrackIds,
                onDismiss = { selectedTrackForActions = null },
                onPlayClick = {
                    onTrackClick(actionTrack, sortedDisplayTracks)
                    selectedTrackForActions = null
                },
                onPlayNextClick = {
                    onPlayNextClick(actionTrack)
                    selectedTrackForActions = null
                },
                onAddToQueueClick = {
                    onAddToQueueClick(actionTrack)
                    selectedTrackForActions = null
                },
                onToggleFavorite = {
                    onToggleFavoriteTrack(actionTrack.id)
                },
                onEditMetadata = { newTitle, newArtist, newAlbum ->
                    onEditMetadata(actionTrack.id, newTitle, newArtist, newAlbum)
                    selectedTrackForActions = null
                },
                onEditLyricsClick = {
                    onEditLyricsClick(actionTrack)
                    selectedTrackForActions = null
                },
                onTrackDetailsClick = {
                    onTrackDetailsClick(actionTrack)
                    selectedTrackForActions = null
                },
                onDeleteTrack = {
                    onDeleteTrack(actionTrack)
                    selectedTrackForActions = null
                },
                isAmoledBlack = isAmoledBlack
            )
        }
    }
}

@Composable
fun HomeScreen(
    homeUiState: HomeUiState,
    playerUiState: PlayerUiState,
    favoriteTrackIds: Set<String> = emptySet(),
    trackerSummary: MusicTrackerSummary = MusicTrackerSummary(),
    initialSortMethod: String = "TITLE",
    initialSortAscending: Boolean = true,
    onSortChange: (method: String, ascending: Boolean) -> Unit = { _, _ -> },
    onOpenTracker: () -> Unit = {},
    onTrackClick: (Track, List<Track>) -> Unit,
    onPlayPauseClick: () -> Unit,
    onPreviousClick: () -> Unit = {},
    onNextClick: () -> Unit = {},
    onShuffleAllClick: () -> Unit,
    onToggleFavoriteTrack: (String) -> Unit = {},
    onDeleteTrack: (Track) -> Unit = {},
    onEditMetadata: (trackId: String, newTitle: String, newArtist: String, newAlbum: String) -> Unit = { _, _, _, _ -> },
    onPlayNextClick: (Track) -> Unit = {},
    onAddToQueueClick: (Track) -> Unit = {},
    onEditLyricsClick: (Track) -> Unit = {},
    onSettingsClick: () -> Unit,
    onOpenChromecast: () -> Unit = {},
    isChromecastConnected: Boolean = false,
    chromecastDeviceName: String? = null,
    onRequestPermission: () -> Unit,
    onRequestMicrophonePermission: () -> Unit = {},
    onRequestNearbyDevicesPermission: () -> Unit = {},
    onRescanClick: () -> Unit,
    onTrackDetailsClick: (Track) -> Unit = {},
    contentPadding: PaddingValues = PaddingValues(0.dp),
    listState: LazyListState = rememberLazyListState(),
    isAmoledBlack: Boolean = false,
    backdropBlurEnabled: Boolean = true,
    liveMovingGradientEnabled: Boolean = true,
    homeScreenEffectsEnabled: Boolean = true,
    homeScreenEffectStyle: String = "AMBIENT_AURA",
    blurIntensity: Float = 0.8f,
    playbackVisualizerEnabled: Boolean = true,
    waveformVisualizerStyle: String = "NEON_BARS",
    modifier: Modifier = Modifier
) = HomeScreen(
    homeUiState = homeUiState,
    currentTrack = playerUiState.currentTrack,
    isPlaying = playerUiState.isPlaying,
    isFavorite = playerUiState.isFavorite,
    favoriteTrackIds = favoriteTrackIds,
    trackerSummary = trackerSummary,
    initialSortMethod = initialSortMethod,
    initialSortAscending = initialSortAscending,
    onSortChange = onSortChange,
    onOpenTracker = onOpenTracker,
    onTrackClick = onTrackClick,
    onPlayPauseClick = onPlayPauseClick,
    onPreviousClick = onPreviousClick,
    onNextClick = onNextClick,
    onShuffleAllClick = onShuffleAllClick,
    onToggleFavoriteTrack = onToggleFavoriteTrack,
    onDeleteTrack = onDeleteTrack,
    onEditMetadata = onEditMetadata,
    onPlayNextClick = onPlayNextClick,
    onAddToQueueClick = onAddToQueueClick,
    onEditLyricsClick = onEditLyricsClick,
    onSettingsClick = onSettingsClick,
    onOpenChromecast = onOpenChromecast,
    isChromecastConnected = isChromecastConnected,
    chromecastDeviceName = chromecastDeviceName,
    onRequestPermission = onRequestPermission,
    onRequestMicrophonePermission = onRequestMicrophonePermission,
    onRequestNearbyDevicesPermission = onRequestNearbyDevicesPermission,
    onRescanClick = onRescanClick,
    onTrackDetailsClick = onTrackDetailsClick,
    contentPadding = contentPadding,
    listState = listState,
    isAmoledBlack = isAmoledBlack,
    backdropBlurEnabled = backdropBlurEnabled,
    liveMovingGradientEnabled = liveMovingGradientEnabled,
    homeScreenEffectsEnabled = homeScreenEffectsEnabled,
    homeScreenEffectStyle = homeScreenEffectStyle,
    blurIntensity = blurIntensity,
    playbackVisualizerEnabled = playbackVisualizerEnabled,
    waveformVisualizerStyle = waveformVisualizerStyle,
    modifier = modifier
)

/**
 * Clean Song List item with thumbnail, format details, and play status indicator.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun CleanSongListItem(
    track: Track,
    isPlaying: Boolean,
    isFavorite: Boolean = false,
    accentColor: Color,
    textColor: Color,
    textMuted: Color,
    pillBackground: Color,
    onClick: () -> Unit,
    onActionClick: () -> Unit = {},
    onToggleFavorite: () -> Unit = {}
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = if (isPlaying) accentColor.copy(alpha = 0.14f) else Color.Transparent,
        border = if (isPlaying) BorderStroke(1.dp, accentColor.copy(alpha = 0.40f)) else null,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 1.5.dp)
            .combinedClickable(
                onClick = onClick,
                onLongClick = onActionClick
            )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Artwork Thumbnail
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(pillBackground)
            ) {
                ArtworkImageOrFallback(
                    track = track,
                    targetSizePx = 132,
                    modifier = Modifier.fillMaxSize()
                )
                if (isPlaying) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.5f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.GraphicEq,
                            contentDescription = "Playing",
                            tint = accentColor,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = track.title.ifBlank { "Local Track" },
                    color = if (isPlaying) accentColor else textColor,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "${track.artist.ifBlank { "Unknown Artist" }} • ${formatDuration(track.durationMs)}",
                        color = textMuted,
                        fontSize = 12.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    if (track.formatName.isNotBlank() && track.formatName.lowercase() != "unknown") {
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = pillBackground,
                            modifier = Modifier.padding(vertical = 1.dp)
                        ) {
                            Text(
                                text = track.formatName.uppercase(),
                                color = textMuted,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.5.dp)
                            )
                        }
                    }
                }
            }

            // Favorite Heart Button
            IconButton(
                onClick = onToggleFavorite,
                modifier = Modifier.size(34.dp)
            ) {
                Icon(
                    imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Outlined.FavoriteBorder,
                    contentDescription = if (isFavorite) "Remove from favorites" else "Add to favorites",
                    tint = if (isFavorite) Color(0xFFFF4B4B) else textMuted.copy(alpha = 0.60f),
                    modifier = Modifier.size(18.dp)
                )
            }

            // Options Button (Action Center)
            IconButton(
                onClick = onActionClick,
                modifier = Modifier.size(34.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = "Options",
                    tint = textMuted,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

private fun formatDuration(durationMs: Long): String {
    if (durationMs <= 0L) return "0:00"
    val totalSeconds = durationMs / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format(java.util.Locale.US, "%d:%02d", minutes, seconds)
}
