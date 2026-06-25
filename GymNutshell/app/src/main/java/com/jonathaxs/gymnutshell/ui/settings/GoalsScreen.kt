package com.jonathaxs.gymnutshell.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.jonathaxs.gymnutshell.R
import com.jonathaxs.gymnutshell.ui.components.GroupCard
import com.jonathaxs.gymnutshell.ui.components.GroupInset
import com.jonathaxs.gymnutshell.ui.components.GroupRow
import com.jonathaxs.gymnutshell.ui.components.GroupRowDivider
import com.jonathaxs.gymnutshell.ui.components.GroupSection

// Verde/vermelho estilo iOS pros botões de restaurar (+) e remover (−).
private val RestoreGreen = Color(0xFF34C759)
private val RemoveRed = Color(0xFFFF3B30)

/** Hub de Goals — porte de TrackingGoalsSettingsView (iOS). */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoalsScreen(
    onBack: () -> Unit,
    onAddGoal: () -> Unit,
    onEditGoal: (Long) -> Unit,
    onOpenGoalDetail: (String) -> Unit,
    onOpenCategories: () -> Unit,
    viewModel: GoalsViewModel = viewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var editMode by remember { mutableStateOf(false) }
    val accent = Color(state.accentArgb)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.goals_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.cd_back))
                    }
                },
                actions = {
                    IconButton(onClick = onAddGoal) {
                        Icon(Icons.Default.Add, contentDescription = stringResource(R.string.a11y_add_goal))
                    }
                    TextButton(onClick = { editMode = !editMode }) {
                        Text(
                            stringResource(
                                if (editMode) R.string.tracking_goals_done else R.string.tracking_goals_edit,
                            ),
                        )
                    }
                },
            )
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(bottom = 32.dp),
        ) {
            // Atalho pra subpágina de Categorias (ordem + edição de personalizadas).
            item {
                GroupSection {
                    GroupRow(
                        title = stringResource(R.string.goals_categories_title),
                        showChevron = true,
                        onClick = onOpenCategories,
                    )
                }
            }

            state.sections.forEach { section ->
                item(key = "hdr_${section.id}") {
                    CategoryHeaderRow(
                        title = section.titleRes?.let { stringResource(it) } ?: section.title.orEmpty(),
                        collapsed = section.collapsed,
                        onToggle = { viewModel.toggleCategory(section.id) },
                    )
                }
                if (!section.collapsed) {
                    item(key = "card_${section.id}") {
                        val siblingIds = section.customRows.map { it.id }
                        GroupCard {
                            section.fixedRows.forEachIndexed { idx, row ->
                                if (idx > 0) GroupRowDivider()
                                FixedGoalRow(
                                    row = row,
                                    editMode = editMode,
                                    canUp = idx > 0,
                                    canDown = idx < section.fixedRows.lastIndex,
                                    onMoveUp = { viewModel.moveFixed(row.key, up = true) },
                                    onMoveDown = { viewModel.moveFixed(row.key, up = false) },
                                    onRemove = { viewModel.removeFixed(row.key) },
                                    onRestore = { viewModel.restoreFixed(row.key) },
                                    onClick = { onOpenGoalDetail(row.key) },
                                )
                            }
                            section.customRows.forEachIndexed { idx, row ->
                                if (idx > 0 || section.fixedRows.isNotEmpty()) GroupRowDivider()
                                CustomGoalRow(
                                    row = row,
                                    editMode = editMode,
                                    canUp = idx > 0,
                                    canDown = idx < section.customRows.lastIndex,
                                    onMoveUp = { viewModel.moveCustom(row.id, up = true, siblingIds) },
                                    onMoveDown = { viewModel.moveCustom(row.id, up = false, siblingIds) },
                                    onDelete = { viewModel.deleteCustom(row.id) },
                                    onClick = { onEditGoal(row.id) },
                                )
                            }
                        }
                    }
                }
            }

            // Metas personalizadas sem categoria, no fim (só excluir, sem reordenar — igual ao iOS).
            if (state.uncategorized.isNotEmpty()) {
                item {
                    GroupSection(title = stringResource(R.string.settings_custom_goals)) {
                        state.uncategorized.forEachIndexed { idx, row ->
                            if (idx > 0) GroupRowDivider()
                            CustomGoalRow(
                                row = row,
                                editMode = editMode,
                                canUp = false,
                                canDown = false,
                                onMoveUp = {},
                                onMoveDown = {},
                                onDelete = { viewModel.deleteCustom(row.id) },
                                onClick = { onEditGoal(row.id) },
                            )
                        }
                    }
                }
            }
        }
    }
}

