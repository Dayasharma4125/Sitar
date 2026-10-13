package com.example.model

/**
 * Track with attached user listening metrics
 */
data class TrackStatsItem(
    val track: Track,
    val playCount: Int = 0,
    val completedPlays: Int = 0,
    val totalListenTimeMs: Long = 0L,
    val lastPlayedTimestamp: Long = 0L
) {
    val formattedListenTime: String
        get() {
            val totalSeconds = totalListenTimeMs / 1000
            val minutes = totalSeconds / 60
            val hours = minutes / 60
            return when {
                hours > 0 -> "${hours}h ${minutes % 60}m"
                minutes > 0 -> "${minutes}m ${totalSeconds % 60}s"
                else -> "${totalSeconds}s"
            }
        }

    val formattedLastPlayed: String
        get() {
            if (lastPlayedTimestamp <= 0L) return "Never"
            val diffMs = System.currentTimeMillis() - lastPlayedTimestamp
            val diffSec = diffMs / 1000
            val diffMin = diffSec / 60
            val diffHours = diffMin / 60
            val diffDays = diffHours / 24
            return when {
                diffMin < 1 -> "Just now"
                diffMin < 60 -> "${diffMin}m ago"
                diffHours < 24 -> "${diffHours}h ago"
                diffDays < 7 -> "${diffDays}d ago"
                else -> "Earlier"
            }
        }
}

/**
 * Overall summary metrics aggregated across the user's music library
 */
data class MusicTrackerSummary(
    val totalPlays: Int = 0,
    val totalListenTimeMs: Long = 0L,
    val uniqueTracksCount: Int = 0,
    val topTrack: TrackStatsItem? = null,
    val topArtist: String? = null,
    val mostListened: List<TrackStatsItem> = emptyList(),
    val recentlyPlayed: List<TrackStatsItem> = emptyList()
) {
    val formattedTotalTime: String
        get() {
            val totalSeconds = totalListenTimeMs / 1000
            val minutes = totalSeconds / 60
            val hours = minutes / 60
            return when {
                hours > 0 -> "${hours}h ${minutes % 60}m"
                minutes > 0 -> "${minutes}m"
                else -> "${totalSeconds}s"
            }
        }
}
