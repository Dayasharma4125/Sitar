package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room database entity storing user-edited, customized, or fetched lyrics.
 * Preserves synced timestamps and text locally across app restarts.
 */
@Entity(tableName = "custom_lyrics")
data class CustomLyricsEntity(
    @PrimaryKey val trackId: String,
    val lrcContent: String,
    val plainText: String? = null,
    val isSynced: Boolean = true,
    val updatedAt: Long = System.currentTimeMillis()
)
