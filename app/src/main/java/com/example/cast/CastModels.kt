package com.example.cast

import androidx.compose.runtime.Immutable
import com.example.model.AudioDeviceType

enum class CastDeviceType {
    NEST_SPEAKER,
    CHROMECAST,
    ANDROID_TV,
    SMART_DISPLAY,
    CAST_GROUP,
    GENERIC_WIRELESS;

    fun toAudioDeviceType(): AudioDeviceType = AudioDeviceType.CHROMECAST
}

enum class CastState {
    DISCONNECTED,
    DISCOVERING,
    CONNECTING,
    CONNECTED,
    STREAMING,
    ERROR
}

enum class CastStreamQuality(val label: String, val description: String) {
    LOSSLESS_ORIGINAL("Lossless (Original)", "Bit-perfect FLAC/WAV streaming"),
    HIGH_320KBPS("High Quality (320kbps)", "Studio-grade stereo audio"),
    BALANCED_192KBPS("Balanced (192kbps)", "Optimized for maximum Wi-Fi stability")
}

@Immutable
data class CastDevice(
    val id: String,
    val name: String,
    val modelName: String,
    val type: CastDeviceType,
    val ipAddress: String,
    val port: Int = 8009,
    val isConnected: Boolean = false,
    val volume: Float = 0.70f,
    val signalStrength: Int = 95
)

@Immutable
data class ChromecastUiState(
    val state: CastState = CastState.DISCONNECTED,
    val activeDevice: CastDevice? = null,
    val availableDevices: List<CastDevice> = emptyList(),
    val streamQuality: CastStreamQuality = CastStreamQuality.LOSSLESS_ORIGINAL,
    val volume: Float = 0.70f,
    val isMuted: Boolean = false,
    val serverIp: String = "127.0.0.1",
    val serverPort: Int = 8989,
    val isServerRunning: Boolean = false,
    val activeStreamUrl: String? = null,
    val errorMessage: String? = null
)
