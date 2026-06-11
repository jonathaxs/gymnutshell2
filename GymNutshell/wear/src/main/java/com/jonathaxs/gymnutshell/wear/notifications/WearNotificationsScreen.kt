package com.jonathaxs.gymnutshell.wear.notifications

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
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
import com.jonathaxs.gymnutshell.core.domain.AppDateFormatters
import com.jonathaxs.gymnutshell.core.domain.NotificationHistoryEntry
import com.jonathaxs.gymnutshell.wear.R
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

/**
 * Página Notifications do relógio — porte da WatchNotificationsView (iOS).
 * Histórico (últimos 3 dias, sincronizado do celular) agrupado por dia.
 * No iOS a exclusão era swipe; aqui tap na linha expande o botão Excluir
 * (mesmo padrão de expandir dos cards de meta).
 */
@Composable
fun WearNotificationsScreen(viewModel: WearNotificationsViewModel = viewModel()) {
    val entries by viewModel.entries.collectAsStateWithLifecycle()
    var expandedId by rememberSaveable { mutableStateOf<String?>(null) }

    if (entries.isEmpty()) {
        EmptyState()
        return
    }

    val zone = ZoneId.systemDefault()
    val grouped = entries
        .groupBy { Instant.ofEpochMilli(it.timestampMillis).atZone(zone).toLocalDate() }
        .toList()
        .sortedByDescending { it.first }

    val listState = rememberScalingLazyListState()
    ScreenScaffold(scrollState = listState) {
        ScalingLazyColumn(state = listState, modifier = Modifier.fillMaxSize()) {
            grouped.forEach { (date, items) ->
                item(key = "header-$date") { DayHeader(date) }
                items.forEach { entry ->
                    item(key = entry.id) {
                        NotificationRow(
                            entry = entry,
                            expanded = expandedId == entry.id,
                            onToggleExpand = {
                                expandedId = if (expandedId == entry.id) null else entry.id
                            },
                            onDelete = { viewModel.delete(entry) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyState() {
    ScreenScaffold {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("🔕", fontSize = 26.sp)
                Spacer(Modifier.height(6.dp))
                Text(
                    stringResource(R.string.wear_notif_empty),
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 24.dp),
                )
            }
        }
    }
}

/** Cabeçalho do grupo: Hoje / Ontem / data média. */
@Composable
private fun DayHeader(date: LocalDate) {
    val today = LocalDate.now()
    val label = when (date) {
        today -> stringResource(R.string.wear_notif_today)
        today.minusDays(1) -> stringResource(R.string.wear_notif_yesterday)
        else -> AppDateFormatters.mediumDate(date)
    }
    Text(
        label,
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.SemiBold,
        color = Color.Gray,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp, bottom = 2.dp),
        textAlign = TextAlign.Center,
    )
}

@Composable
private fun NotificationRow(
    entry: NotificationHistoryEntry,
    expanded: Boolean,
    onToggleExpand: () -> Unit,
    onDelete: () -> Unit,
) {
    val time = Instant.ofEpochMilli(entry.timestampMillis)
        .atZone(ZoneId.systemDefault())
        .format(DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT))

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Color.Gray.copy(alpha = 0.15f))
            .clickable(role = Role.Button, onClick = onToggleExpand)
            .padding(horizontal = 12.dp, vertical = 8.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(entry.kind?.emoji ?: "🔔", fontSize = 13.sp)
            Spacer(Modifier.width(5.dp))
            Text(
                entry.title,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
            )
        }
        Text(
            entry.body,
            style = MaterialTheme.typography.labelSmall,
            color = Color.Gray,
            maxLines = 2,
        )
        Text(
            time,
            style = MaterialTheme.typography.labelSmall,
            color = Color.Gray.copy(alpha = 0.7f),
        )

        if (expanded) {
            Spacer(Modifier.height(6.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(30.dp)
                    .clip(RoundedCornerShape(50))
                    .background(Color(0xFFFF3B30).copy(alpha = 0.25f))
                    .clickable(role = Role.Button, onClick = onDelete),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    stringResource(R.string.wear_notif_delete),
                    color = Color(0xFFFF3B30),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }
}
