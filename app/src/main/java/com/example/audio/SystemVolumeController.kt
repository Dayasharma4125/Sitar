package com.example.audio

import android.content.Context
import android.database.ContentObserver
import android.media.AudioManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.roundToInt

/**
 * Controller and observer for system STREAM_MUSIC volume.
 * Keeps in-app volume slider in 100% sync with Android hardware volume rocker keys.
 */
class SystemVolumeController(private val context: Context) {

    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

    private val maxVolume: Int = try {
        audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC).coerceAtLeast(1)
    } catch (e: Exception) {
        15
    }

    private val minVolume: Int = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        try {
            audioManager.getStreamMinVolume(AudioManager.STREAM_MUSIC)
        } catch (e: Exception) {
            0
        }
    } else {
        0
    }

    private val _volumeFraction = MutableStateFlow(getCurrentVolumeFraction())
    val volumeFraction: StateFlow<Float> = _volumeFraction.asStateFlow()

    private val contentObserver = object : ContentObserver(Handler(Looper.getMainLooper())) {
        override fun onChange(selfChange: Boolean) {
            super.onChange(selfChange)
            val current = getCurrentVolumeFraction()
            _volumeFraction.value = current
        }
    }

    init {
        try {
            context.contentResolver.registerContentObserver(
                Settings.System.CONTENT_URI,
                true,
                contentObserver
            )
        } catch (e: Exception) {
            // Ignore if security restriction applies
        }
    }

    fun getCurrentVolumeFraction(): Float {
        return try {
            val current = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
            val range = (maxVolume - minVolume).coerceAtLeast(1)
            ((current - minVolume).toFloat() / range.toFloat()).coerceIn(0f, 1f)
        } catch (e: Exception) {
            0.75f
        }
    }

    fun setSystemVolumeFraction(fraction: Float) {
        val clamped = fraction.coerceIn(0f, 1f)
        _volumeFraction.value = clamped
        try {
            val range = (maxVolume - minVolume).coerceAtLeast(1)
            val targetLevel = (minVolume + (clamped * range).roundToInt()).coerceIn(minVolume, maxVolume)
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, targetLevel, 0)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun release() {
        try {
            context.contentResolver.unregisterContentObserver(contentObserver)
        } catch (e: Exception) {
            // Ignore
        }
    }
}
