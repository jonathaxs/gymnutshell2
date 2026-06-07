package com.jonathaxs.gymnutshell.ui.settings

import androidx.annotation.StringRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.jonathaxs.gymnutshell.R
import com.jonathaxs.gymnutshell.core.domain.MeasurementSystem
import com.jonathaxs.gymnutshell.ui.theme.color

/** Sub-tela de unidade de medida — porte de MeasurementSettingsView (iOS): Metric/US. */
@Composable
fun MeasurementScreen(onBack: () -> Unit, viewModel: SettingsViewModel = viewModel()) {
    val selected by viewModel.measurementSystem.collectAsStateWithLifecycle()
    val accent = viewModel.accentColor.collectAsStateWithLifecycle().value.color

    Scaffold(topBar = { SettingsTopBar(stringResource(R.string.settings_units), onBack) }) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            MeasurementSystem.entries.forEachIndexed { index, system ->
                if (index > 0) HorizontalDivider()
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { viewModel.setMeasurement(system) }
                        .padding(horizontal = 16.dp, vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        stringResource(labelRes(system)),
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.bodyLarge,
                    )
                    if (system == selected) {
                        Text("✓", color = accent, style = MaterialTheme.typography.titleMedium)
                    }
                }
            }
        }
    }
}

@StringRes
private fun labelRes(system: MeasurementSystem): Int = when (system) {
    MeasurementSystem.Metric -> R.string.unit_metric
    MeasurementSystem.Us -> R.string.unit_us
}
