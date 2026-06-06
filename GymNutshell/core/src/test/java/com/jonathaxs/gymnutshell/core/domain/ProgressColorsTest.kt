package com.jonathaxs.gymnutshell.core.domain

import org.junit.Assert.assertEquals
import org.junit.Test

class ProgressColorsTest {

    @Test
    fun `cor do anel por faixa de progresso`() {
        assertEquals(0xFFFF3B30, ProgressColors.ringArgb(0.0))   // vermelho
        assertEquals(0xFFFF3B30, ProgressColors.ringArgb(0.329))
        assertEquals(0xFFFF9500, ProgressColors.ringArgb(0.33))  // laranja
        assertEquals(0xFF34C759, ProgressColors.ringArgb(0.66))  // verde
        assertEquals(0xFF32ADE6, ProgressColors.ringArgb(0.90))  // ciano
        assertEquals(0xFF007AFF, ProgressColors.ringArgb(1.0))   // azul
    }
}
