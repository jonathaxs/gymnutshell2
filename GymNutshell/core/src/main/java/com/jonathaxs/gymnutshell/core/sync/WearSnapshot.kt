package com.jonathaxs.gymnutshell.core.sync

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/**
 * Snapshot de estado celular → relógio — porte do payload de `sendSnapshot()` do
 * WatchConnectivityManager (iOS). Carrega tudo que a Today do relógio precisa:
 * intakes do dia + dias de descanso + perfil (pra recalcular alvos com a mesma
 * fórmula) + aparência + metas personalizadas.
 *
 * Vai serializado em JSON dentro de um DataItem do Data Layer (path /gymnutshell/snapshot).
 * Como o DataItem persiste e "o último vence", tem a mesma semântica do
 * applicationContext do WatchConnectivity.
 */
@Serializable
data class WearSnapshot(
    val version: Int = 1,
    /** Momento do envio; também garante que cada push altere os bytes do DataItem. */
    val sentAtMillis: Long,
    /** Dia (epoch-day) em que o snapshot foi tirado; vira o lastActiveDay do relógio. */
    val epochDay: Long,
    val intakes: Map<String, Int>,
    val restDays: List<String>,
    val profile: WearProfileSnapshot,
    val themeRaw: String,
    val accentName: String,
    val customGoals: List<WearCustomGoalSnapshot>,
    /** Resumo de estatísticas calculado no celular (página Stats do relógio). */
    val stats: WearStatsSummary? = null,
    /** Histórico de notificações (últimos 3 dias) pra página Notifications do relógio. */
    val notificationHistory: List<WearNotificationEntry>? = null,
)

/**
 * Resumo de estatísticas — porte do WatchStatsSummary (iOS): o celular calcula
 * a partir do Room (registros + bônus) e o relógio só renderiza.
 */
@Serializable
data class WearStatsSummary(
    val totalDays: Int,
    val totalPoints: Int,
    val level1Days: Int,
    val level2Days: Int,
    val level3Days: Int,
    val level4Days: Int,
    val workoutDays: Int,
    val cardioDays: Int,
    /** 7 entradas, da mais antiga pra mais recente (hoje incluso, vazio se sem registro). */
    val last7Days: List<WearDayEntry>,
)

/** Um dia da faixa "últimos 7": emoji da conquista (vazio = sem registro) e percentual. */
@Serializable
data class WearDayEntry(val emoji: String, val percent: Int)

/** Entrada do histórico de notificações espelhada pro relógio. */
@Serializable
data class WearNotificationEntry(
    val id: String,
    val kindRaw: String,
    val title: String,
    val body: String,
    val timestampMillis: Long,
    val routeRaw: String,
)

/** Perfil mínimo pro relógio recalcular os alvos (GoalsCalculator). */
@Serializable
data class WearProfileSnapshot(
    val name: String,
    val weightKg: Double,
    val heightCm: Int,
    val age: Int,
    val sex: String,
    val userGoalRaw: String,
)

/** Meta personalizada; `id` preservado pra manter o intakeKey "custom:<id>" consistente. */
@Serializable
data class WearCustomGoalSnapshot(
    val id: Long,
    val emoji: String,
    val name: String,
    val unit: String,
    val target: Int,
    val increment: Int,
    val categoryRaw: String? = null,
)

/** Codec JSON do snapshot; tolerante a campos novos (versões futuras). */
object WearSnapshotCodec {

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    fun encode(snapshot: WearSnapshot): String = json.encodeToString(WearSnapshot.serializer(), snapshot)

    fun decode(raw: String): WearSnapshot? =
        runCatching { json.decodeFromString(WearSnapshot.serializer(), raw) }.getOrNull()

    // Stats avulsas: usadas pelo relógio pra persistir o resumo localmente (DataStore).
    fun encodeStats(stats: WearStatsSummary): String =
        json.encodeToString(WearStatsSummary.serializer(), stats)

    fun decodeStats(raw: String): WearStatsSummary? =
        runCatching { json.decodeFromString(WearStatsSummary.serializer(), raw) }.getOrNull()
}

/** Constantes do Data Layer compartilhadas entre :app e :wear. */
object WearSyncContract {
    /** Prefixo comum de todos os paths do app (filtro do listener no manifest). */
    const val PATH_PREFIX = "/gymnutshell"

    /** Snapshot completo celular → relógio. */
    const val SNAPSHOT_PATH = "$PATH_PREFIX/snapshot"

    /** Chave do JSON dentro do DataMap. */
    const val KEY_PAYLOAD = "payload"

    // Deltas relógio → celular (porte do sendIntakeUpdate do iOS). Um DataItem por
    // meta: persistente e "último valor vence por chave" — cobre de uma vez o
    // sendMessage (imediato) e o transferUserInfo (fila) do WatchConnectivity.
    const val INTAKE_PATH_PREFIX = "$PATH_PREFIX/intake/"
    const val RESTDAY_PATH_PREFIX = "$PATH_PREFIX/restday/"

    /** Exclusão de entrada do histórico, relógio → celular (porte do sendHistoryDelete). */
    const val HISTDELETE_PATH_PREFIX = "$PATH_PREFIX/histdelete/"
    const val KEY_VALUE = "value"
    const val KEY_ACTIVE = "active"
    /** Dia (epoch-day) do delta; o celular descarta deltas de outro dia (relógio offline). */
    const val KEY_EPOCH_DAY = "epochDay"
    /** Momento do envio; garante que cada envio altere os bytes do DataItem. */
    const val KEY_SENT_AT = "sentAt"

    fun intakePath(goalKey: String): String = INTAKE_PATH_PREFIX + goalKey
    fun restDayPath(goalKey: String): String = RESTDAY_PATH_PREFIX + goalKey

    /** Extrai a chave da meta de um path de delta; null se o prefixo não casa. */
    fun goalKeyFromPath(path: String, prefix: String): String? =
        path.takeIf { it.startsWith(prefix) }?.removePrefix(prefix)?.takeIf { it.isNotEmpty() }
}
