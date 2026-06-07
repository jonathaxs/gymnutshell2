package com.jonathaxs.gymnutshell.ui.welcome

import androidx.annotation.StringRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.jonathaxs.gymnutshell.R
import com.jonathaxs.gymnutshell.core.domain.AppTheme
import com.jonathaxs.gymnutshell.core.domain.GoalsCalculator
import com.jonathaxs.gymnutshell.core.domain.GoalsProvider
import com.jonathaxs.gymnutshell.core.domain.Profile
import com.jonathaxs.gymnutshell.core.domain.ThemeCategory
import com.jonathaxs.gymnutshell.core.domain.UserGoal

/** Etapas do onboarding — porte de WelcomeStep (iOS). */
private enum class WelcomeStep(val emoji: String, @param:StringRes val titleRes: Int) {
    Start("👋", R.string.welcome_start_title),
    Goal("🎯", R.string.welcome_goal_title),
    Physical("👤", R.string.welcome_physical_title),
    Theme("🎭", R.string.welcome_theme_title),
    Summary("✅", R.string.welcome_summary_title),
}

/** Onboarding em 5 passos — porte (MVP, métrico) de WelcomeView (iOS). */
@Composable
fun WelcomeScreen(viewModel: WelcomeViewModel = viewModel()) {
    val steps = WelcomeStep.entries
    var stepIndex by rememberSaveable { mutableIntStateOf(0) }
    val step = steps[stepIndex]

    var name by rememberSaveable { mutableStateOf("") }
    var weight by rememberSaveable { mutableStateOf("") }
    var height by rememberSaveable { mutableStateOf("") }
    var age by rememberSaveable { mutableStateOf("") }
    var sex by rememberSaveable { mutableStateOf("male") }
    var goalOrdinal by rememberSaveable { mutableIntStateOf(UserGoal.Maintenance.ordinal) }
    var themeOrdinal by rememberSaveable { mutableIntStateOf(AppTheme.Gym.ordinal) }

    val goal = UserGoal.entries[goalOrdinal]
    val theme = AppTheme.entries[themeOrdinal]
    val profile = Profile(
        name = name.trim(),
        weightKg = weight.toDoubleOrNull() ?: 0.0,
        heightCm = height.toIntOrNull() ?: 0,
        age = age.toIntOrNull() ?: 0,
        sex = sex,
        goal = goal,
    )
    val physicalValid = profile.weightKg > 0 && profile.heightCm > 0 && profile.age > 0

    Scaffold { padding ->
        Column(Modifier.fillMaxSize().padding(padding).padding(24.dp)) {
            LinearProgressIndicator(
                progress = { (stepIndex + 1f) / steps.size },
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(16.dp))
            // Cabeçalho da etapa
            Text(step.emoji, style = MaterialTheme.typography.displaySmall)
            Text(
                stringResource(step.titleRes),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
            )
            Spacer(Modifier.height(16.dp))

            Box(Modifier.weight(1f).fillMaxWidth()) {
                when (step) {
                    WelcomeStep.Start -> StartStep()
                    WelcomeStep.Goal -> GoalStep(goal) { goalOrdinal = it.ordinal }
                    WelcomeStep.Physical -> PhysicalStep(
                        name, weight, height, age, sex,
                        onName = { name = it }, onWeight = { weight = it },
                        onHeight = { height = it }, onAge = { age = it }, onSex = { sex = it },
                    )
                    WelcomeStep.Theme -> ThemeStep(theme) { themeOrdinal = it.ordinal }
                    WelcomeStep.Summary -> SummaryStep(GoalsProvider.goals(profile), theme)
                }
            }

            Spacer(Modifier.height(16.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (stepIndex > 0) {
                    OutlinedButton(onClick = { stepIndex-- }) { Text(stringResource(R.string.action_back)) }
                }
                Spacer(Modifier.weight(1f))
                val isLast = step == WelcomeStep.Summary
                val buttonText = when {
                    isLast -> R.string.action_finish
                    step == WelcomeStep.Start -> R.string.action_get_started
                    else -> R.string.action_next
                }
                Button(
                    onClick = { if (isLast) viewModel.complete(profile, theme) else stepIndex++ },
                    enabled = step != WelcomeStep.Physical || physicalValid,
                ) { Text(stringResource(buttonText)) }
            }
        }
    }
}

@Composable
private fun StartStep() {
    Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center) {
        Text(
            stringResource(R.string.welcome_start_subtitle),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun GoalStep(selected: UserGoal, onSelect: (UserGoal) -> Unit) {
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        UserGoal.entries.forEachIndexed { index, goal ->
            if (index > 0) HorizontalDivider()
            SelectableRow(stringResource(goalLabel(goal)), goal == selected) { onSelect(goal) }
        }
    }
}

@Composable
private fun PhysicalStep(
    name: String, weight: String, height: String, age: String, sex: String,
    onName: (String) -> Unit, onWeight: (String) -> Unit, onHeight: (String) -> Unit,
    onAge: (String) -> Unit, onSex: (String) -> Unit,
) {
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        OutlinedTextField(name, onName, label = { Text(stringResource(R.string.field_name)) }, singleLine = true, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(
            weight, onWeight, label = { Text(stringResource(R.string.field_weight_kg)) }, singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), modifier = Modifier.fillMaxWidth(),
        )
        OutlinedTextField(
            height, onHeight, label = { Text(stringResource(R.string.field_height_cm)) }, singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth(),
        )
        OutlinedTextField(
            age, onAge, label = { Text(stringResource(R.string.field_age)) }, singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth(),
        )
        Text(stringResource(R.string.field_sex), style = MaterialTheme.typography.labelLarge)
        val sexes = listOf("male" to R.string.sex_male, "female" to R.string.sex_female, "other" to R.string.sex_other)
        SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
            sexes.forEachIndexed { index, (value, labelRes) ->
                SegmentedButton(
                    selected = sex == value,
                    onClick = { onSex(value) },
                    shape = SegmentedButtonDefaults.itemShape(index, sexes.size),
                ) { Text(stringResource(labelRes)) }
            }
        }
    }
}

