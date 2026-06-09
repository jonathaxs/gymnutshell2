package com.jonathaxs.gymnutshell.core.widget

import com.jonathaxs.gymnutshell.core.data.CustomGoal
import com.jonathaxs.gymnutshell.core.data.DailyRecord
import com.jonathaxs.gymnutshell.core.domain.AppTheme
import com.jonathaxs.gymnutshell.core.domain.BuiltInGoals
import com.jonathaxs.gymnutshell.core.domain.DailyAchievement
import com.jonathaxs.gymnutshell.core.domain.GoalsCalculator
import com.jonathaxs.gymnutshell.core.domain.UserGoal
import com.jonathaxs.gymnutshell.core.theme.AccentColor
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/** Cobre o núcleo puro do builder do snapshot do widget. */
class WidgetSnapshotBuilderTest {

    private val result = GoalsCalculator.calculate(80.0, 180, 30, "male", UserGoal.Maintenance)

    private fun snapshot(
        intakes: Map<String, Int> = emptyMap(),
        restDays: Set<String> = emptySet(),
        customGoals: List<CustomGoal> = emptyList(),
        records: List<DailyRecord> = emptyList(),
        theme: AppTheme = AppTheme.Gym,
        accentArgb: Long = AccentColor.Blue.argb,
    ) = WidgetSnapshotBuilder.compute(
        result = result,
        intakes = intakes,
        restDays = restDays,
        customGoals = customGoals,
        records = records,
        theme = theme,
        accentArgb = accentArgb,
        today = 100L,
    )

    @Test
    fun `todas as metas no alvo gera 100 por cento e tier 4`() {
        val full = BuiltInGoals.forResult(result).associate { it.key to it.target }
        val snap = snapshot(intakes = full)

        assertEquals(100, snap.progressPercent)
        assertEquals(4, snap.tier)
        assertEquals(90, snap.tierPoints)
        assertEquals(AppTheme.Gym.emoji(DailyAchievement.Level4), snap.tierEmoji)
    }

    @Test
    fun `sem intakes gera 0 por cento e tier 1`() {
        val snap = snapshot()

        assertEquals(0, snap.progressPercent)
        assertEquals(1, snap.tier)
        assertEquals(0, snap.tierPoints)
    }

    @Test
    fun `dia de descanso conta como meta cheia`() {
        // Só a meta de treino marcada como descanso; resto zerado.
        val snap = snapshot(restDays = setOf("tracking.workout"))

        val workout = snap.goals.first { it.key == "tracking.workout" }
        assertEquals(100, workout.percent)
    }

    @Test
    fun `metas incluem fixas mais personalizadas na ordem da Today`() {
        val custom = CustomGoal(id = 7, emoji = "📖", name = "Ler", unit = "pág", target = 10, increment = 1)
        val snap = snapshot(intakes = mapOf("custom:7" to 5), customGoals = listOf(custom))

        assertEquals(BuiltInGoals.forResult(result).size + 1, snap.goals.size)
        assertEquals("custom:7", snap.goals.last().key) // personalizada no fim
        assertEquals(50, snap.goals.last().percent)
        assertEquals("Ler", snap.goals.last().label) // custom carrega o próprio nome
        assertNull(snap.goals.first().label) // fixa não tem label (resolve por key na UI)
    }

    @Test
    fun `janela recente tem 35 dias com hoje no fim e usa registro dos dias passados`() {
        val records = listOf(
            DailyRecord(date = 99L, percent = 80), // ontem
            DailyRecord(date = 70L, percent = 40), // dentro da janela
            DailyRecord(date = 10L, percent = 90), // fora da janela (>34 dias)
        )
        val full = BuiltInGoals.forResult(result).associate { it.key to it.target }
        val snap = snapshot(intakes = full, records = records)

        assertEquals(35, snap.recentDays.size)
        assertEquals(66L, snap.recentDays.first().epochDay) // 100 - 34
        val last = snap.recentDays.last()
        assertEquals(100L, last.epochDay)
        assertEquals(100, last.percent) // hoje = progresso parcial (tudo no alvo)
        assertEquals(80, snap.recentDays.first { it.epochDay == 99L }.percent)
        assertEquals(40, snap.recentDays.first { it.epochDay == 70L }.percent)
        assertEquals(0, snap.recentDays.first { it.epochDay == 98L }.percent) // sem registro
    }

    @Test
    fun `accentArgb e repassado`() {
        val snap = snapshot(accentArgb = AccentColor.Pink.argb)
        assertEquals(AccentColor.Pink.argb, snap.accentArgb)
    }
}
