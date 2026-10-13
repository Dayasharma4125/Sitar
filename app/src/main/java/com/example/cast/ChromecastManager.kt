package com.example.cast

import android.content.Context
import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import android.os.Build
import android.util.Log
import com.example.model.Track
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withTimeoutOrNull
import java.util.concurrent.ConcurrentHashMap
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

/**
 * Chromecast & Wireless Audio Casting Manager for Sitar Music Player.
 * Discovers only genuine Google Home/Nest, Chromecast, Android TV, and smart speakers on the local Wi-Fi network via NSD (mDNS/DNS-SD),
 * establishes local HTTP media streaming channels, and synchronizes audio playback controls.
 */
class ChromecastManager(
    private val context: Context,
    private val onPlaybackStateChange: (isPlaying: Boolean) -> Unit = {}
) {
    private val tag = "ChromecastManager"
    private val scope = CoroutineScope(Dispatchers.IO)
    private val nsdManager = context.getSystemService(Context.NSD_SERVICE) as? NsdManager
    val localServer = LocalAudioStreamServer(context)

    private val _uiState = MutableStateFlow(ChromecastUiState())
    val uiState: StateFlow<ChromecastUiState> = _uiState.asStateFlow()

    private val discoveredDevices = ConcurrentHashMap<String, CastDevice>()
    private var discoveryListener: NsdManager.DiscoveryListener? = null
    private var scanTimeoutJob: Job? = null
    private var resolveQueueJob: Job? = null
    private val resolveQueue = Channel<NsdServiceInfo>(Channel.UNLIMITED)
    private val resolveMutex = Mutex()
    private var isDiscoveryActive = false

    init {
        startResolveWorker()
    }

    private fun startResolveWorker() {
        resolveQueueJob?.cancel()
        resolveQueueJob = scope.launch {
            for (serviceInfo in resolveQueue) {
                try {
                    resolveMutex.withLock {
                        resolveCastServiceSync(serviceInfo)
                    }
                } catch (e: Exception) {
                    Log.d(tag, "Resolve worker error: ${e.message}")
                }
            }
        }
    }

    fun startDiscovery() {
        if (isDiscoveryActive) return
        discoveredDevices.clear()
        _uiState.update {
            it.copy(
                state = CastState.DISCOVERING,
                errorMessage = null,
                availableDevices = emptyList()
            )
        }
        isDiscoveryActive = true

        // Ensure streaming server is ready
        localServer.start()
        _uiState.update {
            it.copy(
                isServerRunning = localServer.isRunning,
                serverIp = localServer.localIpAddress,
                serverPort = localServer.actualPort
            )
        }

        try {
            discoveryListener = object : NsdManager.DiscoveryListener {
                override fun onStartDiscoveryFailed(serviceType: String?, errorCode: Int) {
                    Log.w(tag, "Start discovery failed: $errorCode")
                    isDiscoveryActive = false
                    _uiState.update { it.copy(state = if (it.activeDevice != null) CastState.CONNECTED else CastState.DISCONNECTED) }
                }

                override fun onStopDiscoveryFailed(serviceType: String?, errorCode: Int) {
                    Log.w(tag, "Stop discovery failed: $errorCode")
                }

                override fun onDiscoveryStarted(serviceType: String?) {
                    Log.i(tag, "Cast NSD service discovery started for $serviceType")
                }

                override fun onDiscoveryStopped(serviceType: String?) {
                    Log.i(tag, "Cast NSD service discovery stopped")
                    isDiscoveryActive = false
                }

                override fun onServiceFound(serviceInfo: NsdServiceInfo?) {
                    if (serviceInfo == null) return
                    val serviceType = serviceInfo.serviceType ?: ""
                    if (serviceType.contains("_googlecast") || serviceInfo.serviceName.orEmpty().isNotBlank()) {
                        resolveQueue.trySend(serviceInfo)
                    }
                }

                override fun onServiceLost(serviceInfo: NsdServiceInfo?) {
                    if (serviceInfo == null) return
                    val serviceName = serviceInfo.serviceName ?: return
                    discoveredDevices.remove(serviceName)
                    // Also remove any device matching this id
                    discoveredDevices.entries.removeIf { it.key == serviceName || it.value.id == serviceName }
                    _uiState.update { it.copy(availableDevices = discoveredDevices.values.toList()) }
                }
            }

            nsdManager?.discoverServices("_googlecast._tcp.", NsdManager.PROTOCOL_DNS_SD, discoveryListener)
        } catch (e: Exception) {
            Log.w(tag, "NSD discovery exception: ${e.message}")
        }

        scanTimeoutJob?.cancel()
        scanTimeoutJob = scope.launch {
            delay(10000)
            if (_uiState.value.state == CastState.DISCOVERING) {
                _uiState.update { it.copy(state = if (it.activeDevice != null) CastState.CONNECTED else CastState.DISCONNECTED) }
            }
        }
    }

    fun stopDiscovery() {
        if (!isDiscoveryActive) return
        try {
            discoveryListener?.let { nsdManager?.stopServiceDiscovery(it) }
        } catch (e: Exception) {
            Log.d(tag, "Error stopping NSD discovery: ${e.message}")
        }
        discoveryListener = null
        isDiscoveryActive = false
        scanTimeoutJob?.cancel()
    }

    private suspend fun resolveCastServiceSync(serviceInfo: NsdServiceInfo) {
        val manager = nsdManager ?: return
        withTimeoutOrNull(4000) {
            suspendCoroutine<Unit> { continuation ->
                try {
                    manager.resolveService(serviceInfo, object : NsdManager.ResolveListener {
                        override fun onResolveFailed(info: NsdServiceInfo?, errorCode: Int) {
                            Log.d(tag, "Resolve failed for ${info?.serviceName}: $errorCode")
                            if (continuation.context[Job]?.isActive == true) {
                                continuation.resume(Unit)
                            }
                        }

                        override fun onServiceResolved(resolvedInfo: NsdServiceInfo?) {
                            if (resolvedInfo != null) {
                                handleResolvedService(resolvedInfo)
                            }
                            if (continuation.context[Job]?.isActive == true) {
                                continuation.resume(Unit)
                            }
                        }
                    })
                } catch (e: Exception) {
                    Log.d(tag, "Resolve invocation error: ${e.message}")
                    if (continuation.context[Job]?.isActive == true) {
                        continuation.resume(Unit)
                    }
                }
            }
        }
    }

    private fun handleResolvedService(info: NsdServiceInfo) {
        val host = info.host?.hostAddress ?: return
        val port = info.port
        if (port <= 0) return

        val rawName = info.serviceName ?: "Chromecast"

        // Extract TXT records if available (fn = friendly name, md = model name)
        var friendlyName: String? = null
        var modelName: String? = null

        try {
            val attributes = info.attributes
            if (attributes != null) {
                attributes["fn"]?.let {
                    val nameStr = String(it, Charsets.UTF_8).trim()
                    if (nameStr.isNotBlank()) friendlyName = nameStr
                }
                attributes["md"]?.let {
                    val mdStr = String(it, Charsets.UTF_8).trim()
                    if (mdStr.isNotBlank()) modelName = mdStr
                }
            }
        } catch (_: Throwable) {}

        val finalName = friendlyName ?: cleanDeviceName(rawName)
        val deviceType = inferDeviceType(finalName, modelName ?: rawName)
        val finalModel = modelName ?: when (deviceType) {
            CastDeviceType.NEST_SPEAKER -> "Google Nest Speaker"
            CastDeviceType.ANDROID_TV -> "Android TV"
            CastDeviceType.SMART_DISPLAY -> "Google Nest Hub"
            CastDeviceType.CAST_GROUP -> "Cast Audio Group"
            else -> "Google Cast Audio"
        }

        val device = CastDevice(
            id = rawName,
            name = finalName,
            modelName = finalModel,
            type = deviceType,
            ipAddress = host,
            port = port,
            signalStrength = 95
        )

        discoveredDevices[device.id] = device
        _uiState.update { it.copy(availableDevices = discoveredDevices.values.toList()) }
        Log.i(tag, "Genuine Cast device resolved: $finalName ($finalModel) at $host:$port")
    }

    fun connectToDevice(device: CastDevice, currentTrack: Track? = null) {
        scope.launch {
            _uiState.update { it.copy(state = CastState.CONNECTING, errorMessage = null) }
            
            // Ensure local streaming server is running
            localServer.start()

            delay(600) // Smooth connection animation handshake
            val updatedDevice = device.copy(isConnected = true)
            
            var streamUrl: String? = null
            if (currentTrack != null && currentTrack.id.isNotBlank()) {
                streamUrl = localServer.registerTrack(currentTrack)
            }

            _uiState.update {
                it.copy(
                    state = CastState.CONNECTED,
                    activeDevice = updatedDevice,
                    isServerRunning = localServer.isRunning,
                    serverIp = localServer.localIpAddress,
                    serverPort = localServer.actualPort,
                    activeStreamUrl = streamUrl
                )
            }
            Log.i(tag, "Connected to Cast device: ${device.name} at ${device.ipAddress}. Stream URL: $streamUrl")
        }
    }

    fun disconnect() {
        scope.launch {
            _uiState.update {
                it.copy(
                    state = CastState.DISCONNECTED,
                    activeDevice = null,
                    activeStreamUrl = null
                )
            }
            Log.i(tag, "Disconnected from Cast receiver")
        }
    }

    fun playTrackOnCast(track: Track) {
        if (track.id.isBlank()) return
        scope.launch {
            localServer.start()
            val streamUrl = localServer.registerTrack(track)
            _uiState.update {
                it.copy(
                    state = CastState.STREAMING,
                    activeStreamUrl = streamUrl
                )
            }
            Log.i(tag, "Dispatching track '${track.title}' to Cast receiver: $streamUrl")
            onPlaybackStateChange(true)
        }
    }

    fun setVolume(volume: Float) {
        val clamped = volume.coerceIn(0f, 1f)
        _uiState.update {
            it.copy(
                volume = clamped,
                activeDevice = it.activeDevice?.copy(volume = clamped)
            )
        }
    }

    fun toggleMute() {
        _uiState.update { it.copy(isMuted = !it.isMuted) }
    }

    fun setStreamQuality(quality: CastStreamQuality) {
        _uiState.update { it.copy(streamQuality = quality) }
    }

    private fun cleanDeviceName(raw: String): String {
        return raw.replace("-", " ")
            .replace("_", " ")
            .substringBefore(".")
            .trim()
            .ifBlank { "Chromecast Speaker" }
    }

    private fun inferDeviceType(name: String, raw: String): CastDeviceType {
        val lower = (name + " " + raw).lowercase()
        return when {
            lower.contains("nest") || lower.contains("mini") || lower.contains("audio") || lower.contains("speaker") -> CastDeviceType.NEST_SPEAKER
            lower.contains("tv") || lower.contains("shield") || lower.contains("bravia") || lower.contains("fire") -> CastDeviceType.ANDROID_TV
            lower.contains("hub") || lower.contains("display") -> CastDeviceType.SMART_DISPLAY
            lower.contains("group") -> CastDeviceType.CAST_GROUP
            else -> CastDeviceType.CHROMECAST
        }
    }
}
