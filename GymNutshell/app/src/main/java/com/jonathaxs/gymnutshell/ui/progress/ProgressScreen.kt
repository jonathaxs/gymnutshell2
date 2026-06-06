package com.jonathaxs.gymnutshell.ui.progress

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

/** Placeholder da aba Progress — porte de ProgressOverView (Fase 4). */
@Composable
fun ProgressScreen(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("Progress", style = MaterialTheme.typography.headlineMedium)
    }
}
