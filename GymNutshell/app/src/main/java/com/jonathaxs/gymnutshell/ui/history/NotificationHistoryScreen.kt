package com.jonathaxs.gymnutshell.ui.history

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.jonathaxs.gymnutshell.R
import com.jonathaxs.gymnutshell.core.domain.AppDateFormatters
import com.jonathaxs.gymnutshell.core.domain.NotificationHistoryEntry
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

/**
 * Histórico de notificações de evento dos últimos 3 dias — porte de NotificationHistorySheet (iOS).
 * Agrupado por dia; tocar numa linha dispara o mesmo deep-link da notificação original.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationHistoryScreen(
    onBack: () -> Unit,
    onOpenRoute: (routeRaw: String, achievementEpochDay: Long?) -> Unit,
    viewModel: NotificationHistoryViewModel = viewModel(),
) {
    val entries by viewModel.entries.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.notif_history_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.cd_back))
                    }
                },
                actions = {
                    if (entries.isNotEmpty()) {
                        TextButton(onClick = { viewModel.clear() }) {
                            Text(stringResource(R.string.notif_history_clear))
                        }
                    }
                },
            )
        },
    ) { padding ->
        if (entries.isEmpty()) {
            EmptyState(Modifier.fillMaxSize().padding(padding))
        } else {
            // Agrupa por dia (mais recente primeiro); itens dentro do dia já vêm ordenados desc.
            val grouped = entries.groupBy { epochDayOf(it.timestampMillis) }
                .toList().sortedByDescending { it.first }

            LazyColumn(Modifier.fillMaxSize().padding(padding)) {
                grouped.forEach { (day, items) ->
                    item(key = "day_$day") { DayHeader(day) }
                    items(items, key = { it.id }) { entry ->
                        HistoryRow(
                            entry = entry,
                            onClick = { onOpenRoute(entry.routeRaw, entry.achievementEpochDay) },
                            onDelete = { viewModel.delete(entry.id) },
                        )
                    }
                }
            }
        }
    }
}

/** Estado vazio: sino + título + descrição. */
@Composable
private fun EmptyState(modifier: Modifier) {
    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text("🔕", style = MaterialTheme.typography.displaySmall)
        Spacer(Modifier.width(8.dp))
        Text(
            stringResource(R.string.notif_history_empty_title),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
        )
        Text(
            stringResource(R.string.notif_history_empty_desc),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

/** Cabeçalho de dia: Hoje / Ontem / data por extenso. */
@Composable
private fun DayHeader(epochDay: Long) {
    val today = LocalDate.now().toEpochDay()
    val title = when (epochDay) {
        today -> stringResource(R.string.notif_history_today)
        today - 1 -> stringResource(R.string.notif_history_yesterday)
        else -> AppDateFormatters.longDate(LocalDate.ofEpochDay(epochDay))
    }
    Text(
        title,
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
    )
}

/** Linha do histórico: emoji + título/corpo/hora (toca pra abrir a rota) + botão de apagar. */
@Composable
private fun HistoryRow(entry: NotificationHistoryEntry, onClick: () -> Unit, onDelete: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(entry.kind?.emoji ?: "🔔", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(entry.title, style = MaterialTheme.typography.bodyLarge)
            Text(
                entry.body,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                timeOf(entry.timestampMillis),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        IconButton(onClick = onDelete) {
            Text("✕", style = MaterialTheme.typography.titleMedium)
        }
    }
}

private val shortTime: DateTimeFormatter = DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT)

private fun epochDayOf(millis: Long): Long =
    Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).toLocalDate().toEpochDay()

private fun timeOf(millis: Long): String =
    Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).toLocalTime().format(shortTime)
