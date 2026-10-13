package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

/**
 * Audiophile Player Room Database for persistent audio track metadata caching.
 */
@Database(
    entities = [TrackEntity::class, TrackStatsEntity::class, FavoriteTrackEntity::class, CustomLyricsEntity::class],
    version = 4,
    exportSchema = false
)
abstract class MusicDatabase : RoomDatabase() {

    abstract fun trackDao(): TrackDao
    abstract fun trackStatsDao(): TrackStatsDao
    abstract fun favoriteTrackDao(): FavoriteTrackDao
    abstract fun customLyricsDao(): CustomLyricsDao

    companion object {
        @Volatile
        private var INSTANCE: MusicDatabase? = null

        fun getInstance(context: Context): MusicDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    MusicDatabase::class.java,
                    "audiophile_music_cache.db"
                )
                    .fallbackToDestructiveMigration()
                    .allowMainThreadQueries()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
