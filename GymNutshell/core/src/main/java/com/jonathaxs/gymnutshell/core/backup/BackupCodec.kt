package com.jonathaxs.gymnutshell.core.backup

import kotlinx.serialization.json.Json

/**
 * Codifica/decodifica o [BackupPayload] em JSON — porte do encode/decode do BackupManager (iOS).
 * `encodeDefaults = false` faz os opcionais nulos sumirem (igual ao `nil`); `ignoreUnknownKeys`
 * tolera campos iOS-only no import.
 */
object BackupCodec {

    /** Versão atual do formato (compatível com o v4 do iOS). */
    const val CURRENT_VERSION = 4

    private val json = Json {
        prettyPrint = true
        prettyPrintIndent = "  "
        ignoreUnknownKeys = true
        encodeDefaults = false
    }

    fun encode(payload: BackupPayload): String = json.encodeToString(payload)

    fun decode(text: String): BackupPayload = json.decodeFromString(text)
}
