package com.jonathaxs.gymnutshell.core.domain

/**
 * Entrada imutável do histórico de notificações de evento — porte de NotificationHistoryEntry (iOS).
 * Lembretes de meta por intervalo também são registrados aqui quando postados.
 */
data class NotificationHistoryEntry(
    val id: String,
    val kindRaw: String,
    val title: String,
    val body: String,
    val timestampMillis: Long,
    val routeRaw: String,
    /** Epoch-day da conquista (quando aplicável); permite o deep-link abrir o dia certo. */
    val achievementEpochDay: Long? = null,
) {
    val kind: NotificationKind? get() = NotificationKind.fromRaw(kindRaw)
    val route: NotificationRoute? get() = NotificationRoute.fromRaw(routeRaw)
}
