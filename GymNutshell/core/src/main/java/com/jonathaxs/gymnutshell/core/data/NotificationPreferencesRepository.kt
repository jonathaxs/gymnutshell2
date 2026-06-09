package com.jonathaxs.gymnutshell.core.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.jonathaxs.gymnutshell.core.domain.NotificationKind
import com.jonathaxs.gymnutshell.core.domain.NotificationSound
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

/**
 * Persiste as preferências de notificação — porte do NotificationPreferences (iOS, UserDefaults).
 *
 * No iOS as leituras eram síncronas; aqui o DataStore é assíncrono, então cada preferência expõe:
 *  - um [Flow] pra UI reagir (toggles/intervalos da tela de Ajustes), e
 *  - uma leitura pontual `suspend` (usada pelo worker de agendamento e no disparo de eventos).
 *
 * Chaves (mesmo esquema do iOS):
 *  - `notifications.enabled.<kind>` : Boolean
 *  - `notifications.intervalMinutes.<kind>` : Int (0 = usa o default do kind)
 *  - `notifications.sound.<kind>` : String (NotificationSound.rawValue)
 *  - variantes `...custom.<id>` para metas personalizadas
 */
class NotificationPreferencesRepository(private val context: Context) {

    // MARK: - Kind fixo

    /**
     * Todos os estados de "habilitado" num Flow só (pra tela de Ajustes reagir de uma vez).
     * Chave = sufixo após "notifications.enabled." (ex.: "water", "custom.3").
     */
    val enabledStates: Flow<Map<String, Boolean>> = context.appPreferences.data.map { prefs ->
        prefs.asMap()
            .filter { (key, value) -> key.name.startsWith(ENABLED_PREFIX) && value is Boolean }
            .map { (key, value) -> key.name.removePrefix(ENABLED_PREFIX) to (value as Boolean) }
            .toMap()
    }

    /** Se o kind está ativo (Flow reativo pra UI). */
    fun isEnabledFlow(kind: NotificationKind): Flow<Boolean> =
        context.appPreferences.data.map { it[enabledKey(kind)] ?: false }

    /** Leitura pontual de "ativo" (worker/eventos). */
    suspend fun isEnabled(kind: NotificationKind): Boolean =
        context.appPreferences.data.first()[enabledKey(kind)] ?: false

    suspend fun setEnabled(enabled: Boolean, kind: NotificationKind) {
        context.appPreferences.edit { it[enabledKey(kind)] = enabled }
    }

    /** Intervalo em minutos (Flow); cai pro default do kind se nada salvo. */
    fun intervalMinutesFlow(kind: NotificationKind): Flow<Int> =
        context.appPreferences.data.map { prefs ->
            prefs[intervalKey(kind)]?.takeIf { it > 0 } ?: kind.defaultIntervalMinutes
        }

    suspend fun intervalMinutes(kind: NotificationKind): Int {
        val stored = context.appPreferences.data.first()[intervalKey(kind)] ?: 0
        return if (stored > 0) stored else kind.defaultIntervalMinutes
    }

    suspend fun setIntervalMinutes(minutes: Int, kind: NotificationKind) {
        context.appPreferences.edit { it[intervalKey(kind)] = minutes }
    }

    /** Som do kind (Flow); cai pra Default se nada salvo. */
    fun soundFlow(kind: NotificationKind): Flow<NotificationSound> =
        context.appPreferences.data.map { NotificationSound.fromRaw(it[soundKey(kind)]) }

    suspend fun sound(kind: NotificationKind): NotificationSound =
        NotificationSound.fromRaw(context.appPreferences.data.first()[soundKey(kind)])

    suspend fun setSound(sound: NotificationSound, kind: NotificationKind) {
        context.appPreferences.edit { it[soundKey(kind)] = sound.rawValue }
    }

    // MARK: - Metas personalizadas (por id)

    fun isCustomEnabledFlow(id: Long): Flow<Boolean> =
        context.appPreferences.data.map { it[customEnabledKey(id)] ?: false }

    suspend fun isCustomEnabled(id: Long): Boolean =
        context.appPreferences.data.first()[customEnabledKey(id)] ?: false

    suspend fun setCustomEnabled(enabled: Boolean, id: Long) {
        context.appPreferences.edit { it[customEnabledKey(id)] = enabled }
    }

    fun customIntervalMinutesFlow(id: Long, fallback: Int = 120): Flow<Int> =
        context.appPreferences.data.map { prefs ->
            prefs[customIntervalKey(id)]?.takeIf { it > 0 } ?: fallback
        }

    suspend fun customIntervalMinutes(id: Long, fallback: Int = 120): Int {
        val stored = context.appPreferences.data.first()[customIntervalKey(id)] ?: 0
        return if (stored > 0) stored else fallback
    }

