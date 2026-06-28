package com.jonathaxs.gymnutshell.ui

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.jonathaxs.gymnutshell.R
import com.jonathaxs.gymnutshell.core.domain.NotificationRoute
import com.jonathaxs.gymnutshell.ui.achievements.AchievementsScreen
import com.jonathaxs.gymnutshell.ui.achievements.EditRecordScreen
import com.jonathaxs.gymnutshell.ui.history.NotificationHistoryScreen
import com.jonathaxs.gymnutshell.ui.progress.ProgressScreen
import com.jonathaxs.gymnutshell.ui.settings.SettingsRoutes
import com.jonathaxs.gymnutshell.ui.settings.settingsGraph
import com.jonathaxs.gymnutshell.ui.theme.color
import com.jonathaxs.gymnutshell.ui.today.TodayScreen
import com.jonathaxs.gymnutshell.ui.util.Breakpoints

/**
 * Abas principais — espelha o enum MainView.Tab do iOS. `route` é o destino de navegação;
 * o de Settings é o grafo aninhado (com sub-telas).
 */
private enum class MainTab(val route: String, @param:StringRes val labelRes: Int, val icon: ImageVector) {
    Today("today", R.string.tab_today, Icons.Default.Check),
    Achievements("achievements", R.string.tab_achievements, Icons.Default.DateRange),
    Progress("progress", R.string.tab_progress, Icons.Default.Star),
    Settings("settings", R.string.tab_settings, Icons.Default.Settings),
}

/**
 * Tela raiz: barra de abas inferior + NavHost. Cada aba é um destino de navegação;
 * a aba Settings é um grafo aninhado com suas sub-telas. O back stack de cada aba é
 * preservado (saveState/restoreState), espelhando o comportamento de NavigationStack por aba do iOS.
 */
@Composable
fun MainScreen(
    viewModel: MainViewModel = viewModel(),
    pendingRoute: String? = null,
    onRouteConsumed: () -> Unit = {},
) {
    val navController = rememberNavController()
    val accent = viewModel.accentColor.collectAsStateWithLifecycle().value.color

    // Deep-link de notificação: ao receber uma rota pendente, troca de aba e consome.
    LaunchedEffect(pendingRoute) {
        if (pendingRoute != null) {
            navigateForRoute(navController, pendingRoute)
            onRouteConsumed()
        }
    }

    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = backStackEntry?.destination
    // Troca de aba: preserva/restaura o back stack de cada uma (saveState/restoreState), sem duplicatas.
    val onSelectTab: (MainTab) -> Unit = { tab ->
        navController.navigate(tab.route) {
            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
            launchSingleTop = true
            restoreState = true
        }
    }
    // Tela larga (tablet/landscape): navegação lateral (NavigationRail). Estreita: barra inferior — convenção Android.
    val isWide = LocalConfiguration.current.screenWidthDp.dp >= Breakpoints.WideThreshold

    Scaffold(
        bottomBar = { if (!isWide) MainBottomBar(currentDestination, accent, onSelectTab) },
    ) { innerPadding ->
        Row(Modifier.fillMaxSize().padding(innerPadding)) {
            if (isWide) MainNavRail(currentDestination, accent, onSelectTab)
            NavHost(
                navController = navController,
                startDestination = MainTab.Today.route,
                modifier = Modifier.weight(1f).fillMaxHeight(),
            ) {
                composable(MainTab.Today.route) {
                    TodayScreen(
                        accent = accent,
                        onOpenHistory = { navController.navigate(HISTORY_ROUTE) },
                        onOpenTheme = { navController.navigate(SettingsRoutes.THEME) },
                    )
                }
                composable(MainTab.Achievements.route) {
                    AchievementsScreen(
                        onOpenHistory = { navController.navigate(HISTORY_ROUTE) },
                        onEditRecord = { epochDay -> navController.navigate("$EDIT_RECORD_ROUTE/$epochDay") },
                    )
                }
                composable(MainTab.Progress.route) {
                    ProgressScreen(
                        onOpenTheme = { navController.navigate(SettingsRoutes.THEME) },
                        onOpenGoals = { navController.navigate(SettingsRoutes.CUSTOM_GOALS) },
                        onOpenUserGoal = { navController.navigate(SettingsRoutes.GOAL) },
                        onOpenPhysical = { navController.navigate(SettingsRoutes.PHYSICAL) },
                        onOpenAchievements = { onSelectTab(MainTab.Achievements) },
                    )
                }
                settingsGraph(navController)
                composable(HISTORY_ROUTE) {
                    NotificationHistoryScreen(
                        onBack = { navController.popBackStack() },
                        onOpenRoute = { route, _ -> navigateForRoute(navController, route) },
                    )
                }
                composable(
                    route = "$EDIT_RECORD_ROUTE/{epochDay}",
                    arguments = listOf(navArgument("epochDay") { type = NavType.LongType }),
                ) { entry ->
                    EditRecordScreen(
                        epochDay = entry.arguments?.getLong("epochDay") ?: 0L,
                        onBack = { navController.popBackStack() },
                    )
                }
            }
        }
    }
}

/** Barra de navegação inferior (telas estreitas) — abas Today/Achievements/Progress/Settings. */
@Composable
private fun MainBottomBar(currentDestination: NavDestination?, accent: Color, onSelect: (MainTab) -> Unit) {
    NavigationBar {
        MainTab.entries.forEach { tab ->
            val selected = currentDestination?.hierarchy?.any { it.route == tab.route } == true
            NavigationBarItem(
                selected = selected,
                onClick = { onSelect(tab) },
                icon = { Icon(tab.icon, contentDescription = null) },
                label = { Text(stringResource(tab.labelRes)) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = accent,
                    selectedTextColor = accent,
                    indicatorColor = accent.copy(alpha = 0.15f),
                ),
            )
        }
    }
}

/** Trilho de navegação lateral (telas largas: tablet/landscape) — mesmas abas, no lado esquerdo. */
@Composable
private fun MainNavRail(currentDestination: NavDestination?, accent: Color, onSelect: (MainTab) -> Unit) {
    NavigationRail {
        MainTab.entries.forEach { tab ->
            val selected = currentDestination?.hierarchy?.any { it.route == tab.route } == true
            NavigationRailItem(
                selected = selected,
                onClick = { onSelect(tab) },
                icon = { Icon(tab.icon, contentDescription = null) },
                label = { Text(stringResource(tab.labelRes)) },
                colors = NavigationRailItemDefaults.colors(
                    selectedIconColor = accent,
                    selectedTextColor = accent,
                    indicatorColor = accent.copy(alpha = 0.15f),
                ),
            )
        }
    }
}

private const val HISTORY_ROUTE = "history"
private const val EDIT_RECORD_ROUTE = "edit_record"

/** Leva ao destino de uma rota de notificação (Today / Achievements / tela de Backup na Settings). */
private fun navigateForRoute(navController: NavController, routeRaw: String) {
    val target = when (NotificationRoute.fromRaw(routeRaw)) {
        NotificationRoute.Today -> MainTab.Today.route
        NotificationRoute.AchievementsToday -> MainTab.Achievements.route
        // Vai direto pra tela de Backup (dentro do grafo da Settings; a aba acende sozinha pela hierarquia).
        NotificationRoute.Backup -> SettingsRoutes.BACKUP
        null -> return
    }
    navController.navigate(target) {
        popUpTo(navController.graph.findStartDestination().id) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}
