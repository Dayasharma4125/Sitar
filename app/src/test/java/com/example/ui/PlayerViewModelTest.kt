package com.example.ui

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import com.example.data.FastLibraryCache
import com.example.model.Track
import com.example.ui.player.PlayerViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class PlayerViewModelTest {

    private lateinit var viewModel: PlayerViewModel

    @Before
    fun setup() {
        val application = ApplicationProvider.getApplicationContext<Application>()
        val sampleTracks = listOf(
            Track(
                id = "track_1",
                title = "Cornfield Chase",
                artist = "Hans Zimmer",
                album = "Interstellar OST",
                durationMs = 126_000L,
                audioQuality = "FLAC 24-bit / 96kHz"
            ),
            Track(
                id = "track_2",
                title = "Stay",
                artist = "Hans Zimmer",
                album = "Interstellar OST",
                durationMs = 412_000L,
                audioQuality = "FLAC 24-bit / 96kHz"
            )
        )
        FastLibraryCache.saveInstantTracks(application, sampleTracks)
        viewModel = PlayerViewModel(application)
    }

    @Test
    fun `initial player state has track and library loaded`() {
        val playerState = viewModel.playerUiState.value
        val homeState = viewModel.homeUiState.value

        assertNotNull(playerState.currentTrack)
        assertTrue(homeState.tracks.isNotEmpty())
        assertEquals(homeState.tracks.size, homeState.filteredTracks.size)
    }

    @Test
    fun `search filter works as expected`() {
        viewModel.onSearchQueryChanged("Interstellar")
        val filtered = viewModel.homeUiState.value.filteredTracks

        assertTrue(filtered.all { it.album.contains("Interstellar", ignoreCase = true) || it.title.contains("Interstellar", ignoreCase = true) })
    }

    @Test
    fun `playTrack expands player and sets active track`() {
        val secondTrack = viewModel.homeUiState.value.tracks.last()
        viewModel.playTrack(secondTrack)

        val playerState = viewModel.playerUiState.value
        assertEquals(secondTrack.id, playerState.currentTrack.id)
        assertTrue(playerState.isNowPlayingExpanded)
        assertTrue(playerState.isPlaying)
    }

    @Test
    fun `seekTo updates playback position accurately`() {
        viewModel.seekTo(12_000L)
        assertEquals(12_000L, viewModel.playerUiState.value.currentPositionMs)
        val lyricsState = viewModel.lyricsUiState.value
        if (lyricsState is com.example.model.LyricsUiState.Success) {
            assertEquals(12_000L, lyricsState.currentPlaybackPositionMs)
        }
    }

    @Test
    fun `favorite toggle flips state`() {
        val initialFav = viewModel.playerUiState.value.isFavorite
        viewModel.toggleFavorite()
        assertEquals(!initialFav, viewModel.playerUiState.value.isFavorite)
    }

    @Test
    fun `setPlayerLayoutType updates settings state`() {
        viewModel.setPlayerLayoutType(com.example.model.PlayerLayoutType.MATERIAL_YOU_EXPRESSIVE)
        assertEquals(com.example.model.PlayerLayoutType.MATERIAL_YOU_EXPRESSIVE, viewModel.settingsState.value.playerLayoutType)

        viewModel.setPlayerLayoutType(com.example.model.PlayerLayoutType.APPLE_MUSIC)
        assertEquals(com.example.model.PlayerLayoutType.APPLE_MUSIC, viewModel.settingsState.value.playerLayoutType)

        viewModel.setPlayerLayoutType(com.example.model.PlayerLayoutType.M3_EXPRESSIVE)
        assertEquals(com.example.model.PlayerLayoutType.M3_EXPRESSIVE, viewModel.settingsState.value.playerLayoutType)
    }

    @Test
    fun `glassmorphic effect toggle and playback visualizer toggle update settings state`() {
        viewModel.setGlassmorphicEffectEnabled(false)
        assertFalse(viewModel.settingsState.value.glassmorphicEffectEnabled)

        viewModel.setGlassmorphicEffectEnabled(true)
        assertTrue(viewModel.settingsState.value.glassmorphicEffectEnabled)

        viewModel.setPlaybackVisualizerEnabled(false)
        assertFalse(viewModel.settingsState.value.playbackVisualizerEnabled)

        viewModel.setPlaybackVisualizerEnabled(true)
        assertTrue(viewModel.settingsState.value.playbackVisualizerEnabled)
    }

    @Test
    fun `show audio details toggle updates settings state and defaults to false`() {
        assertFalse(viewModel.settingsState.value.showAudioDetails)

        viewModel.setShowAudioDetails(true)
        assertTrue(viewModel.settingsState.value.showAudioDetails)

        viewModel.setShowAudioDetails(false)
        assertFalse(viewModel.settingsState.value.showAudioDetails)
    }

    @Test
    fun `whitelist folder strictly filters tracks by storage path and rejects unrelated folders`() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val mediaRepo = com.example.data.MediaRepository(app)

        val musicTrack = Track(
            id = "track_music",
            title = "Bohemian Rhapsody",
            artist = "Queen",
            album = "A Night at the Opera",
            durationMs = 354_000L,
            filePath = "/storage/emulated/0/Music/Queen/Bohemian.mp3"
        )
        val downloadTrack = Track(
            id = "track_download",
            title = "Podcast Episode",
            artist = "Interviewer",
            album = "Podcasts",
            durationMs = 600_000L,
            filePath = "/storage/emulated/0/Download/Episode1.mp3"
        )
        val titleMatchTrack = Track(
            id = "track_recordings",
            title = "My Favorite Music Recording",
            artist = "Me",
            album = "Recordings",
            durationMs = 120_000L,
            filePath = "/storage/emulated/0/Recordings/Voice.mp3"
        )

        val settingsWithWhitelist = com.example.model.AppSettings(
            whitelistedFolders = setOf("Music"),
            useWhitelistOnly = true
        )

        // Music track should pass
        assertTrue(mediaRepo.passesTrackFilters(musicTrack, settingsWithWhitelist))

        // Download track should NOT pass
        assertFalse(mediaRepo.passesTrackFilters(downloadTrack, settingsWithWhitelist))

        // Track that only has "Music" in its title but resides in Recordings should NOT pass!
        assertFalse(mediaRepo.passesTrackFilters(titleMatchTrack, settingsWithWhitelist))
    }
}
