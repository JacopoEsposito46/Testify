package com.example.tastify.profile.logic

sealed interface ProfileEvent {
    data object Refresh : ProfileEvent
}