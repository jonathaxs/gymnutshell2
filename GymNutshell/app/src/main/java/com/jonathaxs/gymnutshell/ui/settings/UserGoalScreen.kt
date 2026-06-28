package com.jonathaxs.gymnutshell.ui.settings

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.jonathaxs.gymnutshell.R
import com.jonathaxs.gymnutshell.core.domain.GoalsProvider
import com.jonathaxs.gymnutshell.core.domain.MeasurementSystem
import com.jonathaxs.gymnutshell.core.domain.UnitConverter
import com.jonathaxs.gymnutshell.core.domain.UserGoal
import com.jonathaxs.gymnutshell.ui.components.GroupRow
import com.jonathaxs.gymnutshell.ui.components.GroupRowDivider
import com.jonathaxs.gymnutshell.ui.components.GroupSection
import com.jonathaxs.gymnutshell.ui.theme.color
import kotlin.math.roundToInt

/** Cores fixas dos objetivos — laranja (bulking) e verde (cutting). Manutenção usa a cor de destaque do usuário. */
private val GoalBulkingColor = Color(0xFFFF9500)
private val GoalCuttingColor = Color(0xFF34C759)

/**
 * Sub-tela do objetivo fitness — porte de UserGoalChangeView (iOS): cards selecionáveis (Bulking/
 * Maintenance/Cutting) com prévia em tempo real das metas recalculadas. Salvar persiste o objetivo
 * (as metas fixas passam a derivar dele). O botão só fica ativo se a seleção mudou de fato.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserGoalScreen(onBack: () -> Unit, viewModel: SettingsViewModel = viewModel()) {
    val profile by viewModel.profile.collectAsStateWithLifecycle()
    val accent = viewModel.accentColor.collectAsStateWithLifecycle().value.color
    val measurement by viewModel.measurementSystem.collectAsStateWithLifecycle()

    // Seleção local; sincroniza com o objetivo salvo na carga inicial e após salvar.
    var selected by remember { mutableStateOf(profile.goal) }
    LaunchedEffect(profile.goal) { selected = profile.goal }
    val hasChanged = selected != profile.goal
    // Prévia recalculada só quando o usuário muda a seleção (igual ao iOS).
    val preview = if (hasChanged) GoalsProvider.goals(profile.copy(goal = selected)) else null

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings_fitness_goal)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.cd_back),
                        )
                    }
                },
                actions = {
                    TextButton(
                        onClick = { viewModel.saveProfile(profile.copy(goal = selected)) },
                        enabled = hasChanged,
                    ) {
                        Text(
                            stringResource(R.string.action_save),
                            color = if (hasChanged) accent else MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                },
            )
        },
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
        ) {
            // Cabeçalho da seleção.
            SectionLabel(stringResource(R.string.settings_goalchange_pick_header), accent)
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                UserGoal.entries.forEach { goal ->
                    GoalOptionCard(goal, goal == selected, accent) { selected = goal }
                }
            }
            // Rodapé explicativo (igual ao footer da seção de seleção do iOS).
            Text(
                stringResource(R.string.settings_goalchange_pick_footer),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 4.dp, end = 4.dp, top = 8.dp),
            )

            // Prévia das metas recalculadas — só aparece quando a seleção mudou.
            if (preview != null) {
                GroupSection(
                    title = stringResource(R.string.settings_goalchange_preview_header),
                    titleColor = accent,
                ) {
                    PreviewRow(stringResource(R.string.today_water), waterDisplay(preview.water, measurement))
                    GroupRowDivider()
                    PreviewRow(stringResource(R.string.today_calories), "${preview.calories} kcal")
                    GroupRowDivider()
                    PreviewRow(stringResource(R.string.today_protein), "${preview.protein} g")
                    GroupRowDivider()
                    PreviewRow(stringResource(R.string.today_carbs), "${preview.carbs} g")
                    GroupRowDivider()
                    PreviewRow(stringResource(R.string.today_good_fat), "${preview.goodFat} g")
                    GroupRowDivider()
                    PreviewRow(stringResource(R.string.today_fiber), "${preview.fiber} g")
                }
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

/** Card selecionável de objetivo: nome + descrição, fundo na cor do objetivo quando ativo — porte do UserGoalPickerView (iOS). */
@Composable
private fun GoalOptionCard(goal: UserGoal, selected: Boolean, accent: Color, onClick: () -> Unit) {
    val bg = if (selected) goalColor(goal, accent) else MaterialTheme.colorScheme.surfaceContainerHigh
    val fg = if (selected) Color.White else MaterialTheme.colorScheme.onSurface
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(bg)
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                stringResource(goalLabel(goal)),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = fg,
            )
            Text(
                stringResource(goalDesc(goal)),
                style = MaterialTheme.typography.bodySmall,
                color = if (selected) Color.White.copy(alpha = 0.85f) else MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        if (selected) {
            Spacer(Modifier.width(8.dp))
            Text("✓", color = Color.White, style = MaterialTheme.typography.titleMedium)
        }
    }
}

/** Linha da prévia: rótulo da meta + valor recalculado à direita. */
@Composable
private fun PreviewRow(label: String, value: String) {
    GroupRow(
        title = label,
        trailing = {
            Text(
                value,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        },
    )
}

/** Rótulo de seção no estilo dos títulos do GroupSection (cor de destaque). */
@Composable
private fun SectionLabel(text: String, accent: Color) {
    Text(
        text,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.SemiBold,
        color = accent,
        modifier = Modifier.padding(start = 4.dp, top = 22.dp, bottom = 8.dp),
    )
}

/** Água em fl oz no sistema US (armazenada em ml); ml para os demais — igual ao iOS. */
private fun waterDisplay(ml: Int, measurement: MeasurementSystem): String = when (measurement) {
    MeasurementSystem.Us -> "${UnitConverter.mlToFlOz(ml.toDouble()).roundToInt()} fl oz"
    else -> "$ml ml"
}

private fun goalColor(goal: UserGoal, accent: Color): Color = when (goal) {
    UserGoal.Bulking -> GoalBulkingColor
    UserGoal.Maintenance -> accent
    UserGoal.Cutting -> GoalCuttingColor
}

@StringRes
private fun goalLabel(goal: UserGoal): Int = when (goal) {
    UserGoal.Bulking -> R.string.goal_bulking
    UserGoal.Maintenance -> R.string.goal_maintenance
    UserGoal.Cutting -> R.string.goal_cutting
}

@StringRes
private fun goalDesc(goal: UserGoal): Int = when (goal) {
    UserGoal.Bulking -> R.string.welcome_goal_bulking_desc
    UserGoal.Maintenance -> R.string.welcome_goal_maintenance_desc
    UserGoal.Cutting -> R.string.welcome_goal_cutting_desc
}
