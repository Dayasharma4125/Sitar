package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

/**
 * Room Data Access Object for tracking user music consumption and listening history.
 */
@Dao
interface TrackStatsDao {

    @Query("SELECT * FROM track_stats ORDER BY playCount DESC, lastPlayedTimestamp DESC")
    fun getAllStatsFlow(): Flow<List<TrackStatsEntity>>

    @Query("SELECT * FROM track_stats ORDER BY playCount DESC LIMIT :limit")
    fun getMostPlayedFlow(limit: Int = 25): Flow<List<TrackStatsEntity>>

    @Query("SELECT * FROM track_stats ORDER BY lastPlayedTimestamp DESC LIMIT :limit")
    fun getRecentlyPlayedFlow(limit: Int = 25): Flow<List<TrackStatsEntity>>

    @Query("SELECT * FROM track_stats WHERE trackId = :trackId LIMIT 1")
    suspend fun getStatsForTrack(trackId: String): TrackStatsEntity?

    @Query("SELECT * FROM track_stats")
    suspend fun getAllStats(): List<TrackStatsEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(stats: TrackStatsEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(statsList: List<TrackStatsEntity>)

    @Query("UPDATE track_stats SET playCount = playCount + 1, lastPlayedTimestamp = :timestamp WHERE trackId = :trackId")
    suspend fun incrementPlay(trackId: String, timestamp: Long): Int

    @Query("UPDATE track_stats SET totalListenTimeMs = totalListenTimeMs + :listenTimeDeltaMs WHERE trackId = :trackId")
    suspend fun addListenTime(trackId: String, listenTimeDeltaMs: Long): Int

    @Query("DELETE FROM track_stats WHERE trackId = :trackId")
    suspend fun deleteTrackStats(trackId: String)

    @Query("DELETE FROM track_stats")
    suspend fun clearAllStats()
}
