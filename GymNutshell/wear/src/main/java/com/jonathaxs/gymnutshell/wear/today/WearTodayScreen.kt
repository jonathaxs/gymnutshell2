package com.jonathaxs.gymnutshell.wear.today

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.items
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.ScreenScaffold
import androidx.wear.compose.material3.Text
import com.jonathaxs.gymnutshell.core.domain.ProgressColors
import com.jonathaxs.gymnutshell.wear.R

/**
 * Página Today do relógio — porte da ContentView do Watch app (iOS).
 * Hero com anel de progresso médio + cards de metas em pílula; tap no card
 * expande os controles −/+ e (nas metas de Treino) o toggle ON/OFF de descanso.
 */
@Composable
fun WearTodayScreen(viewModel: WearTodayViewModel = viewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var expandedKey by rememberSaveable { mutableStateOf<String?>(null) }
    val listState = rememberScalingLazyListState()

    ScreenScaffold(scrollState = listState) {
        ScalingLazyColumn(state = listState, modifier = Modifier.fillMaxSize()) {
            item { WearHero(state.overallProgress, state.tierEmoji, state.overallPercent) }
            items(state.goals, key = { it.key }) { goal ->
                WearGoalCard(
                    goal = goal,
                    accent = Color(state.accentArgb),
                    expanded = expandedKey == goal.key,
                    onToggleExpand = {
                        expandedKey = if (expandedKey == goal.key) null else goal.key
                    },
                    onAdjust = { direction -> viewModel.adjustIntake(goal, direction) },
                    onToggleRestDay = { viewModel.toggleRestDay(goal) },
                )
            }
        }
    }
}

// MARK: - Hero (anel + emoji do tier + percentual)

