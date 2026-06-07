package com.jonathaxs.gymnutshell.core.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Meta de rastreio criada pelo usuário — porte de CustomTrackingGoal (iOS, era Codable em UserDefaults).
 * Aqui vira @Entity Room. `categoryRaw` = rawValue de uma GoalCategory fixa, ou null (sem categoria).
 * Categorias criadas pelo usuário (CustomGoalCategory) ficam pra depois.
 */
@Entity(tableName = "custom_goal")
data class CustomGoal(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val emoji: String,
    val name: String,
    val unit: String,
    val target: Int,
    val increment: Int,
    val categoryRaw: String? = null,
) {
    /** Chave usada nos intakes/Today (ex.: "custom:3"). */
    val intakeKey: String get() = "custom:$id"
}
