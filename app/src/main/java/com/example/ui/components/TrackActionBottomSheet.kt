package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.QueueMusic
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lyrics
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
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
import com.example.model.Track

/**
 * Long-Press Context Action Sheet for music tracks.
 * Provides basic and power functions: Edit tags, Delete, Favorite, Queue, Details, and Lyrics.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrackActionBottomSheet(
    track: Track,
    isFavorite: Boolean,
    onDismiss: () -> Unit,
    onPlayClick: () -> Unit,
    onPlayNextClick: () -> Unit,
    onAddToQueueClick: () -> Unit,
    onToggleFavorite: () -> Unit,
    onEditMetadata: (newTitle: String, newArtist: String, newAlbum: String) -> Unit,
    onEditLyricsClick: () -> Unit,
    onTrackDetailsClick: () -> Unit,
    onDeleteTrack: () -> Unit,
    isAmoledBlack: Boolean = false
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showEditDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    val bgColor = if (isAmoledBlack) Color(0xFF0D1117) else Color(0xFF161F2E)
    val cardColor = if (isAmoledBlack) Color(0xFF161B22) else Color(0xFF1E293B)
    val primaryColor = MaterialTheme.colorScheme.primary
    val textColor = Color.White
    val textMuted = Color(0xFF94A3B8)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = bgColor,
        contentColor = textColor,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp)
        ) {
            // Header: Track Artwork & Info
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                ArtworkImageOrFallback(
                    track = track,
                    contentDescription = track.title,
                    modifier = Modifier
                        .size(54.dp)
                        .clip(RoundedCornerShape(12.dp))
                )
                Spacer(modifier = Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = track.title.ifBlank { "Unknown Title" },
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = textColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${track.artist.ifBlank { "Unknown Artist" }} • ${track.album.ifBlank { "Unknown Album" }}",
                        style = MaterialTheme.typography.bodySmall,
                        color = textMuted,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = primaryColor.copy(alpha = 0.16f),
                    border = BorderStroke(1.dp, primaryColor.copy(alpha = 0.35f))
                ) {
                    Text(
                        text = if (track.audioQuality.contains("FLAC", ignoreCase = true) || track.bitDepth >= 24) "Hi-Res" else "Lossless",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = primaryColor,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(Color(0xFF27354A))
            )
            Spacer(modifier = Modifier.height(10.dp))

            // Action Items
            TrackActionItem(
                icon = Icons.Default.PlayArrow,
                title = "Play Now",
                subtitle = "Start playback immediately",
                tint = primaryColor,
                onClick = {
                    onPlayClick()
                    onDismiss()
                }
            )

            TrackActionItem(
                icon = Icons.Default.SkipNext,
                title = "Play Next",
                subtitle = "Insert right after current playing song",
                tint = Color(0xFF38BDF8),
                onClick = {
                    onPlayNextClick()
                    onDismiss()
                }
            )

            TrackActionItem(
                icon = Icons.AutoMirrored.Filled.QueueMusic,
                title = "Add to Playing Queue",
                subtitle = "Append to bottom of active playlist",
                tint = Color(0xFFA78BFA),
                onClick = {
                    onAddToQueueClick()
                    onDismiss()
                }
            )

            TrackActionItem(
                icon = if (isFavorite) Icons.Default.Favorite else Icons.Outlined.FavoriteBorder,
                title = if (isFavorite) "Remove from Favorites" else "Add to Favorites",
                subtitle = if (isFavorite) "Currently in your liked collection" else "Save to your Favorites playlist",
                tint = if (isFavorite) Color(0xFFFF4B4B) else textMuted,
                onClick = {
                    onToggleFavorite()
                    onDismiss()
                }
            )

            TrackActionItem(
                icon = Icons.Default.Edit,
                title = "Edit Track Info",
                subtitle = "Change title, artist, or album metadata",
                tint = Color(0xFFFBBF24),
                onClick = { showEditDialog = true }
            )

            TrackActionItem(
                icon = Icons.Default.Lyrics,
                title = "Edit Lyrics",
                subtitle = "Synchronize or customize timestamped lyrics",
                tint = Color(0xFF34D399),
                onClick = {
                    onEditLyricsClick()
                    onDismiss()
                }
            )

            TrackActionItem(
                icon = Icons.Default.Info,
                title = "Track Details & Audio Specs",
                subtitle = "Bitrate, sample rate, format & file path",
                tint = Color(0xFF93C5FD),
                onClick = {
                    onTrackDetailsClick()
                    onDismiss()
                }
            )

            TrackActionItem(
                icon = Icons.Default.Delete,
                title = "Delete Song",
                subtitle = "Permanently remove from device & library",
                tint = Color(0xFFEF4444),
                onClick = { showDeleteDialog = true }
            )
        }
    }

    // Edit Metadata Dialog
    if (showEditDialog) {
        var editTitle by remember { mutableStateOf(track.title) }
        var editArtist by remember { mutableStateOf(track.artist) }
        var editAlbum by remember { mutableStateOf(track.album) }

        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            containerColor = Color(0xFF1E293B),
            title = {
                Text("Edit Track Metadata", color = Color.White, fontWeight = FontWeight.Bold)
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = editTitle,
                        onValueChange = { editTitle = it },
                        label = { Text("Title", color = Color(0xFF94A3B8)) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = primaryColor,
                            unfocusedBorderColor = Color(0xFF475569)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = editArtist,
                        onValueChange = { editArtist = it },
                        label = { Text("Artist", color = Color(0xFF94A3B8)) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = primaryColor,
                            unfocusedBorderColor = Color(0xFF475569)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = editAlbum,
                        onValueChange = { editAlbum = it },
                        label = { Text("Album", color = Color(0xFF94A3B8)) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = primaryColor,
                            unfocusedBorderColor = Color(0xFF475569)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onEditMetadata(editTitle.trim(), editArtist.trim(), editAlbum.trim())
                        showEditDialog = false
                        onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = primaryColor)
                ) {
                    Text("Save Changes", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditDialog = false }) {
                    Text("Cancel", color = Color(0xFF94A3B8))
                }
            }
        )
    }

    // Delete Confirmation Dialog
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            containerColor = Color(0xFF1E293B),
            title = {
                Text("Delete Song?", color = Color(0xFFEF4444), fontWeight = FontWeight.Bold)
            },
            text = {
                Text(
                    "Are you sure you want to delete \"${track.title}\"? This will remove it from your library and delete the file from storage.",
                    color = Color(0xFFCBD5E1)
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteTrack()
                        showDeleteDialog = false
                        onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
                ) {
                    Text("Delete", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showDeleteDialog = false },
                    border = BorderStroke(1.dp, Color(0xFF475569))
                ) {
                    Text("Cancel", color = Color.White)
                }
            }
        )
    }
}

@Composable
private fun TrackActionItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    tint: Color,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        color = Color.Transparent,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 9.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = CircleShape,
                color = tint.copy(alpha = 0.14f),
                modifier = Modifier.size(38.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = tint,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                    color = Color.White
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF94A3B8),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
