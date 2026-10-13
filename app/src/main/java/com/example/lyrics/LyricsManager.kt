package com.example.lyrics

import android.content.Context
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.util.Log
import com.example.model.LyricLine
import com.example.model.LyricsUiState
import com.example.model.Track
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.RandomAccessFile
import java.net.URLEncoder
import java.nio.charset.Charset
import java.text.Normalizer
import java.util.concurrent.TimeUnit
import java.util.regex.Pattern

import com.example.data.local.CustomLyricsEntity
import com.example.data.local.MusicDatabase

/**
 * Advanced Multi-Language Lyrics Manager:
 * 1. User-customized & locally edited lyrics from Room DB
 * 2. Companion Local .lrc File (UTF-8, UTF-16, GBK, Shift-JIS, EUC-KR, Windows-125x)
 * 3. Embedded Tag Scanning (USLT, SYLT, LYRICS= with multi-charset auto-detection)
 * 4. Multi-tier LRCLIB API Integration:
 *    - Tier 1: Exact match query (/api/get)
 *    - Tier 2: Track & Artist search (/api/search?track_name=...&artist_name=...)
 *    - Tier 3: Multi-language query search (/api/search?q=...)
 *    - Tier 4: Romanized / De-accented normalization fallback
 * 5. Multi-language character support across English, Hindi, Japanese, Korean, Chinese, Arabic, Spanish, etc.
 */
class LyricsManager(private val context: Context) {

    private val customLyricsDao = MusicDatabase.getInstance(context).customLyricsDao()

    private val lyricsCacheDir by lazy {
        File(context.filesDir, "lyrics_cache").apply {
            if (!exists()) mkdirs()
        }
    }

    // In-memory LRU cache for 64 recently accessed parsed lyrics
    private val memoryCache: MutableMap<String, LyricsUiState.Success> = java.util.Collections.synchronizedMap(
        object : java.util.LinkedHashMap<String, LyricsUiState.Success>(32, 0.75f, true) {
            override fun removeEldestEntry(eldest: MutableMap.MutableEntry<String, LyricsUiState.Success>?): Boolean {
                return size > 64
            }
        }
    )

    private fun normalizeKey(artist: String, title: String): String {
        val raw = "${artist.trim().lowercase()}_${title.trim().lowercase()}"
        return raw.replace(Regex("[^a-zA-Z0-9_\\-]"), "_").take(100)
    }

    private suspend fun cacheLyricsLocally(
        track: Track,
        synced: String?,
        plain: String?,
        isSynced: Boolean
    ) = withContext(Dispatchers.IO) {
        val content = if (isSynced && !synced.isNullOrBlank()) synced else plain ?: ""
        if (content.isBlank()) return@withContext

        // 1. Save to Room database
        try {
            if (track.id.isNotBlank()) {
                customLyricsDao.insertCustomLyrics(
                    CustomLyricsEntity(
                        trackId = track.id,
                        lrcContent = content,
                        plainText = plain,
                        isSynced = isSynced,
                        updatedAt = System.currentTimeMillis()
                    )
                )
            }
            if (track.artist.isNotBlank() && track.title.isNotBlank()) {
                val artistTitleKey = "art_title:${normalizeKey(track.artist, track.title)}"
                customLyricsDao.insertCustomLyrics(
                    CustomLyricsEntity(
                        trackId = artistTitleKey,
                        lrcContent = content,
                        plainText = plain,
                        isSynced = isSynced,
                        updatedAt = System.currentTimeMillis()
                    )
                )
            }
        } catch (e: Exception) {
            Log.w("LyricsManager", "Failed to cache lyrics in DB: ${e.message}")
        }

        // 2. Save to internal disk file cache
        try {
            if (track.id.isNotBlank()) {
                File(lyricsCacheDir, "${track.id}.lrc").writeText(content, Charsets.UTF_8)
            }
            if (track.artist.isNotBlank() && track.title.isNotBlank()) {
                val normKey = normalizeKey(track.artist, track.title)
                File(lyricsCacheDir, "$normKey.lrc").writeText(content, Charsets.UTF_8)
            }
        } catch (e: Exception) {
            Log.w("LyricsManager", "Failed to write disk cache file: ${e.message}")
        }

        // 3. Companion .lrc file next to track if possible
        if (!track.filePath.isNullOrBlank() && isSynced) {
            try {
                val lrcFile = File(track.filePath.substringBeforeLast(".") + ".lrc")
                if (!lrcFile.exists() && lrcFile.parentFile?.canWrite() == true) {
                    lrcFile.writeText(content, Charsets.UTF_8)
                    Log.d("LyricsManager", "Saved companion LRC file: ${lrcFile.absolutePath}")
                }
            } catch (e: Exception) {
                // Ignore companion write if storage is read-only
            }
        }
    }