    suspend fun setCustomIntervalMinutes(minutes: Int, id: Long) {
        context.appPreferences.edit { it[customIntervalKey(id)] = minutes }
    }

    fun customSoundFlow(id: Long): Flow<NotificationSound> =
        context.appPreferences.data.map { NotificationSound.fromRaw(it[customSoundKey(id)]) }

    suspend fun customSound(id: Long): NotificationSound =
        NotificationSound.fromRaw(context.appPreferences.data.first()[customSoundKey(id)])

    suspend fun setCustomSound(sound: NotificationSound, id: Long) {
        context.appPreferences.edit { it[customSoundKey(id)] = sound.rawValue }
    }

    // MARK: - Defaults do onboarding

    /**
     * Aplica os defaults só uma vez no app (guardado por flag) — chamado no primeiro grant de
     * permissão. Evita resobrescrever as escolhas do usuário em grants seguintes.
     */
    suspend fun applyDefaultEnabledKindsOnce() {
        val already = context.appPreferences.data.first()[defaultsAppliedKey] ?: false
        if (already) return
        applyDefaultEnabledKinds()
        context.appPreferences.edit { it[defaultsAppliedKey] = true }
    }

    /**
     * Aplica os kinds ligados por padrão na primeira autorização — porte de applyDefaultEnabledKinds (iOS).
     * Liga Progresso, Conquista, Bônus e Água; deixa o resto desligado.
     */
    suspend fun applyDefaultEnabledKinds() {
        context.appPreferences.edit { prefs ->
            prefs[enabledKey(NotificationKind.Progress)] = true
            prefs[enabledKey(NotificationKind.Achievement)] = true
            prefs[enabledKey(NotificationKind.StreakBonus)] = true
            prefs[enabledKey(NotificationKind.HealthSync)] = false
            prefs[enabledKey(NotificationKind.Backup)] = false
            prefs[enabledKey(NotificationKind.Water)] = true
            for (kind in listOf(
                NotificationKind.Sleep, NotificationKind.Calories, NotificationKind.Protein,
                NotificationKind.Carbs, NotificationKind.GoodFat, NotificationKind.Fiber,
                NotificationKind.Workout, NotificationKind.Cardio, NotificationKind.Creatine,
            )) {
                prefs[enabledKey(kind)] = false
            }
        }
    }

    // MARK: - Backup (snapshots por prefixo + restauração)

    /** Mapa id→valor de cada prefixo (id = sufixo após o prefixo, ex.: "water", "custom.3"). */
    suspend fun enabledSnapshot(): Map<String, Boolean> = snapshot(ENABLED_PREFIX)
    suspend fun intervalSnapshot(): Map<String, Int> = snapshot(INTERVAL_PREFIX)
    suspend fun soundSnapshot(): Map<String, String> = snapshot(SOUND_PREFIX)

    @Suppress("UNCHECKED_CAST")
    private suspend fun <T> snapshot(prefix: String): Map<String, T> =
        context.appPreferences.data.first().asMap()
            .filter { (key, _) -> key.name.startsWith(prefix) }
            .map { (key, value) -> key.name.removePrefix(prefix) to (value as T) }
            .toMap()

    /** Reescreve as preferências de notificação vindas de um backup (ignora mapas nulos). */
    suspend fun restoreFromBackup(
        enabled: Map<String, Boolean>?,
        interval: Map<String, Int>?,
        sound: Map<String, String>?,
    ) {
        context.appPreferences.edit { prefs ->
            enabled?.forEach { (id, v) -> prefs[booleanPreferencesKey("$ENABLED_PREFIX$id")] = v }
            interval?.forEach { (id, v) -> prefs[intPreferencesKey("$INTERVAL_PREFIX$id")] = v }
            sound?.forEach { (id, v) -> prefs[stringPreferencesKey("$SOUND_PREFIX$id")] = v }
        }
    }

    // MARK: - Chaves

    private fun enabledKey(kind: NotificationKind) = booleanPreferencesKey("notifications.enabled.${kind.rawValue}")
    private fun intervalKey(kind: NotificationKind) = intPreferencesKey("notifications.intervalMinutes.${kind.rawValue}")
    private fun soundKey(kind: NotificationKind) = stringPreferencesKey("notifications.sound.${kind.rawValue}")
    private fun customEnabledKey(id: Long) = booleanPreferencesKey("notifications.enabled.custom.$id")
    private fun customIntervalKey(id: Long) = intPreferencesKey("notifications.intervalMinutes.custom.$id")
    private fun customSoundKey(id: Long) = stringPreferencesKey("notifications.sound.custom.$id")
    private val defaultsAppliedKey = booleanPreferencesKey("notifications.defaultsApplied")

    private companion object {
        const val ENABLED_PREFIX = "notifications.enabled."
        const val INTERVAL_PREFIX = "notifications.intervalMinutes."
        const val SOUND_PREFIX = "notifications.sound."
    }
}
