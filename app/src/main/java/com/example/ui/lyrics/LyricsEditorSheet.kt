package com.example.ui.lyrics

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.FastRewind
import androidx.compose.material.icons.filled.FormatAlignLeft
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.lyrics.LyricsParser
import com.example.model.LyricLine
import com.example.model.Track
import kotlinx.coroutines.launch

private enum class EditorTab(val title: String) {
    SYNCED_LINES("Synced Lines"),
    RAW_LRC("Raw / Text"),
    FETCH_ONLINE("Fetch Online")
}

/**
 * Fullscreen Interactive Material 3 Expressive Lyrics Editor.
 * Allows users to change, fetch, update, modify, time-sync, and persist lyrics for any music track.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LyricsEditorSheet(
    track: Track,
    initialLines: List<LyricLine>,
    initialPlainText: String?,
    initialIsSynced: Boolean,
    currentPlaybackPositionMs: Long,
    onSeekTo: (Long) -> Unit,
    onSaveLyrics: (lines: List<LyricLine>, plainText: String?, isSynced: Boolean) -> Unit,
    onFetchOnline: suspend (artist: String, title: String) -> Pair<String?, String?>,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var selectedTab by remember { mutableIntStateOf(if (initialIsSynced && initialLines.isNotEmpty()) 0 else if (!initialPlainText.isNullOrBlank()) 1 else 0) }

    // Editable working state
    val workingLines = remember {
        mutableStateListOf<LyricLine>().apply {
            addAll(initialLines.ifEmpty {
                if (!initialPlainText.isNullOrBlank()) {
                    initialPlainText.lines().filter { it.isNotBlank() }.mapIndexed { i, text ->
                        LyricLine((i * 3000L), text.trim())
                    }
                } else emptyList()
            })
        }
    }

    var workingPlainText by remember {
        mutableStateOf(
            initialPlainText ?: if (initialLines.isNotEmpty()) {
                LyricsParser.formatToLrc(initialLines)
            } else ""
        )
    }

    var isSyncedMode by remember { mutableStateOf(initialIsSynced || workingLines.isNotEmpty()) }

    // Fetch tab state
    var searchArtist by remember { mutableStateOf(track.artist) }
    var searchTitle by remember { mutableStateOf(track.title) }
    var isFetching by remember { mutableStateOf(false) }
    var fetchedSyncedLyrics by remember { mutableStateOf<String?>(null) }
    var fetchedPlainLyrics by remember { mutableStateOf<String?>(null) }
    var fetchStatusMessage by remember { mutableStateOf<String?>(null) }

    // Global offset dialog
    var showOffsetDialog by remember { mutableStateOf(false) }

    // Active color scheme
    val primaryColor = if (track.primaryAmbientColor != Color.Unspecified) track.primaryAmbientColor else MaterialTheme.colorScheme.primary
    val containerBg = MaterialTheme.colorScheme.surface

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding(),
            color = containerBg
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Top App Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        IconButton(onClick = onDismiss) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Close",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                        Column {
                            Text(
                                text = "Lyrics Studio Editor",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "${track.title} • ${track.artist}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    // Save Button
                    Button(
                        onClick = {
                            if (isSyncedMode) {
                                val sorted = workingLines.sortedBy { it.timestampMs }
                                val formattedLrc = LyricsParser.formatToLrc(sorted)
                                onSaveLyrics(sorted, formattedLrc, true)
                            } else {
                                val parsed = LyricsParser.parseLrc(workingPlainText)
                                if (parsed.isNotEmpty()) {
                                    onSaveLyrics(parsed, workingPlainText, true)
                                } else {
                                    onSaveLyrics(emptyList(), workingPlainText, false)
                                }
                            }
                            Toast.makeText(context, "Lyrics saved successfully", Toast.LENGTH_SHORT).show()
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Save", fontWeight = FontWeight.Bold)
                    }
                }

                // Playback Position Indicator Bar
                Surface(
                    color = primaryColor.copy(alpha = 0.12f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.MusicNote,
                                contentDescription = null,
                                tint = primaryColor,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Current Position: ${LyricsParser.formatMsToLrcTimestamp(currentPlaybackPositionMs)}",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.SemiBold
                                ),
                                color = primaryColor
                            )
                        }

                        if (selectedTab == 0) {
                            Text(
                                text = "${workingLines.size} lines",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // Primary Tab Navigation
                PrimaryTabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = containerBg,
                    contentColor = primaryColor
                ) {
                    EditorTab.entries.forEachIndexed { index, tab ->
                        Tab(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            text = {
                                Text(
                                    text = tab.title,
                                    fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal,
                                    fontSize = 13.sp
                                )
                            }
                        )
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

                // Content View according to selected tab
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    when (selectedTab) {
                        0 -> SyncedLinesEditorView(
                            lines = workingLines,
                            currentPlaybackPositionMs = currentPlaybackPositionMs,
                            primaryColor = primaryColor,
                            onSeekTo = onSeekTo,
                            onUpdateLine = { index, newMs, newText ->
                                if (index in workingLines.indices) {
                                    workingLines[index] = LyricLine(newMs, newText)
                                }
                            },
                            onDeleteLine = { index ->
                                if (index in workingLines.indices) {
                                    workingLines.removeAt(index)
                                }
                            },
                            onAddLine = {
                                val newLine = LyricLine(currentPlaybackPositionMs, "New lyric line")
                                workingLines.add(newLine)
                                workingLines.sortBy { it.timestampMs }
                            },
                            onSortLines = {
                                val sorted = workingLines.sortedBy { it.timestampMs }
                                workingLines.clear()
                                workingLines.addAll(sorted)
                            },
                            onOpenOffsetDialog = { showOffsetDialog = true }
                        )

                        1 -> RawTextEditorView(
                            text = workingPlainText,
                            onTextChange = {
                                workingPlainText = it
                                // Also update workingLines if text contains timestamps
                                val parsed = LyricsParser.parseLrc(it)
                                if (parsed.isNotEmpty()) {
                                    workingLines.clear()
                                    workingLines.addAll(parsed)
                                    isSyncedMode = true
                                }
                            },
                            isSynced = isSyncedMode,
                            onToggleSynced = { isSyncedMode = it },
                            primaryColor = primaryColor,
                            onConvertToSynced = {
                                val lines = workingPlainText.lines().filter { it.isNotBlank() }
                                workingLines.clear()
                                lines.forEachIndexed { i, l ->
                                    workingLines.add(LyricLine(i * 3500L, l.trim()))
                                }
                                workingPlainText = LyricsParser.formatToLrc(workingLines)
                                isSyncedMode = true
                                selectedTab = 0
                            }
                        )

                        2 -> FetchOnlineLyricsView(
                            searchArtist = searchArtist,
                            searchTitle = searchTitle,
                            onArtistChange = { searchArtist = it },
                            onTitleChange = { searchTitle = it },
                            isFetching = isFetching,
                            statusMessage = fetchStatusMessage,
                            fetchedSynced = fetchedSyncedLyrics,
                            fetchedPlain = fetchedPlainLyrics,
                            primaryColor = primaryColor,
                            onStartSearch = {
                                scope.launch {
                                    isFetching = true
                                    fetchStatusMessage = null
                                    try {
                                        val (synced, plain) = onFetchOnline(searchArtist, searchTitle)
                                        fetchedSyncedLyrics = synced
                                        fetchedPlainLyrics = plain
                                        if (synced != null || plain != null) {
                                            fetchStatusMessage = "Lyrics successfully found online!"
                                        } else {
                                            fetchStatusMessage = "No lyrics found for '$searchTitle' by '$searchArtist'."
                                        }
                                    } catch (e: Exception) {
                                        fetchStatusMessage = "Search error: ${e.localizedMessage}"
                                    } finally {
                                        isFetching = false
                                    }
                                }
                            },
                            onApplyFetched = {
                                if (!fetchedSyncedLyrics.isNullOrBlank()) {
                                    val parsed = LyricsParser.parseLrc(fetchedSyncedLyrics!!)
                                    if (parsed.isNotEmpty()) {
                                        workingLines.clear()
                                        workingLines.addAll(parsed)
                                        workingPlainText = fetchedSyncedLyrics!!
                                        isSyncedMode = true
                                        selectedTab = 0
                                        Toast.makeText(context, "Applied synced lyrics", Toast.LENGTH_SHORT).show()
                                    }
                                } else if (!fetchedPlainLyrics.isNullOrBlank()) {
                                    workingPlainText = fetchedPlainLyrics!!
                                    isSyncedMode = false
                                    selectedTab = 1
                                    Toast.makeText(context, "Applied plain lyrics", Toast.LENGTH_SHORT).show()
                                }
                            }
                        )
                    }
                }
            }
        }
    }

    // Time Offset Adjustment Dialog
    if (showOffsetDialog) {
        var offsetMs by remember { mutableStateOf("0") }
        Dialog(onDismissRequest = { showOffsetDialog = false }) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.padding(16.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "Global Time Shift",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Shift all timestamps forward (+) or backward (-) to calibrate synchronization.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilledTonalButton(
                            onClick = {
                                val current = offsetMs.toLongOrNull() ?: 0L
                                offsetMs = (current - 500L).toString()
                            }
                        ) {
                            Text("-0.5s")
                        }
                        FilledTonalButton(
                            onClick = {
                                val current = offsetMs.toLongOrNull() ?: 0L
                                offsetMs = (current - 1000L).toString()
                            }
                        ) {
                            Text("-1.0s")
                        }
                        FilledTonalButton(
                            onClick = {
                                val current = offsetMs.toLongOrNull() ?: 0L
                                offsetMs = (current + 500L).toString()
                            }
                        ) {
                            Text("+0.5s")
                        }
                        FilledTonalButton(
                            onClick = {
                                val current = offsetMs.toLongOrNull() ?: 0L
                                offsetMs = (current + 1000L).toString()
                            }
                        ) {
                            Text("+1.0s")
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = offsetMs,
                        onValueChange = { offsetMs = it },
                        label = { Text("Offset (Milliseconds)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { showOffsetDialog = false }) {
                            Text("Cancel")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                val shift = offsetMs.toLongOrNull() ?: 0L
                                if (shift != 0L) {
                                    val shifted = workingLines.map { line ->
                                        LyricLine((line.timestampMs + shift).coerceAtLeast(0L), line.text)
                                    }
                                    workingLines.clear()
                                    workingLines.addAll(shifted)
                                    Toast.makeText(context, "Shifted lines by ${shift}ms", Toast.LENGTH_SHORT).show()
                                }
                                showOffsetDialog = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = primaryColor)
                        ) {
                            Text("Apply Shift")
                        }
                    }
                }
            }
        }
    }
}

/**
 * Tab 1: Interactive Synced Lines View.
 * Displays each line with timestamp chip, tap-to-stamp, seek, text edit, and deletion.
 */
