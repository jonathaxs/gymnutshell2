package com.jonathaxs.gymnutshell.ui.today

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.jonathaxs.gymnutshell.core.data.ProfileRepository
import com.jonathaxs.gymnutshell.core.domain.BuiltInGoals
import com.jonathaxs.gymnutshell.core.domain.DailyAchievement
import com.jonathaxs.gymnutshell.core.domain.GoalsProvider
import com.jonathaxs.gymnutshell.core.domain.Profile
import com.jonathaxs.gymnutshell.core.domain.ProgressHelpers
import com.jonathaxs.gymnutshell.core.domain.UserGoal
import com.jonathaxs.gymnutshell.core.domain.AppDateFormatters
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import java.time.LocalDate
import kotlin.math.floor

/** Uma meta da TodayView pronta pra exibição. */
data class TodayGoalUi(
    val key: String,
    val emoji: String,
    val unit: String,
    val increment: Int,
    val intake: Int,
    val target: Int,
) {
    val progress: Double get() = ProgressHelpers.normalizedProgress(intake, target)
}

/** Estado completo da TodayView. */
data class TodayUiState(
    val dateLabel: String = "",
    val overallPercent: Int = 0,
    val tierEmoji: String = "🐱",
    val overallProgress: Float = 0f,
    val goals: List<TodayGoalUi> = emptyList(),
)

/**
 * ViewModel da TodayView — porte (MVP) do estado da TodayView (iOS).
 * Junta perfil → metas (GoalsProvider) + os intakes do dia, e calcula % geral e tier.
 * Por ora os intakes vivem em memória (resetam ao reabrir); persistência vem na fatia 2.
 */
class TodayViewModel(app: Application) : AndroidViewModel(app) {

    private val profileRepo = ProfileRepository(app.applicationContext)

    /** Ingestão do dia por chave de meta (em memória por enquanto). */
    private val intakes = MutableStateFlow<Map<String, Int>>(emptyMap())

    val uiState: StateFlow<TodayUiState> =
        combine(profileRepo.profile, intakes) { profile, intakeMap ->
            // Enquanto não houver onboarding, usa um perfil-demo se nada foi salvo.
            val effective = if (profile.weightKg <= 0.0) DEMO_PROFILE else profile
            val result = GoalsProvider.goals(effective)

            val goals = BuiltInGoals.forResult(result).map { g ->
                TodayGoalUi(g.key, g.emoji, g.unit, g.increment, intakeMap[g.key] ?: 0, g.target)
            }
            val avg = if (goals.isEmpty()) 0.0 else goals.sumOf { it.progress } / goals.size

            TodayUiState(
                dateLabel = AppDateFormatters.longDate(LocalDate.now()),
                overallPercent = floor(avg * 100).toInt(),
                tierEmoji = DailyAchievement.from(avg).emoji,
                overallProgress = avg.toFloat(),
                goals = goals,
            )
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), TodayUiState())

    fun increment(goal: TodayGoalUi) = changeIntake(goal.key, goal.increment)
    fun decrement(goal: TodayGoalUi) = changeIntake(goal.key, -goal.increment)

    private fun changeIntake(key: String, delta: Int) {
        intakes.update { current ->
            current + (key to ((current[key] ?: 0) + delta).coerceAtLeast(0))
        }
    }

    companion object {
        // Perfil-demo temporário até a tela de Welcome existir (Fase 4).
        private val DEMO_PROFILE = Profile(
            weightKg = 80.0, heightCm = 180, age = 30, sex = "male", goal = UserGoal.Maintenance,
        )
    }
}
