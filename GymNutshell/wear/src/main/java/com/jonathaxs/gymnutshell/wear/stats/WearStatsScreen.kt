package com.jonathaxs.gymnutshell.wear.stats

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.ScreenScaffold
import androidx.wear.compose.material3.Text
import com.jonathaxs.gymnutshell.core.domain.AppTheme
import com.jonathaxs.gymnutshell.core.domain.DailyAchievement
import com.jonathaxs.gymnutshell.core.sync.WearDayEntry
import com.jonathaxs.gymnutshell.core.sync.WearStatsSummary
import com.jonathaxs.gymnutshell.wear.R

/**
 * Página Stats do relógio — porte da WatchStatsView (iOS).
 * Renderiza o resumo sincronizado: pontos/dias, dias por tier, atividade e
 * faixa dos últimos 7 dias. Sem resumo ainda → estado "aguardando sync".
 */
@Composable
fun WearStatsScreen(viewModel: WearStatsViewModel = viewModel()) {
    val stats by viewModel.stats.collectAsStateWithLifecycle()
    val accentArgb by viewModel.accentArgb.collectAsStateWithLifecycle()
    val theme by viewModel.theme.collectAsStateWithLifecycle()
    val sex by viewModel.sex.collectAsStateWithLifecycle()

    val summary = stats
    if (summary == null) {
        SyncingState()
    } else {
        StatsContent(summary, Color(accentArgb), theme, sex)
    }
}

/** Estado aguardando o primeiro snapshot do celular. */
@Composable
private fun SyncingState() {
    ScreenScaffold {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("📱", fontSize = 26.sp)
                Spacer(Modifier.height(6.dp))
                Text(
                    stringResource(R.string.wear_stats_syncing),
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 24.dp),
                )
            }
        }
    }
}

@Composable
private fun StatsContent(s: WearStatsSummary, accent: Color, theme: AppTheme, sex: String) {
    val listState = rememberScalingLazyListState()
    ScreenScaffold(scrollState = listState) {
        ScalingLazyColumn(state = listState, modifier = Modifier.fillMaxSize()) {
            // Pontos e dias, duas colunas no topo.
            item {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    StatCard(formatted(s.totalPoints), stringResource(R.string.wear_stats_points), accent)
                    StatCard("${s.totalDays}", stringResource(R.string.wear_stats_days), accent)
                }
            }

            // Dias por tier.
            item { SectionHeader(stringResource(R.string.wear_stats_tiers)) }
            items(DailyAchievement.entries.size) { index ->
                val tier = DailyAchievement.entries[index]
                // Emoji e nome vêm do tema + sexo escolhidos no celular (chegam pelo snapshot).
                LabeledDaysRow(
                    emoji = theme.emoji(tier, sex),
                    label = stringResource(theme.tierNameRes(tier, sex)),
                    days = daysFor(tier, s),
                )
            }

            // Atividade.
            item { SectionHeader(stringResource(R.string.wear_stats_activity)) }
            item {
                LabeledDaysRow("🏋️", stringResource(R.string.wear_goal_workout), s.workoutDays)
            }
            item {
                LabeledDaysRow("🏃", stringResource(R.string.wear_goal_cardio), s.cardioDays)
            }

            // Últimos 7 dias.
            item { SectionHeader(stringResource(R.string.wear_stats_recent)) }
            item { RecentDaysStrip(s.last7Days, accent) }
        }
    }
}

// MARK: - Componentes

@Composable
private fun RowScope.StatCard(value: String, label: String, accent: Color) {
    Column(
        modifier = Modifier
            .weight(1f)
            .clip(RoundedCornerShape(12.dp))
            .background(Color.Gray.copy(alpha = 0.15f))
            .padding(vertical = 8.dp)
            .semantics { contentDescription = "$label, $value" },
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(value, color = accent, fontWeight = FontWeight.Bold, fontSize = 18.sp, maxLines = 1)
        Text(label, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        title,
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.SemiBold,
        color = Color.Gray,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp, bottom = 2.dp),
        textAlign = TextAlign.Center,
    )
}

/** Linha "emoji + rótulo ........ N days" (tiers e atividade). */
@Composable
private fun LabeledDaysRow(emoji: String, label: String, days: Int) {
    val daysLabel = stringResource(R.string.wear_stats_days_format, days)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 3.dp)
            .semantics { contentDescription = "$label, $daysLabel" },
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(emoji, fontSize = 13.sp)
        Spacer(Modifier.width(6.dp))
        Text(label, style = MaterialTheme.typography.labelMedium, maxLines = 1)
        Spacer(Modifier.weight(1f))
        Text(daysLabel, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
    }
}

/** Faixa dos últimos 7 dias: círculo com emoji da conquista ou ponto vazio. */
@Composable
private fun RecentDaysStrip(days: List<WearDayEntry>, accent: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.Center,
    ) {
        days.forEach { day ->
            Box(
                modifier = Modifier
                    .padding(horizontal = 2.dp)
                    .size(26.dp)
                    .clip(CircleShape)
                    .background(
                        if (day.emoji.isEmpty()) Color.Gray.copy(alpha = 0.2f)
                        else accent.copy(alpha = 0.2f),
                    ),
                contentAlignment = Alignment.Center,
            ) {
                if (day.emoji.isEmpty()) {
                    Box(
                        Modifier
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(Color.Gray.copy(alpha = 0.4f)),
                    )
                } else {
                    Text(day.emoji, fontSize = 12.sp)
                }
            }
        }
    }
}

// MARK: - Helpers

private fun daysFor(tier: DailyAchievement, s: WearStatsSummary): Int = when (tier) {
    DailyAchievement.Level1 -> s.level1Days
    DailyAchievement.Level2 -> s.level2Days
    DailyAchievement.Level3 -> s.level3Days
    DailyAchievement.Level4 -> s.level4Days
}

private fun formatted(value: Int): String =
    if (value >= 1000) String.format(java.util.Locale.US, "%.1fk", value / 1000.0) else "$value"
