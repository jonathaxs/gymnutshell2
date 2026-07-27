package com.jonathaxs.gymnutshell.core.widget

import android.content.Context
import com.jonathaxs.gymnutshell.core.data.CustomGoal
import com.jonathaxs.gymnutshell.core.data.CustomGoalRepository
import com.jonathaxs.gymnutshell.core.data.DailyRecord
import com.jonathaxs.gymnutshell.core.data.DailyRecordRepository
import com.jonathaxs.gymnutshell.core.data.GoalConfigRepository
import com.jonathaxs.gymnutshell.core.data.IntakeRepository
import com.jonathaxs.gymnutshell.core.data.ProfileRepository
import com.jonathaxs.gymnutshell.core.data.SettingsRepository
import com.jonathaxs.gymnutshell.core.data.WidgetBackgroundRepository
import com.jonathaxs.gymnutshell.core.domain.AppTheme
import com.jonathaxs.gymnutshell.core.domain.BuiltInGoals
import com.jonathaxs.gymnutshell.core.domain.DailyAchievement
import com.jonathaxs.gymnutshell.core.domain.GoalConfig
import com.jonathaxs.gymnutshell.core.domain.GoalsCalculator
import com.jonathaxs.gymnutshell.core.domain.GoalsProvider
import com.jonathaxs.gymnutshell.core.domain.Profile
import com.jonathaxs.gymnutshell.core.domain.ProgressHelpers
import com.jonathaxs.gymnutshell.core.domain.UserGoal
import kotlinx.coroutines.flow.first
import java.time.LocalDate
import kotlin.math.floor

/**
 * Monta o [WidgetSnapshot] do dia — porte de WidgetSnapshot.buildCurrent (iOS).
 *
 * [compute] é puro/testável (recebe os dados prontos, mesmo padrão do DailyRecordFactory);
 * [build] lê os repositórios (DataStore + Room) no processo do app e delega. O cálculo de % e
 * tier espelha exatamente a TodayViewModel/DailyRecordFactory, então o widget bate com a tela Hoje.
 */
object WidgetSnapshotBuilder {

    /** Lê o estado atual dos repositórios e monta o snapshot. Chamar dentro do provideGlance. */
    suspend fun build(context: Context): WidgetSnapshot {
        val profile = ProfileRepository(context).profile.first()
        val settings = SettingsRepository(context)
        val intakeRepo = IntakeRepository(context)

        val bgRepo = WidgetBackgroundRepository(context)

        // Mesmo fallback da TodayViewModel: sem peso salvo, usa o perfil-demo pra não zerar as metas.
        val effective = if (profile.weightKg <= 0.0) DEMO_PROFILE else profile
        return compute(
            result = GoalsProvider.goals(effective),
            intakes = intakeRepo.intakes.first(),
            restDays = intakeRepo.restDays.first(),
            customGoals = CustomGoalRepository(context).all(),
            records = DailyRecordRepository(context).allRecords(),
            theme = settings.theme.first(),
            sex = profile.sex,
            accentArgb = settings.accentColor.first().argb,
            backgroundMode = bgRepo.mode(),
            customBackgroundArgb = bgRepo.customColor(),
            config = GoalConfigRepository(context).goalConfig.first(),
        )
    }

    /** Núcleo puro: calcula progresso, tier, metas e janela recente a partir de dados prontos. */
    fun compute(
        result: GoalsCalculator.Result,
        intakes: Map<String, Int>,
        restDays: Set<String>,
        customGoals: List<CustomGoal>,
        records: List<DailyRecord>,
        theme: AppTheme,
        sex: String = "male",
        accentArgb: Long,
        backgroundMode: WidgetBackgroundMode = WidgetBackgroundMode.Default,
        customBackgroundArgb: Long = WidgetBackground.DEFAULT_CUSTOM_ARGB,
        config: GoalConfig = GoalConfig(),
        today: Long = LocalDate.now().toEpochDay(),
        nowMillis: Long = System.currentTimeMillis(),
    ): WidgetSnapshot {
        // Meta em dia de descanso conta como 100% (igual TodayView/DailyRecordFactory).
        fun progress(key: String, target: Int): Double =
            if (key in restDays) 1.0 else ProgressHelpers.normalizedProgress(intakes[key] ?: 0, target)

        fun percent(p: Double): Int = floor(p * 100).toInt()

        // Metas ativas: ordem definida pelo usuário, sem as removidas e com os overrides aplicados.
        val builtin = BuiltInGoals.active(result, config)

        // Metas ativas na mesma ordem da Today: fixas (por categoria) + personalizadas no fim.
        // Metas custom carregam o próprio nome em `label`; as fixas deixam null (a UI resolve por key).
        val goals = builtin.map { GoalProgress(it.key, it.emoji, percent(progress(it.key, it.target))) } +
            customGoals.map { GoalProgress(it.intakeKey, it.emoji, percent(progress(it.intakeKey, it.target)), label = it.name) }

        // Progresso geral = média das metas ativas (mesma conta da Today/DailyRecordFactory).
        val progresses = builtin.map { progress(it.key, it.target) } +
            customGoals.map { progress(it.intakeKey, it.target) }
        val avg = if (progresses.isEmpty()) 0.0 else progresses.average()
        val tier = DailyAchievement.from(avg)

        // Janela de 35 dias terminando hoje: dias passados vêm do DailyRecord; hoje usa o progresso parcial.
        val byDay = records.associate { it.date to it.percent }
        val recentDays = (0..34).reversed().map { offset ->
            val day = today - offset
            val pct = if (offset == 0) percent(avg) else (byDay[day] ?: 0)
            DaySummary(day, pct)
        }

        return WidgetSnapshot(
            progressNormalized = avg,
            tier = tier.ordinal + 1,
            tierEmoji = theme.emoji(tier, sex),
            tierNameRes = theme.tierNameRes(tier, sex),
            accentArgb = accentArgb,
            updatedAtEpochMillis = nowMillis,
            recentDays = recentDays,
            goals = goals,
            backgroundMode = backgroundMode,
            customBackgroundArgb = customBackgroundArgb,
        )
    }

    /**
     * Perfil-demo usado quando ainda não há peso salvo — espelha o DEMO_PROFILE da TodayViewModel
     * pra o widget mostrar o mesmo que a tela Hoje nesse caso.
     */
    private val DEMO_PROFILE = Profile(
        weightKg = 80.0, heightCm = 180, age = 30, sex = "male", goal = UserGoal.Maintenance,
    )
}
