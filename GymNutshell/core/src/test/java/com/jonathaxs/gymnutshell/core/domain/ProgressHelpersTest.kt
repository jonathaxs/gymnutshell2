package com.jonathaxs.gymnutshell.core.domain

import org.junit.Assert.assertEquals
import org.junit.Test

class ProgressHelpersTest {

    @Test
    fun `progresso fica fixado entre 0 e 1`() {
        assertEquals(0.0, ProgressHelpers.clampedProgress(0, 100), 1e-9)
        assertEquals(0.5, ProgressHelpers.clampedProgress(50, 100), 1e-9)
        assertEquals(1.0, ProgressHelpers.clampedProgress(150, 100), 1e-9)
    }

    @Test
    fun `meta zero ou negativa devolve zero`() {
        assertEquals(0.0, ProgressHelpers.clampedProgress(50, 0), 1e-9)
        assertEquals(0.0, ProgressHelpers.clampedProgress(50.0, 0.0), 1e-9)
    }

    @Test
    fun `overload de Double funciona`() {
        assertEquals(0.25, ProgressHelpers.clampedProgress(2.5, 10.0), 1e-9)
    }
}
