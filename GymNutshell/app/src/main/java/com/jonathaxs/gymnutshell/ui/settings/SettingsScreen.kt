package com.jonathaxs.gymnutshell.ui.settings

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.jonathaxs.gymnutshell.R
import com.jonathaxs.gymnutshell.core.theme.AccentColor
import com.jonathaxs.gymnutshell.ui.theme.color

/** Aba Settings — porte (MVP) da SettingsView (iOS): começa pelo seletor de cor de destaque. */
@Composable
fun SettingsScreen(modifier: Modifier = Modifier, viewModel: SettingsViewModel = viewModel()) {
    val selected by viewModel.accentColor.collectAsStateWithLifecycle()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
    ) {
        Text(
            stringResource(R.string.settings_appearance),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
        )
        Spacer(Modifier.height(4.dp))
        Text(
            stringResource(R.string.settings_accent_color),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(16.dp))
        ColorGrid(selected, onSelect = viewModel::setAccent)
    }
}

/** Grade 4 colunas com as 8 cores de destaque. */
@Composable
private fun ColorGrid(selected: AccentColor, onSelect: (AccentColor) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.fillMaxWidth()) {
        AccentColor.entries.chunked(4).forEach { rowColors ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                rowColors.forEach { accent ->
                    ColorSwatch(accent, accent == selected, Modifier.weight(1f)) { onSelect(accent) }
                }
                repeat(4 - rowColors.size) { Spacer(Modifier.weight(1f)) }
            }
        }
    }
}

/** Um círculo de cor com nome; anel + checkmark quando selecionado. */
@Composable
private fun ColorSwatch(
    accent: AccentColor,
    isSelected: Boolean,
    modifier: Modifier,
    onClick: () -> Unit,
) {
    val name = stringResource(colorNameRes(accent))
    val stateDesc = stringResource(if (isSelected) R.string.a11y_selected else R.string.a11y_not_selected)
    Column(
        modifier = modifier
            .clickable(onClick = onClick)
            .semantics {
                contentDescription = name
                stateDescription = stateDesc
            },
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .then(if (isSelected) Modifier.border(2.5.dp, accent.color, CircleShape) else Modifier)
                .padding(4.dp)
                .clip(CircleShape)
                .background(accent.color),
            contentAlignment = Alignment.Center,
        ) {
            if (isSelected) {
                Text("✓", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
        Spacer(Modifier.height(6.dp))
        Text(
            name,
            style = MaterialTheme.typography.labelMedium,
            textAlign = TextAlign.Center,
            modifier = Modifier.clearAndSetSemantics {},
        )
    }
}

@StringRes
private fun colorNameRes(accent: AccentColor): Int = when (accent) {
    AccentColor.Red -> R.string.color_red
    AccentColor.Blue -> R.string.color_blue
    AccentColor.Purple -> R.string.color_purple
    AccentColor.Green -> R.string.color_green
    AccentColor.Yellow -> R.string.color_yellow
    AccentColor.Orange -> R.string.color_orange
    AccentColor.Cyan -> R.string.color_cyan
    AccentColor.Pink -> R.string.color_pink
}
