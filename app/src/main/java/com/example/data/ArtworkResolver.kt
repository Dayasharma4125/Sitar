package com.example.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.media.MediaMetadataRetriever
import android.net.Uri
import androidx.compose.ui.graphics.Color
import androidx.palette.graphics.Palette
import android.util.LruCache
import coil.Coil
import coil.ImageLoader
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.example.model.Track
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import kotlin.math.abs

/**
 * High-resolution Online Artwork & Artist Image Resolver.
 * Fetches high-resolution album artwork and artist portraits from Deezer / iTunes / MusicBrainz.
 * Supports background pre-fetching, disk caching, and real-time palette color extraction.
 * Optimized with bounded LRU caches and low-footprint bitmap sampling.
 */
object ArtworkResolver {

    // Bounded in-memory LRU caches to avoid memory leaks and heap bloat
    private val artworkCache = LruCache<String, String>(150)
    private val artistImageCache = LruCache<String, String>(150)
    private val colorPaletteCache = LruCache<String, Pair<Color, Color>>(150)

    suspend fun resolveArtworkUrl(artist: String, albumOrTitle: String): String? = withContext(Dispatchers.IO) {
        val cleanArtist = artist.trim().takeIf { it.isNotBlank() && !it.equals("<unknown>", ignoreCase = true) } ?: return@withContext null
        val cleanQuery = albumOrTitle.trim().takeIf { it.isNotBlank() && !it.equals("<unknown>", ignoreCase = true) } ?: return@withContext null
        val cacheKey = "$cleanArtist::$cleanQuery".lowercase()

        synchronized(artworkCache) {
            artworkCache.get(cacheKey)?.let { return@withContext it }
        }

        // 1. Query Public iTunes Search API for high-resolution 600x600 artwork
        try {
            val encodedQuery = URLEncoder.encode("$cleanArtist $cleanQuery", "UTF-8")
            val url = URL("https://itunes.apple.com/search?term=$encodedQuery&media=music&entity=album&limit=1")
            val connection = url.openConnection() as HttpURLConnection
            connection.connectTimeout = 4000
            connection.readTimeout = 4000
            connection.requestMethod = "GET"

            if (connection.responseCode == 200) {
                val responseText = connection.inputStream.bufferedReader().use { it.readText() }
                val json = JSONObject(responseText)
                val results = json.optJSONArray("results")
                if (results != null && results.length() > 0) {
                    val first = results.getJSONObject(0)
                    val rawUrl = first.optString("artworkUrl100")
                    if (rawUrl.isNotBlank()) {
                        val hiResUrl = rawUrl.replace("100x100bb", "600x600bb")
                        synchronized(artworkCache) { artworkCache.put(cacheKey, hiResUrl) }
                        return@withContext hiResUrl
                    }
                }
            }
        } catch (e: Exception) {
            // Ignore and try fallback
        }

        // 2. Deezer public search
        try {
            val encodedQuery = URLEncoder.encode("$cleanArtist $cleanQuery", "UTF-8")
            val url = URL("https://api.deezer.com/search?q=$encodedQuery&limit=1")
            val connection = url.openConnection() as HttpURLConnection
            connection.connectTimeout = 4000
            connection.readTimeout = 4000
            connection.requestMethod = "GET"

            if (connection.responseCode == 200) {
                val responseText = connection.inputStream.bufferedReader().use { it.readText() }
                val json = JSONObject(responseText)
                val data = json.optJSONArray("data")
                if (data != null && data.length() > 0) {
                    val item = data.getJSONObject(0)
                    val albumObj = item.optJSONObject("album")
                    val cover = albumObj?.optString("cover_xl") ?: albumObj?.optString("cover_big")
                    if (!cover.isNullOrBlank()) {
                        synchronized(artworkCache) { artworkCache.put(cacheKey, cover) }
                        return@withContext cover
                    }
                }
            }
        } catch (e: Exception) {
            // Ignore and try MusicBrainz
        }

        // 3. Fallback to MusicBrainz & Cover Art Archive API
        try {
            val encodedArtist = URLEncoder.encode(cleanArtist, "UTF-8")
            val encodedAlbum = URLEncoder.encode(cleanQuery, "UTF-8")
            val mbUrl = URL("https://musicbrainz.org/ws/2/release/?query=artist:%22$encodedArtist%22%20AND%20release:%22$encodedAlbum%22&fmt=json&limit=1")
            val mbConn = mbUrl.openConnection() as HttpURLConnection
            mbConn.setRequestProperty("User-Agent", "PixelPlayer/1.0 ( musicplayer@example.com )")
            mbConn.connectTimeout = 3000
            mbConn.readTimeout = 3000
            mbConn.requestMethod = "GET"

            if (mbConn.responseCode == 200) {
                val mbResponse = mbConn.inputStream.bufferedReader().use { it.readText() }
                val mbJson = JSONObject(mbResponse)
                val releases = mbJson.optJSONArray("releases")
                if (releases != null && releases.length() > 0) {
                    val mbid = releases.getJSONObject(0).optString("id")
                    if (mbid.isNotBlank()) {
                        val caaUrl = "https://coverartarchive.org/release/$mbid/front-500.jpg"
                        synchronized(artworkCache) { artworkCache.put(cacheKey, caaUrl) }
                        return@withContext caaUrl
                    }
                }
            }
        } catch (e: Exception) {
            // Fallback exhausted
        }

        null
    }

