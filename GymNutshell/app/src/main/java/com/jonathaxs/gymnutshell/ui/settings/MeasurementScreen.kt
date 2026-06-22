package com.jonathaxs.gymnutshell.ui.settings

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.jonathaxs.gymnutshell.R
import com.jonathaxs.gymnutshell.core.domain.MeasurementSystem
import com.jonathaxs.gymnutshell.ui.components.GroupCheck
import com.jonathaxs.gymnutshell.ui.components.GroupRow
import com.jonathaxs.gymnutshell.ui.components.GroupRowDivider
import com.jonathaxs.gymnutshell.ui.components.GroupSection
import com.jonathaxs.gymnutshell.ui.theme.color

/** Sub-tela de unidade de medida — porte de MeasurementSettingsView (iOS): Metric/US. */
@Composable
fun MeasurementScreen(onBack: () -> Unit, viewModel: SettingsViewModel = viewModel()) {
    val selected by viewModel.measurementSystem.collectAsStateWithLifecycle()
    val accent = viewModel.accentColor.collectAsStateWithLifecycle().value.color

    Scaffold(topBar = { SettingsTopBar(stringResource(R.string.settings_units), onBack) }) { padding ->
        Column(
            Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()),
        ) {
            GroupSection {
                MeasurementSystem.entries.forEachIndexed { index, system ->
                    if (index > 0) GroupRowDivider()
                    GroupRow(
                        title = stringResource(labelRes(system)),
                        trailing = if (system == selected) ({ GroupCheck(accent) }) else null,
                        onClick = { viewModel.setMeasurement(system) },
                    )
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
