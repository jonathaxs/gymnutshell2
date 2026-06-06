package com.jonathaxs.gymnutshell.ui.settings

import androidx.annotation.StringRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.jonathaxs.gymnutshell.R
import com.jonathaxs.gymnutshell.core.domain.UserGoal
import com.jonathaxs.gymnutshell.ui.theme.color

/** Sub-tela do objetivo fitness — porte de UserGoalChangeView (iOS): escolhe Bulking/Maintenance/Cutting. */
@Composable
fun UserGoalScreen(onBack: () -> Unit, viewModel: SettingsViewModel = viewModel()) {
    val profile by viewModel.profile.collectAsStateWithLifecycle()
    val accent = viewModel.accentColor.collectAsStateWithLifecycle().value.color

    Scaffold(topBar = { SettingsTopBar(stringResource(R.string.settings_fitness_goal), onBack) }) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            UserGoal.entries.forEachIndexed { index, goal ->
                if (index > 0) HorizontalDivider()
                GoalOption(
                    label = stringResource(goalRes(goal)),
                    selected = profile.goal == goal,
                    checkColor = accent,
                    onClick = { viewModel.saveProfile(profile.copy(goal = goal)) },
                )
            }
        }
    }
}

/** Linha de opção: rótulo + checkmark (na cor de destaque) quando selecionada. */
@Composable
private fun GoalOption(
    label: String,
    selected: Boolean,
    checkColor: androidx.compose.ui.graphics.Color,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyLarge)
        if (selected) {
            Text("✓", color = checkColor, style = MaterialTheme.typography.titleMedium)
        }
    }
}

@StringRes
private fun goalRes(goal: UserGoal): Int = when (goal) {
    UserGoal.Bulking -> R.string.goal_bulking
    UserGoal.Maintenance -> R.string.goal_maintenance
    UserGoal.Cutting -> R.string.goal_cutting
}