    /**
     * Resolves high-resolution artist portrait image from internet.
     */
    suspend fun resolveArtistImageUrl(artist: String): String? = withContext(Dispatchers.IO) {
        val cleanArtist = artist.trim().takeIf {
            it.isNotBlank() && !it.equals("<unknown>", ignoreCase = true) && !it.equals("Unknown Artist", ignoreCase = true)
        } ?: return@withContext null

        val cacheKey = cleanArtist.lowercase()
        synchronized(artistImageCache) {
            artistImageCache.get(cacheKey)?.let { return@withContext it }
        }

        // 1. Query Deezer Artist API for high-resolution artist picture
        try {
            val encodedArtist = URLEncoder.encode(cleanArtist, "UTF-8")
            val url = URL("https://api.deezer.com/search/artist?q=$encodedArtist&limit=1")
            val connection = url.openConnection() as HttpURLConnection
            connection.connectTimeout = 4000
            connection.readTimeout = 4000
            connection.requestMethod = "GET"

            if (connection.responseCode == 200) {
                val responseText = connection.inputStream.bufferedReader().use { it.readText() }
                val json = JSONObject(responseText)
                val data = json.optJSONArray("data")
                if (data != null && data.length() > 0) {
                    val item = data.getJSONObject(0)
                    val picture = item.optString("picture_xl").takeIf { it.isNotBlank() }
                        ?: item.optString("picture_big").takeIf { it.isNotBlank() }
                        ?: item.optString("picture_medium").takeIf { it.isNotBlank() }
                    if (picture != null) {
                        synchronized(artistImageCache) { artistImageCache.put(cacheKey, picture) }
                        return@withContext picture
                    }
                }
            }
        } catch (e: Exception) {
            // Ignore and try iTunes
        }

        // 2. Query iTunes Search API for artist
        try {
            val encodedQuery = URLEncoder.encode(cleanArtist, "UTF-8")
            val url = URL("https://itunes.apple.com/search?term=$encodedQuery&media=music&entity=musicArtist&limit=1")
            val connection = url.openConnection() as HttpURLConnection
            connection.connectTimeout = 3000
            connection.readTimeout = 3000
            connection.requestMethod = "GET"

            if (connection.responseCode == 200) {
                val responseText = connection.inputStream.bufferedReader().use { it.readText() }
                val json = JSONObject(responseText)
                val results = json.optJSONArray("results")
                if (results != null && results.length() > 0) {
                    // iTunes artist may not directly provide picture, fallback to artist's top album
                    val albumUrl = resolveArtworkUrl(cleanArtist, "Album")
                    if (albumUrl != null) {
                        synchronized(artistImageCache) { artistImageCache.put(cacheKey, albumUrl) }
                        return@withContext albumUrl
                    }
                }
            }
        } catch (e: Exception) {
            // Ignore
        }

        null
    }

