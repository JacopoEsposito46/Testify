package com.example.tastify.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.graphics.*
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.text.Layout
import android.text.Spannable
import android.text.SpannableString
import android.text.StaticLayout
import android.text.TextPaint
import android.text.style.StyleSpan
import androidx.core.app.NotificationCompat
import com.example.tastify.models.Recipe
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL
import androidx.core.graphics.withTranslation
import androidx.core.graphics.withSave

data class PdfThemeColors(
    val primary: Int,
    val primaryLight: Int,
    val text: Int,
    val textGray: Int,
    val divider: Int
)

object PdfGenerator {

    private const val PAGE_WIDTH = 595
    private const val PAGE_HEIGHT = 842
    private const val MARGIN = 50f

    private const val CHANNEL_ID = "pdf_download_channel"
    private const val NOTIFICATION_ID = 1001

    suspend fun generateRecipePdf(context: Context, uri: Uri, recipe: Recipe, themeColors: PdfThemeColors) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        createNotificationChannel(notificationManager)

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.stat_sys_download)
            .setContentTitle("PDF generation: ${recipe.title}")
            .setContentText("In progress...")
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .setProgress(0, 0, true)

        notificationManager.notify(NOTIFICATION_ID, builder.build())

        withContext(Dispatchers.IO + NonCancellable) {
            try {
                val document = PdfDocument()
                var pageNumber = 1
                var pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNumber).create()
                var page = document.startPage(pageInfo)
                var canvas = page.canvas

                // --- GRAPHIC STYLE ---
                val titlePaint = TextPaint().apply {
                    textSize = 28f
                    isFakeBoldText = true
                    color = themeColors.primary
                    typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
                }
                val subtitlePaint = TextPaint().apply {
                    textSize = 12f
                    color = themeColors.textGray
                }
                val headerPaint = TextPaint().apply {
                    textSize = 18f
                    isFakeBoldText = true
                    color = themeColors.text
                }
                val bodyBoldPaint = TextPaint().apply {
                    textSize = 12f
                    isFakeBoldText = true
                    color = themeColors.text
                }
                val bodyPaint = TextPaint().apply {
                    textSize = 12f
                    color = themeColors.text
                }
                val pageNumberPaint = TextPaint().apply {
                    textSize = 10f
                    color = themeColors.textGray
                    textAlign = Paint.Align.CENTER
                }

                val backgroundBoxPaint = Paint().apply { color = themeColors.primaryLight; isAntiAlias = true }
                val dividerPaint = Paint().apply { color = themeColors.divider; strokeWidth = 1f }

                var currentY = MARGIN
                val maxWidth = PAGE_WIDTH - (MARGIN * 2).toInt()

                // --- HELPER FUNCTION ---
                fun drawFooter() {
                    canvas.drawText("- Page $pageNumber -", PAGE_WIDTH / 2f, PAGE_HEIGHT - 20f, pageNumberPaint)
                }

                // FORCE THE PAGE CHANGE
                fun forcePageBreak() {
                    drawFooter()
                    document.finishPage(page)
                    pageNumber++
                    pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNumber).create()
                    page = document.startPage(pageInfo)
                    canvas = page.canvas
                    currentY = MARGIN
                }

                fun checkPageBreak(requiredHeight: Float) {
                    if (currentY + requiredHeight > PAGE_HEIGHT - MARGIN - 20f) {
                        forcePageBreak()
                    }
                }

                fun measureTextHeight(text: CharSequence, paint: TextPaint, customMaxWidth: Int, alignment: Layout.Alignment = Layout.Alignment.ALIGN_NORMAL): Float {
                    if (text.isBlank()) return 0f
                    val staticLayout = StaticLayout.Builder.obtain(text, 0, text.length, paint, customMaxWidth)
                        .setAlignment(alignment)
                        .setLineSpacing(1.2f, 1f)
                        .build()
                    return staticLayout.height.toFloat()
                }

                fun drawText(text: CharSequence, paint: TextPaint, spacingAfter: Float = 0f, customX: Float = MARGIN, customMaxWidth: Int = maxWidth, alignment: Layout.Alignment = Layout.Alignment.ALIGN_NORMAL): Float {
                    if (text.isBlank()) return 0f
                    val staticLayout = StaticLayout.Builder.obtain(text, 0, text.length, paint, customMaxWidth)
                        .setAlignment(alignment)
                        .setLineSpacing(1.2f, 1f)
                        .build()

                    checkPageBreak(staticLayout.height.toFloat())

                    canvas.withTranslation(customX, currentY) {
                        staticLayout.draw(this)
                    }

                    val drawnHeight = staticLayout.height.toFloat()
                    currentY += drawnHeight + spacingAfter
                    return drawnHeight
                }

                fun drawDivider() {
                    checkPageBreak(20f)
                    currentY += 10f
                    canvas.drawLine(MARGIN, currentY, PAGE_WIDTH - MARGIN, currentY, dividerPaint)
                    currentY += 20f
                }

                fun drawCroppedImage(imageUrl: String?, targetWidth: Float, targetHeight: Float, cornerRadius: Float = 8f, customX: Float = MARGIN, customY: Float = currentY) {
                    if (imageUrl.isNullOrBlank()) return

                    val originalBitmap = try {
                        val connection = URL(imageUrl).openConnection() as HttpURLConnection
                        connection.connectTimeout = 5000
                        connection.doInput = true
                        connection.inputStream.use { BitmapFactory.decodeStream(it) }
                    } catch (e: Exception) { null } ?: return

                    val scale: Float
                    var dx = 0f
                    var dy = 0f

                    if (originalBitmap.width * targetHeight > targetWidth * originalBitmap.height) {
                        scale = targetHeight / originalBitmap.height.toFloat()
                        dx = (targetWidth - originalBitmap.width * scale) * 0.5f
                    } else {
                        scale = targetWidth / originalBitmap.width.toFloat()
                        dy = (targetHeight - originalBitmap.height * scale) * 0.5f
                    }

                    val matrix = Matrix()
                    matrix.setScale(scale, scale)
                    matrix.postTranslate(customX + dx, customY + dy)

                    val rect = RectF(customX, customY, customX + targetWidth, customY + targetHeight)

                    canvas.withSave {
                        val path = Path()
                        path.addRoundRect(rect, cornerRadius, cornerRadius, Path.Direction.CW)
                        clipPath(path)
                        drawBitmap(originalBitmap, matrix, Paint(Paint.FILTER_BITMAP_FLAG))
                    }

                    originalBitmap.recycle()
                }

                // ==========================================
                // DOCUMENT BUILDING
                // ==========================================

                // 1. HEADER (Title and author)
                val author = recipe.authorNickname.ifBlank { "Tastify Chef" }
                val subText = "By $author"

                drawText(recipe.title, titlePaint, spacingAfter = 4f, alignment = Layout.Alignment.ALIGN_CENTER)
                drawText(subText, subtitlePaint, spacingAfter = 20f, alignment = Layout.Alignment.ALIGN_CENTER)

                // 2. COVER IMAGE AND DESCRIPTION
                val coverSize = 150f
                val hasCoverImage = recipe.recipePhotoId.isNotBlank()

                var descStartX = MARGIN
                var descMaxWidth = maxWidth

                if (hasCoverImage) {
                    descStartX += coverSize + 20f
                    descMaxWidth -= (coverSize.toInt() + 20)
                }

                val descH = if (recipe.description.isNotBlank()) measureTextHeight(recipe.description, bodyPaint, descMaxWidth) else 0f
                val blockRequiredHeight = maxOf(if (hasCoverImage) coverSize else 0f, descH) + 20f

                checkPageBreak(blockRequiredHeight)

                val blockStartY = currentY

                if (hasCoverImage) {
                    drawCroppedImage(recipe.recipePhotoId, coverSize, coverSize, 12f, customX = MARGIN, customY = blockStartY)
                }

                if (recipe.description.isNotBlank()) {
                    drawText(recipe.description, bodyPaint, spacingAfter = 0f, customX = descStartX, customMaxWidth = descMaxWidth)
                }

                currentY = maxOf(currentY, blockStartY + (if (hasCoverImage) coverSize else 0f)) + 20f

                // 3. STATS GRID
                val costStr = when(recipe.cost) { 1 -> "€"; 2 -> "€€"; 3 -> "€€€"; else -> "N/A" }

                val dietText = if(recipe.dietaryRestrictions.isEmpty()) "None"
                    else recipe.dietaryRestrictions.joinToString(", ") { it.name }
                val fullDietString = "🏷️ Diet: $dietText"
                val dietLayout = StaticLayout.Builder.obtain(fullDietString, 0, fullDietString.length, bodyBoldPaint, maxWidth - 20)
                    .setAlignment(Layout.Alignment.ALIGN_NORMAL)
                    .setLineSpacing(1.2f, 1f)
                    .build()

                val statsHeight = 75f + dietLayout.height.toFloat() + 15f
                checkPageBreak(statsHeight + 20f)

                val boxRect = RectF(MARGIN, currentY, PAGE_WIDTH - MARGIN, currentY + statsHeight)
                canvas.drawRoundRect(boxRect, 8f, 8f, backgroundBoxPaint)

                val colWidth = maxWidth / 3f
                var textY = currentY + 30f

                // Row 1
                canvas.drawText("⏳ Time: ${recipe.cookTime}m", MARGIN + 10f, textY, bodyBoldPaint)
                canvas.drawText("\uD83D\uDC65 Serves: ${recipe.serves}", MARGIN + colWidth, textY, bodyBoldPaint)
                canvas.drawText("\uD83D\uDCCA Difficulty: ${recipe.difficulty.name}", MARGIN + (colWidth * 2), textY, bodyBoldPaint)

                textY += 26f
                // Row 2
                canvas.drawText("\uD83D\uDD25 Calories: ${recipe.calories} kcal", MARGIN + 10f, textY, bodyBoldPaint)
                canvas.drawText("\uD83D\uDCB2 Cost: $costStr", MARGIN + colWidth, textY, bodyBoldPaint)

                // Row 3
                canvas.withTranslation(MARGIN + 10f, textY + 15f) {
                    dietLayout.draw(this)
                }

                currentY += statsHeight + 20f

                drawDivider()

                // 4. INGREDIENTS
                drawText("Ingredients", headerPaint, spacingAfter = 12f)
                recipe.ingredients.forEach { item ->
                    val ingString = SpannableString("${item.quantity} ${item.unit} ${item.ingredient.name}")
                    ingString.setSpan(StyleSpan(Typeface.BOLD), 0, "${item.quantity} ${item.unit}".length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)

                    canvas.drawCircle(MARGIN + 6f, currentY + 6f, 3f, Paint().apply{ color = themeColors.primary })
                    drawText(ingString, bodyPaint, spacingAfter = 6f, customX = MARGIN + 16f, customMaxWidth = maxWidth - 20)
                }

                // ----------------------------------------------------
                // 5. STEPS
                // ----------------------------------------------------

                forcePageBreak()

                drawText("Instructions", headerPaint, spacingAfter = 16f)

                recipe.steps.sortedBy { it.stepNumber }.forEach { step ->
                    val imgSize = 100f
                    val hasImage = step.stepPhotoId.isNotBlank()

                    var textStartX = MARGIN + 40f
                    var textMaxWidth = maxWidth - 40

                    if (hasImage) {
                        textStartX += imgSize + 15f
                        textMaxWidth -= (imgSize.toInt() + 15)
                    }

                    val stepTitleH = if (step.title.isNotBlank()) measureTextHeight(step.title, bodyBoldPaint, textMaxWidth) + 4f else 0f
                    val descStepH = measureTextHeight(step.description, bodyPaint, textMaxWidth)
                    val requiredHeight = maxOf(if (hasImage) imgSize else 0f, stepTitleH + descStepH) + 20f

                    checkPageBreak(requiredHeight)

                    val startY = currentY

                    val stepNumStr = String.format("%02d", step.stepNumber)
                    canvas.drawText(stepNumStr, MARGIN, currentY + 14f, titlePaint)

                    if (hasImage) {
                        drawCroppedImage(step.stepPhotoId, imgSize, imgSize, 8f, customX = MARGIN + 40f, customY = startY)
                    }

                    if (step.title.isNotBlank()) {
                        drawText(step.title, bodyBoldPaint, spacingAfter = 4f, customX = textStartX, customMaxWidth = textMaxWidth)
                    }
                    drawText(step.description, bodyPaint, spacingAfter = 0f, customX = textStartX, customMaxWidth = textMaxWidth)

                    currentY = maxOf(currentY, startY + (if (hasImage) imgSize else 0f)) + 20f
                }

                // END
                drawFooter()
                document.finishPage(page)

                context.contentResolver.openOutputStream(uri)?.use { document.writeTo(it) }
                document.close()

                builder.setContentText("PDF saved successfully!")
                    .setProgress(0, 0, false)
                    .setOngoing(false)
                    .setSmallIcon(android.R.drawable.stat_sys_download_done)
                notificationManager.notify(NOTIFICATION_ID, builder.build())

            } catch (e: Exception) {
                e.printStackTrace()
                builder.setContentText("Error during generation")
                    .setProgress(0, 0, false)
                    .setOngoing(false)
                    .setSmallIcon(android.R.drawable.stat_notify_error)
                notificationManager.notify(NOTIFICATION_ID, builder.build())
                throw e
            }
        }
    }

    private fun createNotificationChannel(notificationManager: NotificationManager) {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Download PDF",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Notification for the generation of the PDF"
        }
        notificationManager.createNotificationChannel(channel)
    }
}