/** Cabeçalho de categoria, clicável pra colapsar/expandir (chevron estilo Finder — porte do header da Settings iOS). */
@Composable
private fun CategoryHeaderRow(title: String, collapsed: Boolean, onToggle: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onToggle)
            .padding(start = GroupInset + 4.dp, end = GroupInset, top = 20.dp, bottom = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(if (collapsed) "▸" else "▾", color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.width(6.dp))
        Text(
            title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

/** Linha de meta fixa: emoji + título + valor; badge/chevron normal, ou setas/remover no modo editar. */
@Composable
private fun FixedGoalRow(
    row: FixedRowUi,
    editMode: Boolean,
    canUp: Boolean,
    canDown: Boolean,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
    onRemove: () -> Unit,
    onRestore: () -> Unit,
    onClick: () -> Unit,
) {
    if (row.removed) {
        // Meta opcional removida: acinzentada + botão verde pra restaurar (sempre visível).
        GroupRow(
            title = stringResource(row.titleRes),
            titleColor = MaterialTheme.colorScheme.onSurfaceVariant,
            leading = { Text(row.emoji, style = MaterialTheme.typography.titleLarge, modifier = Modifier.alpha(0.4f)) },
            trailing = {
                IconButton(onClick = onRestore) {
                    Text("＋", color = RestoreGreen, fontWeight = FontWeight.Bold, fontSize = 22.sp)
                }
            },
            modifier = Modifier.alpha(0.7f),
        )
        return
    }
    GroupRow(
        title = stringResource(row.titleRes),
        subtitle = row.valueDisplay,
        leading = { Text(row.emoji, style = MaterialTheme.typography.titleLarge) },
        trailing = {
            if (editMode) {
                ReorderControls(canUp, canDown, onMoveUp, onMoveDown) {
                    if (row.removable) {
                        IconButton(onClick = onRemove) {
                            Text("−", color = RemoveRed, fontWeight = FontWeight.Bold, fontSize = 24.sp)
                        }
                    }
                }
            } else {
                Text(
                    stringResource(row.badgeRes),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
        },
        showChevron = !editMode,
        onClick = if (editMode) null else onClick,
    )
}

/** Linha de meta personalizada: emoji + nome + valor; badge "Custom"/chevron, ou setas/excluir no modo editar. */
@Composable
private fun CustomGoalRow(
    row: CustomRowUi,
    editMode: Boolean,
    canUp: Boolean,
    canDown: Boolean,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
    onDelete: () -> Unit,
    onClick: () -> Unit,
) {
    GroupRow(
        title = row.title,
        subtitle = row.valueDisplay,
        leading = { Text(row.emoji, style = MaterialTheme.typography.titleLarge) },
        trailing = {
            if (editMode) {
                ReorderControls(canUp, canDown, onMoveUp, onMoveDown) {
                    IconButton(onClick = onDelete) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = stringResource(R.string.cd_delete),
                            tint = RemoveRed,
                        )
                    }
                }
            } else {
                Text(
                    stringResource(R.string.goal_badge_custom),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
        },
        showChevron = !editMode,
        onClick = if (editMode) null else onClick,
    )
}

/** Setas ↑/↓ de reordenação (desabilitadas nos extremos) + ação extra à direita (remover/excluir). */
@Composable
private fun ReorderControls(
    canUp: Boolean,
    canDown: Boolean,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
    trailingAction: @Composable () -> Unit,
) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(0.dp)) {
        IconButton(onClick = onMoveUp, enabled = canUp) {
            Icon(
                Icons.Default.KeyboardArrowUp,
                contentDescription = stringResource(R.string.cd_move_up),
                modifier = Modifier.size(22.dp),
                tint = if (canUp) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
            )
        }
        IconButton(onClick = onMoveDown, enabled = canDown) {
            Icon(
                Icons.Default.KeyboardArrowDown,
                contentDescription = stringResource(R.string.cd_move_down),
                modifier = Modifier.size(22.dp),
                tint = if (canDown) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
            )
        }
        trailingAction()
    }
}