@Composable
private fun WearHero(progress: Float, tierEmoji: String, percent: Int) {
    val ringColor = Color(ProgressColors.ringArgb(progress.toDouble()))
    val trackColor = Color.Gray.copy(alpha = 0.25f)

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(Modifier.size(96.dp), contentAlignment = Alignment.Center) {
            // Anel desenhado na mão (track + arco a partir de -90°), igual à WatchHeroView.
            Canvas(Modifier.fillMaxSize()) {
                val stroke = 8.dp.toPx()
                val inset = stroke / 2
                val arcSize = Size(size.width - stroke, size.height - stroke)
                drawArc(
                    color = trackColor,
                    startAngle = 0f, sweepAngle = 360f, useCenter = false,
                    topLeft = Offset(inset, inset), size = arcSize,
                    style = Stroke(stroke),
                )
                drawArc(
                    color = ringColor,
                    startAngle = -90f, sweepAngle = 360f * progress.coerceIn(0f, 1f), useCenter = false,
                    topLeft = Offset(inset, inset), size = arcSize,
                    style = Stroke(stroke, cap = StrokeCap.Round),
                )
            }
            Text(tierEmoji, fontSize = 34.sp)
        }
        Spacer(Modifier.height(4.dp))
        Text(
            "$percent%",
            color = ringColor,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

// MARK: - Card de meta (pílula com fill + controles)

@Composable
private fun WearGoalCard(
    goal: WearGoalUi,
    accent: Color,
    expanded: Boolean,
    onToggleExpand: () -> Unit,
    onAdjust: (Int) -> Unit,
    onToggleRestDay: () -> Unit,
) {
    val title = goal.title ?: stringResource(goalTitleRes(goal.key))
    Column {
        GoalPill(goal, title, expanded, onToggleExpand)
        if (expanded) {
            Spacer(Modifier.height(4.dp))
            GoalControls(goal, title, accent, onAdjust, onToggleRestDay)
        }
    }
}

/** Pílula da meta: fundo cinza + preenchimento proporcional na cor do progresso. */
@Composable
private fun GoalPill(goal: WearGoalUi, title: String, expanded: Boolean, onToggleExpand: () -> Unit) {
    val fillColor = Color(ProgressColors.ringArgb(goal.progress)).copy(alpha = 0.45f)
    val shape = RoundedCornerShape(50)
    // TalkBack lê "Treino, 0 / 50 min" como um único botão (mesma locução do iOS).
    val valueLabel = if (goal.isRestDay) stringResource(R.string.rest_day)
    else "${goal.intake} / ${goal.target} ${goal.unit}"

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(Color.Gray.copy(alpha = 0.18f))
            .clickable(role = Role.Button, onClick = onToggleExpand)
            .semantics { contentDescription = "$title, $valueLabel" },
    ) {
        Box(Modifier.matchParentSize()) {
            Box(
                Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(goal.progress.toFloat().coerceIn(0f, 1f))
                    .background(fillColor),
            )
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 9.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(goal.emoji, fontSize = 16.sp)
            Spacer(Modifier.width(8.dp))
            Text(
                if (goal.isRestDay) stringResource(R.string.rest_day)
                else "${goal.intake} / ${goal.target} ${goal.unit}",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
            )
            Spacer(Modifier.weight(1f))
            Text(
                if (expanded) "▴" else "▾",
                style = MaterialTheme.typography.labelSmall,
                color = Color.Gray,
            )
        }
    }
}

/** Controles expandidos: ON/OFF (metas de Treino) + botões −/+. */
@Composable
private fun GoalControls(
    goal: WearGoalUi,
    title: String,
    accent: Color,
    onAdjust: (Int) -> Unit,
    onToggleRestDay: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (goal.supportsRestDay) {
            ControlPill(
                label = if (goal.isRestDay) "OFF" else "ON",
                textColor = if (goal.isRestDay) Color.Gray else accent,
                background = if (goal.isRestDay) accent.copy(alpha = 0.25f) else Color.Gray.copy(alpha = 0.20f),
                enabled = true,
                a11yLabel = stringResource(R.string.cd_rest_day, title),
                onClick = onToggleRestDay,
            )
            Spacer(Modifier.width(6.dp))
        }
        if (goal.isRestDay) {
            // Dia de descanso ativo: − e + somem, mostra o rótulo no espaço deles.
            Text(
                stringResource(R.string.rest_day),
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.labelMedium,
                color = Color.Gray,
                fontWeight = FontWeight.SemiBold,
            )
        } else {
            ControlPill(
                label = "−",
                textColor = accent,
                background = Color.Gray.copy(alpha = 0.20f),
                enabled = goal.intake > 0,
                a11yLabel = stringResource(R.string.cd_decrease, title),
                onClick = { onAdjust(-1) },
            )
            Spacer(Modifier.width(6.dp))
            ControlPill(
                label = "+",
                textColor = Color.White,
                background = accent,
                enabled = goal.intake < goal.target,
                a11yLabel = stringResource(R.string.cd_increase, title),
                onClick = { onAdjust(+1) },
            )
        }
    }
}

/** Botão em pílula dos controles (−, + e ON/OFF). */
@Composable
private fun RowScope.ControlPill(
    label: String,
    textColor: Color,
    background: Color,
    enabled: Boolean,
    a11yLabel: String,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .weight(1f)
            .height(34.dp)
            .clip(RoundedCornerShape(50))
            .background(if (enabled) background else background.copy(alpha = 0.10f))
            .clickable(enabled = enabled, role = Role.Button, onClick = onClick)
            .semantics { contentDescription = a11yLabel },
        contentAlignment = Alignment.Center,
    ) {
        Text(
            label,
            color = if (enabled) textColor else Color.Gray,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
        )
    }
}

/** Título localizado das metas built-in (mesmo mapa do GoalsWidget do celular). */
private fun goalTitleRes(key: String): Int = when (key) {
    "tracking.workout" -> R.string.wear_goal_workout
    "tracking.cardio" -> R.string.wear_goal_cardio
    "tracking.sleep" -> R.string.wear_goal_sleep
    "tracking.water" -> R.string.wear_goal_water
    "tracking.calories" -> R.string.wear_goal_calories
    "tracking.protein" -> R.string.wear_goal_protein
    "tracking.carbs" -> R.string.wear_goal_carbs
    "tracking.goodFat" -> R.string.wear_goal_good_fat
    "tracking.fiber" -> R.string.wear_goal_fiber
    "tracking.creatine" -> R.string.wear_goal_creatine
    else -> R.string.wear_goal_water
}
