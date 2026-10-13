package com.example.ui.player.layouts

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.model.LyricsBackdropStyle
import com.example.model.LyricsScreenConfig
import com.example.model.LyricsUiState
import com.example.model.PlayerUiState
import com.example.model.Track
import com.example.ui.lyrics.SyncedLyricsScreen

/**
 * Full-screen immersive synchronized lyrics screen adapter delegating directly
 * to [SyncedLyricsScreen] with 80% contrast, decluttered layout, and customizable settings.
 */
@Composable
fun AppleMusicLyricsPlayerScreen(
    uiState: PlayerUiState,
    lyricsUiState: LyricsUiState,
    track: Track,
    onPlayPauseClick: () -> Unit,
    onPreviousClick: () -> Unit,
    onNextClick: () -> Unit,
    onSeekTo: (Long) -> Unit,
    onFavoriteToggle: () -> Unit,
    onVolumeChange: (Float) -> Unit,
    onLyricsToggle: () -> Unit,
    onCollapseClick: () -> Unit,
    onDevicePickerClick: () -> Unit,
    onQueueClick: () -> Unit,
    onSleepTimerClick: () -> Unit = {},
    onPlaybackSpeedClick: () -> Unit = {},
    onOpenSettings: () -> Unit = {},
    onEqualizerClick: () -> Unit = {},
    onTrackDetailsClick: () -> Unit = {},
    isAmoledBlack: Boolean = false,
    config: LyricsScreenConfig = LyricsScreenConfig(),
    modifier: Modifier = Modifier
) {
    SyncedLyricsScreen(
        uiState = lyricsUiState,
        currentTrack = track,
        onLyricClick = onSeekTo,
        onBackClick = onLyricsToggle,
        onFavoriteToggle = onFavoriteToggle,
        onPlayPauseClick = onPlayPauseClick,
        onPreviousClick = onPreviousClick,
        onNextClick = onNextClick,
        onMenuClick = onOpenSettings,
        config = config,
        blurIntensity = config.blurIntensity,
        isAmoledBlack = config.backdropStyle == LyricsBackdropStyle.PURE_AMOLED_BLACK || isAmoledBlack,
        modifier = modifier
    )
}
