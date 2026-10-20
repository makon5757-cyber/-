package com.example.network

import android.content.Context
import com.example.model.HistoryItem
import com.example.model.LocalFileItem
import com.example.model.TransferProgress
import com.example.model.TransferStatus
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import java.io.BufferedOutputStream
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

class TajSendClient(
    private val context: Context,
    private val onFileSent: (HistoryItem) -> Unit
) {
    private val _progress = MutableStateFlow(TransferProgress(status = TransferStatus.IDLE))
    val progress = _progress.asStateFlow()

    @Volatile
    private var isCancelled = false

    suspend fun sendFiles(
        targetIp: String,
        targetPort: Int,
        pin: String,
        files: List<LocalFileItem>,
        senderDeviceName: String
    ): Boolean = withContext(Dispatchers.IO) {
        if (files.isEmpty()) return@withContext true
        isCancelled = false

        val totalBatchBytes = files.sumOf { it.size }
        var totalSentAllFiles = 0L

        _progress.value = TransferProgress(
            status = TransferStatus.CONNECTING,
            totalFilesCount = files.size,
            totalBatchBytes = totalBatchBytes
        )

        for ((index, fileItem) in files.withIndex()) {
            if (isCancelled) {
                _progress.value = _progress.value.copy(status = TransferStatus.CANCELLED)
                return@withContext false
            }

            _progress.value = _progress.value.copy(
                status = TransferStatus.TRANSFERRING,
                currentFileName = fileItem.name,
                currentFileIndex = index + 1,
                currentFileTotalBytes = fileItem.size,
                currentFileBytes = 0L,
                errorMessage = null
            )

            var connection: HttpURLConnection? = null
            var inputStream: InputStream? = null
            var outputStream: BufferedOutputStream? = null

            try {
                val url = URL("http://$targetIp:$targetPort/api/upload")
                connection = (url.openConnection() as HttpURLConnection).apply {
                    requestMethod = "POST"
                    doOutput = true
                    doInput = true
                    connectTimeout = 8000
                    readTimeout = 60000
                    useCaches = false
                    setChunkedStreamingMode(64 * 1024)

                    val encodedName = URLEncoder.encode(fileItem.name, "UTF-8")
                    setRequestProperty("X-File-Name", encodedName)
                    setRequestProperty("X-File-Size", fileItem.size.toString())
                    setRequestProperty("X-File-Mime", fileItem.mimeType)
                    setRequestProperty("X-Pin-Token", pin)
                    setRequestProperty("X-Sender-Device", URLEncoder.encode(senderDeviceName, "UTF-8"))
                    setRequestProperty("Content-Type", "application/octet-stream")
                }

                inputStream = context.contentResolver.openInputStream(fileItem.uri)
                    ?: throw IllegalStateException("Не удалось открыть файл: ${fileItem.name}")

                outputStream = BufferedOutputStream(connection.outputStream, 64 * 1024)

                val buffer = ByteArray(64 * 1024)
                var bytesRead: Int
                var fileBytesSent = 0L
                var lastTime = System.currentTimeMillis()
                var bytesSinceLast = 0L

                while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                    if (isCancelled) throw CancellationException("Передача отменена пользователем")
                    outputStream.write(buffer, 0, bytesRead)
                    fileBytesSent += bytesRead
                    totalSentAllFiles += bytesRead
                    bytesSinceLast += bytesRead

                    val now = System.currentTimeMillis()
                    val diff = now - lastTime
                    if (diff >= 250) {
                        val speed = (bytesSinceLast * 1000L) / diff
                        lastTime = now
                        bytesSinceLast = 0L

                        _progress.value = _progress.value.copy(
                            currentFileBytes = fileBytesSent,
                            totalTransferredBytes = totalSentAllFiles,
                            currentSpeedBytesPerSec = speed
                        )
                    }
                }
                outputStream.flush()

                val responseCode = connection.responseCode
                if (responseCode != HttpURLConnection.HTTP_OK) {
                    throw RuntimeException("Сервер вернул ошибку HTTP $responseCode")
                }

                // File completed successfully
                val history = HistoryItem(
                    fileName = fileItem.name,
                    fileSize = fileItem.size,
                    isSent = true,
                    uriString = fileItem.uri.toString(),
                    mimeType = fileItem.mimeType,
                    peerName = "$targetIp:$targetPort"
                )
                onFileSent(history)

            } catch (e: Exception) {
                if (isCancelled) {
                    _progress.value = _progress.value.copy(status = TransferStatus.CANCELLED)
                    return@withContext false
                }
                _progress.value = _progress.value.copy(
                    status = TransferStatus.ERROR,
                    errorMessage = "Ошибка при передаче ${fileItem.name}: ${e.localizedMessage ?: "Сбой сети"}"
                )
                return@withContext false
            } finally {
                try { inputStream?.close() } catch (_: Exception) {}
                try { outputStream?.close() } catch (_: Exception) {}
                try { connection?.disconnect() } catch (_: Exception) {}
            }
        }

        _progress.value = _progress.value.copy(
            status = TransferStatus.COMPLETED,
            totalTransferredBytes = totalBatchBytes,
            currentSpeedBytesPerSec = 0L
        )
        return@withContext true
    }

    fun cancel() {
        isCancelled = true
        _progress.value = _progress.value.copy(status = TransferStatus.CANCELLED)
    }

    fun reset() {
        isCancelled = false
        _progress.value = TransferProgress(status = TransferStatus.IDLE)
    }
}
