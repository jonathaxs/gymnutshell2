package com.jonathaxs.gymnutshell.core.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class NotificationKindTest {

    @Test
    fun `metas e progresso sao editaveis por intervalo`() {
        for (kind in listOf(
            NotificationKind.Progress, NotificationKind.Sleep, NotificationKind.Water,
            NotificationKind.Calories, NotificationKind.Protein, NotificationKind.Carbs,
            NotificationKind.GoodFat, NotificationKind.Fiber, NotificationKind.Workout,
            NotificationKind.Cardio, NotificationKind.Creatine,
        )) {
            assertTrue(kind.name, kind.isEditable)
            assertTrue(kind.name, kind.isIntervalBased)
        }
    }

    @Test
    fun `eventos nao sao editaveis`() {
        for (kind in listOf(
            NotificationKind.Achievement, NotificationKind.StreakBonus,
            NotificationKind.HealthSync, NotificationKind.Backup,
        )) {
            assertFalse(kind.name, kind.isEditable)
            assertEquals(0, kind.defaultIntervalMinutes)
        }
    }

    @Test
    fun `intervalos padrao batem com o iOS`() {
        assertEquals(150, NotificationKind.Progress.defaultIntervalMinutes)
        assertEquals(120, NotificationKind.Water.defaultIntervalMinutes)
        assertEquals(180, NotificationKind.Sleep.defaultIntervalMinutes)
        assertEquals(120, NotificationKind.Protein.defaultIntervalMinutes)
    }

    @Test
    fun `janela do sono e mais restrita`() {
        assertEquals(10, NotificationKind.Sleep.dailyStartHour)
        assertEquals(19, NotificationKind.Sleep.dailyCutoffHour)
        assertEquals(6, NotificationKind.Water.dailyStartHour)
        assertEquals(22, NotificationKind.Water.dailyCutoffHour)
    }

    @Test
    fun `rota de deep-link por kind`() {
        assertEquals(NotificationRoute.AchievementsToday, NotificationKind.Achievement.route)
        assertEquals(NotificationRoute.AchievementsToday, NotificationKind.StreakBonus.route)
        assertEquals(NotificationRoute.Backup, NotificationKind.Backup.route)
        assertEquals(NotificationRoute.Today, NotificationKind.Water.route)
        assertEquals(NotificationRoute.Today, NotificationKind.HealthSync.route)
    }

    @Test
    fun `trackingKey faz roundtrip com fromTrackingKey`() {
        assertEquals("tracking.water", NotificationKind.Water.trackingKey)
        assertEquals(NotificationKind.Water, NotificationKind.fromTrackingKey("tracking.water"))
        assertEquals(NotificationKind.Workout, NotificationKind.fromTrackingKey("tracking.workout"))
        assertNull(NotificationKind.fromTrackingKey("tracking.unknown"))
        // Kinds de Sistema não têm chave de meta.
        assertNull(NotificationKind.Progress.trackingKey)
    }

    @Test
    fun `fromRaw resolve e ignora desconhecido`() {
        assertEquals(NotificationKind.StreakBonus, NotificationKind.fromRaw("streakBonus"))
        // Mantém o rawValue legado do iOS pra Saúde.
        assertEquals(NotificationKind.HealthSync, NotificationKind.fromRaw("appleHealth"))
        assertNull(NotificationKind.fromRaw("nope"))
    }

    @Test
    fun `metas tem emoji e sistema nao`() {
        assertEquals("💧", NotificationKind.Water.emoji)
        assertNull(NotificationKind.Progress.emoji)
        assertNull(NotificationKind.Backup.emoji)
    }
}
