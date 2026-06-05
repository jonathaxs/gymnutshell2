package com.jonathaxs.gymnutshell.core.domain

/**
 * Níveis de conquista diária — porte de DailyAchievement (iOS/GymNutshellCore).
 *
 * Aqui ficam só a lógica pura (tier por progresso, pontos). A exibição (emoji/nome localizado),
 * que no iOS dependia do AppTheme, entra junto com o porte de temas + strings.
 */
enum class DailyAchievement {
    Level1,
    Level2,
    Level3,
    Level4;

    /** Pontos que o tier vale — valores idênticos ao iOS. */
    val points: Int
        get() = when (this) {
            Level1 -> 0
            Level2 -> 40
            Level3 -> 60
            Level4 -> 90
        }

    companion object {
        /**
         * Converte um progresso normalizado (0..1) no tier correspondente.
         * Thresholds idênticos ao iOS: <0.33 = L1, <0.66 = L2, <0.9 = L3, senão L4.
         */
        fun from(progress: Double): DailyAchievement = when {
            progress < 0.33 -> Level1
            progress < 0.66 -> Level2
            progress < 0.9 -> Level3
            else -> Level4
        }
    }
}
