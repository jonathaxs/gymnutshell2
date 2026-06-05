package com.jonathaxs.gymnutshell.core.domain

import kotlin.math.roundToInt

/**
 * Calcula as metas diárias recomendadas — porte de GoalsCalculator (iOS).
 * Calorias via Mifflin-St Jeor (peso, altura, idade, sexo) → TDEE (×1.55) → ajuste por objetivo.
 * Macros/hidratação/creatina escalam com o peso; fibra deriva das calorias (~14 g/1000 kcal).
 */
object GoalsCalculator {

    /** Todas as metas diárias calculadas pra um perfil. */
    data class Result(
        val calories: Int,  // kcal
        val water: Int,     // ml
        val protein: Int,   // g
        val carbs: Int,     // g
        val goodFat: Int,   // g
        val fiber: Int,     // g
        val sleep: Int,     // h (fixo)
        val creatine: Int,  // g
        val workout: Int,   // min (fixo)
        val cardio: Int,    // min (fixo)
    )

    fun calculate(weightKg: Double, heightCm: Int, age: Int, sex: String, goal: UserGoal): Result {
        val calories = calculatedCalories(weightKg, heightCm, age, sex, goal)
        return Result(
            calories = calories,
            water = calculatedWater(weightKg),
            protein = calculatedProtein(weightKg, goal),
            carbs = calculatedCarbs(weightKg, goal),
            goodFat = calculatedGoodFat(weightKg, goal),
            fiber = calculatedFiber(calories),
            sleep = DefaultGoals.SLEEP,
            creatine = calculatedCreatine(weightKg),
            workout = DefaultGoals.WORKOUT,
            cardio = DefaultGoals.CARDIO,
        )
    }

    // BMR = 10·kg + 6.25·cm − 5·idade + constante de sexo (♂ +5 / ♀ −161 / −78 neutro).
    // TDEE = BMR × 1.55 (atividade moderada). Ajuste: cutting −20%, manutenção 0, bulking +15%.
    private fun calculatedCalories(weightKg: Double, heightCm: Int, age: Int, sex: String, goal: UserGoal): Int {
        val h = if (heightCm > 0) heightCm.toDouble() else 170.0
        val a = if (age > 0) age.toDouble() else 30.0
        val sexConstant = when (sex.lowercase()) {
            "female" -> -161.0
            "male" -> 5.0
            else -> -78.0
        }
        val bmr = 10 * weightKg + 6.25 * h - 5 * a + sexConstant
        val tdee = bmr * 1.55
        val adjusted = when (goal) {
            UserGoal.Cutting -> tdee * 0.80
            UserGoal.Maintenance -> tdee
            UserGoal.Bulking -> tdee * 1.15
        }
        return roundToNearest(adjusted, 50)
    }

    private fun calculatedWater(weightKg: Double): Int {
        val rounded = (weightKg * 35 / 250).roundToInt() * 250
        return maxOf(rounded, 1500)
    }

    private fun calculatedProtein(weightKg: Double, goal: UserGoal): Int {
        val multiplier = if (goal == UserGoal.Cutting) 2.2 else 2.0
        return roundToNearest(weightKg * multiplier, 5)
    }

    private fun calculatedCarbs(weightKg: Double, goal: UserGoal): Int {
        val multiplier = when (goal) {
            UserGoal.Bulking -> 4.5
            UserGoal.Maintenance -> 3.5
            UserGoal.Cutting -> 2.5
        }
        return roundToNearest(weightKg * multiplier, 10)
    }

    private fun calculatedGoodFat(weightKg: Double, goal: UserGoal): Int {
        val multiplier = when (goal) {
            UserGoal.Bulking -> 1.1
            UserGoal.Maintenance -> 0.9
            UserGoal.Cutting -> 0.7
        }
        return roundToNearest(weightKg * multiplier, 5)
    }

    private fun calculatedFiber(calories: Int): Int {
        val raw = calories / 1000.0 * 14
        return maxOf(roundToNearest(raw, 1), 21)
    }

    private fun calculatedCreatine(weightKg: Double): Int {
        if (weightKg <= 0) return DefaultGoals.CREATINE
        return (weightKg * 0.05).roundToInt().coerceIn(3, 10)
    }

    /** Arredonda pro múltiplo de `step` mais próximo, com piso = step. */
    private fun roundToNearest(value: Double, step: Int): Int =
        maxOf((value / step).roundToInt() * step, step)
}
