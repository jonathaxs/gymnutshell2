package com.jonathaxs.gymnutshell.core.domain

/**
 * Rotas suportadas pelas notificações — definem pra onde o app navega ao tocar.
 * Porte 1:1 do NotificationRoute (iOS).
 */
enum class NotificationRoute(val rawValue: String) {
    /** TodayScreen. */
    Today("today"),

    /** AchievementsScreen no dia de hoje, filtro "dia". */
    AchievementsToday("achievementsToday"),

    /** Settings → Backup. */
    Backup("backup");

    companion object {
        /** Resolve a partir do rawValue persistido; null se desconhecido. */
        fun fromRaw(raw: String?): NotificationRoute? = entries.firstOrNull { it.rawValue == raw }
    }
}
