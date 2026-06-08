package com.jonathaxs.gymnutshell.core.domain

/**
 * Catálogo de todas as notificações fixas (não-customizadas) do Gym Nutshell.
 * Porte do NotificationKind (iOS). Metas personalizadas usam identifiers `custom.<id>`
 * e não entram neste enum.
 */
enum class NotificationKind(val rawValue: String) {

    // Sistema
    Progress("progress"),
    Achievement("achievement"),
    StreakBonus("streakBonus"),
    HealthSync("appleHealth"),
    Backup("backup"),

    // Metas
    Sleep("sleep"),
    Water("water"),
    Calories("calories"),
    Protein("protein"),
    Carbs("carbs"),
    GoodFat("goodFat"),
    Fiber("fiber"),
    Workout("workout"),
    Cardio("cardio"),
    Creatine("creatine");

    /**
     * True se a notificação permite o usuário editar o intervalo em minutos.
     * Todas as Metas + Progresso são editáveis; Conquista, Bônus, Saúde e Backup são eventos.
     */
    val isEditable: Boolean
        get() = when (this) {
            Progress, Sleep, Water, Calories, Protein, Carbs, GoodFat, Fiber,
            Workout, Cardio, Creatine -> true
            Achievement, StreakBonus, HealthSync, Backup -> false
        }

    /** True quando a notificação é baseada em intervalo (agendamento periódico no dia). */
    val isIntervalBased: Boolean get() = isEditable

    /**
     * Intervalo padrão em minutos para notificações baseadas em intervalo.
     * Progresso: 150 | Água: 120 | Sono: 180 | demais metas: 120 | eventos: 0.
     */
    val defaultIntervalMinutes: Int
        get() = when (this) {
            Progress -> 150
            Water -> 120
            Sleep -> 180
            Calories, Protein, Carbs, GoodFat, Fiber, Workout, Cardio, Creatine -> 120
            Achievement, StreakBonus, HealthSync, Backup -> 0
        }

    /**
     * Hora limite (24h) a partir da qual não agendamos mais lembretes no dia.
     * Sono tem cutoff mais cedo (19h): não faz sentido sugerir cochilo à noite.
     */
    val dailyCutoffHour: Int get() = if (this == Sleep) 19 else 22

    /**
     * Hora mínima (24h) a partir da qual os lembretes podem começar no dia.
     * Sono só sugere a partir das 10h; antes disso o usuário provavelmente acabou de acordar.
     */
    val dailyStartHour: Int get() = if (this == Sleep) 10 else 6

    /**
     * Rota de deep-link ao tocar na notificação.
     * Conquista/Bônus abrem Achievements (hoje); Backup abre a tela de Backup; o resto abre a Today.
     */
    val route: NotificationRoute
        get() = when (this) {
            Achievement, StreakBonus -> NotificationRoute.AchievementsToday
            Backup -> NotificationRoute.Backup
            else -> NotificationRoute.Today
        }

    /**
     * Emoji representativo dos kinds de Meta (espelha o usado na Today/Settings).
     * Retorna null para kinds da seção Sistema (que usam ícone na UI).
     */
    val emoji: String?
        get() = when (this) {
            Workout -> "🏋️"
            Cardio -> "🏃"
            Sleep -> "💤"
            Water -> "💧"
            Calories -> "🔥"
            Protein -> "🍗"
            Carbs -> "🍞"
            GoodFat -> "🧈"
            Fiber -> "🌾"
            Creatine -> "🧪"
            else -> null
        }

    /** Chave `tracking.*` correspondente da meta (liga o kind à intake key da Today). */
    val trackingKey: String?
        get() = when (this) {
            Workout -> "tracking.workout"
            Cardio -> "tracking.cardio"
            Sleep -> "tracking.sleep"
            Water -> "tracking.water"
            Calories -> "tracking.calories"
            Protein -> "tracking.protein"
            Carbs -> "tracking.carbs"
            GoodFat -> "tracking.goodFat"
            Fiber -> "tracking.fiber"
            Creatine -> "tracking.creatine"
            else -> null
        }

    companion object {
        /** Resolve a partir do rawValue persistido; null se desconhecido. */
        fun fromRaw(raw: String?): NotificationKind? = entries.firstOrNull { it.rawValue == raw }

        /** Inverso de [trackingKey]: resolve um kind a partir da chave da meta. */
        fun fromTrackingKey(key: String): NotificationKind? = entries.firstOrNull { it.trackingKey == key }
    }
}
