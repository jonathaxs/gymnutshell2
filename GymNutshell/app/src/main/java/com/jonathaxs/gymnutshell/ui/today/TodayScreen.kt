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
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import kotlin.math.roundToInt

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
                            onSet = { viewModel.updateIntake(goal, it) },
                            onToggleRest = { viewModel.toggleRestDay(goal) },
                        )
                    }
                }
            }
            // Metas personalizadas sem categoria, no fim.
            items(state.uncategorizedGoals, key = { it.key }) { goal ->
                GoalRow(
                    goal = goal,
                    onSet = { viewModel.updateIntake(goal, it) },
                    onToggleRest = { viewModel.toggleRestDay(goal) },
                )
            }
        }
    }
}

/** Cabeçalho: data por extenso, anel de progresso (com % no centro) e emoji do tier. */
@Composable
private fun TodayHeader(state: TodayUiState) {
    val progressDesc = stringResource(R.string.cd_daily_progress, state.overallPercent)
    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(state.dateLabel, style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(16.dp))
        TodayProgressRing(
            progress = state.overallProgress,
            percent = state.overallPercent,
            modifier = Modifier.semantics { contentDescription = progressDesc },
        )
        Spacer(Modifier.height(8.dp))
        Text(state.tierEmoji, style = MaterialTheme.typography.headlineLarge)
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

/** Linha de uma meta: emoji, título, valor/alvo, slider e, quando aplicável, dia de descanso. */
@Composable
private fun GoalRow(goal: TodayGoalUi, onSet: (Int) -> Unit, onToggleRest: () -> Unit) {
    // Metas custom já trazem o título; built-in resolvem via string resource.
    val title = goal.title ?: stringResource(titleRes(goal.key))
    val restLabel = stringResource(R.string.rest_day)

    // Valor do slider em estado local (atualiza ao vivo no arraste) e sincronizado com o persistido.
    var sliderValue by remember(goal.key) { mutableFloatStateOf(goal.intake.toFloat()) }
    LaunchedEffect(goal.intake) { sliderValue = goal.intake.toFloat() }
    val shownValue = snapToIncrement(sliderValue, goal.increment, goal.target)

    Card {
        Column(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(goal.emoji, style = MaterialTheme.typography.titleLarge)
                Spacer(Modifier.width(12.dp))
                Text(title, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyLarge)
                Text(
                    text = if (goal.isRestDay) restLabel else "$shownValue/${goal.target} ${goal.unit}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            // Slider pra ajustar o valor (oculto no dia de descanso, que já vale 100%).
            if (!goal.isRestDay) {
                Slider(
                    value = sliderValue,
                    onValueChange = { sliderValue = it },
                    onValueChangeFinished = { onSet(snapToIncrement(sliderValue, goal.increment, goal.target)) },
                    valueRange = 0f..goal.target.toFloat().coerceAtLeast(1f),
                )
            }
            // Toggle de dia de descanso (só metas de Treino).
            if (goal.supportsRestDay) {
                FilterChip(
                    selected = goal.isRestDay,
                    onClick = onToggleRest,
                    label = { Text(restLabel) },
                )
            }
        }
    }
}

/** Arredonda o valor do slider pro múltiplo de increment mais próximo, dentro de [0, target]. */
private fun snapToIncrement(value: Float, increment: Int, target: Int): Int =
    ((value / increment).roundToInt() * increment).coerceIn(0, target)

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
