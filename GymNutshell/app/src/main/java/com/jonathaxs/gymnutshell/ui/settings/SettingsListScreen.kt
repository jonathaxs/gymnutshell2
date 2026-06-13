package com.jonathaxs.gymnutshell.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.jonathaxs.gymnutshell.R
import com.jonathaxs.gymnutshell.ui.theme.color

/**
 * Lista raiz da Settings — porte da SettingsView (iOS): mesmas seções, na mesma ordem
 * (Profile → Preferences → System), com headers coloridos pela cor de destaque.
 * Itens do iOS ainda sem equivalente aqui: Orientation, Language e a seção About.
 */
@Composable
fun SettingsListScreen(
    onOpenColor: () -> Unit,
    onOpenPhysical: () -> Unit,
    onOpenGoal: () -> Unit,
    onOpenTheme: () -> Unit,
    onOpenCustomGoals: () -> Unit,
    onOpenUnits: () -> Unit,
    onOpenNotifications: () -> Unit,
    onOpenHealth: () -> Unit,
    onOpenBackup: () -> Unit,
    onOpenWidgetBackground: () -> Unit,
    onOpenAbout: () -> Unit,
    onOpenWear: () -> Unit,
    onOpenRingInfo: () -> Unit,
    onOpenTierInfo: () -> Unit,
    onOpenBonusInfo: () -> Unit,
    viewModel: SettingsViewModel = viewModel(),
) {
    val accent by viewModel.accentColor.collectAsStateWithLifecycle()

    Scaffold(topBar = { SettingsTopBar(stringResource(R.string.tab_settings)) }) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState()),
        ) {
            // Seção Profile: dados físicos, objetivo fitness e metas (settings.section.edit do iOS).
            SettingsSection(stringResource(R.string.settings_section_profile), accent.color) {
                SettingsRow(stringResource(R.string.settings_physical_data), onClick = onOpenPhysical)
                HorizontalDivider()
                SettingsRow(stringResource(R.string.settings_fitness_goal), onClick = onOpenGoal)
                HorizontalDivider()
                SettingsRow(stringResource(R.string.settings_custom_goals), onClick = onOpenCustomGoals)
            }

            // Seção Preferences: tema, cor, widgets e unidades.
            SettingsSection(stringResource(R.string.settings_section_preferences), accent.color) {
                SettingsRow(stringResource(R.string.settings_theme), onClick = onOpenTheme)
                HorizontalDivider()
                SettingsRow(stringResource(R.string.settings_accent_color), onClick = onOpenColor)
                HorizontalDivider()
                SettingsRow(stringResource(R.string.settings_widget_background), onClick = onOpenWidgetBackground)
                HorizontalDivider()
                SettingsRow(stringResource(R.string.settings_units), onClick = onOpenUnits)
            }

            // Seção System: notificações, Health Connect e backup.
            SettingsSection(stringResource(R.string.settings_section_system), accent.color) {
                SettingsRow(stringResource(R.string.settings_notifications), onClick = onOpenNotifications)
                HorizontalDivider()
                SettingsRow(stringResource(R.string.settings_health), onClick = onOpenHealth)
                HorizontalDivider()
                SettingsRow(stringResource(R.string.settings_backup), onClick = onOpenBackup)
            }

            // Seção About: páginas informativas (app, relógio, anel, tiers e bônus).
            SettingsSection(stringResource(R.string.settings_section_about), accent.color) {
                SettingsRow(stringResource(R.string.settings_about_link), onClick = onOpenAbout)
                HorizontalDivider()
                SettingsRow(stringResource(R.string.settings_wear_os), onClick = onOpenWear)
                HorizontalDivider()
                SettingsRow(stringResource(R.string.settings_about_progress_ring), onClick = onOpenRingInfo)
                HorizontalDivider()
                SettingsRow(stringResource(R.string.settings_about_achievement), onClick = onOpenTierInfo)
                HorizontalDivider()
                SettingsRow(stringResource(R.string.settings_about_streak_bonus), onClick = onOpenBonusInfo)
            }
        }
    }
}

/** Header colorido pela accent + linhas da seção, espelhando a Section do iOS. */
@Composable
private fun SettingsSection(title: String, accent: Color, content: @Composable () -> Unit) {
    Text(
        title,
        style = MaterialTheme.typography.titleSmall,
        color = accent,
        modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 24.dp, bottom = 4.dp),
    )
    content()
}

/** Linha clicável: título à esquerda, chevron à direita. */
@Composable
private fun SettingsRow(title: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(title, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyLarge)
        Text("›", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
