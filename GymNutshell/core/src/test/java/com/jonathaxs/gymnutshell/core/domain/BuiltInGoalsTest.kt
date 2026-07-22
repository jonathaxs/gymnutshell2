package com.jonathaxs.gymnutshell.core.domain

import org.junit.Assert.assertEquals
import org.junit.Test

class BuiltInGoalsTest {

    @Test
    fun `gera as 10 metas fixas usando os alvos do Result`() {
        val result = GoalsCalculator.calculate(80.0, 180, 30, "male", UserGoal.Maintenance)
        val goals = BuiltInGoals.forResult(result)

        assertEquals(10, goals.size)

        val water = goals.first { it.key == "tracking.water" }
        assertEquals(result.water, water.target)
        assertEquals("💧", water.emoji)
        assertEquals(250, water.increment)

        val protein = goals.first { it.key == "tracking.protein" }
        assertEquals(result.protein, protein.target)
    }

    @Test
    fun `ordem segue as categorias - Essencial antes de Treino`() {
        val goals = BuiltInGoals.forResult(GoalsCalculator.calculate(80.0, 180, 30, "male", UserGoal.Maintenance))
        val keys = goals.map { it.key }

        // Essencial (sleep/water) vem primeiro
        assertEquals("tracking.sleep", keys.first())
        // Treino (workout/cardio) vem DEPOIS de Nutrição (protein) — o bug histórico do iOS
        assert(keys.indexOf("tracking.workout") > keys.indexOf("tracking.protein"))
        assert(keys.indexOf("tracking.water") < keys.indexOf("tracking.workout"))
    }

    // MARK: - active(result, config): trava ordem/remoção/overrides do hub de Goals.

    private val result = GoalsCalculator.calculate(80.0, 180, 30, "male", UserGoal.Maintenance)

    @Test
    fun `active com config padrao é idêntico a forResult`() {
        // Config default = ordem padrão, nada removido, sem override → passthrough puro.
        assertEquals(BuiltInGoals.forResult(result), BuiltInGoals.active(result, GoalConfig()))
    }

    @Test
    fun `active descarta as metas removidas`() {
        val config = GoalConfig(removedKeys = setOf("tracking.goodFat", "tracking.creatine"))
        val keys = BuiltInGoals.active(result, config).map { it.key }

        assertEquals(8, keys.size)
        assert("tracking.goodFat" !in keys)
        assert("tracking.creatine" !in keys)
    }

    @Test
    fun `active aplica override de valor e incremento só na meta editada`() {
        val config = GoalConfig(
            valueOverrides = mapOf("tracking.water" to 3000),
            incrementOverrides = mapOf("tracking.water" to 500),
        )
        val goals = BuiltInGoals.active(result, config)

        val water = goals.first { it.key == "tracking.water" }
        assertEquals(3000, water.target)
        assertEquals(500, water.increment)

        // As outras seguem a base (protein não foi editada).
        val protein = goals.first { it.key == "tracking.protein" }
        assertEquals(result.protein, protein.target)
        assertEquals(20, protein.increment)
    }

    @Test
    fun `active respeita a ordem definida pelo usuário`() {
        val reversed = GoalOrder.DEFAULT.reversed()
        val keys = BuiltInGoals.active(result, GoalConfig(fixedOrder = reversed)).map { it.key }

        assertEquals(reversed, keys)
    }

    @Test
    fun `active anexa no fim as chaves ausentes na ordem salva`() {
        // Ordem salva incompleta (só creatine): as outras 9 vêm depois, na ordem natural.
        val keys = BuiltInGoals.active(result, GoalConfig(fixedOrder = listOf("tracking.creatine"))).map { it.key }

        assertEquals(10, keys.size)
        assertEquals("tracking.creatine", keys.first())
        assertEquals(
            BuiltInGoals.forResult(result).map { it.key }.filter { it != "tracking.creatine" },
            keys.drop(1),
        )
    }

    @Test
    fun `active ignora chave desconhecida na ordem salva`() {
        val config = GoalConfig(fixedOrder = listOf("tracking.bogus") + GoalOrder.DEFAULT)
        val keys = BuiltInGoals.active(result, config).map { it.key }

        assertEquals(GoalOrder.DEFAULT, keys)
        assert("tracking.bogus" !in keys)
    }

    @Test
    fun `remoção vence override - meta removida não reaparece por ter valor editado`() {
        val config = GoalConfig(
            removedKeys = setOf("tracking.goodFat"),
            valueOverrides = mapOf("tracking.goodFat" to 99),
        )
        val keys = BuiltInGoals.active(result, config).map { it.key }

        assert("tracking.goodFat" !in keys)
    }
}
