package com.example.ui.player

import android.app.Application
import android.net.Uri
import android.os.Build
import android.util.Log
import android.content.Intent
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.example.audio.AudioEngineManager
import com.example.audio.AudioOutputDetector
import com.example.audio.AudioRoutingMode
import com.example.audio.EqualizerManager
import com.example.audio.SystemVolumeController
import com.example.cast.CastDevice
import com.example.cast.CastDeviceType
import com.example.cast.CastState
import com.example.cast.CastStreamQuality
import com.example.cast.ChromecastManager
import com.example.cast.ChromecastUiState
import com.example.data.BackupRepository
import com.example.data.BackupResult
import com.example.data.BackupSummary
import com.example.data.FastLibraryCache
import com.example.data.MediaRepository
import com.example.data.MusicTrackerRepository
import com.example.data.SessionManager
import com.example.data.SettingsRepository
import com.example.data.local.FavoriteTrackEntity
import com.example.data.local.MusicDatabase
import com.example.data.local.TrackStatsEntity
import com.example.lyrics.LyricsManager
import com.example.model.Album
import com.example.model.AppSettings
import com.example.model.Artist
import com.example.model.AudioDeviceType
import com.example.model.ConnectedAudioDevice
import com.example.model.EqualizerUiState
import com.example.model.HomeUiState
import com.example.model.LyricLine
import com.example.model.LyricsUiState
import com.example.model.MusicTrackerSummary
import com.example.model.PlayerUiState
import com.example.model.SleepTimerState
import com.example.model.Track
import com.example.model.TrackStatsItem
import com.example.service.PlaybackMediaSessionService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PlayerViewModel(application: Application) : AndroidViewModel(application) {

    val mediaRepository = MediaRepository(application)
    val settingsRepository = SettingsRepository(application)
    val sessionManager = SessionManager(application)
    val musicDatabase = MusicDatabase.getInstance(application)
    val audioEngineManager = AudioEngineManager(application)
    val equalizerManager = EqualizerManager(application)
    private val lyricsManager = LyricsManager(application)
    private val systemVolumeController = SystemVolumeController(application)

    val exoPlayer: ExoPlayer = PlaybackMediaSessionService.getSharedPlayer(application)

    private var mediaControllerFuture: com.google.common.util.concurrent.ListenableFuture<androidx.media3.session.MediaController>? = null
    private var mediaController: androidx.media3.session.MediaController? = null

    val audioEngineStatus = audioEngineManager.engineStatus

    // Real-time audio routing & unplug detection
    private val audioOutputDetector = AudioOutputDetector(
        context = application,
        onBecomingNoisy = {
            pause()
        }
    )
    val connectedDevice: StateFlow<ConnectedAudioDevice> = audioOutputDetector.currentDevice
    val availableDevices: StateFlow<List<ConnectedAudioDevice>> = audioOutputDetector.availableDevices

    // Chromecast & Wireless Audio Casting Manager
    val chromecastManager = ChromecastManager(
        context = application,
        onPlaybackStateChange = { isPlaying ->
            _playerUiState.update { it.copy(isPlaying = isPlaying) }
        }
    )
    val chromecastUiState: StateFlow<ChromecastUiState> = chromecastManager.uiState

    // Persistent App Settings
    private val _settingsState = MutableStateFlow(settingsRepository.getSettingsFastSync())
    val settingsState: StateFlow<AppSettings> = _settingsState.asStateFlow()

    private val initialInstantTracks = FastLibraryCache.getInstantTracks(application)

    // Home / Library State: initialized with instant cache so HomeScreen renders immediately
    private val _homeUiState = MutableStateFlow(
        HomeUiState(
            tracks = initialInstantTracks,
            filteredTracks = initialInstantTracks,
            searchQuery = "",
            isScanning = false,
            hasStoragePermission = false,
            totalCount = initialInstantTracks.size
        )
    )
    val homeUiState: StateFlow<HomeUiState> = _homeUiState.asStateFlow()

    // Music Tracker & Listening Analytics
    val musicTrackerRepository = MusicTrackerRepository(application)
    val trackerStatsMap: StateFlow<Map<String, TrackStatsEntity>> = musicTrackerRepository.statsMapFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    val trackerSummary: StateFlow<MusicTrackerSummary> = combine(
        _homeUiState,
        musicTrackerRepository.statsMapFlow
    ) { homeState, statsMap ->
        computeTrackerSummary(homeState.tracks, statsMap)
    }.flowOn(Dispatchers.Default).stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), MusicTrackerSummary())

    // Backup & Restore Repository
    val backupRepository = BackupRepository(application)
    private val _backupStatus = MutableStateFlow<String?>(null)
    val backupStatus: StateFlow<String?> = _backupStatus.asStateFlow()
    private val _localSnapshotInfo = MutableStateFlow<BackupSummary?>(null)
    val localSnapshotInfo: StateFlow<BackupSummary?> = _localSnapshotInfo.asStateFlow()

    // Equalizer UI State
    val equalizerUiState: StateFlow<EqualizerUiState> = equalizerManager.uiState

    // Player State
    private val _playerUiState = MutableStateFlow(
        PlayerUiState(
            currentTrack = initialInstantTracks.firstOrNull() ?: Track.EMPTY,
            isPlaying = false,
            currentPositionMs = 0L,
            durationMs = initialInstantTracks.firstOrNull()?.durationMs ?: 0L,
            isFavorite = false,
            volume = 0.75f,
            activeDevice = audioOutputDetector.currentDevice.value,
            isLyricsVisible = false,
            isNowPlayingExpanded = false,
            playlist = initialInstantTracks,
            currentTrackIndex = 0,
            queueCount = initialInstantTracks.size
        )
    )
    val playerUiState: StateFlow<PlayerUiState> = _playerUiState.asStateFlow()

    // Lyrics State
    private val _lyricsUiState = MutableStateFlow<LyricsUiState>(
        LyricsUiState.NotFound()
    )
    val lyricsUiState: StateFlow<LyricsUiState> = _lyricsUiState.asStateFlow()

    // Artists derived from current scanned library
    val artists: StateFlow<List<Artist>> = _homeUiState
        .map { state ->
            val allTracks = state.tracks
            allTracks.groupBy { if (it.artist.isBlank()) "Unknown Artist" else it.artist }
                .map { (artistName, artistTracks) ->
                    val albums = artistTracks.groupBy { if (it.album.isBlank()) "Unknown Album" else it.album }
                        .map { (albumName, albumTracks) ->
                            Album(
                                name = albumName,
                                artist = artistName,
                                coverUrl = albumTracks.firstOrNull { it.coverUrl != null }?.coverUrl,
                                coverResId = albumTracks.firstOrNull { it.coverResId != null }?.coverResId,
                                trackCount = albumTracks.size,
                                tracks = albumTracks
                            )
                        }
                    Artist(
                        name = artistName,
                        trackCount = artistTracks.size,
                        albumCount = albums.size,
                        bannerUrl = artistTracks.firstOrNull { it.coverUrl != null }?.coverUrl,
                        tracks = artistTracks,
                        albums = albums
                    )
                }.sortedBy { it.name.lowercase() }
        }
        .flowOn(Dispatchers.Default)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedArtist = MutableStateFlow<Artist?>(null)
    val selectedArtist: StateFlow<Artist?> = _selectedArtist.asStateFlow()

    private val _isCachingImages = MutableStateFlow(false)
    val isCachingImages: StateFlow<Boolean> = _isCachingImages.asStateFlow()

    private val _cachingProgress = MutableStateFlow<String?>(null)
    val cachingProgress: StateFlow<String?> = _cachingProgress.asStateFlow()

    private var progressTickerJob: Job? = null
    private var lyricsFetchJob: Job? = null
    private var sleepTimerJob: Job? = null
    private var isSimulatedPlayback = false

    private val _sleepTimerState = MutableStateFlow(SleepTimerState())
    val sleepTimerState: StateFlow<SleepTimerState> = _sleepTimerState.asStateFlow()

    private val _favoriteTrackIds = MutableStateFlow<Set<String>>(emptySet())
    val favoriteTrackIds: StateFlow<Set<String>> = _favoriteTrackIds.asStateFlow()

    private var hasScannedStorage = false
    private var lastFolderFilterHash: Int? = null

    init {
        PlaybackMediaSessionService.onSkipNextListener = { skipNext() }
        PlaybackMediaSessionService.onSkipPreviousListener = { skipPrevious() }

        audioOutputDetector.startListening()
        startProgressTicker()
        observeDeviceChanges()
        observeSettingsChanges()
        observeSystemVolume()
        observeFavorites()
        loadCachedTracksInitial()

        viewModelScope.launch {
            setupExoPlayerListener()
            connectToPlaybackService()
        }
    }

    private fun connectToPlaybackService() {
        try {
            val sessionToken = androidx.media3.session.SessionToken(
                getApplication(),
                android.content.ComponentName(getApplication(), PlaybackMediaSessionService::class.java)
            )
            mediaControllerFuture = androidx.media3.session.MediaController.Builder(
                getApplication(),
                sessionToken
            ).buildAsync()

            mediaControllerFuture?.addListener(
                {
                    try {
                        mediaController = mediaControllerFuture?.get()
                    } catch (e: Exception) {
                        Log.w("PlayerViewModel", "Error connecting MediaController: ${e.message}")
                    }
                },
                androidx.core.content.ContextCompat.getMainExecutor(getApplication())
            )
        } catch (e: Exception) {
            Log.w("PlayerViewModel", "Error initiating MediaController: ${e.message}")
        }
    }

    fun updatePlaybackServiceNotification(isPlaying: Boolean = _playerUiState.value.isPlaying) {
        // Handled automatically via PlaybackMediaSessionService & MediaSession
    }

    private fun startPlaybackService() {
        PlaybackMediaSessionService.start(getApplication())
    }

    private fun observeFavorites() {
        viewModelScope.launch {
            // 1. Immediately hydrate from Room database
            try {
                val roomFavs = musicDatabase.favoriteTrackDao().getAllFavoriteIds().toSet()
                if (roomFavs.isNotEmpty()) {
                    _favoriteTrackIds.value = roomFavs
                    val currentTrackId = _playerUiState.value.currentTrack.id
                    if (currentTrackId.isNotBlank()) {
                        val isFav = roomFavs.contains(currentTrackId)
                        _playerUiState.update { it.copy(isFavorite = isFav) }
                        _lyricsUiState.update { lState ->
                            if (lState is LyricsUiState.Success) lState.copy(isFavorite = isFav) else lState
                        }
                    }
                }
            } catch (e: Exception) {
                Log.w("PlayerViewModel", "Error loading initial Room favorites: ${e.message}")
            }

            // 2. Reactively observe DataStore and sync two-way
            sessionManager.favoritesFlow.collect { ids ->
                if (ids.isNotEmpty()) {
                    val combined = _favoriteTrackIds.value + ids
                    _favoriteTrackIds.value = combined
                    try {
                        ids.forEach { id ->
                            musicDatabase.favoriteTrackDao().addFavorite(FavoriteTrackEntity(id))
                        }
                    } catch (_: Exception) {}
                }
                val currentTrackId = _playerUiState.value.currentTrack.id
                val isFav = _favoriteTrackIds.value.contains(currentTrackId)
                _playerUiState.update { it.copy(isFavorite = isFav) }
                _lyricsUiState.update { lState ->
                    if (lState is LyricsUiState.Success) lState.copy(isFavorite = isFav) else lState
                }
            }
        }

        // 3. Reactively observe Room database changes
        viewModelScope.launch {
            try {
                musicDatabase.favoriteTrackDao().getAllFavoriteIdsFlow().collect { idsList ->
                    val ids = idsList.toSet()
                    if (ids.isNotEmpty() || _favoriteTrackIds.value.isNotEmpty()) {
                        _favoriteTrackIds.value = ids
                        val currentTrackId = _playerUiState.value.currentTrack.id
                        val isFav = ids.contains(currentTrackId)
                        _playerUiState.update { it.copy(isFavorite = isFav) }
                        _lyricsUiState.update { lState ->
                            if (lState is LyricsUiState.Success) lState.copy(isFavorite = isFav) else lState
                        }
                    }
                }
            } catch (e: Exception) {
                Log.w("PlayerViewModel", "Error in Room favorites flow: ${e.message}")
            }
        }
    }

    private fun loadCachedTracksInitial() {
        viewModelScope.launch(Dispatchers.IO) {
            val settings = try {
                settingsRepository.settingsFlow.first()
            } catch (e: Exception) {
                _settingsState.value
            }
            val cached = mediaRepository.getCachedTracksFast(settings)
            if (cached.isNotEmpty()) {
                musicTrackerRepository.reconcileStats(cached)
                val session = sessionManager.getSession()
                val shouldShuffle = session.isShuffle || settings.alwaysShuffle
                val initialList = if (shouldShuffle) cached.shuffled() else cached

                _homeUiState.update { current ->
                    current.copy(
                        tracks = cached,
                        filteredTracks = filterTracks(cached, current.searchQuery),
                        minDurationFilterSec = settings.minDurationSec,
                        blacklistedFoldersCount = settings.blacklistedFolders.size,
                        whitelistedFoldersCount = settings.whitelistedFolders.size,
                        totalCount = cached.size
                    )
                }
                _playerUiState.update { current ->
                    if (current.isPlaying) {
                        current
                    } else {
                        current.copy(
                            playlist = initialList,
                            queueCount = initialList.size,
                            isShuffle = shouldShuffle,
                            isRepeat = session.isRepeat
                        )
                    }
                }

                // Restore last playback session (track, position, queue) if not currently playing
                val targetTrack = cached.find { it.contentUri == session.lastPlayedTrackUri || it.id == session.lastPlayedTrackId }
                    ?: cached.firstOrNull()

                if (targetTrack != null) {
                    val savedPos = session.lastPlaybackPositionMs.coerceIn(0L, targetTrack.durationMs)
                    val index = initialList.indexOfFirst { it.id == targetTrack.id }.coerceAtLeast(0)
                    val isFav = _favoriteTrackIds.value.contains(targetTrack.id) || sessionManager.isFavorite(targetTrack.id)
                    _playerUiState.update { current ->
                        if (current.isPlaying) {
                            current
                        } else {
                            current.copy(
                                currentTrack = targetTrack,
                                currentPositionMs = savedPos,
                                durationMs = targetTrack.durationMs,
                                currentTrackIndex = index,
                                isPlaying = false,
                                isFavorite = isFav,
                                isShuffle = shouldShuffle,
                                isRepeat = session.isRepeat
                            )
                        }
                    }
                }
            }
        }
    }

    private fun createMediaItem(track: Track): MediaItem {
        val metadataBuilder = MediaMetadata.Builder()
            .setTitle(track.title)
            .setArtist(track.artist)
            .setAlbumTitle(track.album)

        val artUri = track.albumArtUri ?: track.coverUrl
        if (!artUri.isNullOrBlank()) {
            metadataBuilder.setArtworkUri(Uri.parse(artUri))
        } else {
            track.coverResId?.let { resId ->
                val uri = Uri.parse(
                    "android.resource://${getApplication<Application>().packageName}/$resId"
                )
                metadataBuilder.setArtworkUri(uri)
            }
        }

        val mediaItemBuilder = MediaItem.Builder()
            .setMediaId(track.id)
            .setMediaMetadata(metadataBuilder.build())

        if (!track.contentUri.isNullOrBlank()) {
            mediaItemBuilder.setUri(Uri.parse(track.contentUri))
        }

        return mediaItemBuilder.build()
    }

    private fun updatePlayerArtwork(track: Track) {
        val currentIndex = exoPlayer.currentMediaItemIndex
        if (currentIndex == C.INDEX_UNSET) return
        val currentItem = exoPlayer.currentMediaItem ?: return

        val artUri = track.albumArtUri ?: track.coverUrl
        val targetArtworkUri = if (!artUri.isNullOrBlank()) {
            Uri.parse(artUri)
        } else {
            track.coverResId?.let { resId ->
                Uri.parse("android.resource://${getApplication<Application>().packageName}/$resId")
            }
        }

        val metadata = currentItem.mediaMetadata
        val updatedMetadata = metadata.buildUpon()
            .setArtworkUri(targetArtworkUri)
            .build()

        val updatedItem = currentItem.buildUpon()
            .setMediaMetadata(updatedMetadata)
            .build()

        exoPlayer.replaceMediaItem(currentIndex, updatedItem)
    }

    private fun prepareExoPlayerPaused(track: Track, positionMs: Long) {
        audioEngineManager.onTrackLoaded(
            sampleRateHz = track.sampleRateHz,
            bitDepth = track.bitDepth,
            formatName = track.formatName
        )
        try {
            if (!track.contentUri.isNullOrBlank()) {
                isSimulatedPlayback = false
                val mediaItem = createMediaItem(track)
                exoPlayer.setMediaItem(mediaItem)
                exoPlayer.prepare()
                exoPlayer.seekTo(positionMs)
                exoPlayer.pause()
            } else {
                isSimulatedPlayback = true
                exoPlayer.stop()
                exoPlayer.clearMediaItems()
            }
        } catch (e: Exception) {
            Log.w("PlayerViewModel", "Falling back to simulated paused state: ${e.message}")
            isSimulatedPlayback = true
        }
    }

    private fun observeSystemVolume() {
        viewModelScope.launch {
            systemVolumeController.volumeFraction.collect { vol ->
                _playerUiState.update { it.copy(volume = vol) }
            }
        }
    }

    private fun observeDeviceChanges() {
        viewModelScope.launch {
            audioOutputDetector.currentDevice.collect { device ->
                _playerUiState.update { it.copy(activeDevice = device) }
            }
        }
    }

    private fun observeSettingsChanges() {
        viewModelScope.launch {
            settingsRepository.settingsFlow.collect { settings ->
                _settingsState.value = settings
                _homeUiState.update {
                    it.copy(
                        minDurationFilterSec = settings.minDurationSec,
                        blacklistedFoldersCount = settings.blacklistedFolders.size,
                        whitelistedFoldersCount = settings.whitelistedFolders.size
                    )
                }
                val newHash = (settings.whitelistedFolders.hashCode() * 31 + settings.blacklistedFolders.hashCode()) * 31 + settings.minDurationSec + (if (settings.useWhitelistOnly) 1 else 0)
                val filterChanged = lastFolderFilterHash != null && lastFolderFilterHash != newHash
                val isFirstLoad = lastFolderFilterHash == null
                lastFolderFilterHash = newHash

                if (filterChanged) {
                    if (_homeUiState.value.hasStoragePermission) {
                        scanLocalStorage(isInitialSilentScan = true)
                    } else {
                        viewModelScope.launch(Dispatchers.IO) {
                            val cached = mediaRepository.getCachedTracksFast(settings)
                            _homeUiState.update { current ->
                                current.copy(
                                    tracks = cached,
                                    filteredTracks = filterTracks(cached, current.searchQuery),
                                    totalCount = cached.size
                                )
                            }
                        }
                    }
                } else if (isFirstLoad && (settings.whitelistedFolders.isNotEmpty() || settings.blacklistedFolders.isNotEmpty() || settings.minDurationSec > 0)) {
                    viewModelScope.launch(Dispatchers.IO) {
                        val cached = mediaRepository.getCachedTracksFast(settings)
                        _homeUiState.update { current ->
                            current.copy(
                                tracks = cached,
                                filteredTracks = filterTracks(cached, current.searchQuery),
                                totalCount = cached.size
                            )
                        }
                    }
                }
            }
        }
    }

    private fun setupExoPlayerListener() {
        exoPlayer.volume = 1.0f

        exoPlayer.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                _playerUiState.update { it.copy(isPlaying = isPlaying) }
                updateLyricsPlayingState(isPlaying)
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                when (playbackState) {
                    Player.STATE_READY -> {
                        val duration = exoPlayer.duration
                        if (duration > 0) {
                            _playerUiState.update { it.copy(durationMs = duration) }
                        }
                        equalizerManager.bindAudioSession(exoPlayer.audioSessionId)
                    }
                    Player.STATE_ENDED -> {
                        if (!isCrossfadeTransitionTriggered) {
                            isCrossfadeTransitionTriggered = true
                            val endedTrack = _playerUiState.value.currentTrack
                            onTrackPlaybackCompleted(endedTrack.id, endedTrack.durationMs)
                            skipNext()
                        }
                    }
                    Player.STATE_BUFFERING -> {}
                    Player.STATE_IDLE -> {}
                }
            }

            override fun onPlayerError(error: PlaybackException) {
                Log.w("PlayerViewModel", "ExoPlayer error: ${error.message}. Simulated fallback.")
                isSimulatedPlayback = true
                _playerUiState.update { it.copy(isPlaying = true) }
                updateLyricsPlayingState(true)
            }
        })
    }

    private fun updateLyricsPlayingState(isPlaying: Boolean) {
        _lyricsUiState.update { state ->
            if (state is LyricsUiState.Success) {
                state.copy(isPlaying = isPlaying)
            } else {
                state
            }
        }
    }

    private var trackerTickCount = 0
    private var isCrossfadeTransitionTriggered = false

    private fun startProgressTicker() {
        progressTickerJob?.cancel()
        progressTickerJob = viewModelScope.launch {
            var lastTrackerTime = System.currentTimeMillis()
            while (isActive) {
                val isPlaying = _playerUiState.value.isPlaying
                delay(if (isPlaying) 50L else 250L)
                if (isPlaying) {
                    val currentTrack = _playerUiState.value.currentTrack
                    val speed = _playerUiState.value.playbackSpeed
                    val newPosition = if (isSimulatedPlayback || exoPlayer.duration <= 0) {
                        val next = _playerUiState.value.currentPositionMs + (50L * speed).toLong()
                        if (next >= currentTrack.durationMs) {
                            if (!isCrossfadeTransitionTriggered) {
                                isCrossfadeTransitionTriggered = true
                                onTrackPlaybackCompleted(currentTrack.id, currentTrack.durationMs)
                                skipNext()
                            }
                            0L
                        } else {
                            next
                        }
                    } else {
                        exoPlayer.currentPosition
                    }

                    // Handle smooth crossfade out near end of track
                    val isCrossfade = _settingsState.value.crossfadeEnabled && _settingsState.value.crossfadeDurationSec > 0
                    val duration = if (exoPlayer.duration > 0) exoPlayer.duration else currentTrack.durationMs
                    val crossfadeMs = _settingsState.value.crossfadeDurationSec * 1000L

                    if (isCrossfade && !isSimulatedPlayback && duration > crossfadeMs * 2 && newPosition > 0) {
                        val remainingMs = duration - newPosition
                        if (remainingMs <= crossfadeMs && remainingMs > 0) {
                            if (remainingMs <= 100L) {
                                if (!isCrossfadeTransitionTriggered) {
                                    isCrossfadeTransitionTriggered = true
                                    onTrackPlaybackCompleted(currentTrack.id, currentTrack.durationMs)
                                    skipNext()
                                }
                            } else if (!isCrossfadeTransitionTriggered) {
                                val fraction = (remainingMs.toFloat() / crossfadeMs.toFloat()).coerceIn(0f, 1f)
                                val curvedVolume = kotlin.math.sin(fraction * (Math.PI / 2.0)).toFloat()
                                exoPlayer.volume = curvedVolume.coerceIn(0f, 1f)
                            }
                        }
                    }

                    val now = System.currentTimeMillis()
                    if (now - lastTrackerTime >= 5000L && currentTrack.id.isNotBlank()) {
                        val delta = now - lastTrackerTime
                        musicTrackerRepository.recordListenTime(currentTrack.id, delta, currentTrack.durationMs, isCompleted = false)
                        lastTrackerTime = now
                    }

                    _playerUiState.update { it.copy(currentPositionMs = newPosition) }
                    _lyricsUiState.update { state ->
                        if (state is LyricsUiState.Success) {
                            state.copy(currentPlaybackPositionMs = newPosition)
                        } else {
                            state
                        }
                    }
                }
            }
        }
    }

    fun setStoragePermission(granted: Boolean) {
        val wasGranted = _homeUiState.value.hasStoragePermission
        _homeUiState.update { it.copy(hasStoragePermission = granted) }
        // Only trigger scan if permission was newly granted, or if local music has never been scanned
        // DO NOT reload all music on every app open!
        if (granted && (!wasGranted || _homeUiState.value.tracks.isEmpty() || !hasScannedStorage)) {
            hasScannedStorage = true
            scanLocalStorage(isInitialSilentScan = true)
        }
    }

    fun setMicrophonePermission(granted: Boolean) {
        _homeUiState.update { it.copy(hasMicrophonePermission = granted) }
    }

    fun setNearbyDevicesPermission(granted: Boolean) {
        _homeUiState.update { it.copy(hasNearbyDevicesPermission = granted) }
        if (granted) {
            audioOutputDetector.refreshDevice()
        }
    }

    fun scanLocalStorage(isInitialSilentScan: Boolean = false) {
        viewModelScope.launch(Dispatchers.IO) {
            if (!isInitialSilentScan || _homeUiState.value.tracks.isEmpty()) {
                _homeUiState.update { it.copy(isScanning = true) }
            }
            val currentSettings = try {
                settingsRepository.settingsFlow.first()
            } catch (_: Exception) {
                settingsRepository.getSettingsFastSync()
            }
            _settingsState.value = currentSettings
            val scanned = mediaRepository.scanLocalAudio(currentSettings)
            val session = sessionManager.getSession()
            val trackMap = scanned.associateBy { it.id }
            val savedQueue = if (session.lastPlaylistQueue.isNotEmpty()) {
                session.lastPlaylistQueue.mapNotNull { trackMap[it] }
            } else emptyList()

            val shouldShuffle = _playerUiState.value.isShuffle || currentSettings.alwaysShuffle
            val orderedList = when {
                savedQueue.isNotEmpty() -> savedQueue
                shouldShuffle -> scanned.shuffled()
                else -> scanned
            }

            val filtered = filterTracks(scanned, _homeUiState.value.searchQuery)
            musicTrackerRepository.reconcileStats(scanned)

            _homeUiState.update { current ->
                current.copy(
                    tracks = scanned,
                    filteredTracks = filtered,
                    isScanning = false,
                    totalCount = scanned.size
                )
            }

            // DO NOT disruptively overwrite active playback or existing user queue
            _playerUiState.update { current ->
                val currentTrackStillExists = scanned.any { it.id == current.currentTrack.id }
                val currentTrack = if (currentTrackStillExists) current.currentTrack else (savedQueue.firstOrNull() ?: scanned.firstOrNull() ?: current.currentTrack)
                val activePlaylist = if (current.isPlaying && current.playlist.isNotEmpty()) current.playlist else orderedList
                val activeIndex = activePlaylist.indexOfFirst { it.id == currentTrack.id }.coerceAtLeast(0)
                current.copy(
                    currentTrack = currentTrack,
                    currentTrackIndex = activeIndex,
                    playlist = activePlaylist,
                    queueCount = activePlaylist.size
                )
            }

            // If no track is currently loaded or playing, restore the last session track & position
            if (!_playerUiState.value.isPlaying && scanned.isNotEmpty() && _playerUiState.value.currentTrack.id.isBlank()) {
                val targetTrack = scanned.find { it.contentUri == session.lastPlayedTrackUri || it.id == session.lastPlayedTrackId }
                    ?: savedQueue.firstOrNull()
                    ?: scanned.firstOrNull()

                if (targetTrack != null) {
                    val savedPos = if (_playerUiState.value.currentPositionMs > 0L) {
                        _playerUiState.value.currentPositionMs
                    } else {
                        session.lastPlaybackPositionMs.coerceIn(0L, targetTrack.durationMs)
                    }
                    val index = orderedList.indexOfFirst { it.id == targetTrack.id }.coerceAtLeast(0)
                    val isFav = _favoriteTrackIds.value.contains(targetTrack.id) || sessionManager.isFavorite(targetTrack.id)
                    _playerUiState.update { current ->
                        current.copy(
                            currentTrack = targetTrack,
                            currentPositionMs = savedPos,
                            durationMs = targetTrack.durationMs,
                            currentTrackIndex = index,
                            playlist = orderedList,
                            queueCount = orderedList.size,
                            isFavorite = isFav,
                            isShuffle = shouldShuffle,
                            isRepeat = session.isRepeat
                        )
                    }
                }
            }

            // If lyrics are not yet loaded for the active track, load them now
            val activeTrack = _playerUiState.value.currentTrack
            if (activeTrack.id.isNotBlank()) {
                val currentLyricsTrackId = (_lyricsUiState.value as? LyricsUiState.Success)?.track?.id
                if (currentLyricsTrackId != activeTrack.id && _lyricsUiState.value !is LyricsUiState.Loading) {
                    loadLyricsForTrack(activeTrack)
                }
            }

            hasScannedStorage = true
        }
    }

    fun onSearchQueryChanged(query: String) {
        _homeUiState.update { current ->
            val filtered = filterTracks(current.tracks, query)
            current.copy(
                searchQuery = query,
                filteredTracks = filtered
            )
        }
    }

    private fun filterTracks(tracks: List<Track>, query: String): List<Track> {
        if (query.isBlank()) return tracks
        val q = query.trim().lowercase()
        return tracks.filter { track ->
            track.title.lowercase().contains(q) ||
                track.artist.lowercase().contains(q) ||
                track.album.lowercase().contains(q)
        }
    }

    fun playTrack(track: Track, contextList: List<Track>? = null) {
        isCrossfadeTransitionTriggered = false
        val shouldShuffle = _playerUiState.value.isShuffle || settingsState.value.alwaysShuffle
        val playlist = when {
            contextList != null && contextList.isNotEmpty() -> {
                if (shouldShuffle) {
                    listOf(track) + (contextList.filter { it.id != track.id }).shuffled()
                } else {
                    contextList
                }
            }
            _playerUiState.value.playlist.any { it.id == track.id } -> {
                _playerUiState.value.playlist
            }
            else -> {
                val currentSorted = _homeUiState.value.filteredTracks.ifEmpty { _homeUiState.value.tracks }
                val baseList = currentSorted.ifEmpty { listOf(track) }
                if (shouldShuffle) {
                    listOf(track) + (baseList.filter { it.id != track.id }).shuffled()
                } else {
                    baseList
                }
            }
        }
        val index = playlist.indexOfFirst { it.id == track.id }.coerceAtLeast(0)
        val isFav = _favoriteTrackIds.value.contains(track.id)

        _playerUiState.update {
            it.copy(
                currentTrack = track,
                currentPositionMs = 0L,
                durationMs = track.durationMs,
                isPlaying = true,
                currentTrackIndex = index,
                playlist = playlist,
                queueCount = playlist.size,
                isNowPlayingExpanded = true,
                isFavorite = isFav,
                isShuffle = shouldShuffle
            )
        }

        // Fetch lyrics via the 3-step sequence
        loadLyricsForTrack(track)
        extractAndApplyArtworkColors(track)
        startExoPlayback(track)

        // If Chromecast is actively connected, stream the track to the Cast receiver
        if (chromecastManager.uiState.value.activeDevice != null) {
            chromecastManager.playTrackOnCast(track)
        }

        viewModelScope.launch {
            musicTrackerRepository.recordPlay(track.id, track.durationMs)
        }

        viewModelScope.launch {
            sessionManager.saveSession(
                trackUri = track.contentUri,
                trackId = track.id,
                positionMs = 0L,
                queueIds = playlist.map { it.id },
                isShuffle = shouldShuffle,
                isRepeat = _playerUiState.value.isRepeat
            )
        }

        // Fetch online artwork fallback if online artwork is not yet attached
        val hasOnlineCover = track.coverUrl?.startsWith("http", ignoreCase = true) == true
        if (track.coverResId == null && !hasOnlineCover) {
            viewModelScope.launch {
                val onlineCover = com.example.data.ArtworkResolver.resolveArtworkUrl(
                    track.artist,
                    track.album.ifBlank { track.title }
                )
                if (onlineCover != null) {
                    val updatedTrack = track.copy(coverUrl = onlineCover)
                    val (primary, secondary) = com.example.data.ArtworkResolver.extractTrackColors(getApplication(), updatedTrack)
                    _playerUiState.update { state ->
                        if (state.currentTrack.id == track.id) {
                            state.copy(
                                currentTrack = updatedTrack.copy(
                                    primaryAmbientColor = primary,
                                    secondaryAmbientColor = secondary
                                )
                            )
                        } else state
                    }
                    updatePlayerArtwork(updatedTrack)
                }
            }
        }
    }

    private fun extractAndApplyArtworkColors(track: Track) {
        viewModelScope.launch {
            val (primary, secondary) = com.example.data.ArtworkResolver.extractTrackColors(getApplication(), track)
            _playerUiState.update { current ->
                if (current.currentTrack.id == track.id) {
                    current.copy(
                        currentTrack = current.currentTrack.copy(
                            primaryAmbientColor = primary,
                            secondaryAmbientColor = secondary
                        )
                    )
                } else current
            }
        }
    }

    private fun loadLyricsForTrack(track: Track) {
        lyricsFetchJob?.cancel()
        _lyricsUiState.value = LyricsUiState.Loading

        lyricsFetchJob = viewModelScope.launch {
            val result = lyricsManager.fetchLyricsForTrack(track)
            val finalResult = if (result is LyricsUiState.Success) {
                result.copy(
                    isPlaying = _playerUiState.value.isPlaying,
                    isFavorite = _playerUiState.value.isFavorite
                )
            } else {
                result
            }
            _lyricsUiState.value = finalResult
        }
    }

    fun saveCustomLyrics(
        trackId: String,
        lines: List<LyricLine>,
        plainText: String?,
        isSynced: Boolean
    ) {
        viewModelScope.launch {
            val lrcContent = if (lines.isNotEmpty()) {
                com.example.lyrics.LyricsParser.formatToLrc(lines)
            } else plainText ?: ""

            lyricsManager.saveCustomLyrics(trackId, lrcContent, plainText, isSynced)

            if (_playerUiState.value.currentTrack.id == trackId) {
                _lyricsUiState.value = LyricsUiState.Success(
                    track = _playerUiState.value.currentTrack,
                    lines = lines,
                    currentPlaybackPositionMs = _playerUiState.value.currentPositionMs,
                    isPlaying = _playerUiState.value.isPlaying,
                    isFavorite = _playerUiState.value.isFavorite,
                    isSynced = isSynced,
                    plainLyrics = plainText
                )
            }
        }
    }

    suspend fun searchOnlineLyrics(artist: String, title: String): Pair<String?, String?> {
        return lyricsManager.searchOnlineLyrics(artist, title)
    }

    fun reloadLyricsForCurrentTrack() {
        loadLyricsForTrack(_playerUiState.value.currentTrack)
    }

    private var fadeJob: Job? = null

    private fun triggerCrossfadeVolumeIn(durationSec: Int) {
        fadeJob?.cancel()
        if (!_settingsState.value.crossfadeEnabled || durationSec <= 0) {
            exoPlayer.volume = 1.0f
            return
        }
        fadeJob = viewModelScope.launch {
            val totalDurationMs = durationSec * 1000L
            val steps = (durationSec * 20).coerceIn(20, 80)
            val stepDelay = (totalDurationMs / steps).coerceAtLeast(15L)
            for (i in 0..steps) {
                val fraction = i / steps.toFloat()
                val curvedVolume = kotlin.math.sin(fraction * (Math.PI / 2.0)).toFloat()
                exoPlayer.volume = curvedVolume.coerceIn(0f, 1f)
                delay(stepDelay)
            }
            exoPlayer.volume = 1.0f
        }
    }

    private fun startExoPlayback(track: Track) {
        audioEngineManager.onTrackLoaded(
            sampleRateHz = track.sampleRateHz,
            bitDepth = track.bitDepth,
            formatName = track.formatName
        )
        startPlaybackService()
        try {
            if (!track.contentUri.isNullOrBlank()) {
                isSimulatedPlayback = false
                val isCrossfade = _settingsState.value.crossfadeEnabled && _settingsState.value.crossfadeDurationSec > 0
                if (isCrossfade) {
                    fadeJob?.cancel()
                    exoPlayer.volume = 0f
                } else {
                    fadeJob?.cancel()
                    exoPlayer.volume = 1.0f
                }
                val mediaItem = createMediaItem(track)
                exoPlayer.setMediaItem(mediaItem)
                exoPlayer.setPlaybackSpeed(_playerUiState.value.playbackSpeed)
                exoPlayer.prepare()
                exoPlayer.play()
                if (isCrossfade) {
                    triggerCrossfadeVolumeIn(_settingsState.value.crossfadeDurationSec)
                }
                equalizerManager.bindAudioSession(exoPlayer.audioSessionId)
            } else {
                isSimulatedPlayback = true
                exoPlayer.stop()
                exoPlayer.clearMediaItems()
                _playerUiState.update { it.copy(isPlaying = true) }
                updateLyricsPlayingState(true)
            }
        } catch (e: Exception) {
            Log.e("PlayerViewModel", "Error starting playback", e)
            isSimulatedPlayback = true
            _playerUiState.update { it.copy(isPlaying = true) }
            updateLyricsPlayingState(true)
        }
    }

    fun togglePlayPause() {
        // Ensure lyrics are loaded for current track if missing
        val currentTrack = _playerUiState.value.currentTrack
        if (currentTrack.id.isNotBlank()) {
            val lyricsTrackId = (_lyricsUiState.value as? LyricsUiState.Success)?.track?.id
            if (lyricsTrackId != currentTrack.id && _lyricsUiState.value !is LyricsUiState.Loading) {
                loadLyricsForTrack(currentTrack)
            }
        }

        if (isSimulatedPlayback) {
            val next = !_playerUiState.value.isPlaying
            _playerUiState.update { it.copy(isPlaying = next) }
            updateLyricsPlayingState(next)
        } else {
            if (exoPlayer.isPlaying) {
                exoPlayer.pause()
            } else {
                if (exoPlayer.currentMediaItem == null) {
                    val savedPos = _playerUiState.value.currentPositionMs
                    playTrack(currentTrack)
                    if (savedPos > 0L) {
                        seekTo(savedPos)
                    }
                } else {
                    if (exoPlayer.playbackState == Player.STATE_ENDED) {
                        exoPlayer.seekTo(0)
                    }
                    exoPlayer.play()
                }
            }
        }
    }

    fun pause() {
        if (isSimulatedPlayback) {
            _playerUiState.update { it.copy(isPlaying = false) }
            updateLyricsPlayingState(false)
        } else {
            exoPlayer.pause()
        }
        updatePlaybackServiceNotification(isPlaying = false)
        viewModelScope.launch {
            sessionManager.updatePosition(_playerUiState.value.currentPositionMs)
        }
    }

    fun seekTo(positionMs: Long) {
        isCrossfadeTransitionTriggered = false
        val duration = _playerUiState.value.currentTrack.durationMs
        val clamped = positionMs.coerceIn(0L, duration)

        if (!isSimulatedPlayback) {
            if (exoPlayer.currentMediaItem == null && _playerUiState.value.currentTrack.id.isNotBlank()) {
                prepareExoPlayerPaused(_playerUiState.value.currentTrack, clamped)
            } else if (exoPlayer.duration > 0) {
                exoPlayer.seekTo(clamped)
                // If user sought away from the end, ensure full volume
                val crossfadeMs = _settingsState.value.crossfadeDurationSec * 1000L
                if (duration - clamped > crossfadeMs) {
                    fadeJob?.cancel()
                    exoPlayer.volume = 1.0f
                }
            }
        }
        _playerUiState.update { it.copy(currentPositionMs = clamped) }
        viewModelScope.launch {
            sessionManager.updatePosition(clamped)
        }
        _lyricsUiState.update { state ->
            if (state is LyricsUiState.Success) {
                state.copy(currentPlaybackPositionMs = clamped)
            } else {
                state
            }
        }
    }

    fun skipNext() {
        val playlist = _playerUiState.value.playlist.ifEmpty { _homeUiState.value.tracks }
        if (playlist.isEmpty()) return

        if (_playerUiState.value.isRepeat) {
            val currentTrack = _playerUiState.value.currentTrack
            val currentIndex = _playerUiState.value.currentTrackIndex
            loadTrack(currentTrack, currentIndex)
            return
        }

        val nextIndex = (_playerUiState.value.currentTrackIndex + 1) % playlist.size
        val nextTrack = playlist[nextIndex]
        loadTrack(nextTrack, nextIndex)
    }

    fun skipPrevious() {
        if (_playerUiState.value.currentPositionMs > 3000L) {
            seekTo(0L)
            return
        }

        val playlist = _playerUiState.value.playlist.ifEmpty { _homeUiState.value.tracks }
        if (playlist.isEmpty()) return

        val prevIndex = if (_playerUiState.value.currentTrackIndex - 1 < 0) {
            playlist.size - 1
        } else {
            _playerUiState.value.currentTrackIndex - 1
        }
        val prevTrack = playlist[prevIndex]
        loadTrack(prevTrack, prevIndex)
    }

    private fun loadTrack(track: Track, index: Int) {
        isCrossfadeTransitionTriggered = false
        _playerUiState.update {
            it.copy(
                currentTrack = track,
                currentPositionMs = 0L,
                durationMs = track.durationMs,
                currentTrackIndex = index,
                isPlaying = true
            )
        }

        loadLyricsForTrack(track)
        extractAndApplyArtworkColors(track)
        startExoPlayback(track)

        viewModelScope.launch {
            musicTrackerRepository.recordPlay(track.id, track.durationMs)
        }

        viewModelScope.launch {
            sessionManager.saveSession(
                trackUri = track.contentUri,
                trackId = track.id,
                positionMs = 0L,
                queueIds = _playerUiState.value.playlist.map { it.id },
                isShuffle = _playerUiState.value.isShuffle,
                isRepeat = _playerUiState.value.isRepeat
            )
        }
    }

    fun toggleShuffle() {
        val nextShuffle = !_playerUiState.value.isShuffle
        _playerUiState.update { current ->
            val currentTrack = current.currentTrack
            val currentPlaylist = current.playlist
            val updatedPlaylist = if (nextShuffle && currentPlaylist.isNotEmpty()) {
                if (currentTrack.id.isNotBlank()) {
                    listOf(currentTrack) + (currentPlaylist - currentTrack).shuffled()
                } else {
                    currentPlaylist.shuffled()
                }
            } else {
                _homeUiState.value.tracks.ifEmpty { currentPlaylist }
            }
            val newIndex = updatedPlaylist.indexOfFirst { it.id == currentTrack.id }.coerceAtLeast(0)
            current.copy(
                isShuffle = nextShuffle,
                playlist = updatedPlaylist,
                currentTrackIndex = newIndex,
                queueCount = updatedPlaylist.size
            )
        }
        viewModelScope.launch {
            sessionManager.updatePlaybackModes(
                isShuffle = nextShuffle,
                isRepeat = _playerUiState.value.isRepeat
            )
        }
    }

    fun toggleRepeat() {
        val nextRepeat = !_playerUiState.value.isRepeat
        _playerUiState.update { it.copy(isRepeat = nextRepeat) }
        viewModelScope.launch {
            sessionManager.updatePlaybackModes(
                isShuffle = _playerUiState.value.isShuffle,
                isRepeat = nextRepeat
            )
        }
    }

    fun shuffleAll() {
        val list = _homeUiState.value.filteredTracks.ifEmpty { _homeUiState.value.tracks }
        if (list.isNotEmpty()) {
            val shuffled = list.shuffled()
            _playerUiState.update { it.copy(playlist = shuffled, queueCount = shuffled.size) }
            playTrack(shuffled.first())
        }
    }

    fun toggleFavoriteTrack(trackId: String) {
        if (trackId.isBlank()) return
        val currentSet = _favoriteTrackIds.value
        val isNowFav = !currentSet.contains(trackId)
        val newSet = if (isNowFav) currentSet + trackId else currentSet - trackId
        _favoriteTrackIds.value = newSet

        if (_playerUiState.value.currentTrack.id == trackId) {
            _playerUiState.update { it.copy(isFavorite = isNowFav) }
            _lyricsUiState.update { lyricsState ->
                if (lyricsState is LyricsUiState.Success) {
                    lyricsState.copy(isFavorite = isNowFav)
                } else {
                    lyricsState
                }
            }
        }

        viewModelScope.launch {
            sessionManager.toggleFavorite(trackId)
            try {
                if (isNowFav) {
                    musicDatabase.favoriteTrackDao().addFavorite(FavoriteTrackEntity(trackId))
                } else {
                    musicDatabase.favoriteTrackDao().removeFavorite(trackId)
                }
            } catch (e: Exception) {
                Log.w("PlayerViewModel", "Error persisting favorite to Room: ${e.message}")
            }
            updatePlaybackServiceNotification()
        }
    }

    fun toggleFavorite() {
        val currentTrack = _playerUiState.value.currentTrack
        if (currentTrack.id.isNotBlank()) {
            toggleFavoriteTrack(currentTrack.id)
        }
    }

    private fun persistCurrentQueue(updatedList: List<Track>? = null) {
        viewModelScope.launch {
            val q = updatedList ?: _playerUiState.value.playlist
            sessionManager.saveSession(
                trackUri = _playerUiState.value.currentTrack.contentUri,
                trackId = _playerUiState.value.currentTrack.id,
                positionMs = _playerUiState.value.currentPositionMs,
                queueIds = q.map { it.id },
                isShuffle = _playerUiState.value.isShuffle,
                isRepeat = _playerUiState.value.isRepeat
            )
        }
    }

    fun addToQueue(track: Track) {
        if (track.id.isBlank()) return
        _playerUiState.update { state ->
            val updated = state.playlist + track
            persistCurrentQueue(updated)
            state.copy(playlist = updated, queueCount = updated.size)
        }
    }

    fun playNext(track: Track) {
        if (track.id.isBlank()) return
        _playerUiState.update { state ->
            val currentIndex = state.currentTrackIndex
            val list = state.playlist.toMutableList()
            val insertIndex = (currentIndex + 1).coerceAtMost(list.size)
            list.add(insertIndex, track)
            persistCurrentQueue(list)
            state.copy(playlist = list, queueCount = list.size)
        }
    }

    fun deleteTrack(track: Track) {
        if (track.id.isBlank()) return
        viewModelScope.launch {
            mediaRepository.deleteTrack(track)
            _homeUiState.update { state ->
                val newTracks = state.tracks.filter { it.id != track.id }
                state.copy(
                    tracks = newTracks,
                    filteredTracks = state.filteredTracks.filter { it.id != track.id }
                )
            }
            _playerUiState.update { state ->
                val newPlaylist = state.playlist.filter { it.id != track.id }
                if (state.currentTrack.id == track.id) {
                    if (newPlaylist.isNotEmpty()) {
                        state.copy(playlist = newPlaylist, queueCount = newPlaylist.size)
                    } else {
                        state.copy(
                            currentTrack = Track(id = "", title = "No Track Selected", artist = "", album = "", durationMs = 0L),
                            isPlaying = false,
                            playlist = emptyList(),
                            queueCount = 0
                        )
                    }
                } else {
                    state.copy(playlist = newPlaylist, queueCount = newPlaylist.size)
                }
            }
            if (_playerUiState.value.currentTrack.id == track.id) {
                if (_playerUiState.value.playlist.isNotEmpty()) {
                    playTrack(_playerUiState.value.playlist.first())
                } else {
                    pause()
                }
            }
        }
    }

    fun updateTrackMetadata(trackId: String, newTitle: String, newArtist: String, newAlbum: String) {
        if (trackId.isBlank()) return
        viewModelScope.launch {
            mediaRepository.updateTrackMetadata(trackId, newTitle, newArtist, newAlbum)
            _homeUiState.update { state ->
                val updatedTracks = state.tracks.map { tr ->
                    if (tr.id == trackId) tr.copy(title = newTitle, artist = newArtist, album = newAlbum) else tr
                }
                val updatedFiltered = state.filteredTracks.map { tr ->
                    if (tr.id == trackId) tr.copy(title = newTitle, artist = newArtist, album = newAlbum) else tr
                }
                state.copy(tracks = updatedTracks, filteredTracks = updatedFiltered)
            }
            _playerUiState.update { state ->
                val updatedPlaylist = state.playlist.map { tr ->
                    if (tr.id == trackId) tr.copy(title = newTitle, artist = newArtist, album = newAlbum) else tr
                }
                val updatedCurrent = if (state.currentTrack.id == trackId) {
                    state.currentTrack.copy(title = newTitle, artist = newArtist, album = newAlbum)
                } else {
                    state.currentTrack
                }
                state.copy(currentTrack = updatedCurrent, playlist = updatedPlaylist)
            }
        }
    }

    fun setVolume(newVolume: Float) {
        val clamped = newVolume.coerceIn(0f, 1f)
        systemVolumeController.setSystemVolumeFraction(clamped)
        exoPlayer.volume = clamped
        _playerUiState.update { it.copy(volume = clamped) }
    }

    fun setPlaybackSpeed(speed: Float) {
        val clamped = speed.coerceIn(0.25f, 3.0f)
        try {
            exoPlayer.setPlaybackSpeed(clamped)
        } catch (_: Exception) {}
        _playerUiState.update { it.copy(playbackSpeed = clamped) }
    }

    fun cyclePlaybackSpeed() {
        val speedCycle = listOf(0.5f, 0.75f, 1.0f, 1.25f, 1.5f, 2.0f)
        val currentSpeed = _playerUiState.value.playbackSpeed
        val currentIndex = speedCycle.indexOfFirst { kotlin.math.abs(it - currentSpeed) < 0.05f }
        val nextSpeed = if (currentIndex != -1) {
            speedCycle[(currentIndex + 1) % speedCycle.size]
        } else {
            1.0f
        }
        setPlaybackSpeed(nextSpeed)
    }

    fun selectArtist(artist: Artist?) {
        _selectedArtist.value = artist
    }

    fun playArtistTracks(tracks: List<Track>, shuffle: Boolean = false) {
        if (tracks.isEmpty()) return
        val playList = if (shuffle) tracks.shuffled() else tracks
        playTrack(playList.first(), playList)
    }

    fun removeFromQueue(index: Int) {
        _playerUiState.update { state ->
            if (index in state.playlist.indices) {
                val updatedList = state.playlist.toMutableList().apply { removeAt(index) }
                persistCurrentQueue(updatedList)
                state.copy(playlist = updatedList, queueCount = updatedList.size)
            } else state
        }
    }

    fun moveQueueItem(fromIndex: Int, toIndex: Int) {
        _playerUiState.update { state ->
            if (fromIndex in state.playlist.indices && toIndex in state.playlist.indices) {
                val updatedList = state.playlist.toMutableList()
                val item = updatedList.removeAt(fromIndex)
                updatedList.add(toIndex, item)
                persistCurrentQueue(updatedList)
                state.copy(playlist = updatedList)
            } else state
        }
    }

    fun shuffleQueue() {
        _playerUiState.update { state ->
            val current = state.currentTrack
            val others = state.playlist.filter { it.id != current.id }.shuffled()
            val newQueue = listOf(current) + others
            persistCurrentQueue(newQueue)
            state.copy(playlist = newQueue, currentTrackIndex = 0)
        }
    }

    fun selectDevice(device: ConnectedAudioDevice) {
        audioOutputDetector.routeAudioToDevice(device, exoPlayer)
        _playerUiState.update { it.copy(activeDevice = audioOutputDetector.currentDevice.value) }
    }

    fun selectDevice(deviceName: String, deviceType: AudioDeviceType = AudioDeviceType.OTHER) {
        val target = availableDevices.value.firstOrNull { it.name == deviceName && (deviceType == AudioDeviceType.OTHER || it.type == deviceType) }
            ?: ConnectedAudioDevice(
                type = deviceType,
                name = deviceName,
                description = when (deviceType) {
                    AudioDeviceType.SPEAKER -> "Internal Device Speakers"
                    AudioDeviceType.BLUETOOTH -> "Bluetooth High-Definition Stream"
                    AudioDeviceType.WIRED_HEADPHONES -> "3.5mm Direct Analog Output"
                    AudioDeviceType.USB_DAC -> "External Digital Audio Processor"
                    else -> "Active Hardware Destination"
                }
            )
        selectDevice(target)
    }

    fun resetAudioRoutingToDefault() {
        audioOutputDetector.resetToSystemDefault(exoPlayer)
        _playerUiState.update { it.copy(activeDevice = audioOutputDetector.currentDevice.value) }
    }

    // --- Chromecast & Wireless Audio Control Methods ---
    fun startCastDiscovery() {
        chromecastManager.startDiscovery()
    }

    fun stopCastDiscovery() {
        chromecastManager.stopDiscovery()
    }

    fun connectToCastDevice(device: CastDevice) {
        val current = _playerUiState.value.currentTrack
        chromecastManager.connectToDevice(device, current)
        _playerUiState.update {
            it.copy(
                activeDevice = ConnectedAudioDevice(
                    type = AudioDeviceType.CHROMECAST,
                    name = device.name,
                    description = "${device.modelName} • ${device.ipAddress}"
                )
            )
        }
    }

    fun disconnectCast() {
        chromecastManager.disconnect()
        resetAudioRoutingToDefault()
    }

    fun setCastVolume(volume: Float) {
        chromecastManager.setVolume(volume)
    }

    fun setCastQuality(quality: CastStreamQuality) {
        chromecastManager.setStreamQuality(quality)
    }

    fun openSystemAudioSettings(context: android.content.Context): Boolean {
        return audioOutputDetector.openSystemAudioSettings(context)
    }

    fun setNowPlayingExpanded(expanded: Boolean) {
        _playerUiState.update { it.copy(isNowPlayingExpanded = expanded) }
    }

    fun toggleLyricsScreen() {
        val next = !_playerUiState.value.isLyricsVisible
        _playerUiState.update { it.copy(isLyricsVisible = next) }
        if (next) {
            val currentTrack = _playerUiState.value.currentTrack
            if (currentTrack.id.isNotBlank()) {
                val lyricsTrackId = (_lyricsUiState.value as? LyricsUiState.Success)?.track?.id
                if (lyricsTrackId != currentTrack.id && _lyricsUiState.value !is LyricsUiState.Loading) {
                    loadLyricsForTrack(currentTrack)
                }
            }
        }
    }

    fun setLyricsVisible(visible: Boolean) {
        _playerUiState.update { it.copy(isLyricsVisible = visible) }
        if (visible) {
            val currentTrack = _playerUiState.value.currentTrack
            if (currentTrack.id.isNotBlank()) {
                val lyricsTrackId = (_lyricsUiState.value as? LyricsUiState.Success)?.track?.id
                if (lyricsTrackId != currentTrack.id && _lyricsUiState.value !is LyricsUiState.Loading) {
                    loadLyricsForTrack(currentTrack)
                }
            }
        }
    }

    // ================= Sleep Timer =================

    fun startSleepTimer(minutes: Int) {
        if (minutes <= 0) {
            cancelSleepTimer()
            return
        }
        val totalSecs = (minutes * 60).toLong()
        sleepTimerJob?.cancel()
        _sleepTimerState.value = SleepTimerState(
            isActive = true,
            remainingSeconds = totalSecs,
            totalSeconds = totalSecs,
            isEndOfTrack = false
        )

        sleepTimerJob = viewModelScope.launch {
            var remaining = totalSecs
            while (isActive && remaining > 0) {
                delay(1000)
                remaining -= 1
                _sleepTimerState.update { it.copy(remainingSeconds = remaining) }
            }
            if (remaining <= 0) {
                pause()
                _sleepTimerState.value = SleepTimerState(isActive = false)
            }
        }
    }

    fun startSleepTimerEndOfTrack() {
        val currentTrack = _playerUiState.value.currentTrack
        val currentPos = _playerUiState.value.currentPositionMs
        val remainingMs = (currentTrack.durationMs - currentPos).coerceAtLeast(1000L)
        val remainingSecs = (remainingMs / 1000).coerceAtLeast(1L)

        sleepTimerJob?.cancel()
        _sleepTimerState.value = SleepTimerState(
            isActive = true,
            remainingSeconds = remainingSecs,
            totalSeconds = remainingSecs,
            isEndOfTrack = true
        )

        val targetTrackId = currentTrack.id

        sleepTimerJob = viewModelScope.launch {
            while (isActive) {
                delay(1000)
                val current = _playerUiState.value
                val nowPos = current.currentPositionMs
                val nowDur = current.currentTrack.durationMs
                val nowTrackId = current.currentTrack.id

                if (nowTrackId != targetTrackId || nowPos >= (nowDur - 1000L)) {
                    pause()
                    _sleepTimerState.value = SleepTimerState(isActive = false)
                    break
                } else {
                    val rem = ((nowDur - nowPos) / 1000).coerceAtLeast(0L)
                    _sleepTimerState.update { it.copy(remainingSeconds = rem) }
                }
            }
        }
    }

    fun addSleepTimerMinutes(minutes: Int) {
        val current = _sleepTimerState.value
        if (current.isActive) {
            val additionalSecs = (minutes * 60).toLong()
            val newRemaining = current.remainingSeconds + additionalSecs
            val newTotal = current.totalSeconds + additionalSecs
            _sleepTimerState.update {
                it.copy(remainingSeconds = newRemaining, totalSeconds = newTotal, isEndOfTrack = false)
            }
            // Restart coroutine ticker with new remaining
            sleepTimerJob?.cancel()
            sleepTimerJob = viewModelScope.launch {
                var remaining = newRemaining
                while (isActive && remaining > 0) {
                    delay(1000)
                    remaining -= 1
                    _sleepTimerState.update { it.copy(remainingSeconds = remaining) }
                }
                if (remaining <= 0) {
                    pause()
                    _sleepTimerState.value = SleepTimerState(isActive = false)
                }
            }
        } else {
            startSleepTimer(minutes)
        }
    }

    fun cancelSleepTimer() {
        sleepTimerJob?.cancel()
        sleepTimerJob = null
        _sleepTimerState.value = SleepTimerState(isActive = false)
    }

    // Equalizer Passthroughs
    fun toggleEqualizer(enabled: Boolean) = equalizerManager.setEqualizerEnabled(enabled)
    fun setEqBandGain(bandIndex: Short, gainMb: Short) = equalizerManager.setBandGain(bandIndex, gainMb)
    fun setEqPreset(presetIndex: Short) = equalizerManager.usePreset(presetIndex)
    fun setEqBassBoost(strength: Short) = equalizerManager.setBassBoost(strength)
    fun setEqVirtualizer(strength: Short) = equalizerManager.setVirtualizer(strength)
    fun setEqReverb(preset: Short) = equalizerManager.setReverbPreset(preset)
    fun setEqLoudness(gainMb: Int) = equalizerManager.setLoudnessGain(gainMb)
    fun openSystemEqualizer() = equalizerManager.openSystemEqualizer()

    // Settings Passthroughs
    fun setThemeMode(mode: com.example.model.AppThemeMode) {
        val isDark = mode == com.example.model.AppThemeMode.DARK || mode == com.example.model.AppThemeMode.PURE_BLACK
        val isAmoled = mode == com.example.model.AppThemeMode.PURE_BLACK
        _settingsState.update { it.copy(themeMode = mode, darkTheme = isDark, amoledBlack = isAmoled) }
        viewModelScope.launch { settingsRepository.setThemeMode(mode) }
    }

    fun setUseDynamicColors(enabled: Boolean) {
        _settingsState.update { it.copy(useDynamicColors = enabled, dynamicColor = enabled) }
        viewModelScope.launch { settingsRepository.setUseDynamicColors(enabled) }
    }

    fun setPlayerLayoutType(layoutType: com.example.model.PlayerLayoutType) {
        _settingsState.update { it.copy(playerLayoutType = layoutType) }
        viewModelScope.launch { settingsRepository.setPlayerLayoutType(layoutType) }
    }

    fun updateM3ExpressiveConfig(config: com.example.model.M3ExpressivePlayerConfig) {
        _settingsState.update { it.copy(m3ExpressiveConfig = config) }
        viewModelScope.launch { settingsRepository.updateM3ExpressiveConfig(config) }
    }

    fun updateLyricsConfig(config: com.example.model.LyricsScreenConfig) {
        _settingsState.update { it.copy(lyricsConfig = config) }
        viewModelScope.launch { settingsRepository.updateLyricsConfig(config) }
    }

    fun setDarkTheme(enabled: Boolean) {
        _settingsState.update { it.copy(darkTheme = enabled) }
        viewModelScope.launch { settingsRepository.setDarkTheme(enabled) }
    }

    fun setAmoledBlack(enabled: Boolean) {
        _settingsState.update { it.copy(amoledBlack = enabled) }
        viewModelScope.launch { settingsRepository.setAmoledBlack(enabled) }
    }

    fun setDynamicColor(enabled: Boolean) {
        _settingsState.update { it.copy(dynamicColor = enabled, useDynamicColors = enabled) }
        viewModelScope.launch { settingsRepository.setDynamicColor(enabled) }
    }

    fun setBlurIntensity(intensity: Float) {
        _settingsState.update { it.copy(blurIntensity = intensity) }
        viewModelScope.launch { settingsRepository.setBlurIntensity(intensity) }
    }

    fun setForceHighRefreshRate(enabled: Boolean) {
        _settingsState.update { it.copy(forceHighRefreshRate = enabled) }
        viewModelScope.launch { settingsRepository.setForceHighRefreshRate(enabled) }
    }

    fun setMinDurationSec(sec: Int) {
        _settingsState.update { it.copy(minDurationSec = sec) }
        viewModelScope.launch { settingsRepository.setMinDurationSec(sec) }
        scanLocalStorage()
    }

    fun addBlacklistFolder(name: String) {
        val trimmed = name.trim()
        if (trimmed.isBlank()) return
        _settingsState.update { it.copy(blacklistedFolders = it.blacklistedFolders + trimmed) }
        viewModelScope.launch { settingsRepository.addBlacklistedFolder(trimmed) }
        scanLocalStorage()
    }

    fun removeBlacklistFolder(name: String) {
        _settingsState.update { it.copy(blacklistedFolders = it.blacklistedFolders - name) }
        viewModelScope.launch { settingsRepository.removeBlacklistedFolder(name) }
        scanLocalStorage()
    }

    fun addWhitelistFolder(path: String) {
        val trimmed = path.trim()
        if (trimmed.isBlank()) return
        _settingsState.update { it.copy(whitelistedFolders = it.whitelistedFolders + trimmed, useWhitelistOnly = true) }
        viewModelScope.launch {
            settingsRepository.addWhitelistedFolder(trimmed)
            settingsRepository.setUseWhitelistOnly(true)
        }
        scanLocalStorage()
    }

    fun removeWhitelistFolder(path: String) {
        val remaining = _settingsState.value.whitelistedFolders - path
        _settingsState.update { it.copy(whitelistedFolders = remaining, useWhitelistOnly = remaining.isNotEmpty()) }
        viewModelScope.launch {
            settingsRepository.removeWhitelistedFolder(path)
            if (remaining.isEmpty()) {
                settingsRepository.setUseWhitelistOnly(false)
            }
        }
        scanLocalStorage()
    }

    fun setUseWhitelistOnly(enabled: Boolean) {
        _settingsState.update { it.copy(useWhitelistOnly = enabled) }
        viewModelScope.launch { settingsRepository.setUseWhitelistOnly(enabled) }
        scanLocalStorage()
    }

    fun setUseSystemEqualizerPref(enabled: Boolean) {
        _settingsState.update { it.copy(useSystemEqualizer = enabled) }
        viewModelScope.launch { settingsRepository.setUseSystemEqualizer(enabled) }
    }

    fun setShowAudioDetails(enabled: Boolean) {
        _settingsState.update { it.copy(showAudioDetails = enabled) }
        viewModelScope.launch { settingsRepository.setShowAudioDetails(enabled) }
    }

    fun setLockScreenPlayer(enabled: Boolean) {
        _settingsState.update { it.copy(lockScreenPlayer = enabled) }
        viewModelScope.launch { settingsRepository.setLockScreenPlayer(enabled) }
    }

    fun setMusicDynamicTheming(enabled: Boolean) {
        _settingsState.update { it.copy(musicDynamicTheming = enabled) }
        viewModelScope.launch { settingsRepository.setMusicDynamicTheming(enabled) }
    }

    fun setChangeFontsWithMusic(enabled: Boolean) {
        _settingsState.update { it.copy(changeFontsWithMusic = enabled) }
        viewModelScope.launch { settingsRepository.setChangeFontsWithMusic(enabled) }
    }

    fun setDefaultFontFamily(fontFamily: String) {
        _settingsState.update { it.copy(defaultFontFamily = fontFamily) }
        viewModelScope.launch { settingsRepository.setDefaultFontFamily(fontFamily) }
    }

    fun setBackdropBlurEnabled(enabled: Boolean) {
        _settingsState.update { it.copy(backdropBlurEnabled = enabled) }
        viewModelScope.launch { settingsRepository.setBackdropBlurEnabled(enabled) }
    }

    fun setGlassmorphicEffectEnabled(enabled: Boolean) {
        _settingsState.update { it.copy(glassmorphicEffectEnabled = enabled) }
        viewModelScope.launch { settingsRepository.setGlassmorphicEffectEnabled(enabled) }
    }

    fun setSnowballEffectEnabled(enabled: Boolean) {
        _settingsState.update { it.copy(snowballEffectEnabled = enabled) }
        viewModelScope.launch { settingsRepository.setSnowballEffectEnabled(enabled) }
    }

    fun setLiveMovingGradientEnabled(enabled: Boolean) {
        _settingsState.update { it.copy(liveMovingGradientEnabled = enabled) }
        viewModelScope.launch { settingsRepository.setLiveMovingGradientEnabled(enabled) }
    }

    fun setHomeScreenEffectsEnabled(enabled: Boolean) {
        _settingsState.update { it.copy(homeScreenEffectsEnabled = enabled) }
        viewModelScope.launch { settingsRepository.setHomeScreenEffectsEnabled(enabled) }
    }

    fun setHomeScreenEffectStyle(styleName: String) {
        _settingsState.update { it.copy(homeScreenEffectStyle = styleName) }
        viewModelScope.launch { settingsRepository.setHomeScreenEffectStyle(styleName) }
    }

    fun setPlaybackVisualizerEnabled(enabled: Boolean) {
        _settingsState.update { it.copy(playbackVisualizerEnabled = enabled) }
        viewModelScope.launch { settingsRepository.setPlaybackVisualizerEnabled(enabled) }
    }

    fun setWaveformVisualizerStyle(styleName: String) {
        _settingsState.update { it.copy(waveformVisualizerStyle = styleName) }
        viewModelScope.launch { settingsRepository.setWaveformVisualizerStyle(styleName) }
    }

    fun setGaplessPlayback(enabled: Boolean) {
        _settingsState.update { it.copy(gaplessPlayback = enabled) }
        viewModelScope.launch { settingsRepository.setGaplessPlayback(enabled) }
    }

    fun setCrossfadeEnabled(enabled: Boolean) {
        _settingsState.update { it.copy(crossfadeEnabled = enabled) }
        if (!enabled) {
            fadeJob?.cancel()
            exoPlayer.volume = 1.0f
        }
        viewModelScope.launch { settingsRepository.setCrossfadeEnabled(enabled) }
    }

    fun setCrossfadeDurationSec(seconds: Int) {
        _settingsState.update { it.copy(crossfadeDurationSec = seconds) }
        viewModelScope.launch { settingsRepository.setCrossfadeDurationSec(seconds) }
    }

    fun setAlwaysShuffle(enabled: Boolean) {
        _settingsState.update { it.copy(alwaysShuffle = enabled) }
        viewModelScope.launch { settingsRepository.setAlwaysShuffle(enabled) }
        if (enabled) {
            _playerUiState.update { current ->
                val currentTrack = current.currentTrack
                val currentPlaylist = current.playlist
                val updatedPlaylist = if (currentPlaylist.isNotEmpty()) {
                    if (currentTrack.id.isNotBlank()) {
                        listOf(currentTrack) + (currentPlaylist - currentTrack).shuffled()
                    } else {
                        currentPlaylist.shuffled()
                    }
                } else {
                    currentPlaylist
                }
                val newIndex = updatedPlaylist.indexOfFirst { it.id == currentTrack.id }.coerceAtLeast(0)
                current.copy(
                    isShuffle = true,
                    playlist = updatedPlaylist,
                    currentTrackIndex = newIndex
                )
            }
        }
    }

    fun setColorPalette(palette: com.example.model.AppColorPalette) {
        _settingsState.update { it.copy(colorPalette = palette) }
        viewModelScope.launch { settingsRepository.setColorPalette(palette) }
    }

    fun setAodEnabled(enabled: Boolean) {
        _settingsState.update { it.copy(aodEnabled = enabled) }
        viewModelScope.launch { settingsRepository.setAodEnabled(enabled) }
    }

    fun setAodLayoutType(layoutType: com.example.model.AodLayoutType) {
        _settingsState.update { it.copy(aodLayoutType = layoutType) }
        viewModelScope.launch { settingsRepository.setAodLayoutType(layoutType) }
    }

    fun setAodKeepScreenAwake(awake: Boolean) {
        _settingsState.update { it.copy(aodKeepScreenAwake = awake) }
        viewModelScope.launch { settingsRepository.setAodKeepScreenAwake(awake) }
    }

    fun setAodBurnInProtection(protection: Boolean) {
        _settingsState.update { it.copy(aodBurnInProtection = protection) }
        viewModelScope.launch { settingsRepository.setAodBurnInProtection(protection) }
    }

    fun setAodBrightnessDim(dim: Float) {
        _settingsState.update { it.copy(aodBrightnessDim = dim) }
        viewModelScope.launch { settingsRepository.setAodBrightnessDim(dim) }
    }

    fun cacheAllImages() {
        if (_isCachingImages.value) return
        viewModelScope.launch {
            _isCachingImages.value = true
            _cachingProgress.value = "Starting image caching..."
            try {
                val tracks = _homeUiState.value.tracks
                val totalSuccess = com.example.data.ArtworkResolver.prefetchAndCacheImages(
                    context = getApplication(),
                    tracks = tracks
                ) { current, total ->
                    _cachingProgress.value = "Caching artwork $current / $total"
                }
                _cachingProgress.value = "Successfully cached $totalSuccess images!"
            } catch (e: Exception) {
                _cachingProgress.value = "Caching complete."
            } finally {
                kotlinx.coroutines.delay(2500)
                _isCachingImages.value = false
                _cachingProgress.value = null
            }
        }
    }

    fun setAudioRoutingMode(mode: AudioRoutingMode) {
        audioEngineManager.setRoutingMode(mode)
    }

    fun setNotificationPattern(pattern: com.example.model.NotificationPattern) {
        _settingsState.update { it.copy(notificationPattern = pattern) }
        viewModelScope.launch { settingsRepository.setNotificationPattern(pattern) }
        updatePlaybackServiceNotification()
    }

    fun setSilentNotification(silent: Boolean) {
        _settingsState.update { it.copy(silentNotification = silent) }
        viewModelScope.launch { settingsRepository.setSilentNotification(silent) }
        updatePlaybackServiceNotification()
    }

    fun setLibrarySort(method: String, ascending: Boolean) {
        _settingsState.update { it.copy(librarySortMethod = method, librarySortAscending = ascending) }
        viewModelScope.launch { settingsRepository.setLibrarySort(method, ascending) }
    }

    fun setLibraryTab(tab: String) {
        _settingsState.update { it.copy(libraryTab = tab) }
        viewModelScope.launch { settingsRepository.setLibraryTab(tab) }
    }

    fun resetEqualizerToFlat() {
        val bands = equalizerUiState.value.bands
        bands.forEach { band ->
            equalizerManager.setBandGain(band.bandIndex, 0.toShort())
        }
        equalizerManager.setBassBoost(0.toShort())
        equalizerManager.setVirtualizer(0.toShort())
        equalizerManager.setReverbPreset(0.toShort())
        equalizerManager.setLoudnessGain(0)
    }

    private fun onTrackPlaybackCompleted(trackId: String, durationMs: Long = 0L) {
        if (trackId.isNotBlank()) {
            viewModelScope.launch {
                musicTrackerRepository.recordTrackCompletion(trackId, durationMs)
            }
        }
    }

    fun clearTrackerStats() {
        viewModelScope.launch {
            musicTrackerRepository.clearStats()
        }
    }

    fun playMostListened(shuffle: Boolean = false) {
        val mostPlayedList = trackerSummary.value.mostListened.map { it.track }
        val targetList = if (mostPlayedList.isNotEmpty()) mostPlayedList else _homeUiState.value.tracks
        if (targetList.isEmpty()) return
        val listToPlay = if (shuffle) targetList.shuffled() else targetList
        val firstTrack = listToPlay.first()
        playTrack(firstTrack)
        _playerUiState.update { it.copy(playlist = listToPlay, queueCount = listToPlay.size) }
    }

    // --- Backup & Restore Methods ---
    fun loadLocalSnapshotInfo() {
        viewModelScope.launch {
            _localSnapshotInfo.value = backupRepository.getLocalSnapshotSummary()
        }
    }

    fun exportBackupToUri(uri: Uri, onResult: (BackupResult) -> Unit = {}) {
        viewModelScope.launch {
            val result = backupRepository.exportBackupToUri(uri)
            if (result is BackupResult.Success) {
                _backupStatus.value = "Backup created: ${result.summary.statsCount} stats, ${result.summary.favoritesCount} favorites, ${result.summary.lyricsCount} lyrics"
                loadLocalSnapshotInfo()
            } else if (result is BackupResult.Error) {
                _backupStatus.value = "Export error: ${result.message}"
            }
            onResult(result)
        }
    }

    fun restoreBackupFromUri(uri: Uri, mergeMode: Boolean = true, onResult: (BackupResult) -> Unit = {}) {
        viewModelScope.launch {
            val result = backupRepository.restoreBackupFromUri(uri, mergeMode)
            if (result is BackupResult.Success) {
                _backupStatus.value = "Restored: ${result.summary.statsCount} stats, ${result.summary.favoritesCount} favorites, ${result.summary.lyricsCount} lyrics"
                loadLocalSnapshotInfo()
                // Sync favorites state
                try {
                    val favIds = musicDatabase.favoriteTrackDao().getAllFavoriteIds().toSet()
                    _favoriteTrackIds.value = favIds
                    sessionManager.setFavorites(favIds)
                    val currentTrackId = _playerUiState.value.currentTrack.id
                    if (currentTrackId.isNotBlank()) {
                        val isFav = favIds.contains(currentTrackId)
                        _playerUiState.update { it.copy(isFavorite = isFav) }
                        _lyricsUiState.update { lState ->
                            if (lState is LyricsUiState.Success) lState.copy(isFavorite = isFav) else lState
                        }
                    }
                } catch (e: Exception) {
                    Log.w("PlayerViewModel", "Error syncing favorites after restore: ${e.message}")
                }
                // Reload settings and UI
                _settingsState.value = settingsRepository.getSettingsFastSync()
            } else if (result is BackupResult.Error) {
                _backupStatus.value = "Restore error: ${result.message}"
            }
            onResult(result)
        }
    }

    fun createLocalSnapshot(onResult: (BackupResult) -> Unit = {}) {
        viewModelScope.launch {
            val result = backupRepository.saveLocalSnapshot()
            if (result is BackupResult.Success) {
                _backupStatus.value = "Local snapshot saved: ${result.summary.statsCount} stats, ${result.summary.favoritesCount} favorites, ${result.summary.lyricsCount} lyrics"
                loadLocalSnapshotInfo()
            } else if (result is BackupResult.Error) {
                _backupStatus.value = "Snapshot failed: ${result.message}"
            }
            onResult(result)
        }
    }

    fun restoreLocalSnapshot(mergeMode: Boolean = true, onResult: (BackupResult) -> Unit = {}) {
        viewModelScope.launch {
            val result = backupRepository.restoreLocalSnapshot(mergeMode)
            if (result is BackupResult.Success) {
                _backupStatus.value = "Restored from local snapshot successfully!"
                loadLocalSnapshotInfo()
                // Sync favorites state
                try {
                    val favIds = musicDatabase.favoriteTrackDao().getAllFavoriteIds().toSet()
                    _favoriteTrackIds.value = favIds
                    sessionManager.setFavorites(favIds)
                    val currentTrackId = _playerUiState.value.currentTrack.id
                    if (currentTrackId.isNotBlank()) {
                        val isFav = favIds.contains(currentTrackId)
                        _playerUiState.update { it.copy(isFavorite = isFav) }
                        _lyricsUiState.update { lState ->
                            if (lState is LyricsUiState.Success) lState.copy(isFavorite = isFav) else lState
                        }
                    }
                } catch (e: Exception) {
                    Log.w("PlayerViewModel", "Error syncing favorites after local restore: ${e.message}")
                }
                _settingsState.value = settingsRepository.getSettingsFastSync()
            } else if (result is BackupResult.Error) {
                _backupStatus.value = "Local restore error: ${result.message}"
            }
            onResult(result)
        }
    }

    fun clearBackupStatus() {
        _backupStatus.value = null
    }

    override fun onCleared() {
        super.onCleared()
        sleepTimerJob?.cancel()
        progressTickerJob?.cancel()
        lyricsFetchJob?.cancel()
        systemVolumeController.release()
        audioOutputDetector.stopListening()
        // Do not release exoPlayer here as PlaybackMediaSessionService manages its lifecycle
    }
}

