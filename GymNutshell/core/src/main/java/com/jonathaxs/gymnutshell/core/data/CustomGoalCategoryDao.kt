package com.jonathaxs.gymnutshell.core.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface CustomGoalCategoryDao {

    @Query("SELECT * FROM custom_goal_category")
    fun observeAll(): Flow<List<CustomGoalCategory>>

    @Query("SELECT * FROM custom_goal_category")
    suspend fun getAll(): List<CustomGoalCategory>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(category: CustomGoalCategory)

    @Delete
    suspend fun delete(category: CustomGoalCategory)

    @Query("DELETE FROM custom_goal_category")
    suspend fun deleteAll()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(categories: List<CustomGoalCategory>)

    /** Substitui todas as categorias personalizadas de uma vez (restauração de backup). */
    @Transaction
    suspend fun replaceAll(categories: List<CustomGoalCategory>) {
        deleteAll()
        insertAll(categories)
    }
}
