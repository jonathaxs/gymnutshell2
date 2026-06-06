package com.jonathaxs.gymnutshell.ui.today

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.jonathaxs.gymnutshell.core.data.IntakeRepository
import com.jonathaxs.gymnutshell.core.data.ProfileRepository
import com.jonathaxs.gymnutshell.core.domain.AppDateFormatters
import com.jonathaxs.gymnutshell.core.domain.BuiltInGoals
import com.jonathaxs.gymnutshell.core.domain.DailyAchievement
import com.jonathaxs.gymnutshell.core.domain.GoalsProvider
import com.jonathaxs.gymnutshell.core.domain.Profile
import com.jonathaxs.gymnutshell.core.domain.ProgressHelpers
import com.jonathaxs.gymnutshell.core.domain.UserGoal
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
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
    val tierEmoji: String = "🐓",
    val overallProgress: Float = 0f,
    val goals: List<TodayGoalUi> = emptyList(),
)

/**
 * ViewModel da TodayView — porte (MVP) do estado da TodayView (iOS).
 * Junta perfil → metas (GoalsProvider) + os intakes do dia (persistidos no DataStore),
 * e calcula % geral e tier. Os intakes resetam quando vira o dia.
 */
class TodayViewModel(app: Application) : AndroidViewModel(app) {

    private val profileRepo = ProfileRepository(app.applicationContext)
    private val intakeRepo = IntakeRepository(app.applicationContext)

    init {
        // Ao abrir, se virou o dia, zera os intakes. (Fatia 6: salvar o DailyRecord do dia
        // anterior ANTES de zerar; por ora só reseta.)
        viewModelScope.launch {
            val today = LocalDate.now().toEpochDay()
            if (intakeRepo.lastActiveDay() != today) {
                intakeRepo.resetAllIntakes()
                intakeRepo.setLastActiveDay(today)
            }
        }
    }

    val uiState: StateFlow<TodayUiState> =
        combine(profileRepo.profile, intakeRepo.intakes) { profile, intakeMap ->
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

    fun increment(goal: TodayGoalUi) = setIntake(goal, goal.intake + goal.increment)
    fun decrement(goal: TodayGoalUi) = setIntake(goal, goal.intake - goal.increment)

    // Persiste o valor, limitado entre 0 e a meta (o slider do iOS também é limitado ao alvo).
    private fun setIntake(goal: TodayGoalUi, value: Int) {
        viewModelScope.launch {
            intakeRepo.setIntake(goal.key, value.coerceIn(0, goal.target))
        }
    }

    companion object {
        // Perfil-demo temporário até a tela de Welcome existir (Fase 4).
        private val DEMO_PROFILE = Profile(
            weightKg = 80.0, heightCm = 180, age = 30, sex = "male", goal = UserGoal.Maintenance,
        )
    }
}
