package com.jonathaxs.gymnutshell.wear

import androidx.compose.runtime.Composable
import androidx.wear.compose.foundation.pager.HorizontalPager
import androidx.wear.compose.foundation.pager.rememberPagerState
import androidx.wear.compose.material3.AppScaffold
import androidx.wear.compose.material3.HorizontalPagerScaffold
import com.jonathaxs.gymnutshell.wear.notifications.WearNotificationsScreen
import com.jonathaxs.gymnutshell.wear.stats.WearStatsScreen
import com.jonathaxs.gymnutshell.wear.today.WearTodayScreen

/**
 * Container de navegação por página — porte da WatchRootView (iOS).
 * Ordem: Stats ← Today → Notifications; Today (índice 1) é a página padrão.
 */
@Composable
fun WearRootScreen() {
    val pagerState = rememberPagerState(initialPage = 1, pageCount = { 3 })

    AppScaffold {
        HorizontalPagerScaffold(pagerState = pagerState) {
            HorizontalPager(state = pagerState) { page ->
                when (page) {
                    0 -> WearStatsScreen()
                    1 -> WearTodayScreen()
                    else -> WearNotificationsScreen()
                }
            }
        }
    }
}
