package com.jonathaxs.gymnutshell.core.domain

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

class GoalsProviderTest {

    @Test
    fun `goals do perfil batem com o GoalsCalculator`() {
        val profile = Profile(weightKg = 80.0, heightCm = 180, age = 30, sex = "male", goal = UserGoal.Maintenance)
        val expected = GoalsCalculator.calculate(80.0, 180, 30, "male", UserGoal.Maintenance)
        assertEquals(expected, GoalsProvider.goals(profile))
    }

    @Test
    fun `idade calculada a partir do nascimento`() {
        val birthday = LocalDate.of(1996, 5, 16)
        assertEquals(30, Profile.age(birthday, now = LocalDate.of(2026, 5, 16)))
        assertEquals(29, Profile.age(birthday, now = LocalDate.of(2026, 5, 15))) // véspera do aniversário
    }
}
