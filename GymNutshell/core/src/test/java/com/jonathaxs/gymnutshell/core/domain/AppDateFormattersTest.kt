package com.jonathaxs.gymnutshell.core.domain

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

class AppDateFormattersTest {

    @Test
    fun `dayKey gera yyyy-MM-dd com zero a esquerda`() {
        assertEquals("2026-05-16", AppDateFormatters.dayKey(LocalDate.of(2026, 5, 16)))
        assertEquals("2026-01-05", AppDateFormatters.dayKey(LocalDate.of(2026, 1, 5)))
    }

    @Test
    fun `epoch-day faz ida e volta sem perder o dia`() {
        val date = LocalDate.of(2026, 5, 16)
        val epochDay = date.toEpochDay()
        assertEquals(date, LocalDate.ofEpochDay(epochDay))
        // a chave reconstruída a partir do epoch-day bate com a original
        assertEquals("2026-05-16", AppDateFormatters.dayKey(LocalDate.ofEpochDay(epochDay)))
    }
}
