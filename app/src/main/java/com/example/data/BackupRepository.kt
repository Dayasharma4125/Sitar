package com.example.data

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import com.example.data.local.CustomLyricsEntity
import com.example.data.local.FavoriteTrackEntity
import com.example.data.local.MusicDatabase
import com.example.data.local.TrackStatsEntity
import com.example.model.AppColorPalette
import com.example.model.AppSettings
import com.example.model.AppThemeMode
import com.example.model.LyricsBackdropStyle
import com.example.model.LyricsScreenConfig
import com.example.model.LyricsTextAlignment
import com.example.model.LyricsTextSize
import com.example.model.M3ArtworkShape
import com.example.model.M3ArtworkSize
import com.example.model.M3BackgroundStyle
import com.example.model.M3ControlButtonStyle
import com.example.model.M3ElementPlacement
import com.example.model.M3ExpressivePlayerConfig
import com.example.model.M3HeroButtonStyle
import com.example.model.M3InfoAlignment
import com.example.model.M3PlayerStyle
import com.example.model.M3ScrubberStyle
import com.example.model.NotificationPattern
import com.example.model.PlayerLayoutType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Summary information about a backup file or restore operation.
 */
data class BackupSummary(
    val version: Int = 1,
    val appName: String = "Sitar",
    val exportDate: String = "",
    val timestamp: Long = 0L,
    val statsCount: Int = 0,
    val favoritesCount: Int = 0,
    val lyricsCount: Int = 0,
    val hasSettings: Boolean = false,
    val formattedDate: String = "",
    val fileSizeFormatted: String = ""
)

sealed class BackupResult {
    data class Success(val summary: BackupSummary, val message: String) : BackupResult()
    data class Error(val message: String, val throwable: Throwable? = null) : BackupResult()
}

/**
 * Robust Backup & Restore repository for Sitar Music Player.
 * Serializes and restores listening metrics, favorite tracks, custom lyrics, and user preferences.
 */
class BackupRepository(private val context: Context) {

    private val database = MusicDatabase.getInstance(context)
    private val statsDao = database.trackStatsDao()
    private val favoriteDao = database.favoriteTrackDao()
    private val customLyricsDao = database.customLyricsDao()
    private val settingsRepository = SettingsRepository(context)

    private val backupDir = File(context.filesDir, "backups").apply { if (!exists()) mkdirs() }
    private val snapshotFile = File(backupDir, "sitar_local_snapshot.json")

