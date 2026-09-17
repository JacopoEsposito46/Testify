package com.example.tastify.profile.analytics.logic

sealed class ProfileAnalyticsEvent {
    data class CalculateAnalytics(val userId: String) : ProfileAnalyticsEvent()
}