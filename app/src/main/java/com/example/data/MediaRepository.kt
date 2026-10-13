package com.example.data

import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.compose.ui.graphics.Color
import com.example.R
import com.example.data.local.MusicDatabase
import com.example.data.local.TrackEntity
import com.example.data.local.toEntity
import com.example.data.local.toTrack
import com.example.model.AppSettings
import com.example.model.LyricLine
import com.example.model.Track
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.io.File

class MediaRepository(private val context: Context) {

    private val musicDatabase = MusicDatabase.getInstance(context)
    private val trackDao = musicDatabase.trackDao()

    /**
     * Flow of cached tracks from Room Database for instant cold start and reactive updates.
     */
    val cachedTracksFlow: Flow<List<Track>> = trackDao.getAllTracksFlow().map { entities ->
        entities.map { entity -> entity.toTrack() }
    }

    /**
     * Synchronously loads tracks from local Room cache for instant, non-lazy startup.
     */
    fun getCachedTracksDirect(settings: AppSettings? = null): List<Track> {
        val effectiveSettings = settings ?: try { SettingsRepository(context).getSettingsFastSync() } catch (_: Throwable) { null }
        val rawTracks = try {
            val cached = trackDao.getAllTracksDirect()
            if (cached.isNotEmpty()) {
                cached.map { entity -> entity.toTrack() }
            } else {
                FastLibraryCache.getInstantTracks(context)
            }
        } catch (e: Throwable) {
            try {
                FastLibraryCache.getInstantTracks(context)
            } catch (_: Throwable) {
                emptyList()
            }
        }

        return if (effectiveSettings != null) {
            rawTracks.filter { passesTrackFilters(it, effectiveSettings) }
        } else {
            rawTracks
        }
    }

    /**
     * Instantly loads tracks from local Room cache for fast startup with whitelist/blacklist filtering.
     */
    suspend fun getCachedTracksFast(settings: AppSettings? = null): List<Track> = withContext(Dispatchers.IO) {
        val effectiveSettings = settings ?: try { SettingsRepository(context).getSettingsFastSync() } catch (_: Throwable) { null }
        val cached = trackDao.getAllTracks()
        val rawTracks = if (cached.isNotEmpty()) {
            cached.map { entity -> entity.toTrack() }
        } else {
            FastLibraryCache.getInstantTracks(context)
        }

        val filtered = if (effectiveSettings != null) {
            rawTracks.filter { passesTrackFilters(it, effectiveSettings) }
        } else {
            rawTracks
        }

        if (filtered.isNotEmpty()) {
            FastLibraryCache.saveInstantTracks(context, filtered)
        }

        filtered
    }

    val builtInTracks: List<Track> = emptyList()

    /**
     * Scans the device's MediaStore for local audio files (MP3, FLAC, WAV, AAC)
     * with strict whitelist & blacklist filtering, minimum duration exclusion,
     * and directory matching.
     */
    suspend fun scanLocalAudio(settings: AppSettings? = null): List<Track> = withContext(Dispatchers.IO) {
        val effectiveSettings = settings ?: try { SettingsRepository(context).getSettingsFastSync() } catch (_: Throwable) { null }
        val scannedList = mutableListOf<Track>()

        val uri: Uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val projectionList = mutableListOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.DATA,
            MediaStore.Audio.Media.ALBUM_ID,
            MediaStore.Audio.Media.MIME_TYPE,
            MediaStore.Audio.Media.SIZE,
            MediaStore.Audio.Media.DATE_ADDED
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            projectionList.add(MediaStore.Audio.Media.RELATIVE_PATH)
            projectionList.add(MediaStore.Audio.Media.DISPLAY_NAME)
        }
        val projection = projectionList.toTypedArray()

        val minDurationMs = ((effectiveSettings?.minDurationSec ?: 30) * 1000).coerceAtLeast(3000)
        val selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0 AND ${MediaStore.Audio.Media.DURATION} >= $minDurationMs"
        val sortOrder = "${MediaStore.Audio.Media.TITLE} ASC"

