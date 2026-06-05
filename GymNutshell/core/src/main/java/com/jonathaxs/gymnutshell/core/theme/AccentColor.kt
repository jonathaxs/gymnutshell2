package com.jonathaxs.gymnutshell.core.theme

/**
 * Cor de destaque configurável pelo usuário — porte do AppAccentColor (iOS/GymNutshellCore).
 *
 * Mora no :core e é livre de UI: guarda a cor como valor cru ARGB (0xAARRGGBB).
 * Cada camada de UI (:app, :wear, :widget) converte pro seu próprio tipo de cor.
 * As 8 cores espelham a paleta de sistema da Apple usada no app iOS.
 */
enum class AccentColor(val argb: Long) {
    Red(0xFFFF3B30),
    Blue(0xFF007AFF),
    Purple(0xFFAF52DE),
    Green(0xFF34C759),
    Yellow(0xFFFFCC00),
    Orange(0xFFFF9500),
    Cyan(0xFF32ADE6),
    Pink(0xFFFF2D55);

    companion object {
        /** Chave de persistência (DataStore mais à frente) — espelha AppAccentColor.storageKey. */
        const val STORAGE_KEY = "profile.accentColor"

        /** Cor padrão antes de o usuário customizar (≈ defaultForSex no iOS). */
        val Default = Blue
    }
}
