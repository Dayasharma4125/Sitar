package com.example.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.ForwardingPlayer
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import android.view.KeyEvent
import androidx.media3.session.CommandButton
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import androidx.media3.session.SessionCommand
import androidx.media3.session.SessionResult
import com.example.MainActivity
import com.example.data.SessionManager
import com.example.data.SettingsRepository
import com.example.model.NotificationPattern
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

/**
 * Single Media3 MediaSessionService managing ExoPlayer and system media session.
 * Powers:
 * - ColorOS / Android 13+ Notification controls and Quick Settings scrubber
 * - Lock screen and Always-On Display controls
 * - Bluetooth and headset playback controls
 */
class PlaybackMediaSessionService : MediaSessionService() {

    private lateinit var player: ExoPlayer
    private lateinit var mediaSession: MediaSession
    private val serviceScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    companion object {
        const val ACTION_REWIND_10 = "com.example.ACTION_REWIND_10"
        const val ACTION_FORWARD_10 = "com.example.ACTION_FORWARD_10"
        const val ACTION_TOGGLE_FAVORITE = "com.example.ACTION_TOGGLE_FAVORITE"
        const val ACTION_SKIP_NEXT = "com.example.ACTION_SKIP_NEXT"
        const val ACTION_SKIP_PREVIOUS = "com.example.ACTION_SKIP_PREVIOUS"

        @Volatile
        var onSkipNextListener: (() -> Unit)? = null

        @Volatile
        var onSkipPreviousListener: (() -> Unit)? = null

        @Volatile
        private var sharedPlayerInstance: ExoPlayer? = null

        @Synchronized
        fun getSharedPlayer(context: Context): ExoPlayer {
            return sharedPlayerInstance ?: try {
                com.example.audio.AudioEngineManager(context.applicationContext).buildAudiophileExoPlayer()
            } catch (e: Throwable) {
                Log.w("PlaybackMediaSessionService", "AudioEngineManager fallback: ${e.message}")
                ExoPlayer.Builder(context.applicationContext)
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
                            .setUsage(C.USAGE_MEDIA)
                            .build(),
                        true
                    )
                    .setHandleAudioBecomingNoisy(true)
                    .build()
            }.also {
                sharedPlayerInstance = it
                activeExoPlayer = it
            }
        }

        @Volatile
        var activeExoPlayer: ExoPlayer? = null
            get() = sharedPlayerInstance
            internal set

        val activeAudioSessionId: Int
            get() = sharedPlayerInstance?.audioSessionId ?: 0

        fun start(context: Context) {
            try {
                val intent = Intent(context, PlaybackMediaSessionService::class.java)
                context.startService(intent)
            } catch (e: Exception) {
                Log.w("PlaybackMediaSessionService", "Error starting service: ${e.message}")
            }
        }

        fun stop(context: Context) {
            try {
                val intent = Intent(context, PlaybackMediaSessionService::class.java)
                context.stopService(intent)
            } catch (_: Exception) {}
        }

