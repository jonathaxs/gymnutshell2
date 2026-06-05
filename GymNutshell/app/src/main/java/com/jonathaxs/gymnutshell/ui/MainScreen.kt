package com.jonathaxs.gymnutshell.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.annotation.StringRes
import com.jonathaxs.gymnutshell.R
import com.jonathaxs.gymnutshell.core.theme.AccentColor
import com.jonathaxs.gymnutshell.ui.achievements.AchievementsScreen
import com.jonathaxs.gymnutshell.ui.profile.ProfileScreen
import com.jonathaxs.gymnutshell.ui.settings.SettingsScreen
import com.jonathaxs.gymnutshell.ui.theme.color
import com.jonathaxs.gymnutshell.ui.today.TodayScreen

/**
 * Abas principais — espelha o enum MainView.Tab do iOS (índices 0..3).
 * Cada aba carrega o texto (string localizada) e o ícone do NavigationBar.
 */
private enum class MainTab(@param:StringRes val labelRes: Int, val icon: ImageVector) {
    Today(R.string.tab_today, Icons.Default.Check),
    Achievements(R.string.tab_achievements, Icons.Default.Star),
    Profile(R.string.tab_profile, Icons.Default.Person),
    Settings(R.string.tab_settings, Icons.Default.Settings)
}

/**
 * Tela raiz do app: hospeda a barra de abas inferior e roteia pra tela selecionada.
 * Equivale ao TabView(selection:) do MainView.swift; o selectedTab persiste rotações
 * via rememberSaveable (≈ @AppStorage selectedTab no iOS, que persistiremos na Fase 2).
 */
@Composable
fun MainScreen() {
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }
    val tabs = MainTab.entries
    val accent = AccentColor.Default.color

    Scaffold(
        bottomBar = {
            NavigationBar {
                tabs.forEachIndexed { index, tab ->
                    NavigationBarItem(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        icon = { Icon(tab.icon, contentDescription = null) },
                        label = { Text(stringResource(tab.labelRes)) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = accent,
                            selectedTextColor = accent,
                            indicatorColor = accent.copy(alpha = 0.15f)
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        val contentModifier = Modifier.padding(innerPadding)
        when (tabs[selectedTab]) {
            MainTab.Today -> TodayScreen(contentModifier)
            MainTab.Achievements -> AchievementsScreen(contentModifier)
            MainTab.Profile -> ProfileScreen(contentModifier)
            MainTab.Settings -> SettingsScreen(contentModifier)
        }
    }
}
