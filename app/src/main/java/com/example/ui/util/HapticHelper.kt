package com.example.ui.util

import android.content.Context
import android.os.Build
import android.os.CombinedVibration
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback

enum class HapticType {
    LIGHT_CLICK,
    MEDIUM_CLICK,
    HEAVY_CLICK,
    DOUBLE_CLICK,
    TICK
}

class HapticController(
    private val context: Context,
    private val composeHaptics: HapticFeedback?
) {
    private val vibrator: Vibrator? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val manager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
        manager?.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    }

    fun performHaptic(type: HapticType = HapticType.MEDIUM_CLICK) {
        try {
            if (vibrator != null && vibrator.hasVibrator()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    val effectId = when (type) {
                        HapticType.LIGHT_CLICK -> VibrationEffect.EFFECT_TICK
                        HapticType.MEDIUM_CLICK -> VibrationEffect.EFFECT_CLICK
                        HapticType.HEAVY_CLICK -> VibrationEffect.EFFECT_HEAVY_CLICK
                        HapticType.DOUBLE_CLICK -> VibrationEffect.EFFECT_DOUBLE_CLICK
                        HapticType.TICK -> VibrationEffect.EFFECT_TICK
                    }
                    vibrator.vibrate(VibrationEffect.createPredefined(effectId))
                    return
                } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    val durationMs = when (type) {
                        HapticType.LIGHT_CLICK, HapticType.TICK -> 15L
                        HapticType.MEDIUM_CLICK -> 25L
                        HapticType.HEAVY_CLICK -> 40L
                        HapticType.DOUBLE_CLICK -> 35L
                    }
                    val amplitude = when (type) {
                        HapticType.LIGHT_CLICK, HapticType.TICK -> 80
                        HapticType.MEDIUM_CLICK -> 160
                        HapticType.HEAVY_CLICK -> 255
                        HapticType.DOUBLE_CLICK -> 200
                    }
                    vibrator.vibrate(VibrationEffect.createOneShot(durationMs, amplitude))
                    return
                }
            }
        } catch (_: Exception) {
            // Fall back to Compose HapticFeedback if Vibrator throws SecurityException or is unavailable
        }

        // Fallback Compose standard haptics
        try {
            composeHaptics?.performHapticFeedback(
                when (type) {
                    HapticType.HEAVY_CLICK, HapticType.DOUBLE_CLICK -> HapticFeedbackType.LongPress
                    else -> HapticFeedbackType.TextHandleMove
                }
            )
        } catch (_: Exception) {}
    }
}

@Composable
fun rememberHapticController(): HapticController {
    val context = LocalContext.current
    val composeHaptics = LocalHapticFeedback.current
    return remember(context, composeHaptics) {
        HapticController(context, composeHaptics)
    }
}
