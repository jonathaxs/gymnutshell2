package com.jonathaxs.gymnutshell.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.jonathaxs.gymnutshell.R

/** Lista raiz da Settings — porte da SettingsView (iOS): linhas que abrem sub-telas. */
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
) {
    Scaffold(topBar = { SettingsTopBar(stringResource(R.string.tab_settings)) }) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            SettingsRow(stringResource(R.string.settings_notifications), onClick = onOpenNotifications)
            HorizontalDivider()
            SettingsRow(stringResource(R.string.settings_health), onClick = onOpenHealth)
            HorizontalDivider()
            SettingsRow(stringResource(R.string.settings_backup), onClick = onOpenBackup)
            HorizontalDivider()
            SettingsRow(stringResource(R.string.settings_theme), onClick = onOpenTheme)
            HorizontalDivider()
            SettingsRow(stringResource(R.string.settings_accent_color), onClick = onOpenColor)
            HorizontalDivider()
            SettingsRow(stringResource(R.string.settings_custom_goals), onClick = onOpenCustomGoals)
            HorizontalDivider()
            SettingsRow(stringResource(R.string.settings_units), onClick = onOpenUnits)
            HorizontalDivider()
            SettingsRow(stringResource(R.string.settings_physical_data), onClick = onOpenPhysical)
            HorizontalDivider()
            SettingsRow(stringResource(R.string.settings_fitness_goal), onClick = onOpenGoal)
        }
    }
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