        /**
         * Creates a MediaItem containing the metadata that Android,
         * ColorOS, lock screen, Bluetooth devices, etc. can consume.
         */
        fun createMediaItem(
            mediaId: String,
            uri: String,
            title: String,
            artist: String?,
            album: String?,
            artworkUri: String?,
            coverResId: Int? = null,
            packageName: String? = null
        ): MediaItem {
            val metadataBuilder = MediaMetadata.Builder()
                .setTitle(title)
                .setArtist(artist ?: "")
                .setAlbumTitle(album ?: "")

            if (!artworkUri.isNullOrBlank()) {
                try {
                    metadataBuilder.setArtworkUri(Uri.parse(artworkUri))
                } catch (_: Exception) {}
            } else if (coverResId != null && packageName != null) {
                try {
                    metadataBuilder.setArtworkUri(
                        Uri.parse("android.resource://$packageName/$coverResId")
                    )
                } catch (_: Exception) {}
            }

            return MediaItem.Builder()
                .setMediaId(mediaId)
                .setUri(Uri.parse(uri))
                .setMediaMetadata(metadataBuilder.build())
                .build()
        }
    }

    override fun onCreate() {
        super.onCreate()

        // 1. Obtain the ONE ExoPlayer instance used across the app
        player = getSharedPlayer(this)

        // Wrap player in ForwardingPlayer ensuring Next/Previous commands are always available to the notification
        val forwardingPlayer = object : ForwardingPlayer(player) {
            override fun getAvailableCommands(): Player.Commands {
                return super.getAvailableCommands().buildUpon()
                    .add(Player.COMMAND_SEEK_TO_NEXT)
                    .add(Player.COMMAND_SEEK_TO_NEXT_MEDIA_ITEM)
                    .add(Player.COMMAND_SEEK_TO_PREVIOUS)
                    .add(Player.COMMAND_SEEK_TO_PREVIOUS_MEDIA_ITEM)
                    .build()
            }

            override fun isCommandAvailable(command: @Player.Command Int): Boolean {
                return when (command) {
                    Player.COMMAND_SEEK_TO_NEXT,
                    Player.COMMAND_SEEK_TO_NEXT_MEDIA_ITEM,
                    Player.COMMAND_SEEK_TO_PREVIOUS,
                    Player.COMMAND_SEEK_TO_PREVIOUS_MEDIA_ITEM -> true
                    else -> super.isCommandAvailable(command)
                }
            }

            override fun hasNextMediaItem(): Boolean = true
            override fun hasPreviousMediaItem(): Boolean = true

            override fun seekToNext() {
                handleSkipNext()
            }

            override fun seekToNextMediaItem() {
                handleSkipNext()
            }

            override fun seekToPrevious() {
                handleSkipPrevious()
            }

            override fun seekToPreviousMediaItem() {
                handleSkipPrevious()
            }
        }

        // 2. Intent used when the user taps the media notification
        val launchIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        val sessionActivity = PendingIntent.getActivity(
            this,
            0,
            launchIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        // 3. MediaSession Callback for handling notification & hardware media actions
        val callback = object : MediaSession.Callback {
            override fun onConnect(
                session: MediaSession,
                controller: MediaSession.ControllerInfo
            ): MediaSession.ConnectionResult {
                val connectionResult = super.onConnect(session, controller)
                val availablePlayerCommands = connectionResult.availablePlayerCommands.buildUpon()
                    .add(Player.COMMAND_SEEK_TO_NEXT)
                    .add(Player.COMMAND_SEEK_TO_NEXT_MEDIA_ITEM)
                    .add(Player.COMMAND_SEEK_TO_PREVIOUS)
                    .add(Player.COMMAND_SEEK_TO_PREVIOUS_MEDIA_ITEM)
                    .build()
                return MediaSession.ConnectionResult.accept(
                    connectionResult.availableSessionCommands,
                    availablePlayerCommands
                )
            }

            override fun onPlayerCommandRequest(
                session: MediaSession,
                controller: MediaSession.ControllerInfo,
                playerCommand: @Player.Command Int
            ): Int {
                if (playerCommand == Player.COMMAND_SEEK_TO_NEXT ||
                    playerCommand == Player.COMMAND_SEEK_TO_NEXT_MEDIA_ITEM
                ) {
                    handleSkipNext()
                    return SessionResult.RESULT_SUCCESS
                }
                if (playerCommand == Player.COMMAND_SEEK_TO_PREVIOUS ||
                    playerCommand == Player.COMMAND_SEEK_TO_PREVIOUS_MEDIA_ITEM
                ) {
                    handleSkipPrevious()
                    return SessionResult.RESULT_SUCCESS
                }
                return super.onPlayerCommandRequest(session, controller, playerCommand)
            }

            override fun onMediaButtonEvent(
                session: MediaSession,
                controllerInfo: MediaSession.ControllerInfo,
                intent: Intent
            ): Boolean {
                val keyEvent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    intent.getParcelableExtra(Intent.EXTRA_KEY_EVENT, KeyEvent::class.java)
                } else {
                    @Suppress("DEPRECATION")
                    intent.getParcelableExtra(Intent.EXTRA_KEY_EVENT)
                }
                if (keyEvent != null && keyEvent.action == KeyEvent.ACTION_DOWN) {
                    when (keyEvent.keyCode) {
                        KeyEvent.KEYCODE_MEDIA_NEXT, KeyEvent.KEYCODE_MEDIA_FAST_FORWARD -> {
                            handleSkipNext()
                            return true
                        }
                        KeyEvent.KEYCODE_MEDIA_PREVIOUS, KeyEvent.KEYCODE_MEDIA_REWIND -> {
                            handleSkipPrevious()
                            return true
                        }
                    }
                }
                return super.onMediaButtonEvent(session, controllerInfo, intent)
            }

            override fun onCustomCommand(
                session: MediaSession,
                controller: MediaSession.ControllerInfo,
                customCommand: SessionCommand,
                args: Bundle
            ): ListenableFuture<SessionResult> {
                when (customCommand.customAction) {
                    ACTION_SKIP_NEXT -> handleSkipNext()
                    ACTION_SKIP_PREVIOUS -> handleSkipPrevious()
                    ACTION_REWIND_10 -> {
                        val newPos = (player.currentPosition - 10_000L).coerceAtLeast(0L)
                        player.seekTo(newPos)
                    }
                    ACTION_FORWARD_10 -> {
                        val duration = if (player.duration > 0) player.duration else Long.MAX_VALUE
                        val newPos = (player.currentPosition + 10_000L).coerceAtMost(duration)
                        player.seekTo(newPos)
                    }
                    ACTION_TOGGLE_FAVORITE -> {
                        val currentMediaId = player.currentMediaItem?.mediaId ?: ""
                        if (currentMediaId.isNotBlank()) {
                            serviceScope.launch {
                                SessionManager(applicationContext).toggleFavorite(currentMediaId)
                            }
                        }
                    }
                }
                return Futures.immediateFuture(SessionResult(SessionResult.RESULT_SUCCESS))
            }
        }

        // 4. Create MediaSession wrapping forwardingPlayer
        mediaSession = MediaSession.Builder(this, forwardingPlayer)
            .setId("AudiophileMediaSession")
            .setSessionActivity(sessionActivity)
            .setCallback(callback)
            .build()

        observeNotificationSettings()
    }

    private fun observeNotificationSettings() {
        serviceScope.launch {
            SettingsRepository(applicationContext).settingsFlow.collect { settings ->
                updateMediaSessionLayout(settings.notificationPattern)
                updateNotificationChannel(
                    settings.silentNotification || settings.notificationPattern == NotificationPattern.SILENT_MEDIA_CONTROLS
                )
            }
        }
    }

    private fun updateMediaSessionLayout(pattern: NotificationPattern) {
        val customLayout = mutableListOf<CommandButton>()

        when (pattern) {
            NotificationPattern.DJ_SCRUBBER -> {
                val rewind10Btn = CommandButton.Builder()
                    .setDisplayName("Rewind 10s")
                    .setIconResId(android.R.drawable.ic_media_rew)
                    .setSessionCommand(SessionCommand(ACTION_REWIND_10, Bundle.EMPTY))
                    .build()

                val forward10Btn = CommandButton.Builder()
                    .setDisplayName("Forward 10s")
                    .setIconResId(android.R.drawable.ic_media_ff)
                    .setSessionCommand(SessionCommand(ACTION_FORWARD_10, Bundle.EMPTY))
                    .build()

                customLayout.add(rewind10Btn)
                customLayout.add(forward10Btn)
            }
            NotificationPattern.EXTENDED_ACTIONS -> {
                val favoriteBtn = CommandButton.Builder()
                    .setDisplayName("Favorite")
                    .setIconResId(android.R.drawable.btn_star)
                    .setSessionCommand(SessionCommand(ACTION_TOGGLE_FAVORITE, Bundle.EMPTY))
                    .build()

                customLayout.add(favoriteBtn)
            }
            else -> {}
        }

        try {
            mediaSession.setCustomLayout(customLayout)
        } catch (e: Exception) {
            Log.w("PlaybackMediaSessionService", "Error setting custom layout: ${e.message}")
        }
    }

    private fun updateNotificationChannel(isSilent: Boolean) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
            val channelId = "media_playback_channel"
            val channelName = "Media Playback Controls"
            val importance = if (isSilent) {
                NotificationManager.IMPORTANCE_LOW
            } else {
                NotificationManager.IMPORTANCE_DEFAULT
            }

            val channel = NotificationChannel(channelId, channelName, importance).apply {
                description = "Music notification controls"
                if (isSilent) {
                    setSound(null, null)
                    enableVibration(false)
                }
            }
            notificationManager?.createNotificationChannel(channel)
        }
    }

    private fun handleSkipNext() {
        val listener = onSkipNextListener
        if (listener != null) {
            android.os.Handler(android.os.Looper.getMainLooper()).post {
                listener.invoke()
            }
        } else {
            serviceScope.launch {
                try {
                    val mediaRepo = com.example.data.MediaRepository(applicationContext)
                    val allTracks = mediaRepo.getCachedTracksFast().ifEmpty { mediaRepo.builtInTracks }
                    if (allTracks.isNotEmpty()) {
                        val currentId = player.currentMediaItem?.mediaId
                        val currentIndex = allTracks.indexOfFirst { it.id == currentId }
                        val nextIndex = if (currentIndex != -1) (currentIndex + 1) % allTracks.size else 0
                        val nextTrack = allTracks[nextIndex]
                        val mediaItem = createMediaItem(
                            mediaId = nextTrack.id,
                            uri = nextTrack.contentUri ?: "",
                            title = nextTrack.title,
                            artist = nextTrack.artist,
                            album = nextTrack.album,
                            artworkUri = nextTrack.albumArtUri ?: nextTrack.coverUrl,
                            coverResId = nextTrack.coverResId,
                            packageName = packageName
                        )
                        player.setMediaItem(mediaItem)
                        player.prepare()
                        player.play()
                    }
                } catch (e: Exception) {
                    Log.w("PlaybackMediaSessionService", "Error in fallback skipNext: ${e.message}")
                }
            }
        }
    }

    private fun handleSkipPrevious() {
        val listener = onSkipPreviousListener
        if (listener != null) {
            android.os.Handler(android.os.Looper.getMainLooper()).post {
                listener.invoke()
            }
        } else {
            serviceScope.launch {
                try {
                    val mediaRepo = com.example.data.MediaRepository(applicationContext)
                    val allTracks = mediaRepo.getCachedTracksFast().ifEmpty { mediaRepo.builtInTracks }
                    if (allTracks.isNotEmpty()) {
                        val currentId = player.currentMediaItem?.mediaId
                        val currentIndex = allTracks.indexOfFirst { it.id == currentId }
                        val prevIndex = if (currentIndex > 0) currentIndex - 1 else allTracks.size - 1
                        val prevTrack = allTracks[prevIndex]
                        val mediaItem = createMediaItem(
                            mediaId = prevTrack.id,
                            uri = prevTrack.contentUri ?: "",
                            title = prevTrack.title,
                            artist = prevTrack.artist,
                            album = prevTrack.album,
                            artworkUri = prevTrack.albumArtUri ?: prevTrack.coverUrl,
                            coverResId = prevTrack.coverResId,
                            packageName = packageName
                        )
                        player.setMediaItem(mediaItem)
                        player.prepare()
                        player.play()
                    }
                } catch (e: Exception) {
                    Log.w("PlaybackMediaSessionService", "Error in fallback skipPrevious: ${e.message}")
                }
            }
        }
    }

    override fun onGetSession(
        controllerInfo: MediaSession.ControllerInfo
    ): MediaSession? {
        return if (::mediaSession.isInitialized) mediaSession else null
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        // Keep playing if music is currently playing.
        // If playback has stopped, the service can be removed.
        if (::player.isInitialized && !player.isPlaying) {
            stopSelf()
        }
        super.onTaskRemoved(rootIntent)
    }

    override fun onDestroy() {
        serviceScope.cancel()
        activeExoPlayer = null
        if (::mediaSession.isInitialized) {
            mediaSession.release()
        }
        if (::player.isInitialized) {
            player.release()
        }
        sharedPlayerInstance = null
        super.onDestroy()
    }
}
