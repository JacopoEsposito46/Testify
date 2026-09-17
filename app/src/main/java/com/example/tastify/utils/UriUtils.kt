package com.example.tastify.utils

fun String.isLocalUri(): Boolean {
    return startsWith("content://") || startsWith("file://")
}

fun String.isRemoteUrl(): Boolean {
    return startsWith("http://") || startsWith("https://")
}

fun String.withCacheBuster(version: Long? = null): String {
    val source = trim()
    if (source.isBlank() || source.isLocalUri()) return source

    val cacheVersion = version ?: source.hashCode().toLong()
    return if (source.contains("?")) {
        "$source&t=$cacheVersion"
    } else {
        "$source?t=$cacheVersion"
    }
}
