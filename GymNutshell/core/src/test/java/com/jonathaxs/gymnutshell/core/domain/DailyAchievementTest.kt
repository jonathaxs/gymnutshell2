package com.jonathaxs.gymnutshell.core.domain

import org.junit.Assert.assertEquals
import org.junit.Test

class DailyAchievementTest {

    @Test
    fun `from progress mapeia os tiers nos mesmos limites do iOS`() {
        assertEquals(DailyAchievement.Level1, DailyAchievement.from(0.0))
        assertEquals(DailyAchievement.Level1, DailyAchievement.from(0.329))
        assertEquals(DailyAchievement.Level2, DailyAchievement.from(0.33))
        assertEquals(DailyAchievement.Level2, DailyAchievement.from(0.659))
        assertEquals(DailyAchievement.Level3, DailyAchievement.from(0.66))
        assertEquals(DailyAchievement.Level3, DailyAchievement.from(0.899))
        assertEquals(DailyAchievement.Level4, DailyAchievement.from(0.9))
        assertEquals(DailyAchievement.Level4, DailyAchievement.from(1.0))
    }

    @Test
    fun `pontos de cada tier batem com o iOS`() {
        assertEquals(0, DailyAchievement.Level1.points)
        assertEquals(40, DailyAchievement.Level2.points)
        assertEquals(60, DailyAchievement.Level3.points)
        assertEquals(90, DailyAchievement.Level4.points)
    }
}
