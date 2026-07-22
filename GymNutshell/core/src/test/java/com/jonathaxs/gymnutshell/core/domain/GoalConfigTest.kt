package com.jonathaxs.gymnutshell.core.domain

import com.jonathaxs.gymnutshell.core.data.CustomGoalCategory
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Trava a reconciliação de [UnifiedCategoryOrder] — porte de UnifiedCategoryOrderStore.load (iOS).
 * O ponto sensível é sempre o mesmo: itens conhecidos na ordem salva, o resto (categorias novas)
 * anexado no fim, e ids órfãos descartados sem quebrar.
 */
class GoalConfigTest {

    private fun custom(id: String) = CustomGoalCategory(id = id, name = "Cat $id")

    // Ids das 4 categorias fixas, na ordem do enum (Essencial primeiro).
    private val builtinIds =
        listOf("builtin:essencial", "builtin:nutricao", "builtin:treino", "builtin:suplemento")

    @Test
    fun `sem ordem salva devolve as fixas na ordem do enum, depois as custom`() {
        val ids = UnifiedCategoryOrder.resolve(emptyList(), listOf(custom("a"), custom("b"))).map { it.id }

        assertEquals(builtinIds + listOf("custom:a", "custom:b"), ids)
    }

    @Test
    fun `respeita a ordem salva`() {
        val saved = listOf("builtin:suplemento", "builtin:essencial", "builtin:treino", "builtin:nutricao")
        val ids = UnifiedCategoryOrder.resolve(saved, emptyList()).map { it.id }

        assertEquals(saved, ids)
    }

    @Test
    fun `anexa no fim a categoria custom nova ainda sem ordem`() {
        // Ordem salva conhece todas as fixas mas não a custom recém-criada.
        val ids = UnifiedCategoryOrder.resolve(builtinIds, listOf(custom("nova"))).map { it.id }

        assertEquals(builtinIds + "custom:nova", ids)
    }

    @Test
    fun `ignora id salvo sem categoria correspondente`() {
        val saved = listOf("custom:fantasma", "builtin:essencial")
        val ids = UnifiedCategoryOrder.resolve(saved, emptyList()).map { it.id }

        assert("custom:fantasma" !in ids)
        assertEquals("builtin:essencial", ids.first())
        // As demais fixas continuam aparecendo (anexadas no fim).
        assertEquals(GoalCategory.entries.size, ids.size)
    }
}
