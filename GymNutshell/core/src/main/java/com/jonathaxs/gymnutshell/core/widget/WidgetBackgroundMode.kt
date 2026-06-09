package com.jonathaxs.gymnutshell.core.widget

/**
 * Modo de fundo dos widgets escolhido pelo usuário — porte de WidgetBackgroundMode (iOS).
 * `rawValue` persiste no DataStore. Default é Accent (igual iOS): widgets já saem com a cor do app.
 */
enum class WidgetBackgroundMode(val rawValue: String) {
    System("system"),   // Fundo padrão do sistema (claro/escuro)
    Accent("accent"),   // Cor de destaque do app
    Custom("custom");   // Cor personalizada escolhida pelo usuário

    companion object {
        val Default = Accent
        fun fromRaw(raw: String?): WidgetBackgroundMode = entries.firstOrNull { it.rawValue == raw } ?: Default
    }
}
