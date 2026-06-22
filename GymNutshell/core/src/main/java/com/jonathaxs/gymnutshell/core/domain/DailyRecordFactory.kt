package com.jonathaxs.gymnutshell.core.domain

import com.jonathaxs.gymnutshell.core.data.CustomGoal
import com.jonathaxs.gymnutshell.core.data.DailyRecord
import kotlin.math.floor

/**
 * Monta o DailyRecord de um dia — porte da lógica de finishSpecificDay (iOS).
 * Puro/testável: recebe os intakes + as metas calculadas e devolve o registro pronto.
 * O % e o tier vêm da média dos progressos das metas fixas (mesma regra da TodayView).
 */
object DailyRecordFactory {

    // Metas built-in que NÃO têm coluna própria no DailyRecord, então persistem em customValues
    // (água/proteína/carbo/gordura/fibra/sono já têm coluna; treino/cardio guardam só o booleano).
    private val builtInExtraKeys = listOf(
        "tracking.calories", "tracking.workout", "tracking.cardio", "tracking.creatine",
    )

    /** Registro de um dia concluído, com os intakes informados e o emoji do tema escolhido. */
    fun build(
        epochDay: Long,
        intakes: Map<String, Int>,
        result: GoalsCalculator.Result,
        theme: AppTheme,
        restDays: Set<String> = emptySet(),
        customGoals: List<CustomGoal> = emptyList(),
    ): DailyRecord {
        // Meta em dia de descanso conta como 100% (1.0), sem precisar de intake.
        fun progress(key: String, target: Int): Double =
            if (key in restDays) 1.0 else ProgressHelpers.normalizedProgress(intakes[key] ?: 0, target)

        val progresses = BuiltInGoals.forResult(result).map { progress(it.key, it.target) } +
            customGoals.map { progress(it.intakeKey, it.target) }
        val avg = if (progresses.isEmpty()) 0.0 else progresses.average()
        val tier = DailyAchievement.from(avg)

        // Guarda os intakes que não têm coluna própria (built-in extras + metas custom),
        // pra que o dia seja reconstruível na edição — espelha o customValues do iOS.
        val storedValues = buildMap {
            builtInExtraKeys.forEach { put(it, intakes[it] ?: 0) }
            customGoals.forEach { put(it.intakeKey, intakes[it.intakeKey] ?: 0) }
        }
        // Só os dias de descanso das metas custom (treino/cardio built-in têm campo próprio).
        val customRest = customGoals
            .filter { it.intakeKey in restDays }
            .associate { it.intakeKey to true }

        return DailyRecord(
            date = epochDay,
            water = intakes["tracking.water"] ?: 0,
            protein = intakes["tracking.protein"] ?: 0,
            carbs = intakes["tracking.carbs"] ?: 0,
            goodFat = intakes["tracking.goodFat"] ?: 0,
            fiber = intakes["tracking.fiber"] ?: 0,
            sleep = intakes["tracking.sleep"] ?: 0,
            customValues = DailyRecordCodec.encodeIntMap(storedValues),
            // Dia de descanso NÃO conta como dia de atividade (igual iOS).
            didWorkout = (intakes["tracking.workout"] ?: 0) > 0,
            didCardio = (intakes["tracking.cardio"] ?: 0) > 0,
            workoutRestDay = "tracking.workout" in restDays,
            cardioRestDay = "tracking.cardio" in restDays,
            customRestDays = DailyRecordCodec.encodeBoolMap(customRest),
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
