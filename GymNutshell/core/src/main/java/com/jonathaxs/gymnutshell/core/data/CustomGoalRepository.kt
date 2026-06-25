package com.jonathaxs.gymnutshell.core.data

import android.content.Context
import kotlinx.coroutines.flow.Flow

/** Acesso às metas personalizadas (Room) — porte do CustomGoalsStore (iOS). */
class CustomGoalRepository(context: Context) {

    private val dao = DatabaseProvider.get(context).customGoalDao()

    val goals: Flow<List<CustomGoal>> = dao.observeAll()

    suspend fun all(): List<CustomGoal> = dao.getAll()

    /** Insere uma meta nova já no fim da ordem (maior posição + 1). */
    suspend fun add(goal: CustomGoal) = dao.insert(goal.copy(position = dao.maxPosition() + 1))

    suspend fun update(goal: CustomGoal) = dao.update(goal)
    suspend fun delete(goal: CustomGoal) = dao.delete(goal)

    /** Aplica uma nova ordem (ids na sequência desejada) gravando a posição de cada meta. */
    suspend fun reorder(orderedIds: List<Long>) {
        orderedIds.forEachIndexed { index, id -> dao.setPosition(id, index) }
    }

    /** Desvincula da categoria removida todas as metas que apontavam pra ela. */
    suspend fun clearCustomCategory(categoryId: String) = dao.clearCustomCategory(categoryId)

    /** Substitui todas as metas personalizadas (restauração de backup). */
    suspend fun replaceAll(goals: List<CustomGoal>) = dao.replaceAll(goals)
}
