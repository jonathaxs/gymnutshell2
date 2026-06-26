package com.jonathaxs.gymnutshell.ui.today

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlin.math.max
import kotlin.math.min

/** Uma célula renderizável do grid (uma categoria ou o bloco de metas sem categoria). */
class TodayGridCell(val id: String, val content: @Composable () -> Unit)

/**
 * Grid coluna-major das metas no modo wide+alto (tablet) — porte do TodayGoalsGrid (iOS).
 *
 * As células caem de cima pra baixo na coluna 1, depois a 2, etc. (em vez de por linha), pra que
 * categorias de alturas diferentes não criem buracos. O nº de colunas vem da largura disponível;
 * a sobra de células vai pras colunas da direita (que ficaram mais curtas), como no iOS.
 */
@Composable
fun TodayGoalsGrid(cells: List<TodayGridCell>, modifier: Modifier = Modifier) {
    BoxWithConstraints(modifier) {
        val cellMinWidth = 320.dp
        val columnSpacing = 16.dp
        // Cap no nº de células: em telas muito largas, mais colunas que células empurraria tudo pro canto.
        val computed = ((maxWidth + columnSpacing).value / (cellMinWidth + columnSpacing).value).toInt()
        val columns = max(1, min(computed, cells.size.coerceAtLeast(1)))

        // Distribuição contígua: base por coluna + 1 extra nas últimas (remainder).
        val base = cells.size / columns
        val remainder = cells.size % columns
        val perColumn = IntArray(columns) { col -> base + if (col >= columns - remainder) 1 else 0 }

        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(vertical = 12.dp),
        ) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(columnSpacing),
            ) {
                var start = 0
                for (col in 0 until columns) {
                    val count = perColumn[col]
                    val end = start + count
                    Column(
                        Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        for (i in start until end) cells[i].content()
                    }
                    start = end
                }
            }
        }
    }
}
