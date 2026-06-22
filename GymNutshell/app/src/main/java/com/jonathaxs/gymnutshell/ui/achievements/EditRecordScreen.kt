package com.jonathaxs.gymnutshell.ui.achievements

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.jonathaxs.gymnutshell.R
import com.jonathaxs.gymnutshell.ui.today.CategorySection
import com.jonathaxs.gymnutshell.ui.today.GoalRow

/**
 * Edição de uma conquista (DailyRecord) recente — porte da EditTodayView (iOS), aqui como página.
 * Cabeçalho com a data e o tier recalculado, metas agrupadas por categoria e botão Salvar na top bar.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditRecordScreen(
    epochDay: Long,
    onBack: () -> Unit,
    viewModel: EditRecordViewModel = viewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val accent = Color(state.accentArgb)
    LaunchedEffect(epochDay) { viewModel.load(epochDay) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.edit_record_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.cd_back))
                    }
                },
                actions = {
                    TextButton(onClick = { viewModel.save(onBack) }, enabled = state.loaded) {
                        Text(stringResource(R.string.edit_record_save))
                    }
                },
            )
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            item(key = "header") { EditHeaderCard(state, accent) }
            state.sections.forEach { section ->
                // Cabeçalho centralizado recolhível + metas animadas, idêntico à Today.
                item(key = "cat_${section.category.name}") {
                    CategorySection(
                        section = section,
                        accent = accent,
                        onToggle = { viewModel.toggleCategory(section.category) },
                        onSetIntake = { goal, value -> viewModel.setIntake(goal, value) },
                        onToggleRest = { goal -> viewModel.toggleRestDay(goal) },
                    )
                }
            }
            items(state.uncategorizedGoals, key = { it.key }) { goal ->
                GoalRow(
                    goal = goal,
                    accent = accent,
                    onSet = { viewModel.setIntake(goal, it) },
                    onToggleRest = { viewModel.toggleRestDay(goal) },
                )
            }
        }
    }
}

/** Cabeçalho: emoji do tier + data + "Level N · X%" — espelha o card de resumo da EditTodayView. */
@Composable
private fun EditHeaderCard(state: EditRecordUiState, accent: Color) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = accent.copy(alpha = 0.15f)),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(state.tierEmoji, fontSize = 40.sp, modifier = Modifier.clearAndSetSemantics {})
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    state.dateLabel,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    "${stringResource(R.string.progress_tier_level, state.tierLevel)} · ${state.percent}%",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}
