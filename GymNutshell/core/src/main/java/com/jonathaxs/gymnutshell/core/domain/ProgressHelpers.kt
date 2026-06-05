package com.jonathaxs.gymnutshell.core.domain

/**
 * Utilitários de progresso normalizado (fixado em 0..1) — porte de ProgressHelpers (iOS).
 * `coerceIn` faz o papel do `min(max(...))` do Swift.
 */
object ProgressHelpers {

    fun normalizedProgress(current: Int, goal: Int): Double = clampedProgress(current, goal)

    fun clampedProgress(current: Int, goal: Int): Double {
        if (goal <= 0) return 0.0
        return (current.toDouble() / goal.toDouble()).coerceIn(0.0, 1.0)
    }

    fun normalizedProgress(current: Double, goal: Double): Double = clampedProgress(current, goal)

    fun clampedProgress(current: Double, goal: Double): Double {
        if (goal <= 0.0) return 0.0
        return (current / goal).coerceIn(0.0, 1.0)
    }
}
