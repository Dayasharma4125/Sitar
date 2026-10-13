package com.example.audio

import android.content.Context
import android.content.Intent
import android.media.audiofx.AudioEffect
import android.media.audiofx.BassBoost
import android.media.audiofx.Equalizer
import android.media.audiofx.LoudnessEnhancer
import android.media.audiofx.PresetReverb
import android.media.audiofx.Virtualizer
import android.util.Log
import com.example.model.EqualizerBandState
import com.example.model.EqualizerUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class EqualizerManager(private val context: Context) {

    private var equalizer: Equalizer? = null
    private var bassBoost: BassBoost? = null
    private var virtualizer: Virtualizer? = null
    private var presetReverb: PresetReverb? = null
    private var loudnessEnhancer: LoudnessEnhancer? = null
    private var currentAudioSessionId: Int = 0
    private var isBassBoostSupported: Boolean = true
    private var isVirtualizerSupported: Boolean = true

    private val _uiState = MutableStateFlow(EqualizerUiState())
    val uiState: StateFlow<EqualizerUiState> = _uiState.asStateFlow()

    fun bindAudioSession(sessionId: Int) {
        if (sessionId <= 0 || sessionId == currentAudioSessionId) return
        currentAudioSessionId = sessionId
        releaseEffects()

        try {
            val availableEffects = try {
                AudioEffect.queryEffects() ?: emptyArray()
            } catch (e: Throwable) {
                emptyArray()
            }

            val hasEqualizer = availableEffects.any { it.type == AudioEffect.EFFECT_TYPE_EQUALIZER }
            val hasBassBoost = availableEffects.any { it.type == AudioEffect.EFFECT_TYPE_BASS_BOOST }
            val hasVirtualizer = availableEffects.any { it.type == AudioEffect.EFFECT_TYPE_VIRTUALIZER }
            val hasLoudness = availableEffects.any { it.type == AudioEffect.EFFECT_TYPE_LOUDNESS_ENHANCER }
            val reverbDesc = availableEffects.firstOrNull { it.type == AudioEffect.EFFECT_TYPE_PRESET_REVERB }

            // 1. Equalizer (Priority 0, audioSessionId)
            var eq: Equalizer? = null
            if (hasEqualizer) {
                try {
                    eq = Equalizer(0, sessionId)
                    equalizer = eq
                } catch (e: Throwable) {
                    Log.w("EqualizerManager", "Hardware Equalizer initialization failed: ${e.message}")
                }
            }

            // 2. Bass Boost (Lazy on-demand only when user strength > 0)
            if (hasBassBoost && isBassBoostSupported && _uiState.value.bassBoostStrength > 0) {
                try {
                    val bb = BassBoost(0, sessionId)
                    bb.enabled = _uiState.value.isEnabled
                    bb.setStrength(_uiState.value.bassBoostStrength.coerceIn(0, 1000))
                    bassBoost = bb
                } catch (e: Throwable) {
                    Log.w("EqualizerManager", "Hardware BassBoost not supported on this session: ${e.message}")
                    isBassBoostSupported = false
                    bassBoost = null
                }
            }

            // 3. Virtualizer (Lazy on-demand only when user strength > 0)
            if (hasVirtualizer && isVirtualizerSupported && _uiState.value.virtualizerStrength > 0) {
                try {
                    val vt = Virtualizer(0, sessionId)
                    vt.enabled = _uiState.value.isEnabled
                    vt.setStrength(_uiState.value.virtualizerStrength.coerceIn(0, 1000))
                    virtualizer = vt
                } catch (e: Throwable) {
                    Log.w("EqualizerManager", "Hardware Virtualizer not supported on this session: ${e.message}")
                    isVirtualizerSupported = false
                    virtualizer = null
                }
            }

            // 4. PresetReverb (Lazy on-demand only when user preset > 0)
            if (_uiState.value.reverbPreset > 0 && reverbDesc != null) {
                try {
                    val reverbSession = if (reverbDesc.connectMode.equals(AudioEffect.EFFECT_AUXILIARY, ignoreCase = true)) {
                        0
                    } else {
                        sessionId
                    }
                    val rv = PresetReverb(0, reverbSession)
                    rv.enabled = _uiState.value.isEnabled
                    rv.preset = _uiState.value.reverbPreset
                    presetReverb = rv
                } catch (e: Throwable) {
                    Log.w("EqualizerManager", "Hardware PresetReverb not supported: ${e.message}")
                    presetReverb = null
                }
            }

            // 5. Loudness Enhancer (Lazy on-demand only when loudnessGainMb > 0)
            if (hasLoudness && _uiState.value.loudnessGainMb > 0) {
                try {
                    val le = LoudnessEnhancer(sessionId)
                    le.enabled = _uiState.value.isEnabled
                    le.setTargetGain(_uiState.value.loudnessGainMb)
                    loudnessEnhancer = le
                } catch (e: Throwable) {
                    Log.w("EqualizerManager", "Hardware LoudnessEnhancer not supported: ${e.message}")
                    loudnessEnhancer = null
                }
            }

            if (eq != null) {
                val numBands = eq.numberOfBands.toInt()
                val minBandLevel = eq.bandLevelRange[0]
                val maxBandLevel = eq.bandLevelRange[1]

                val bands = (0 until numBands).map { bandIndex ->
                    val freq = eq.getCenterFreq(bandIndex.toShort()) / 1000 // Convert mHz to Hz
                    val currentLevel = eq.getBandLevel(bandIndex.toShort())
                    EqualizerBandState(
                        bandIndex = bandIndex.toShort(),
                        centerFreqHz = freq,
                        currentGainMb = currentLevel,
                        minGainMb = minBandLevel,
                        maxGainMb = maxBandLevel
                    )
                }

                val numPresets = eq.numberOfPresets.toInt()
                val presetNames = (0 until numPresets).map { presetIndex ->
                    try {
                        eq.getPresetName(presetIndex.toShort())
                    } catch (e: Exception) {
                        "Preset $presetIndex"
                    }
                } + listOf("Custom")

                val currentPreset: Short = try {
                    eq.currentPreset
                } catch (e: Exception) {
                    (-1).toShort()
                }

                val bb = bassBoost
                val vt = virtualizer
                val bbStrength: Short = if (bb != null && bb.strengthSupported) {
                    try { bb.roundedStrength } catch (e: Exception) { 0.toShort() }
                } else 0.toShort()

                val vtStrength: Short = if (vt != null && vt.strengthSupported) {
                    try { vt.roundedStrength } catch (e: Exception) { 0.toShort() }
                } else 0.toShort()

                _uiState.update {
                    it.copy(
                        isSupported = true,
                        isEnabled = eq.enabled,
                        bands = bands,
                        presets = presetNames,
                        currentPresetIndex = currentPreset,
                        bassBoostStrength = bbStrength,
                        virtualizerStrength = vtStrength
                    )
                }
            } else {
                // Graceful fallback for devices / emulators without hardware equalizer
                val defaultBands = listOf(
                    EqualizerBandState(0, 60, 0, -1500, 1500),
                    EqualizerBandState(1, 230, 0, -1500, 1500),
                    EqualizerBandState(2, 910, 0, -1500, 1500),
                    EqualizerBandState(3, 3600, 0, -1500, 1500),
                    EqualizerBandState(4, 14000, 0, -1500, 1500)
                )
                _uiState.update {
                    it.copy(
                        isSupported = false,
                        bands = defaultBands,
                        presets = listOf("Normal", "Rock", "Pop", "Jazz", "Classical", "Custom")
                    )
                }
            }
        } catch (e: Throwable) {
            Log.w("EqualizerManager", "Audio effects initialization handled gracefully: ${e.message}")
            _uiState.update { it.copy(isSupported = false) }
        }
    }

    fun setEqualizerEnabled(enabled: Boolean) {
        try {
            equalizer?.enabled = enabled
            bassBoost?.enabled = enabled
            virtualizer?.enabled = enabled
            presetReverb?.enabled = enabled && _uiState.value.reverbPreset > 0
            loudnessEnhancer?.enabled = enabled && _uiState.value.loudnessGainMb > 0
            _uiState.update { it.copy(isEnabled = enabled) }
        } catch (e: Exception) {
            Log.e("EqualizerManager", "Error toggling equalizer: ${e.message}")
        }
    }

    fun setReverbPreset(preset: Short) {
        try {
            if (preset > 0 && presetReverb == null) {
                try {
                    val availableEffects = AudioEffect.queryEffects() ?: emptyArray()
                    val reverbDesc = availableEffects.firstOrNull { it.type == AudioEffect.EFFECT_TYPE_PRESET_REVERB }
                    if (reverbDesc != null) {
                        val session = if (reverbDesc.connectMode.equals(AudioEffect.EFFECT_AUXILIARY, ignoreCase = true)) {
                            0
                        } else {
                            currentAudioSessionId
                        }
                        presetReverb = PresetReverb(0, session)
                    }
                } catch (e: Throwable) {
                    Log.w("EqualizerManager", "Hardware PresetReverb not supported: ${e.message}")
                    presetReverb = null
                }
            }
            val rv = presetReverb
            if (rv != null) {
                if (preset > 0) {
                    rv.preset = preset
                    rv.enabled = _uiState.value.isEnabled
                } else {
                    rv.preset = PresetReverb.PRESET_NONE
                    rv.enabled = false
                }
            }
            _uiState.update { it.copy(reverbPreset = preset) }
        } catch (e: Exception) {
            Log.e("EqualizerManager", "Error setting reverb preset $preset: ${e.message}")
        }
    }

    fun setLoudnessGain(gainMb: Int) {
        try {
            val le = loudnessEnhancer
            if (le != null) {
                le.setTargetGain(gainMb.coerceIn(0, 1200))
                le.enabled = _uiState.value.isEnabled && gainMb > 0
            }
            _uiState.update { it.copy(loudnessGainMb = gainMb) }
        } catch (e: Exception) {
            Log.e("EqualizerManager", "Error setting loudness gain: ${e.message}")
        }
    }

    fun setBandGain(bandIndex: Short, gainMb: Short) {
        try {
            equalizer?.setBandLevel(bandIndex, gainMb)
            _uiState.update { current ->
                val updatedBands = current.bands.map { band ->
                    if (band.bandIndex == bandIndex) {
                        band.copy(currentGainMb = gainMb)
                    } else {
                        band
                    }
                }
                current.copy(bands = updatedBands, currentPresetIndex = -1) // -1 is Custom
            }
        } catch (e: Exception) {
            Log.e("EqualizerManager", "Error setting band $bandIndex gain: ${e.message}")
        }
    }

    fun usePreset(presetIndex: Short) {
        val eq = equalizer ?: return
        try {
            if (presetIndex >= 0 && presetIndex < eq.numberOfPresets) {
                eq.usePreset(presetIndex)
                val updatedBands = _uiState.value.bands.map { band ->
                    val level = eq.getBandLevel(band.bandIndex)
                    band.copy(currentGainMb = level)
                }
                _uiState.update {
                    it.copy(bands = updatedBands, currentPresetIndex = presetIndex)
                }
            }
        } catch (e: Exception) {
            Log.e("EqualizerManager", "Error applying preset $presetIndex: ${e.message}")
        }
    }

    fun setBassBoost(strength: Short) {
        _uiState.update { it.copy(bassBoostStrength = strength) }
        if (strength <= 0) {
            try {
                bassBoost?.enabled = false
            } catch (_: Exception) {}
            return
        }
        if (!isBassBoostSupported) return

        if (bassBoost == null && currentAudioSessionId > 0) {
            try {
                bassBoost = BassBoost(0, currentAudioSessionId)
            } catch (e: Throwable) {
                Log.w("EqualizerManager", "Hardware BassBoost not supported on this session: ${e.message}")
                isBassBoostSupported = false
                bassBoost = null
                return
            }
        }
        try {
            val bb = bassBoost
            if (bb != null && bb.strengthSupported) {
                bb.enabled = _uiState.value.isEnabled
                bb.setStrength(strength.coerceIn(0, 1000))
            }
        } catch (e: Exception) {
            Log.e("EqualizerManager", "Error setting bass boost: ${e.message}")
        }
    }

    fun setVirtualizer(strength: Short) {
        _uiState.update { it.copy(virtualizerStrength = strength) }
        if (strength <= 0) {
            try {
                virtualizer?.enabled = false
            } catch (_: Exception) {}
            return
        }
        if (!isVirtualizerSupported) return

        if (virtualizer == null && currentAudioSessionId > 0) {
            try {
                virtualizer = Virtualizer(0, currentAudioSessionId)
            } catch (e: Throwable) {
                Log.w("EqualizerManager", "Hardware Virtualizer not supported on this session: ${e.message}")
                isVirtualizerSupported = false
                virtualizer = null
                return
            }
        }
        try {
            val vt = virtualizer
            if (vt != null && vt.strengthSupported) {
                vt.enabled = _uiState.value.isEnabled
                vt.setStrength(strength.coerceIn(0, 1000))
            }
        } catch (e: Exception) {
            Log.e("EqualizerManager", "Error setting virtualizer: ${e.message}")
        }
    }

    /**
     * Attempts to open the Android native device equalizer panel.
     * Returns true if an activity handled the intent, false otherwise.
     */
    fun openSystemEqualizer(): Boolean {
        return try {
            val intent = Intent(AudioEffect.ACTION_DISPLAY_AUDIO_EFFECT_CONTROL_PANEL).apply {
                putExtra(AudioEffect.EXTRA_AUDIO_SESSION, currentAudioSessionId)
                putExtra(AudioEffect.EXTRA_PACKAGE_NAME, context.packageName)
                putExtra(AudioEffect.EXTRA_CONTENT_TYPE, AudioEffect.CONTENT_TYPE_MUSIC)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            if (intent.resolveActivity(context.packageManager) != null) {
                context.startActivity(intent)
                true
            } else {
                false
            }
        } catch (e: Exception) {
            Log.w("EqualizerManager", "Native system equalizer intent failed: ${e.message}")
            false
        }
    }

    fun releaseEffects() {
        try {
            equalizer?.release()
        } catch (_: Exception) {}
        try {
            bassBoost?.release()
        } catch (_: Exception) {}
        try {
            virtualizer?.release()
        } catch (_: Exception) {}
        try {
            presetReverb?.release()
        } catch (_: Exception) {}
        try {
            loudnessEnhancer?.release()
        } catch (_: Exception) {}

        equalizer = null
        bassBoost = null
        virtualizer = null
        presetReverb = null
        loudnessEnhancer = null
    }
}
