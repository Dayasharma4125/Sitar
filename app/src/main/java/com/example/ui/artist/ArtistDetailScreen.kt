package com.example.ui.artist

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
import androidx.compose.foundation.layout.navigationBarsPadding
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Album
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.model.Album
import com.example.model.Artist
import com.example.model.Track
import java.util.Locale

@Composable
fun ArtistDetailScreen(
    artist: Artist,
    onBackClick: () -> Unit,
    onTrackClick: (Track) -> Unit,
    onPlayAllClick: (List<Track>) -> Unit,
    onShuffleClick: (List<Track>) -> Unit,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    isAmoledBlack: Boolean = false,
    showAudioDetails: Boolean = true,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val representativeTrack = artist.tracks.firstOrNull()
    val bannerUrl = artist.bannerUrl ?: representativeTrack?.coverUrl ?: representativeTrack?.albumArtUri

    val backgroundColor = if (isAmoledBlack) Color.Black else MaterialTheme.colorScheme.background
    val textColor = MaterialTheme.colorScheme.onBackground
    val textSecondary = MaterialTheme.colorScheme.onSurfaceVariant
    val primaryColor = MaterialTheme.colorScheme.primary

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(backgroundColor)
            .testTag("artist_detail_screen")
    ) {
        val bottomInset = contentPadding.calculateBottomPadding()
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = bottomInset + 96.dp)
        ) {
            // 1. Hero Artist Banner
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(280.dp)
                ) {
                    if (bannerUrl != null) {
                        AsyncImage(
                            model = ImageRequest.Builder(context)
                                .data(bannerUrl)
                                .crossfade(true)
                                .build(),
                            contentDescription = artist.name,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )

                        // Scrim gradient over real photo banner so text is always legible
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.verticalGradient(
                                        listOf(
                                            Color.Black.copy(alpha = 0.4f),
                                            Color.Black.copy(alpha = 0.2f),
                                            backgroundColor.copy(alpha = 0.85f),
                                            backgroundColor
                                        )
                                    )
                                )
                        )
                    } else {
                        // Dynamic themed gradient background
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.verticalGradient(
                                        listOf(
                                            MaterialTheme.colorScheme.primaryContainer,
                                            MaterialTheme.colorScheme.surfaceContainerHigh,
                                            backgroundColor
                                        )
                                    )
                                )
                        )
                    }

                    // Top Back Button
                    IconButton(
                        onClick = onBackClick,
                        modifier = Modifier
                            .statusBarsPadding()
                            .padding(12.dp)
                            .clip(CircleShape)
                            .background(
                                if (bannerUrl != null) Color.Black.copy(alpha = 0.5f)
                                else MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.8f)
                            )
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = if (bannerUrl != null) Color.White else textColor
                        )
                    }

                    // Artist Title & Meta info in banner bottom
                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(horizontal = 20.dp, vertical = 16.dp)
                    ) {
                        Text(
                            text = artist.name,
                            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                            color = if (bannerUrl != null) Color.White else textColor
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${artist.trackCount} tracks • ${artist.albumCount} albums in library",
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (bannerUrl != null) Color.White.copy(alpha = 0.85f) else textSecondary
                        )
                    }
                }
            }

            // 2. Play All / Shuffle Actions
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = { onPlayAllClick(artist.tracks) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Play All", fontWeight = FontWeight.Bold)
                    }

                    FilledTonalButton(
                        onClick = { onShuffleClick(artist.tracks) },
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                        ),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Shuffle, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Shuffle", fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            // 3. Discography / Albums Section
            if (artist.albums.isNotEmpty()) {
                item {
                    Column(modifier = Modifier.padding(top = 16.dp)) {
                        Text(
                            text = "Albums & Releases",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = textColor,
                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
                        )

                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 20.dp),
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            items(artist.albums, key = { it.name }) { album ->
                                AlbumCard(album = album, onTrackClick = onTrackClick)
                            }
                        }
                    }
                }
            }

            // 4. Songs List Section
            item {
                Text(
                    text = "Songs",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = textColor,
                    modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 24.dp, bottom = 8.dp)
                )
            }

            itemsIndexed(artist.tracks, key = { _, track -> track.id }) { index, track ->
                ArtistTrackRow(
                    index = index + 1,
                    track = track,
                    showAudioDetails = showAudioDetails,
                    onClick = { onTrackClick(track) }
                )
            }
        }
    }
}

@Composable
fun AlbumCard(
    album: Album,
    onTrackClick: (Track) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val repTrack = album.tracks.firstOrNull()
    val artUrl = album.coverUrl ?: repTrack?.coverUrl ?: repTrack?.albumArtUri
    val textColor = MaterialTheme.colorScheme.onSurface
    val textSecondary = MaterialTheme.colorScheme.onSurfaceVariant

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        border = androidx.compose.foundation.BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f)
        ),
        modifier = modifier
            .width(140.dp)
            .clickable {
                if (album.tracks.isNotEmpty()) {
                    onTrackClick(album.tracks.first())
                }
            }
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceContainerHighest),
                contentAlignment = Alignment.Center
            ) {
                if (artUrl != null) {
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(artUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = album.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Album,
                        contentDescription = null,
                        tint = textSecondary,
                        modifier = Modifier.size(40.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = album.name,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                color = textColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = "${album.trackCount} songs",
                style = MaterialTheme.typography.labelSmall,
                color = textSecondary
            )
        }
    }
}

@Composable
fun ArtistTrackRow(
    index: Int,
    track: Track,
    showAudioDetails: Boolean = true,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val textColor = MaterialTheme.colorScheme.onSurface
    val textSecondary = MaterialTheme.colorScheme.onSurfaceVariant

    Surface(
        onClick = onClick,
        color = Color.Transparent,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "$index",
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                color = textSecondary,
                modifier = Modifier.width(28.dp)
            )

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = track.title,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = textColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                val subtitle = if (showAudioDetails && track.audioQuality.isNotBlank()) {
                    "${track.album} • ${track.audioQuality}"
                } else {
                    track.album
                }
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = textSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            val totalSec = track.durationMs / 1000L
            val min = totalSec / 60L
            val sec = totalSec % 60L
            Text(
                text = String.format(Locale.getDefault(), "%d:%02d", min, sec),
                style = MaterialTheme.typography.labelSmall,
                color = textSecondary
            )
        }
    }
}
