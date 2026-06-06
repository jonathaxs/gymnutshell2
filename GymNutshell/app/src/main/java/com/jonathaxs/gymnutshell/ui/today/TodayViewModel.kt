package com.jonathaxs.gymnutshell.ui.today

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.jonathaxs.gymnutshell.core.data.DailyRecordRepository
import com.jonathaxs.gymnutshell.core.data.IntakeRepository
import com.jonathaxs.gymnutshell.core.data.ProfileRepository
import com.jonathaxs.gymnutshell.core.data.SettingsRepository
import com.jonathaxs.gymnutshell.core.data.StreakBonus
import com.jonathaxs.gymnutshell.core.data.TodayPreferencesRepository
import com.jonathaxs.gymnutshell.core.domain.AppDateFormatters
import com.jonathaxs.gymnutshell.core.domain.BuiltInGoals
import com.jonathaxs.gymnutshell.core.domain.DailyAchievement
import com.jonathaxs.gymnutshell.core.domain.DailyRecordFactory
import com.jonathaxs.gymnutshell.core.domain.GoalCategory
import com.jonathaxs.gymnutshell.core.domain.GoalsProvider
import com.jonathaxs.gymnutshell.core.domain.Profile
import com.jonathaxs.gymnutshell.core.domain.ProgressHelpers
import com.jonathaxs.gymnutshell.core.domain.StreakBonusEvaluator
import com.jonathaxs.gymnutshell.core.domain.UserGoal
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
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

/** Uma categoria com suas metas e o estado de colapso. */
data class TodayCategoryUi(
    val category: GoalCategory,
    val goals: List<TodayGoalUi>,
    val collapsed: Boolean,
)

/** Estado completo da TodayView. */
data class TodayUiState(
    val dateLabel: String = "",
    val overallPercent: Int = 0,
    val tierEmoji: String = "🐓",
    val overallProgress: Float = 0f,
    val sections: List<TodayCategoryUi> = emptyList(),
)

/**
 * ViewModel da TodayView — porte (MVP) do estado da TodayView (iOS).
 * Junta perfil → metas (GoalsProvider) + intakes (persistidos) + colapso de categorias,
 * e calcula % geral e tier. As metas são agrupadas por categoria (Essencial primeiro).
 */
class TodayViewModel(app: Application) : AndroidViewModel(app) {

    private val profileRepo = ProfileRepository(app.applicationContext)
    private val intakeRepo = IntakeRepository(app.applicationContext)
    private val todayPrefs = TodayPreferencesRepository(app.applicationContext)
    private val recordRepo = DailyRecordRepository(app.applicationContext)
    private val settingsRepo = SettingsRepository(app.applicationContext)

    init {
        viewModelScope.launch { rolloverIfNeeded() }
    }

    /**
     * Vira o dia (porte de checkIfNewDay/finishSpecificDay do iOS): grava o DailyRecord do último
     * dia ativo com os intakes atuais, zera os intakes, preenche dias perdidos com Level1 e
     * avalia os bônus de sequência. No primeiro uso, só marca hoje como dia ativo.
     */
    private suspend fun rolloverIfNeeded() {
        val today = LocalDate.now().toEpochDay()
        val last = intakeRepo.lastActiveDay()
        if (last == null) {
            intakeRepo.setLastActiveDay(today)
            return
        }
        if (last >= today) return

        val profile = profileRepo.profile.first()
        val effective = if (profile.weightKg <= 0.0) DEMO_PROFILE else profile
        val result = GoalsProvider.goals(effective)
        val theme = settingsRepo.theme.first()

        // 1) grava o último dia com os intakes que ficaram (emoji do tema escolhido)
        recordRepo.upsert(DailyRecordFactory.build(last, intakeRepo.intakes.first(), result, theme))
        // 2) zera os intakes pro novo dia
        intakeRepo.resetAllIntakes()
        // 3) preenche dias perdidos (last+1 .. today-1) com Level1
        var day = last + 1
        while (day < today) {
            if (recordRepo.findByDate(day) == null) recordRepo.upsert(DailyRecordFactory.missed(day, theme))
            day++
        }
        // 4) marca hoje como dia ativo
        intakeRepo.setLastActiveDay(today)
        // 5) avalia bônus de sequência nos registros acumulados
        val awarded = recordRepo.awardedAnchors()
        StreakBonusEvaluator.evaluate(recordRepo.allRecords(), awarded).forEach { award ->
            recordRepo.insertBonus(
                StreakBonus(
                    anchorDate = award.anchorEpochDay,
                    bonusType = award.bonusType,
                    bonusPoints = award.points,
                    bonusEmoji = award.emoji,
                ),
            )
        }
    }

    val uiState: StateFlow<TodayUiState> =
        combine(
            profileRepo.profile,
            intakeRepo.intakes,
            todayPrefs.collapsedCategories,
            settingsRepo.theme,
        ) { profile, intakeMap, collapsed, theme ->
            // Enquanto não houver onboarding, usa um perfil-demo se nada foi salvo.
            val effective = if (profile.weightKg <= 0.0) DEMO_PROFILE else profile
            val builtins = BuiltInGoals.forResult(GoalsProvider.goals(effective))

            val allGoals = builtins.map { g ->
                TodayGoalUi(g.key, g.emoji, g.unit, g.increment, intakeMap[g.key] ?: 0, g.target)
            }
            // Agrupa por categoria na ordem da GoalCategory (Essencial → Nutrição → Treino → Suplemento).
            val sections = GoalCategory.entries.mapNotNull { category ->
                val goals = allGoals.filter { GoalCategory.defaultCategory(it.key) == category }
                if (goals.isEmpty()) null
                else TodayCategoryUi(category, goals, collapsed = category.rawValue in collapsed)
            }

            val avg = if (allGoals.isEmpty()) 0.0 else allGoals.sumOf { it.progress } / allGoals.size

            TodayUiState(
                dateLabel = AppDateFormatters.longDate(LocalDate.now()),
                overallPercent = floor(avg * 100).toInt(),
                tierEmoji = theme.emoji(DailyAchievement.from(avg)),
                overallProgress = avg.toFloat(),
                sections = sections,
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

    fun toggleCategory(category: GoalCategory) {
        viewModelScope.launch { todayPrefs.toggleCategory(category.rawValue) }
    }

    companion object {
        // Perfil-demo temporário até a tela de Welcome existir (Fase 4).
        private val DEMO_PROFILE = Profile(
            weightKg = 80.0, heightCm = 180, age = 30, sex = "male", goal = UserGoal.Maintenance,
        )
    }
}
