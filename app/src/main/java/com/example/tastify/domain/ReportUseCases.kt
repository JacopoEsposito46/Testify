package com.example.tastify.domain

import com.example.tastify.data.ReportRepository
import com.example.tastify.models.Report
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetReportsUseCase @Inject constructor(private val repository: ReportRepository) {
    operator fun invoke(recipeId: String): Flow<List<Report>> = repository.getReportsForRecipe(recipeId)
}

class AddReportUseCase @Inject constructor(private val repository: ReportRepository) {
    suspend operator fun invoke(report: Report) = repository.addReport(report)
}