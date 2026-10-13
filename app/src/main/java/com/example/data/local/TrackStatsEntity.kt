package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Persistent Room entity recording user listening metrics:
 * - Total play count
 * - Completed plays
 * - Total listening duration in milliseconds
 * - Last and first played timestamps
 */
@Entity(tableName = "track_stats")
data class TrackStatsEntity(
    @PrimaryKey val trackId: String,
    val playCount: Int = 0,
    val completedPlays: Int = 0,
    val totalListenTimeMs: Long = 0L,
    val lastPlayedTimestamp: Long = 0L,
    val firstPlayedTimestamp: Long = System.currentTimeMillis()
)
