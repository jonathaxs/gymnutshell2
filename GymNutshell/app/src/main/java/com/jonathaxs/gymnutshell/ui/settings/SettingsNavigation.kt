package com.jonathaxs.gymnutshell.ui.settings

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import com.jonathaxs.gymnutshell.R

/** Rotas da Settings. GRAPH casa com o route da aba (MainTab.Settings). */
object SettingsRoutes {
    const val GRAPH = "settings"
    const val LIST = "settings_list"
    const val COLOR = "settings_color"
    const val PHYSICAL = "settings_physical"
    const val GOAL = "settings_goal"
    const val THEME = "settings_theme"
    const val CUSTOM_GOALS = "settings_custom_goals"
    const val UNITS = "settings_units"
}

/** Grafo aninhado da Settings: lista → sub-telas (cor, dados físicos). */
fun NavGraphBuilder.settingsGraph(navController: NavController) {
    navigation(startDestination = SettingsRoutes.LIST, route = SettingsRoutes.GRAPH) {
        composable(SettingsRoutes.LIST) {
            SettingsListScreen(
                onOpenColor = { navController.navigate(SettingsRoutes.COLOR) },
                onOpenPhysical = { navController.navigate(SettingsRoutes.PHYSICAL) },
                onOpenGoal = { navController.navigate(SettingsRoutes.GOAL) },
                onOpenTheme = { navController.navigate(SettingsRoutes.THEME) },
                onOpenCustomGoals = { navController.navigate(SettingsRoutes.CUSTOM_GOALS) },
                onOpenUnits = { navController.navigate(SettingsRoutes.UNITS) },
            )
        }
        composable(SettingsRoutes.COLOR) {
            ColorSettingsScreen(onBack = { navController.popBackStack() })
        }
        composable(SettingsRoutes.PHYSICAL) {
            PhysicalDataScreen(onBack = { navController.popBackStack() })
        }
        composable(SettingsRoutes.GOAL) {
            UserGoalScreen(onBack = { navController.popBackStack() })
        }
        composable(SettingsRoutes.THEME) {
            ThemeSettingsScreen(onBack = { navController.popBackStack() })
        }
        composable(SettingsRoutes.CUSTOM_GOALS) {
            CustomGoalsScreen(onBack = { navController.popBackStack() })
        }
        composable(SettingsRoutes.UNITS) {
            MeasurementScreen(onBack = { navController.popBackStack() })
        }
    }
}

/** Top bar das telas de Settings; mostra o botão de voltar quando `onBack` é fornecido. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsTopBar(title: String, onBack: (() -> Unit)? = null) {
    TopAppBar(
        title = { Text(title) },
        navigationIcon = {
            if (onBack != null) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.cd_back))
                }
            }
        },
    )
}
