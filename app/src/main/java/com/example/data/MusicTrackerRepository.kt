package com.example.data

import android.content.Context
import com.example.data.local.MusicDatabase
import com.example.data.local.TrackStatsEntity
import com.example.model.Track
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

/**
 * Repository responsible for user listening habits tracking:
 * - Plays count (including queue plays, manual selections, and loop completions)
 * - Completed track playthroughs
 * - Total listening duration in milliseconds
 * - Last played timestamp
 * - Play count reconciliation for tracks with large accumulated listening time
 */
class MusicTrackerRepository(private val context: Context) {

    private val database = MusicDatabase.getInstance(context)
    private val statsDao = database.trackStatsDao()

    /**
     * Map of TrackId -> TrackStatsEntity reactive flow
     */
    val statsMapFlow: Flow<Map<String, TrackStatsEntity>> = statsDao.getAllStatsFlow().map { list ->
        list.associateBy { it.trackId }
    }

    val mostPlayedFlow: Flow<List<TrackStatsEntity>> = statsDao.getMostPlayedFlow(50)

    val recentlyPlayedFlow: Flow<List<TrackStatsEntity>> = statsDao.getRecentlyPlayedFlow(50)

    /**
     * Records a new play event for the given track (from Home, Library, Search, or Queue).
     * Calculates the minimum accurate play count based on previous plays, completions,
     * and total listening time.
     */
    suspend fun recordPlay(trackId: String, trackDurationMs: Long = 0L) = withContext(Dispatchers.IO) {
        if (trackId.isBlank()) return@withContext
        val now = System.currentTimeMillis()
        val existing = statsDao.getStatsForTrack(trackId)
        val effectiveDuration = if (trackDurationMs > 15_000L) trackDurationMs else 210_000L

        if (existing == null) {
            statsDao.upsert(
                TrackStatsEntity(
                    trackId = trackId,
                    playCount = 1,
                    completedPlays = 0,
                    totalListenTimeMs = 0L,
                    lastPlayedTimestamp = now,
                    firstPlayedTimestamp = now
                )
            )
        } else {
            val playsFromListenTime = if (existing.totalListenTimeMs > 0) {
                (existing.totalListenTimeMs / effectiveDuration).toInt()
            } else 0
            val baselinePlays = maxOf(existing.playCount, existing.completedPlays, playsFromListenTime)
            statsDao.upsert(
                existing.copy(
                    playCount = baselinePlays + 1,
                    lastPlayedTimestamp = now
                )
            )
        }
    }

    /**
     * Records when a track finishes playback completely (100% or reaches end / crossfade transition).
     * Increments completedPlays and ensures playCount reflects completions and listening time proportion.
     */
    suspend fun recordTrackCompletion(trackId: String, trackDurationMs: Long = 0L) = withContext(Dispatchers.IO) {
        if (trackId.isBlank()) return@withContext
        val now = System.currentTimeMillis()
        val existing = statsDao.getStatsForTrack(trackId)
        val effectiveDuration = if (trackDurationMs > 15_000L) trackDurationMs else 210_000L

        if (existing == null) {
            statsDao.upsert(
                TrackStatsEntity(
                    trackId = trackId,
                    playCount = 1,
                    completedPlays = 1,
                    totalListenTimeMs = if (trackDurationMs > 0) trackDurationMs else 0L,
                    lastPlayedTimestamp = now,
                    firstPlayedTimestamp = now
                )
            )
        } else {
            val nextCompleted = existing.completedPlays + 1
            val playsFromListenTime = (existing.totalListenTimeMs / effectiveDuration).toInt()
            val newPlayCount = maxOf(existing.playCount, nextCompleted, playsFromListenTime)
            statsDao.upsert(
                existing.copy(
                    playCount = newPlayCount,
                    completedPlays = nextCompleted,
                    lastPlayedTimestamp = now
                )
            )
        }
    }

    /**
     * Records elapsed listening time for a track.
     * Keeps playCount in sync so tracks with long listening time (e.g. 50+ minutes)
     * accurately reflect their true play count.
     */
    suspend fun recordListenTime(
        trackId: String,
        durationDeltaMs: Long,
        trackDurationMs: Long = 0L,
        isCompleted: Boolean = false
    ) = withContext(Dispatchers.IO) {
        if (trackId.isBlank() || (durationDeltaMs <= 0 && !isCompleted)) return@withContext
        val now = System.currentTimeMillis()
        val existing = statsDao.getStatsForTrack(trackId)
        val effectiveDuration = if (trackDurationMs > 15_000L) trackDurationMs else 210_000L

        if (existing == null) {
            val totalTime = durationDeltaMs.coerceAtLeast(0L)
            val playsFromTime = (totalTime / effectiveDuration).toInt().coerceAtLeast(1)
            statsDao.upsert(
                TrackStatsEntity(
                    trackId = trackId,
                    playCount = playsFromTime,
                    completedPlays = if (isCompleted) 1 else 0,
                    totalListenTimeMs = totalTime,
                    lastPlayedTimestamp = now,
                    firstPlayedTimestamp = now
                )
            )
        } else {
            val newTotalTime = existing.totalListenTimeMs + durationDeltaMs.coerceAtLeast(0L)
            val newCompleted = existing.completedPlays + (if (isCompleted) 1 else 0)
            val playsFromTime = (newTotalTime / effectiveDuration).toInt()
            val newPlayCount = maxOf(
                existing.playCount + (if (isCompleted && existing.completedPlays >= existing.playCount) 1 else 0),
                newCompleted,
                playsFromTime
            )

            statsDao.upsert(
                existing.copy(
                    playCount = newPlayCount,
                    completedPlays = newCompleted,
                    totalListenTimeMs = newTotalTime,
                    lastPlayedTimestamp = now
                )
            )
        }
    }

    /**
     * Reconciles all tracks in the database to fix play counts for tracks that have accumulated
     * long listening durations (e.g. 50+ minutes) but only had 2-3 play counts.
     */
    suspend fun reconcileStats(tracks: List<Track>) = withContext(Dispatchers.IO) {
        if (tracks.isEmpty()) return@withContext
        try {
            val trackMap = tracks.associateBy { it.id }
            val allStats = statsDao.getAllStats()
            val toUpdate = mutableListOf<TrackStatsEntity>()

            for (stat in allStats) {
                val track = trackMap[stat.trackId]
                val durationMs = if (track != null && track.durationMs > 15_000L) track.durationMs else 210_000L
                val playsFromTime = (stat.totalListenTimeMs / durationMs).toInt()
                val accuratePlays = maxOf(stat.playCount, stat.completedPlays, playsFromTime)
                if (accuratePlays > stat.playCount) {
                    toUpdate.add(stat.copy(playCount = accuratePlays))
                }
            }

            if (toUpdate.isNotEmpty()) {
                statsDao.insertAll(toUpdate)
            }
        } catch (_: Throwable) {
        }
    }

    suspend fun clearStats() = withContext(Dispatchers.IO) {
        statsDao.clearAllStats()
    }
}
