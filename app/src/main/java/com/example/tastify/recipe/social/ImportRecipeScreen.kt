package com.example.tastify.recipe.social

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.tastify.components.SharedTopBar
import com.example.tastify.recipe.social.components.*
import com.example.tastify.recipe.social.logic.ImportRecipeEvent
import com.example.tastify.recipe.social.logic.ImportRecipeViewModel
import kotlinx.serialization.json.Json

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImportRecipeScreen(
    sharedUrl: String? = null,
    onBack: () -> Unit,
    onImportSuccess: (String) -> Unit,
    viewModel: ImportRecipeViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var hasConsumedIntent by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(sharedUrl) {
        if (!hasConsumedIntent) {
            if (!sharedUrl.isNullOrBlank()) {
                viewModel.onEvent(ImportRecipeEvent.OnSharedUrlReceived(sharedUrl))
            }
            hasConsumedIntent = true
        }
    }

    LaunchedEffect(state.extractedData) {
        state.extractedData?.let {
            val jsonString = Json.encodeToString(it)
            onImportSuccess(jsonString)
            viewModel.onEvent(ImportRecipeEvent.ClearExtractedData)
        }
    }

    if (state.importUrlToConfirm != null) {
        ImportConfirmDialog(
            importUrlToConfirm = state.importUrlToConfirm!!,
            onConfirm = { viewModel.onEvent(ImportRecipeEvent.ConfirmImportPreview) },
            onDismiss = { viewModel.onEvent(ImportRecipeEvent.DismissImportPreview) }
        )
    }

    if (state.error != null) {
        ImportErrorDialog(
            error = state.error!!,
            onDismiss = { viewModel.onEvent(ImportRecipeEvent.ClearError) }
        )
    }

    Scaffold(
        topBar = {
            SharedTopBar(
                title = "Import Recipe",
                onBack = onBack
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize()) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 24.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                item {
                    ImportUrlSection(
                        urlInput = state.urlInput,
                        isLoading = state.isLoading,
                        onUrlChanged = { viewModel.onEvent(ImportRecipeEvent.OnUrlChanged(it)) },
                        onImportClicked = { viewModel.onEvent(ImportRecipeEvent.OnImportClicked(state.urlInput)) }
                    )
                }

                item {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        HorizontalDivider(
                            modifier = Modifier.weight(1f),
                            color = MaterialTheme.colorScheme.outlineVariant
                        )
                        Text(
                            text = "OR",
                            modifier = Modifier.padding(horizontal = 16.dp),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        HorizontalDivider(
                            modifier = Modifier.weight(1f),
                            color = MaterialTheme.colorScheme.outlineVariant
                        )
                    }
                }

                item {
                    SocialImportSection(context = context)
                }

                item {
                    ImportTutorialSection()
                }

                item {
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }

            if (state.isLoading) {
                ImportLoadingOverlay(loadingMessage = state.loadingMessage)
            }
        }
    }
}
