package com.jonathaxs.gymnutshell.ui.settings

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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.jonathaxs.gymnutshell.R

/** Sub-tela de dados físicos — porte de PhysicalDataSettingsView (iOS). */
@Composable
fun PhysicalDataScreen(onBack: () -> Unit, viewModel: SettingsViewModel = viewModel()) {
    val profile by viewModel.profile.collectAsStateWithLifecycle()
    Scaffold(topBar = { SettingsTopBar(stringResource(R.string.settings_physical_data), onBack) }) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
        ) {
            PhysicalDataSection(profile, onSave = viewModel::saveProfile)
        }
    }
}