private fun computeTrackerSummary(
    tracks: List<Track>,
    statsMap: Map<String, TrackStatsEntity>
): MusicTrackerSummary {
    val trackMap = tracks.associateBy { it.id }
    val items = statsMap.mapNotNull { (trackId, entity) ->
        val track = trackMap[trackId] ?: return@mapNotNull null
        val durationMs = if (track.durationMs > 15_000L) track.durationMs else 210_000L
        val playsFromTime = (entity.totalListenTimeMs / durationMs).toInt()
        val effectivePlayCount = maxOf(entity.playCount, entity.completedPlays, playsFromTime)
        TrackStatsItem(
            track = track,
            playCount = effectivePlayCount,
            completedPlays = entity.completedPlays,
            totalListenTimeMs = entity.totalListenTimeMs,
            lastPlayedTimestamp = entity.lastPlayedTimestamp
        )
    }

    val mostListened = items.filter { it.playCount > 0 }.sortedByDescending { it.playCount }
    val recentlyPlayed = items.filter { it.lastPlayedTimestamp > 0L }.sortedByDescending { it.lastPlayedTimestamp }

    val totalPlays = items.sumOf { it.playCount }
    val totalTime = items.sumOf { it.totalListenTimeMs }
    val uniqueTracks = items.count { it.playCount > 0 }
    val topTrack = mostListened.firstOrNull()

    val artistPlayMap = mutableMapOf<String, Int>()
    items.forEach { item ->
        val artist = item.track.artist.takeIf { it.isNotBlank() && it != "Unknown Artist" } ?: return@forEach
        artistPlayMap[artist] = (artistPlayMap[artist] ?: 0) + item.playCount
    }
    val topArtist = artistPlayMap.maxByOrNull { it.value }?.key

    return MusicTrackerSummary(
        totalPlays = totalPlays,
        totalListenTimeMs = totalTime,
        uniqueTracksCount = uniqueTracks,
        topTrack = topTrack,
        topArtist = topArtist,
        mostListened = mostListened,
        recentlyPlayed = recentlyPlayed
    )
}
