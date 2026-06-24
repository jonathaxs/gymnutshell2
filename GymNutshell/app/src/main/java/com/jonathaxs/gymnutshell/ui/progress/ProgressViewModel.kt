package com.jonathaxs.gymnutshell.ui.progress

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.jonathaxs.gymnutshell.core.data.CustomGoalRepository
import com.jonathaxs.gymnutshell.core.data.DailyRecordRepository
import com.jonathaxs.gymnutshell.core.data.ProfileRepository
import com.jonathaxs.gymnutshell.core.data.SettingsRepository
import com.jonathaxs.gymnutshell.core.domain.AppDateFormatters
import com.jonathaxs.gymnutshell.core.domain.BuiltInGoals
import com.jonathaxs.gymnutshell.core.domain.DailyAchievement
import com.jonathaxs.gymnutshell.core.domain.GoalsProvider
import com.jonathaxs.gymnutshell.core.domain.MeasurementSystem
import com.jonathaxs.gymnutshell.core.domain.Profile
import com.jonathaxs.gymnutshell.core.domain.UserGoal
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import java.time.LocalDate

/** Contagem de dias num tier. */
data class TierCountUi(val emoji: String, val level: Int, val days: Int)

/** Uma célula da grade "Últimos 7 dias". `emoji` nulo = dia sem registro (mostra um ponto). */
data class RecentDayUi(
    val epochDay: Long,
    val dayNumber: Int,
    val weekday: String,
    val emoji: String?,
    val isToday: Boolean,
)

/** Dados físicos do perfil (valores brutos em métrico; formatação fica na UI conforme o measurement). */
data class PhysicalUi(
    val heightCm: Int,
    val weightKg: Double,
    val age: Int,
    val sexRaw: String,
    val measurement: MeasurementSystem,
)

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
    val activeGoals: Int = 0,
    val userGoal: UserGoal = UserGoal.Maintenance,
    val physical: PhysicalUi? = null,
    val recentDays: List<RecentDayUi> = emptyList(),
    val accentArgb: Long = 0xFF007AFF,
)

/**
 * ViewModel da ProgressView — porte da ProgressOverView (iOS).
 * Deriva estatísticas dos DailyRecord + StreakBonus (Room) e do perfil/configurações:
 * resumo, distribuição por tier, bônus, atividade, metas ativas, objetivo, dados físicos e últimos 7 dias.
 */
class ProgressViewModel(app: Application) : AndroidViewModel(app) {

    private val recordRepo = DailyRecordRepository(app.applicationContext)
    private val settingsRepo = SettingsRepository(app.applicationContext)
    private val profileRepo = ProfileRepository(app.applicationContext)
    private val customGoalRepo = CustomGoalRepository(app.applicationContext)

    // Measurement + perfil + metas personalizadas agrupados num fluxo só (combine de 5 tem limite de aridade).
    private val extras = combine(
        settingsRepo.measurementSystem,
        profileRepo.profile,
        customGoalRepo.goals,
    ) { measurement, profile, customGoals -> Triple(measurement, profile, customGoals) }

    val uiState: StateFlow<ProgressUiState> =
        combine(
            recordRepo.records,
            recordRepo.bonuses,
            settingsRepo.theme,
            settingsRepo.accentColor,
            extras,
        ) { records, bonuses, theme, accent, extra ->
            val (measurement, profile, customGoals) = extra
            val countByTier = records.groupingBy { DailyAchievement.from(it.percent / 100.0) }.eachCount()

            // Todas as metas built-in estão sempre ativas no Android (sem feature de remoção ainda) + as personalizadas.
            val activeGoals = BuiltInGoals.forResult(GoalsProvider.goals(profile)).size + customGoals.size

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
                activeGoals = activeGoals,
                userGoal = profile.goal,
                physical = physicalOf(profile, measurement),
                recentDays = recentDaysOf(records, theme),
                accentArgb = accent.argb,
            )
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ProgressUiState())

    /** Dados físicos só quando há algo preenchido — espelha o `hasPhysicalData` do iOS. */
    private fun physicalOf(profile: Profile, measurement: MeasurementSystem): PhysicalUi? {
        val hasData = profile.heightCm > 0 || profile.weightKg > 0 || profile.age > 0 ||
            profile.sex == "male" || profile.sex == "female"
        if (!hasData) return null
        return PhysicalUi(profile.heightCm, profile.weightKg, profile.age, profile.sex, measurement)
    }

    /** Últimos 7 dias do calendário (mais antigo → hoje), cada um com o emoji do tier daquele dia. */
    private fun recentDaysOf(
        records: List<com.jonathaxs.gymnutshell.core.data.DailyRecord>,
        theme: com.jonathaxs.gymnutshell.core.domain.AppTheme,
    ): List<RecentDayUi> {
        val todayEpoch = LocalDate.now().toEpochDay()
        val byDate = records.associateBy { it.date }
        return (6 downTo 0).map { offset ->
            val epoch = todayEpoch - offset
            val date = LocalDate.ofEpochDay(epoch)
            val record = byDate[epoch]
            val hasRecord = record != null && record.percent > 0
            RecentDayUi(
                epochDay = epoch,
                dayNumber = date.dayOfMonth,
                weekday = AppDateFormatters.weekdayInitial(date),
                emoji = if (hasRecord) theme.emoji(DailyAchievement.from(record!!.percent / 100.0)) else null,
                isToday = epoch == todayEpoch,
            )
        }
    }
}
