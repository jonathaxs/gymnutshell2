package com.jonathaxs.gymnutshell.core.domain

/**
 * Som disponível para notificações locais — porte do NotificationSound (iOS).
 *
 * No Android o som é definido no canal (NotificationChannel, API 26+): `Default` usa o som
 * padrão do canal e `Silent` posta sem som. A escolha por kind é mapeada na camada de UI/serviço.
 */
enum class NotificationSound(val rawValue: String) {
    Default("default"),
    Silent("silent");

    companion object {
        /** Resolve a partir do rawValue persistido; cai pra Default se inválido. */
        fun fromRaw(raw: String?): NotificationSound = entries.firstOrNull { it.rawValue == raw } ?: Default
    }
}
