package com.jonathaxs.gymnutshell.core.domain

import java.time.LocalDate
import java.time.Period

/**
 * Dados de perfil usados pra calcular as metas — porte das chaves de UserProfile (iOS).
 * Valores guardados sempre em métrico (kg, cm). `sex`: "male" | "female" | "other".
 */
data class Profile(
    val name: String = "",
    val weightKg: Double = 0.0,
    val heightCm: Int = 0,
    val age: Int = 0,
    val sex: String = "other",
    val goal: UserGoal = UserGoal.Maintenance,
) {
    companion object {
        /** Idade em anos a partir do nascimento — porte de UserProfile.age(from:now:). */
        fun age(birthday: LocalDate, now: LocalDate = LocalDate.now()): Int =
            maxOf(0, Period.between(birthday, now).years)
    }
}
