package com.example.tastify.utils

import com.example.tastify.models.ProfileFrame
import kotlin.math.pow
import kotlin.math.sqrt

object LevelSystem {

    fun calculateLevel(exp: Int): Int {
        return (sqrt(exp.toDouble()) / 10).toInt() + 1
    }

    fun expForNextLevel(currentLevel: Int): Int {
        return ((currentLevel) * 10.0).pow(2.0).toInt()
    }

    fun expForCurrentLevel(currentLevel: Int): Int {
        return ((currentLevel - 1) * 10.0).pow(2.0).toInt()
    }

    fun getLevelProgress(exp: Int): Float {
        val currentLevel = calculateLevel(exp)
        val currentLevelExp = expForCurrentLevel(currentLevel)
        val nextLevelExp = expForNextLevel(currentLevel)
        val expInCurrentLevel = exp - currentLevelExp
        val expRequiredForNext = nextLevelExp - currentLevelExp
        if (expRequiredForNext == 0) return 1f
        return expInCurrentLevel.toFloat() / expRequiredForNext.toFloat()
    }

    fun getFrameForLevel(level: Int): ProfileFrame {
        return when {
            level < 10 -> ProfileFrame.BRONZE
            level < 25 -> ProfileFrame.SILVER
            level < 50 -> ProfileFrame.GOLD
            level < 100 -> ProfileFrame.PLATINUM
            else -> ProfileFrame.DIAMOND
        }
    }
}