    /**
     * Generates a complete JSON backup of listening stats, favorites, custom lyrics, and settings.
     */
    suspend fun createBackupJson(): String = withContext(Dispatchers.IO) {
        val root = JSONObject()
        val timestamp = System.currentTimeMillis()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

        // 1. Header Metadata
        root.put("schemaVersion", 1)
        root.put("appName", "Sitar")
        root.put("appVersion", "1.0")
        root.put("timestamp", timestamp)
        root.put("exportDate", dateFormat.format(Date(timestamp)))

        // 2. Listening Stats
        val statsList = statsDao.getAllStats()
        val statsArray = JSONArray()
        for (stat in statsList) {
            val statObj = JSONObject().apply {
                put("trackId", stat.trackId)
                put("playCount", stat.playCount)
                put("completedPlays", stat.completedPlays)
                put("totalListenTimeMs", stat.totalListenTimeMs)
                put("lastPlayedTimestamp", stat.lastPlayedTimestamp)
                put("firstPlayedTimestamp", stat.firstPlayedTimestamp)
            }
            statsArray.put(statObj)
        }
        root.put("listeningStats", statsArray)

        // 3. Favorites
        val favorites = favoriteDao.getAllFavorites()
        val favoritesArray = JSONArray()
        for (fav in favorites) {
            val favObj = JSONObject().apply {
                put("trackId", fav.trackId)
                put("addedAt", fav.addedAt)
            }
            favoritesArray.put(favObj)
        }
        root.put("favorites", favoritesArray)

        // 4. Custom & Edited Lyrics
        val lyricsList = customLyricsDao.getAllCustomLyrics()
        val lyricsArray = JSONArray()
        for (lyric in lyricsList) {
            val lyricObj = JSONObject().apply {
                put("trackId", lyric.trackId)
                put("lrcContent", lyric.lrcContent)
                put("plainText", lyric.plainText ?: "")
                put("isSynced", lyric.isSynced)
                put("updatedAt", lyric.updatedAt)
            }
            lyricsArray.put(lyricObj)
        }
        root.put("customLyrics", lyricsArray)

        // 5. App Settings Profile
        val currentSettings = settingsRepository.settingsFlow.first()
        val settingsObj = JSONObject().apply {
            put("themeMode", currentSettings.themeMode.name)
            put("colorPalette", currentSettings.colorPalette.name)
            put("useDynamicColors", currentSettings.useDynamicColors)
            put("darkTheme", currentSettings.darkTheme)
            put("amoledBlack", currentSettings.amoledBlack)
            put("dynamicColor", currentSettings.dynamicColor)
            put("blurIntensity", currentSettings.blurIntensity.toDouble())
            put("forceHighRefreshRate", currentSettings.forceHighRefreshRate)
            put("playerLayoutType", currentSettings.playerLayoutType.name)
            put("minDurationSec", currentSettings.minDurationSec)
            put("blacklistedFolders", JSONArray(currentSettings.blacklistedFolders))
            put("whitelistedFolders", JSONArray(currentSettings.whitelistedFolders))
            put("useWhitelistOnly", currentSettings.useWhitelistOnly)
            put("gaplessPlayback", currentSettings.gaplessPlayback)
            put("crossfadeEnabled", currentSettings.crossfadeEnabled)
            put("crossfadeDurationSec", currentSettings.crossfadeDurationSec)
            put("alwaysShuffle", currentSettings.alwaysShuffle)
            put("showAudioDetails", currentSettings.showAudioDetails)
            put("lockScreenPlayer", currentSettings.lockScreenPlayer)
            put("musicDynamicTheming", currentSettings.musicDynamicTheming)
            put("changeFontsWithMusic", currentSettings.changeFontsWithMusic)
            put("defaultFontFamily", currentSettings.defaultFontFamily)
            put("backdropBlurEnabled", currentSettings.backdropBlurEnabled)
            put("glassmorphicEffectEnabled", currentSettings.glassmorphicEffectEnabled)
            put("snowballEffectEnabled", currentSettings.snowballEffectEnabled)
            put("liveMovingGradientEnabled", currentSettings.liveMovingGradientEnabled)
            put("homeScreenEffectsEnabled", currentSettings.homeScreenEffectsEnabled)
            put("homeScreenEffectStyle", currentSettings.homeScreenEffectStyle)
            put("playbackVisualizerEnabled", currentSettings.playbackVisualizerEnabled)
            put("waveformVisualizerStyle", currentSettings.waveformVisualizerStyle)
            put("notificationPattern", currentSettings.notificationPattern.name)
            put("silentNotification", currentSettings.silentNotification)
            put("librarySortMethod", currentSettings.librarySortMethod)
            put("librarySortAscending", currentSettings.librarySortAscending)
            put("libraryTab", currentSettings.libraryTab)
            put("aodEnabled", currentSettings.aodEnabled)
            put("aodLayoutType", currentSettings.aodLayoutType.name)
            put("aodKeepScreenAwake", currentSettings.aodKeepScreenAwake)
            put("aodBurnInProtection", currentSettings.aodBurnInProtection)
            put("aodBrightnessDim", currentSettings.aodBrightnessDim.toDouble())

            // M3 Expressive Config
            val m3Obj = JSONObject().apply {
                put("playerStyle", currentSettings.m3ExpressiveConfig.playerStyle.name)
                put("artworkShape", currentSettings.m3ExpressiveConfig.artworkShape.name)
                put("artworkSize", currentSettings.m3ExpressiveConfig.artworkSize.name)
                put("scrubberStyle", currentSettings.m3ExpressiveConfig.scrubberStyle.name)
                put("heroButtonStyle", currentSettings.m3ExpressiveConfig.heroButtonStyle.name)
                put("controlButtonStyle", currentSettings.m3ExpressiveConfig.controlButtonStyle.name)
                put("elementPlacement", currentSettings.m3ExpressiveConfig.elementPlacement.name)
                put("infoAlignment", currentSettings.m3ExpressiveConfig.infoAlignment.name)
                put("backgroundStyle", currentSettings.m3ExpressiveConfig.backgroundStyle.name)
                put("showVisualizer", currentSettings.m3ExpressiveConfig.showVisualizer)
                put("showAudioSpecs", currentSettings.m3ExpressiveConfig.showAudioSpecs)
                put("showVolumeSlider", currentSettings.m3ExpressiveConfig.showVolumeSlider)
                put("showDeviceCapsule", currentSettings.m3ExpressiveConfig.showDeviceCapsule)
                put("showActionPills", currentSettings.m3ExpressiveConfig.showActionPills)
                put("showShuffleRepeat", currentSettings.m3ExpressiveConfig.showShuffleRepeat)
                put("showFavoriteButton", currentSettings.m3ExpressiveConfig.showFavoriteButton)
                put("showTrackDetails", currentSettings.m3ExpressiveConfig.showTrackDetails)
                put("showLyricsCard", currentSettings.m3ExpressiveConfig.showLyricsCard)
                put("expressiveHaptics", currentSettings.m3ExpressiveConfig.expressiveHaptics)
                put("buttonGlowEffect", currentSettings.m3ExpressiveConfig.buttonGlowEffect)
                put("showVinylTonearm", currentSettings.m3ExpressiveConfig.showVinylTonearm)
                put("showVuMeters", currentSettings.m3ExpressiveConfig.showVuMeters)
            }
            put("m3ExpressiveConfig", m3Obj)

            // Lyrics Config
            val lyricsConfigObj = JSONObject().apply {
                put("textSize", currentSettings.lyricsConfig.textSize.name)
                put("textAlignment", currentSettings.lyricsConfig.textAlignment.name)
                put("inactiveTextContrast", currentSettings.lyricsConfig.inactiveTextContrast.toDouble())
                put("backdropStyle", currentSettings.lyricsConfig.backdropStyle.name)
                put("blurIntensity", currentSettings.lyricsConfig.blurIntensity.toDouble())
                put("karaokeWordFillEnabled", currentSettings.lyricsConfig.karaokeWordFillEnabled)
                put("showMiniTransportDeck", currentSettings.lyricsConfig.showMiniTransportDeck)
                put("autoScrollEnabled", currentSettings.lyricsConfig.autoScrollEnabled)
                put("tapToSeekEnabled", currentSettings.lyricsConfig.tapToSeekEnabled)
                put("showAudioQualityBadge", currentSettings.lyricsConfig.showAudioQualityBadge)
                put("lyricGlowBloom", currentSettings.lyricsConfig.lyricGlowBloom)
                put("staggeredSlideIn", currentSettings.lyricsConfig.staggeredSlideIn)
            }
            put("lyricsConfig", lyricsConfigObj)
        }
        root.put("appSettings", settingsObj)

        root.toString(2)
    }

