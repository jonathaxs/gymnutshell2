package com.jonathaxs.gymnutshell.core.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

/**
 * Trava o mapa tema × tier. São 19 temas × 4 níveis escritos à mão, então o erro provável é
 * copiar/colar (repetir uma chave ou pular um nível) — é isso que os testes de unicidade pegam.
 * O texto em si não é checado aqui: sem Robolectric o @StringRes é só o id.
 */
class AppThemeTest {

    @Test
    fun everyThemeAndTierHasItsOwnName() {
        val ids = AppTheme.entries.flatMap { theme ->
            DailyAchievement.entries.map { tier -> theme.tierNameRes(tier) }
        }
        assertEquals(19 * 4, ids.size)
        assertEquals("nome de tier repetido entre tema/nível", ids.size, ids.distinct().size)
    }

    /** Dentro de um tema, os 4 níveis têm que ser nomes diferentes (pega pick() mal preenchido). */
    @Test
    fun tiersWithinAThemeAreDistinct() {
        AppTheme.entries.forEach { theme ->
            val ids = DailyAchievement.entries.map { theme.tierNameRes(it) }
            assertEquals("tema $theme repete nome entre níveis", 4, ids.distinct().size)
        }
    }

    /** Espelha AppTheme.emoji(for:)/name(for:) do iOS: cada tema tem os 4 emojis distintos. */
    @Test
    fun everyThemeHasFourPreviewEmojis() {
        AppTheme.entries.forEach { theme ->
            assertEquals(4, theme.previewEmojis.size)
            assertEquals("tema $theme repete emoji entre níveis", 4, theme.previewEmojis.distinct().size)
        }
    }

    @Test
    fun defaultThemeIsGym() {
        assertEquals(AppTheme.Gym, AppTheme.Default)
        assertEquals(AppTheme.Gym, AppTheme.fromRaw("desconhecido"))
        assertEquals(AppTheme.Cat, AppTheme.fromRaw("cat"))
    }

    /** O tema muda o nome do mesmo nível — é o ponto todo da feature. */
    @Test
    fun themeChangesTheTierName() {
        assertNotEquals(
            AppTheme.Gym.tierNameRes(DailyAchievement.Level1),
            AppTheme.Cat.tierNameRes(DailyAchievement.Level1),
        )
    }
}
