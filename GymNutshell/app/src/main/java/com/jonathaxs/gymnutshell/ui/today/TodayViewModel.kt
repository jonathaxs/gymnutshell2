package com.jonathaxs.gymnutshell.ui.today

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.jonathaxs.gymnutshell.core.data.CustomGoalRepository
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
    val isRestDay: Boolean = false,
    val supportsRestDay: Boolean = false,
    val category: GoalCategory? = null,
    /** Título já resolvido (metas custom); null = built-in (UI resolve via string). */
    val title: String? = null,
) {
    // Em dia de descanso a meta conta como 100%, sem precisar de intake.
    val progress: Double get() = if (isRestDay) 1.0 else ProgressHelpers.normalizedProgress(intake, target)
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
    /** Metas personalizadas sem categoria, exibidas no fim da lista. */
    val uncategorizedGoals: List<TodayGoalUi> = emptyList(),
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
    private val customGoalRepo = CustomGoalRepository(app.applicationContext)

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
        val restDays = intakeRepo.restDays.first()
        val customGoals = customGoalRepo.all()

        // 1) grava o último dia com os intakes que ficaram (tema + dias de descanso + metas custom)
        recordRepo.upsert(
            DailyRecordFactory.build(last, intakeRepo.intakes.first(), result, theme, restDays, customGoals),
        )
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
            combine(profileRepo.profile, customGoalRepo.goals) { profile, custom -> profile to custom },
            intakeRepo.intakes,
            todayPrefs.collapsedCategories,
            settingsRepo.theme,
            intakeRepo.restDays,
        ) { (profile, customGoals), intakeMap, collapsed, theme, restDays ->
            // Enquanto não houver onboarding, usa um perfil-demo se nada foi salvo.
            val effective = if (profile.weightKg <= 0.0) DEMO_PROFILE else profile

            // Metas fixas.
            val builtinGoals = BuiltInGoals.forResult(GoalsProvider.goals(effective)).map { g ->
                val category = GoalCategory.defaultCategory(g.key)
                TodayGoalUi(
                    key = g.key, emoji = g.emoji, unit = g.unit, increment = g.increment,
                    intake = intakeMap[g.key] ?: 0, target = g.target,
                    isRestDay = g.key in restDays,
                    supportsRestDay = category == GoalCategory.Treino,
                    category = category,
                )
            }
            // Metas personalizadas (key "custom:<id>", título = nome).
            val customGoalsUi = customGoals.map { c ->
                val category = GoalCategory.fromRaw(c.categoryRaw)
                TodayGoalUi(
                    key = c.intakeKey, emoji = c.emoji, unit = c.unit, increment = c.increment,
                    intake = intakeMap[c.intakeKey] ?: 0, target = c.target,
                    isRestDay = c.intakeKey in restDays,
                    supportsRestDay = category == GoalCategory.Treino,
                    category = category,
                    title = c.name,
                )
            }
            val allGoals = builtinGoals + customGoalsUi

            // Agrupa por categoria (Essencial → … → Suplemento); custom sem categoria vão no fim.
            val sections = GoalCategory.entries.mapNotNull { category ->
                val goals = allGoals.filter { it.category == category }
                if (goals.isEmpty()) null
                else TodayCategoryUi(category, goals, collapsed = category.rawValue in collapsed)
            }
            val uncategorized = allGoals.filter { it.category == null }

            val avg = if (allGoals.isEmpty()) 0.0 else allGoals.sumOf { it.progress } / allGoals.size

            TodayUiState(
                dateLabel = AppDateFormatters.longDate(LocalDate.now()),
                overallPercent = floor(avg * 100).toInt(),
                tierEmoji = theme.emoji(DailyAchievement.from(avg)),
                overallProgress = avg.toFloat(),
                sections = sections,
                uncategorizedGoals = uncategorized,
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

    fun toggleRestDay(goal: TodayGoalUi) {
        viewModelScope.launch { intakeRepo.toggleRestDay(goal.key) }
    }

    companion object {
        // Perfil-demo temporário até a tela de Welcome existir (Fase 4).
        private val DEMO_PROFILE = Profile(
            weightKg = 80.0, heightCm = 180, age = 30, sex = "male", goal = UserGoal.Maintenance,
        )
    }
}
