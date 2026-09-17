package com.example.tastify.recipe.social.domain

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast

fun Context.openSocialApp(uriString: String, webUrl: String) {
    Toast.makeText(this, "Share your recipe on Tastify!", Toast.LENGTH_LONG).show()
    try {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uriString)).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        startActivity(intent)
    } catch (e: ActivityNotFoundException) {
        val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse(webUrl)).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        startActivity(webIntent)
    }
}
