package com.example.tastify.report

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.tastify.components.SharedTopBar
import com.example.tastify.report.components.ReportSummaryCard
import com.example.tastify.report.components.ReportTextField
import com.example.tastify.report.logic.ReportEvent
import com.example.tastify.report.logic.ReportViewModel
import com.example.tastify.utils.SessionManager

@Composable
fun ReportScreen(
    viewModel: ReportViewModel,
    onBack: () -> Unit,
){

    val state by viewModel.state.collectAsStateWithLifecycle()
    val recipe by viewModel.recipe.collectAsStateWithLifecycle()
    val currentUserId = SessionManager.CURRENT_LOGGED_IN_USER_ID ?: ""
    val isOwner = recipe?.authorId == currentUserId

    Scaffold(
        topBar = {
            SharedTopBar(
                title = "Report",
                onBack = onBack,
                actions = {}
            )
        },
        bottomBar = {
            if (!isOwner && state.userReport == null) {
                Button(
                    onClick = {
                        viewModel.onEvent(ReportEvent.SubmitReport(currentUserId))
                    },
                    content = { Text("Send Report") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .navigationBarsPadding()
                )
            }
        }
    ){ innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(horizontal = 16.dp, vertical = 10.dp)
                .fillMaxWidth()
        ) {
            when {

                state.userReport != null -> {
                    Text(
                        text = "Your Report",
                        style = MaterialTheme.typography.headlineMedium,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    ReportSummaryCard(
                        reportText = state.userReport!!.text,
                        reportTimestamp = state.userReport!!.timestamp
                    )
                }

                else -> {
                    ReportTextField(
                        recipe = recipe,
                        onValueChange = { newText ->
                            viewModel.onEvent(ReportEvent.OnTextChange(newText))
                        },
                        draftReportText = state.draftReportText,
                        reportError = state.reportError,
                        onClearError = {
                            viewModel.onEvent(ReportEvent.OnTextChange(""))
                            viewModel.onEvent(ReportEvent.ClearError)
                        }
                    )
                }
            }
        }

    }
}