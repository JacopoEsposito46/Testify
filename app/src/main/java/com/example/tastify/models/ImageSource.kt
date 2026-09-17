package com.example.tastify.models

import androidx.annotation.DrawableRes

sealed interface ImageSource {
    data class Resource(@DrawableRes val resId: Int) : ImageSource
    data class Uri(val uriString: String) : ImageSource
}