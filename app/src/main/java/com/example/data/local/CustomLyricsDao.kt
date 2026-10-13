package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface CustomLyricsDao {

    @Query("SELECT * FROM custom_lyrics WHERE trackId = :trackId LIMIT 1")
    suspend fun getLyricsForTrack(trackId: String): CustomLyricsEntity?

    @Query("SELECT * FROM custom_lyrics WHERE trackId = :trackId LIMIT 1")
    fun getLyricsForTrackDirect(trackId: String): CustomLyricsEntity?

    @Query("SELECT * FROM custom_lyrics ORDER BY updatedAt DESC")
    suspend fun getAllCustomLyrics(): List<CustomLyricsEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCustomLyrics(entity: CustomLyricsEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(lyrics: List<CustomLyricsEntity>)

    @Query("DELETE FROM custom_lyrics WHERE trackId = :trackId")
    suspend fun deleteLyricsForTrack(trackId: String)

    @Query("DELETE FROM custom_lyrics")
    suspend fun clearAll()
}
