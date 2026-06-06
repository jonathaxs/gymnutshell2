package com.jonathaxs.gymnutshell.ui.progress

import androidx.annotation.StringRes
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
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.jonathaxs.gymnutshell.R

/** Aba Progress — porte (MVP) da ProgressOverView (iOS): resumo, tiers, atividade e bônus. */
@Composable
fun ProgressScreen(modifier: Modifier = Modifier, viewModel: ProgressViewModel = viewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item { SummaryRow(state) }

        item {
            SectionCard(R.string.progress_section_tiers) {
                state.tierCounts.forEach { tier ->
                    StatLine(
                        leading = tier.emoji,
                        label = stringResource(R.string.progress_tier_level, tier.level),
                        value = stringResource(R.string.progress_days_count, tier.days),
                    )
                }
            }
        }

        item {
            SectionCard(R.string.progress_section_activity) {
                StatLine("🏋️", stringResource(R.string.progress_activity_workout),
                    stringResource(R.string.progress_days_count, state.workoutDays))
                StatLine("🏃", stringResource(R.string.progress_activity_cardio),
                    stringResource(R.string.progress_days_count, state.cardioDays))
            }
        }

        item {
            SectionCard(R.string.progress_section_bonuses) {
                StatLine("🎖️", stringResource(R.string.progress_bonus_weekly_strong), "${state.weeklyStrong}")
                StatLine("💀", stringResource(R.string.progress_bonus_weekly_expert), "${state.weeklyExpert}")
                StatLine("🏆", stringResource(R.string.progress_bonus_monthly_strong), "${state.monthlyStrong}")
                StatLine("☠️", stringResource(R.string.progress_bonus_monthly_expert), "${state.monthlyExpert}")
            }
        }
    }
}

/** Linha de 3 estatísticas-resumo: dias, pontos, bônus. */
@Composable
private fun SummaryRow(state: ProgressUiState) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        SummaryStat(state.totalDays.toString(), R.string.progress_days, Modifier.weight(1f))
        SummaryStat(state.totalPoints.toString(), R.string.progress_points, Modifier.weight(1f))
        SummaryStat(state.bonusCount.toString(), R.string.progress_bonuses, Modifier.weight(1f))
    }
}

@Composable
private fun SummaryStat(value: String, @StringRes labelRes: Int, modifier: Modifier = Modifier) {
    Card(modifier) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(value, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Text(
                stringResource(labelRes),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

/** Card de seção com título + conteúdo. */
@Composable
private fun SectionCard(@StringRes titleRes: Int, content: @Composable () -> Unit) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Text(
                stringResource(titleRes),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
            Spacer(Modifier.width(8.dp))
            content()
        }
    }
}

/** Linha: emoji + rótulo (esquerda) e valor (direita). */
@Composable
private fun StatLine(leading: String, label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(leading, style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.width(12.dp))
        Text(label, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyLarge)
        Text(
            value,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.End,
        )
    }
}