        try {
            val cursor = context.contentResolver.query(
                uri,
                projection,
                selection,
                null,
                sortOrder
            )

            cursor?.use { c ->
                val idCol = c.getColumnIndex(MediaStore.Audio.Media._ID)
                val titleCol = c.getColumnIndex(MediaStore.Audio.Media.TITLE)
                val artistCol = c.getColumnIndex(MediaStore.Audio.Media.ARTIST)
                val albumCol = c.getColumnIndex(MediaStore.Audio.Media.ALBUM)
                val durationCol = c.getColumnIndex(MediaStore.Audio.Media.DURATION)
                val dataCol = c.getColumnIndex(MediaStore.Audio.Media.DATA)
                val albumIdCol = c.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID)
                val mimeCol = c.getColumnIndex(MediaStore.Audio.Media.MIME_TYPE)
                val sizeCol = c.getColumnIndex(MediaStore.Audio.Media.SIZE)
                val dateAddedCol = c.getColumnIndex(MediaStore.Audio.Media.DATE_ADDED)
                val relPathCol = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) c.getColumnIndex(MediaStore.Audio.Media.RELATIVE_PATH) else -1
                val dispNameCol = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) c.getColumnIndex(MediaStore.Audio.Media.DISPLAY_NAME) else -1

                if (idCol < 0) return@use

                while (c.moveToNext()) {
                    val id = c.getLong(idCol)
                    val title = if (titleCol >= 0) c.getString(titleCol) ?: "Unknown Title" else "Unknown Title"
                    val artist = if (artistCol >= 0) {
                        c.getString(artistCol).takeIf { !it.isNullOrBlank() && it != "<unknown>" } ?: "Unknown Artist"
                    } else "Unknown Artist"
                    val album = if (albumCol >= 0) {
                        c.getString(albumCol).takeIf { !it.isNullOrBlank() && it != "<unknown>" } ?: "Unknown Album"
                    } else "Unknown Album"
                    val duration = if (durationCol >= 0) c.getLong(durationCol) else 0L
                    val rawFilePath = if (dataCol >= 0) c.getString(dataCol) else null
                    val relativePath = if (relPathCol >= 0) c.getString(relPathCol) else null
                    val displayName = if (dispNameCol >= 0) c.getString(dispNameCol) else null

                    val filePath = when {
                        !rawFilePath.isNullOrBlank() -> rawFilePath
                        !relativePath.isNullOrBlank() && !displayName.isNullOrBlank() -> "/storage/emulated/0/$relativePath$displayName"
                        !relativePath.isNullOrBlank() -> "/storage/emulated/0/$relativePath$title"
                        else -> null
                    }

                    val albumId = if (albumIdCol >= 0) c.getLong(albumIdCol) else -1L
                    val mime = if (mimeCol >= 0) c.getString(mimeCol) ?: "audio/mpeg" else "audio/mpeg"
                    val size = if (sizeCol >= 0) c.getLong(sizeCol) else 0L

                    // Duration Filter
                    if (settings != null && settings.minDurationSec > 0 && duration < settings.minDurationSec * 1000L) {
                        continue
                    }

                    val contentUri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id).toString()
                    val albumArtUri = if (albumId > 0) {
                        ContentUris.withAppendedId(
                            Uri.parse("content://media/external/audio/albumart"),
                            albumId
                        ).toString()
                    } else null

                    val isFlac = mime.contains("flac", ignoreCase = true) || filePath?.endsWith(".flac", ignoreCase = true) == true
                    val isWav = mime.contains("wav", ignoreCase = true) || filePath?.endsWith(".wav", ignoreCase = true) == true
                    val isAac = mime.contains("aac", ignoreCase = true) || mime.contains("m4a", ignoreCase = true) || filePath?.endsWith(".m4a", ignoreCase = true) == true
                    val isOgg = mime.contains("ogg", ignoreCase = true) || filePath?.endsWith(".ogg", ignoreCase = true) == true

                    val formatName = when {
                        isFlac -> "FLAC"
                        isWav -> "WAV"
                        isAac -> "AAC"
                        isOgg -> "OGG"
                        else -> "MP3"
                    }

                    val sampleRateHz = when {
                        isFlac -> 96000
                        isWav -> 96000
                        else -> 44100
                    }

                    val bitDepth = when {
                        isFlac || isWav -> 24
                        else -> 16
                    }

                    val calculatedBitrate = if (duration > 0 && size > 0) {
                        ((size * 8L) / (duration / 1000L) / 1000L).toInt().coerceIn(64, 9216)
                    } else {
                        if (isFlac) 1411 else 320
                    }

                    val qualityBadge = when {
                        isFlac -> "FLAC $bitDepth-bit / ${sampleRateHz / 1000}kHz"
                        isWav -> "WAV Lossless $bitDepth-bit / ${sampleRateHz / 1000}kHz"
                        isAac -> "AAC ${calculatedBitrate}kbps"
                        else -> "MP3 ${calculatedBitrate}kbps"
                    }

                    val hash = (title + artist).hashCode()
                    val primaryColor = getHarmonicColor(hash)
                    val secondaryColor = getDarkerVariant(primaryColor)

                    scannedList.add(
                        Track(
                            id = "local_$id",
                            title = title,
                            artist = artist,
                            album = album,
                            coverUrl = albumArtUri,
                            coverResId = null,
                            durationMs = duration,
                            audioQuality = qualityBadge,
                            primaryAmbientColor = primaryColor,
                            secondaryAmbientColor = secondaryColor,
                            contentUri = contentUri,
                            contentId = id,
                            albumArtUri = albumArtUri,
                            mimeType = mime,
                            filePath = filePath,
                            sizeBytes = size,
                            lyrics = emptyList(),
                            sampleRateHz = sampleRateHz,
                            bitDepth = bitDepth,
                            bitrateKbps = calculatedBitrate,
                            formatName = formatName,
                            dateAdded = if (dateAddedCol >= 0) c.getLong(dateAddedCol) else id
                        )
                    )
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        val combined = scannedList.filter { passesTrackFilters(it, settings) }

        try {
            val entitiesToInsert = combined.map { it.toEntity() }
            trackDao.syncLibrary(entitiesToInsert)
            FastLibraryCache.saveInstantTracks(context, combined)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        combined
    }

    /**
     * Normalizes a folder path, tree URI, or file pattern into a clean relative subpath.
     */
    fun normalizeFilterPattern(pattern: String): String {
        var p = pattern.trim().lowercase().replace('\\', '/')
        if (p.startsWith("content://") || p.startsWith("file://")) {
            val decoded = Uri.decode(p)
            val idx = decoded.lastIndexOf(":")
            p = if (idx != -1 && idx + 1 < decoded.length) {
                decoded.substring(idx + 1)
            } else {
                decoded.substringAfterLast("/")
            }
        } else if (p.contains(":")) {
            p = p.substringAfterLast(":")
        }

        // Strip common storage mount root prefixes
        p = p.removePrefix("/storage/emulated/0/")
            .removePrefix("storage/emulated/0/")
            .removePrefix("/sdcard/")
            .removePrefix("sdcard/")
            .removePrefix("/storage/")
            .removePrefix("storage/")
            .trim('/')

        return p
    }

    /**
     * Strictly verifies whether a track's file location / storage directory matches a folder or file pattern.
     * Note: Does NOT match against song title or artist metadata to prevent false positive inclusions.
     */
    fun matchesFolderOrFilePattern(track: Track, pattern: String): Boolean {
        val raw = pattern.trim()
        if (raw.isBlank()) return false

        val normalized = normalizeFilterPattern(raw).lowercase()
        if (normalized.isBlank()) return false

        val rawFilePath = track.filePath?.lowercase()?.replace('\\', '/') ?: ""
        val cleanFilePath = rawFilePath
            .removePrefix("/storage/emulated/0/")
            .removePrefix("storage/emulated/0/")
            .removePrefix("/sdcard/")
            .removePrefix("sdcard/")
            .removePrefix("/storage/")
            .removePrefix("storage/")
            .trim('/')

        val fileName = if (cleanFilePath.isNotEmpty()) cleanFilePath.substringAfterLast('/') else ""
        val dirPath = if (cleanFilePath.isNotEmpty()) cleanFilePath.substringBeforeLast('/', "") else ""
        val dirSegments = if (dirPath.isNotEmpty()) dirPath.split('/') else emptyList()

        // 1. Exact file name match (e.g., "song.mp3", "track.flac")
        if (fileName.isNotEmpty()) {
            if (fileName == normalized ||
                fileName.substringBeforeLast('.', "") == normalized ||
                (normalized.contains('.') && fileName.endsWith(normalized))
            ) {
                return true
            }
        }

        // 2. Exact directory path / subpath match
        if (dirPath.isNotEmpty()) {
            if (dirPath == normalized ||
                dirPath.startsWith("$normalized/") ||
                dirPath.endsWith("/$normalized") ||
                dirPath.contains("/$normalized/") ||
                dirSegments.any { it == normalized }
            ) {
                return true
            }
        }

        // 3. Match against decoded contentUri if cleanFilePath is empty
        val contentUri = track.contentUri?.lowercase() ?: ""
        if (cleanFilePath.isEmpty() && contentUri.isNotEmpty()) {
            val decoded = Uri.decode(contentUri)
            if (decoded.contains(normalized)) {
                return true
            }
        }

        return false
    }

    /**
     * Verifies if a track satisfies duration, Whitelist, and Blacklist constraints.
     */
    fun passesTrackFilters(track: Track, settings: AppSettings?): Boolean {
        if (settings == null) return true

        // 1. Min Duration Check
        if (settings.minDurationSec > 0 && track.durationMs < settings.minDurationSec * 1000L) {
            return false
        }

        // 2. Blacklist check (strictly excludes if any directory or file matches blacklisted folders)
        for (blacklisted in settings.blacklistedFolders) {
            if (matchesFolderOrFilePattern(track, blacklisted)) {
                return false
            }
        }

        // 3. Whitelist check (if active, requires at least one folder/file match)
        val isWhitelistActive = (settings.useWhitelistOnly || settings.whitelistedFolders.isNotEmpty()) && settings.whitelistedFolders.isNotEmpty()
        if (isWhitelistActive) {
            val matchesAnyWhitelist = settings.whitelistedFolders.any { matchesFolderOrFilePattern(track, it) }
            if (!matchesAnyWhitelist) {
                return false
            }
        }

        return true
    }

    suspend fun deleteTrack(track: Track): Boolean = withContext(Dispatchers.IO) {
        try {
            trackDao.deleteTrackById(track.id)
            if (track.filePath != null) {
                val file = File(track.filePath)
                if (file.exists()) file.delete()
            }
            if (track.contentUri != null) {
                try {
                    context.contentResolver.delete(Uri.parse(track.contentUri), null, null)
                } catch (_: Exception) {}
            }
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun updateTrackMetadata(trackId: String, newTitle: String, newArtist: String, newAlbum: String) = withContext(Dispatchers.IO) {
        try {
            trackDao.updateTrackMetadata(trackId, newTitle, newArtist, newAlbum)
        } catch (_: Exception) {}
    }

    companion object {
        fun getHarmonicColor(seed: Int): Color {
            val palettes = listOf(
                Color(0xFF6366F1), // Electric Indigo
                Color(0xFFEC4899), // Pink Punch
                Color(0xFF06B6D4), // Cyan Glow
                Color(0xFF8B5CF6), // Deep Violet
                Color(0xFFF59E0B), // Warm Amber
                Color(0xFF10B981), // Emerald Pulse
                Color(0xFFF43F5E), // Rose Neon
                Color(0xFF3B82F6), // Azure Blue
                Color(0xFF14B8A6), // Teal Vivid
                Color(0xFFA855F7), // Purple Radiant
                Color(0xFFF97316), // Sunset Orange
                Color(0xFF0EA5E9), // Sky Blue
                Color(0xFFD946EF), // Fuchsia Spark
                Color(0xFF84CC16), // Lime Glow
                Color(0xFFE11D48), // Crimson Vibrant
                Color(0xFF2DD4BF)  // Mint Cyber
            )
            val index = (seed and 0x7FFFFFFF) % palettes.size
            return palettes[index]
        }

        fun getDarkerVariant(color: Color): Color {
            return Color(
                red = color.red * 0.35f,
                green = color.green * 0.35f,
                blue = color.blue * 0.35f,
                alpha = 1f
            )
        }
    }
}
