package com.jonathaxs.gymnutshell.core.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Um bônus de sequência ganho pelo usuário — porte do @Model StreakBonus (SwiftData → Room).
 *
 * Concedido por completar uma semana (sábado) ou um mês inteiro no nível 3 ou 4.
 * `id` autogerado (o iOS permitia 2 bônus na mesma data: semanal + mensal coincidentes),
 * `anchorDate` é epoch-day (sábado p/ semanal; último dia do mês p/ mensal).
 */
@Entity(tableName = "streak_bonus")
data class StreakBonus(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val anchorDate: Long, // epoch-day
    val bonusType: String, // "weekly.level3" | "weekly.level4" | "monthly.level3" | "monthly.level4"
    val bonusPoints: Int,
    val bonusEmoji: String,
)
