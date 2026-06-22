package com.jonathaxs.gymnutshell.ui.settings

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.jonathaxs.gymnutshell.R
import com.jonathaxs.gymnutshell.core.domain.UserGoal
import com.jonathaxs.gymnutshell.ui.components.GroupCheck
import com.jonathaxs.gymnutshell.ui.components.GroupRow
import com.jonathaxs.gymnutshell.ui.components.GroupRowDivider
import com.jonathaxs.gymnutshell.ui.components.GroupSection
import com.jonathaxs.gymnutshell.ui.theme.color

/** Sub-tela do objetivo fitness — porte de UserGoalChangeView (iOS): escolhe Bulking/Maintenance/Cutting. */
@Composable
fun UserGoalScreen(onBack: () -> Unit, viewModel: SettingsViewModel = viewModel()) {
    val profile by viewModel.profile.collectAsStateWithLifecycle()
    val accent = viewModel.accentColor.collectAsStateWithLifecycle().value.color

    Scaffold(topBar = { SettingsTopBar(stringResource(R.string.settings_fitness_goal), onBack) }) { padding ->
        Column(
            Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()),
        ) {
            GroupSection {
                UserGoal.entries.forEachIndexed { index, goal ->
                    if (index > 0) GroupRowDivider()
                    GroupRow(
                        title = stringResource(goalRes(goal)),
                        trailing = if (profile.goal == goal) ({ GroupCheck(accent) }) else null,
                        onClick = { viewModel.saveProfile(profile.copy(goal = goal)) },
                    )
                }
            }
        }
    }
}

@StringRes
private fun goalRes(goal: UserGoal): Int = when (goal) {
    UserGoal.Bulking -> R.string.goal_bulking
    UserGoal.Maintenance -> R.string.goal_maintenance
    UserGoal.Cutting -> R.string.goal_cutting
}
