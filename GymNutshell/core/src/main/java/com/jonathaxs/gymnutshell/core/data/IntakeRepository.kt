package com.jonathaxs.gymnutshell.core.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

/**
 * Persiste a ingestão (intake) do dia por meta — porte dos @AppStorage "...Intake" (iOS).
 * Cada meta vira uma chave "intake.<chaveDaMeta>" no DataStore; expõe tudo como Flow<Map>.
 * Também guarda o último dia ativo (epoch-day) pra resetar quando vira o dia.
 */
class IntakeRepository(private val context: Context) {

    private val lastActiveDayKey = longPreferencesKey("today.lastActiveDay")

    /** Mapa chaveDaMeta → intake, lido das preferências com prefixo "intake.". */
    val intakes: Flow<Map<String, Int>> = context.appPreferences.data.map { prefs ->
        prefs.asMap()
            .filter { (key, value) -> key.name.startsWith(INTAKE_PREFIX) && value is Int }
            .map { (key, value) -> key.name.removePrefix(INTAKE_PREFIX) to (value as Int) }
            .toMap()
    }

    suspend fun setIntake(goalKey: String, value: Int) {
        context.appPreferences.edit { prefs -> prefs[intPreferencesKey(INTAKE_PREFIX + goalKey)] = value }
    }

    /** Apaga todos os intakes (usado ao virar o dia). */
    suspend fun resetAllIntakes() {
        context.appPreferences.edit { prefs ->
            prefs.asMap().keys
                .filter { it.name.startsWith(INTAKE_PREFIX) }
                .forEach { prefs.remove(it) }
        }
    }

    suspend fun lastActiveDay(): Long? = context.appPreferences.data.first()[lastActiveDayKey]

    suspend fun setLastActiveDay(epochDay: Long) {
        context.appPreferences.edit { prefs -> prefs[lastActiveDayKey] = epochDay }
    }

    private companion object {
        const val INTAKE_PREFIX = "intake."
    }
}