@Composable
private fun SyncedLinesEditorView(
    lines: List<LyricLine>,
    currentPlaybackPositionMs: Long,
    primaryColor: Color,
    onSeekTo: (Long) -> Unit,
    onUpdateLine: (Int, Long, String) -> Unit,
    onDeleteLine: (Int) -> Unit,
    onAddLine: () -> Unit,
    onSortLines: () -> Unit,
    onOpenOffsetDialog: () -> Unit
) {
    val listState = rememberLazyListState()

    Column(modifier = Modifier.fillMaxSize()) {
        // Quick Action Bar (Scrollable)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.Start),
            verticalAlignment = Alignment.CenterVertically
        ) {
            FilledTonalButton(
                onClick = onAddLine,
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Add Line", fontSize = 12.sp)
            }

            Spacer(modifier = Modifier.weight(1f, fill = false))

            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                FilledTonalIconButton(
                    onClick = onOpenOffsetDialog,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(Icons.Default.Timer, contentDescription = "Time Shift", modifier = Modifier.size(18.dp))
                }

                FilledTonalIconButton(
                    onClick = onSortLines,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(Icons.Default.Sort, contentDescription = "Sort Lines", modifier = Modifier.size(18.dp))
                }
            }
        }

        if (lines.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "No Synced Lines Yet",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Tap '+ Add Line' or switch to 'Fetch Online' to pull lyrics automatically.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = onAddLine,
                        colors = ButtonDefaults.buttonColors(containerColor = primaryColor)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Add First Line")
                    }
                }
            }
        } else {
            LazyColumn(
                state = listState,
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                itemsIndexed(lines) { index, line ->
                    SyncedLineItem(
                        index = index,
                        line = line,
                        currentPositionMs = currentPlaybackPositionMs,
                        primaryColor = primaryColor,
                        onSeekTo = onSeekTo,
                        onUpdate = { newMs, newText -> onUpdateLine(index, newMs, newText) },
                        onDelete = { onDeleteLine(index) }
                    )
                }
            }
        }
    }
}

