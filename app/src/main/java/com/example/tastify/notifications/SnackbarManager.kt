package com.example.tastify.notifications

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SnackbarManager @Inject constructor() {

    private val _messages = MutableSharedFlow<SnackbarMessage>(extraBufferCapacity = 1)
    val messages: SharedFlow<SnackbarMessage> = _messages.asSharedFlow()

    fun showMessage(
        text: String,
        actionLabel: String? = null,
        onAction: (() -> Unit)? = null
    ) {
        _messages.tryEmit(SnackbarMessage(text, actionLabel, onAction))
    }
}

data class SnackbarMessage(
    val text: String,
    val actionLabel: String? = null,
    val onAction: (() -> Unit)? = null
)

