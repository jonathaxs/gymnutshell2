package com.jonathaxs.gymnutshell.core.domain

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * (De)serialização dos mapas JSON guardados no DailyRecord (`customValues`/`customRestDays`).
 * Centraliza aqui pra que a serialização fique no core (o módulo `app` não depende de
 * kotlinx.serialization) e pra evitar divergência entre quem grava (DailyRecordFactory) e quem lê.
 */
object DailyRecordCodec {

    private val json = Json { ignoreUnknownKeys = true }

    fun encodeIntMap(map: Map<String, Int>): String = json.encodeToString(map)

    fun decodeIntMap(text: String): Map<String, Int> =
        runCatching { json.decodeFromString<Map<String, Int>>(text) }.getOrDefault(emptyMap())

    fun encodeBoolMap(map: Map<String, Boolean>): String = json.encodeToString(map)

    fun decodeBoolMap(text: String): Map<String, Boolean> =
        runCatching { json.decodeFromString<Map<String, Boolean>>(text) }.getOrDefault(emptyMap())
}