    companion object {
        private val sharedHttpClient = OkHttpClient.Builder()
            .connectTimeout(6, TimeUnit.SECONDS)
            .readTimeout(8, TimeUnit.SECONDS)
            .build()
    }

    private val httpClient get() = sharedHttpClient

    suspend fun fetchLyricsForTrack(track: Track): LyricsUiState = withContext(Dispatchers.IO) {
        val memKey = if (track.id.isNotBlank()) track.id else normalizeKey(track.artist, track.title)
        val normKey = normalizeKey(track.artist, track.title)

        // Tier 0: Fast In-Memory Cache
        memoryCache[memKey]?.let {
            return@withContext it.copy(track = track)
        }
        if (normKey.isNotBlank()) {
            memoryCache[normKey]?.let {
                return@withContext it.copy(track = track)
            }
        }

        // Tier 1: Check user-edited or saved cached lyrics in Room DB
        try {
            var custom = if (track.id.isNotBlank()) customLyricsDao.getLyricsForTrack(track.id) else null
            if (custom == null && normKey.isNotBlank()) {
                custom = customLyricsDao.getLyricsForTrack("art_title:$normKey")
            }
            if (custom != null) {
                if (custom.isSynced) {
                    val parsed = LyricsParser.parseLrc(custom.lrcContent)
                    if (parsed.isNotEmpty()) {
                        val state = LyricsUiState.Success(
                            track = track,
                            lines = parsed,
                            isSynced = true,
                            plainLyrics = custom.plainText
                        )
                        memoryCache[memKey] = state
                        return@withContext state
                    }
                } else if (!custom.plainText.isNullOrBlank() || custom.lrcContent.isNotBlank()) {
                    val state = LyricsUiState.Success(
                        track = track,
                        lines = emptyList(),
                        isSynced = false,
                        plainLyrics = custom.plainText ?: custom.lrcContent
                    )
                    memoryCache[memKey] = state
                    return@withContext state
                }
            }
        } catch (e: Exception) {
            Log.e("LyricsManager", "Failed checking custom lyrics: ${e.message}")
        }

        // Tier 2: Check internal disk file cache
        try {
            val fileById = if (track.id.isNotBlank()) File(lyricsCacheDir, "${track.id}.lrc") else null
            val fileByName = if (normKey.isNotBlank()) File(lyricsCacheDir, "$normKey.lrc") else null
            val cachedFile = when {
                fileById != null && fileById.exists() && fileById.canRead() -> fileById
                fileByName != null && fileByName.exists() && fileByName.canRead() -> fileByName
                else -> null
            }
            if (cachedFile != null) {
                val content = cachedFile.readText(Charsets.UTF_8)
                val parsed = LyricsParser.parseLrc(content)
                if (parsed.isNotEmpty()) {
                    val state = LyricsUiState.Success(
                        track = track,
                        lines = parsed,
                        isSynced = true
                    )
                    memoryCache[memKey] = state
                    return@withContext state
                } else if (content.isNotBlank()) {
                    val state = LyricsUiState.Success(
                        track = track,
                        lines = emptyList(),
                        isSynced = false,
                        plainLyrics = content
                    )
                    memoryCache[memKey] = state
                    return@withContext state
                }
            }
        } catch (e: Exception) {
            Log.d("LyricsManager", "Disk cache read error: ${e.message}")
        }

        // Tier 3: Check companion local .lrc file (e.g. song.mp3 -> song.lrc)
        if (!track.filePath.isNullOrBlank()) {
            val lrcFile = File(track.filePath.substringBeforeLast(".") + ".lrc")
            if (lrcFile.exists() && lrcFile.canRead()) {
                try {
                    val bytes = lrcFile.readBytes()
                    val parsedLines = LyricsParser.parseLrcBytes(bytes)
                    if (parsedLines.isNotEmpty()) {
                        val state = LyricsUiState.Success(
                            track = track,
                            lines = parsedLines,
                            isSynced = true
                        )
                        memoryCache[memKey] = state
                        return@withContext state
                    }
                } catch (e: Exception) {
                    Log.d("LyricsManager", "Companion LRC parse error: ${e.message}")
                }
            }
        }

        // Tier 4: Local embedded tags extraction (USLT, SYLT, Vorbis comments)
        val embeddedLyrics = readEmbeddedLyrics(track)
        if (!embeddedLyrics.isNullOrBlank()) {
            val parsedLines = LyricsParser.parseLrc(embeddedLyrics)
            if (parsedLines.isNotEmpty()) {
                val state = LyricsUiState.Success(
                    track = track,
                    lines = parsedLines,
                    isSynced = true
                )
                memoryCache[memKey] = state
                return@withContext state
            } else {
                val state = LyricsUiState.Success(
                    track = track,
                    lines = emptyList(),
                    isSynced = false,
                    plainLyrics = embeddedLyrics
                )
                memoryCache[memKey] = state
                return@withContext state
            }
        }

        // Tier 5: Check if track has bundled sample lyrics (e.g. demo tracks)
        if (track.lyrics.isNotEmpty()) {
            val state = LyricsUiState.Success(
                track = track,
                lines = track.lyrics,
                isSynced = true
            )
            memoryCache[memKey] = state
            return@withContext state
        }

        // Tier 6: Multi-tier multi-language search from LRCLIB API through internet
        val lrclibResult = fetchFromLrclibMultiTier(track.artist, track.title, track.album, track.durationMs)
        if (lrclibResult != null) {
            if (!lrclibResult.syncedLyrics.isNullOrBlank()) {
                val parsed = LyricsParser.parseLrc(lrclibResult.syncedLyrics)
                if (parsed.isNotEmpty()) {
                    // CACHE SYNCED LYRICS PERMANENTLY
                    cacheLyricsLocally(
                        track = track,
                        synced = lrclibResult.syncedLyrics,
                        plain = lrclibResult.plainLyrics,
                        isSynced = true
                    )
                    val state = LyricsUiState.Success(
                        track = track,
                        lines = parsed,
                        isSynced = true,
                        plainLyrics = lrclibResult.plainLyrics
                    )
                    memoryCache[memKey] = state
                    return@withContext state
                }
            }
            if (!lrclibResult.plainLyrics.isNullOrBlank()) {
                // CACHE PLAIN LYRICS PERMANENTLY
                cacheLyricsLocally(
                    track = track,
                    synced = null,
                    plain = lrclibResult.plainLyrics,
                    isSynced = false
                )
                val state = LyricsUiState.Success(
                    track = track,
                    lines = emptyList(),
                    isSynced = false,
                    plainLyrics = lrclibResult.plainLyrics
                )
                memoryCache[memKey] = state
                return@withContext state
            }
        }

        // Not found state
        return@withContext LyricsUiState.NotFound("Lyrics Not Available")
    }

