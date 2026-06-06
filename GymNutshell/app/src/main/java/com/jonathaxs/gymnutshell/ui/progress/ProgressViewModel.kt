package com.jonathaxs.gymnutshell.ui.progress

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.jonathaxs.gymnutshell.core.data.DailyRecordRepository
import com.jonathaxs.gymnutshell.core.data.SettingsRepository
import com.jonathaxs.gymnutshell.core.domain.DailyAchievement
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

/** Contagem de dias num tier. */
data class TierCountUi(val emoji: String, val level: Int, val days: Int)

/** Estado da ProgressView. */
data class ProgressUiState(
    val totalDays: Int = 0,
    val totalPoints: Int = 0,
    val bonusCount: Int = 0,
    val tierCounts: List<TierCountUi> = emptyList(),
    val workoutDays: Int = 0,
    val cardioDays: Int = 0,
    val weeklyStrong: Int = 0,
    val weeklyExpert: Int = 0,
    val monthlyStrong: Int = 0,
    val monthlyExpert: Int = 0,
)

/**
 * ViewModel da ProgressView — porte (MVP) da ProgressOverView (iOS).
 * Deriva estatísticas dos DailyRecord + StreakBonus (Room): resumo, tiers, atividade e bônus.
 */
class ProgressViewModel(app: Application) : AndroidViewModel(app) {

    private val recordRepo = DailyRecordRepository(app.applicationContext)
    private val settingsRepo = SettingsRepository(app.applicationContext)

    val uiState: StateFlow<ProgressUiState> =
        combine(recordRepo.records, recordRepo.bonuses, settingsRepo.theme) { records, bonuses, theme ->
            val countByTier = records.groupingBy { DailyAchievement.from(it.percent / 100.0) }.eachCount()

            ProgressUiState(
                totalDays = records.size,
                totalPoints = records.sumOf { it.points } + bonuses.sumOf { it.bonusPoints },
                bonusCount = bonuses.size,
                tierCounts = DailyAchievement.entries.map { tier ->
                    TierCountUi(emoji = theme.emoji(tier), level = tier.ordinal + 1, days = countByTier[tier] ?: 0)
                },
                workoutDays = records.count { it.didWorkout },
                cardioDays = records.count { it.didCardio },
                weeklyStrong = bonuses.count { it.bonusType == "weekly.level3" },
                weeklyExpert = bonuses.count { it.bonusType == "weekly.level4" },
                monthlyStrong = bonuses.count { it.bonusType == "monthly.level3" },
                monthlyExpert = bonuses.count { it.bonusType == "monthly.level4" },
            )
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ProgressUiState())
}
