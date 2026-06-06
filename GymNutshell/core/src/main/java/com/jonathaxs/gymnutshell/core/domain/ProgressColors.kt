package com.jonathaxs.gymnutshell.core.domain

/**
 * Mapeia progresso → cor do anel/barra — porte de ProgressColors (iOS).
 * Livre de UI: devolve ARGB cru (a camada de UI converte). Mesma escala em todas as superfícies.
 * Faixas: <33% vermelho, <66% laranja, <90% verde, <100% ciano, 100% azul (paleta de sistema da Apple).
 */
object ProgressColors {

    fun ringArgb(progress: Double): Long = when {
        progress < 0.33 -> 0xFFFF3B30 // vermelho
        progress < 0.66 -> 0xFFFF9500 // laranja
        progress < 0.90 -> 0xFF34C759 // verde
        progress < 1.0 -> 0xFF32ADE6  // ciano
        else -> 0xFF007AFF            // azul
    }
}
