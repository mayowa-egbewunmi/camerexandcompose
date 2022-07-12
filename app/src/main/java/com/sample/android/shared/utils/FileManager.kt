package com.sample.android.shared.utils

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class FileManager(private val context: Context) {

    private fun getPrivateFileDirectory(dir: String): File? {
        val directory = File(context.filesDir, dir)
        return if (directory.exists() || directory.mkdirs()) {
            directory
        } else null
    }

    suspend fun createFile(directory: String, ext: String): String {
        return withContext(Dispatchers.IO) {
            val timestamp = SimpleDateFormat(
                FILE_TIMESTAMP_FORMAT,
                Locale.getDefault()
            ).format(System.currentTimeMillis())
            return@withContext File(getPrivateFileDirectory(directory), "$timestamp.$ext").canonicalPath
        }
    }

    companion object {
        const val FILE_TIMESTAMP_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"
    }
}