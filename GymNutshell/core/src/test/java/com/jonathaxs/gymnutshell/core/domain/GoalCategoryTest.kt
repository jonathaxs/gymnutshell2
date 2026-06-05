package com.jonathaxs.gymnutshell.core.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class GoalCategoryTest {

    @Test
    fun `defaultCategory mapeia as chaves fixas`() {
        assertEquals(GoalCategory.Essencial, GoalCategory.defaultCategory("tracking.water"))
        assertEquals(GoalCategory.Essencial, GoalCategory.defaultCategory("tracking.sleep"))
        assertEquals(GoalCategory.Nutricao, GoalCategory.defaultCategory("tracking.protein"))
        assertEquals(GoalCategory.Treino, GoalCategory.defaultCategory("tracking.workout"))
        assertEquals(GoalCategory.Suplemento, GoalCategory.defaultCategory("tracking.creatine"))
    }

    @Test
    fun `chave desconhecida devolve null`() {
        assertNull(GoalCategory.defaultCategory("tracking.unknown"))
    }

    @Test
    fun `ordem das categorias comeca por Essencial`() {
        assertEquals(GoalCategory.Essencial, GoalCategory.entries.first())
    }
}
