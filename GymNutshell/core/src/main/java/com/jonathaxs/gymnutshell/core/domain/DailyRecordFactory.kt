package com.jonathaxs.gymnutshell.core.domain

import com.jonathaxs.gymnutshell.core.data.DailyRecord
import kotlin.math.floor

/**
 * Monta o DailyRecord de um dia — porte da lógica de finishSpecificDay (iOS).
 * Puro/testável: recebe os intakes + as metas calculadas e devolve o registro pronto.
 * O % e o tier vêm da média dos progressos das metas fixas (mesma regra da TodayView).
 */
object DailyRecordFactory {

    /** Registro de um dia concluído, com os intakes informados e o emoji do tema escolhido. */
    fun build(
        epochDay: Long,
        intakes: Map<String, Int>,
        result: GoalsCalculator.Result,
        theme: AppTheme,
        restDays: Set<String> = emptySet(),
    ): DailyRecord {
        val goals = BuiltInGoals.forResult(result)
        // Meta em dia de descanso conta como 100% (1.0), sem precisar de intake.
        val avg = if (goals.isEmpty()) 0.0
        else goals.sumOf { g ->
            if (g.key in restDays) 1.0 else ProgressHelpers.normalizedProgress(intakes[g.key] ?: 0, g.target)
        } / goals.size
        val tier = DailyAchievement.from(avg)

        return DailyRecord(
            date = epochDay,
            water = intakes["tracking.water"] ?: 0,
            protein = intakes["tracking.protein"] ?: 0,
            carbs = intakes["tracking.carbs"] ?: 0,
            goodFat = intakes["tracking.goodFat"] ?: 0,
            fiber = intakes["tracking.fiber"] ?: 0,
            sleep = intakes["tracking.sleep"] ?: 0,
            // Dia de descanso NÃO conta como dia de atividade (igual iOS).
            didWorkout = (intakes["tracking.workout"] ?: 0) > 0,
            didCardio = (intakes["tracking.cardio"] ?: 0) > 0,
            workoutRestDay = "tracking.workout" in restDays,
            cardioRestDay = "tracking.cardio" in restDays,
            percent = floor(avg * 100).toInt(),
            achievementEmoji = theme.emoji(tier), // congela o emoji do tema no dia
            points = tier.points,
        )
    }

    /** Registro "vazio" de um dia perdido (Level1), igual ao sadRecord do iOS. */
    fun missed(epochDay: Long, theme: AppTheme): DailyRecord {
        val tier = DailyAchievement.Level1
        return DailyRecord(
            date = epochDay,
            percent = 0,
            achievementEmoji = theme.emoji(tier),
            points = tier.points,
        )
    }
}
