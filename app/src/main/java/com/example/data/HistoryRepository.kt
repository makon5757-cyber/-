package com.example.data

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.core.content.FileProvider
import com.example.model.HistoryItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

class HistoryRepository(private val context: Context) {

    private val prefs = context.getSharedPreferences("tajsend_history_prefs", Context.MODE_PRIVATE)
    private val _history = MutableStateFlow<List<HistoryItem>>(emptyList())
    val history = _history.asStateFlow()

    init {
        loadHistory()
    }

    private fun loadHistory() {
        val raw = prefs.getString("items_json", null)
        if (raw.isNullOrEmpty()) {
            // Initial sample items to show in history
            val initial = listOf(
                HistoryItem(
                    fileName = "TajSend_Руководство_KolidCompany.pdf",
                    fileSize = 1_842_110L,
                    isSent = false,
                    timestamp = System.currentTimeMillis() - 1800000L,
                    peerName = "Kolid Server"
                ),
                HistoryItem(
                    fileName = "TajSend_Wallpaper_4K.jpg",
                    fileSize = 4_920_500L,
                    isSent = true,
                    timestamp = System.currentTimeMillis() - 5400000L,
                    peerName = "Samsung S24"
                )
            )
            _history.value = initial
            saveHistory(initial)
            return
        }

        try {
            val jsonArray = JSONArray(raw)
            val list = mutableListOf<HistoryItem>()
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                list.add(
                    HistoryItem(
                        id = obj.optString("id"),
                        fileName = obj.optString("fileName"),
                        fileSize = obj.optLong("fileSize"),
                        isSent = obj.optBoolean("isSent"),
                        timestamp = obj.optLong("timestamp"),
                        filePath = obj.optString("filePath").takeIf { it.isNotEmpty() },
                        uriString = obj.optString("uriString").takeIf { it.isNotEmpty() },
                        mimeType = obj.optString("mimeType", "*/*"),
                        peerName = obj.optString("peerName", "Устройство")
                    )
                )
            }
            _history.value = list
        } catch (_: Exception) {
            _history.value = emptyList()
        }
    }

    @Synchronized
    fun addHistoryItem(item: HistoryItem) {
        val updated = listOf(item) + _history.value.filter { it.id != item.id }
        _history.value = updated
        saveHistory(updated)

        // Update cumulative stats
        val totalSent = prefs.getLong("stat_total_sent", 0L)
        val totalRecv = prefs.getLong("stat_total_recv", 0L)
        val count = prefs.getInt("stat_total_count", 0)

        prefs.edit().apply {
            if (item.isSent) {
                putLong("stat_total_sent", totalSent + item.fileSize)
            } else {
                putLong("stat_total_recv", totalRecv + item.fileSize)
            }
            putInt("stat_total_count", count + 1)
            apply()
        }
    }

    fun clearHistory() {
        _history.value = emptyList()
        prefs.edit().remove("items_json").apply()
    }

    fun getTotalSentBytes(): Long = prefs.getLong("stat_total_sent", 125_000_000L)
    fun getTotalReceivedBytes(): Long = prefs.getLong("stat_total_recv", 380_000_000L)
    fun getTotalTransfersCount(): Int = prefs.getInt("stat_total_count", 18)

    private fun saveHistory(list: List<HistoryItem>) {
        try {
            val jsonArray = JSONArray()
            for (item in list.take(100)) {
                val obj = JSONObject().apply {
                    put("id", item.id)
                    put("fileName", item.fileName)
                    put("fileSize", item.fileSize)
                    put("isSent", item.isSent)
                    put("timestamp", item.timestamp)
                    put("filePath", item.filePath ?: "")
                    put("uriString", item.uriString ?: "")
                    put("mimeType", item.mimeType)
                    put("peerName", item.peerName)
                }
                jsonArray.put(obj)
            }
            prefs.edit().putString("items_json", jsonArray.toString()).apply()
        } catch (_: Exception) {}
    }

    fun openFile(item: HistoryItem) {
        try {
            val file = item.filePath?.let { File(it) }
            val uri = when {
                file != null && file.exists() -> {
                    FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
                }
                !item.uriString.isNullOrEmpty() -> {
                    Uri.parse(item.uriString)
                }
                else -> null
            }

            if (uri == null) {
                Toast.makeText(context, "Файл не найден на устройстве", Toast.LENGTH_SHORT).show()
                return
            }

            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, item.mimeType)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(Intent.createChooser(intent, "Открыть файл"))
        } catch (e: Exception) {
            Toast.makeText(context, "Не удалось открыть файл: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
        }
    }

    fun shareFile(item: HistoryItem) {
        try {
            val file = item.filePath?.let { File(it) }
            val uri = when {
                file != null && file.exists() -> {
                    FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
                }
                !item.uriString.isNullOrEmpty() -> {
                    Uri.parse(item.uriString)
                }
                else -> null
            }

            if (uri == null) {
                Toast.makeText(context, "Файл не найден", Toast.LENGTH_SHORT).show()
                return
            }

            val intent = Intent(Intent.ACTION_SEND).apply {
                type = item.mimeType
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(Intent.createChooser(intent, "Поделиться файлом"))
        } catch (e: Exception) {
            Toast.makeText(context, "Ошибка: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
        }
    }
}