    /**
     * Multi-tier fallback sequence for fetching lyrics across various languages:
     * Tier 1: Exact GET endpoint
     * Tier 2: Search endpoint with track_name and artist_name
     * Tier 3: Search endpoint with full query q
     * Tier 4: Cleaned / Romanized normalized query search
     */
    private fun fetchFromLrclibMultiTier(
        artist: String,
        title: String,
        album: String,
        durationMs: Long
    ): LrclibResponse? {
        val cleanArtist = cleanQuery(artist)
        val cleanTitle = cleanQuery(title)
        val cleanAlbum = cleanQuery(album)
        val durationSec = (durationMs / 1000).toInt()

        // Tier 1: Exact GET endpoint
        val tier1 = tryFetchExact(cleanArtist, cleanTitle, cleanAlbum, durationSec)
        if (tier1 != null && (tier1.syncedLyrics != null || tier1.plainLyrics != null)) {
            return tier1
        }

        // Tier 2: Search endpoint with track_name & artist_name
        val tier2 = tryFetchSearchByTrackAndArtist(cleanTitle, cleanArtist)
        if (tier2 != null && (tier2.syncedLyrics != null || tier2.plainLyrics != null)) {
            return tier2
        }

        // Tier 3: Search endpoint with combined query (works great for multi-language & regional songs)
        val tier3 = tryFetchSearchGeneral("$cleanTitle $cleanArtist")
        if (tier3 != null && (tier3.syncedLyrics != null || tier3.plainLyrics != null)) {
            return tier3
        }

        // Tier 4: De-accented / Normalized Romanized search
        val normTitle = normalizeText(cleanTitle)
        val normArtist = normalizeText(cleanArtist)
        if (normTitle != cleanTitle || normArtist != cleanArtist) {
            val tier4 = tryFetchSearchGeneral("$normTitle $normArtist")
            if (tier4 != null && (tier4.syncedLyrics != null || tier4.plainLyrics != null)) {
                return tier4
            }
        }

        return null
    }

