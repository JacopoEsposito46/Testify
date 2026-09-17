package com.example.tastify.recipe.social.domain

import android.util.Log
import com.example.tastify.models.ExtractedRecipeData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.*
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.util.regex.Pattern
import javax.inject.Inject

class UrlRecipeExtractor @Inject constructor() {

    private val json = Json { ignoreUnknownKeys = true; isLenient = true }

    fun detectSource(url: String): RecipeSource? {
        return when {
            url.contains("instagram.com") -> RecipeSource.INSTAGRAM
            url.contains("tiktok.com") -> RecipeSource.TIKTOK
            url.contains("facebook.com") || url.contains("fb.watch") -> RecipeSource.FACEBOOK
            url.contains("pinterest.com") || url.contains("pin.it") -> RecipeSource.PINTEREST
            else -> null
        }
    }

    suspend fun extractRecipeData(url: String): ExtractedRecipeData = withContext(Dispatchers.IO) {
        try {
            Log.d("UrlRecipeExtractor", "Fetching URL: $url")
            val doc = Jsoup.connect(url)
                .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
                .header("Accept-Language", "en-US,en;q=0.9,it;q=0.8")
                .timeout(10000)
                .get()

            // JSON-LD (Schema.org)
            val jsonLdData = extractJsonLd(doc, url)
            if (jsonLdData != null) {
                Log.d("UrlRecipeExtractor", "Found Recipe in JSON-LD")
                return@withContext jsonLdData
            }

            // Microdata
            val microdataData = extractMicrodata(doc, url)
            if (microdataData != null) {
                Log.d("UrlRecipeExtractor", "Found Recipe in Microdata")
                return@withContext microdataData
            }

            // OpenGraph (Fallback)
            Log.d("UrlRecipeExtractor", "No structured data found. Fallback su OpenGraph.")
            extractOpenGraph(doc, url)

        } catch (e: Exception) {
            Log.e("UrlRecipeExtractor", "Failed to extract recipe from URL", e)
            ExtractedRecipeData(url = url)
        }
    }

    // ─────────────────────────────────────────────────
    // JSON-LD
    // ─────────────────────────────────────────────────
    private fun extractJsonLd(doc: Document, url: String): ExtractedRecipeData? {
        val scripts = doc.select("script[type=application/ld+json]")
        for (script in scripts) {
            try {
                val root = json.parseToJsonElement(script.data().trim())
                val recipeObj = findRecipeObject(root)
                if (recipeObj != null) {
                    return mapJsonToRecipeData(recipeObj, url)
                }
            } catch (e: Exception) {
                Log.e("UrlRecipeExtractor", "Error parsing JSON-LD script", e)
            }
        }
        return null
    }

    private fun findRecipeObject(element: JsonElement): JsonObject? {
        if (element is JsonObject) {
            val type = element["@type"]
            val isRecipe = when (type) {
                is JsonPrimitive -> type.content.equals("Recipe", ignoreCase = true)
                is JsonArray -> type.any { it.jsonPrimitive.content.equals("Recipe", ignoreCase = true) }
                else -> false
            }
            if (isRecipe) return element

            val graph = element["@graph"]
            if (graph is JsonArray) {
                for (item in graph) {
                    val found = findRecipeObject(item)
                    if (found != null) return found
                }
            }
            
            // proprietà nidificate
            for ((_, value) in element) {
                 if (value is JsonObject || value is JsonArray) {
                     val found = findRecipeObject(value)
                     if (found != null) return found
                 }
            }
        } else if (element is JsonArray) {
            for (item in element) {
                val found = findRecipeObject(item)
                if (found != null) return found
            }
        }
        return null
    }

    private fun mapJsonToRecipeData(obj: JsonObject, url: String): ExtractedRecipeData? {
        val name = obj.str("name") ?: return null

        return ExtractedRecipeData(
            url = url,
            title = name,
            description = obj.str("description"),
            imageUrl = normalizeImage(obj["image"]),
            ingredients = normalizeIngredients(obj["recipeIngredient"]),
            instructions = normalizeInstructions(obj["recipeInstructions"]),
            prepTimeMinutes = parseIso8601Duration(obj.str("prepTime")),
            cookTimeMinutes = parseIso8601Duration(obj.str("cookTime")),
            serves = normalizeYield(obj["recipeYield"]),
            calories = normalizeCalories(obj["nutrition"])
        )
    }

    // ─────────────────────────────────────────────────
    // MICRODATA (itemprop)
    // ─────────────────────────────────────────────────
    private fun extractMicrodata(doc: Document, url: String): ExtractedRecipeData? {
        val recipeEl = doc.selectFirst(
            "[itemtype~=(?i)schema\\.org/Recipe], [itemtype~=(?i)data-vocabulary\\.org/Recipe]"
        ) ?: return null

        fun propText(name: String): String? =
            recipeEl.selectFirst("[itemprop=$name]")?.run {
                attr("content").takeIf { it.isNotBlank() }
                    ?: attr("datetime").takeIf { it.isNotBlank() }
                    ?: text().takeIf { it.isNotBlank() }
            }

        fun propAll(name: String): List<String> =
            recipeEl.select("[itemprop=$name]").mapNotNull { el ->
                el.attr("content").takeIf { it.isNotBlank() }
                    ?: el.text().takeIf { it.isNotBlank() }
            }

        val name = propText("name") ?: return null

        val instructions = recipeEl.select("[itemprop=recipeInstructions]").mapNotNull { el ->
            el.text().takeIf { it.isNotBlank() }
        }

        val imageUrl = recipeEl.selectFirst("[itemprop=image]")?.run {
            attr("src").takeIf { it.isNotBlank() } ?: attr("content").takeIf { it.isNotBlank() }
        }

        val caloriesStr = recipeEl.selectFirst("[itemprop=nutrition] [itemprop=calories]")?.text()
        val calories = extractNumber(caloriesStr ?: "")

        return ExtractedRecipeData(
            url = url,
            title = name,
            description = propText("description"),
            imageUrl = imageUrl,
            ingredients = propAll("recipeIngredient").ifEmpty { propAll("ingredients") },
            instructions = instructions,
            prepTimeMinutes = parseIso8601Duration(propText("prepTime")),
            cookTimeMinutes = parseIso8601Duration(propText("cookTime")),
            serves = extractNumber(propText("recipeYield") ?: ""),
            calories = calories
        )
    }

