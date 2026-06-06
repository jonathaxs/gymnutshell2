package com.jonathaxs.gymnutshell.ui.today

import androidx.annotation.StringRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.jonathaxs.gymnutshell.R
import com.jonathaxs.gymnutshell.core.domain.GoalCategory

/** Tela "Hoje" — porte (MVP) da TodayView (iOS): header com % do dia + metas agrupadas por categoria. */
@Composable
fun TodayScreen(modifier: Modifier = Modifier, viewModel: TodayViewModel = viewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Column(modifier.fillMaxSize()) {
        TodayHeader(state)
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            state.sections.forEach { section ->
                item(key = "cat_${section.category.name}") {
                    CategoryHeader(section) { viewModel.toggleCategory(section.category) }
                }
                if (!section.collapsed) {
                    items(section.goals, key = { it.key }) { goal ->
                        GoalRow(
                            goal = goal,
                            onMinus = { viewModel.decrement(goal) },
                            onPlus = { viewModel.increment(goal) },
                        )
                    }
                }
            }
        }
    }
}

/** Cabeçalho: data por extenso, emoji do tier, % do dia e barra de progresso geral. */
@Composable
private fun TodayHeader(state: TodayUiState) {
    Column(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = state.dateLabel,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.weight(1f),
            )
            Text(state.tierEmoji, style = MaterialTheme.typography.headlineSmall)
            Spacer(Modifier.width(8.dp))
            Text("${state.overallPercent}%", style = MaterialTheme.typography.titleLarge)
        }
        Spacer(Modifier.height(8.dp))
        LinearProgressIndicator(
            progress = { state.overallProgress },
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

/** Cabeçalho de categoria, clicável pra abrir/fechar. O chevron indica o estado. */
@Composable
private fun CategoryHeader(section: TodayCategoryUi, onToggle: () -> Unit) {
    val title = stringResource(categoryTitleRes(section.category))
    val stateDesc = stringResource(
        if (section.collapsed) R.string.state_collapsed else R.string.state_expanded,
    )
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onToggle)
            .semantics { stateDescription = stateDesc }
            .padding(vertical = 10.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // chevron visual escondido do TalkBack (o estado já vem pelo stateDescription)
        Text(if (section.collapsed) "▸" else "▾", modifier = Modifier.clearAndSetSemantics {})
        Spacer(Modifier.width(8.dp))
        Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
    }
}

/** Linha de uma meta: emoji, título, valor/alvo e os botões –/+. */
@Composable
private fun GoalRow(goal: TodayGoalUi, onMinus: () -> Unit, onPlus: () -> Unit) {
    val title = stringResource(titleRes(goal.key))
    Card {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(goal.emoji, style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.bodyLarge)
                Text(
                    text = "${goal.intake}/${goal.target} ${goal.unit}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            // Rótulos de acessibilidade nos botões: o TalkBack lê "Decrease Água"/"Increase Água"
            // (contentDescription no botão), e o texto visual "−"/"+" é escondido da árvore a11y.
            val decreaseLabel = stringResource(R.string.cd_decrease, title)
            val increaseLabel = stringResource(R.string.cd_increase, title)
            FilledTonalIconButton(
                onClick = onMinus,
                modifier = Modifier.semantics { contentDescription = decreaseLabel },
            ) { Text("−", modifier = Modifier.clearAndSetSemantics {}) }
            Spacer(Modifier.width(4.dp))
            FilledTonalIconButton(
                onClick = onPlus,
                modifier = Modifier.semantics { contentDescription = increaseLabel },
            ) { Text("+", modifier = Modifier.clearAndSetSemantics {}) }
        }
    }
}

/** Mapeia a categoria pro título localizado. */
@StringRes
private fun categoryTitleRes(category: GoalCategory): Int = when (category) {
    GoalCategory.Essencial -> R.string.category_essencial
    GoalCategory.Nutricao -> R.string.category_nutricao
    GoalCategory.Treino -> R.string.category_treino
    GoalCategory.Suplemento -> R.string.category_suplemento
}

/** Mapeia a chave da meta pro título localizado (strings.xml). */
@StringRes
private fun titleRes(key: String): Int = when (key) {
    "tracking.workout" -> R.string.today_workout
    "tracking.cardio" -> R.string.today_cardio
    "tracking.sleep" -> R.string.today_sleep
    "tracking.water" -> R.string.today_water
    "tracking.calories" -> R.string.today_calories
    "tracking.protein" -> R.string.today_protein
    "tracking.carbs" -> R.string.today_carbs
    "tracking.goodFat" -> R.string.today_good_fat
    "tracking.fiber" -> R.string.today_fiber
    "tracking.creatine" -> R.string.today_creatine
    else -> R.string.app_name
}
