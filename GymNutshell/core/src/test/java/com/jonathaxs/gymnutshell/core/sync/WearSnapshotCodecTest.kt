package com.jonathaxs.gymnutshell.core.sync

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/** Roundtrip e tolerância do codec do snapshot celular → relógio. */
class WearSnapshotCodecTest {

    private fun sample() = WearSnapshot(
        sentAtMillis = 1_700_000_000_000,
        epochDay = 20_600,
        intakes = mapOf("tracking.water" to 750, "custom:3" to 2),
        restDays = listOf("tracking.workout"),
        profile = WearProfileSnapshot(
            name = "Jonathas", weightKg = 80.0, heightCm = 180,
            age = 30, sex = "male", userGoalRaw = "maintenance",
        ),
        themeRaw = "gym",
        accentName = "Purple",
        customGoals = listOf(
            WearCustomGoalSnapshot(
                id = 3, emoji = "🧘", name = "Yoga", unit = "min",
                target = 30, increment = 10, categoryRaw = null,
            ),
        ),
    )

    @Test
    fun `roundtrip preserva todos os campos`() {
        val decoded = WearSnapshotCodec.decode(WearSnapshotCodec.encode(sample()))
        assertEquals(sample(), decoded)
    }

    @Test
    fun `payload corrompido devolve null em vez de quebrar`() {
        assertNull(WearSnapshotCodec.decode("{not json"))
        assertNull(WearSnapshotCodec.decode("""{"version":1}"""))
    }

    @Test
    fun `campos desconhecidos de versoes futuras sao ignorados`() {
        val withExtra = WearSnapshotCodec.encode(sample())
            .removeSuffix("}") + ""","novidade":"x"}"""
        assertEquals(sample(), WearSnapshotCodec.decode(withExtra))
    }
}
