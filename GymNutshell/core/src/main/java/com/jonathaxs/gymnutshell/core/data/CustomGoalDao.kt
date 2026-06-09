package com.jonathaxs.gymnutshell.core.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface CustomGoalDao {

    @Query("SELECT * FROM custom_goal ORDER BY id")
    fun observeAll(): Flow<List<CustomGoal>>

    @Query("SELECT * FROM custom_goal")
    suspend fun getAll(): List<CustomGoal>

    @Insert
    suspend fun insert(goal: CustomGoal)

    @Delete
    suspend fun delete(goal: CustomGoal)

    @Query("DELETE FROM custom_goal")
    suspend fun deleteAll()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(goals: List<CustomGoal>)

    /** Substitui todas as metas personalizadas de uma vez (restauração de backup). */
    @Transaction
    suspend fun replaceAll(goals: List<CustomGoal>) {
        deleteAll()
        insertAll(goals)
    }
}