    /**
     * Pre-fetches all artist portraits and album artwork into Coil disk cache.
     */
    suspend fun prefetchAndCacheImages(
        context: Context,
        tracks: List<Track>,
        onProgress: (current: Int, total: Int) -> Unit
    ): Int = withContext(Dispatchers.IO) {
        val imageLoader = Coil.imageLoader(context)
        val distinctArtists = tracks.map { it.artist }.filter { it.isNotBlank() && !it.equals("<unknown>", ignoreCase = true) }.distinct()
        val distinctAlbums = tracks.map { Pair(it.artist, it.album) }.distinct()

        val totalTasks = distinctArtists.size + distinctAlbums.size
        var completedCount = 0
        var cachedSuccessCount = 0

        // 1. Fetch & cache artist portraits
        for (artist in distinctArtists) {
            try {
                val url = resolveArtistImageUrl(artist)
                if (!url.isNullOrBlank()) {
                    val request = ImageRequest.Builder(context)
                        .data(url)
                        .diskCachePolicy(CachePolicy.ENABLED)
                        .memoryCachePolicy(CachePolicy.ENABLED)
                        .build()
                    imageLoader.execute(request)
                    cachedSuccessCount++
                }
            } catch (e: Exception) {
                // Continue with next
            }
            completedCount++
            onProgress(completedCount, totalTasks)
        }

        // 2. Fetch & cache album covers
        for ((artist, album) in distinctAlbums) {
            try {
                val url = resolveArtworkUrl(artist, album)
                if (!url.isNullOrBlank()) {
                    val request = ImageRequest.Builder(context)
                        .data(url)
                        .diskCachePolicy(CachePolicy.ENABLED)
                        .memoryCachePolicy(CachePolicy.ENABLED)
                        .build()
                    imageLoader.execute(request)
                    cachedSuccessCount++
                }
            } catch (e: Exception) {
                // Continue
            }
            completedCount++
            onProgress(completedCount, totalTasks)
        }

        cachedSuccessCount
    }

    /**
     * Extracts dynamic ambient primary and secondary colors directly from the track's album artwork bitmap.
     * Uses vibrancy and saturation analysis to find striking complementary colors.
     */
    suspend fun extractTrackColors(context: Context, track: Track): Pair<Color, Color> = withContext(Dispatchers.IO) {
        val cacheKey = (track.albumArtUri ?: track.coverUrl ?: "${track.artist}::${track.album}").lowercase()
        synchronized(colorPaletteCache) {
            colorPaletteCache.get(cacheKey)?.let { return@withContext it }
        }

        // 1. Try resolving bitmap
        val bitmap = loadTrackBitmap(context, track)
        if (bitmap != null) {
            val colors = analyzeBitmapColors(bitmap)
            synchronized(colorPaletteCache) { colorPaletteCache.put(cacheKey, colors) }
            return@withContext colors
        }

        // 2. Fallback to existing ambient colors or harmonic algorithm
        val fallback = try {
            if (track.primaryAmbientColor != Color.Unspecified && track.primaryAmbientColor != Color(0xFF263859)) {
                Pair(track.primaryAmbientColor, track.secondaryAmbientColor)
            } else {
                val hash = (track.title + track.artist).hashCode()
                val primary = MediaRepository.getHarmonicColor(hash)
                val secondary = MediaRepository.getDarkerVariant(primary)
                Pair(primary, secondary)
            }
        } catch (_: Throwable) {
            Pair(Color(0xFF3860B2), Color(0xFF1E293B))
        }
        synchronized(colorPaletteCache) { colorPaletteCache.put(cacheKey, fallback) }
        fallback
    }

    private suspend fun loadTrackBitmap(context: Context, track: Track): Bitmap? = withContext(Dispatchers.IO) {
        try {
            val imageLoader = Coil.imageLoader(context)

            // Try direct coverUrl or album art
            val coverSource: Any? = track.coverUrl
                ?: track.albumArtUri
                ?: track.coverResId

            if (coverSource != null) {
                val request = ImageRequest.Builder(context)
                    .data(coverSource)
                    .dispatcher(Dispatchers.IO)
                    .size(96, 96)
                    .allowHardware(false)
                    .build()
                val result = imageLoader.execute(request)
                val drawable = result.drawable
                if (drawable is BitmapDrawable && drawable.bitmap != null) {
                    return@withContext drawable.bitmap
                }
            }

            // Try embedded metadata retriever for local file Uri or filePath
            val retrieverDataSource = track.contentUri ?: track.filePath
            if (retrieverDataSource != null) {
                var retriever: MediaMetadataRetriever? = null
                try {
                    retriever = MediaMetadataRetriever()
                    if (track.filePath != null) {
                        retriever.setDataSource(track.filePath)
                    } else {
                        retriever.setDataSource(context, Uri.parse(retrieverDataSource))
                    }
                    val artBytes = retriever.embeddedPicture
                    if (artBytes != null) {
                        val options = BitmapFactory.Options().apply {
                            inSampleSize = 4
                            inPreferredConfig = Bitmap.Config.RGB_565
                        }
                        return@withContext BitmapFactory.decodeByteArray(artBytes, 0, artBytes.size, options)
                    }
                } catch (_: Exception) {
                } finally {
                    try {
                        retriever?.release()
                    } catch (_: Exception) {}
                }
            }
        } catch (_: Exception) {
        }
        null
    }

