package com.jonathaxs.gymnutshell.core.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface StreakBonusDao {

    @Query("SELECT * FROM streak_bonus ORDER BY anchorDate DESC")
    fun observeAll(): Flow<List<StreakBonus>>

    /** Âncoras (epoch-day) já premiadas — alimenta o StreakBonusEvaluator pra não repetir. */
    @Query("SELECT DISTINCT anchorDate FROM streak_bonus")
    suspend fun awardedAnchorDays(): List<Long>

    @Query("SELECT * FROM streak_bonus")
    suspend fun getAll(): List<StreakBonus>

    @Insert
    suspend fun insert(bonus: StreakBonus)
}
