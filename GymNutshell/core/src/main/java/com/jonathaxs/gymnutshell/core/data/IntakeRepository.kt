package com.jonathaxs.gymnutshell.core.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
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
    private val restDaysKey = stringSetPreferencesKey("today.restDays")

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

    /** Metas em "dia de descanso" hoje (por chave). Contam como 100% sem slider. */
    val restDays: Flow<Set<String>> =
        context.appPreferences.data.map { prefs -> prefs[restDaysKey] ?: emptySet() }

    suspend fun toggleRestDay(goalKey: String) {
        context.appPreferences.edit { prefs ->
            val current = prefs[restDaysKey] ?: emptySet()
            prefs[restDaysKey] = if (goalKey in current) current - goalKey else current + goalKey
        }
    }

    /** Define o estado de "dia de descanso" de forma idempotente (aplicação de delta do relógio). */
    suspend fun setRestDay(goalKey: String, active: Boolean) {
        context.appPreferences.edit { prefs ->
            val current = prefs[restDaysKey] ?: emptySet()
            prefs[restDaysKey] = if (active) current + goalKey else current - goalKey
        }
    }

    /**
     * Substitui todos os intakes e dias de descanso de uma vez (sync com o relógio).
     * Chaves locais ausentes do mapa recebido são removidas — o snapshot é a verdade.
     */
    suspend fun replaceAll(intakes: Map<String, Int>, restDays: Set<String>) {
        context.appPreferences.edit { prefs ->
            prefs.asMap().keys
                .filter { it.name.startsWith(INTAKE_PREFIX) }
                .forEach { prefs.remove(it) }
            intakes.forEach { (goalKey, value) ->
                prefs[intPreferencesKey(INTAKE_PREFIX + goalKey)] = value
            }
            prefs[restDaysKey] = restDays
        }
    }

    /** Apaga todos os intakes e dias de descanso (usado ao virar o dia). */
    suspend fun resetAllIntakes() {
        context.appPreferences.edit { prefs ->
            prefs.asMap().keys
                .filter { it.name.startsWith(INTAKE_PREFIX) }
                .forEach { prefs.remove(it) }
            prefs.remove(restDaysKey)
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
