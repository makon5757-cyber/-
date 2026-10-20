package com.example.network

import android.content.Context
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Environment
import com.example.model.HistoryItem
import com.example.model.TransferProgress
import com.example.model.TransferStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.BufferedReader
import java.io.File
import java.io.FileOutputStream
import java.io.InputStreamReader
import java.net.ServerSocket
import java.net.Socket
import java.net.URLDecoder

class TajSendServer(
    private val context: Context,
    private val port: Int = 8888,
    private val expectedPin: String,
    private val deviceName: String,
    private val onFileReceived: (HistoryItem) -> Unit
) {
    private var serverSocket: ServerSocket? = null
    private var serverJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.IO)

    private val _progress = MutableStateFlow(TransferProgress(status = TransferStatus.WAITING_CONNECTION))
    val progress = _progress.asStateFlow()

    private var isRunning = false

    fun start() {
        if (isRunning) return
        isRunning = true

        serverJob = scope.launch {
            try {
                serverSocket = ServerSocket(port).apply {
                    reuseAddress = true
                }
                _progress.value = TransferProgress(status = TransferStatus.WAITING_CONNECTION)

                while (isActive && isRunning) {
                    val clientSocket = try {
                        serverSocket?.accept() ?: break
                    } catch (e: Exception) {
                        break
                    }
                    launch {
                        handleClient(clientSocket)
                    }
                }
            } catch (e: Exception) {
                if (isRunning) {
                    _progress.value = TransferProgress(
                        status = TransferStatus.ERROR,
                        errorMessage = "Ошибка сервера: ${e.localizedMessage ?: "Не удалось запустить порт $port"}"
                    )
                }
            }
        }
    }

    private suspend fun handleClient(socket: Socket) {
        withContext(Dispatchers.IO) {
            try {
                val input = BufferedInputStream(socket.getInputStream())
                val output = BufferedOutputStream(socket.getOutputStream())
                val reader = BufferedReader(InputStreamReader(input, Charsets.UTF_8))

                val requestLine = reader.readLine() ?: run {
                    socket.close()
                    return@withContext
                }

                val headers = mutableMapOf<String, String>()
                var headerLine = reader.readLine()
                while (!headerLine.isNullOrEmpty()) {
                    val idx = headerLine.indexOf(':')
                    if (idx != -1) {
                        val key = headerLine.substring(0, idx).trim().lowercase()
                        val value = headerLine.substring(idx + 1).trim()
                        headers[key] = value
                    }
                    headerLine = reader.readLine()
                }

                if (requestLine.startsWith("GET /api/info")) {
                    val json = """{"status":"ready","device":"$deviceName","pin":"$expectedPin"}"""
                    val response = "HTTP/1.1 200 OK\r\nContent-Type: application/json\r\nContent-Length: ${json.toByteArray().size}\r\nConnection: close\r\n\r\n$json"
                    output.write(response.toByteArray())
                    output.flush()
                    socket.close()
                    return@withContext
                }

                if (requestLine.startsWith("POST /api/upload")) {
                    val pin = headers["x-pin-token"]
                    val encodedName = headers["x-file-name"] ?: "received_file_${System.currentTimeMillis()}"
                    val rawFileName = try {
                        URLDecoder.decode(encodedName, "UTF-8")
                    } catch (_: Exception) {
                        encodedName
                    }
                    val fileName = File(rawFileName).name.ifBlank { "received_${System.currentTimeMillis()}" }
                    val fileSize = headers["x-file-size"]?.toLongOrNull() ?: headers["content-length"]?.toLongOrNull() ?: 0L
                    val mimeType = headers["x-file-mime"] ?: "application/octet-stream"
                    val senderName = headers["x-sender-device"] ?: "Отправитель"

                    // Security check: optional PIN verification
                    if (!expectedPin.isBlank() && pin != null && pin != expectedPin) {
                        val err = """{"error":"Invalid PIN"}"""
                        val res = "HTTP/1.1 403 Forbidden\r\nContent-Type: application/json\r\n\r\n$err"
                        output.write(res.toByteArray())
                        output.flush()
                        socket.close()
                        return@withContext
                    }

                    // Prepare download file directory
                    val downloadDir = getDownloadDirectory()
                    if (!downloadDir.exists()) downloadDir.mkdirs()

                    var targetFile = File(downloadDir, fileName)
                    if (targetFile.exists()) {
                        val base = targetFile.nameWithoutExtension
                        val ext = targetFile.extension.let { if (it.isNotEmpty()) ".$it" else "" }
                        targetFile = File(downloadDir, "${base}_${System.currentTimeMillis()}$ext")
                    }

                    _progress.value = TransferProgress(
                        currentFileName = fileName,
                        currentFileTotalBytes = fileSize,
                        totalBatchBytes = fileSize,
                        status = TransferStatus.TRANSFERRING
                    )

                    val buffer = ByteArray(64 * 1024)
                    var bytesRead: Int
                    var totalReceived = 0L
                    var lastTime = System.currentTimeMillis()
                    var bytesSinceLastTime = 0L
                    var currentSpeed = 0L

                    val fileOut = FileOutputStream(targetFile)
                    try {
                        while (totalReceived < fileSize || fileSize <= 0L) {
                            val toRead = if (fileSize > 0) {
                                val remaining = fileSize - totalReceived
                                if (remaining <= 0) break
                                minOf(buffer.size.toLong(), remaining).toInt()
                            } else {
                                buffer.size
                            }
                            bytesRead = input.read(buffer, 0, toRead)
                            if (bytesRead == -1) break

                            fileOut.write(buffer, 0, bytesRead)
                            totalReceived += bytesRead
                            bytesSinceLastTime += bytesRead

                            val now = System.currentTimeMillis()
                            val elapsed = now - lastTime
                            if (elapsed >= 300) {
                                currentSpeed = (bytesSinceLastTime * 1000L) / elapsed
                                lastTime = now
                                bytesSinceLastTime = 0L

                                _progress.value = _progress.value.copy(
                                    currentFileBytes = totalReceived,
                                    totalTransferredBytes = totalReceived,
                                    currentSpeedBytesPerSec = currentSpeed
                                )
                            }
                        }
                        fileOut.flush()
                    } finally {
                        fileOut.close()
                    }

                    // Scan file into Android media provider
                    MediaScannerConnection.scanFile(
                        context,
                        arrayOf(targetFile.absolutePath),
                        arrayOf(mimeType),
                        null
                    )

                    val historyItem = HistoryItem(
                        fileName = targetFile.name,
                        fileSize = targetFile.length(),
                        isSent = false,
                        filePath = targetFile.absolutePath,
                        mimeType = mimeType,
                        peerName = senderName
                    )
                    onFileReceived(historyItem)

                    _progress.value = _progress.value.copy(
                        currentFileBytes = totalReceived,
                        totalTransferredBytes = totalReceived,
                        currentSpeedBytesPerSec = 0L,
                        status = TransferStatus.COMPLETED
                    )

                    val successJson = """{"status":"ok","receivedBytes":$totalReceived}"""
                    val res = "HTTP/1.1 200 OK\r\nContent-Type: application/json\r\nContent-Length: ${successJson.toByteArray().size}\r\nConnection: close\r\n\r\n$successJson"
                    output.write(res.toByteArray())
                    output.flush()
                    socket.close()
                }
            } catch (e: Exception) {
                _progress.value = _progress.value.copy(
                    status = TransferStatus.ERROR,
                    errorMessage = e.localizedMessage ?: "Сбой соединения при передаче"
                )
                try { socket.close() } catch (_: Exception) {}
            }
        }
    }

    private fun getDownloadDirectory(): File {
        // Preferred: Public Downloads/TajSend
        val publicDir = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "TajSend")
        if (publicDir.exists() || publicDir.mkdirs()) {
            return publicDir
        }
        // Fallback: app external files dir
        val appDir = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
        if (appDir != null && (appDir.exists() || appDir.mkdirs())) {
            return appDir
        }
        return context.filesDir
    }

    fun stop() {
        isRunning = false
        try {
            serverSocket?.close()
        } catch (_: Exception) {}
        serverJob?.cancel()
        _progress.value = TransferProgress(status = TransferStatus.IDLE)
    }
}
