package com.jonathaxs.gymnutshell.core.widget

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** Cobre a matemática de cor do fundo dos widgets (contraste + endpoints do gradiente). */
class WidgetBackgroundTest {

    @Test
    fun `texto preto em fundo claro e branco em fundo escuro`() {
        assertEquals(0xFF000000L, WidgetBackground.contrastingTextArgb(0xFFFFFFFF)) // branco -> preto
        assertEquals(0xFFFFFFFFL, WidgetBackground.contrastingTextArgb(0xFF000000)) // preto -> branco
        assertEquals(0xFFFFFFFFL, WidgetBackground.contrastingTextArgb(0xFF007AFF)) // azul (escuro) -> branco
    }

    @Test
    fun `gradiente clareia no topo e escurece na base`() {
        val base = 0xFF3366CC
        val lighter = WidgetBackground.lighterArgb(base)
        val darker = WidgetBackground.darkerArgb(base)
        // O canal azul (mais alto) deve subir no lighter e cair no darker.
        val baseBlue = base and 0xFF
        assertTrue((lighter and 0xFF) > baseBlue)
        assertTrue((darker and 0xFF) < baseBlue)
        // Alpha preservado opaco.
        assertEquals(0xFFL, (lighter shr 24) and 0xFF)
        assertEquals(0xFFL, (darker shr 24) and 0xFF)
    }
}
