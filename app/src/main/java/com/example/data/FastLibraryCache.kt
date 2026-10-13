package com.example.data

import android.content.Context
import androidx.compose.ui.graphics.Color
import com.example.data.local.safeColorFromLong
import com.example.data.local.toSafeArgbLong
import com.example.model.Track
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

/**
 * Ultra-fast zero-delay memory and disk cache for instant startup.
 * Ensures the Home screen and player queue are instantly populated on frame 0 with zero loading delay.
 */
object FastLibraryCache {

    private val lock = Any()
    @Volatile
    private var memoryCachedTracks: List<Track>? = null

    private fun getCacheFile(context: Context): File {
        return File(context.filesDir, "fast_library_cache.json")
    }

    fun getInstantTracks(context: Context): List<Track> {
        memoryCachedTracks?.let { if (it.isNotEmpty()) return it }

        synchronized(lock) {
            memoryCachedTracks?.let { if (it.isNotEmpty()) return it }

            val file = getCacheFile(context)
            if (!file.exists() || file.length() == 0L) {
                return emptyList()
            }

            try {
                val jsonStr = file.readText()
                val jsonArray = JSONArray(jsonStr)
                val list = ArrayList<Track>(jsonArray.length())

                for (i in 0 until jsonArray.length()) {
                    val obj = jsonArray.getJSONObject(i)
                    val primaryHex = obj.optLong("primaryAmbientColor", 0xFF263859L)
                    val secondaryHex = obj.optLong("secondaryAmbientColor", 0xFF0B132BL)

                    list.add(
                        Track(
                            id = obj.optString("id"),
                            title = obj.optString("title"),
                            artist = obj.optString("artist"),
                            album = obj.optString("album"),
                            coverUrl = obj.optString("coverUrl").takeIf { it.isNotBlank() },
                            coverResId = if (obj.has("coverResId")) obj.getInt("coverResId") else null,
                            durationMs = obj.optLong("durationMs", 0L),
                            audioQuality = obj.optString("audioQuality", "FLAC 24-bit / 96kHz"),
                            primaryAmbientColor = safeColorFromLong(primaryHex, 0xFF263859L),
                            secondaryAmbientColor = safeColorFromLong(secondaryHex, 0xFF0B132BL),
                            contentUri = obj.optString("contentUri").takeIf { it.isNotBlank() },
                            contentId = obj.optLong("contentId", 0L),
                            albumArtUri = obj.optString("albumArtUri").takeIf { it.isNotBlank() },
                            mimeType = obj.optString("mimeType").takeIf { it.isNotBlank() },
                            filePath = obj.optString("filePath").takeIf { it.isNotBlank() },
                            sizeBytes = obj.optLong("sizeBytes", 0L),
                            sampleRateHz = obj.optInt("sampleRateHz", 44100),
                            bitDepth = obj.optInt("bitDepth", 16),
                            bitrateKbps = obj.optInt("bitrateKbps", 320),
                            formatName = obj.optString("formatName", "FLAC"),
                            dateAdded = obj.optLong("dateAdded", 0L)
                        )
                    )
                }
                memoryCachedTracks = list
                return list
            } catch (e: Exception) {
                return emptyList()
            }
        }
    }

    fun saveInstantTracks(context: Context, tracks: List<Track>) {
        if (tracks.isEmpty()) return
        memoryCachedTracks = tracks

        try {
            val jsonArray = JSONArray()
            // Cap instant startup cache to top 250 tracks to keep memory & startup file lightweight
            for (track in tracks.take(250)) {
                val obj = JSONObject().apply {
                    put("id", track.id)
                    put("title", track.title)
                    put("artist", track.artist)
                    put("album", track.album)
                    track.coverUrl?.let { put("coverUrl", it) }
                    track.coverResId?.let { put("coverResId", it) }
                    put("durationMs", track.durationMs)
                    put("audioQuality", track.audioQuality)
                    put("primaryAmbientColor", track.primaryAmbientColor.toSafeArgbLong())
                    put("secondaryAmbientColor", track.secondaryAmbientColor.toSafeArgbLong())
                    track.contentUri?.let { put("contentUri", it) }
                    put("contentId", track.contentId)
                    track.albumArtUri?.let { put("albumArtUri", it) }
                    track.mimeType?.let { put("mimeType", it) }
                    track.filePath?.let { put("filePath", it) }
                    put("sizeBytes", track.sizeBytes)
                    put("sampleRateHz", track.sampleRateHz)
                    put("bitDepth", track.bitDepth)
                    put("bitrateKbps", track.bitrateKbps)
                    put("formatName", track.formatName)
                    put("dateAdded", track.dateAdded)
                }
                jsonArray.put(obj)
            }

            val file = getCacheFile(context)
            val tempFile = File(context.filesDir, "fast_library_cache.json.tmp")
            tempFile.writeText(jsonArray.toString())
            if (tempFile.exists()) {
                tempFile.renameTo(file)
            }
        } catch (e: Exception) {
            // Ignore cache write error
        }
    }
}
