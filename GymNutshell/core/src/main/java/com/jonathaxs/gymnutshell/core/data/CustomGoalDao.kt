package com.jonathaxs.gymnutshell.core.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
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
}
