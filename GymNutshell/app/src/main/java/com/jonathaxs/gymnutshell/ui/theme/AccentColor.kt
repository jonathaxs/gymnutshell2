package com.jonathaxs.gymnutshell.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * Cor de destaque configurável pelo usuário — porte do AppAccentColor (iOS/GymNutshellCore).
 *
 * As 8 cores espelham a paleta de sistema da Apple usada no app iOS, pra manter
 * a identidade visual idêntica entre as plataformas.
 */
enum class AccentColor(val color: Color) {
    Red(Color(0xFFFF3B30)),
    Blue(Color(0xFF007AFF)),
    Purple(Color(0xFFAF52DE)),
    Green(Color(0xFF34C759)),
    Yellow(Color(0xFFFFCC00)),
    Orange(Color(0xFFFF9500)),
    Cyan(Color(0xFF32ADE6)),
    Pink(Color(0xFFFF2D55));

    companion object {
        /** Chave de persistência (DataStore na Fase 2) — espelha AppAccentColor.storageKey. */
        const val STORAGE_KEY = "profile.accentColor"

        /** Cor padrão antes de o usuário customizar (≈ defaultForSex no iOS). */
        val Default = Blue
    }
}
