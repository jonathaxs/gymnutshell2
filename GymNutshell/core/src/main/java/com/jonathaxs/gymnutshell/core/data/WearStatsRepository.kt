package com.jonathaxs.gymnutshell.core.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.jonathaxs.gymnutshell.core.sync.WearSnapshotCodec
import com.jonathaxs.gymnutshell.core.sync.WearStatsSummary
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Persiste no relógio o resumo de estatísticas recebido do celular — porte do
 * WatchStatsStore (iOS). null = nunca sincronizou (a página Stats mostra o
 * estado "aguardando sync").
 */
class WearStatsRepository(private val context: Context) {

    private val statsKey = stringPreferencesKey("wear.stats.summary")

    val stats: Flow<WearStatsSummary?> = context.appPreferences.data.map { prefs ->
        prefs[statsKey]?.let(WearSnapshotCodec::decodeStats)
    }

    suspend fun save(summary: WearStatsSummary) {
        context.appPreferences.edit { prefs ->
            prefs[statsKey] = WearSnapshotCodec.encodeStats(summary)
        }
    }
}