    private fun tryFetchExact(artist: String, title: String, album: String, durationSec: Int): LrclibResponse? {
        return try {
            val encodedArtist = URLEncoder.encode(artist, "UTF-8")
            val encodedTitle = URLEncoder.encode(title, "UTF-8")
            val encodedAlbum = URLEncoder.encode(album, "UTF-8")

            var url = "https://lrclib.net/api/get?artist_name=$encodedArtist&track_name=$encodedTitle"
            if (album.isNotBlank()) url += "&album_name=$encodedAlbum"
            if (durationSec > 0) url += "&duration=$durationSec"

            executeLrclibRequest(url)
        } catch (_: Exception) {
            null
        }
    }

    private fun tryFetchSearchByTrackAndArtist(title: String, artist: String): LrclibResponse? {
        return try {
            val encodedTitle = URLEncoder.encode(title, "UTF-8")
            val encodedArtist = URLEncoder.encode(artist, "UTF-8")
            val url = "https://lrclib.net/api/search?track_name=$encodedTitle&artist_name=$encodedArtist"
            executeLrclibSearchRequest(url)
        } catch (_: Exception) {
            null
        }
    }

    private fun tryFetchSearchGeneral(query: String): LrclibResponse? {
        return try {
            val encodedQuery = URLEncoder.encode(query.trim(), "UTF-8")
            val url = "https://lrclib.net/api/search?q=$encodedQuery"
            executeLrclibSearchRequest(url)
        } catch (_: Exception) {
            null
        }
    }

    private fun executeLrclibRequest(url: String): LrclibResponse? {
        val request = Request.Builder()
            .url(url)
            .header("User-Agent", "PixelPlayer-Android/1.0 (High-Fidelity Audio)")
            .build()

        return httpClient.newCall(request).execute().use { response ->
            if (response.isSuccessful) {
                val body = response.body?.string() ?: return null
                val json = JSONObject(body)
                val synced = json.optString("syncedLyrics").takeIf { it.isNotBlank() && it != "null" }
                val plain = json.optString("plainLyrics").takeIf { it.isNotBlank() && it != "null" }
                LrclibResponse(syncedLyrics = synced, plainLyrics = plain)
            } else {
                null
            }
        }
    }

    private fun executeLrclibSearchRequest(url: String): LrclibResponse? {
        val request = Request.Builder()
            .url(url)
            .header("User-Agent", "PixelPlayer-Android/1.0 (High-Fidelity Audio)")
            .build()

        return httpClient.newCall(request).execute().use { response ->
            if (response.isSuccessful) {
                val body = response.body?.string() ?: return null
                val jsonArray = JSONArray(body)
                if (jsonArray.length() == 0) return null

                // Prefer synced lyrics first from the results
                var fallbackPlain: String? = null
                for (i in 0 until minOf(jsonArray.length(), 6)) {
                    val item = jsonArray.getJSONObject(i)
                    val synced = item.optString("syncedLyrics").takeIf { it.isNotBlank() && it != "null" }
                    val plain = item.optString("plainLyrics").takeIf { it.isNotBlank() && it != "null" }

                    if (synced != null) {
                        return LrclibResponse(syncedLyrics = synced, plainLyrics = plain)
                    }
                    if (fallbackPlain == null && plain != null) {
                        fallbackPlain = plain
                    }
                }

                if (fallbackPlain != null) {
                    LrclibResponse(syncedLyrics = null, plainLyrics = fallbackPlain)
                } else {
                    null
                }
            } else {
                null
            }
        }
    }

    /**
     * Reads embedded audio tags looking for USLT, SYLT, or Vorbis comments.
     */
    private fun readEmbeddedLyrics(track: Track): String? {
        if (!track.filePath.isNullOrBlank()) {
            val file = File(track.filePath)
            if (file.exists() && file.canRead()) {
                try {
                    RandomAccessFile(file, "r").use { raf ->
                        val buffer = ByteArray(minOf(raf.length(), 65536L).toInt())
                        raf.readFully(buffer)
                        val text = String(buffer, Charset.forName("ISO-8859-1"))

                        val usltIndex = text.indexOf("USLT")
                        if (usltIndex != -1 && usltIndex + 10 < buffer.size) {
                            val frameSlice = buffer.copyOfRange(usltIndex + 10, minOf(usltIndex + 8192, buffer.size))
                            val decoded = LyricsParser.decodeId3UsltFrame(frameSlice)
                            val cleaned = LyricsParser.sanitizeText(decoded)
                            if (cleaned.length > 10) return cleaned
                        }

                        val lyricsTagIndex = text.indexOf("LYRICS=")
                        if (lyricsTagIndex != -1) {
                            val sub = text.substring(lyricsTagIndex + 7, minOf(lyricsTagIndex + 8192, text.length))
                            val endIdx = sub.indexOfAny(charArrayOf('\u0000', '\u0001', '\u0002'))
                            val raw = if (endIdx != -1) sub.substring(0, endIdx) else sub
                            val cleaned = LyricsParser.sanitizeText(raw)
                            if (cleaned.isNotBlank()) return cleaned
                        }
                    }
                } catch (e: Exception) {
                    Log.d("LyricsManager", "Direct tag read failed: ${e.message}")
                }
            }
        }

        return null
    }

