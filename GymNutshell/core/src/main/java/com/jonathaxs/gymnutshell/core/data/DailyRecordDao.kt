package com.jonathaxs.gymnutshell.core.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

/**
 * Acesso ao histórico de dias. O Room gera a implementação em tempo de compilação (via KSP).
 * `observeAll` devolve um Flow: a UI reage automaticamente a mudanças no banco
 * (≈ o @Query do SwiftData que atualizava as views sozinho).
 */
@Dao
interface DailyRecordDao {

    @Query("SELECT * FROM daily_record ORDER BY date DESC")
    fun observeAll(): Flow<List<DailyRecord>>

    @Query("SELECT * FROM daily_record WHERE date = :date LIMIT 1")
    suspend fun findByDate(date: Long): DailyRecord?

    @Upsert
    suspend fun upsert(record: DailyRecord)

    @Delete
    suspend fun delete(record: DailyRecord)

    @Query("DELETE FROM daily_record WHERE date = :date")
    suspend fun deleteByDate(date: Long)
}
