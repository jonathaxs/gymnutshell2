package com.jonathaxs.gymnutshell.ui.settings

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.jonathaxs.gymnutshell.R
import com.jonathaxs.gymnutshell.core.domain.AppTheme
import com.jonathaxs.gymnutshell.core.domain.DailyAchievement
import com.jonathaxs.gymnutshell.core.domain.ThemeCategory
import com.jonathaxs.gymnutshell.ui.components.GroupCheck
import com.jonathaxs.gymnutshell.ui.components.GroupRow
import com.jonathaxs.gymnutshell.ui.components.GroupRowDivider
import com.jonathaxs.gymnutshell.ui.components.GroupSection
import com.jonathaxs.gymnutshell.ui.theme.color

/** Sub-tela de tema — porte de ThemeSettingsView (iOS): mascotes agrupados por categoria. */
@Composable
fun ThemeSettingsScreen(onBack: () -> Unit, viewModel: SettingsViewModel = viewModel()) {
    val selected by viewModel.theme.collectAsStateWithLifecycle()
    val accent = viewModel.accentColor.collectAsStateWithLifecycle().value.color
    // Tema cujo sheet de níveis está aberto (null = nenhum), igual ao $infoTheme do iOS.
    var infoTheme by remember { mutableStateOf<AppTheme?>(null) }

    Scaffold(topBar = { SettingsTopBar(stringResource(R.string.settings_theme), onBack) }) { padding ->
        LazyColumn(modifier = Modifier.fillMaxSize().padding(padding)) {
            ThemeCategory.entries.forEach { category ->
                item(key = "cat_${category.name}") {
                    GroupSection(title = stringResource(categoryNameRes(category)), titleColor = accent) {
                        AppTheme.inCategory(category).forEachIndexed { index, theme ->
                            if (index > 0) GroupRowDivider()
                            ThemeRow(
                                theme = theme,
                                selected = theme == selected,
                                accent = accent,
                                onClick = { viewModel.setTheme(theme) },
                                onInfo = { infoTheme = theme },
                            )
                        }
                    }
                }
            }
            item(key = "bottom_spacer") { Spacer(Modifier.height(24.dp)) }
        }
    }

    // Sheet com os 4 níveis do tema tocado — porte do ThemeInfoView (iOS, sempre apresentado como sheet).
    infoTheme?.let { theme ->
        ThemeInfoSheet(theme = theme, onDismiss = { infoTheme = null })
    }
}

/**
 * Linha de tema: nome + prévia dos 4 emojis; checkmark (na cor de destaque) quando selecionado
 * e botão (i) que abre o sheet de níveis. O botão fica fora da área de toque que seleciona o tema.
 */
@Composable
private fun ThemeRow(
    theme: AppTheme,
    selected: Boolean,
    accent: Color,
    onClick: () -> Unit,
    onInfo: () -> Unit,
) {
    val themeName = stringResource(themeNameRes(theme))
    // Linha selecionada ganha fundo na cor de destaque e conteúdo branco — igual ao listRowBackground do iOS.
    // O Surface do GroupCard já recorta os cantos, então a primeira/última linha herdam o arredondamento.
    val contentColor = if (selected) Color.White else MaterialTheme.colorScheme.onSurface
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp)
            .then(if (selected) Modifier.background(accent) else Modifier)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(start = 20.dp, top = 12.dp, bottom = 12.dp, end = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text(themeName, style = MaterialTheme.typography.bodyLarge, color = contentColor)
            // Prévia decorativa: ler a lista de emojis do nível não acrescenta nada ao nome do tema.
            Text(
                theme.previewEmojis.joinToString("  "),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.clearAndSetSemantics {},
            )
        }
        if (selected) {
            Spacer(Modifier.width(8.dp))
            GroupCheck(Color.White)
        }
        IconButton(onClick = onInfo) {
            Icon(
                Icons.Outlined.Info,
                contentDescription = stringResource(R.string.a11y_theme_info, themeName),
                tint = if (selected) Color.White else accent,
            )
        }
    }
}

/** Sheet informativo dos 4 níveis de um tema: emoji + nível + faixa de progresso. Porte do ThemeInfoView (iOS). */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ThemeInfoSheet(theme: AppTheme, onDismiss: () -> Unit) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val rangeSuffix = stringResource(R.string.tier_info_range_suffix)
    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        // Título = nome do tema, como o navigationTitle do ThemeInfoView.
        InfoSheetTitle(stringResource(themeNameRes(theme)))
        Column(
            Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(bottom = 24.dp),
        ) {
            GroupSection(title = stringResource(R.string.tier_info_section_levels)) {
                DailyAchievement.entries.forEachIndexed { index, tier ->
                    if (index > 0) GroupRowDivider()
                    GroupRow(
                        // Nomes de tier por tema ainda não existem no Android; até lá o título é "Level N".
                        title = stringResource(R.string.ring_info_level_label, index + 1),
                        subtitle = tierRange(tier) + rangeSuffix,
                        leading = { Text(theme.emoji(tier), style = MaterialTheme.typography.headlineSmall) },
                    )
                }
            }
        }
    }
}

@StringRes
private fun categoryNameRes(category: ThemeCategory): Int = when (category) {
    ThemeCategory.Sport -> R.string.theme_cat_sport
    ThemeCategory.Animals -> R.string.theme_cat_animals
    ThemeCategory.Warrior -> R.string.theme_cat_warrior
    ThemeCategory.Space -> R.string.theme_cat_space
    ThemeCategory.Elements -> R.string.theme_cat_elements
    ThemeCategory.Competition -> R.string.theme_cat_competition
}

@StringRes
internal fun themeNameRes(theme: AppTheme): Int = when (theme) {
    AppTheme.Gym -> R.string.theme_gym
    AppTheme.Running -> R.string.theme_running
    AppTheme.Cat -> R.string.theme_cat
    AppTheme.Dog -> R.string.theme_dog
    AppTheme.Bear -> R.string.theme_bear
    AppTheme.Dino -> R.string.theme_dino
    AppTheme.Dragon -> R.string.theme_dragon
    AppTheme.Horse -> R.string.theme_horse
    AppTheme.Ocean -> R.string.theme_ocean
    AppTheme.Monkey -> R.string.theme_monkey
    AppTheme.Bird -> R.string.theme_bird
    AppTheme.Doctor -> R.string.theme_doctor
    AppTheme.Ninja -> R.string.theme_ninja
    AppTheme.Fire -> R.string.theme_fire
    AppTheme.Plant -> R.string.theme_plant
    AppTheme.Astronaut -> R.string.theme_astronaut
    AppTheme.Celestial -> R.string.theme_celestial
    AppTheme.Champion -> R.string.theme_champion
    AppTheme.Number -> R.string.theme_number
}
