package com.jonathaxs.gymnutshell.core.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Categoria de meta criada pelo usuário — porte de CustomGoalCategory (iOS, era Codable em UserDefaults).
 * `supportsRestDay` liga o botão ON/OFF de "dia de descanso" em todas as metas que pertencerem a ela.
 * A ordem de exibição vive na ordem unificada de categorias (GoalConfigRepository), não nesta tabela.
 */
@Entity(tableName = "custom_goal_category")
data class CustomGoalCategory(
    @PrimaryKey val id: String,
    val name: String,
    val supportsRestDay: Boolean = false,
)
