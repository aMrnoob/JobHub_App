package com.example.jobhub.utils

import android.content.Context
import android.content.Intent
import android.os.Environment
import android.util.Log
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

object ResumeViewerUtils {

    suspend fun downloadAndOpenResume(context: Context, url: String) {
        withContext(Dispatchers.IO) {
            val client = OkHttpClient()
            val request = Request.Builder().url(url).build()

            try {
                val response = client.newCall(request).execute()

                if (!response.isSuccessful) {
                    throw IOException("Không thể tải file: ${response.code}")
                }

                val fileName = getFileNameFromUrl(url)
                val extension = getFileExtension(fileName)

                val downloadDir = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
                val file = File(downloadDir, fileName)

                response.body?.byteStream()?.use { inputStream ->
                    FileOutputStream(file).use { outputStream ->
                        inputStream.copyTo(outputStream)
                    }
                }

                withContext(Dispatchers.Main) {
                    openFileWithProperIntent(context, file, extension)
                }
            } catch (e: Exception) {
                Log.e("ResumeViewer", "Error downloading resume: ${e.message}")
                throw e
            }
        }
    }

    private fun openFileWithProperIntent(context: Context, file: File, extension: String) {
        try {
            val fileUri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )

            val intent = Intent(Intent.ACTION_VIEW).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION

                when (extension.lowercase()) {
                    "pdf" -> setDataAndType(fileUri, "application/pdf")
                    "doc", "docx" -> setDataAndType(fileUri, "application/msword")
                    "xls", "xlsx" -> setDataAndType(fileUri, "application/vnd.ms-excel")
                    "jpg", "jpeg", "png" -> setDataAndType(fileUri, "image/*")
                    else -> setDataAndType(fileUri, "*/*")
                }
            }

            val packageManager = context.packageManager
            if (intent.resolveActivity(packageManager) != null) {
                context.startActivity(intent)
            } else {
                val chooserIntent = Intent.createChooser(intent, "Chọn ứng dụng để mở file")
                chooserIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(chooserIntent)
            }
        } catch (e: Exception) {
            Log.e("ResumeViewer", "Error opening file: ${e.message}")
            throw IOException("Không thể mở file: ${e.message}")
        }
    }

    private fun getFileNameFromUrl(url: String): String {
        val lastSlashIndex = url.lastIndexOf('/')
        return if (lastSlashIndex != -1 && lastSlashIndex < url.length - 1) {
            val fileName = url.substring(lastSlashIndex + 1)
            val queryIndex = fileName.indexOf('?')
            if (queryIndex != -1) fileName.substring(0, queryIndex) else fileName
        } else {
            "resume_${System.currentTimeMillis()}.pdf"
        }
    }

    private fun getFileExtension(fileName: String): String {
        val dotIndex = fileName.lastIndexOf('.')
        return if (dotIndex != -1 && dotIndex < fileName.length - 1) {
            fileName.substring(dotIndex + 1)
        } else {
            "pdf"
        }
    }
}