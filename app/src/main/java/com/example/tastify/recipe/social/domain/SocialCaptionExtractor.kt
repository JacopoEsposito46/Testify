package com.example.tastify.recipe.social.domain

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import com.example.tastify.BuildConfig
import org.jsoup.Jsoup
import java.net.URLEncoder
import javax.inject.Inject

enum class RecipeSource { INSTAGRAM, TIKTOK, FACEBOOK, PINTEREST }

sealed class CaptionResult {
    data class Success(val caption: String, val thumbnailUrl: String?) : CaptionResult()
    data class Error(val message: String) : CaptionResult()
}

class SocialCaptionExtractor @Inject constructor() {
    private val json = Json { ignoreUnknownKeys = true; isLenient = true }

    suspend fun fetchCaption(url: String, source: RecipeSource): CaptionResult = withContext(Dispatchers.IO) {
        when (source) {
            RecipeSource.TIKTOK -> fetchTikTokCaption(url)
            RecipeSource.INSTAGRAM -> fetchScrapeCreatorsCaption(url, "instagram", "post")
            RecipeSource.FACEBOOK -> fetchScrapeCreatorsCaption(url, "facebook", "post")
            RecipeSource.PINTEREST -> fetchScrapeCreatorsCaption(url, "pinterest", "pin")
            else -> CaptionResult.Error("Source not supported for the import.")
        }
    }

    private fun fetchTikTokCaption(url: String): CaptionResult {
        return try {
            val encodedUrl = URLEncoder.encode(url, "UTF-8")
            val oembedUrl = "https://www.tiktok.com/oembed?url=$encodedUrl"
            
            val response = Jsoup.connect(oembedUrl).ignoreContentType(true).execute().body()
            val root = json.parseToJsonElement(response).jsonObject
            
            val title = root["title"]?.jsonPrimitive?.content
            val thumbnailUrl = root["thumbnail_url"]?.jsonPrimitive?.content
            
            if (!title.isNullOrBlank()) {
                CaptionResult.Success(title, thumbnailUrl)
            } else {
                CaptionResult.Error("No caption found.")
            }
        } catch (e: Exception) {
            CaptionResult.Error("Error in the text extraction.")
        }
    }

    private fun fetchScrapeCreatorsCaption(url: String, platform: String, endpoint: String): CaptionResult {
        return try {
            val apiKey = BuildConfig.SCRAPECREATORS_API_KEY
            if (apiKey.isBlank()) {
                return CaptionResult.Error("API KEY ScrapeCreators not found.")
            }

            val encodedUrl = URLEncoder.encode(url, "UTF-8")
            val apiUrl = "https://api.scrapecreators.com/v1/$platform/$endpoint?url=$encodedUrl"

            val response = Jsoup.connect(apiUrl)
                .header("x-api-key", apiKey)
                .ignoreContentType(true)
                .timeout(15000)
                .get()
                .body()
                .text()

            val root = json.parseToJsonElement(response).jsonObject
            
            // GraphQL xdt_shortcode_media
            val mediaObj = root["data"]?.jsonObject?.get("xdt_shortcode_media")?.jsonObject
            
            val captionText = mediaObj?.get("edge_media_to_caption")?.jsonObject
                ?.get("edges")?.jsonArray?.firstOrNull()?.jsonObject
                ?.get("node")?.jsonObject?.get("text")?.jsonPrimitive?.content

            val candidates = listOf(
                captionText,
                root["caption"]?.jsonPrimitive?.content,
                root["text"]?.jsonPrimitive?.content,
                root["description"]?.jsonPrimitive?.content,
                root["seoDescription"]?.jsonPrimitive?.content
            )

            val caption = candidates.firstOrNull { !it.isNullOrBlank() } ?: response

            val imageCandidates = listOf(
                mediaObj?.get("display_url")?.jsonPrimitive?.content,
                mediaObj?.get("thumbnail_src")?.jsonPrimitive?.content,
                root["thumbnail_url"]?.jsonPrimitive?.content,
                root["display_url"]?.jsonPrimitive?.content,
                root["image_url"]?.jsonPrimitive?.content,
                root["images_orig"]?.jsonObject?.get("url")?.jsonPrimitive?.content,
                root["imageCoverUrl"]?.jsonPrimitive?.content
            )
            
            val image = imageCandidates.firstOrNull { !it.isNullOrBlank() }

            if (caption.isNotBlank()) {
                CaptionResult.Success(caption, image)
            } else {
                CaptionResult.Error("No caption found.")
            }
        } catch (e: Exception) {
            CaptionResult.Error("Impossible import the recipe from the link attached.")
        }
    }
}
