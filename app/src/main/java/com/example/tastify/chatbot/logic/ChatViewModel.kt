package com.example.tastify.chatbot.logic

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tastify.BuildConfig
import com.example.tastify.data.ChatLocalRepository
import com.example.tastify.domain.GetFilteredRecipesUseCase
import com.example.tastify.models.ChatMessage
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.FunctionDeclaration
import com.google.ai.client.generativeai.type.FunctionResponsePart
import com.google.ai.client.generativeai.type.FunctionType
import com.google.ai.client.generativeai.type.Schema
import com.google.ai.client.generativeai.type.Tool
import com.google.ai.client.generativeai.type.content
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import org.json.JSONObject
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val getFilteredRecipesUseCase: GetFilteredRecipesUseCase,
    private val chatLocalRepository: ChatLocalRepository
) : ViewModel() {

    private val _state = MutableStateFlow(ChatState())
    val state: StateFlow<ChatState> = _state.asStateFlow()

    private val searchRecipesTool = Tool(
        functionDeclarations = listOf(
            FunctionDeclaration(
                name = "searchAppRecipes",
                description = "Search recipes in the app database by name, category, or ingredient.",
                parameters = listOf(
                    Schema(
                        name = "query",
                        description = "The search query text (e.g., 'strawberries', 'quick dessert')",
                        type = FunctionType.STRING
                    )
                ),
                requiredParameters = listOf("query")
            )
        )
    )

    private val generativeModel = GenerativeModel(
        modelName = "gemini-2.5-flash",
        apiKey = BuildConfig.GEMINI_API_KEY,
        tools = listOf(searchRecipesTool),
        systemInstruction = content {
            text("You are the culinary assistant for the Tastify app. You must use the 'searchAppRecipes' tool to find recipes in the database. If the tool returns results, you MUST mention them formatting their name EXACTLY like this: [Recipe Name](recipe://RECIPE_ID|IMAGE_URL). NEVER invent recipe IDs or URLs. If the tool returns NO results, you can invent a recipe to help the user, but you MUST provide the ingredients and steps as PLAIN TEXT ONLY. DO NOT use the [Name](recipe://...) link format for invented recipes.")
        }
    )

    private var chat = generativeModel.startChat()

    init {
        viewModelScope.launch {
            val history = chatLocalRepository.getMessages()
            if (history.isNotEmpty()) {
                _state.update { it.copy(messages = history) }

                val historyContent = history.filter { !it.isError }.map { msg ->
                    content(msg.role) { text(msg.text) }
                }
                chat = generativeModel.startChat(historyContent)
            }
        }
    }

    fun onEvent(event: ChatEvent) {
        when (event) {
            is ChatEvent.SendMessage -> sendMessage(event.text)
            is ChatEvent.DismissError -> _state.update { it.copy(error = null) }
        }
    }

    private suspend fun saveCurrentChatState() {
        chatLocalRepository.saveMessages(_state.value.messages)
    }

    private suspend fun <T> retryWithBackoff(
        maxAttempts: Int = 3,
        initialDelayMillis: Long = 1000,
        block: suspend () -> T
    ): T {
        var currentDelay = initialDelayMillis
        var attempt = 0
        while (true) {
            try {
                return block()
            } catch (e: Exception) {
                attempt++
                if (attempt >= maxAttempts) {
                    throw e
                }
                delay(currentDelay)
                currentDelay *= 2
            }
        }
    }

    private fun sendMessage(userText: String) {
        val trimmedText = userText.trim()
        if (trimmedText.isBlank() || _state.value.isTyping) return

        _state.update { currentState ->
            currentState.copy(
                messages = currentState.messages + ChatMessage(role = "user", text = trimmedText),
                isTyping = true,
                error = null
            )
        }

        viewModelScope.launch {
            saveCurrentChatState()
            try {
                var response = retryWithBackoff { chat.sendMessage(trimmedText) }

                response.functionCalls.firstOrNull()?.let { functionCall ->
                    if (functionCall.name == "searchAppRecipes") {
                        val query = functionCall.args["query"] as? String ?: ""

                        var allRecipes = getFilteredRecipesUseCase(
                            searchQuery = "",
                            difficulty = null,
                            cuisineTypes = emptySet(),
                            dietaryRestrictions = emptySet(),
                            maxCalories = Float.MAX_VALUE
                        ).firstOrNull() ?: emptyList()

                        if (allRecipes.isEmpty()) {
                            allRecipes = withTimeoutOrNull(2000) {
                                getFilteredRecipesUseCase(
                                    searchQuery = "",
                                    difficulty = null,
                                    cuisineTypes = emptySet(),
                                    dietaryRestrictions = emptySet(),
                                    maxCalories = Float.MAX_VALUE
                                ).first { it.isNotEmpty() }
                            } ?: emptyList()
                        }

                        val foundRecipes = if (query.isBlank()) {
                            allRecipes
                        } else {
                            allRecipes.filter { recipe ->
                                recipe.title.contains(query, ignoreCase = true) ||
                                        recipe.cuisineType.displayName.contains(query, ignoreCase = true) ||
                                        recipe.ingredients.any { it.ingredient.name.contains(query, ignoreCase = true) }
                            }
                        }

                        val resultsString = if (foundRecipes.isEmpty()) {
                            "NO_RESULTS_FOUND"
                        } else {
                            foundRecipes.take(5).joinToString("\n") { recipe ->
                                "ID: ${recipe.recipeId}, Title: ${recipe.title}, ImageUrl: ${recipe.recipePhotoId}, Ingredients: ${recipe.ingredients.joinToString { it.ingredient.name }}"
                            }
                        }

                        val functionResponse = content {
                            part(
                                FunctionResponsePart(
                                    "searchAppRecipes",
                                    JSONObject(mapOf("results" to resultsString))
                                )
                            )
                        }

                        response = retryWithBackoff { chat.sendMessage(functionResponse) }
                    }
                }

                val finalAiText = response.text ?: ""

                if (finalAiText.isNotBlank()) {
                    _state.update { currentState ->
                        currentState.copy(
                            messages = currentState.messages + ChatMessage(role = "model", text = finalAiText),
                            isTyping = false
                        )
                    }
                    saveCurrentChatState()
                } else {
                    handleError("Ricevuta risposta vuota dal server.")
                }

            } catch (e: Exception) {
                handleError(e.message ?: "Errore sconosciuto.")
            }
        }
    }

    private fun handleError(errorMessage: String) {
        _state.update { currentState ->
            currentState.copy(
                messages = currentState.messages + ChatMessage(
                    role = "model",
                    text = "Ops,there was an error, please try again later.",
                    isError = true
                ),
                isTyping = false,
                error = errorMessage
            )
        }
        viewModelScope.launch {
            saveCurrentChatState()
        }
    }
}