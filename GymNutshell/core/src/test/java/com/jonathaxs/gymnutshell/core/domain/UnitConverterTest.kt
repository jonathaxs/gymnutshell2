package com.jonathaxs.gymnutshell.core.domain

import org.junit.Assert.assertEquals
import org.junit.Test

class UnitConverterTest {

    @Test
    fun `kg e lbs convertem e voltam`() {
        assertEquals(220.462, UnitConverter.kgToLbs(100.0), 1e-3)
        assertEquals(100.0, UnitConverter.lbsToKg(220.462), 1e-3)
    }

    @Test
    fun `kg para stones e libras`() {
        // 100 kg = 220.462 lbs → 15 stones e 10 lbs
        val (stones, lbs) = UnitConverter.kgToStoneLbs(100.0)
        assertEquals(15, stones)
        assertEquals(10, lbs)
    }

    @Test
    fun `stones e libras voltam pra kg`() {
        assertEquals(99.79, UnitConverter.stoneLbsToKg(stones = 15, lbs = 10), 1e-2)
    }

    @Test
    fun `agua ml e flOz convertem e voltam`() {
        assertEquals(16.907, UnitConverter.mlToFlOz(500.0), 1e-3)
        assertEquals(500.0, UnitConverter.flOzToMl(16.907), 1e-2)
    }

    @Test
    fun `cm para pes e polegadas`() {
        // 180 cm → 5 pés e 11 polegadas
        val (feet, inches) = UnitConverter.cmToFeetAndInches(180)
        assertEquals(5, feet)
        assertEquals(11, inches)
    }

    @Test
    fun `pes e polegadas voltam pra cm`() {
        assertEquals(180, UnitConverter.feetAndInchesToCm(feet = 5, inches = 11))
    }
}
