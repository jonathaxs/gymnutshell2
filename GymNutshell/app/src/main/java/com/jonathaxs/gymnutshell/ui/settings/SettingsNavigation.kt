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
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.navArgument
import com.jonathaxs.gymnutshell.R

/** Rotas da Settings. GRAPH casa com o route da aba (MainTab.Settings). */
object SettingsRoutes {
    const val GRAPH = "settings"
    const val LIST = "settings_list"
    const val COLOR = "settings_color"
    const val PHYSICAL = "settings_physical"
    const val GOAL = "settings_goal"
    const val THEME = "settings_theme"
    const val CUSTOM_GOALS = "settings_custom_goals" // hub de Goals (porte de TrackingGoalsSettingsView)
    const val ADD_GOAL = "settings_add_goal" // + "?goalId={goalId}" pra editar
    const val GOAL_DETAIL = "settings_goal_detail" // + "/{key}"
    const val CATEGORIES = "settings_categories"
    const val UNITS = "settings_units"
    const val ORIENTATION = "settings_orientation_pref"
    const val NOTIFICATIONS = "settings_notifications"
    const val NOTIF_EDIT = "settings_notif_edit" // + "/{target}"
    const val HEALTH = "settings_health"
    const val BACKUP = "settings_backup"
    const val LANGUAGE = "settings_language"
    const val WIDGET_BG = "settings_widget_bg"
    const val ABOUT = "settings_about"
    const val WEAR = "settings_wear"
    const val RING_INFO = "settings_ring_info"
    const val TIER_INFO = "settings_tier_info"
    const val BONUS_INFO = "settings_bonus_info"
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
                onOpenOrientation = { navController.navigate(SettingsRoutes.ORIENTATION) },
                onOpenNotifications = { navController.navigate(SettingsRoutes.NOTIFICATIONS) },
                onOpenHealth = { navController.navigate(SettingsRoutes.HEALTH) },
                onOpenBackup = { navController.navigate(SettingsRoutes.BACKUP) },
                onOpenLanguage = { navController.navigate(SettingsRoutes.LANGUAGE) },
                onOpenWidgetBackground = { navController.navigate(SettingsRoutes.WIDGET_BG) },
                onOpenAbout = { navController.navigate(SettingsRoutes.ABOUT) },
                onOpenWear = { navController.navigate(SettingsRoutes.WEAR) },
                onOpenRingInfo = { navController.navigate(SettingsRoutes.RING_INFO) },
                onOpenTierInfo = { navController.navigate(SettingsRoutes.TIER_INFO) },
                onOpenBonusInfo = { navController.navigate(SettingsRoutes.BONUS_INFO) },
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
            GoalsScreen(
                onBack = { navController.popBackStack() },
                onAddGoal = { navController.navigate(SettingsRoutes.ADD_GOAL) },
                onEditGoal = { id -> navController.navigate("${SettingsRoutes.ADD_GOAL}?goalId=$id") },
                onOpenGoalDetail = { key -> navController.navigate("${SettingsRoutes.GOAL_DETAIL}/$key") },
                onOpenCategories = { navController.navigate(SettingsRoutes.CATEGORIES) },
            )
        }
        composable(
            route = "${SettingsRoutes.ADD_GOAL}?goalId={goalId}",
            arguments = listOf(
                navArgument("goalId") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                },
            ),
        ) { entry ->
            AddGoalScreen(
                goalId = entry.arguments?.getString("goalId")?.toLongOrNull(),
                onBack = { navController.popBackStack() },
            )
        }
        composable(
            route = "${SettingsRoutes.GOAL_DETAIL}/{key}",
            arguments = listOf(navArgument("key") { type = NavType.StringType }),
        ) { entry ->
            GoalDetailScreen(
                goalKey = entry.arguments?.getString("key").orEmpty(),
                onBack = { navController.popBackStack() },
            )
        }
        composable(SettingsRoutes.CATEGORIES) {
            CategoriesScreen(onBack = { navController.popBackStack() })
        }
        composable(SettingsRoutes.UNITS) {
            MeasurementScreen(onBack = { navController.popBackStack() })
        }
        composable(SettingsRoutes.ORIENTATION) {
            OrientationSettingsScreen(onBack = { navController.popBackStack() })
        }
        composable(SettingsRoutes.NOTIFICATIONS) {
            NotificationsSettingsScreen(
                onBack = { navController.popBackStack() },
                onEditTarget = { target -> navController.navigate("${SettingsRoutes.NOTIF_EDIT}/$target") },
            )
        }
        composable(SettingsRoutes.HEALTH) {
            HealthSettingsScreen(onBack = { navController.popBackStack() })
        }
        composable(SettingsRoutes.BACKUP) {
            BackupScreen(onBack = { navController.popBackStack() })
        }
        composable(SettingsRoutes.LANGUAGE) {
            LanguageScreen(onBack = { navController.popBackStack() })
        }
        composable(SettingsRoutes.WIDGET_BG) {
            WidgetBackgroundScreen(onBack = { navController.popBackStack() })
        }
        composable(SettingsRoutes.ABOUT) {
            AboutScreen(onBack = { navController.popBackStack() })
        }
        composable(SettingsRoutes.WEAR) {
            WearInstructionsScreen(onBack = { navController.popBackStack() })
        }
        composable(SettingsRoutes.RING_INFO) {
            ProgressRingInfoScreen(onBack = { navController.popBackStack() })
        }
        composable(SettingsRoutes.TIER_INFO) {
            TierInfoScreen(
                onBack = { navController.popBackStack() },
                onOpenTheme = { navController.navigate(SettingsRoutes.THEME) },
            )
        }
        composable(SettingsRoutes.BONUS_INFO) {
            StreakBonusInfoScreen(onBack = { navController.popBackStack() })
        }
        composable(
            route = "${SettingsRoutes.NOTIF_EDIT}/{target}",
            arguments = listOf(navArgument("target") { type = NavType.StringType }),
        ) { entry ->
            NotificationIntervalEditScreen(
                target = entry.arguments?.getString("target").orEmpty(),
                onBack = { navController.popBackStack() },
            )
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
