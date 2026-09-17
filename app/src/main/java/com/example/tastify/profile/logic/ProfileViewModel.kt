package com.example.tastify.profile.logic

import android.app.AppOpsManager
import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.os.Process
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tastify.domain.GetDoneHistoryUseCase
import com.example.tastify.domain.GetRecipesUseCase
import com.example.tastify.domain.GetUserProfileUseCase
import com.example.tastify.domain.UpdateCookedRecipesCountUseCase
import com.example.tastify.models.Recipe
import com.example.tastify.models.UserProfile
import com.example.tastify.utils.SessionManager
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val getUserProfileUseCase: GetUserProfileUseCase,
    private val getDoneHistoryUseCase: GetDoneHistoryUseCase,
    private val updateCookedRecipesCountUseCase: UpdateCookedRecipesCountUseCase,
    private val getRecipesUseCase: GetRecipesUseCase
) : ViewModel() {

    private val currentUserId = SessionManager.CURRENT_LOGGED_IN_USER_ID ?: ""
    private val _isLoading = MutableStateFlow(true)
    private val usageStatsManager = context.getSystemService(UsageStatsManager::class.java)
    private var cookedRecipesSyncStarted = false

    private val usageSummaryFlow = flow {
        emit(readWeeklyUsage())
    }.flowOn(Dispatchers.IO)

    val uiState: StateFlow<ProfileState> = combine(
        getUserProfileUseCase(currentUserId),
        if (currentUserId.isBlank()) flowOf(emptyList()) else getRecipesUseCase(ownerId = currentUserId)
            .catch { emit(emptyList()) },
        usageSummaryFlow,
        _isLoading
    ) { profile, recipes, usageSummary, isLoading ->
        syncCookedRecipesCountIfNeeded(profile)
        ProfileState(
            profile = profile,
            isLoading = isLoading,
            totalUsageMinutes = usageSummary.totalMinutes,
            averageDailyUsageMinutes = usageSummary.averageDailyMinutes,
            weeklyUsageMinutes = usageSummary.dailyMinutes,
            usageAccessGranted = usageSummary.accessGranted,
            topIngredient = profile.topIngredient.ifBlank { recipes.calculateTopIngredient() }
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ProfileState(profile = UserProfile(internalID ="-1", username = "", name = "", surname = "", email = ""), isLoading = true)
    )

    fun onEvent(event: ProfileEvent) {
        when (event) {
            is ProfileEvent.Refresh -> {
                _isLoading.update { true }
                viewModelScope.launch {
                    delay(2000)
                    _isLoading.update { false }
                }
            }

        }
    }

    private fun List<Recipe>.calculateTopIngredient(): String {
        return flatMap { recipe -> recipe.ingredients }
            .map { recipeIngredient -> recipeIngredient.ingredient.name.trim() }
            .filter { it.isNotBlank() }
            .groupingBy { it.lowercase() }
            .eachCount()
            .maxByOrNull { it.value }
            ?.key
            ?.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
            .orEmpty()
    }

    private fun syncCookedRecipesCountIfNeeded(profile: UserProfile) {
        if (cookedRecipesSyncStarted || currentUserId.isBlank() || profile.cookedRecipes != 0) return

        cookedRecipesSyncStarted = true
        viewModelScope.launch {
            runCatching {
                val doneCount = getDoneHistoryUseCase(currentUserId, DONE_HISTORY_LIMIT)
                    .first()
                    .distinctBy { it.recipeId }
                    .size

                if (doneCount > 0) {
                    updateCookedRecipesCountUseCase(currentUserId, doneCount)
                }
            }
        }
    }

    private fun readWeeklyUsage(): UsageSummary {
        if (!hasUsageAccess()) return UsageSummary(accessGranted = false)

        val zone = ZoneId.systemDefault()
        val weekStart = LocalDate.now().with(DayOfWeek.MONDAY)
        val startMillis = weekStart.atStartOfDay(zone).toInstant().toEpochMilli()
        val endMillis = System.currentTimeMillis()
        val durations = LongArray(7)
        val event = UsageEvents.Event()
        var lastResume: Long? = null

        usageStatsManager.queryEvents(startMillis, endMillis).let { events ->
            while (events.hasNextEvent()) {
                events.getNextEvent(event)
                if (event.packageName != context.packageName) continue

                when (event.eventType) {
                    UsageEvents.Event.ACTIVITY_RESUMED -> lastResume = event.timeStamp

                    UsageEvents.Event.ACTIVITY_PAUSED,
                    UsageEvents.Event.ACTIVITY_STOPPED -> {
                        val startedAt = lastResume
                        if (startedAt != null && event.timeStamp > startedAt) {
                            addUsageDuration(durations, startedAt, event.timeStamp, weekStart, zone)
                        }
                        lastResume = null
                    }
                }
            }
        }

        lastResume?.let { startedAt ->
            if (endMillis > startedAt) {
                addUsageDuration(durations, startedAt, endMillis, weekStart, zone)
            }
        }

        val dailyMinutes = durations.map { (it / MILLIS_PER_MINUTE).toInt() }
        return UsageSummary(
            accessGranted = true,
            dailyMinutes = dailyMinutes
        )
    }

    private fun addUsageDuration(
        durations: LongArray,
        startMillis: Long,
        endMillis: Long,
        weekStart: LocalDate,
        zone: ZoneId
    ) {
        var cursor = startMillis
        val weekEnd = weekStart.plusDays(6)

        while (cursor < endMillis) {
            val date = Instant.ofEpochMilli(cursor).atZone(zone).toLocalDate()
            val nextDayMillis = date.plusDays(1).atStartOfDay(zone).toInstant().toEpochMilli()
            val segmentEnd = minOf(endMillis, nextDayMillis)

            if (!date.isBefore(weekStart) && !date.isAfter(weekEnd)) {
                durations[date.dayOfWeek.value - 1] += segmentEnd - cursor
            }

            cursor = segmentEnd
        }
    }

    private fun hasUsageAccess(): Boolean {
        val appOpsManager = context.getSystemService(AppOpsManager::class.java)
        val mode = appOpsManager.checkOpNoThrow(
            AppOpsManager.OPSTR_GET_USAGE_STATS,
            Process.myUid(),
            context.packageName
        )
        return mode == AppOpsManager.MODE_ALLOWED
    }

    private data class UsageSummary(
        val accessGranted: Boolean,
        val dailyMinutes: List<Int> = emptyList()
    ) {
        val totalMinutes: Int = dailyMinutes.sum()
        val averageDailyMinutes: Int = if (dailyMinutes.isEmpty()) 0 else totalMinutes / dailyMinutes.size
    }

    private companion object {
        const val MILLIS_PER_MINUTE = 60_000L
        const val DONE_HISTORY_LIMIT = 1000
    }
}
