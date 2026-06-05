package com.jonathaxs.gymnutshell.core.domain

/**
 * Descritor de uma meta fixa da TodayView — porte das linhas hardcoded do goalRow (iOS).
 * O `key` identifica a meta (ex.: "tracking.water"); o título localizado é resolvido na UI.
 * `target` vem das metas calculadas (GoalsCalculator.Result).
 */
data class BuiltInGoal(
    val key: String,
    val emoji: String,
    val unit: String,
    val increment: Int,
    val target: Int,
)

/** Monta a lista das 10 metas fixas a partir das metas calculadas do perfil. */
object BuiltInGoals {

    // Ordem segue a GoalCategory: Essencial → Nutrição → Treino → Suplemento.
    // (Corrige o bug histórico do iOS em que Treino aparecia acima das Essenciais.)
    fun forResult(result: GoalsCalculator.Result): List<BuiltInGoal> = listOf(
        // Essencial
        BuiltInGoal("tracking.sleep", "💤", "h", 1, result.sleep),
        BuiltInGoal("tracking.water", "💧", "ml", 250, result.water),
        // Nutrição
        BuiltInGoal("tracking.calories", "🔥", "kcal", DefaultGoals.CALORIES_INCREMENT, result.calories),
        BuiltInGoal("tracking.protein", "🍗", "g", 20, result.protein),
        BuiltInGoal("tracking.carbs", "🍞", "g", 20, result.carbs),
        BuiltInGoal("tracking.goodFat", "🧈", "g", 5, result.goodFat),
        BuiltInGoal("tracking.fiber", "🌾", "g", 5, result.fiber),
        // Treino
        BuiltInGoal("tracking.workout", "🏋️", "min", DefaultGoals.WORKOUT_INCREMENT, result.workout),
        BuiltInGoal("tracking.cardio", "🏃", "min", DefaultGoals.CARDIO_INCREMENT, result.cardio),
        // Suplemento
        BuiltInGoal("tracking.creatine", "🧪", "g", DefaultGoals.CREATINE_INCREMENT, result.creatine),
    )
}
