package com.jonathaxs.gymnutshell.core.domain

import kotlin.math.roundToInt

/**
 * Converte peso e altura entre métrico e imperial — porte de UnitConverter (iOS).
 * O app sempre guarda os valores internamente em kg e cm.
 *
 * Onde o iOS devolvia tuplas nomeadas (stones:lbs / feet:inches), aqui usamos Pair:
 * `first` = stones/feet, `second` = lbs/inches (dá pra desestruturar: `val (s, l) = ...`).
 */
object UnitConverter {

    // MARK: Peso (US: libras)
    fun kgToLbs(kg: Double): Double = kg * 2.20462
    fun lbsToKg(lbs: Double): Double = lbs / 2.20462

    // MARK: Peso (UK: stones + libras)
    fun kgToStoneLbs(kg: Double): Pair<Int, Int> {
        val totalLbs = kg * 2.20462
        val stones = (totalLbs / 14).toInt()
        val remainingLbs = totalLbs.roundToInt() - stones * 14
        return stones to remainingLbs
    }

    fun stoneLbsToKg(stones: Int, lbs: Int): Double = (stones * 14 + lbs) / 2.20462

    // MARK: Água
    fun mlToFlOz(ml: Double): Double = ml / 29.5735
    fun flOzToMl(flOz: Double): Double = flOz * 29.5735

    // MARK: Altura
    fun cmToFeetAndInches(cm: Int): Pair<Int, Int> {
        val totalInches = (cm / 2.54).roundToInt()
        return (totalInches / 12) to (totalInches % 12)
    }

    fun feetAndInchesToCm(feet: Int, inches: Int): Int =
        ((feet * 12 + inches) * 2.54).roundToInt()
}
