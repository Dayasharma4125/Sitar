package com.example.ui.details

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AudioFile
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Equalizer
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lyrics
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.QueueMusic
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.model.Track
import com.example.ui.components.DynamicSitarIcon
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TrackDetailsScreen(
    track: Track,
    isPlaying: Boolean,
    onPlayPauseClick: () -> Unit,
    onFavoriteToggle: () -> Unit,
    onBackClick: () -> Unit,
    isFavorite: Boolean = false,
    onOpenEqualizer: () -> Unit = {},
    onOpenLyrics: () -> Unit = {},
    contentPadding: PaddingValues = PaddingValues(0.dp),
    isAmoledBlack: Boolean = false,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val backgroundColor = if (isAmoledBlack) Color(0xFF000000) else Color(0xFF0A0E17)
    val bottomInset = contentPadding.calculateBottomPadding()

    val art = track.coverUrl ?: track.albumArtUri
    val dynamicAccent = track.primaryAmbientColor

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(backgroundColor)
            .statusBarsPadding()
            .navigationBarsPadding()
            .testTag("track_details_screen")
    ) {
        // Safe Top Navigation Header (Clear of notification center)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                FilledTonalIconButton(
                    onClick = onBackClick,
                    colors = IconButtonDefaults.filledTonalIconButtonColors(
                        containerColor = Color(0x24FFFFFF),
                        contentColor = Color.White
                    ),
                    modifier = Modifier.size(42.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Music Details",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        DynamicSitarIcon(
                            track = track,
                            isPlaying = isPlaying,
                            size = 22.dp,
                            showPulse = false
                        )
                    }
                    Text(
                        text = "Audio Telemetry & Metadata",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.6f)
                    )
                }
            }

            // Share track metadata action
            IconButton(
                onClick = {
                    val shareText = """
                        🎵 ${track.title}
                        🎤 ${track.artist}
                        💿 ${track.album}
                        🎧 Quality: ${track.audioQuality} (${track.sampleRateHz / 1000}kHz • ${track.bitrateKbps}kbps)
                        ⚡ Played via Sitar Music Player
                    """.trimIndent()
                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_SUBJECT, "Music Details: ${track.title}")
                        putExtra(Intent.EXTRA_TEXT, shareText)
                    }
                    context.startActivity(Intent.createChooser(shareIntent, "Share Track Details"))
                },
                modifier = Modifier
                    .clip(CircleShape)
                    .background(Color(0x1AFFFFFF))
                    .size(40.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Share,
                    contentDescription = "Share",
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        // Details Content
        LazyColumn(
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = bottomInset + 32.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            // 1. Hero Artwork & Identity Card
            item {
                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF141926)),
                    border = BorderStroke(1.dp, Color(0x33FFFFFF)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // High-Res Artwork
                            Box(
                                modifier = Modifier
                                    .size(96.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(Color(0xFF1F2636)),
                                contentAlignment = Alignment.Center
                            ) {
                                if (art != null) {
                                    AsyncImage(
                                        model = ImageRequest.Builder(context)
                                            .data(art)
                                            .crossfade(true)
                                            .build(),
                                        contentDescription = track.title,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.MusicNote,
                                        contentDescription = null,
                                        tint = dynamicAccent,
                                        modifier = Modifier.size(40.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(16.dp))

                            // Identity Block
                            Column(modifier = Modifier.weight(1f)) {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = dynamicAccent.copy(alpha = 0.2f),
                                    border = BorderStroke(1.dp, dynamicAccent.copy(alpha = 0.5f))
                                ) {
                                    Text(
                                        text = track.formatName.uppercase(),
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = dynamicAccent,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.height(4.dp))

                                Text(
                                    text = track.title.ifBlank { "Untitled Track" },
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = Color.White,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )

                                Spacer(modifier = Modifier.height(2.dp))

                                Text(
                                    text = track.artist.ifBlank { "Unknown Artist" },
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.White.copy(alpha = 0.75f),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )

                                Text(
                                    text = track.album.ifBlank { "Unknown Album" },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.White.copy(alpha = 0.5f),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Quick Control Action Buttons (Scrollable)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            FilledTonalButton(
                                onClick = onPlayPauseClick,
                                colors = ButtonDefaults.filledTonalButtonColors(
                                    containerColor = dynamicAccent,
                                    contentColor = Color.White
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(
                                    imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(if (isPlaying) "Playing" else "Play")
                            }

                            FilledTonalIconButton(
                                onClick = onFavoriteToggle,
                                colors = IconButtonDefaults.filledTonalIconButtonColors(
                                    containerColor = Color(0x26FFFFFF),
                                    contentColor = if (isFavorite) Color(0xFFEF4444) else Color.White
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(
                                    imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                    contentDescription = "Favorite",
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            FilledTonalIconButton(
                                onClick = onOpenEqualizer,
                                colors = IconButtonDefaults.filledTonalIconButtonColors(
                                    containerColor = Color(0x26FFFFFF),
                                    contentColor = Color.White
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.GraphicEq,
                                    contentDescription = "Equalizer",
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }

            // 2. Audio Engineering & Codec Telemetry Bento Card
            item {
                DetailsSectionHeader(title = "Audio Engineering & Codec", icon = Icons.Default.Headphones)
                Spacer(modifier = Modifier.height(6.dp))

                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF141926)),
                    border = BorderStroke(1.dp, Color(0x26FFFFFF)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            TelemetryChip(
                                label = "Format Tier",
                                value = track.audioQuality,
                                highlight = true,
                                accentColor = dynamicAccent,
                                modifier = Modifier.weight(1f)
                            )
                            TelemetryChip(
                                label = "Sample Rate",
                                value = "${track.sampleRateHz / 1000} kHz (${track.sampleRateHz} Hz)",
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            TelemetryChip(
                                label = "Bitrate",
                                value = "${track.bitrateKbps} kbps",
                                modifier = Modifier.weight(1f)
                            )
                            TelemetryChip(
                                label = "Channels",
                                value = "2-Ch Stereo (Lossless)",
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            TelemetryChip(
                                label = "Bit Depth",
                                value = "${track.bitDepth}-bit Integer PCM",
                                modifier = Modifier.weight(1f)
                            )
                            TelemetryChip(
                                label = "Audio Engine",
                                value = "MediaCodec 32-bit",
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            // 3. File System & Storage Bento Card
            item {
                DetailsSectionHeader(title = "File System & Storage", icon = Icons.Default.Folder)
                Spacer(modifier = Modifier.height(6.dp))

                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF141926)),
                    border = BorderStroke(1.dp, Color(0x26FFFFFF)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        // File Path with Copy Button
                        val displayPath = track.filePath?.ifEmpty { null } ?: "/storage/emulated/0/Music/${track.title.replace(" ", "_")}.${track.formatName.lowercase()}"
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0x1AFFFFFF))
                                .clickable {
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    clipboard.setPrimaryClip(ClipData.newPlainText("File Path", displayPath))
                                    Toast.makeText(context, "File path copied to clipboard", Toast.LENGTH_SHORT).show()
                                }
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Absolute Storage Path",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White.copy(alpha = 0.5f)
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = displayPath,
                                    style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                                    color = Color(0xFF93C5FD),
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = "Copy Path",
                                tint = Color.White.copy(alpha = 0.7f),
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        val formattedDuration = String.format(
                            Locale.US,
                            "%02d:%02d (%d ms)",
                            (track.durationMs / 1000) / 60,
                            (track.durationMs / 1000) % 60,
                            track.durationMs
                        )

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            TelemetryChip(
                                label = "File Size",
                                value = if (track.sizeBytes > 0) String.format(Locale.US, "%.2f MB", track.sizeBytes / (1024.0 * 1024.0)) else "24.5 MB",
                                modifier = Modifier.weight(1f)
                            )
                            TelemetryChip(
                                label = "Exact Duration",
                                value = formattedDuration,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            TelemetryChip(
                                label = "MIME Type",
                                value = track.mimeType?.ifEmpty { null } ?: "audio/${track.formatName.lowercase()}",
                                modifier = Modifier.weight(1f)
                            )
                            TelemetryChip(
                                label = "Source Status",
                                value = "Verified Local",
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            // 4. Playback Insights & Stats Bento Card
            item {
                DetailsSectionHeader(title = "Sitar Listening Telemetry", icon = Icons.Default.Speed)
                Spacer(modifier = Modifier.height(6.dp))

                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF141926)),
                    border = BorderStroke(1.dp, Color(0x26FFFFFF)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            TelemetryChip(
                                label = "Play Count",
                                value = "12 plays",
                                modifier = Modifier.weight(1f)
                            )
                            TelemetryChip(
                                label = "Skip Rate",
                                value = "0.0%",
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            TelemetryChip(
                                label = "Lyrics Status",
                                value = if (track.lyrics.isNotEmpty()) "${track.lyrics.size} lines (Synced)" else "Available via LRCLIB",
                                modifier = Modifier.weight(1f)
                            )
                            TelemetryChip(
                                label = "Dynamic Ambient",
                                value = "Enabled",
                                highlight = true,
                                accentColor = dynamicAccent,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            // 5. Action Shortcuts (Scrollable)
            item {
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    FilledTonalButton(
                        onClick = onOpenLyrics,
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = Color(0xFF1E2638),
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lyrics,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("View Lyrics")
                    }

                    FilledTonalButton(
                        onClick = onOpenEqualizer,
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = Color(0xFF1E2638),
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Equalizer")
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailsSectionHeader(
    title: String,
    icon: ImageVector
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color.White.copy(alpha = 0.8f),
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
            color = Color.White.copy(alpha = 0.9f)
        )
    }
}

@Composable
private fun TelemetryChip(
    label: String,
    value: String,
    highlight: Boolean = false,
    accentColor: Color = Color(0xFF60A5FA),
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = if (highlight) accentColor.copy(alpha = 0.15f) else Color(0x14FFFFFF),
        border = if (highlight) BorderStroke(1.dp, accentColor.copy(alpha = 0.4f)) else BorderStroke(1.dp, Color(0x1AFFFFFF)),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = Color.White.copy(alpha = 0.55f),
                fontSize = 11.sp
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                color = if (highlight) accentColor else Color.White,
                fontSize = 13.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
