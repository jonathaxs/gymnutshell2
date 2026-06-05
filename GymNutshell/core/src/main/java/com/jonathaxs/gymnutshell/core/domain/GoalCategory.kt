package com.jonathaxs.gymnutshell.core.domain

/**
 * Categorias que agrupam as metas — porte de GoalCategory (iOS).
 * A ordem das constantes define a ordem de exibição (Essencial primeiro).
 */
enum class GoalCategory(val rawValue: String) {
    Essencial("essencial"),
    Nutricao("nutricao"),
    Treino("treino"),
    Suplemento("suplemento");

    companion object {
        fun fromRaw(raw: String?): GoalCategory? = entries.firstOrNull { it.rawValue == raw }

        /** Categoria padrão de uma chave de meta fixa — espelha defaultCategory(for:) do iOS. */
        fun defaultCategory(key: String): GoalCategory? = when (key) {
            "tracking.sleep", "tracking.water" -> Essencial
            "tracking.calories", "tracking.protein", "tracking.carbs",
            "tracking.goodFat", "tracking.fiber" -> Nutricao
            "tracking.workout", "tracking.cardio" -> Treino
            "tracking.creatine" -> Suplemento
            else -> null
        }
    }
}
