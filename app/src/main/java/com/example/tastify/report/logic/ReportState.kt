package com.example.tastify.report.logic

import com.example.tastify.models.Report

data class ReportState (
    val recipeId: String = "",
    val reports: List<Report> = emptyList(),
    val userReport: Report? = null,
    val isLoading: Boolean = false,

    val draftReportText: String = "",
    val reportError: String? = null,
    val submitSuccess: Boolean = false,
)