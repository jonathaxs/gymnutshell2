package com.jonathaxs.gymnutshell.core.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DailyRecordFactoryTest {

    private val result = GoalsCalculator.calculate(80.0, 180, 30, "male", UserGoal.Maintenance)

    @Test
    fun `build com todas as metas no alvo gera 100 por cento e tier Level4`() {
        val full = BuiltInGoals.forResult(result).associate { it.key to it.target }
        val rec = DailyRecordFactory.build(epochDay = 100L, intakes = full, result = result, theme = AppTheme.Gym)

        assertEquals(100L, rec.date)
        assertEquals(100, rec.percent)
        assertEquals(DailyAchievement.Level4.points, rec.points)
        assertEquals(result.water, rec.water)
        assertEquals(result.protein, rec.protein)
        assertTrue(rec.didWorkout)
        assertTrue(rec.didCardio)
    }

    @Test
    fun `missed gera registro Level1 zerado sem atividade`() {
        val rec = DailyRecordFactory.missed(epochDay = 50L, theme = AppTheme.Gym)
        assertEquals(50L, rec.date)
        assertEquals(0, rec.percent)
        assertEquals(DailyAchievement.Level1.points, rec.points) // 0
        assertFalse(rec.didWorkout)
    }
}
