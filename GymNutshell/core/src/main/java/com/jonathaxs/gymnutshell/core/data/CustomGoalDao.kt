package com.jonathaxs.gymnutshell.core.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface CustomGoalDao {

    // Ordena pela posição (reordenação do usuário) e desempata por id (ordem de criação).
    @Query("SELECT * FROM custom_goal ORDER BY position, id")
    fun observeAll(): Flow<List<CustomGoal>>

    @Query("SELECT * FROM custom_goal ORDER BY position, id")
    suspend fun getAll(): List<CustomGoal>

    @Insert
    suspend fun insert(goal: CustomGoal)

    @Update
    suspend fun update(goal: CustomGoal)

    @Delete
    suspend fun delete(goal: CustomGoal)

    /** Maior posição atual (-1 se a tabela está vazia), pra colocar metas novas no fim. */
    @Query("SELECT COALESCE(MAX(position), -1) FROM custom_goal")
    suspend fun maxPosition(): Int

    /** Atualiza só a posição de uma meta (usado na reordenação). */
    @Query("UPDATE custom_goal SET position = :position WHERE id = :id")
    suspend fun setPosition(id: Long, position: Int)

    /** Desvincula da categoria personalizada removida todas as metas que apontavam pra ela. */
    @Query("UPDATE custom_goal SET customCategoryId = NULL WHERE customCategoryId = :categoryId")
    suspend fun clearCustomCategory(categoryId: String)

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
