package com.jonathaxs.gymnutshell.ui.settings

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.jonathaxs.gymnutshell.R
import com.jonathaxs.gymnutshell.core.widget.WidgetBackground
import com.jonathaxs.gymnutshell.core.widget.WidgetBackgroundMode

/** Paleta de cores personalizadas pro fundo do widget (ARGB). */
private val CUSTOM_PALETTE = listOf(
    0xFFFF3B30L, 0xFFFF9500L, 0xFFFFCC00L, 0xFF34C759L,
    0xFF32ADE6L, 0xFF007AFFL, 0xFF5856D6L, 0xFFAF52DEL,
    0xFFFF2D55L, 0xFFA2845EL, 0xFF8E8E93L, 0xFF1C1C1EL,
)

/** Sub-tela "Fundo do widget" — porte de WidgetBackgroundSettingsView (iOS). */
@Composable
fun WidgetBackgroundScreen(onBack: () -> Unit, viewModel: WidgetBackgroundViewModel = viewModel()) {
    val mode by viewModel.mode.collectAsStateWithLifecycle()
    val customArgb by viewModel.customArgb.collectAsStateWithLifecycle()
    val accentArgb by viewModel.accentArgb.collectAsStateWithLifecycle()

    Scaffold(topBar = { SettingsTopBar(stringResource(R.string.settings_widget_background), onBack) }) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
        ) {
            // Seletor de modo (Padrão / Destaque / Personalizado).
            SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
                val options = WidgetBackgroundMode.entries
                options.forEachIndexed { index, option ->
                    SegmentedButton(
                        selected = mode == option,
                        onClick = { viewModel.setMode(option) },
                        shape = SegmentedButtonDefaults.itemShape(index, options.size),
                    ) {
                        Text(stringResource(modeLabelRes(option)))
                    }
                }
            }

            Spacer(Modifier.height(12.dp))
            Text(
                stringResource(footerRes(mode)),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            // Prévia do fundo (com a cor da fonte contrastante), exceto no modo Padrão.
            if (mode != WidgetBackgroundMode.System) {
                val source = if (mode == WidgetBackgroundMode.Accent) accentArgb else customArgb
                Spacer(Modifier.height(20.dp))
                Text(stringResource(R.string.widget_bg_preview), style = MaterialTheme.typography.titleSmall)
                Spacer(Modifier.height(8.dp))
                PreviewTile(source)
            }

            // Paleta de cores no modo Personalizado.
            if (mode == WidgetBackgroundMode.Custom) {
                Spacer(Modifier.height(20.dp))
                Text(stringResource(R.string.widget_bg_pick_color), style = MaterialTheme.typography.titleSmall)
                Spacer(Modifier.height(8.dp))
                ColorPalette(selected = customArgb, onSelect = viewModel::setCustomColor)
            }
        }
    }
}

/** Tile de prévia: gradiente da cor escolhida + um "65%" na cor de texto contrastante. */
@Composable
private fun PreviewTile(argb: Long) {
    val textColor = Color(WidgetBackground.contrastingTextArgb(argb))
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(96.dp)
            .clip(RoundedCornerShape(22.dp))
            .background(gradientBrush(argb)),
        contentAlignment = Alignment.Center,
    ) {
        Text("65%", color = textColor, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.headlineMedium)
    }
}

/** Grade 4 colunas com as cores da paleta; anel + check na selecionada. */
@Composable
private fun ColorPalette(selected: Long, onSelect: (Long) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
        CUSTOM_PALETTE.chunked(4).forEach { rowColors ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                rowColors.forEach { argb ->
                    val color = Color(argb)
                    val isSelected = argb == selected
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp)
                            .then(if (isSelected) Modifier.border(2.5.dp, color, CircleShape) else Modifier)
                            .padding(4.dp)
                            .clip(CircleShape)
                            .background(color)
                            .clickable { onSelect(argb) },
                        contentAlignment = Alignment.Center,
                    ) {
                        if (isSelected) {
                            Text("✓", color = Color(WidgetBackground.contrastingTextArgb(argb)), fontWeight = FontWeight.Bold)
                        }
                    }
                }
                repeat(4 - rowColors.size) { Spacer(Modifier.weight(1f)) }
            }
        }
    }
}

private fun gradientBrush(argb: Long): Brush = Brush.linearGradient(
    listOf(Color(WidgetBackground.lighterArgb(argb)), Color(WidgetBackground.darkerArgb(argb))),
)

private fun modeLabelRes(mode: WidgetBackgroundMode): Int = when (mode) {
    WidgetBackgroundMode.System -> R.string.widget_bg_mode_system
    WidgetBackgroundMode.Accent -> R.string.widget_bg_mode_accent
    WidgetBackgroundMode.Custom -> R.string.widget_bg_mode_custom
}

private fun footerRes(mode: WidgetBackgroundMode): Int = when (mode) {
    WidgetBackgroundMode.System -> R.string.widget_bg_footer_system
    WidgetBackgroundMode.Accent -> R.string.widget_bg_footer_accent
    WidgetBackgroundMode.Custom -> R.string.widget_bg_footer_custom
}
