package com.jonathaxs.gymnutshell.wear

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.wear.compose.foundation.pager.HorizontalPager
import androidx.wear.compose.foundation.pager.rememberPagerState
import androidx.wear.compose.material3.AppScaffold
import androidx.wear.compose.material3.HorizontalPagerScaffold
import androidx.wear.compose.material3.ScreenScaffold
import androidx.wear.compose.material3.Text

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
                    0 -> PlaceholderScreen(stringResource(R.string.wear_page_stats))
                    1 -> PlaceholderScreen(stringResource(R.string.wear_page_today))
                    else -> PlaceholderScreen(stringResource(R.string.wear_page_notifications))
                }
            }
        }
    }
}

/** Página provisória; cada uma vira a tela real nas fatias 7B e 7E. */
@Composable
private fun PlaceholderScreen(title: String) {
    ScreenScaffold {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(title, textAlign = TextAlign.Center)
        }
    }
}
