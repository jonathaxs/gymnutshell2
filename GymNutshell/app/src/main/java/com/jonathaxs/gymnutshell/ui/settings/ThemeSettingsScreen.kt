package com.jonathaxs.gymnutshell.ui.settings

import androidx.annotation.StringRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.jonathaxs.gymnutshell.R
import com.jonathaxs.gymnutshell.core.domain.AppTheme
import com.jonathaxs.gymnutshell.core.domain.ThemeCategory
import com.jonathaxs.gymnutshell.ui.theme.color

/** Sub-tela de tema — porte de ThemeSettingsView (iOS): mascotes agrupados por categoria. */
@Composable
fun ThemeSettingsScreen(onBack: () -> Unit, viewModel: SettingsViewModel = viewModel()) {
    val selected by viewModel.theme.collectAsStateWithLifecycle()
    val accent = viewModel.accentColor.collectAsStateWithLifecycle().value.color

    Scaffold(topBar = { SettingsTopBar(stringResource(R.string.settings_theme), onBack) }) { padding ->
        LazyColumn(modifier = Modifier.fillMaxSize().padding(padding)) {
            ThemeCategory.entries.forEach { category ->
                item(key = "cat_${category.name}") {
                    Text(
                        stringResource(categoryNameRes(category)),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    )
                }
                items(AppTheme.inCategory(category), key = { it.rawValue }) { theme ->
                    ThemeRow(
                        theme = theme,
                        selected = theme == selected,
                        checkColor = accent,
                        onClick = { viewModel.setTheme(theme) },
                    )
                }
            }
        }
    }
}

/** Linha de tema: nome + prévia dos 4 emojis; checkmark (na cor de destaque) quando selecionado. */
@Composable
private fun ThemeRow(
    theme: AppTheme,
    selected: Boolean,
    checkColor: androidx.compose.ui.graphics.Color,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text(stringResource(themeNameRes(theme)), style = MaterialTheme.typography.bodyLarge)
            Text(theme.previewEmojis.joinToString("  "), style = MaterialTheme.typography.titleMedium)
        }
        if (selected) {
            Spacer(Modifier.width(8.dp))
            Text("✓", color = checkColor, style = MaterialTheme.typography.titleLarge)
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
private fun themeNameRes(theme: AppTheme): Int = when (theme) {
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
