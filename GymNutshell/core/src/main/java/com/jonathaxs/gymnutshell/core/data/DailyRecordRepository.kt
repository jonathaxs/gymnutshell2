package com.jonathaxs.gymnutshell.core.data

import android.content.Context
import kotlinx.coroutines.flow.Flow

/**
 * Acesso ao histórico de dias e aos bônus de sequência (Room).
 * Espelha o que no iOS era feito direto no ModelContext do SwiftData.
 */
class DailyRecordRepository(context: Context) {

    private val db = DatabaseProvider.get(context)
    private val recordDao = db.dailyRecordDao()
    private val streakDao = db.streakBonusDao()

    /** Histórico observável (a UI reage a mudanças). */
    val records: Flow<List<DailyRecord>> = recordDao.observeAll()

    /** Bônus de sequência observáveis. */
    val bonuses: Flow<List<StreakBonus>> = streakDao.observeAll()

    suspend fun findByDate(epochDay: Long): DailyRecord? = recordDao.findByDate(epochDay)
    suspend fun upsert(record: DailyRecord) = recordDao.upsert(record)
    suspend fun allRecords(): List<DailyRecord> = recordDao.getAll()

    suspend fun awardedAnchors(): Set<Long> = streakDao.awardedAnchorDays().toSet()
    suspend fun insertBonus(bonus: StreakBonus) = streakDao.insert(bonus)
}
