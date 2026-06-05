package com.jonathaxs.gymnutshell.core.data

import androidx.room.Database
import androidx.room.RoomDatabase

/**
 * Banco Room do app — equivalente ao ModelContainer do SwiftData.
 * version=1: primeira versão do schema. Ao mudar entities no futuro, sobe a versão + migração.
 * exportSchema=false por ora (ligamos export quando formos cuidar de migrações).
 */
@Database(
    entities = [DailyRecord::class],
    version = 1,
    exportSchema = false,
)
abstract class GymNutshellDatabase : RoomDatabase() {
    abstract fun dailyRecordDao(): DailyRecordDao
}
