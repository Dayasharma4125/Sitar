package com.example.data.local

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

/**
 * Entity for storing user's liked / favorited track IDs permanently in Room database.
 * Survives app restarts, cache clears, and cold starts.
 */
@Entity(tableName = "favorite_tracks")
data class FavoriteTrackEntity(
    @PrimaryKey val trackId: String,
    val addedAt: Long = System.currentTimeMillis()
)

@Dao
interface FavoriteTrackDao {

    @Query("SELECT trackId FROM favorite_tracks ORDER BY addedAt DESC")
    fun getAllFavoriteIdsFlow(): Flow<List<String>>

    @Query("SELECT trackId FROM favorite_tracks ORDER BY addedAt DESC")
    suspend fun getAllFavoriteIds(): List<String>

    @Query("SELECT * FROM favorite_tracks ORDER BY addedAt DESC")
    suspend fun getAllFavorites(): List<FavoriteTrackEntity>

    @Query("SELECT EXISTS(SELECT 1 FROM favorite_tracks WHERE trackId = :trackId)")
    suspend fun isFavorite(trackId: String): Boolean

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addFavorite(favorite: FavoriteTrackEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(favorites: List<FavoriteTrackEntity>)

    @Query("DELETE FROM favorite_tracks WHERE trackId = :trackId")
    suspend fun removeFavorite(trackId: String)

    @Query("DELETE FROM favorite_tracks")
    suspend fun clearAll()

    @Query("SELECT COUNT(*) FROM favorite_tracks")
    suspend fun getCount(): Int
}