/**
 * Individual editable line card in SyncedLinesEditorView.
 */
@Composable
private fun SyncedLineItem(
    index: Int,
    line: LyricLine,
    currentPositionMs: Long,
    primaryColor: Color,
    onSeekTo: (Long) -> Unit,
    onUpdate: (Long, String) -> Unit,
    onDelete: () -> Unit
) {
    var isEditingText by remember { mutableStateOf(false) }
    var editText by remember(line.text) { mutableStateOf(line.text) }

    val isCurrent = currentPositionMs >= line.timestampMs &&
        (index == 0 || currentPositionMs >= line.timestampMs)

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isCurrent) primaryColor.copy(alpha = 0.08f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
        ),
        border = if (isCurrent) BorderStroke(1.dp, primaryColor.copy(alpha = 0.5f)) else null,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Timestamp and Seek Pill
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = primaryColor.copy(alpha = 0.18f),
                    modifier = Modifier.clickable { onSeekTo(line.timestampMs) }
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Seek",
                            tint = primaryColor,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = LyricsParser.formatMsToLrcTimestamp(line.timestampMs),
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            ),
                            color = primaryColor
                        )
                    }
                }

                // Precision Nudge Buttons & Stamp Button
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // -250ms
                    FilledTonalIconButton(
                        onClick = { onUpdate((line.timestampMs - 250L).coerceAtLeast(0L), line.text) },
                        modifier = Modifier.size(28.dp),
                        shape = CircleShape
                    ) {
                        Text("-", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }

                    // +250ms
                    FilledTonalIconButton(
                        onClick = { onUpdate(line.timestampMs + 250L, line.text) },
                        modifier = Modifier.size(28.dp),
                        shape = CircleShape
                    ) {
                        Text("+", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }

                    // Stamp Current Playback Time
                    FilledTonalButton(
                        onClick = { onUpdate(currentPositionMs, line.text) },
                        shape = RoundedCornerShape(6.dp),
                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                        modifier = Modifier.height(28.dp)
                    ) {
                        Icon(Icons.Default.Timer, contentDescription = null, modifier = Modifier.size(12.dp))
                        Spacer(modifier = Modifier.width(3.dp))
                        Text("Stamp", fontSize = 10.sp)
                    }

                    // Delete
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete line",
                            tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Line Text Editor
            if (isEditingText) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = editText,
                        onValueChange = { editText = it },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = primaryColor
                        )
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    IconButton(
                        onClick = {
                            onUpdate(line.timestampMs, editText)
                            isEditingText = false
                        }
                    ) {
                        Icon(Icons.Default.Check, contentDescription = "Done", tint = primaryColor)
                    }
                }
            } else {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { isEditingText = true }
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = line.text.ifBlank { "(Empty lyric line - tap to edit)" },
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal
                        ),
                        color = if (line.text.isBlank()) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f) else MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f)
                    )
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

