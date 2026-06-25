package com.jonathaxs.gymnutshell.core.data

import android.content.Context
import kotlinx.coroutines.flow.Flow

/** Acesso às categorias personalizadas (Room) — porte do CustomGoalCategoriesStore (iOS). */
class CustomGoalCategoryRepository(context: Context) {

    private val dao = DatabaseProvider.get(context).customGoalCategoryDao()

    val categories: Flow<List<CustomGoalCategory>> = dao.observeAll()

    suspend fun all(): List<CustomGoalCategory> = dao.getAll()
    suspend fun upsert(category: CustomGoalCategory) = dao.upsert(category)
    suspend fun delete(category: CustomGoalCategory) = dao.delete(category)

    /** Substitui todas as categorias personalizadas (restauração de backup). */
    suspend fun replaceAll(categories: List<CustomGoalCategory>) = dao.replaceAll(categories)
}
