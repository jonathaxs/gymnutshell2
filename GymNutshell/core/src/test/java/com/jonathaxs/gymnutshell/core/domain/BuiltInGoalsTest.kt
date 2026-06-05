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
}
