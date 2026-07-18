package com.jonathaxs.gymnutshell.wear.today

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.jonathaxs.gymnutshell.core.data.CustomGoalRepository
import com.jonathaxs.gymnutshell.core.data.IntakeRepository
import com.jonathaxs.gymnutshell.core.data.ProfileRepository
import com.jonathaxs.gymnutshell.core.data.SettingsRepository
import com.jonathaxs.gymnutshell.core.domain.BuiltInGoals
import com.jonathaxs.gymnutshell.core.domain.DailyAchievement
import com.jonathaxs.gymnutshell.core.domain.GoalCategory
import com.jonathaxs.gymnutshell.core.domain.GoalsProvider
import com.jonathaxs.gymnutshell.core.domain.Profile
import com.jonathaxs.gymnutshell.core.domain.ProgressHelpers
import com.jonathaxs.gymnutshell.core.domain.UserGoal
import com.jonathaxs.gymnutshell.core.theme.AccentColor
import com.jonathaxs.gymnutshell.wear.sync.WatchWearSync
import com.jonathaxs.gymnutshell.wear.tile.ProgressTileService
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import kotlin.math.floor

/** Uma meta pronta pra exibir no card do relógio. */
data class WearGoalUi(
    val key: String,
    val emoji: String,
    /** Título já resolvido (metas custom); null = built-in (UI resolve via string). */
    val title: String?,
    val unit: String,
    val increment: Int,
    val intake: Int,
    val target: Int,
    val isRestDay: Boolean,
    val supportsRestDay: Boolean,
) {
    // Em dia de descanso a meta conta como 100%, igual ao celular.
    val progress: Double get() = if (isRestDay) 1.0 else ProgressHelpers.normalizedProgress(intake, target)
}

/** Estado da página Today do relógio. */
data class WearTodayUiState(
    val overallProgress: Float = 0f,
    val overallPercent: Int = 0,
    val tierEmoji: String = "🐓",
    val accentArgb: Long = AccentColor.Default.argb,
    val goals: List<WearGoalUi> = emptyList(),
)

/**
 * ViewModel da Today do relógio — porte da ContentView do Watch app (iOS).
 * Mesma matemática do TodayViewModel do celular (metas built-in + custom, média,
 * tier), mas sem Health Connect, notificações e colapso de categorias.
 * Lê/escreve os repos locais do :core; o sync com o celular chega nas fatias 7C/7D.
 */
class WearTodayViewModel(app: Application) : AndroidViewModel(app) {

    private val profileRepo = ProfileRepository(app.applicationContext)
    private val intakeRepo = IntakeRepository(app.applicationContext)
    private val settingsRepo = SettingsRepository(app.applicationContext)
    private val customGoalRepo = CustomGoalRepository(app.applicationContext)

    init {
        viewModelScope.launch { rolloverIfNeeded() }
    }

    /**
     * Virada de dia no relógio: só zera o estado local. Gravar DailyRecord, preencher
     * dias perdidos e avaliar bônus é papel do celular (igual à divisão do iOS, onde
     * o iPhone é a fonte da verdade do histórico).
     */
    private suspend fun rolloverIfNeeded() {
        val today = LocalDate.now().toEpochDay()
        val last = intakeRepo.lastActiveDay()
        if (last == null) {
            intakeRepo.setLastActiveDay(today)
            return
        }
        if (last >= today) return
        intakeRepo.resetAllIntakes()
        intakeRepo.setLastActiveDay(today)
    }

    val uiState: StateFlow<WearTodayUiState> =
        combine(
            combine(profileRepo.profile, customGoalRepo.goals, settingsRepo.accentColor) { p, c, a ->
                Triple(p, c, a)
            },
            intakeRepo.intakes,
            settingsRepo.theme,
            intakeRepo.restDays,
        ) { (profile, customGoals, accent), intakeMap, theme, restDays ->
            val effective = if (profile.weightKg <= 0.0) DEMO_PROFILE else profile

            val builtin = BuiltInGoals.forResult(GoalsProvider.goals(effective)).map { g ->
                val category = GoalCategory.defaultCategory(g.key)
                WearGoalUi(
                    key = g.key,
                    emoji = g.emoji,
                    title = null,
                    unit = g.unit,
                    increment = g.increment,
                    intake = intakeMap[g.key] ?: 0,
                    target = g.target,
                    isRestDay = g.key in restDays,
                    supportsRestDay = category == GoalCategory.Treino,
                ) to category
            }
            val custom = customGoals.map { c ->
                val category = GoalCategory.fromRaw(c.categoryRaw)
                WearGoalUi(
                    key = c.intakeKey,
                    emoji = c.emoji,
                    title = c.name,
                    unit = c.unit,
                    increment = c.increment,
                    intake = intakeMap[c.intakeKey] ?: 0,
                    target = c.target,
                    isRestDay = c.intakeKey in restDays,
                    supportsRestDay = category == GoalCategory.Treino,
                ) to category
            }
            // Lista achatada na mesma ordem do celular: categoria a categoria, custom sem categoria no fim.
            val allGoals = builtin + custom
            val ordered = GoalCategory.entries.flatMap { cat ->
                allGoals.filter { it.second == cat }.map { it.first }
            } + allGoals.filter { it.second == null }.map { it.first }

            val avg = if (ordered.isEmpty()) 0.0 else ordered.sumOf { it.progress } / ordered.size

            WearTodayUiState(
                overallProgress = avg.toFloat(),
                overallPercent = floor(avg * 100).toInt(),
                tierEmoji = theme.emoji(DailyAchievement.from(avg), profile.sex),
                accentArgb = accent.argb,
                goals = ordered,
            )
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), WearTodayUiState())

    /**
     * Soma/subtrai um incremento, limitado entre 0 e o alvo (igual ao WatchGoalCard
     * do iOS), e publica o delta pro celular via Data Layer.
     */
    fun adjustIntake(goal: WearGoalUi, direction: Int) {
        val next = (goal.intake + direction * goal.increment).coerceIn(0, goal.target)
        viewModelScope.launch {
            intakeRepo.setIntake(goal.key, next)
            WatchWearSync.sendIntake(getApplication(), goal.key, next)
            // Tile acompanha incrementos feitos no próprio relógio (igual à complication do iOS).
            ProgressTileService.requestUpdate(getApplication())
        }
    }

    fun toggleRestDay(goal: WearGoalUi) {
        val nowActive = !goal.isRestDay
        viewModelScope.launch {
            intakeRepo.toggleRestDay(goal.key)
            WatchWearSync.sendRestDay(getApplication(), goal.key, nowActive)
            ProgressTileService.requestUpdate(getApplication())
        }
    }

    private companion object {
        // Mesmo perfil-demo do celular enquanto não há onboarding no relógio.
        val DEMO_PROFILE = Profile(
            weightKg = 80.0, heightCm = 180, age = 30, sex = "male", goal = UserGoal.Maintenance,
        )
    }
}
