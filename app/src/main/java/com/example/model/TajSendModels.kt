package com.example.model

import android.net.Uri

enum class FileCategory(val title: String) {
    ALL("Все"),
    IMAGES("Фото"),
    VIDEOS("Видео"),
    AUDIO("Музыка"),
    DOCUMENTS("Документы"),
    APPS("Файлы")
}

data class LocalFileItem(
    val id: String,
    val name: String,
    val size: Long,
    val mimeType: String,
    val uri: Uri,
    val category: FileCategory,
    val dateModified: Long = System.currentTimeMillis(),
    val isSelected: Boolean = false
) {
    val formattedSize: String
        get() = formatFileSize(size)
}

enum class TransferDirection {
    SEND,
    RECEIVE
}

enum class TransferStatus {
    IDLE,
    WAITING_CONNECTION,
    CONNECTING,
    TRANSFERRING,
    COMPLETED,
    ERROR,
    CANCELLED
}

data class TransferProgress(
    val currentFileName: String = "",
    val currentFileIndex: Int = 0,
    val totalFilesCount: Int = 0,
    val currentFileBytes: Long = 0L,
    val currentFileTotalBytes: Long = 0L,
    val totalTransferredBytes: Long = 0L,
    val totalBatchBytes: Long = 0L,
    val currentSpeedBytesPerSec: Long = 0L,
    val status: TransferStatus = TransferStatus.IDLE,
    val errorMessage: String? = null
) {
    val overallPercent: Float
        get() = if (totalBatchBytes > 0) {
            (totalTransferredBytes.toFloat() / totalBatchBytes.toFloat()).coerceIn(0f, 1f)
        } else 0f

    val formattedSpeed: String
        get() = "${String.format("%.1f", currentSpeedBytesPerSec / (1024f * 1024f))} МБ/с"

    val formattedTransferred: String
        get() = "${formatFileSize(totalTransferredBytes)} / ${formatFileSize(totalBatchBytes)}"
}

data class ConnectionPayload(
    val ip: String,
    val port: Int,
    val deviceName: String,
    val pinToken: String,
    val protocol: String = "tajsend"
) {
    fun toQrString(): String {
        return "tajsend://$ip:$port?pin=$pinToken&dev=${Uri.encode(deviceName)}"
    }

    companion object {
        fun fromQrString(raw: String): ConnectionPayload? {
            return try {
                if (!raw.startsWith("tajsend://")) {
                    // Try parsing plain ip:port or ip:port:pin
                    val parts = raw.trim().split(":")
                    if (parts.size >= 2) {
                        val ip = parts[0]
                        val port = parts[1].substringBefore("/").toIntOrNull() ?: 8888
                        val pin = if (parts.size >= 3) parts[2] else "123456"
                        return ConnectionPayload(ip = ip, port = port, deviceName = "TajSend Peer", pinToken = pin)
                    }
                    return null
                }
                val uri = Uri.parse(raw)
                val ip = uri.host ?: return null
                val port = if (uri.port != -1) uri.port else 8888
                val pin = uri.getQueryParameter("pin") ?: "123456"
                val dev = uri.getQueryParameter("dev") ?: "TajSend Device"
                ConnectionPayload(ip = ip, port = port, deviceName = dev, pinToken = pin)
            } catch (e: Exception) {
                null
            }
        }
    }
}

data class HistoryItem(
    val id: String = java.util.UUID.randomUUID().toString(),
    val fileName: String,
    val fileSize: Long,
    val isSent: Boolean,
    val timestamp: Long = System.currentTimeMillis(),
    val filePath: String? = null,
    val uriString: String? = null,
    val mimeType: String = "*/*",
    val peerName: String = "Устройство"
) {
    val formattedSize: String
        get() = formatFileSize(fileSize)
}

fun formatFileSize(bytes: Long): String {
    if (bytes <= 0) return "0 Б"
    val units = arrayOf("Б", "КБ", "МБ", "ГБ", "ТБ")
    val digitGroups = (Math.log10(bytes.toDouble()) / Math.log10(1024.0)).toInt().coerceIn(0, units.size - 1)
    val value = bytes / Math.pow(1024.0, digitGroups.toDouble())
    return String.format("%.1f %s", value, units[digitGroups])
}
