package com.example.ui.music

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.Equalizer
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Track
import com.example.ui.components.ArtworkImageOrFallback
import com.example.ui.theme.ExpressiveShapes
import com.example.ui.theme.ensureVibrantDarkAccent
import com.example.ui.theme.springPressPhysics
import java.util.Locale

enum class TrackSortOrder {
    TITLE,
    ARTIST,
    ALBUM,
    DURATION
}

/**
 * All Music Screen built exclusively with Material 3 Expressive design tokens,
 * tactile spring physics, elevated surface containers, and expressive shapes.
 */
@Composable
fun AllMusicScreen(
    tracks: List<Track>,
    currentPlayingTrackId: String,
    isPlaying: Boolean,
    onTrackClick: (Track) -> Unit,
    onShuffleAllClick: () -> Unit,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    showAudioDetails: Boolean = true,
    isAmoledBlack: Boolean = false,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var sortOrder by remember { mutableStateOf(TrackSortOrder.TITLE) }

    val filteredAndSortedTracks = remember(tracks, searchQuery, sortOrder) {
        val filtered = if (searchQuery.isBlank()) {
            tracks
        } else {
            val q = searchQuery.trim().lowercase()
            tracks.filter {
                it.title.lowercase().contains(q) ||
                    it.artist.lowercase().contains(q) ||
                    it.album.lowercase().contains(q)
            }
        }

        val sorted = when (sortOrder) {
            TrackSortOrder.TITLE -> filtered.sortedBy { it.title.lowercase() }
            TrackSortOrder.ARTIST -> filtered.sortedBy { it.artist.lowercase() }
            TrackSortOrder.ALBUM -> filtered.sortedBy { it.album.lowercase() }
            TrackSortOrder.DURATION -> filtered.sortedByDescending { it.durationMs }
        }
        sorted.distinctBy { it.id.ifBlank { it.contentUri ?: it.title } }
    }

    val backgroundColor = if (isAmoledBlack) Color(0xFF000000) else MaterialTheme.colorScheme.background

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(backgroundColor)
            .testTag("all_music_screen")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            // M3 Expressive Header Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "All Music",
                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "${filteredAndSortedTracks.size} tracks available",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Button(
                    onClick = onShuffleAllClick,
                    shape = ExpressiveShapes.PillDock,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    ),
                    modifier = Modifier
                        .springPressPhysics { onShuffleAllClick() }
                        .testTag("all_music_shuffle_button")
                ) {
                    Icon(Icons.Default.Shuffle, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Shuffle", style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold))
                }
            }

            // Material 3 Expressive Search Field
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = {
                    Text("Search title, artist, or album...", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f))
                },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = "Search", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                },
                trailingIcon = {
                    IconButton(onClick = {
                        sortOrder = when (sortOrder) {
                            TrackSortOrder.TITLE -> TrackSortOrder.ARTIST
                            TrackSortOrder.ARTIST -> TrackSortOrder.ALBUM
                            TrackSortOrder.ALBUM -> TrackSortOrder.DURATION
                            TrackSortOrder.DURATION -> TrackSortOrder.TITLE
                        }
                    }) {
                        Icon(
                            Icons.AutoMirrored.Filled.Sort,
                            contentDescription = "Sort: ${sortOrder.name}",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                singleLine = true,
                shape = ExpressiveShapes.Medium,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainer,
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                    focusedTextColor = MaterialTheme.colorScheme.onSurface,
                    unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 6.dp)
                    .testTag("all_music_search_bar")
            )

            // Tracks List with M3 Expressive Cards
            val bottomInset = contentPadding.calculateBottomPadding()
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 10.dp, bottom = bottomInset + 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                itemsIndexed(
                    items = filteredAndSortedTracks,
                    key = { index, track -> track.id.takeIf { it.isNotBlank() } ?: "all_track_$index" }
                ) { index, track ->
                    val isCurrent = track.id == currentPlayingTrackId
                    val vibrantAccent = ensureVibrantDarkAccent(track.primaryAmbientColor, fallback = MaterialTheme.colorScheme.primary)

                    Surface(
                        onClick = { onTrackClick(track) },
                        shape = ExpressiveShapes.Medium,
                        color = if (isCurrent) {
                            MaterialTheme.colorScheme.secondaryContainer
                        } else {
                            if (isAmoledBlack) Color(0xFF0D1016) else MaterialTheme.colorScheme.surfaceContainer
                        },
                        border = if (isCurrent) {
                            BorderStroke(1.5.dp, vibrantAccent.copy(alpha = 0.6f))
                        } else {
                            BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))
                        },
                        tonalElevation = if (isCurrent) 4.dp else 1.dp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .springPressPhysics { onTrackClick(track) }
                            .testTag("track_row_${track.id}")
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Artwork
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(ExpressiveShapes.AlbumArtRounded)
                            ) {
                                ArtworkImageOrFallback(
                                    track = track,
                                    modifier = Modifier.fillMaxSize()
                                )
                                if (isCurrent && isPlaying) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(Color.Black.copy(alpha = 0.5f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Equalizer,
                                            contentDescription = "Playing",
                                            tint = vibrantAccent,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.width(14.dp))

                            // Track Info
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = track.title,
                                    style = MaterialTheme.typography.bodyLarge.copy(
                                        fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.SemiBold
                                    ),
                                    color = if (isCurrent) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = track.artist,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.weight(1f, fill = false)
                                    )
                                    if (showAudioDetails && track.audioQuality.isNotBlank()) {
                                        Text(
                                            text = " • ${track.audioQuality}",
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                            color = MaterialTheme.colorScheme.primary,
                                            maxLines = 1
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            // Duration
                            Text(
                                text = formatDuration(track.durationMs),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun formatDuration(durationMs: Long): String {
    val totalSeconds = (durationMs / 1000).coerceAtLeast(0)
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format(Locale.US, "%d:%02d", minutes, seconds)
}
