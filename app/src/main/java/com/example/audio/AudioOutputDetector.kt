package com.example.audio

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioDeviceInfo
import android.media.AudioManager
import android.os.Build
import android.provider.Settings
import com.example.model.AudioDeviceType
import com.example.model.ConnectedAudioDevice
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AudioOutputDetector(
    private val context: Context,
    private val onBecomingNoisy: () -> Unit
) {
    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private val scope = CoroutineScope(Dispatchers.Main)

    // User manual routing override tracking to prevent automatic detector bouncing
    private var userPinnedDeviceId: Int? = null
    private var userPinnedDeviceType: AudioDeviceType? = null
    private var userPinnedDeviceName: String? = null

    private val defaultSpeakerDevice = ConnectedAudioDevice(
        type = AudioDeviceType.SPEAKER,
        name = "Internal Speaker",
        description = "Device Stereo Speakers",
        id = 0
    )

    private val _currentDevice = MutableStateFlow(defaultSpeakerDevice)
    val currentDevice: StateFlow<ConnectedAudioDevice> = _currentDevice.asStateFlow()

    private val _availableDevices = MutableStateFlow<List<ConnectedAudioDevice>>(listOf(defaultSpeakerDevice))
    val availableDevices: StateFlow<List<ConnectedAudioDevice>> = _availableDevices.asStateFlow()

    private val noisyReceiver = object : BroadcastReceiver() {
        override fun onReceive(c: Context?, intent: Intent?) {
            when (intent?.action) {
                AudioManager.ACTION_AUDIO_BECOMING_NOISY -> {
                    onBecomingNoisy()
                    refreshDevice()
                }
                Intent.ACTION_HEADSET_PLUG -> {
                    refreshDevice()
                }
            }
        }
    }

    private val audioDeviceCallback = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        object : android.media.AudioDeviceCallback() {
            override fun onAudioDevicesAdded(addedDevices: Array<out AudioDeviceInfo>?) {
                refreshDevice()
            }

            override fun onAudioDevicesRemoved(removedDevices: Array<out AudioDeviceInfo>?) {
                // If pinned device was disconnected, clear pinned state
                if (userPinnedDeviceId != null && removedDevices != null) {
                    val removed = removedDevices.any { it.id == userPinnedDeviceId }
                    if (removed) {
                        userPinnedDeviceId = null
                        userPinnedDeviceType = null
                        userPinnedDeviceName = null
                        onBecomingNoisy()
                    }
                }
                refreshDevice()
            }
        }
    } else null

    fun startListening() {
        refreshDevice()

        val filter = IntentFilter().apply {
            addAction(AudioManager.ACTION_AUDIO_BECOMING_NOISY)
            addAction(Intent.ACTION_HEADSET_PLUG)
        }
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                context.registerReceiver(noisyReceiver, filter, Context.RECEIVER_NOT_EXPORTED)
            } else {
                context.registerReceiver(noisyReceiver, filter)
            }
        } catch (e: Exception) {
            android.util.Log.w("AudioOutputDetector", "Error registering receiver: ${e.message}")
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && audioDeviceCallback != null) {
            try {
                audioManager.registerAudioDeviceCallback(audioDeviceCallback, null)
            } catch (e: Exception) {
                android.util.Log.w("AudioOutputDetector", "Error registering AudioDeviceCallback: ${e.message}")
            }
        }
    }

    fun stopListening() {
        try {
            context.unregisterReceiver(noisyReceiver)
        } catch (_: Exception) {}

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && audioDeviceCallback != null) {
            try {
                audioManager.unregisterAudioDeviceCallback(audioDeviceCallback)
            } catch (_: Exception) {}
        }
    }

    fun refreshDevice() {
        scope.launch(Dispatchers.IO) {
            val avail = detectAllAvailableDevices()
            val curr = detectCurrentDevice()
            _availableDevices.value = avail
            _currentDevice.value = curr
        }
    }

    fun detectAllAvailableDevices(): List<ConnectedAudioDevice> {
        val list = mutableListOf<ConnectedAudioDevice>()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val devices = try {
                audioManager.getDevices(AudioManager.GET_DEVICES_OUTPUTS)
            } catch (_: Throwable) {
                emptyArray()
            }

            for (dev in devices) {
                val devName = try {
                    dev.productName?.toString().takeIf { !it.isNullOrBlank() }
                } catch (_: Throwable) {
                    null
                }

                when (dev.type) {
                    AudioDeviceInfo.TYPE_BLUETOOTH_A2DP,
                    AudioDeviceInfo.TYPE_BLUETOOTH_SCO -> {
                        list.add(
                            ConnectedAudioDevice(
                                type = AudioDeviceType.BLUETOOTH,
                                name = devName ?: "Bluetooth Audio",
                                description = "Wireless Audio Stream (A2DP)",
                                id = dev.id
                            )
                        )
                    }
                    AudioDeviceInfo.TYPE_WIRED_HEADSET,
                    AudioDeviceInfo.TYPE_WIRED_HEADPHONES,
                    AudioDeviceInfo.TYPE_LINE_ANALOG,
                    AudioDeviceInfo.TYPE_LINE_DIGITAL,
                    AudioDeviceInfo.TYPE_AUX_LINE -> {
                        list.add(
                            ConnectedAudioDevice(
                                type = AudioDeviceType.WIRED_HEADPHONES,
                                name = devName ?: "Wired Headphones",
                                description = "Direct 3.5mm Analog Output",
                                id = dev.id
                            )
                        )
                    }
                    AudioDeviceInfo.TYPE_USB_DEVICE,
                    AudioDeviceInfo.TYPE_USB_HEADSET,
                    AudioDeviceInfo.TYPE_USB_ACCESSORY -> {
                        list.add(
                            ConnectedAudioDevice(
                                type = AudioDeviceType.USB_DAC,
                                name = devName ?: "USB Audio Output",
                                description = "External Digital Audio Processor",
                                id = dev.id
                            )
                        )
                    }
                    AudioDeviceInfo.TYPE_BUILTIN_SPEAKER -> {
                        list.add(
                            ConnectedAudioDevice(
                                type = AudioDeviceType.SPEAKER,
                                name = devName ?: "Internal Speaker",
                                description = "Device Stereo Speakers",
                                id = dev.id
                            )
                        )
                    }
                    AudioDeviceInfo.TYPE_BUILTIN_EARPIECE -> {
                        list.add(
                            ConnectedAudioDevice(
                                type = AudioDeviceType.SPEAKER,
                                name = devName ?: "Earpiece Speaker",
                                description = "Device Earpiece",
                                id = dev.id
                            )
                        )
                    }
                    else -> {
                        // Check for Android S (API 31) and Hearing Aid (API 28)
                        val isBle = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                            dev.type == AudioDeviceInfo.TYPE_BLE_HEADSET ||
                                dev.type == AudioDeviceInfo.TYPE_BLE_SPEAKER ||
                                dev.type == AudioDeviceInfo.TYPE_BLE_BROADCAST
                        } else false
                        val isHearingAid = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                            dev.type == AudioDeviceInfo.TYPE_HEARING_AID
                        } else false

                        if (isBle || isHearingAid) {
                            list.add(
                                ConnectedAudioDevice(
                                    type = AudioDeviceType.BLUETOOTH,
                                    name = devName ?: "BLE Wireless Audio",
                                    description = "Low-Energy Bluetooth Output",
                                    id = dev.id
                                )
                            )
                        } else if (dev.isSink) {
                            list.add(
                                ConnectedAudioDevice(
                                    type = AudioDeviceType.OTHER,
                                    name = devName ?: "External Device",
                                    description = "Direct Audio Output",
                                    id = dev.id
                                )
                            )
                        }
                    }
                }
            }
        }

        // Ensure built-in speaker is always present as an available choice
        if (list.none { it.type == AudioDeviceType.SPEAKER }) {
            list.add(
                ConnectedAudioDevice(
                    type = AudioDeviceType.SPEAKER,
                    name = "Internal Speaker",
                    description = "Device Stereo Speakers",
                    id = 0
                )
            )
        }

        return list.distinctBy { if (it.id != 0) "${it.type}_${it.id}" else it.name }
    }

    /**
     * Physically routes audio to the selected device via Media3 Track Selection and ExoPlayer Device APIs.
     * Keeps pinned state so background callbacks don't revert user choice.
     */
    fun routeAudioToDevice(targetDevice: ConnectedAudioDevice, player: androidx.media3.exoplayer.ExoPlayer) {
        if (targetDevice.isSystemDefault || targetDevice.type == AudioDeviceType.SYSTEM_DEFAULT) {
            resetToSystemDefault(player)
            return
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val devices = try {
                audioManager.getDevices(AudioManager.GET_DEVICES_OUTPUTS)
            } catch (_: Throwable) {
                emptyArray()
            }

            // 1. Try matching by exact device id first
            var matchedDevice: AudioDeviceInfo? = if (targetDevice.id != 0) {
                devices.firstOrNull { it.id == targetDevice.id }
            } else null

            // 2. Fallback to matching by type & name
            if (matchedDevice == null) {
                matchedDevice = when (targetDevice.type) {
                    AudioDeviceType.SPEAKER -> {
                        devices.firstOrNull { it.type == AudioDeviceInfo.TYPE_BUILTIN_SPEAKER }
                            ?: devices.firstOrNull { it.type == AudioDeviceInfo.TYPE_BUILTIN_EARPIECE }
                    }
                    AudioDeviceType.BLUETOOTH -> {
                        devices.firstOrNull {
                            it.type == AudioDeviceInfo.TYPE_BLUETOOTH_A2DP ||
                                it.type == AudioDeviceInfo.TYPE_BLUETOOTH_SCO ||
                                (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && (it.type == AudioDeviceInfo.TYPE_BLE_HEADSET || it.type == AudioDeviceInfo.TYPE_BLE_SPEAKER)) ||
                                (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P && it.type == AudioDeviceInfo.TYPE_HEARING_AID)
                        }
                    }
                    AudioDeviceType.WIRED_HEADPHONES -> {
                        devices.firstOrNull {
                            it.type == AudioDeviceInfo.TYPE_WIRED_HEADSET ||
                                it.type == AudioDeviceInfo.TYPE_WIRED_HEADPHONES ||
                                it.type == AudioDeviceInfo.TYPE_LINE_ANALOG ||
                                it.type == AudioDeviceInfo.TYPE_AUX_LINE
                        }
                    }
                    AudioDeviceType.USB_DAC -> {
                        devices.firstOrNull {
                            it.type == AudioDeviceInfo.TYPE_USB_DEVICE ||
                                it.type == AudioDeviceInfo.TYPE_USB_HEADSET ||
                                it.type == AudioDeviceInfo.TYPE_USB_ACCESSORY
                        }
                    }
                    else -> devices.firstOrNull { it.productName?.toString() == targetDevice.name }
                }
            }

            if (matchedDevice != null) {
                userPinnedDeviceId = matchedDevice.id
                userPinnedDeviceType = targetDevice.type
                userPinnedDeviceName = targetDevice.name

                try {
                    player.setPreferredAudioDevice(matchedDevice)
                } catch (e: Exception) {
                    android.util.Log.w("AudioOutputDetector", "Error setting preferred device on player: ${e.message}")
                }

                // Android 12+ Communication Override handling for internal speaker override
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    try {
                        if (targetDevice.type == AudioDeviceType.SPEAKER) {
                            audioManager.setCommunicationDevice(matchedDevice)
                        } else {
                            audioManager.clearCommunicationDevice()
                        }
                    } catch (_: Exception) {}
                }

                _currentDevice.value = targetDevice.copy(id = matchedDevice.id)
                return
            }
        }

        // Fallback if matched device was null or pre-M
        userPinnedDeviceId = null
        userPinnedDeviceType = null
        userPinnedDeviceName = null
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            try { player.setPreferredAudioDevice(null) } catch (_: Exception) {}
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            try { audioManager.clearCommunicationDevice() } catch (_: Exception) {}
        }
        _currentDevice.value = detectCurrentDevice()
    }

    /**
     * Resets audio routing to standard Android OS automatic management.
     */
    fun resetToSystemDefault(player: androidx.media3.exoplayer.ExoPlayer) {
        userPinnedDeviceId = null
        userPinnedDeviceType = null
        userPinnedDeviceName = null

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            try {
                player.setPreferredAudioDevice(null)
            } catch (_: Exception) {}
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            try {
                audioManager.clearCommunicationDevice()
            } catch (_: Exception) {}
        }

        refreshDevice()
    }

    /**
     * Clears preferred devices and communication overrides on player release.
     */
    fun releasePlayerAndRouting(player: androidx.media3.exoplayer.ExoPlayer) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            try {
                player.setPreferredAudioDevice(null)
            } catch (_: Exception) {}
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            try {
                audioManager.clearCommunicationDevice()
            } catch (_: Exception) {}
        }
    }

    /**
     * Safely launches system Media Output Switcher panel (Android 11+) or Bluetooth/Sound settings.
     */
    fun openSystemAudioSettings(activityOrContext: Context): Boolean {
        // 1. Android 11+ Media Output Panel
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            try {
                val intent = Intent("com.android.settings.panel.action.MEDIA_OUTPUT").apply {
                    putExtra("com.android.settings.panel.extra.PACKAGE_NAME", activityOrContext.packageName)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                activityOrContext.startActivity(intent)
                return true
            } catch (_: Throwable) {}
        }

        // 2. Bluetooth Settings
        try {
            val intent = Intent(Settings.ACTION_BLUETOOTH_SETTINGS).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            activityOrContext.startActivity(intent)
            return true
        } catch (_: Throwable) {}

        // 3. Sound Settings fallback
        try {
            val intent = Intent(Settings.ACTION_SOUND_SETTINGS).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            activityOrContext.startActivity(intent)
            return true
        } catch (_: Throwable) {}

        return false
    }

    private fun detectCurrentDevice(): ConnectedAudioDevice {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val devices = try {
                    audioManager.getDevices(AudioManager.GET_DEVICES_OUTPUTS)
                } catch (_: Throwable) {
                    emptyArray()
                }

                // If user pinned a specific device, verify it's still connected
                if (userPinnedDeviceId != null) {
                    val pinned = devices.firstOrNull { it.id == userPinnedDeviceId }
                        ?: if (userPinnedDeviceType != null) {
                            devices.firstOrNull { it.productName?.toString() == userPinnedDeviceName }
                        } else null

                    if (pinned != null) {
                        val name = pinned.productName?.toString().takeIf { !it.isNullOrBlank() }
                            ?: userPinnedDeviceName ?: "Selected Device"
                        return ConnectedAudioDevice(
                            type = userPinnedDeviceType ?: AudioDeviceType.OTHER,
                            name = name,
                            description = "Active Selected Output",
                            id = pinned.id
                        )
                    } else {
                        // Device disconnected, clear pin
                        userPinnedDeviceId = null
                        userPinnedDeviceType = null
                        userPinnedDeviceName = null
                    }
                }

                // 1. Check for Bluetooth Audio (A2DP, BLE Headset, Hearing Aid)
                val btDevice = devices.firstOrNull { dev ->
                    dev.type == AudioDeviceInfo.TYPE_BLUETOOTH_A2DP ||
                        dev.type == AudioDeviceInfo.TYPE_BLUETOOTH_SCO ||
                        (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && (dev.type == AudioDeviceInfo.TYPE_BLE_HEADSET || dev.type == AudioDeviceInfo.TYPE_BLE_SPEAKER || dev.type == AudioDeviceInfo.TYPE_BLE_BROADCAST)) ||
                        (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P && dev.type == AudioDeviceInfo.TYPE_HEARING_AID)
                }
                if (btDevice != null) {
                    val name = try {
                        btDevice.productName?.toString().takeIf { !it.isNullOrBlank() }
                    } catch (_: Throwable) {
                        null
                    } ?: "Bluetooth Audio"
                    return ConnectedAudioDevice(
                        type = AudioDeviceType.BLUETOOTH,
                        name = name,
                        description = "Bluetooth Audio • High Quality Wireless",
                        id = btDevice.id
                    )
                }

                // 2. Check for Wired Headset / Headphones
                val wiredDevice = devices.firstOrNull { dev ->
                    dev.type == AudioDeviceInfo.TYPE_WIRED_HEADSET ||
                        dev.type == AudioDeviceInfo.TYPE_WIRED_HEADPHONES ||
                        dev.type == AudioDeviceInfo.TYPE_LINE_ANALOG ||
                        dev.type == AudioDeviceInfo.TYPE_AUX_LINE
                }
                if (wiredDevice != null) {
                    val name = try {
                        wiredDevice.productName?.toString().takeIf { !it.isNullOrBlank() }
                    } catch (_: Throwable) {
                        null
                    } ?: "Wired Headphones"
                    return ConnectedAudioDevice(
                        type = AudioDeviceType.WIRED_HEADPHONES,
                        name = name,
                        description = "3.5mm Direct Analog Output",
                        id = wiredDevice.id
                    )
                }

                // 3. Check for USB DAC / Digital Headset
                val usbDevice = devices.firstOrNull { dev ->
                    dev.type == AudioDeviceInfo.TYPE_USB_DEVICE ||
                        dev.type == AudioDeviceInfo.TYPE_USB_HEADSET ||
                        dev.type == AudioDeviceInfo.TYPE_USB_ACCESSORY
                }
                if (usbDevice != null) {
                    val name = try {
                        usbDevice.productName?.toString().takeIf { !it.isNullOrBlank() }
                    } catch (_: Throwable) {
                        null
                    } ?: "USB DAC Audio"
                    return ConnectedAudioDevice(
                        type = AudioDeviceType.USB_DAC,
                        name = name,
                        description = "External Digital-to-Analog Converter",
                        id = usbDevice.id
                    )
                }

                // 4. Default to Built-in Speaker
                val speakerDevice = devices.firstOrNull { dev ->
                    dev.type == AudioDeviceInfo.TYPE_BUILTIN_SPEAKER ||
                        dev.type == AudioDeviceInfo.TYPE_BUILTIN_EARPIECE
                }
                val speakerName = try {
                    speakerDevice?.productName?.toString().takeIf { !it.isNullOrBlank() }
                } catch (_: Throwable) {
                    null
                } ?: "Device Speaker"
                return ConnectedAudioDevice(
                    type = AudioDeviceType.SPEAKER,
                    name = speakerName,
                    description = "Internal Stereo Speakers",
                    id = speakerDevice?.id ?: 0
                )
            }

            // Fallback for pre-Marshmallow
            val isWired = try { audioManager.isWiredHeadsetOn } catch (_: Throwable) { false }
            val isBt = try { audioManager.isBluetoothA2dpOn || audioManager.isBluetoothScoOn } catch (_: Throwable) { false }
            return when {
                isBt -> ConnectedAudioDevice(AudioDeviceType.BLUETOOTH, "Bluetooth Audio", "Wireless Audio Stream", id = 0)
                isWired -> ConnectedAudioDevice(AudioDeviceType.WIRED_HEADPHONES, "Wired Headphones", "Headphone Jack", id = 0)
                else -> ConnectedAudioDevice(AudioDeviceType.SPEAKER, "Device Speaker", "Internal Device Audio", id = 0)
            }
        } catch (_: Throwable) {
            return ConnectedAudioDevice(AudioDeviceType.SPEAKER, "Internal Speaker", "Device Stereo Speakers", id = 0)
        }
    }
}
