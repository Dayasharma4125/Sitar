package com.example.ui.library

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Equalizer
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
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
import com.example.ui.theme.ExpressiveShapes
import com.example.ui.theme.harmonicBreathing
import com.example.ui.theme.springPressPhysics
import com.example.ui.theme.staggeredSpringEnter

enum class LibraryTab {
    SONGS, ALBUMS, ARTIST
}

enum class LibrarySortMethod(val label: String) {
    TITLE("Title"),
    ARTIST("Artist"),
    ALBUM("Album"),
    MOST_PLAYED("Most Played"),
    DATE_ADDED("Date Added"),
    DURATION("Duration"),
    FILE_SIZE("File Size")
}

/**
 * Strict Material 3 Library Screen (LibraryScreen.kt):
 * 1. Header: "Library" title using M3 headlineLarge.
 * 2. Filters: A horizontal scrollable row of standard M3 FilterChip components ("SONGS", "ALBUMS", "ARTIST").
 * 3. Contained List: Main list wrapped in a large M3 Surface with tonalElevation = 2.dp and
 *    shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp).
 * 4. Active Track Highlighting: Currently playing song gets wrapped in an M3 Surface with
 *    color = MaterialTheme.colorScheme.secondaryContainer, while inactive items use transparent backgrounds.
 * 5. List Items: Standard M3 ListItem composable containing the album art, title, artist, and a trailing
 *    IconButton for the overflow menu.
 * 6. Overlap Prevention: Uses contentPadding from Scaffold.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreen(
    homeUiState: HomeUiState,
    playerUiState: PlayerUiState,
    trackerSummary: MusicTrackerSummary = MusicTrackerSummary(),
    initialSortMethod: LibrarySortMethod = LibrarySortMethod.TITLE,
    initialSortAscending: Boolean = true,
    initialTab: LibraryTab = LibraryTab.SONGS,
    onSortChange: (LibrarySortMethod, Boolean) -> Unit = { _, _ -> },
    onTabChange: (LibraryTab) -> Unit = {},
    onTrackClick: (Track) -> Unit,
    onShuffleAllClick: () -> Unit,
    onSettingsClick: () -> Unit,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    isAmoledBlack: Boolean = false,
    modifier: Modifier = Modifier
) {
    var selectedTab by rememberSaveable { mutableStateOf(initialTab) }
    var sortMethod by rememberSaveable { mutableStateOf(initialSortMethod) }
    var isSortAscending by rememberSaveable { mutableStateOf(initialSortAscending) }
    var showSortMenu by remember { mutableStateOf(false) }

    // Sync with external updates if any
    LaunchedEffect(initialSortMethod, initialSortAscending) {
        sortMethod = initialSortMethod
        isSortAscending = initialSortAscending
    }
    LaunchedEffect(initialTab) {
        selectedTab = initialTab
    }

    val rawTracks = homeUiState.filteredTracks.ifEmpty { homeUiState.tracks }
    val playCountMap = remember(trackerSummary) {
        trackerSummary.mostListened.associate { it.track.id to it.playCount }
    }

    val sortedTracks = remember(rawTracks, isSortAscending, selectedTab, sortMethod, playCountMap) {
        val tracksForTab = when (selectedTab) {
            LibraryTab.SONGS -> rawTracks
            LibraryTab.ALBUMS -> rawTracks.sortedBy { it.album.lowercase() }
            LibraryTab.ARTIST -> rawTracks.sortedBy { it.artist.lowercase() }
        }

        when (sortMethod) {
            LibrarySortMethod.TITLE ->
                if (isSortAscending) tracksForTab.sortedBy { it.title.lowercase() }
                else tracksForTab.sortedByDescending { it.title.lowercase() }

            LibrarySortMethod.ARTIST ->
                if (isSortAscending) tracksForTab.sortedBy { it.artist.lowercase() }
                else tracksForTab.sortedByDescending { it.artist.lowercase() }

            LibrarySortMethod.ALBUM ->
                if (isSortAscending) tracksForTab.sortedBy { it.album.lowercase() }
                else tracksForTab.sortedByDescending { it.album.lowercase() }

            LibrarySortMethod.MOST_PLAYED -> {
                if (isSortAscending) tracksForTab.sortedBy { playCountMap[it.id] ?: 0 }
                else tracksForTab.sortedByDescending { playCountMap[it.id] ?: 0 }
            }

            LibrarySortMethod.DATE_ADDED ->
                if (isSortAscending) tracksForTab.sortedBy { it.contentId }
                else tracksForTab.sortedByDescending { it.contentId }

            LibrarySortMethod.DURATION ->
                if (isSortAscending) tracksForTab.sortedBy { it.durationMs }
                else tracksForTab.sortedByDescending { it.durationMs }

            LibrarySortMethod.FILE_SIZE ->
                if (isSortAscending) tracksForTab.sortedBy { it.sizeBytes }
                else tracksForTab.sortedByDescending { it.sizeBytes }
        }.distinctBy { it.id.ifBlank { it.contentUri ?: it.title } }
    }

    val backgroundColor = if (isAmoledBlack) Color(0xFF000000) else MaterialTheme.colorScheme.background

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(backgroundColor)
            .statusBarsPadding()
            .testTag("library_screen")
    ) {
        // 1. Header Bar: "Library" title using M3 headlineLarge + Settings Icon
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 24.dp, end = 20.dp, top = 16.dp, bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Library",
                style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground
            )

            IconButton(
                onClick = onSettingsClick,
                modifier = Modifier.testTag("library_settings_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Settings",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // 2. Filters: Horizontal scrollable row of standard M3 FilterChip components
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            items(LibraryTab.entries) { tab ->
                val isSelected = selectedTab == tab
                FilterChip(
                    selected = isSelected,
                    onClick = {
                        selectedTab = tab
                        onTabChange(tab)
                    },
                    label = {
                        Text(
                            text = tab.name,
                            style = MaterialTheme.typography.labelLarge
                        )
                    },
                    shape = MaterialTheme.shapes.extraLarge,
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                        selectedLabelColor = MaterialTheme.colorScheme.onSecondaryContainer,
                        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                        labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    modifier = Modifier.testTag("filter_chip_${tab.name.lowercase()}")
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // 3. Contained List: Wrapped in a large M3 Surface with tonalElevation = 2.dp and top-rounded corners
        Surface(
            shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
            color = if (isAmoledBlack) Color(0xFF0B0D12) else MaterialTheme.colorScheme.surfaceContainer,
            tonalElevation = 2.dp,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Actions Header row: Shuffle button & Sort selector + direction toggle
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ElevatedButton(
                        onClick = onShuffleAllClick,
                        shape = MaterialTheme.shapes.medium,
                        colors = ButtonDefaults.elevatedButtonColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                        ),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        modifier = Modifier.testTag("library_shuffle_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Shuffle,
                            contentDescription = "Shuffle",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Shuffle",
                            style = MaterialTheme.typography.labelLarge
                        )
                    }

                    // Sort Methods Menu + Direction Button
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable { showSortMenu = true }
                                    .testTag("library_sort_selector")
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.Sort,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp),
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = sortMethod.label,
                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Spacer(modifier = Modifier.width(2.dp))
                                    Icon(
                                        imageVector = Icons.Default.ArrowDropDown,
                                        contentDescription = "Open Sort Menu",
                                        modifier = Modifier.size(16.dp),
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            DropdownMenu(
                                expanded = showSortMenu,
                                onDismissRequest = { showSortMenu = false }
                            ) {
                                LibrarySortMethod.entries.forEach { method ->
                                    DropdownMenuItem(
                                        text = {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = method.label,
                                                    fontWeight = if (method == sortMethod) FontWeight.Bold else FontWeight.Normal,
                                                    color = if (method == sortMethod) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                                )
                                                if (method == sortMethod) {
                                                    Icon(
                                                        imageVector = Icons.Default.Check,
                                                        contentDescription = "Selected",
                                                        tint = MaterialTheme.colorScheme.primary,
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                }
                                            }
                                        },
                                        onClick = {
                                            sortMethod = method
                                            val newAscending = if (method == LibrarySortMethod.MOST_PLAYED || method == LibrarySortMethod.DATE_ADDED) {
                                                false
                                            } else {
                                                isSortAscending
                                            }
                                            isSortAscending = newAscending
                                            onSortChange(method, newAscending)
                                            showSortMenu = false
                                        }
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.width(4.dp))

                        IconButton(
                            onClick = {
                                val newAscending = !isSortAscending
                                isSortAscending = newAscending
                                onSortChange(sortMethod, newAscending)
                            },
                            modifier = Modifier.testTag("library_sort_direction_button")
                        ) {
                            Icon(
                                imageVector = if (isSortAscending) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
                                contentDescription = if (isSortAscending) "Ascending (Tap to change)" else "Descending (Tap to change)",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }

                // 4. Overlap Prevention: Receives contentPadding from Scaffold
                val bottomInset = contentPadding.calculateBottomPadding()

                LazyColumn(
                    contentPadding = PaddingValues(
                        start = 12.dp,
                        end = 12.dp,
                        top = 2.dp,
                        bottom = bottomInset + 16.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("library_tracks_list")
                ) {
                    itemsIndexed(
                        items = sortedTracks,
                        key = { index, track -> track.id.takeIf { it.isNotBlank() } ?: "lib_track_$index" }
                    ) { index, track ->
                        val isCurrentlyPlaying = playerUiState.currentTrack.id == track.id
                        M3LibraryTrackItem(
                            index = index,
                            track = track,
                            isPlaying = isCurrentlyPlaying && playerUiState.isPlaying,
                            isSelected = isCurrentlyPlaying,
                            onClick = { onTrackClick(track) }
                        )
                    }
                }
            }
        }
    }
}

/**
 * Material 3 Track Item:
 * - Active Track Highlighting: Currently playing song gets wrapped in an M3 Surface with
 *   color = MaterialTheme.colorScheme.secondaryContainer, while inactive items use transparent backgrounds.
 * - Standard M3 ListItem containing album art, title, artist, and a trailing IconButton for the overflow menu.
 */
