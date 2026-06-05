package com.jonathaxs.gymnutshell.ui.achievements

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

/** Placeholder da aba Achievements — porte de AchievementsView (Fase 4). */
@Composable
fun AchievementsScreen(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("Achievements", style = MaterialTheme.typography.headlineMedium)
    }
}
