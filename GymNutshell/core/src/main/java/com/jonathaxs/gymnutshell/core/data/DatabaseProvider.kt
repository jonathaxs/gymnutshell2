package com.jonathaxs.gymnutshell.core.data

import android.content.Context
import androidx.room.Room

/**
 * Instância única (singleton) do banco Room — equivalente ao ModelContainer compartilhado do iOS.
 * Lazy + thread-safe; usa o applicationContext pra não vazar Activity.
 */
object DatabaseProvider {

    @Volatile
    private var instance: GymNutshellDatabase? = null

    fun get(context: Context): GymNutshellDatabase =
        instance ?: synchronized(this) {
            instance ?: Room.databaseBuilder(
                context.applicationContext,
                GymNutshellDatabase::class.java,
                "gymnutshell.db",
            )
                // Em dev, recria o banco quando o schema muda (sem migração manual ainda).
                .fallbackToDestructiveMigration(dropAllTables = true)
                .build().also { instance = it }
        }
}
