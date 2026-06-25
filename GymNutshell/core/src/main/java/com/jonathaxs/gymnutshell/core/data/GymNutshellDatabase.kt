package com.jonathaxs.gymnutshell.core.data

import androidx.room.Database
import androidx.room.RoomDatabase

/**
 * Banco Room do app — equivalente ao ModelContainer do SwiftData.
 * version=1: primeira versão do schema. Ao mudar entities no futuro, sobe a versão + migração.
 * version=3: CustomGoal ganha customCategoryId/position + nova entidade CustomGoalCategory.
 * exportSchema=false por ora (ligamos export quando formos cuidar de migrações).
 */
@Database(
    entities = [DailyRecord::class, StreakBonus::class, CustomGoal::class, CustomGoalCategory::class],
    version = 3,
    exportSchema = false,
)
abstract class GymNutshellDatabase : RoomDatabase() {
    abstract fun dailyRecordDao(): DailyRecordDao
    abstract fun streakBonusDao(): StreakBonusDao
    abstract fun customGoalDao(): CustomGoalDao
    abstract fun customGoalCategoryDao(): CustomGoalCategoryDao
}
