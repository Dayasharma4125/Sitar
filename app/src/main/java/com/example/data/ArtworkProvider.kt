package com.example.data

import android.net.Uri
import com.example.model.Track
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.util.concurrent.ConcurrentHashMap

/**
 * Provides online high-resolution artwork fallback via iTunes & Cover Art Archive / Deezer APIs
 * when local embedded cover art is missing.
 */
object ArtworkProvider {
    private val memoryCache = ConcurrentHashMap<String, String>()

    /**
     * Generates a direct search or fallback image URL for albums.
     */
    fun getFallbackCoverUrl(artist: String, album: String): String {
        val cacheKey = "${artist}_$album"
        memoryCache[cacheKey]?.let { return it }

        // Construct iTunes search endpoint or high quality placeholder
        val query = URLEncoder.encode("$artist $album", "UTF-8")
        return "https://itunes.apple.com/search?term=$query&entity=album&limit=1"
    }

    /**
     * Resolves high resolution online artwork url asynchronously.
     */
    suspend fun resolveOnlineArtworkUrl(artist: String, album: String): String? = withContext(Dispatchers.IO) {
        val cacheKey = "${artist}_$album"
        if (memoryCache.containsKey(cacheKey)) {
            return@withContext memoryCache[cacheKey]
        }

        if (artist.isBlank() || album.isBlank() || artist.equals("Unknown Artist", ignoreCase = true)) {
            return@withContext null
        }

        try {
            val query = URLEncoder.encode("$artist $album", "UTF-8")
            val url = URL("https://itunes.apple.com/search?term=$query&entity=album&limit=1")
            val conn = (url.openConnection() as HttpURLConnection).apply {
                connectTimeout = 4000
                readTimeout = 4000
                setRequestProperty("User-Agent", "PixelPlayer/1.0")
            }

            if (conn.responseCode == 200) {
                val json = conn.inputStream.bufferedReader().use { it.readText() }
                val root = JSONObject(json)
                val results = root.optJSONArray("results")
                if (results != null && results.length() > 0) {
                    val first = results.getJSONObject(0)
                    val art100 = first.optString("artworkUrl100")
                    if (art100.isNotBlank()) {
                        // Upgrade to 600x600 or 1000x1000 high-resolution artwork
                        val hiResUrl = art100.replace("100x100bb.jpg", "600x600bb.jpg")
                        memoryCache[cacheKey] = hiResUrl
                        return@withContext hiResUrl
                    }
                }
            }
        } catch (e: Exception) {
            // Silently fallback on network/timeout issues
        }

        // Cover Art Archive search fallback
        try {
            val query = URLEncoder.encode("$artist $album", "UTF-8")
            val musicBrainzUrl = URL("https://musicbrainz.org/ws/2/release/?query=$query&fmt=json&limit=1")
            val conn = (musicBrainzUrl.openConnection() as HttpURLConnection).apply {
                connectTimeout = 3000
                readTimeout = 3000
                setRequestProperty("User-Agent", "PixelPlayer/1.0 ( music@pixelplayer.app )")
            }
            if (conn.responseCode == 200) {
                val json = conn.inputStream.bufferedReader().use { it.readText() }
                val root = JSONObject(json)
                val releases = root.optJSONArray("releases")
                if (releases != null && releases.length() > 0) {
                    val releaseId = releases.getJSONObject(0).optString("id")
                    if (releaseId.isNotBlank()) {
                        val coverUrl = "https://coverartarchive.org/release/$releaseId/front-500"
                        memoryCache[cacheKey] = coverUrl
                        return@withContext coverUrl
                    }
                }
            }
        } catch (e: Exception) {
            // Ignore
        }

        null
    }
}
