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
import com.jonathaxs.gymnutshell.core.domain.AppOrientation
import com.jonathaxs.gymnutshell.ui.components.GroupCheck
import com.jonathaxs.gymnutshell.ui.components.GroupRow
import com.jonathaxs.gymnutshell.ui.components.GroupRowDivider
import com.jonathaxs.gymnutshell.ui.components.GroupSection
import com.jonathaxs.gymnutshell.ui.theme.color

/**
 * Sub-tela de orientação — porte de OrientationSettingsView (iOS): trava na vertical ou aceita ambas.
 * Toque aplica na hora (sem botão de salvar). Só é exibida no celular; o tablet fica sempre livre.
 */
@Composable
fun OrientationSettingsScreen(onBack: () -> Unit, viewModel: SettingsViewModel = viewModel()) {
    val selected by viewModel.orientation.collectAsStateWithLifecycle()
    val accent = viewModel.accentColor.collectAsStateWithLifecycle().value.color

    Scaffold(topBar = { SettingsTopBar(stringResource(R.string.settings_orientation), onBack) }) { padding ->
        Column(
            Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()),
        ) {
            GroupSection(footer = stringResource(R.string.settings_orientation_footer)) {
                AppOrientation.entries.forEachIndexed { index, orientation ->
                    if (index > 0) GroupRowDivider()
                    GroupRow(
                        title = stringResource(labelRes(orientation)),
                        trailing = if (orientation == selected) ({ GroupCheck(accent) }) else null,
                        onClick = { viewModel.setOrientation(orientation) },
                    )
                }
            }
        }
    }
}

@StringRes
private fun labelRes(orientation: AppOrientation): Int = when (orientation) {
    AppOrientation.Portrait -> R.string.settings_orientation_portrait
    AppOrientation.Both -> R.string.settings_orientation_both
}
