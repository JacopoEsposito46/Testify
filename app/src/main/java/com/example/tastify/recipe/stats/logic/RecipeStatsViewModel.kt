package com.example.tastify.recipe.stats.logic

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.example.tastify.domain.GetRecipeByIdUseCase
import com.example.tastify.domain.GetRecipeDailyAnalyticsUseCase
import com.example.tastify.models.RecipeDailyAnalytics
import com.example.tastify.navigation.RecipeStatsRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import javax.inject.Inject
import kotlin.math.abs
import kotlin.math.round

@HiltViewModel
class RecipeStatsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getRecipeByIdUseCase: GetRecipeByIdUseCase,
    private val getRecipeDailyAnalyticsUseCase: GetRecipeDailyAnalyticsUseCase
) : ViewModel() {

    private val route = savedStateHandle.toRoute<RecipeStatsRoute>()
    private val recipeId = route.recipeId
    private val selectedPeriod = MutableStateFlow(RecipeStatsPeriod.WEEK)

    val uiState: StateFlow<RecipeStatsState> = combine(
        selectedPeriod,
        getRecipeByIdUseCase(recipeId),
        getRecipeDailyAnalyticsUseCase(recipeId, ANALYTICS_LIMIT)
    ) { period, recipe, analytics ->
        RecipeStatsState(
            isLoading = false,
            recipeTitle = recipe?.title.orEmpty(),
            totalViews = recipe?.views ?: 0,
            totalFavorites = recipe?.favouriteCount ?: 0,
            totalComments = recipe?.commentCount ?: 0,
            totalReviews = recipe?.reviewCount ?: 0,
            averageRating = recipe?.rating ?: 0.0,
            viewsChangeText = changeText(
                todayValue = dailyMetric(analytics, 0, ::viewsMetric),
                yesterdayValue = dailyMetric(analytics, 1, ::viewsMetric)
            ),
            favoritesChangeText = changeText(
                todayValue = dailyMetric(analytics, 0, ::favoritesMetric),
                yesterdayValue = dailyMetric(analytics, 1, ::favoritesMetric)
            ),
            commentsChangeText = changeText(
                todayValue = dailyMetric(analytics, 0, ::commentsMetric),
                yesterdayValue = dailyMetric(analytics, 1, ::commentsMetric)
            ),
            reviewsChangeText = changeText(
                todayValue = dailyMetric(analytics, 0, ::reviewsMetric),
                yesterdayValue = dailyMetric(analytics, 1, ::reviewsMetric)
            ),
            selectedPeriod = period,
            trendPoints = buildTrendPoints(analytics, period)
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = RecipeStatsState()
    )

    fun onEvent(event: RecipeStatsEvent) {
        when (event) {
            is RecipeStatsEvent.ChangePeriod -> selectedPeriod.update { event.period }
        }
    }

    private fun buildTrendPoints(
        analytics: List<RecipeDailyAnalytics>,
        period: RecipeStatsPeriod
    ): List<RecipeTrendPoint> {
        if (period == RecipeStatsPeriod.MONTH) {
            return buildMonthlyTrendPoints(analytics)
        }

        val dateFormat = SimpleDateFormat(DATE_PATTERN, Locale.US)
        val labelFormat = SimpleDateFormat(WEEK_LABEL_PATTERN, Locale.US)
        val analyticsByDate = analytics.associateBy { it.date }

        return (WEEK_DAYS - 1 downTo 0).map { offset ->
            val calendar = Calendar.getInstance()
            calendar.add(Calendar.DAY_OF_YEAR, -offset)
            val dateKey = dateFormat.format(calendar.time)
            RecipeTrendPoint(
                label = labelFormat.format(calendar.time),
                value = analyticsByDate[dateKey]?.views ?: 0
            )
        }
    }

    private fun buildMonthlyTrendPoints(analytics: List<RecipeDailyAnalytics>): List<RecipeTrendPoint> {
        val monthKeyFormat = SimpleDateFormat(MONTH_KEY_PATTERN, Locale.US)
        val monthLabelFormat = SimpleDateFormat(MONTH_LABEL_PATTERN, Locale.US)
        val viewsByMonth = analytics.groupBy { it.date.take(MONTH_KEY_LENGTH) }
            .mapValues { entry -> entry.value.sumOf { it.views } }

        return (MONTHS_TO_SHOW - 1 downTo 0).map { offset ->
            val calendar = Calendar.getInstance()
            calendar.add(Calendar.MONTH, -offset)
            val monthKey = monthKeyFormat.format(calendar.time)
            RecipeTrendPoint(
                label = monthLabelFormat.format(calendar.time),
                value = viewsByMonth[monthKey] ?: 0
            )
        }
    }

    private fun dailyMetric(
        analytics: List<RecipeDailyAnalytics>,
        dayOffset: Int,
        selector: (RecipeDailyAnalytics) -> Int
    ): Int {
        val dateFormat = SimpleDateFormat(DATE_PATTERN, Locale.US)
        val date = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, -dayOffset)
        }.let { calendar -> dateFormat.format(calendar.time) }

        return analytics
            .filter { it.date == date }
            .sumOf(selector)
    }

    private fun changeText(todayValue: Int, yesterdayValue: Int): String {
        val difference = todayValue - yesterdayValue
        val percentage = when {
            todayValue == 0 && yesterdayValue == 0 -> 0.0
            todayValue == 0 -> 0.0
            yesterdayValue == 0 -> if (todayValue > 0) 100.0 else -100.0
            else -> difference.toDouble() / abs(yesterdayValue).toDouble() * 100.0
        }
        val roundedPercentage = (round(percentage * 10.0) / 10.0)
            .let { if (it == 0.0) 0.0 else it }
        val formattedPercentage = if (roundedPercentage % 1.0 == 0.0) {
            String.format(Locale.US, "%.0f", roundedPercentage)
        } else {
            String.format(Locale.US, "%.1f", roundedPercentage)
        }
        val sign = if (roundedPercentage >= 0.0) "+" else ""
        return "$sign$formattedPercentage% vs yesterday"
    }

    private fun viewsMetric(item: RecipeDailyAnalytics) = item.views

    private fun favoritesMetric(item: RecipeDailyAnalytics) = item.favoritesAdded - item.favoritesRemoved

    private fun commentsMetric(item: RecipeDailyAnalytics) = item.comments - item.commentsRemoved

    private fun reviewsMetric(item: RecipeDailyAnalytics) = item.reviews - item.reviewsRemoved

    private companion object {
        const val ANALYTICS_LIMIT = 190
        const val WEEK_DAYS = 7
        const val MONTHS_TO_SHOW = 6
        const val MONTH_KEY_LENGTH = 7
        const val DATE_PATTERN = "yyyy-MM-dd"
        const val MONTH_KEY_PATTERN = "yyyy-MM"
        const val WEEK_LABEL_PATTERN = "EEE"
        const val MONTH_LABEL_PATTERN = "MMM"
    }
}
