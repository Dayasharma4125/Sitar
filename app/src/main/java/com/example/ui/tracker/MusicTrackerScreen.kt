package com.example.ui.tracker

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.MusicTrackerSummary
import com.example.model.Track
import com.example.model.TrackStatsItem
import com.example.ui.components.ArtworkImageOrFallback

enum class TrackerFilter {
    MOST_LISTENED,
    LISTEN_DURATION,
    RECENTLY_PLAYED
}

/**
 * Dedicated Music Tracker & Listening Analytics Screen
 * Shows real-time play counts, total listening duration, top artists,
 * and ranking of favorite music.
 */
@Composable
fun MusicTrackerScreen(
    summary: MusicTrackerSummary,
    currentPlayingTrackId: String = "",
    isPlaying: Boolean = false,
    onTrackClick: (Track) -> Unit,
    onPlayAllTopClick: (shuffle: Boolean) -> Unit = {},
    onClearStatsClick: () -> Unit = {},
    onBackClick: () -> Unit,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    isAmoledBlack: Boolean = false,
    modifier: Modifier = Modifier
) {
    val dynamicBackground = if (isAmoledBlack) Color(0xFF000000) else MaterialTheme.colorScheme.background
    val cardBackground = if (isAmoledBlack) Color(0xFF10131A) else MaterialTheme.colorScheme.surfaceContainer
    val primaryColor = MaterialTheme.colorScheme.primary
    val onPrimaryColor = MaterialTheme.colorScheme.onPrimary
    val textColor = MaterialTheme.colorScheme.onBackground
    val mutedColor = MaterialTheme.colorScheme.onSurfaceVariant

    var selectedFilter by remember { mutableStateOf(TrackerFilter.MOST_LISTENED) }
    var showResetDialog by remember { mutableStateOf(false) }

    val displayedItems = remember(summary, selectedFilter) {
        when (selectedFilter) {
            TrackerFilter.MOST_LISTENED -> summary.mostListened
            TrackerFilter.LISTEN_DURATION -> summary.mostListened.sortedByDescending { it.totalListenTimeMs }
            TrackerFilter.RECENTLY_PLAYED -> summary.recentlyPlayed
        }
    }

    val maxListenMs = remember(displayedItems) {
        displayedItems.maxOfOrNull { it.totalListenTimeMs }?.coerceAtLeast(1L) ?: 1L
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(dynamicBackground)
            .statusBarsPadding()
            .testTag("music_tracker_screen")
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = 16.dp,
                end = 16.dp,
                top = 12.dp,
                bottom = contentPadding.calculateBottomPadding() + 90.dp
            ),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Top Bar
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onBackClick,
                        modifier = Modifier.testTag("tracker_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = textColor
                        )
                    }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "Music Tracker",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = textColor,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "Listening Insights & Play Counts",
                            style = MaterialTheme.typography.bodySmall,
                            color = mutedColor,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    IconButton(
                        onClick = { showResetDialog = true },
                        modifier = Modifier.testTag("tracker_reset_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteOutline,
                            contentDescription = "Reset History",
                            tint = mutedColor
                        )
                    }
                }
            }

            // Overview Metric Grid Cards
            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        TrackerMetricCard(
                            icon = Icons.Default.Whatshot,
                            title = "Total Plays",
                            value = "${summary.totalPlays}",
                            subtitle = "${summary.uniqueTracksCount} songs played",
                            containerColor = cardBackground,
                            accentColor = Color(0xFFFF6D00),
                            modifier = Modifier.weight(1f)
                        )
                        TrackerMetricCard(
                            icon = Icons.Default.AccessTime,
                            title = "Listen Time",
                            value = summary.formattedTotalTime,
                            subtitle = "Across all tracks",
                            containerColor = cardBackground,
                            accentColor = Color(0xFF00B0FF),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        TrackerMetricCard(
                            icon = Icons.Default.Person,
                            title = "Top Artist",
                            value = summary.topArtist ?: "None yet",
                            subtitle = if (summary.topArtist != null) "Most listened artist" else "Listen to tracks",
                            containerColor = cardBackground,
                            accentColor = Color(0xFF00E676),
                            modifier = Modifier.weight(1f)
                        )
                        TrackerMetricCard(
                            icon = Icons.Default.MusicNote,
                            title = "Favorite Song",
                            value = summary.topTrack?.track?.title ?: "None yet",
                            subtitle = if (summary.topTrack != null) "${summary.topTrack.playCount} total plays" else "Play songs to track",
                            containerColor = cardBackground,
                            accentColor = primaryColor,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // Quick Play Actions (Scrollable)
            if (summary.mostListened.isNotEmpty()) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = { onPlayAllTopClick(false) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = primaryColor,
                                contentColor = onPrimaryColor
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.testTag("tracker_play_all_button")
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Play Top List", fontWeight = FontWeight.Bold)
                        }

                        OutlinedButton(
                            onClick = { onPlayAllTopClick(true) },
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, primaryColor.copy(alpha = 0.6f)),
                            modifier = Modifier.testTag("tracker_shuffle_all_button")
                        ) {
                            Icon(Icons.Default.Shuffle, contentDescription = null, tint = primaryColor, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Shuffle Top", color = primaryColor, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Filter Chips (Scrollable)
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = selectedFilter == TrackerFilter.MOST_LISTENED,
                        onClick = { selectedFilter = TrackerFilter.MOST_LISTENED },
                        label = { Text("🔥 Most Listened") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = primaryColor.copy(alpha = 0.25f),
                            selectedLabelColor = primaryColor
                        ),
                        modifier = Modifier.testTag("tracker_filter_most_listened")
                    )

                    FilterChip(
                        selected = selectedFilter == TrackerFilter.LISTEN_DURATION,
                        onClick = { selectedFilter = TrackerFilter.LISTEN_DURATION },
                        label = { Text("⏱ Duration") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = primaryColor.copy(alpha = 0.25f),
                            selectedLabelColor = primaryColor
                        ),
                        modifier = Modifier.testTag("tracker_filter_duration")
                    )

                    FilterChip(
                        selected = selectedFilter == TrackerFilter.RECENTLY_PLAYED,
                        onClick = { selectedFilter = TrackerFilter.RECENTLY_PLAYED },
                        label = { Text("🕒 Recent") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = primaryColor.copy(alpha = 0.25f),
                            selectedLabelColor = primaryColor
                        ),
                        modifier = Modifier.testTag("tracker_filter_recent")
                    )
                }
            }

            // Track List or Empty State
            if (displayedItems.isEmpty()) {
                item {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        shape = RoundedCornerShape(20.dp),
                        color = cardBackground
                    ) {
                        Column(
                            modifier = Modifier.padding(28.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Headphones,
                                contentDescription = null,
                                tint = primaryColor.copy(alpha = 0.7f),
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(14.dp))
                            Text(
                                text = "No Listening History Yet",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = textColor
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Play any song from your library or home screen. Eco Player will automatically track how many times and how long you listen!",
                                style = MaterialTheme.typography.bodySmall,
                                color = mutedColor,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }
            } else {
                itemsIndexed(displayedItems, key = { _, item -> item.track.id }) { index, item ->
                    val isCurrent = currentPlayingTrackId == item.track.id
                    TrackerSongRow(
                        rank = index + 1,
                        item = item,
                        maxListenMs = maxListenMs,
                        isCurrentPlaying = isCurrent,
                        isPlaying = isPlaying && isCurrent,
                        cardColor = cardBackground,
                        primaryColor = primaryColor,
                        textColor = textColor,
                        mutedColor = mutedColor,
                        onClick = { onTrackClick(item.track) }
                    )
                }
            }
        }

        // Confirmation dialog for resetting stats
        if (showResetDialog) {
            AlertDialog(
                onDismissRequest = { showResetDialog = false },
                title = { Text("Reset Music Tracker?") },
                text = { Text("This will clear all play counts, total listen time records, and recently played metrics. Your music files will remain untouched.") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            onClearStatsClick()
                            showResetDialog = false
                        }
                    ) {
                        Text("Reset All", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showResetDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

@Composable
private fun TrackerMetricCard(
    icon: ImageVector,
    title: String,
    value: String,
    subtitle: String,
    containerColor: Color,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(accentColor.copy(alpha = 0.16f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun TrackerSongRow(
    rank: Int,
    item: TrackStatsItem,
    maxListenMs: Long,
    isCurrentPlaying: Boolean,
    isPlaying: Boolean,
    cardColor: Color,
    primaryColor: Color,
    textColor: Color,
    mutedColor: Color,
    onClick: () -> Unit
) {
    val progress = (item.totalListenTimeMs.toFloat() / maxListenMs.toFloat()).coerceIn(0.05f, 1f)

    Surface(
        shape = RoundedCornerShape(14.dp),
        color = if (isCurrentPlaying) primaryColor.copy(alpha = 0.12f) else cardColor,
        border = if (isCurrentPlaying) BorderStroke(1.5.dp, primaryColor) else null,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .testTag("tracker_item_${item.track.id}")
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Rank number
                Box(
                    modifier = Modifier.size(28.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "#$rank",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                        color = if (rank <= 3) primaryColor else mutedColor
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Artwork
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(RoundedCornerShape(8.dp))
                ) {
                    ArtworkImageOrFallback(
                        track = item.track,
                        contentDescription = item.track.title,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Title & Artist
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.track.title,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = if (isCurrentPlaying) primaryColor else textColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = item.track.artist,
                        style = MaterialTheme.typography.bodySmall,
                        color = mutedColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Play Count Badge
                Column(horizontalAlignment = Alignment.End) {
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = primaryColor.copy(alpha = 0.16f)
                    ) {
                        Text(
                            text = "${item.playCount} plays",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = primaryColor,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = item.formattedListenTime,
                        style = MaterialTheme.typography.labelSmall,
                        color = mutedColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Listening Duration Visual Progress Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .weight(1f)
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp)),
                    color = primaryColor,
                    trackColor = primaryColor.copy(alpha = 0.15f)
                )

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = "Last: ${item.formattedLastPlayed}",
                    style = MaterialTheme.typography.labelSmall,
                    color = mutedColor,
                    fontSize = 11.sp
                )
            }
        }
    }
}
