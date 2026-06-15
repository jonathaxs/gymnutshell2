package com.jonathaxs.gymnutshell.ui.history

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
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
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

/**
 * Histórico de notificações de evento dos últimos 3 dias — porte da NotificationHistorySheet (iOS),
 * apresentado como bottom sheet (ModalBottomSheet) aberto ao tocar na data da TodayScreen, espelhando
 * a apresentação em sheet do iOS. Agrupado por dia; tocar numa linha dispara o mesmo deep-link da
 * notificação original e fecha o sheet.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationHistorySheet(
    onDismiss: () -> Unit,
    onOpenRoute: (routeRaw: String, achievementEpochDay: Long?) -> Unit,
    viewModel: NotificationHistoryViewModel = viewModel(),
) {
    val entries by viewModel.entries.collectAsStateWithLifecycle()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()

    // Anima o fechamento do sheet e só então executa a ação (navegar pra rota da notificação).
    fun closeThen(action: () -> Unit) {
        scope.launch { sheetState.hide() }.invokeOnCompletion {
            if (!sheetState.isVisible) {
                onDismiss()
                action()
            }
        }
    }

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        // Cabeçalho do sheet: título + botão limpar (só quando há entradas).
        Row(
            modifier = Modifier.fillMaxWidth().padding(start = 24.dp, end = 12.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                stringResource(R.string.notif_history_title),
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
            )
            if (entries.isNotEmpty()) {
                TextButton(onClick = { viewModel.clear() }) {
                    Text(stringResource(R.string.notif_history_clear))
                }
            }
        }
        NotificationHistoryContent(
            entries = entries,
            onOpenRoute = { route, day -> closeThen { onOpenRoute(route, day) } },
            onDelete = { viewModel.delete(it) },
        )
    }
}

/** Miolo do histórico: estado vazio ou lista agrupada por dia. Rola dentro do sheet quando passa da altura. */
@Composable
private fun NotificationHistoryContent(
    entries: List<NotificationHistoryEntry>,
    onOpenRoute: (routeRaw: String, achievementEpochDay: Long?) -> Unit,
    onDelete: (String) -> Unit,
) {
    if (entries.isEmpty()) {
        EmptyState(Modifier.fillMaxWidth().padding(vertical = 24.dp))
        return
    }
    // Agrupa por dia (mais recente primeiro); itens dentro do dia já vêm ordenados desc.
    val grouped = entries.groupBy { epochDayOf(it.timestampMillis) }
        .toList().sortedByDescending { it.first }

    Column(
        Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 16.dp),
    ) {
        grouped.forEach { (day, items) ->
            DayHeader(day)
            items.forEach { entry ->
                HistoryRow(
                    entry = entry,
                    onClick = { onOpenRoute(entry.routeRaw, entry.achievementEpochDay) },
                    onDelete = { onDelete(entry.id) },
                )
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
