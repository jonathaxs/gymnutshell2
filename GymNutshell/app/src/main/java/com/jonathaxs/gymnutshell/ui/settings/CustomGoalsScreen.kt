package com.jonathaxs.gymnutshell.ui.settings

import androidx.annotation.StringRes
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.jonathaxs.gymnutshell.R
import com.jonathaxs.gymnutshell.core.data.CustomGoal
import com.jonathaxs.gymnutshell.core.domain.GoalCategory

/** Sub-tela de metas personalizadas — porte (MVP) de TrackingGoalsSettingsView/AddTrackingGoalView (iOS). */
@Composable
fun CustomGoalsScreen(onBack: () -> Unit, viewModel: CustomGoalsViewModel = viewModel()) {
    val goals by viewModel.goals.collectAsStateWithLifecycle()

    Scaffold(topBar = { SettingsTopBar(stringResource(R.string.settings_custom_goals), onBack) }) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                AddGoalForm(onAdd = viewModel::add)
            }
            items(goals, key = { it.id }) { goal ->
                GoalItem(goal, onDelete = { viewModel.delete(goal) })
            }
        }
    }
}

/** Formulário de adicionar meta. */
@Composable
private fun AddGoalForm(onAdd: (String, String, String, Int, Int, GoalCategory?) -> Unit) {
    var name by remember { mutableStateOf("") }
    var emoji by remember { mutableStateOf("") }
    var unit by remember { mutableStateOf("") }
    var target by remember { mutableStateOf("") }
    var increment by remember { mutableStateOf("") }
    var category by remember { mutableStateOf<GoalCategory?>(null) }

    val targetValue = target.toIntOrNull() ?: 0
    val canAdd = name.isNotBlank() && targetValue > 0

    Card {
        Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = emoji, onValueChange = { emoji = it.take(2) },
                    label = { Text(stringResource(R.string.field_emoji)) },
                    singleLine = true, modifier = Modifier.width(96.dp),
                )
                OutlinedTextField(
                    value = name, onValueChange = { name = it },
                    label = { Text(stringResource(R.string.field_name)) },
                    singleLine = true, modifier = Modifier.weight(1f),
                )
            }
            OutlinedTextField(
                value = unit, onValueChange = { unit = it },
                label = { Text(stringResource(R.string.field_unit)) },
                singleLine = true, modifier = Modifier.fillMaxWidth(),
            )
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = target, onValueChange = { target = it },
                    label = { Text(stringResource(R.string.field_target)) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f),
                )
                OutlinedTextField(
                    value = increment, onValueChange = { increment = it },
                    label = { Text(stringResource(R.string.field_increment)) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f),
                )
            }
            // Categoria: "None" + as 4 fixas (scroll horizontal).
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                CategoryChip(null, category) { category = null }
                GoalCategory.entries.forEach { option ->
                    CategoryChip(option, category) { category = option }
                }
            }
            Button(
                onClick = {
                    onAdd(name, emoji, unit, targetValue, increment.toIntOrNull() ?: 1, category)
                    name = ""; emoji = ""; unit = ""; target = ""; increment = ""; category = null
                },
                enabled = canAdd,
                modifier = Modifier.fillMaxWidth(),
            ) { Text(stringResource(R.string.custom_goal_add)) }
        }
    }
}

@Composable
private fun CategoryChip(option: GoalCategory?, selected: GoalCategory?, onClick: () -> Unit) {
    FilterChip(
        selected = option == selected,
        onClick = onClick,
        label = { Text(stringResource(categoryLabelRes(option))) },
    )
}

/** Linha de uma meta personalizada existente. */
@Composable
private fun GoalItem(goal: CustomGoal, onDelete: () -> Unit) {
    Card(Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(start = 16.dp, top = 8.dp, bottom = 8.dp, end = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(goal.emoji, style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(goal.name, style = MaterialTheme.typography.bodyLarge)
                Text(
                    "${goal.target} ${goal.unit} · ${stringResource(categoryLabelRes(GoalCategory.fromRaw(goal.categoryRaw)))}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = stringResource(R.string.cd_delete))
            }
        }
    }
}

@StringRes
private fun categoryLabelRes(category: GoalCategory?): Int = when (category) {
    null -> R.string.category_none
    GoalCategory.Essencial -> R.string.category_essencial
    GoalCategory.Nutricao -> R.string.category_nutricao
    GoalCategory.Treino -> R.string.category_treino
    GoalCategory.Suplemento -> R.string.category_suplemento
}