    private fun cleanQuery(text: String): String {
        return text
            .replace(Regex("\\(.*?\\)"), "")
            .replace(Regex("\\[.*?\\]"), "")
            .replace(Regex("feat\\..*|ft\\..*", RegexOption.IGNORE_CASE), "")
            .replace(Regex("official.*|video.*|audio.*", RegexOption.IGNORE_CASE), "")
            .trim()
    }

    private fun normalizeText(text: String): String {
        return Normalizer.normalize(text, Normalizer.Form.NFD)
            .replace(Regex("\\p{InCombiningDiacriticalMarks}+"), "")
            .trim()
    }

    private data class LrclibResponse(
        val syncedLyrics: String?,
        val plainLyrics: String?
    )

    suspend fun saveCustomLyrics(
        trackId: String,
        lrcContent: String,
        plainText: String?,
        isSynced: Boolean
    ) = withContext(Dispatchers.IO) {
        customLyricsDao.insertCustomLyrics(
            CustomLyricsEntity(
                trackId = trackId,
                lrcContent = lrcContent,
                plainText = plainText,
                isSynced = isSynced,
                updatedAt = System.currentTimeMillis()
            )
        )
        // Invalidate and refresh cache
        memoryCache.remove(trackId)
        try {
            if (trackId.isNotBlank()) {
                File(lyricsCacheDir, "$trackId.lrc").writeText(lrcContent, Charsets.UTF_8)
            }
        } catch (_: Exception) {}
    }

    suspend fun deleteCustomLyrics(trackId: String) = withContext(Dispatchers.IO) {
        customLyricsDao.deleteLyricsForTrack(trackId)
        memoryCache.remove(trackId)
        try {
            if (trackId.isNotBlank()) {
                File(lyricsCacheDir, "$trackId.lrc").delete()
            }
        } catch (_: Exception) {}
    }

    suspend fun getCustomLyrics(trackId: String): CustomLyricsEntity? = withContext(Dispatchers.IO) {
        customLyricsDao.getLyricsForTrack(trackId)
    }

    suspend fun searchOnlineLyrics(
        artist: String,
        title: String,
        album: String = "",
        durationMs: Long = 0L
    ): Pair<String?, String?> = withContext(Dispatchers.IO) {
        val normKey = "art_title:${normalizeKey(artist, title)}"
        // Check cache first before querying internet
        try {
            val cached = customLyricsDao.getLyricsForTrack(normKey)
            if (cached != null) {
                return@withContext Pair(
                    if (cached.isSynced) cached.lrcContent else null,
                    cached.plainText ?: cached.lrcContent
                )
            }
        } catch (_: Exception) {}

        val result = fetchFromLrclibMultiTier(artist, title, album, durationMs)
        if (result != null && (!result.syncedLyrics.isNullOrBlank() || !result.plainLyrics.isNullOrBlank())) {
            try {
                val isSynced = !result.syncedLyrics.isNullOrBlank()
                val content = result.syncedLyrics ?: result.plainLyrics ?: ""
                customLyricsDao.insertCustomLyrics(
                    CustomLyricsEntity(
                        trackId = normKey,
                        lrcContent = content,
                        plainText = result.plainLyrics,
                        isSynced = isSynced,
                        updatedAt = System.currentTimeMillis()
                    )
                )
                val diskFile = File(lyricsCacheDir, "${normalizeKey(artist, title)}.lrc")
                diskFile.writeText(content, Charsets.UTF_8)
            } catch (e: Exception) {
                Log.w("LyricsManager", "Failed to cache online search lyrics: ${e.message}")
            }
        }
        Pair(result?.syncedLyrics, result?.plainLyrics)
    }
}
