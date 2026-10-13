package com.example.cast

import android.content.Context
import android.net.Uri
import android.util.Log
import com.example.model.Track
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.io.BufferedInputStream
import java.io.File
import java.io.InputStream
import java.io.OutputStream
import java.net.Inet4Address
import java.net.InetAddress
import java.net.NetworkInterface
import java.net.ServerSocket
import java.net.Socket
import java.util.StringTokenizer
import java.util.concurrent.ConcurrentHashMap

/**
 * High-performance, lightweight local HTTP media streaming server.
 * Enables Chromecast, Google Nest Audio, and DLNA/UPnP receivers on the same Wi-Fi network
 * to stream local audio tracks with byte-range seek support and accurate MIME headers.
 */
class LocalAudioStreamServer(
    private val context: Context,
    private val requestedPort: Int = 8989
) {
    private val tag = "LocalAudioStreamServer"
    private var serverSocket: ServerSocket? = null
    private var serverJob: Job? = null
    private val serverScope = CoroutineScope(Dispatchers.IO)
    private var activeTrackMap = ConcurrentHashMap<String, Track>()
    
    var actualPort: Int = requestedPort
        private set

    var localIpAddress: String = "127.0.0.1"
        private set

    val isRunning: Boolean
        get() = serverSocket != null && !serverSocket!!.isClosed

    fun start(): Boolean {
        if (isRunning) return true

        return try {
            localIpAddress = getLocalWifiIpAddress()
            var portToUse = requestedPort
            
            // Attempt to bind to requested port or next available port
            var boundSocket: ServerSocket? = null
            for (p in portToUse..(portToUse + 10)) {
                try {
                    boundSocket = ServerSocket(p)
                    actualPort = p
                    break
                } catch (e: Exception) {
                    Log.d(tag, "Port $p occupied, trying next...")
                }
            }

            if (boundSocket == null) {
                boundSocket = ServerSocket(0)
                actualPort = boundSocket.localPort
            }

            serverSocket = boundSocket
            Log.i(tag, "Local audio streaming server started at http://$localIpAddress:$actualPort")

            serverJob = serverScope.launch {
                while (isRunning) {
                    try {
                        val clientSocket = serverSocket?.accept() ?: break
                        serverScope.launch {
                            handleClientRequest(clientSocket)
                        }
                    } catch (e: Exception) {
                        if (!isRunning) break
                        Log.w(tag, "Error accepting client: ${e.message}")
                    }
                }
            }
            true
        } catch (e: Exception) {
            Log.e(tag, "Failed to start local audio streaming server: ${e.message}", e)
            false
        }
    }

    fun stop() {
        try {
            serverSocket?.close()
            serverSocket = null
            serverJob?.cancel()
            serverJob = null
            activeTrackMap.clear()
            Log.i(tag, "Local audio streaming server stopped")
        } catch (e: Exception) {
            Log.w(tag, "Error stopping server: ${e.message}")
        }
    }

    fun registerTrack(track: Track): String {
        activeTrackMap[track.id] = track
        val safeId = Uri.encode(track.id)
        val ext = when (track.formatName.uppercase()) {
            "FLAC" -> "flac"
            "WAV" -> "wav"
            "M4A", "AAC" -> "m4a"
            "OGG" -> "ogg"
            else -> "mp3"
        }
        return "http://$localIpAddress:$actualPort/audio/$safeId.$ext"
    }

    fun getArtworkUrl(track: Track): String {
        val safeId = Uri.encode(track.id)
        return "http://$localIpAddress:$actualPort/artwork/$safeId.jpg"
    }

    private fun handleClientRequest(socket: Socket) {
        try {
            socket.use { s ->
                s.soTimeout = 15000
                val reader = s.getInputStream().bufferedReader()
                val requestLine = reader.readLine() ?: return
                val tokenizer = StringTokenizer(requestLine)
                if (!tokenizer.hasMoreTokens()) return
                val method = tokenizer.nextToken()
                if (!tokenizer.hasMoreTokens()) return
                val path = tokenizer.nextToken()

                var rangeHeader: String? = null
                var line: String? = reader.readLine()
                while (!line.isNullOrEmpty()) {
                    if (line.startsWith("Range:", ignoreCase = true)) {
                        rangeHeader = line.substring(6).trim()
                    }
                    line = reader.readLine()
                }

                if (method.equals("GET", ignoreCase = true) || method.equals("HEAD", ignoreCase = true)) {
                    when {
                        path.startsWith("/audio/") -> serveAudioStream(s.getOutputStream(), path, rangeHeader, method.equals("HEAD", ignoreCase = true))
                        path.startsWith("/artwork/") -> serveArtwork(s.getOutputStream(), path, method.equals("HEAD", ignoreCase = true))
                        path == "/status" || path == "/" -> serveStatus(s.getOutputStream())
                        else -> send404(s.getOutputStream())
                    }
                } else {
                    send405(s.getOutputStream())
                }
            }
        } catch (e: Exception) {
            Log.d(tag, "Client connection finished/closed: ${e.message}")
        }
    }

    private fun serveAudioStream(outputStream: OutputStream, path: String, rangeHeader: String?, isHeadOnly: Boolean) {
        try {
            val fileName = path.removePrefix("/audio/").substringBefore("?")
            val trackId = Uri.decode(fileName.substringBeforeLast("."))
            val track = activeTrackMap[trackId] ?: findTrackFallback(trackId)

            if (track == null) {
                send404(outputStream)
                return
            }

            val uriString = track.contentUri ?: track.filePath
            if (uriString.isNullOrBlank()) {
                send404(outputStream)
                return
            }

            val uri = Uri.parse(uriString)
            val totalLength = getFileSize(uri)
            val mimeType = getMimeType(track)

            if (totalLength <= 0L) {
                send404(outputStream)
                return
            }

            var startByte = 0L
            var endByte = totalLength - 1L

            if (!rangeHeader.isNullOrBlank() && rangeHeader.startsWith("bytes=")) {
                val rangeVal = rangeHeader.substring(6).trim()
                val dashIdx = rangeVal.indexOf('-')
                if (dashIdx != -1) {
                    val startStr = rangeVal.substring(0, dashIdx).trim()
                    val endStr = rangeVal.substring(dashIdx + 1).trim()
                    if (startStr.isNotEmpty()) {
                        startByte = startStr.toLongOrNull() ?: 0L
                    }
                    if (endStr.isNotEmpty()) {
                        endByte = endStr.toLongOrNull() ?: (totalLength - 1L)
                    }
                }
            }

            val contentLength = (endByte - startByte + 1L).coerceAtLeast(0L)
            val isPartial = rangeHeader != null

            val headerBuilder = StringBuilder()
            if (isPartial) {
                headerBuilder.append("HTTP/1.1 206 Partial Content\r\n")
                headerBuilder.append("Content-Range: bytes $startByte-$endByte/$totalLength\r\n")
            } else {
                headerBuilder.append("HTTP/1.1 200 OK\r\n")
            }
            headerBuilder.append("Content-Type: $mimeType\r\n")
            headerBuilder.append("Content-Length: $contentLength\r\n")
            headerBuilder.append("Accept-Ranges: bytes\r\n")
            headerBuilder.append("Connection: keep-alive\r\n")
            headerBuilder.append("Access-Control-Allow-Origin: *\r\n")
            headerBuilder.append("\r\n")

            outputStream.write(headerBuilder.toString().toByteArray(Charsets.UTF_8))
            outputStream.flush()

            if (!isHeadOnly && contentLength > 0L) {
                openInputStream(uri)?.use { rawIn ->
                    val bis = BufferedInputStream(rawIn, 64 * 1024)
                    if (startByte > 0) {
                        bis.skip(startByte)
                    }
                    val buffer = ByteArray(32 * 1024)
                    var bytesRemaining = contentLength
                    while (bytesRemaining > 0) {
                        val toRead = minOf(buffer.size.toLong(), bytesRemaining).toInt()
                        val read = bis.read(buffer, 0, toRead)
                        if (read == -1) break
                        outputStream.write(buffer, 0, read)
                        bytesRemaining -= read
                    }
                    outputStream.flush()
                }
            }
        } catch (e: Exception) {
            Log.d(tag, "Audio stream pipe completed: ${e.message}")
        }
    }

    private fun serveArtwork(outputStream: OutputStream, path: String, isHeadOnly: Boolean) {
        try {
            val fileName = path.removePrefix("/artwork/").substringBefore("?")
            val trackId = Uri.decode(fileName.substringBeforeLast("."))
            val track = activeTrackMap[trackId] ?: findTrackFallback(trackId)

            val artUriStr = track?.albumArtUri
            if (!artUriStr.isNullOrBlank()) {
                val uri = Uri.parse(artUriStr)
                openInputStream(uri)?.use { rawIn ->
                    val bytes = rawIn.readBytes()
                    val response = "HTTP/1.1 200 OK\r\n" +
                            "Content-Type: image/jpeg\r\n" +
                            "Content-Length: ${bytes.size}\r\n" +
                            "Access-Control-Allow-Origin: *\r\n\r\n"
                    outputStream.write(response.toByteArray(Charsets.UTF_8))
                    if (!isHeadOnly) {
                        outputStream.write(bytes)
                    }
                    outputStream.flush()
                    return
                }
            }
            send404(outputStream)
        } catch (e: Exception) {
            send404(outputStream)
        }
    }

    private fun serveStatus(outputStream: OutputStream) {
        val body = "{\"status\":\"running\",\"service\":\"Sitar Cast Media Server\",\"ip\":\"$localIpAddress\",\"port\":$actualPort}"
        val bytes = body.toByteArray(Charsets.UTF_8)
        val response = "HTTP/1.1 200 OK\r\n" +
                "Content-Type: application/json\r\n" +
                "Content-Length: ${bytes.size}\r\n" +
                "Access-Control-Allow-Origin: *\r\n\r\n"
        outputStream.write(response.toByteArray(Charsets.UTF_8))
        outputStream.write(bytes)
        outputStream.flush()
    }

    private fun send404(outputStream: OutputStream) {
        val response = "HTTP/1.1 404 Not Found\r\nContent-Length: 0\r\n\r\n"
        outputStream.write(response.toByteArray(Charsets.UTF_8))
        outputStream.flush()
    }

    private fun send405(outputStream: OutputStream) {
        val response = "HTTP/1.1 405 Method Not Allowed\r\nContent-Length: 0\r\n\r\n"
        outputStream.write(response.toByteArray(Charsets.UTF_8))
        outputStream.flush()
    }

    private fun findTrackFallback(trackId: String): Track? {
        return activeTrackMap.values.find { it.id == trackId || it.contentUri?.contains(trackId) == true }
    }

    private fun getFileSize(uri: Uri): Long {
        return try {
            if (uri.scheme == "file") {
                val path = uri.path ?: return 0L
                File(path).length()
            } else {
                context.contentResolver.openFileDescriptor(uri, "r")?.use { pfd ->
                    pfd.statSize
                } ?: 0L
            }
        } catch (e: Exception) {
            0L
        }
    }

    private fun openInputStream(uri: Uri): InputStream? {
        return try {
            if (uri.scheme == "file") {
                val path = uri.path ?: return null
                File(path).inputStream()
            } else {
                context.contentResolver.openInputStream(uri)
            }
        } catch (e: Exception) {
            null
        }
    }

    private fun getMimeType(track: Track): String {
        track.mimeType?.takeIf { it.isNotBlank() }?.let { return it }
        return when (track.formatName.uppercase()) {
            "FLAC" -> "audio/flac"
            "WAV" -> "audio/wav"
            "M4A", "AAC" -> "audio/mp4"
            "OGG" -> "audio/ogg"
            "OPUS" -> "audio/opus"
            else -> "audio/mpeg"
        }
    }

    private fun getLocalWifiIpAddress(): String {
        try {
            val interfaces = NetworkInterface.getNetworkInterfaces()
            while (interfaces.hasMoreElements()) {
                val networkInterface = interfaces.nextElement()
                if (networkInterface.isLoopback || !networkInterface.isUp) continue
                val addresses = networkInterface.inetAddresses
                while (addresses.hasMoreElements()) {
                    val address: InetAddress = addresses.nextElement()
                    if (address is Inet4Address && !address.isLoopbackAddress) {
                        val hostAddress = address.hostAddress ?: continue
                        if (hostAddress.startsWith("192.168.") || hostAddress.startsWith("10.") || hostAddress.startsWith("172.")) {
                            return hostAddress
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.w(tag, "Error resolving Wi-Fi IP address: ${e.message}")
        }
        return "127.0.0.1"
    }
}
