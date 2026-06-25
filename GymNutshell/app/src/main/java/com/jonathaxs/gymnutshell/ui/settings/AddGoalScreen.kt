package com.jonathaxs.gymnutshell.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.jonathaxs.gymnutshell.R
import com.jonathaxs.gymnutshell.core.domain.GoalCategory
import java.text.BreakIterator

/** Formulário de criar/editar meta personalizada — porte de AddTrackingGoalView (iOS). */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddGoalScreen(goalId: Long?, onBack: () -> Unit, viewModel: AddGoalViewModel = viewModel()) {
    val data by viewModel.data.collectAsStateWithLifecycle()
    LaunchedEffect(Unit) { viewModel.start(goalId) }

    // Campos de rascunho.
    var selectedCategory by remember { mutableStateOf<CategorySelection>(CategorySelection.Builtin(GoalCategory.Essencial)) }
    var emoji by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var unit by remember { mutableStateOf("") }
    var goalText by remember { mutableStateOf("") }
    var incrementText by remember { mutableStateOf("") }
    var creatingNewCategory by remember { mutableStateOf(false) }
    var newCategoryName by remember { mutableStateOf("") }
    var newCategoryRestDay by remember { mutableStateOf(false) }

    // Pré-preenche uma vez quando a meta em edição termina de carregar.
    LaunchedEffect(data.editing?.id) {
        val editing = data.editing ?: return@LaunchedEffect
        selectedCategory = when {
            editing.customCategoryId != null -> CategorySelection.Custom(editing.customCategoryId!!)
            editing.categoryRaw != null ->
                CategorySelection.Builtin(GoalCategory.fromRaw(editing.categoryRaw) ?: GoalCategory.Essencial)
            else -> CategorySelection.Builtin(GoalCategory.Essencial)
        }
        emoji = editing.emoji
        name = editing.name
        unit = editing.unit
        goalText = editing.target.toString()
        incrementText = editing.increment.toString()
    }

    val accent = Color(data.accentArgb)
    val builtinNames = GoalCategory.entries.associateWith { stringResource(GoalsViewModel.categoryTitleRes(it)) }

    // Validações (espelham o iOS).
    val trimmedEmoji = firstGrapheme(emoji)
    val emojiConflict = trimmedEmoji.isNotEmpty() &&
        (trimmedEmoji in AddGoalViewModel.RESERVED_EMOJIS ||
            data.existingGoals.any { it.id != data.editing?.id && it.emoji == trimmedEmoji })
    val nameLower = name.trim().lowercase()
    val nameConflict = nameLower.isNotEmpty() &&
        (nameLower in AddGoalViewModel.RESERVED_NAMES ||
            data.existingGoals.any { it.id != data.editing?.id && it.name.lowercase() == nameLower })
    val newCatLower = newCategoryName.trim().lowercase()
    val categoryNameConflict = creatingNewCategory && newCatLower.isNotEmpty() &&
        (builtinNames.values.any { it.lowercase() == newCatLower } ||
            data.customCategories.any { it.name.lowercase() == newCatLower })
    val goalValue = goalText.toIntOrNull()
    val stepValue = incrementText.toIntOrNull()
    val stepTooLarge = goalValue != null && goalValue > 0 && stepValue != null && stepValue > 0 && stepValue > goalValue

    val isValid = trimmedEmoji.isNotEmpty() && name.isNotBlank() && !emojiConflict && !nameConflict &&
        unit.isNotBlank() && goalValue != null && goalValue > 0 &&
        stepValue != null && stepValue > 0 && stepValue <= goalValue &&
        (!creatingNewCategory || (newCategoryName.isNotBlank() && !categoryNameConflict))

    val title = data.editing?.let { "${it.emoji} ${it.name}" } ?: stringResource(R.string.addgoal_title_new)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.cd_back))
                    }
                },
                actions = {
                    TextButton(
                        enabled = isValid,
                        onClick = {
                            viewModel.save(
                                emoji = trimmedEmoji,
                                name = name,
                                unit = unit,
                                target = goalValue ?: 1,
                                increment = stepValue ?: 1,
                                selection = selectedCategory,
                                newCategory = if (creatingNewCategory && newCategoryName.isNotBlank()) {
                                    newCategoryName to newCategoryRestDay
                                } else {
                                    null
                                },
                                editingId = data.editing?.id,
                            )
                            onBack()
                        },
                    ) { Text(stringResource(R.string.action_save)) }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // Categoria (dropdown fixas + personalizadas).
            FieldLabel(stringResource(R.string.addgoal_section_category))
            CategoryDropdown(
                selected = selectedCategory,
                builtinNames = builtinNames,
                customCategories = data.customCategories,
                onSelect = { selectedCategory = it },
            )
            TextButton(onClick = { creatingNewCategory = !creatingNewCategory }) {
                Text(
                    (if (creatingNewCategory) "− " else "+ ") + stringResource(R.string.addgoal_category_new_button),
                    color = accent,
                )
            }

            // Nova categoria (inline).
            if (creatingNewCategory) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(stringResource(R.string.addgoal_category_new_rest_day), modifier = Modifier.weight(1f))
                    Switch(checked = newCategoryRestDay, onCheckedChange = { newCategoryRestDay = it })
                }
                OutlinedTextField(
                    value = newCategoryName,
                    onValueChange = { newCategoryName = it },
                    label = { Text(stringResource(R.string.addgoal_category_new_section)) },
                    placeholder = { Text(stringResource(R.string.addgoal_category_new_name_placeholder)) },
                    singleLine = true,
                    isError = categoryNameConflict,
                    supportingText = errorText(categoryNameConflict, R.string.addgoal_warning_category_duplicate),
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            // Emoji.
            OutlinedTextField(
                value = emoji,
                onValueChange = { emoji = it },
                label = { Text(stringResource(R.string.field_emoji)) },
                placeholder = { Text(stringResource(R.string.addgoal_emoji_placeholder)) },
                singleLine = true,
                isError = emojiConflict,
                supportingText = errorText(emojiConflict, R.string.addgoal_warning_emoji),
                modifier = Modifier.fillMaxWidth(),
            )

            // Nome.
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text(stringResource(R.string.field_name)) },
                placeholder = { Text(stringResource(R.string.addgoal_name_placeholder)) },
                singleLine = true,
                isError = nameConflict,
                supportingText = errorText(nameConflict, R.string.addgoal_warning_catalog),
                modifier = Modifier.fillMaxWidth(),
            )

            // Unidade (primeira letra minúscula, igual ao iOS).
            OutlinedTextField(
                value = unit,
                onValueChange = { newValue ->
                    unit = newValue.replaceFirstChar { if (it.isUpperCase()) it.lowercaseChar() else it }
                },
                label = { Text(stringResource(R.string.field_unit)) },
                placeholder = { Text(stringResource(R.string.addgoal_unit_placeholder)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )

            // Alvo.
            OutlinedTextField(
                value = goalText,
                onValueChange = { goalText = it.filter(Char::isDigit) },
                label = { Text(stringResource(R.string.field_target)) },
                placeholder = { Text(stringResource(R.string.addgoal_goal_placeholder)) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
            )

            // Passo.
            OutlinedTextField(
                value = incrementText,
                onValueChange = { incrementText = it.filter(Char::isDigit) },
                label = { Text(stringResource(R.string.addgoal_section_step)) },
                placeholder = { Text(stringResource(R.string.addgoal_increment_placeholder)) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                isError = stepTooLarge,
                supportingText = errorText(stepTooLarge, R.string.addgoal_warning_step),
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

/** Dropdown de categoria com as fixas + personalizadas. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CategoryDropdown(
    selected: CategorySelection,
    builtinNames: Map<GoalCategory, String>,
    customCategories: List<com.jonathaxs.gymnutshell.core.data.CustomGoalCategory>,
    onSelect: (CategorySelection) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedLabel = when (selected) {
        is CategorySelection.Builtin -> builtinNames[selected.category].orEmpty()
        is CategorySelection.Custom -> customCategories.firstOrNull { it.id == selected.id }?.name.orEmpty()
    }
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
        OutlinedTextField(
            value = selectedLabel,
            onValueChange = {},
            readOnly = true,
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.fillMaxWidth().menuAnchor(),
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            GoalCategory.entries.forEach { category ->
                DropdownMenuItem(
                    text = { Text(builtinNames[category].orEmpty()) },
                    onClick = { onSelect(CategorySelection.Builtin(category)); expanded = false },
                )
            }
            customCategories.forEach { category ->
                DropdownMenuItem(
                    text = { Text(category.name) },
                    onClick = { onSelect(CategorySelection.Custom(category.id)); expanded = false },
                )
            }
        }
    }
}

/** Rótulo de seção do formulário. */
@Composable
private fun FieldLabel(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

/** Texto de aviso em vermelho pro supportingText do campo, ou null quando não há erro. */
private fun errorText(show: Boolean, resId: Int): (@Composable () -> Unit)? =
    if (show) {
        { Text(stringResource(resId), color = Color(0xFFFF3B30)) }
    } else {
        null
    }

/** Primeiro grafema da string (1 emoji), tolerando emojis multi-codepoint — aproxima o .prefix(1) do iOS. */
private fun firstGrapheme(s: String): String {
    val t = s.trim()
    if (t.isEmpty()) return ""
    val iterator = BreakIterator.getCharacterInstance()
    iterator.setText(t)
    val end = iterator.next()
    return if (end == BreakIterator.DONE) t else t.substring(0, end)
}
