package com.example.tastify.data

import com.example.tastify.models.Report
import kotlinx.coroutines.flow.Flow

interface ReportRepository {
    fun getReportsForRecipe(recipeId: String): Flow<List<Report>>
    suspend fun addReport(report: Report)
}