    /**
     * Saves a snapshot of current data to local app internal storage.
     */
    suspend fun saveLocalSnapshot(): BackupResult = withContext(Dispatchers.IO) {
        try {
            val json = createBackupJson()
            snapshotFile.writeText(json, Charsets.UTF_8)
            val summary = parseSummaryFromJson(json, snapshotFile.length())
            BackupResult.Success(summary, "Local snapshot saved successfully (${summary.statsCount} stats, ${summary.favoritesCount} favorites, ${summary.lyricsCount} lyrics)")
        } catch (e: Exception) {
            BackupResult.Error("Failed to save local snapshot: ${e.message}", e)
        }
    }

    /**
     * Checks if a local snapshot exists and returns its summary.
     */
    suspend fun getLocalSnapshotSummary(): BackupSummary? = withContext(Dispatchers.IO) {
        try {
            if (!snapshotFile.exists() || snapshotFile.length() == 0L) return@withContext null
            val json = snapshotFile.readText(Charsets.UTF_8)
            parseSummaryFromJson(json, snapshotFile.length())
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Writes the backup JSON directly into a user-selected Uri from SAF CreateDocument contract.
     */
    suspend fun exportBackupToUri(uri: Uri): BackupResult = withContext(Dispatchers.IO) {
        try {
            val json = createBackupJson()
            context.contentResolver.openOutputStream(uri)?.use { os ->
                os.write(json.toByteArray(Charsets.UTF_8))
                os.flush()
            } ?: return@withContext BackupResult.Error("Unable to open output stream for selected file.")

            // Also refresh local snapshot
            snapshotFile.writeText(json, Charsets.UTF_8)

            val summary = parseSummaryFromJson(json, json.toByteArray().size.toLong())
            BackupResult.Success(summary, "Backup successfully exported to file.")
        } catch (e: Exception) {
            BackupResult.Error("Failed to export backup: ${e.localizedMessage ?: e.message}", e)
        }
    }

    /**
     * Restores data from a user-selected Uri from SAF OpenDocument.
     */
    suspend fun restoreBackupFromUri(uri: Uri, mergeMode: Boolean = true): BackupResult = withContext(Dispatchers.IO) {
        try {
            val inputStream: InputStream = context.contentResolver.openInputStream(uri)
                ?: return@withContext BackupResult.Error("Unable to read selected backup file.")
            val json = inputStream.bufferedReader(Charsets.UTF_8).use { it.readText() }
            restoreFromJson(json, mergeMode)
        } catch (e: Exception) {
            BackupResult.Error("Restore failed: ${e.localizedMessage ?: e.message}", e)
        }
    }

    /**
     * Restores data from the saved internal local snapshot.
     */
    suspend fun restoreLocalSnapshot(mergeMode: Boolean = true): BackupResult = withContext(Dispatchers.IO) {
        try {
            if (!snapshotFile.exists()) {
                return@withContext BackupResult.Error("No local snapshot found.")
            }
            val json = snapshotFile.readText(Charsets.UTF_8)
            restoreFromJson(json, mergeMode)
        } catch (e: Exception) {
            BackupResult.Error("Local restore failed: ${e.localizedMessage ?: e.message}", e)
        }
    }

    /**
     * Parses and applies a backup JSON string to Room DB and DataStore.
     */
    suspend fun restoreFromJson(jsonString: String, mergeMode: Boolean = true): BackupResult = withContext(Dispatchers.IO) {
        try {
            val root = JSONObject(jsonString)
            val schemaVersion = root.optInt("schemaVersion", 1)

            if (!root.has("listeningStats") && !root.has("favorites") && !root.has("appSettings")) {
                return@withContext BackupResult.Error("Invalid backup file format: missing required data sections.")
            }

            // 1. Process Listening Stats
            var statsRestored = 0
            if (root.has("listeningStats")) {
                val statsArray = root.getJSONArray("listeningStats")
                val restoredStats = mutableListOf<TrackStatsEntity>()

                if (!mergeMode) {
                    statsDao.clearAllStats()
                }

                val existingMap = if (mergeMode) statsDao.getAllStats().associateBy { it.trackId } else emptyMap()

                for (i in 0 until statsArray.length()) {
                    val obj = statsArray.optJSONObject(i) ?: continue
                    val trackId = obj.optString("trackId", "")
                    if (trackId.isBlank()) continue
                    val playCount = obj.optInt("playCount", 0)
                    val completedPlays = obj.optInt("completedPlays", 0)
                    val totalListenTimeMs = obj.optLong("totalListenTimeMs", 0L)
                    val lastPlayedTimestamp = obj.optLong("lastPlayedTimestamp", 0L)
                    val firstPlayedTimestamp = obj.optLong("firstPlayedTimestamp", System.currentTimeMillis())

                    val existing = existingMap[trackId]
                    val entity = if (existing != null && mergeMode) {
                        TrackStatsEntity(
                            trackId = trackId,
                            playCount = maxOf(existing.playCount, playCount),
                            completedPlays = maxOf(existing.completedPlays, completedPlays),
                            totalListenTimeMs = maxOf(existing.totalListenTimeMs, totalListenTimeMs),
                            lastPlayedTimestamp = maxOf(existing.lastPlayedTimestamp, lastPlayedTimestamp),
                            firstPlayedTimestamp = minOf(existing.firstPlayedTimestamp, firstPlayedTimestamp)
                        )
                    } else {
                        TrackStatsEntity(
                            trackId = trackId,
                            playCount = playCount,
                            completedPlays = completedPlays,
                            totalListenTimeMs = totalListenTimeMs,
                            lastPlayedTimestamp = lastPlayedTimestamp,
                            firstPlayedTimestamp = firstPlayedTimestamp
                        )
                    }
                    restoredStats.add(entity)
                }

                if (restoredStats.isNotEmpty()) {
                    statsDao.insertAll(restoredStats)
                    statsRestored = restoredStats.size
                }
            }

            // 2. Process Favorites
            var favoritesRestored = 0
            if (root.has("favorites")) {
                val favArray = root.getJSONArray("favorites")
                val restoredFavs = mutableListOf<FavoriteTrackEntity>()

                if (!mergeMode) {
                    favoriteDao.clearAll()
                }

                for (i in 0 until favArray.length()) {
                    val obj = favArray.optJSONObject(i) ?: continue
                    val trackId = obj.optString("trackId", "")
                    if (trackId.isBlank()) continue
                    val addedAt = obj.optLong("addedAt", System.currentTimeMillis())
                    restoredFavs.add(FavoriteTrackEntity(trackId = trackId, addedAt = addedAt))
                }

                if (restoredFavs.isNotEmpty()) {
                    favoriteDao.insertAll(restoredFavs)
                    favoritesRestored = restoredFavs.size
                }
            }

            // 3. Process Custom Lyrics
            var lyricsRestored = 0
            if (root.has("customLyrics")) {
                val lyricsArray = root.getJSONArray("customLyrics")
                val restoredLyrics = mutableListOf<CustomLyricsEntity>()

                if (!mergeMode) {
                    customLyricsDao.clearAll()
                }

                for (i in 0 until lyricsArray.length()) {
                    val obj = lyricsArray.optJSONObject(i) ?: continue
                    val trackId = obj.optString("trackId", "")
                    if (trackId.isBlank()) continue
                    val lrcContent = obj.optString("lrcContent", "")
                    val plainText = obj.optString("plainText", "")
                    val isSynced = obj.optBoolean("isSynced", true)
                    val updatedAt = obj.optLong("updatedAt", System.currentTimeMillis())

                    restoredLyrics.add(
                        CustomLyricsEntity(
                            trackId = trackId,
                            lrcContent = lrcContent,
                            plainText = if (plainText.isNotBlank()) plainText else null,
                            isSynced = isSynced,
                            updatedAt = updatedAt
                        )
                    )
                }

                if (restoredLyrics.isNotEmpty()) {
                    customLyricsDao.insertAll(restoredLyrics)
                    lyricsRestored = restoredLyrics.size
                }
            }

            // 4. Process App Settings
            var hasSettings = false
            if (root.has("appSettings")) {
                val settingsObj = root.getJSONObject("appSettings")
                val current = settingsRepository.settingsFlow.first()

                val restoredSettings = parseAppSettingsFromJson(settingsObj, current)
                settingsRepository.restoreAllSettings(restoredSettings)
                hasSettings = true
            }

            val summary = BackupSummary(
                version = schemaVersion,
                appName = root.optString("appName", "Sitar"),
                exportDate = root.optString("exportDate", ""),
                timestamp = root.optLong("timestamp", System.currentTimeMillis()),
                statsCount = statsRestored,
                favoritesCount = favoritesRestored,
                lyricsCount = lyricsRestored,
                hasSettings = hasSettings,
                formattedDate = SimpleDateFormat("MMM d, yyyy • h:mm a", Locale.getDefault()).format(Date(root.optLong("timestamp", System.currentTimeMillis())))
            )

            BackupResult.Success(summary, "Restored $statsRestored listening stats, $favoritesRestored favorites, $lyricsRestored custom lyrics, and settings.")
        } catch (e: Exception) {
            BackupResult.Error("Failed to parse and restore backup data: ${e.localizedMessage ?: e.message}", e)
        }
    }

    /**
     * Parses summary info from a JSON string without committing it to the database.
     */
    fun parseSummaryFromJson(jsonString: String, fileSizeBytes: Long = 0L): BackupSummary {
        return try {
            val root = JSONObject(jsonString)
            val version = root.optInt("schemaVersion", 1)
            val appName = root.optString("appName", "Sitar")
            val exportDate = root.optString("exportDate", "")
            val timestamp = root.optLong("timestamp", System.currentTimeMillis())
            val statsCount = root.optJSONArray("listeningStats")?.length() ?: 0
            val favoritesCount = root.optJSONArray("favorites")?.length() ?: 0
            val lyricsCount = root.optJSONArray("customLyrics")?.length() ?: 0
            val hasSettings = root.has("appSettings")

            val formattedDate = if (timestamp > 0) {
                SimpleDateFormat("MMM d, yyyy • h:mm a", Locale.getDefault()).format(Date(timestamp))
            } else exportDate

            val sizeStr = when {
                fileSizeBytes >= 1024 * 1024 -> String.format(Locale.getDefault(), "%.1f MB", fileSizeBytes / (1024.0 * 1024.0))
                fileSizeBytes >= 1024 -> String.format(Locale.getDefault(), "%.1f KB", fileSizeBytes / 1024.0)
                fileSizeBytes > 0 -> "$fileSizeBytes bytes"
                else -> ""
            }

            BackupSummary(
                version = version,
                appName = appName,
                exportDate = exportDate,
                timestamp = timestamp,
                statsCount = statsCount,
                favoritesCount = favoritesCount,
                lyricsCount = lyricsCount,
                hasSettings = hasSettings,
                formattedDate = formattedDate,
                fileSizeFormatted = sizeStr
            )
        } catch (e: Exception) {
            BackupSummary(exportDate = "Unknown")
        }
    }

    private fun parseAppSettingsFromJson(obj: JSONObject, fallback: AppSettings): AppSettings {
        val themeMode = safeEnum(obj.optString("themeMode"), fallback.themeMode)
        val colorPalette = safeEnum(obj.optString("colorPalette"), fallback.colorPalette)
        val playerLayout = safeEnum(obj.optString("playerLayoutType"), fallback.playerLayoutType)
        val notificationPattern = safeEnum(obj.optString("notificationPattern"), fallback.notificationPattern)
        val aodLayout = safeEnum(obj.optString("aodLayoutType"), fallback.aodLayoutType)

        val blacklisted = mutableSetOf<String>()
        obj.optJSONArray("blacklistedFolders")?.let { arr ->
            for (i in 0 until arr.length()) blacklisted.add(arr.getString(i))
        }

        val whitelisted = mutableSetOf<String>()
        obj.optJSONArray("whitelistedFolders")?.let { arr ->
            for (i in 0 until arr.length()) whitelisted.add(arr.getString(i))
        }

        val m3Obj = obj.optJSONObject("m3ExpressiveConfig")
        val m3Config = if (m3Obj != null) {
            val fb = fallback.m3ExpressiveConfig
            M3ExpressivePlayerConfig(
                playerStyle = safeEnum(m3Obj.optString("playerStyle"), fb.playerStyle),
                artworkShape = safeEnum(m3Obj.optString("artworkShape"), fb.artworkShape),
                artworkSize = safeEnum(m3Obj.optString("artworkSize"), fb.artworkSize),
                scrubberStyle = safeEnum(m3Obj.optString("scrubberStyle"), fb.scrubberStyle),
                heroButtonStyle = safeEnum(m3Obj.optString("heroButtonStyle"), fb.heroButtonStyle),
                controlButtonStyle = safeEnum(m3Obj.optString("controlButtonStyle"), fb.controlButtonStyle),
                elementPlacement = safeEnum(m3Obj.optString("elementPlacement"), fb.elementPlacement),
                infoAlignment = safeEnum(m3Obj.optString("infoAlignment"), fb.infoAlignment),
                backgroundStyle = safeEnum(m3Obj.optString("backgroundStyle"), fb.backgroundStyle),
                showVisualizer = m3Obj.optBoolean("showVisualizer", fb.showVisualizer),
                showAudioSpecs = m3Obj.optBoolean("showAudioSpecs", fb.showAudioSpecs),
                showVolumeSlider = m3Obj.optBoolean("showVolumeSlider", fb.showVolumeSlider),
                showDeviceCapsule = m3Obj.optBoolean("showDeviceCapsule", fb.showDeviceCapsule),
                showActionPills = m3Obj.optBoolean("showActionPills", fb.showActionPills),
                showShuffleRepeat = m3Obj.optBoolean("showShuffleRepeat", fb.showShuffleRepeat),
                showFavoriteButton = m3Obj.optBoolean("showFavoriteButton", fb.showFavoriteButton),
                showTrackDetails = m3Obj.optBoolean("showTrackDetails", fb.showTrackDetails),
                showLyricsCard = m3Obj.optBoolean("showLyricsCard", fb.showLyricsCard),
                expressiveHaptics = m3Obj.optBoolean("expressiveHaptics", fb.expressiveHaptics),
                buttonGlowEffect = m3Obj.optBoolean("buttonGlowEffect", fb.buttonGlowEffect),
                showVinylTonearm = m3Obj.optBoolean("showVinylTonearm", fb.showVinylTonearm),
                showVuMeters = m3Obj.optBoolean("showVuMeters", fb.showVuMeters)
            )
        } else fallback.m3ExpressiveConfig

        val lyricsObj = obj.optJSONObject("lyricsConfig")
        val lyricsConfig = if (lyricsObj != null) {
            val fb = fallback.lyricsConfig
            LyricsScreenConfig(
                textSize = safeEnum(lyricsObj.optString("textSize"), fb.textSize),
                textAlignment = safeEnum(lyricsObj.optString("textAlignment"), fb.textAlignment),
                inactiveTextContrast = lyricsObj.optDouble("inactiveTextContrast", fb.inactiveTextContrast.toDouble()).toFloat(),
                backdropStyle = safeEnum(lyricsObj.optString("backdropStyle"), fb.backdropStyle),
                blurIntensity = lyricsObj.optDouble("blurIntensity", fb.blurIntensity.toDouble()).toFloat(),
                karaokeWordFillEnabled = lyricsObj.optBoolean("karaokeWordFillEnabled", fb.karaokeWordFillEnabled),
                showMiniTransportDeck = lyricsObj.optBoolean("showMiniTransportDeck", fb.showMiniTransportDeck),
                autoScrollEnabled = lyricsObj.optBoolean("autoScrollEnabled", fb.autoScrollEnabled),
                tapToSeekEnabled = lyricsObj.optBoolean("tapToSeekEnabled", fb.tapToSeekEnabled),
                showAudioQualityBadge = lyricsObj.optBoolean("showAudioQualityBadge", fb.showAudioQualityBadge),
                lyricGlowBloom = lyricsObj.optBoolean("lyricGlowBloom", fb.lyricGlowBloom),
                staggeredSlideIn = lyricsObj.optBoolean("staggeredSlideIn", fb.staggeredSlideIn)
            )
        } else fallback.lyricsConfig

        return fallback.copy(
            themeMode = themeMode,
            colorPalette = colorPalette,
            useDynamicColors = obj.optBoolean("useDynamicColors", fallback.useDynamicColors),
            darkTheme = obj.optBoolean("darkTheme", fallback.darkTheme),
            amoledBlack = obj.optBoolean("amoledBlack", fallback.amoledBlack),
            dynamicColor = obj.optBoolean("dynamicColor", fallback.dynamicColor),
            blurIntensity = obj.optDouble("blurIntensity", fallback.blurIntensity.toDouble()).toFloat(),
            forceHighRefreshRate = obj.optBoolean("forceHighRefreshRate", fallback.forceHighRefreshRate),
            playerLayoutType = playerLayout,
            minDurationSec = obj.optInt("minDurationSec", fallback.minDurationSec),
            blacklistedFolders = if (blacklisted.isNotEmpty()) blacklisted else fallback.blacklistedFolders,
            whitelistedFolders = whitelisted,
            useWhitelistOnly = obj.optBoolean("useWhitelistOnly", fallback.useWhitelistOnly),
            gaplessPlayback = obj.optBoolean("gaplessPlayback", fallback.gaplessPlayback),
            crossfadeEnabled = obj.optBoolean("crossfadeEnabled", fallback.crossfadeEnabled),
            crossfadeDurationSec = obj.optInt("crossfadeDurationSec", fallback.crossfadeDurationSec),
            alwaysShuffle = obj.optBoolean("alwaysShuffle", fallback.alwaysShuffle),
            showAudioDetails = obj.optBoolean("showAudioDetails", fallback.showAudioDetails),
            lockScreenPlayer = obj.optBoolean("lockScreenPlayer", fallback.lockScreenPlayer),
            musicDynamicTheming = obj.optBoolean("musicDynamicTheming", fallback.musicDynamicTheming),
            changeFontsWithMusic = obj.optBoolean("changeFontsWithMusic", fallback.changeFontsWithMusic),
            defaultFontFamily = obj.optString("defaultFontFamily", fallback.defaultFontFamily),
            backdropBlurEnabled = obj.optBoolean("backdropBlurEnabled", fallback.backdropBlurEnabled),
            glassmorphicEffectEnabled = obj.optBoolean("glassmorphicEffectEnabled", fallback.glassmorphicEffectEnabled),
            snowballEffectEnabled = obj.optBoolean("snowballEffectEnabled", fallback.snowballEffectEnabled),
            liveMovingGradientEnabled = obj.optBoolean("liveMovingGradientEnabled", fallback.liveMovingGradientEnabled),
            homeScreenEffectsEnabled = obj.optBoolean("homeScreenEffectsEnabled", fallback.homeScreenEffectsEnabled),
            homeScreenEffectStyle = obj.optString("homeScreenEffectStyle", fallback.homeScreenEffectStyle),
            playbackVisualizerEnabled = obj.optBoolean("playbackVisualizerEnabled", fallback.playbackVisualizerEnabled),
            waveformVisualizerStyle = obj.optString("waveformVisualizerStyle", fallback.waveformVisualizerStyle),
            notificationPattern = notificationPattern,
            silentNotification = obj.optBoolean("silentNotification", fallback.silentNotification),
            librarySortMethod = obj.optString("librarySortMethod", fallback.librarySortMethod),
            librarySortAscending = obj.optBoolean("librarySortAscending", fallback.librarySortAscending),
            libraryTab = obj.optString("libraryTab", fallback.libraryTab),
            aodEnabled = obj.optBoolean("aodEnabled", fallback.aodEnabled),
            aodLayoutType = aodLayout,
            aodKeepScreenAwake = obj.optBoolean("aodKeepScreenAwake", fallback.aodKeepScreenAwake),
            aodBurnInProtection = obj.optBoolean("aodBurnInProtection", fallback.aodBurnInProtection),
            aodBrightnessDim = obj.optDouble("aodBrightnessDim", fallback.aodBrightnessDim.toDouble()).toFloat(),
            m3ExpressiveConfig = m3Config,
            lyricsConfig = lyricsConfig
        )
    }

    private inline fun <reified T : Enum<T>> safeEnum(name: String?, fallback: T): T {
        if (name.isNullOrBlank()) return fallback
        return try {
            java.lang.Enum.valueOf(T::class.java, name)
        } catch (e: Exception) {
            fallback
        }
    }
}
