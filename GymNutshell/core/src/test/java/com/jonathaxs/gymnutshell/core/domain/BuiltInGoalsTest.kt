package com.jonathaxs.gymnutshell.core.domain

import org.junit.Assert.assertEquals
import org.junit.Test

class BuiltInGoalsTest {

    @Test
    fun `gera as 10 metas fixas usando os alvos do Result`() {
        val result = GoalsCalculator.calculate(80.0, 180, 30, "male", UserGoal.Maintenance)
        val goals = BuiltInGoals.forResult(result)

        assertEquals(10, goals.size)

        val water = goals.first { it.key == "tracking.water" }
        assertEquals(result.water, water.target)
        assertEquals("💧", water.emoji)
        assertEquals(250, water.increment)

        val protein = goals.first { it.key == "tracking.protein" }
        assertEquals(result.protein, protein.target)
    }

    @Test
    fun `ordem segue as categorias - Essencial antes de Treino`() {
        val goals = BuiltInGoals.forResult(GoalsCalculator.calculate(80.0, 180, 30, "male", UserGoal.Maintenance))
        val keys = goals.map { it.key }

        // Essencial (sleep/water) vem primeiro
        assertEquals("tracking.sleep", keys.first())
        // Treino (workout/cardio) vem DEPOIS de Nutrição (protein) — o bug histórico do iOS
        assert(keys.indexOf("tracking.workout") > keys.indexOf("tracking.protein"))
        assert(keys.indexOf("tracking.water") < keys.indexOf("tracking.workout"))
    }
}
