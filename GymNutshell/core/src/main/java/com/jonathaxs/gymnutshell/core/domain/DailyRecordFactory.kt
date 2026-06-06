package com.jonathaxs.gymnutshell.core.domain

import com.jonathaxs.gymnutshell.core.data.DailyRecord
import kotlin.math.floor

/**
 * Monta o DailyRecord de um dia — porte da lógica de finishSpecificDay (iOS).
 * Puro/testável: recebe os intakes + as metas calculadas e devolve o registro pronto.
 * O % e o tier vêm da média dos progressos das metas fixas (mesma regra da TodayView).
 */
object DailyRecordFactory {

    /** Registro de um dia concluído, com os intakes informados. */
    fun build(epochDay: Long, intakes: Map<String, Int>, result: GoalsCalculator.Result): DailyRecord {
        val goals = BuiltInGoals.forResult(result)
        val avg = if (goals.isEmpty()) 0.0
        else goals.sumOf { ProgressHelpers.normalizedProgress(intakes[it.key] ?: 0, it.target) } / goals.size
        val tier = DailyAchievement.from(avg)

        return DailyRecord(
            date = epochDay,
            water = intakes["tracking.water"] ?: 0,
            protein = intakes["tracking.protein"] ?: 0,
            carbs = intakes["tracking.carbs"] ?: 0,
            goodFat = intakes["tracking.goodFat"] ?: 0,
            fiber = intakes["tracking.fiber"] ?: 0,
            sleep = intakes["tracking.sleep"] ?: 0,
            didWorkout = (intakes["tracking.workout"] ?: 0) > 0,
            didCardio = (intakes["tracking.cardio"] ?: 0) > 0,
            percent = floor(avg * 100).toInt(),
            achievementEmoji = tier.emoji, // nome do tier (achievementTitle) vem com os temas (fatia 7)
            points = tier.points,
        )
    }

    /** Registro "vazio" de um dia perdido (Level1), igual ao sadRecord do iOS. */
    fun missed(epochDay: Long): DailyRecord {
        val tier = DailyAchievement.Level1
        return DailyRecord(
            date = epochDay,
            percent = 0,
            achievementEmoji = tier.emoji,
            points = tier.points,
        )
    }
}
