package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.model.LyricLine
import com.example.model.Track
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb

/**
 * Safely converts a Compose Color to an ARGB 32-bit integer packed in a Long.
 */
fun Color.toSafeArgbLong(): Long {
    return try {
        if (this == Color.Unspecified) 0xFF263859L else (this.toArgb().toLong() and 0xFFFFFFFFL)
    } catch (_: Throwable) {
        0xFF263859L
    }
}

/**
 * Safely restores a Compose Color from a stored Long (supporting both 32-bit ARGB and legacy 64-bit Color.value).
 * Always returns a valid sRGB Color.
 */
fun safeColorFromLong(value: Long, defaultArgb: Long = 0xFF263859L): Color {
    return try {
        if (value == 0L) return Color(defaultArgb.toInt())
        // If high 32 bits are 0, it's a 32-bit ARGB value (e.g. 0xFF263859L)
        if ((value ushr 32) == 0L) {
            val argb = ((value and 0xFFFFFFFFL) or 0xFF000000L).toInt()
            Color(argb)
        } else {
            // Check if legacy Compose Color.value was stored:
            // Compose Color.value sRGB has color space id 0 (lower 6 bits are 0).
            val csId = (value.toULong() and 0x3fUL).toInt()
            if (csId == 0) {
                val argb = (value ushr 32).toInt()
                Color(argb)
            } else {
                val argb = (value ushr 32).toInt()
                if (argb != 0) Color(argb) else Color(defaultArgb.toInt())
            }
        }
    } catch (_: Throwable) {
        Color(defaultArgb.toInt())
    }
}

/**
 * Room database entity caching extracted audio metadata from MediaStore or built-in tracks.
 * Enables instantaneous cold starts without rescanning file systems.
 */
@Entity(tableName = "tracks")
data class TrackEntity(
    @PrimaryKey val id: String,
    val title: String,
    val artist: String,
    val album: String,
    val durationMs: Long,
    val audioQuality: String,
    val primaryColorArgb: Long,
    val secondaryColorArgb: Long,
    val contentUri: String?,
    val contentId: Long,
    val albumArtUri: String?,
    val mimeType: String?,
    val filePath: String?,
    val sizeBytes: Long,
    val sampleRateHz: Int,
    val bitDepth: Int,
    val bitrateKbps: Int,
    val formatName: String,
    val coverResId: Int? = null,
    val lastModified: Long = System.currentTimeMillis()
)

fun TrackEntity.toTrack(lyrics: List<LyricLine> = emptyList()): Track {
    return Track(
        id = id,
        title = title,
        artist = artist,
        album = album,
        coverResId = coverResId,
        coverUrl = albumArtUri,
        durationMs = durationMs,
        audioQuality = audioQuality,
        primaryAmbientColor = safeColorFromLong(primaryColorArgb, 0xFF263859L),
        secondaryAmbientColor = safeColorFromLong(secondaryColorArgb, 0xFF0B132BL),
        contentUri = contentUri,
        contentId = contentId,
        albumArtUri = albumArtUri,
        mimeType = mimeType,
        filePath = filePath,
        sizeBytes = sizeBytes,
        lyrics = lyrics,
        sampleRateHz = sampleRateHz,
        bitDepth = bitDepth,
        bitrateKbps = bitrateKbps,
        formatName = formatName
    )
}

fun Track.toEntity(): TrackEntity {
    return TrackEntity(
        id = id,
        title = title,
        artist = artist,
        album = album,
        durationMs = durationMs,
        audioQuality = audioQuality,
        primaryColorArgb = primaryAmbientColor.toSafeArgbLong(),
        secondaryColorArgb = secondaryAmbientColor.toSafeArgbLong(),
        contentUri = contentUri,
        contentId = contentId,
        albumArtUri = albumArtUri ?: coverUrl,
        mimeType = mimeType,
        filePath = filePath,
        sizeBytes = sizeBytes,
        sampleRateHz = sampleRateHz,
        bitDepth = bitDepth,
        bitrateKbps = bitrateKbps,
        formatName = formatName,
        coverResId = coverResId
    )
}
