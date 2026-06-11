package com.jonathaxs.gymnutshell.core.sync

import com.jonathaxs.gymnutshell.core.data.DailyRecord
import org.junit.Assert.assertEquals
import org.junit.Test

/** Cálculo puro do resumo de estatísticas (porte do init de WatchStatsSummary do iOS). */
class WearSnapshotSyncTest {

    private fun record(date: Long, percent: Int, points: Int = 0, workout: Boolean = false, cardio: Boolean = false) =
        DailyRecord(
            date = date, percent = percent, points = points,
            didWorkout = workout, didCardio = cardio, achievementEmoji = "💪",
        )

    @Test
    fun `agrega pontos com bonus e conta dias por tier`() {
        val today = 20_600L
        val records = listOf(
            record(today - 3, percent = 100, points = 90, workout = true, cardio = true), // L4
            record(today - 2, percent = 70, points = 60, workout = true), // L3
            record(today - 1, percent = 10), // L1
        )
        val stats = WearSnapshotSync.computeStats(records, bonusPoints = 50, today = today)

        assertEquals(3, stats.totalDays)
        assertEquals(90 + 60 + 0 + 50, stats.totalPoints)
        assertEquals(1, stats.level1Days)
        assertEquals(0, stats.level2Days)
        assertEquals(1, stats.level3Days)
        assertEquals(1, stats.level4Days)
        assertEquals(2, stats.workoutDays)
        assertEquals(1, stats.cardioDays)
    }

    @Test
    fun `faixa de 7 dias vai do mais antigo ao mais recente com buracos vazios`() {
        val today = 20_600L
        val records = listOf(record(today - 1, percent = 80), record(today - 6, percent = 30))
        val stats = WearSnapshotSync.computeStats(records, bonusPoints = 0, today = today)

        assertEquals(7, stats.last7Days.size)
        assertEquals(WearDayEntry("💪", 30), stats.last7Days.first()) // today-6
        assertEquals(WearDayEntry("💪", 80), stats.last7Days[5]) // today-1
        assertEquals(WearDayEntry("", 0), stats.last7Days.last()) // hoje sem registro
        assertEquals(WearDayEntry("", 0), stats.last7Days[1]) // buraco
    }
}
