package com.example.tastify.report.logic

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tastify.domain.AddReportUseCase
import com.example.tastify.domain.GetRecipeByIdUseCase
import com.example.tastify.domain.GetReportsUseCase
import com.example.tastify.models.Recipe
import com.example.tastify.models.Report
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ReportViewModel @Inject constructor(
    private val addReportUseCase: AddReportUseCase,
    private val getRecipeByIdUseCase: GetRecipeByIdUseCase,
    private val getReportsUseCase: GetReportsUseCase
) : ViewModel() {
    private val _state = MutableStateFlow(ReportState())
    val state: StateFlow<ReportState> = _state.asStateFlow()
    private val _recipe = MutableStateFlow<Recipe?>(null)
    val recipe: StateFlow<Recipe?> = _recipe.asStateFlow()

    private var reportsJob: Job? = null

    fun loadRecipe(id: String){
        viewModelScope.launch {
            getRecipeByIdUseCase(id).collect { recipe ->
                _recipe.value = recipe
            }
        }
    }

    fun onEvent(event: ReportEvent) {
        when (event) {
            is ReportEvent.LoadReport -> {
                observeUserReport(event.recipeId, event.currentUserId)
            }
            is ReportEvent.OnTextChange -> {
                _state.update { it.copy(draftReportText = event.text) }
            }
            is ReportEvent.SubmitReport -> {
                submitReport(event.authorId)
            }
            is ReportEvent.ClearError -> {
                _state.update { it.copy(reportError = null) }
            }
        }
    }

    private fun observeUserReport(recipeId: String, currentUserId: String) {
        _state.update { it.copy(recipeId = recipeId, isLoading = true)}
        reportsJob?.cancel()
        reportsJob = viewModelScope.launch {

        combine(
            getRecipeByIdUseCase(recipeId),
            getReportsUseCase(recipeId)
        ) { recipeObj, reportsList ->
            recipeObj to reportsList
        }.collect{ (recipeObj, reportsList) ->

            _recipe.value = recipeObj
            val myReport = reportsList.find { it.authorId == currentUserId }
            val filteredReports = if (recipeObj?.authorId == currentUserId) {
                reportsList
            } else {
                if (myReport != null) listOf(myReport) else emptyList()
            }

            _state.update {
                it.copy(
                    reports = filteredReports,
                    userReport = myReport
                )
            }
        }
    }
}

    private fun submitReport(currentUserId: String) {
        val reportText = _state.value.draftReportText.trim()
        val recipeId = _state.value.recipeId

        if (reportText.isEmpty()) {
            _state.update { it.copy(reportError = "The report test cannot be blank") }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }

            val newReport = Report(
                recipeId = recipeId,
                authorId = currentUserId,
                text = reportText,
                timestamp = System.currentTimeMillis()
            )

            try {
                addReportUseCase(newReport)
                _state.update {
                    it.copy(
                        isLoading = false,
                        submitSuccess = true,
                        draftReportText = ""
                    )
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isLoading = false,
                        reportError = e.message ?: "Error submitting report"
                    )
                }
            }
        }
    }
}