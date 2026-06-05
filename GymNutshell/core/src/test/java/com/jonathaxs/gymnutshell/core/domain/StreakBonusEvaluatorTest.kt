package com.jonathaxs.gymnutshell.core.domain

import com.jonathaxs.gymnutshell.core.data.DailyRecord
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.temporal.TemporalAdjusters

class StreakBonusEvaluatorTest {

    // Um sábado garantido, pra ancorar a semana.
    private val saturday: LocalDate =
        LocalDate.of(2026, 1, 1).with(TemporalAdjusters.nextOrSame(DayOfWeek.SATURDAY))

    /** Semana completa (domingo..sábado) com o mesmo percent. */
    private fun week(percent: Int): List<DailyRecord> =
        (0L..6L).map { offset ->
            DailyRecord(date = saturday.minusDays(6 - offset).toEpochDay(), percent = percent)
        }

    @Test
    fun `semana toda nivel 4 premia weekly_level4`() {
        val award = StreakBonusEvaluator.evaluate(week(95), emptySet()).single()
        assertEquals("weekly.level4", award.bonusType)
        assertEquals(800, award.points)
        assertEquals("💀", award.emoji)
        assertEquals(saturday.toEpochDay(), award.anchorEpochDay)
    }

    @Test
    fun `semana toda nivel 3 premia weekly_level3`() {
        val award = StreakBonusEvaluator.evaluate(week(70), emptySet()).single()
        assertEquals("weekly.level3", award.bonusType)
        assertEquals(400, award.points)
        assertEquals("🎖️", award.emoji)
    }

    @Test
    fun `um dia fraco na semana nao premia`() {
        val records = week(95).toMutableList()
        records[3] = records[3].copy(percent = 50)
        assertTrue(StreakBonusEvaluator.evaluate(records, emptySet()).isEmpty())
    }

    @Test
    fun `semana incompleta nao premia`() {
        val records = week(95).drop(1) // só 6 dias
        assertTrue(StreakBonusEvaluator.evaluate(records, emptySet()).isEmpty())
    }

    @Test
    fun `ancora ja premiada e ignorada`() {
        val awarded = setOf(saturday.toEpochDay())
        assertTrue(StreakBonusEvaluator.evaluate(week(95), awarded).isEmpty())
    }

    @Test
    fun `mes completo nivel 4 premia monthly_level4`() {
        val ym = YearMonth.of(2026, 2) // fevereiro 2026 = 28 dias
        val records = (1..ym.lengthOfMonth()).map {
            DailyRecord(date = ym.atDay(it).toEpochDay(), percent = 95)
        }
        val monthly = StreakBonusEvaluator.evaluate(records, emptySet())
            .single { it.bonusType.startsWith("monthly") }
        assertEquals("monthly.level4", monthly.bonusType)
        assertEquals(5000, monthly.points)
        assertEquals("☠️", monthly.emoji)
        assertEquals(ym.atEndOfMonth().toEpochDay(), monthly.anchorEpochDay)
    }
}