    /**
     * Color vibrancy, dominance, and palette extraction using AndroidX Palette API.
     * Prioritizes the brightest, most luminous, and saturated colors from the artwork
     * for high-contrast visibility on dark backgrounds, buttons, and UI accents.
     */
    fun analyzeBitmapColors(bitmap: Bitmap): Pair<Color, Color> {
        try {
            val sampleBitmap = if (bitmap.width > 64 || bitmap.height > 64) {
                Bitmap.createScaledBitmap(bitmap, 64, 64, false)
            } else {
                bitmap
            }
            val palette = Palette.from(sampleBitmap).maximumColorCount(16).generate()

            // Find the brightest, most vibrant swatch among all detected swatches
            val allSwatches = palette.swatches
            val brightestVibrantSwatch = allSwatches
                .filter { swatch ->
                    val hsl = swatch.hsl
                    val sat = hsl[1]
                    val light = hsl[2]
                    // Prefer visible color with good saturation and brightness (not pure black / dark grey)
                    sat >= 0.20f && light >= 0.35f
                }
                .maxByOrNull { swatch ->
                    val hsl = swatch.hsl
                    // Score = balance of lightness, saturation, and presence
                    (hsl[1] * 2.5f) + (hsl[2] * 3.0f) + (swatch.population.toFloat() / 1000f)
                }

            val primarySwatch = brightestVibrantSwatch
                ?: palette.lightVibrantSwatch
                ?: palette.vibrantSwatch
                ?: palette.dominantSwatch
                ?: palette.lightMutedSwatch
                ?: palette.mutedSwatch

            // Secondary: find another distinct hue swatch for complementary aesthetic
            val secondarySwatch = allSwatches
                .filter { swatch ->
                    swatch != primarySwatch && swatch.hsl[1] >= 0.15f
                }
                .maxByOrNull { swatch ->
                    val hueDiff = kotlin.math.abs(swatch.hsl[0] - (primarySwatch?.hsl?.get(0) ?: 0f))
                    val light = swatch.hsl[2]
                    hueDiff * 1.5f + light * 2f
                }
                ?: palette.darkVibrantSwatch
                ?: palette.vibrantSwatch
                ?: palette.mutedSwatch

            var primaryColor = primarySwatch?.let { Color(it.rgb) } ?: Color(0xFF64B5F6)
            var secondaryColor = secondarySwatch?.let { Color(it.rgb) } ?: Color(
                red = (primaryColor.red * 0.7f + 0.2f).coerceIn(0f, 1f),
                green = (primaryColor.green * 0.7f + 0.2f).coerceIn(0f, 1f),
                blue = (primaryColor.blue * 0.7f + 0.2f).coerceIn(0f, 1f),
                alpha = 1f
            )

            // Ensure primary color has sufficient luminance for dark mode buttons
            val r = primaryColor.red
            val g = primaryColor.green
            val b = primaryColor.blue
            val luminance = 0.2126f * r + 0.7152f * g + 0.0722f * b
            if (luminance < 0.40f) {
                // Brighten the color up for striking visibility
                val boost = (0.55f / luminance.coerceAtLeast(0.05f)).coerceIn(1.2f, 2.5f)
                primaryColor = Color(
                    red = (r * boost).coerceIn(0f, 1f),
                    green = (g * boost).coerceIn(0f, 1f),
                    blue = (b * boost).coerceIn(0f, 1f),
                    alpha = 1f
                )
            }

            return Pair(primaryColor, secondaryColor)
        } catch (e: Exception) {
            return Pair(Color(0xFF64B5F6), Color(0xFF388E3C))
        }
    }
}
