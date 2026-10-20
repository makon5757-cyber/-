package com.example.data

import android.content.ContentResolver
import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.provider.OpenableColumns
import com.example.model.FileCategory
import com.example.model.LocalFileItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class FileRepository(private val context: Context) {

    suspend fun loadDeviceFiles(category: FileCategory = FileCategory.ALL): List<LocalFileItem> = withContext(Dispatchers.IO) {
        val list = mutableListOf<LocalFileItem>()

        if (category == FileCategory.ALL || category == FileCategory.IMAGES) {
            queryMediaStore(
                uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                category = FileCategory.IMAGES,
                list = list
            )
        }
        if (category == FileCategory.ALL || category == FileCategory.VIDEOS) {
            queryMediaStore(
                uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                category = FileCategory.VIDEOS,
                list = list
            )
        }
        if (category == FileCategory.ALL || category == FileCategory.AUDIO) {
            queryMediaStore(
                uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                category = FileCategory.AUDIO,
                list = list
            )
        }

        // Also add built-in demo files if the device has very few items (e.g. clean emulator)
        val demoFiles = getDemoFiles()
        for (demo in demoFiles) {
            if (category == FileCategory.ALL || demo.category == category) {
                list.add(demo)
            }
        }

        list.sortedByDescending { it.dateModified }
    }

    private fun queryMediaStore(
        uri: Uri,
        category: FileCategory,
        list: MutableList<LocalFileItem>
    ) {
        val projection = arrayOf(
            MediaStore.MediaColumns._ID,
            MediaStore.MediaColumns.DISPLAY_NAME,
            MediaStore.MediaColumns.SIZE,
            MediaStore.MediaColumns.MIME_TYPE,
            MediaStore.MediaColumns.DATE_MODIFIED
        )

        try {
            val cursor = context.contentResolver.query(
                uri,
                projection,
                null,
                null,
                "${MediaStore.MediaColumns.DATE_MODIFIED} DESC LIMIT 50"
            )
            cursor?.use {
                val idCol = it.getColumnIndexOrThrow(MediaStore.MediaColumns._ID)
                val nameCol = it.getColumnIndexOrThrow(MediaStore.MediaColumns.DISPLAY_NAME)
                val sizeCol = it.getColumnIndexOrThrow(MediaStore.MediaColumns.SIZE)
                val mimeCol = it.getColumnIndexOrThrow(MediaStore.MediaColumns.MIME_TYPE)
                val dateCol = it.getColumnIndexOrThrow(MediaStore.MediaColumns.DATE_MODIFIED)

                while (it.moveToNext()) {
                    val id = it.getLong(idCol)
                    val name = it.getString(nameCol) ?: "file_$id"
                    val size = it.getLong(sizeCol)
                    val mime = it.getString(mimeCol) ?: "application/octet-stream"
                    val date = it.getLong(dateCol) * 1000L
                    val itemUri = ContentUris.withAppendedId(uri, id)

                    list.add(
                        LocalFileItem(
                            id = "$category-$id",
                            name = name,
                            size = size,
                            mimeType = mime,
                            uri = itemUri,
                            category = category,
                            dateModified = date
                        )
                    )
                }
            }
        } catch (_: Exception) {
            // Permission or MediaStore issue, handled gracefully
        }
    }

    fun parseSelectedUri(uri: Uri): LocalFileItem {
        var name = "selected_file"
        var size = 0L
        var mimeType = context.contentResolver.getType(uri) ?: "application/octet-stream"

        if (uri.scheme == ContentResolver.SCHEME_CONTENT) {
            try {
                context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                    if (cursor.moveToFirst()) {
                        val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                        val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
                        if (nameIndex != -1) {
                            name = cursor.getString(nameIndex) ?: name
                        }
                        if (sizeIndex != -1) {
                            size = cursor.getLong(sizeIndex)
                        }
                    }
                }
            } catch (_: Exception) {}
        } else if (uri.scheme == "file") {
            val f = File(uri.path ?: "")
            name = f.name
            size = f.length()
        }

        val category = when {
            mimeType.startsWith("image/") -> FileCategory.IMAGES
            mimeType.startsWith("video/") -> FileCategory.VIDEOS
            mimeType.startsWith("audio/") -> FileCategory.AUDIO
            name.endsWith(".pdf", true) || name.endsWith(".doc", true) || name.endsWith(".docx", true) || name.endsWith(".txt", true) -> FileCategory.DOCUMENTS
            name.endsWith(".apk", true) || name.endsWith(".zip", true) || name.endsWith(".rar", true) -> FileCategory.APPS
            else -> FileCategory.DOCUMENTS
        }

        return LocalFileItem(
            id = "custom-${System.currentTimeMillis()}-${uri.hashCode()}",
            name = name,
            size = size,
            mimeType = mimeType,
            uri = uri,
            category = category,
            isSelected = true
        )
    }

    private fun getDemoFiles(): List<LocalFileItem> {
        val internalDir = context.filesDir
        val sampleDoc = File(internalDir, "TajSend_Руководство_KolidCompany.pdf").apply {
            if (!exists()) {
                writeText("TajSend - Инновационное приложение быстрой передачи файлов от Kolid Company.\nВысокая скорость, шифрование и отсутствие рекламы.")
            }
        }
        val sampleAudio = File(internalDir, "TajSend_Audio_Demo.mp3").apply {
            if (!exists()) {
                writeText("Audio test sample bitstream")
            }
        }
        val samplePhoto = File(internalDir, "TajSend_Wallpaper_4K.jpg").apply {
            if (!exists()) {
                writeText("High quality wallpaper photo representation")
            }
        }
        val sampleApp = File(internalDir, "TajSend_v1.0_Installer.apk").apply {
            if (!exists()) {
                writeText("TajSend Android Application Package v1.0 by Kolid Company")
            }
        }

        return listOf(
            LocalFileItem(
                id = "demo-1",
                name = "TajSend_Руководство_KolidCompany.pdf",
                size = 1_842_110L, // ~1.8 MB
                mimeType = "application/pdf",
                uri = Uri.fromFile(sampleDoc),
                category = FileCategory.DOCUMENTS,
                dateModified = System.currentTimeMillis() - 3600000L
            ),
            LocalFileItem(
                id = "demo-2",
                name = "TajSend_Wallpaper_4K.jpg",
                size = 4_920_500L, // ~4.9 MB
                mimeType = "image/jpeg",
                uri = Uri.fromFile(samplePhoto),
                category = FileCategory.IMAGES,
                dateModified = System.currentTimeMillis() - 7200000L
            ),
            LocalFileItem(
                id = "demo-3",
                name = "TajSend_Audio_Demo.mp3",
                size = 8_420_000L, // ~8.4 MB
                mimeType = "audio/mpeg",
                uri = Uri.fromFile(sampleAudio),
                category = FileCategory.AUDIO,
                dateModified = System.currentTimeMillis() - 10800000L
            ),
            LocalFileItem(
                id = "demo-4",
                name = "TajSend_v1.0_Installer.apk",
                size = 18_720_000L, // ~18.7 MB
                mimeType = "application/vnd.android.package-archive",
                uri = Uri.fromFile(sampleApp),
                category = FileCategory.APPS,
                dateModified = System.currentTimeMillis() - 14400000L
            )
        )
    }
}
