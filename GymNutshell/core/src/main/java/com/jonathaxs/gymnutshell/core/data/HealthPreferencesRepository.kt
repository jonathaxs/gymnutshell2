package com.jonathaxs.gymnutshell.core.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

/**
 * Persiste as preferências de integração com o Health Connect — porte dos @AppStorage
 * `healthkit.syncSleepEnabled` / `healthkit.autoWorkoutCheckin` (iOS). Ambas começam desligadas.
 *
 * Cada preferência expõe um [Flow] (pra tela de Ajustes reagir) e uma leitura `suspend`
 * pontual (usada no momento de gravar o sono ou rodar o auto check-in).
 */
class HealthPreferencesRepository(private val context: Context) {

    /** Sincronizar as horas de sono pro Health Connect ao finalizar o dia. */
    val syncSleepEnabledFlow: Flow<Boolean> =
        context.appPreferences.data.map { it[SYNC_SLEEP_KEY] ?: false }

    suspend fun syncSleepEnabled(): Boolean =
        context.appPreferences.data.first()[SYNC_SLEEP_KEY] ?: false

    suspend fun setSyncSleepEnabled(enabled: Boolean) {
        context.appPreferences.edit { it[SYNC_SLEEP_KEY] = enabled }
    }

    /** Detectar treinos do Health Connect automaticamente e preencher as metas de Treino/Cardio. */
    val autoWorkoutCheckinFlow: Flow<Boolean> =
        context.appPreferences.data.map { it[AUTO_CHECKIN_KEY] ?: false }

    suspend fun autoWorkoutCheckin(): Boolean =
        context.appPreferences.data.first()[AUTO_CHECKIN_KEY] ?: false

    suspend fun setAutoWorkoutCheckin(enabled: Boolean) {
        context.appPreferences.edit { it[AUTO_CHECKIN_KEY] = enabled }
    }

    private companion object {
        val SYNC_SLEEP_KEY = booleanPreferencesKey("health.syncSleepEnabled")
        val AUTO_CHECKIN_KEY = booleanPreferencesKey("health.autoWorkoutCheckin")
    }
}
