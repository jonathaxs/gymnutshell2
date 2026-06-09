package com.jonathaxs.gymnutshell.core.widget

/**
 * Matemática de cor do fundo dos widgets — porte de WidgetBackground (iOS). Livre de UI: opera
 * sobre ARGB cru (0xAARRGGBB). A camada de UI (:app) usa pra montar o gradiente e a cor de texto.
 */
object WidgetBackground {

    /** Cor personalizada padrão antes de o usuário escolher uma (azul, igual ao accent default). */
    const val DEFAULT_CUSTOM_ARGB = 0xFF007AFFL

    /**
     * Cor de texto que contrasta com a base: branco em fundos escuros, preto em claros.
     * Luminância perceptual (Rec. 601), threshold 0.6 (favorece branco), igual ao iOS.
     */
    fun contrastingTextArgb(baseArgb: Long): Long {
        val r = ((baseArgb shr 16) and 0xFF) / 255.0
        val g = ((baseArgb shr 8) and 0xFF) / 255.0
        val b = (baseArgb and 0xFF) / 255.0
        val luminance = 0.299 * r + 0.587 * g + 0.114 * b
        return if (luminance > 0.6) 0xFF000000L else 0xFFFFFFFFL
    }

    /** Topo do gradiente: base clareada (mistura com branco a 18%), igual ao iOS. */
    fun lighterArgb(baseArgb: Long): Long = mix(baseArgb) { c -> c + (1 - c) * 0.18 }

    /** Base do gradiente: base escurecida (×0.70), igual ao iOS. */
    fun darkerArgb(baseArgb: Long): Long = mix(baseArgb) { c -> c * (1 - 0.30) }

    private inline fun mix(argb: Long, transform: (Double) -> Double): Long {
        val r = channel(((argb shr 16) and 0xFF) / 255.0, transform)
        val g = channel(((argb shr 8) and 0xFF) / 255.0, transform)
        val b = channel((argb and 0xFF) / 255.0, transform)
        return 0xFF000000L or (r shl 16) or (g shl 8) or b
    }

    private inline fun channel(value: Double, transform: (Double) -> Double): Long =
        (transform(value).coerceIn(0.0, 1.0) * 255).toLong() and 0xFF
}
