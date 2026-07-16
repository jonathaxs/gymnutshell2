package com.jonathaxs.gymnutshell.ui.components

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertContentDescriptionContains
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Garante que a lista agrupada (base de todas as telas de Ajustes) fala direito no TalkBack:
 * a linha é um botão único, o chevron é mudo e o checkmark vira "Selected".
 */
@RunWith(AndroidJUnit4::class)
class GroupedListA11yTest {

    @get:Rule
    val rule = createComposeRule()

    @Test
    fun clickableRowIsAnnouncedAsButton() {
        rule.setContent {
            GroupCard { GroupRow(title = "Your data", showChevron = true, onClick = {}) }
        }
        rule.onNodeWithText("Your data")
            .assert(SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.Button))
    }

    /** O "›" é decorativo: o Role.Button já diz que a linha navega. */
    @Test
    fun chevronIsNotAnnounced() {
        rule.setContent {
            GroupCard { GroupRow(title = "Your data", showChevron = true, onClick = {}) }
        }
        rule.onNodeWithText("›", useUnmergedTree = true).assertDoesNotExist()
    }

    /** O checkmark das linhas de seleção precisa virar "Selected", não o glifo cru. */
    @Test
    fun checkmarkIsAnnouncedAsSelected() {
        rule.setContent {
            GroupCard {
                GroupRow(title = "Portrait", trailing = { GroupCheck(Color.Blue) }, onClick = {})
            }
        }
        rule.onNodeWithText("Portrait").assertContentDescriptionContains("Selected")
    }

    /** A dica de ação (porte do accessibilityHint do iOS) chega como rótulo do clique. */
    @Test
    fun rowExposesClickAction() {
        rule.setContent {
            GroupCard { GroupRow(title = "Your data", showChevron = true, onClick = {}) }
        }
        val node = rule.onNodeWithText("Your data").fetchSemanticsNode()
        assertEquals(true, node.config.contains(SemanticsActions.OnClick))
    }
}
