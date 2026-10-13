package com.example.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import org.json.JSONArray

private val Context.sessionDataStore: DataStore<Preferences> by preferencesDataStore(name = "audiophile_playback_session")

data class PlaybackSession(
    val lastPlayedTrackUri: String? = null,
    val lastPlayedTrackId: String? = null,
    val lastPlaybackPositionMs: Long = 0L,
    val lastPlaylistQueue: List<String> = emptyList(),
    val isShuffle: Boolean = false,
    val isRepeat: Boolean = false
)

/**
 * Persists and restores last playback state and user favorites across cold starts via DataStore.
 * Ensures immediate resumption of track metadata, seek position, queue, and liked tracks.
 */
class SessionManager(private val context: Context) {

    private object Keys {
        val LAST_TRACK_URI = stringPreferencesKey("last_played_track_uri")
        val LAST_TRACK_ID = stringPreferencesKey("last_played_track_id")
        val LAST_POSITION_MS = longPreferencesKey("last_playback_position_ms")
        val LAST_QUEUE_JSON = stringPreferencesKey("last_playlist_queue_json")
        val LAST_IS_SHUFFLE = androidx.datastore.preferences.core.booleanPreferencesKey("last_is_shuffle")
        val LAST_IS_REPEAT = androidx.datastore.preferences.core.booleanPreferencesKey("last_is_repeat")
        val FAVORITE_TRACK_IDS = stringSetPreferencesKey("favorite_track_ids")
    }

    val favoritesFlow: Flow<Set<String>> = context.sessionDataStore.data.map { prefs ->
        prefs[Keys.FAVORITE_TRACK_IDS] ?: emptySet()
    }

    suspend fun toggleFavorite(trackId: String): Boolean {
        var isNowFavorite = false
        context.sessionDataStore.edit { prefs ->
            val current = prefs[Keys.FAVORITE_TRACK_IDS] ?: emptySet()
            if (current.contains(trackId)) {
                prefs[Keys.FAVORITE_TRACK_IDS] = current - trackId
                isNowFavorite = false
            } else {
                prefs[Keys.FAVORITE_TRACK_IDS] = current + trackId
                isNowFavorite = true
            }
        }
        return isNowFavorite
    }

    suspend fun isFavorite(trackId: String): Boolean {
        val prefs = context.sessionDataStore.data.first()
        return prefs[Keys.FAVORITE_TRACK_IDS]?.contains(trackId) == true
    }

    suspend fun setFavorites(trackIds: Set<String>) {
        context.sessionDataStore.edit { prefs ->
            prefs[Keys.FAVORITE_TRACK_IDS] = trackIds
        }
    }

    val sessionFlow: Flow<PlaybackSession> = context.sessionDataStore.data.map { prefs ->
        val uri = prefs[Keys.LAST_TRACK_URI]
        val id = prefs[Keys.LAST_TRACK_ID]
        val pos = prefs[Keys.LAST_POSITION_MS] ?: 0L
        val queueJson = prefs[Keys.LAST_QUEUE_JSON]
        val isShuffle = prefs[Keys.LAST_IS_SHUFFLE] ?: false
        val isRepeat = prefs[Keys.LAST_IS_REPEAT] ?: false
        val queue = parseQueueJson(queueJson)
        PlaybackSession(
            lastPlayedTrackUri = uri,
            lastPlayedTrackId = id,
            lastPlaybackPositionMs = pos,
            lastPlaylistQueue = queue,
            isShuffle = isShuffle,
            isRepeat = isRepeat
        )
    }

    suspend fun getSession(): PlaybackSession {
        return sessionFlow.first()
    }

    suspend fun saveSession(
        trackUri: String?,
        trackId: String?,
        positionMs: Long,
        queueIds: List<String>,
        isShuffle: Boolean = false,
        isRepeat: Boolean = false
    ) {
        context.sessionDataStore.edit { prefs ->
            if (trackUri != null) prefs[Keys.LAST_TRACK_URI] = trackUri
            if (trackId != null) prefs[Keys.LAST_TRACK_ID] = trackId
            prefs[Keys.LAST_POSITION_MS] = positionMs.coerceAtLeast(0L)
            prefs[Keys.LAST_QUEUE_JSON] = serializeQueue(queueIds)
            prefs[Keys.LAST_IS_SHUFFLE] = isShuffle
            prefs[Keys.LAST_IS_REPEAT] = isRepeat
        }
    }

    suspend fun updatePosition(positionMs: Long) {
        context.sessionDataStore.edit { prefs ->
            prefs[Keys.LAST_POSITION_MS] = positionMs.coerceAtLeast(0L)
        }
    }

    suspend fun updatePlaybackModes(isShuffle: Boolean, isRepeat: Boolean) {
        context.sessionDataStore.edit { prefs ->
            prefs[Keys.LAST_IS_SHUFFLE] = isShuffle
            prefs[Keys.LAST_IS_REPEAT] = isRepeat
        }
    }

    private fun serializeQueue(ids: List<String>): String {
        val array = JSONArray()
        ids.forEach { array.put(it) }
        return array.toString()
    }

    private fun parseQueueJson(json: String?): List<String> {
        if (json.isNullOrBlank()) return emptyList()
        return try {
            val array = JSONArray(json)
            val list = mutableListOf<String>()
            for (i in 0 until array.length()) {
                list.add(array.getString(i))
            }
            list
        } catch (_: Exception) {
            emptyList()
        }
    }
}
