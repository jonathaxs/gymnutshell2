package com.jonathaxs.gymnutshell.ui.achievements

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.jonathaxs.gymnutshell.R

/** Aba Achievements — porte da AchievementsView (iOS): sino de notificações, seletor Calendário/Lista e histórico. */
@Composable
fun AchievementsScreen(
    modifier: Modifier = Modifier,
    onOpenHistory: () -> Unit = {},
    viewModel: AchievementsViewModel = viewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val accent = Color(state.accentArgb)

    Column(modifier.fillMaxSize()) {
        AchievementsHeader(accent = accent, onOpenHistory = onOpenHistory)
        FilterSelector(selected = state.filterMode, accent = accent, onSelect = viewModel::setFilterMode)
        // Calendário só aparece no modo Calendário; no modo Lista some e o histórico ocupa a tela toda.
        if (state.filterMode == AchievementsFilterMode.Calendar) {
            MonthHeader(state.monthLabel, onPrev = viewModel::previousMonth, onNext = viewModel::nextMonth)
            CalendarGrid(state, accent, onSelect = viewModel::selectDay)
        }
        HorizontalDivider(Modifier.padding(vertical = 8.dp))
        if (state.history.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    stringResource(R.string.achievements_empty),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        } else {
            LazyColumn(Modifier.fillMaxSize()) {
                items(state.history, key = { it.epochDay }) { HistoryRow(it) }
            }
        }
    }
}

/** Cabeçalho: sino que abre o histórico de notificações + título da tela. Espelha a toolbar do iOS. */
@Composable
private fun AchievementsHeader(accent: Color, onOpenHistory: () -> Unit) {
    val bellDesc = stringResource(R.string.cd_notification_history)
    Row(
        modifier = Modifier.fillMaxWidth().padding(start = 4.dp, end = 16.dp, top = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onOpenHistory, modifier = Modifier.semantics { contentDescription = bellDesc }) {
            Icon(Icons.Default.Notifications, contentDescription = null, tint = accent)
        }
        Text(
            stringResource(R.string.achievements_header_title),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
        )
    }
}

/** Seletor segmentado Calendário / Lista — espelha o Picker(.segmented) do iOS. */
@Composable
private fun FilterSelector(
    selected: AchievementsFilterMode,
    accent: Color,
    onSelect: (AchievementsFilterMode) -> Unit,
) {
    val colors = SegmentedButtonDefaults.colors(
        activeContainerColor = accent.copy(alpha = 0.15f),
        activeContentColor = accent,
        activeBorderColor = accent,
    )
    SingleChoiceSegmentedButtonRow(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
    ) {
        SegmentedButton(
            selected = selected == AchievementsFilterMode.Calendar,
            onClick = { onSelect(AchievementsFilterMode.Calendar) },
            shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
            colors = colors,
        ) { Text(stringResource(R.string.achievements_filter_calendar)) }
        SegmentedButton(
            selected = selected == AchievementsFilterMode.List,
            onClick = { onSelect(AchievementsFilterMode.List) },
            shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
            colors = colors,
        ) { Text(stringResource(R.string.achievements_filter_list)) }
    }
}

/** Cabeçalho do mês com setas ‹ ›. */
@Composable
private fun MonthHeader(monthLabel: String, onPrev: () -> Unit, onNext: () -> Unit) {
    val prevDesc = stringResource(R.string.cd_prev_month)
    val nextDesc = stringResource(R.string.cd_next_month)
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onPrev, modifier = Modifier.semantics { contentDescription = prevDesc }) {
            Text("‹", style = MaterialTheme.typography.headlineSmall)
        }
        Text(
            monthLabel,
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.titleMedium,
        )
        IconButton(onClick = onNext, modifier = Modifier.semantics { contentDescription = nextDesc }) {
            Text("›", style = MaterialTheme.typography.headlineSmall)
        }
    }
}

/** Grade do mês: cabeçalho de dias da semana + semanas de 7 células. */
@Composable
private fun CalendarGrid(state: AchievementsUiState, accent: Color, onSelect: (Long) -> Unit) {
    Column(Modifier.fillMaxWidth().padding(horizontal = 8.dp)) {
        Row(Modifier.fillMaxWidth()) {
            state.weekdays.forEach { symbol ->
                Text(
                    symbol,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        Spacer(Modifier.height(8.dp))
        state.days.chunked(7).forEach { week ->
            Row(Modifier.fillMaxWidth()) {
                week.forEach { day ->
                    DayCell(day, accent, Modifier.weight(1f)) { onSelect(day.epochDay) }
                }
                repeat(7 - week.size) { Spacer(Modifier.weight(1f)) }
            }
        }
    }
}

/** Célula de um dia: número + emoji do tier; fundo na cor de destaque quando selecionada. */
@Composable
private fun DayCell(day: CalendarDayUi, accent: Color, modifier: Modifier, onSelect: () -> Unit) {
    val contentColor = when {
        day.isSelected -> Color.White
        day.inMonth -> MaterialTheme.colorScheme.onSurface
        else -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.45f)
    }
    Column(
        modifier = modifier
            .height(48.dp)
            .padding(2.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(if (day.isSelected) accent else Color.Transparent)
            .clickable(enabled = day.inMonth, onClick = onSelect)
            .semantics { contentDescription = "${day.dayNumber}" },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text("${day.dayNumber}", style = MaterialTheme.typography.bodyMedium, color = contentColor)
        if (day.emoji != null) {
            Text(day.emoji, style = MaterialTheme.typography.bodySmall)
        }
    }
}

/** Linha do histórico: emoji do tier, data e %. */
@Composable
private fun HistoryRow(item: HistoryItemUi) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(item.emoji, style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.width(12.dp))
        Text(item.dateLabel, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyLarge)
        Text(
            "${item.percent}%",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