    // ─────────────────────────────────────────────────
    // OPENGRAPH (Fallback)
    // ─────────────────────────────────────────────────
    private fun extractOpenGraph(doc: Document, url: String): ExtractedRecipeData {
        fun meta(property: String) = doc
            .selectFirst("meta[property=$property], meta[name=$property]")
            ?.attr("content")?.takeIf { it.isNotBlank() }

        val title = meta("og:title")
            ?: meta("twitter:title")
            ?: doc.title().takeIf { it.isNotBlank() }
            ?: "Ricetta Importata"

        return ExtractedRecipeData(
            url = url,
            title = title,
            description = meta("og:description") ?: meta("description"),
            imageUrl = meta("og:image") ?: meta("twitter:image"),
            ingredients = emptyList(),
            instructions = emptyList(),
            prepTimeMinutes = null,
            cookTimeMinutes = null,
            serves = null,
            calories = null
        )
    }

    // ─────────────────────────────────────────────────
    // NORMALIZZAZIONE
    // ─────────────────────────────────────────────────

    private fun normalizeImage(el: JsonElement?): String? = when {
        el == null || el is kotlinx.serialization.json.JsonNull -> null
        el is JsonPrimitive -> el.content.takeIf { it.startsWith("http") }
        el is JsonObject -> el.str("url") ?: el.str("contentUrl")
        el is JsonArray -> {
            el.firstNotNullOfOrNull { item ->
                when (item) {
                    is JsonPrimitive -> item.content.takeIf { it.startsWith("http") }
                    is JsonObject -> item.str("url") ?: item.str("contentUrl")
                    else -> null
                }
            }
        }
        else -> null
    }

    private fun normalizeIngredients(el: JsonElement?): List<String> = when {
        el == null || el is kotlinx.serialization.json.JsonNull -> emptyList()
        el is JsonArray -> el.mapNotNull { item ->
            when (item) {
                is JsonPrimitive -> item.content.trim().takeIf { it.isNotBlank() }
                is JsonObject -> item.str("name") ?: item.str("text")
                else -> null
            }
        }
        el is JsonPrimitive -> el.content.split("\n", ";").map { it.trim() }.filter { it.isNotBlank() }
        else -> emptyList()
    }

    private fun normalizeInstructions(el: JsonElement?): List<String> = when {
        el == null || el is kotlinx.serialization.json.JsonNull -> emptyList()
        el is JsonPrimitive -> el.content
            .split(Regex("\n{2,}|(?<=\\.)\\s+(?=[A-Z0-9])"))
            .map { it.trim() }
            .filter { it.isNotBlank() }
        el is JsonArray -> parseInstructionArray(el)
        else -> emptyList()
    }

    private fun parseInstructionArray(arr: JsonArray): List<String> {
        val result = mutableListOf<String>()
        for (item in arr) {
            when (item) {
                is JsonPrimitive -> result.add(item.content.trim())
                is JsonObject -> {
                    when (item.str("@type")) {
                        "HowToSection" -> {
                            val subSteps = item["itemListElement"]
                            if (subSteps is JsonArray) {
                                result.addAll(parseInstructionArray(subSteps))
                            } else {
                                val sectionName = item.str("name") ?: item.str("text")
                                sectionName?.let { result.add(it.trim()) }
                            }
                        }
                        else -> {
                            val text = item.str("text") ?: item.str("name")
                            text?.let { result.add(it.trim()) }
                        }
                    }
                }
                else -> {}
            }
        }
        return result
    }

    private fun normalizeYield(el: JsonElement?): Int? = when {
        el == null || el is kotlinx.serialization.json.JsonNull -> null
        el is JsonPrimitive -> extractNumber(el.content)
        el is JsonArray -> {
            val firstPrimitive = el.firstOrNull { it is JsonPrimitive } as? JsonPrimitive
            firstPrimitive?.let { extractNumber(it.content) }
        }
        else -> null
    }

    private fun normalizeCalories(el: JsonElement?): Int? {
        if (el == null || el !is JsonObject) return null
        val caloriesStr = el.str("calories") ?: return null
        return extractNumber(caloriesStr)
    }

    private fun extractNumber(str: String): Int? {
        val match = Regex("\\d+").find(str)
        return match?.value?.toIntOrNull()
    }

    private fun JsonObject.str(key: String): String? =
        try {
            val element = this[key]
            if (element is JsonPrimitive) {
                element.content.trim().takeIf { it.isNotBlank() }
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }

    private fun parseIso8601Duration(duration: String?): Int? {
        if (duration.isNullOrBlank()) return null
        try {
            var totalMinutes = 0
            val pattern = Pattern.compile("PT(?:(\\d+)H)?(?:(\\d+)M)?(?:(\\d+)S)?")
            val matcher = pattern.matcher(duration.uppercase())
            if (matcher.matches()) {
                val hours = matcher.group(1)?.toIntOrNull() ?: 0
                val minutes = matcher.group(2)?.toIntOrNull() ?: 0
                totalMinutes = (hours * 60) + minutes
            }
            return if (totalMinutes > 0) totalMinutes else null
        } catch (e: Exception) {
            return null
        }
    }
}