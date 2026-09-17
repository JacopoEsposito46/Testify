package com.example.tastify.report.logic

sealed interface ReportEvent {
    data class LoadReport(val recipeId: String, val currentUserId: String) : ReportEvent
    data class OnTextChange(val text: String) : ReportEvent
    data class SubmitReport(val authorId: String) : ReportEvent
    data object ClearError : ReportEvent
}