@Composable
private fun M3LibraryTrackItem(
    index: Int = 0,
    track: Track,
    isPlaying: Boolean,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        shape = ExpressiveShapes.Medium,
        color = if (isSelected) MaterialTheme.colorScheme.secondaryContainer else Color.Transparent,
        tonalElevation = if (isSelected) 3.dp else 0.dp,
        modifier = modifier
            .fillMaxWidth()
            .staggeredSpringEnter(index)
            .springPressPhysics { onClick() }
            .testTag("library_track_item_${track.id}")
    ) {
        ListItem(
            headlineContent = {
                Text(
                    text = track.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = if (isSelected) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            },
            supportingContent = {
                Text(
                    text = "${track.artist} • ${track.album}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (isSelected) MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            },
            leadingContent = {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(ExpressiveShapes.AlbumArtRounded)
                        .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                        .harmonicBreathing(isPlaying = isPlaying, minScale = 0.95f, maxScale = 1.05f, durationMs = 1800),
                    contentAlignment = Alignment.Center
                ) {
                    ArtworkImageOrFallback(
                        track = track,
                        shape = ExpressiveShapes.AlbumArtRounded,
                        modifier = Modifier.fillMaxSize()
                    )

                    if (isPlaying) {
                        Surface(
                            shape = ExpressiveShapes.AlbumArtRounded,
                            color = MaterialTheme.colorScheme.scrim.copy(alpha = 0.45f),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Equalizer,
                                    contentDescription = "Playing",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                    }
                }
            },
            trailingContent = {
                IconButton(
                    onClick = onClick,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "More Options",
                        tint = if (isSelected) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }
            },
            colors = ListItemDefaults.colors(
                containerColor = Color.Transparent
            )
        )
    }
}