@Composable
private fun ThemeStep(selected: AppTheme, onSelect: (AppTheme) -> Unit) {
    LazyColumn(Modifier.fillMaxSize()) {
        ThemeCategory.entries.forEach { category ->
            item(key = "cat_${category.name}") {
                Text(
                    stringResource(themeCategoryLabel(category)),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 8.dp),
                )
            }
            items(AppTheme.inCategory(category), key = { it.rawValue }) { theme ->
                SelectableRow(theme.previewEmojis.joinToString("  "), theme == selected) { onSelect(theme) }
            }
        }
    }
}

@Composable
private fun SummaryStep(goals: GoalsCalculator.Result, theme: AppTheme) {
    val items = listOf(
        Triple(R.string.today_calories, goals.calories, "kcal"),
        Triple(R.string.today_water, goals.water, "ml"),
        Triple(R.string.today_protein, goals.protein, "g"),
        Triple(R.string.today_carbs, goals.carbs, "g"),
        Triple(R.string.today_good_fat, goals.goodFat, "g"),
        Triple(R.string.today_fiber, goals.fiber, "g"),
        Triple(R.string.today_sleep, goals.sleep, "h"),
        Triple(R.string.today_creatine, goals.creatine, "g"),
        Triple(R.string.today_workout, goals.workout, "min"),
        Triple(R.string.today_cardio, goals.cardio, "min"),
    )
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        Text(theme.previewEmojis.joinToString("  "), style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(12.dp))
        items.forEach { (labelRes, value, unit) ->
            Row(Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
                Text(stringResource(labelRes), modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyLarge)
                Text("$value $unit", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
            }
            HorizontalDivider()
        }
    }
}

/** Linha selecionável genérica com checkmark. */
@Composable
private fun SelectableRow(label: String, selected: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyLarge)
        if (selected) {
            Spacer(Modifier.width(8.dp))
            Text("✓", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.titleMedium)
        }
    }
}

@StringRes
private fun goalLabel(goal: UserGoal): Int = when (goal) {
    UserGoal.Bulking -> R.string.goal_bulking
    UserGoal.Maintenance -> R.string.goal_maintenance
    UserGoal.Cutting -> R.string.goal_cutting
}

@StringRes
private fun themeCategoryLabel(category: ThemeCategory): Int = when (category) {
    ThemeCategory.Sport -> R.string.theme_cat_sport
    ThemeCategory.Animals -> R.string.theme_cat_animals
    ThemeCategory.Warrior -> R.string.theme_cat_warrior
    ThemeCategory.Space -> R.string.theme_cat_space
    ThemeCategory.Elements -> R.string.theme_cat_elements
    ThemeCategory.Competition -> R.string.theme_cat_competition
}