/**
 * Tab 2: Raw LRC / Text Editor View.
 */
@Composable
private fun RawTextEditorView(
    text: String,
    onTextChange: (String) -> Unit,
    isSynced: Boolean,
    onToggleSynced: (Boolean) -> Unit,
    primaryColor: Color,
    onConvertToSynced: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (isSynced) "Standard LRC Format ([mm:ss.xx] Lyrics)" else "Plain Unsynced Lyrics",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (!isSynced) {
                FilledTonalButton(
                    onClick = onConvertToSynced,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Generate Timestamps", fontSize = 11.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = text,
            onValueChange = onTextChange,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            textStyle = MaterialTheme.typography.bodySmall.copy(
                fontFamily = FontFamily.Monospace,
                fontSize = 12.sp,
                lineHeight = 18.sp
            ),
            placeholder = {
                Text(
                    text = "[00:12.34] First lyric line\n[00:18.50] Second lyric line...",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.sp
                )
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = primaryColor
            )
        )
    }
}

/**
 * Tab 3: Online Lyrics Fetching View.
 */
@Composable
private fun FetchOnlineLyricsView(
    searchArtist: String,
    searchTitle: String,
    onArtistChange: (String) -> Unit,
    onTitleChange: (String) -> Unit,
    isFetching: Boolean,
    statusMessage: String?,
    fetchedSynced: String?,
    fetchedPlain: String?,
    primaryColor: Color,
    onStartSearch: () -> Unit,
    onApplyFetched: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Search & Fetch Lyrics Online",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = "Queries LRCLIB and international synchronized lyric databases for exact timestamp matches.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = searchTitle,
            onValueChange = onTitleChange,
            label = { Text("Track Title") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = primaryColor)
        )

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
            value = searchArtist,
            onValueChange = onArtistChange,
            label = { Text("Artist Name") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = primaryColor)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onStartSearch,
            enabled = !isFetching && searchTitle.isNotBlank(),
            colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            if (isFetching) {
                CircularProgressIndicator(
                    color = Color.White,
                    modifier = Modifier.size(18.dp),
                    strokeWidth = 2.dp
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Searching Database...")
            } else {
                Icon(Icons.Default.CloudDownload, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Search & Fetch Online")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        statusMessage?.let {
            Surface(
                color = if (fetchedSynced != null || fetchedPlain != null) primaryColor.copy(alpha = 0.12f) else MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.4f),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                    color = if (fetchedSynced != null || fetchedPlain != null) primaryColor else MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(12.dp)
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        // Preview of fetched lyrics
        if (fetchedSynced != null || fetchedPlain != null) {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (fetchedSynced != null) "✓ Synchronized Lyrics Found" else "✓ Plain Lyrics Found",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = primaryColor
                        )

                        Button(
                            onClick = onApplyFetched,
                            colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Apply to Song", fontSize = 12.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = fetchedSynced ?: fetchedPlain ?: "",
                        style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}
