package com.jonathaxs.gymnutshell.core.data

import android.content.Context
import kotlinx.coroutines.flow.Flow

/** Acesso às metas personalizadas (Room) — porte do CustomGoalsStore (iOS). */
class CustomGoalRepository(context: Context) {

    private val dao = DatabaseProvider.get(context).customGoalDao()

    val goals: Flow<List<CustomGoal>> = dao.observeAll()

    suspend fun all(): List<CustomGoal> = dao.getAll()
    suspend fun add(goal: CustomGoal) = dao.insert(goal)
    suspend fun delete(goal: CustomGoal) = dao.delete(goal)

    /** Substitui todas as metas personalizadas (restauração de backup). */
    suspend fun replaceAll(goals: List<CustomGoal>) = dao.replaceAll(goals)
}
