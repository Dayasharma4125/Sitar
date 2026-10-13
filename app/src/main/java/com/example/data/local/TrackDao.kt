package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

/**
 * Room Data Access Object for fast track metadata persistence and delta updates.
 */
@Dao
interface TrackDao {

    @Query("SELECT * FROM tracks ORDER BY title ASC")
    fun getAllTracksFlow(): Flow<List<TrackEntity>>

    @Query("SELECT * FROM tracks ORDER BY title ASC")
    fun getAllTracksDirect(): List<TrackEntity>

    @Query("SELECT * FROM tracks ORDER BY title ASC")
    suspend fun getAllTracks(): List<TrackEntity>

    @Query("SELECT * FROM tracks WHERE id = :trackId LIMIT 1")
    suspend fun getTrackById(trackId: String): TrackEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTracks(tracks: List<TrackEntity>)

    @Query("DELETE FROM tracks WHERE id = :trackId")
    suspend fun deleteTrackById(trackId: String)

    @Query("UPDATE tracks SET title = :newTitle, artist = :newArtist, album = :newAlbum WHERE id = :trackId")
    suspend fun updateTrackMetadata(trackId: String, newTitle: String, newArtist: String, newAlbum: String)

    @Query("DELETE FROM tracks WHERE id NOT IN (:validIds) AND id LIKE 'local_%'")
    suspend fun deleteOrphanedTracks(validIds: List<String>)

    @Query("DELETE FROM tracks")
    suspend fun clearAll()

    @Transaction
    suspend fun syncLibrary(currentTracks: List<TrackEntity>) {
        insertTracks(currentTracks)
        val validIds = currentTracks.map { it.id }
        if (validIds.isNotEmpty()) {
            deleteOrphanedTracks(validIds)
        }
    }
}
