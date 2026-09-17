package com.example.tastify.utils

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import java.io.File
import java.util.UUID

fun Bitmap.toTempImageUri(
    context: Context,
    fileName: String? = null,
    prefix: String = "image",
    quality: Int = 100
): Uri {
    val targetFileName = fileName ?: "${prefix}_${UUID.randomUUID()}.jpg"
    val tempFile = File(context.cacheDir, targetFileName)

    tempFile.outputStream().use { out ->
        compress(Bitmap.CompressFormat.JPEG, quality, out)
    }

    return Uri.fromFile(tempFile)
}
