package com.example.tastify.recipe.social.domain

import android.util.Log
import com.example.tastify.BuildConfig
import com.example.tastify.models.ExtractedRecipeData
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.generationConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import javax.inject.Inject

@Serializable
data class LlmRecipeResponse(
    val title: String? = null,
    val description: String? = null,
    val servings: String? = null,
    val ingredients: List<String> = emptyList(),
    val instructions: List<String> = emptyList(),
    val prepTimeMinutes: Int? = null,
    val cookTimeMinutes: Int? = null,
    val tags: List<String> = emptyList(),
    val error: String? = null
)

class LlmRecipeExtractor @Inject constructor() {
    private val model = GenerativeModel(
        modelName = "gemini-3.1-flash-lite",
        apiKey = BuildConfig.GEMINI_API_KEY,
        generationConfig = generationConfig {
            responseMimeType = "application/json"
            temperature = 0.1f
        }
    )
    
    private val json = Json { ignoreUnknownKeys = true; isLenient = true }

    suspend fun extractRecipe(captionText: String): ExtractedRecipeData? = withContext(Dispatchers.IO) {
        val prompt = """
            Sei un esperto culinario e un assistente IA specializzato nell'estrazione e normalizzazione di ricette da testi informali, 
            come le caption caotiche dei post di Instagram o TikTok, o messaggi raw di utenti.
            
            Regole TASSATIVE:
            1. Devi restituire ESCLUSIVAMENTE un oggetto JSON valido. NESSUN blocco markdown aggiuntivo (come ```json), NESSUNA spiegazione.
            2. Se manca un'informazione (es. tempi di cottura, porzioni), usa esplicitamente "null".
            3. Gli array "ingredients" e "instructions" se mancano devono essere []. Pulisci le instructions da emoji eccessive o simboli inutili. Formatta gli step in modo pulito.
            4. Se il testo chiaramente NON parla di cibo o non contiene minimamente istruzioni/ingredienti, restituisci solo: {"error": "no_recipe"}
            5. NON RIPETERE mai gli stessi ingredienti.
            6. Mantieni SEMPRE le quantità e le unità di misura per gli ingredienti (es. "100g di", "2 cucchiai di"). Pulisci il resto da etichette fittizie, voti o descrizioni estranee (es. rimuovi cose come "(Livello 2)").
            
            Schema JSON esatto da rispettare:
            {
              "title": "Un titolo conciso ed elegante (se manca deducilo o usa null)",
              "description": "Breve descrizione della ricetta (null se assente)",
              "servings": "Numero puro di porzioni come stringa (es. '4', o null)",
              "ingredients": [
                "100g di farina",
                "2 uova"
              ],
              "instructions": [
                "Mescola la farina e le uova.",
                "Cuoci per 5 minuti."
              ],
              "prepTimeMinutes": <numero intero o null se non specificato>,
              "cookTimeMinutes": <numero intero o null se non specificato>,
              "tags": ["pasta", "veloce"]
            }
            
            Testo da analizzare:
            $captionText
        """.trimIndent()

        try {
            val response = model.generateContent(prompt)
            val text = response.text ?: return@withContext null
            
            val llmResponse = json.decodeFromString<LlmRecipeResponse>(text)
            
            if (llmResponse.error != null) {
                Log.w("LlmRecipeExtractor", "LLM returned error: ${llmResponse.error}")
                return@withContext null
            }
            
            ExtractedRecipeData(
                url = "",
                title = llmResponse.title,
                description = llmResponse.description,
                ingredients = llmResponse.ingredients,
                instructions = llmResponse.instructions,
                prepTimeMinutes = llmResponse.prepTimeMinutes,
                cookTimeMinutes = llmResponse.cookTimeMinutes,
                serves = llmResponse.servings?.replace(Regex("[^0-9]"), "")?.toIntOrNull()
            )
        } catch (e: Exception) {
            Log.e("LlmRecipeExtractor", "Error during LLM extraction", e)
            null
        }
    }
}
