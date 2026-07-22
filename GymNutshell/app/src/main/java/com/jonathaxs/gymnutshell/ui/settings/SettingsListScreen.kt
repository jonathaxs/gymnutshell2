package com.jonathaxs.gymnutshell.ui.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.jonathaxs.gymnutshell.R
import com.jonathaxs.gymnutshell.ui.components.GroupRow
import com.jonathaxs.gymnutshell.ui.components.GroupRowDivider
import com.jonathaxs.gymnutshell.ui.components.GroupSection
import com.jonathaxs.gymnutshell.ui.theme.color
import com.jonathaxs.gymnutshell.ui.util.rememberIsTablet

/**
 * Lista raiz da Settings — porte da SettingsView (iOS): mesmas seções, na mesma ordem
 * (Profile → Preferences → System → About), agrupadas em cards arredondados (visual moderno).
 */
@Composable
fun SettingsListScreen(
    onOpenColor: () -> Unit,
    onOpenPhysical: () -> Unit,
    onOpenGoal: () -> Unit,
    onOpenTheme: () -> Unit,
    onOpenCustomGoals: () -> Unit,
    onOpenUnits: () -> Unit,
    onOpenOrientation: () -> Unit,
    onOpenNotifications: () -> Unit,
    onOpenHealth: () -> Unit,
    onOpenBackup: () -> Unit,
    onOpenLanguage: () -> Unit,
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
            GroupSection(title = stringResource(R.string.settings_section_profile), titleColor = accent.color) {
                GroupRow(stringResource(R.string.settings_physical_data), showChevron = true, onClick = onOpenPhysical)
                GroupRowDivider()
                GroupRow(stringResource(R.string.settings_fitness_goal), showChevron = true, onClick = onOpenGoal)
                GroupRowDivider()
                GroupRow(stringResource(R.string.settings_daily_goals), showChevron = true, onClick = onOpenCustomGoals)
            }

            // Seção Preferences: tema, cor, widgets e unidades.
            GroupSection(title = stringResource(R.string.settings_section_preferences), titleColor = accent.color) {
                GroupRow(stringResource(R.string.settings_theme), showChevron = true, onClick = onOpenTheme)
                GroupRowDivider()
                GroupRow(stringResource(R.string.settings_accent_color), showChevron = true, onClick = onOpenColor)
                GroupRowDivider()
                GroupRow(stringResource(R.string.settings_widget_background), showChevron = true, onClick = onOpenWidgetBackground)
                GroupRowDivider()
                GroupRow(stringResource(R.string.settings_units), showChevron = true, onClick = onOpenUnits)
                // Orientação só faz sentido no celular; o tablet sempre aceita ambas (igual ao iOS).
                if (!rememberIsTablet()) {
                    GroupRowDivider()
                    GroupRow(stringResource(R.string.settings_orientation), showChevron = true, onClick = onOpenOrientation)
                }
            }

            // Seção System: notificações, Health Connect e backup.
            GroupSection(title = stringResource(R.string.settings_section_system), titleColor = accent.color) {
                GroupRow(stringResource(R.string.settings_notifications), showChevron = true, onClick = onOpenNotifications)
                GroupRowDivider()
                GroupRow(stringResource(R.string.settings_health), showChevron = true, onClick = onOpenHealth)
                GroupRowDivider()
                GroupRow(stringResource(R.string.settings_backup), showChevron = true, onClick = onOpenBackup)
                GroupRowDivider()
                GroupRow(stringResource(R.string.settings_language), showChevron = true, onClick = onOpenLanguage)
            }

            // Seção About: páginas informativas (app, relógio, anel, tiers e bônus).
            GroupSection(title = stringResource(R.string.settings_section_about), titleColor = accent.color) {
                GroupRow(stringResource(R.string.settings_about_link), showChevron = true, onClick = onOpenAbout)
                GroupRowDivider()
                GroupRow(stringResource(R.string.settings_wear_os), showChevron = true, onClick = onOpenWear)
                GroupRowDivider()
                GroupRow(stringResource(R.string.settings_about_progress_ring), showChevron = true, onClick = onOpenRingInfo)
                GroupRowDivider()
                GroupRow(stringResource(R.string.settings_about_achievement), showChevron = true, onClick = onOpenTierInfo)
                GroupRowDivider()
                GroupRow(stringResource(R.string.settings_about_streak_bonus), showChevron = true, onClick = onOpenBonusInfo)
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}
