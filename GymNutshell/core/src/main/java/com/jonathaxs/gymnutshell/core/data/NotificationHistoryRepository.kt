package com.jonathaxs.gymnutshell.core.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.jonathaxs.gymnutshell.core.domain.NotificationHistoryEntry
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

/**
 * Persiste o histórico de notificações de evento — porte do NotificationHistoryStore (iOS).
 * No iOS era UserDefaults + JSON (Codable); aqui é DataStore + JSON (org.json), o que evita uma
 * migração de schema do Room. Mantém só os últimos 3 dias, podando na leitura e na escrita.
 */
class NotificationHistoryRepository(private val context: Context) {

    private val historyKey = stringPreferencesKey(STORAGE_KEY)

    /** Histórico (mais recente primeiro), já podado pra 3 dias. */
    val entries: Flow<List<NotificationHistoryEntry>> = context.appPreferences.data.map { prefs ->
        prune(decode(prefs[historyKey]))
    }

    /** Adiciona uma entrada no topo e poda o que passou de 3 dias. */
    suspend fun append(
        kindRaw: String,
        title: String,
        body: String,
        routeRaw: String,
        achievementEpochDay: Long? = null,
    ) {
        val entry = NotificationHistoryEntry(
            id = UUID.randomUUID().toString(),
            kindRaw = kindRaw,
            title = title,
            body = body,
            timestampMillis = System.currentTimeMillis(),
            routeRaw = routeRaw,
            achievementEpochDay = achievementEpochDay,
        )
        context.appPreferences.edit { prefs ->
            val updated = prune(listOf(entry) + decode(prefs[historyKey]))
            prefs[historyKey] = encode(updated)
        }
    }

    /** Remove uma entrada específica pelo id. */
    suspend fun delete(id: String) {
        context.appPreferences.edit { prefs ->
            val updated = decode(prefs[historyKey]).filterNot { it.id == id }
            prefs[historyKey] = encode(updated)
        }
    }

    /** Limpa todo o histórico. */
    suspend fun clear() {
        context.appPreferences.edit { prefs -> prefs.remove(historyKey) }
    }

    /** Snapshot pontual do histórico (montagem do payload pro relógio). */
    suspend fun snapshot(): List<NotificationHistoryEntry> = prune(decode(currentRaw()))

    /** Substitui todo o histórico (aplicação do snapshot vindo do celular). */
    suspend fun replaceAll(entries: List<NotificationHistoryEntry>) {
        context.appPreferences.edit { prefs ->
            prefs[historyKey] = encode(prune(entries))
        }
    }

    private suspend fun currentRaw(): String? =
        context.appPreferences.data.first()[historyKey]

    // MARK: - JSON

    private fun decode(raw: String?): List<NotificationHistoryEntry> {
        if (raw.isNullOrBlank()) return emptyList()
        return runCatching {
            val array = JSONArray(raw)
            (0 until array.length()).map { i ->
                val o = array.getJSONObject(i)
                NotificationHistoryEntry(
                    id = o.getString("id"),
                    kindRaw = o.getString("kind"),
                    title = o.getString("title"),
                    body = o.getString("body"),
                    timestampMillis = o.getLong("ts"),
                    routeRaw = o.getString("route"),
                    achievementEpochDay = if (o.has("achDay") && !o.isNull("achDay")) o.getLong("achDay") else null,
                )
            }
        }.getOrDefault(emptyList())
    }

    private fun encode(list: List<NotificationHistoryEntry>): String {
        val array = JSONArray()
        list.forEach { e ->
            val o = JSONObject()
                .put("id", e.id)
                .put("kind", e.kindRaw)
                .put("title", e.title)
                .put("body", e.body)
                .put("ts", e.timestampMillis)
                .put("route", e.routeRaw)
            e.achievementEpochDay?.let { o.put("achDay", it) }
            array.put(o)
        }
        return array.toString()
    }

    private fun prune(list: List<NotificationHistoryEntry>): List<NotificationHistoryEntry> {
        val cutoff = System.currentTimeMillis() - RETENTION_MILLIS
        return list.filter { it.timestampMillis >= cutoff }.sortedByDescending { it.timestampMillis }
    }

    private companion object {
        const val STORAGE_KEY = "notifications.history.v1"
        const val RETENTION_MILLIS = 3L * 24 * 60 * 60 * 1000 // 3 dias
    }
}
