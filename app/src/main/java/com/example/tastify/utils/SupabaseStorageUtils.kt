package com.example.tastify.utils

import android.content.Context
import android.net.Uri
import android.util.Log
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

suspend fun uploadImageToSupabaseStorage(
    context: Context,
    supabase: SupabaseClient,
    bucket: String,
    imageUri: String,
    storagePath: String,
    logTag: String
): String {
    if (!imageUri.isLocalUri()) return imageUri.toSupabaseStoragePath(bucket)

    return try {
        withContext(Dispatchers.IO) {
            val uri = Uri.parse(imageUri)
            val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                ?: throw IllegalStateException("Unable to read image uri: $imageUri")

            supabase.storage.from(bucket).upload(storagePath, bytes, upsert = true)
            storagePath
        }
    } catch (e: Exception) {
        Log.e(logTag, "Image upload failed for bucket=$bucket path=$storagePath uri=$imageUri", e)
        throw e
    }
}

fun String.toSupabasePublicUrl(
    supabase: SupabaseClient,
    bucket: String
): String {
    val source = trim()
    if (source.isBlank() || source.isRemoteUrl() || source.isLocalUri()) {
        return source
    }

    val cleanBucket = bucket.trim().trim('/')
    if (cleanBucket.isBlank()) return source

    val path = source.toBucketRelativePath(cleanBucket)
    if (path.isBlank()) return source

    return supabase.storage.from(cleanBucket).publicUrl(path)
}

fun String.toSupabaseStoragePath(bucket: String): String {
    val source = trim()
    val cleanBucket = bucket.trim().trim('/')

    if (source.isBlank() || source.isLocalUri() || cleanBucket.isBlank()) return source

    val publicUrlMarker = "/storage/v1/object/public/$cleanBucket/"
    if (source.contains(publicUrlMarker)) {
        return source.substringAfter(publicUrlMarker).substringBefore("?")
    }

    if (source.isRemoteUrl()) return source

    return source.toBucketRelativePath(cleanBucket)
}

private fun String.toBucketRelativePath(bucket: String): String {
    var path = removePrefix("/")
    while (path.startsWith("$bucket/")) {
        path = path.removePrefix("$bucket/")
    }
    return path
}
