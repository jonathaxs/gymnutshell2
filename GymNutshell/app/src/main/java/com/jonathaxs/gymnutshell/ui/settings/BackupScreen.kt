package com.jonathaxs.gymnutshell.ui.settings

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.jonathaxs.gymnutshell.R

/**
 * Tela de Backup — porte da tela de backup do iOS (Settings → Backup).
 * Exporta o estado como JSON (criar documento) e restaura de um arquivo (abrir documento),
 * ambos via Storage Access Framework. A restauração é destrutiva, então pede confirmação.
 */
@Composable
fun BackupScreen(
    onBack: () -> Unit,
    viewModel: BackupViewModel = viewModel(),
) {
    val status by viewModel.status.collectAsStateWithLifecycle()
    val suggestedFilename by viewModel.suggestedFilename.collectAsStateWithLifecycle()

    // Uri pendente de import, aguardando confirmação do usuário.
    var pendingImport by remember { mutableStateOf<Uri?>(null) }

    val exportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/json"),
    ) { uri -> uri?.let { viewModel.exportTo(it) } }

    val importLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument(),
    ) { uri -> uri?.let { pendingImport = it } }

    Scaffold(topBar = { SettingsTopBar(stringResource(R.string.settings_backup), onBack) }) { padding ->
        Column(
            Modifier.fillMaxSize().padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                stringResource(R.string.backup_description),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Button(
                onClick = { viewModel.clearStatus(); exportLauncher.launch(suggestedFilename) },
                modifier = Modifier.fillMaxWidth(),
            ) { Text(stringResource(R.string.backup_export)) }
            OutlinedButton(
                onClick = { viewModel.clearStatus(); importLauncher.launch(arrayOf("application/json")) },
                modifier = Modifier.fillMaxWidth(),
            ) { Text(stringResource(R.string.backup_import)) }

            status?.let { StatusMessage(it) }
        }
    }

    // Confirmação antes da restauração (operação destrutiva).
    pendingImport?.let { uri ->
        AlertDialog(
            onDismissRequest = { pendingImport = null },
            title = { Text(stringResource(R.string.backup_import_confirm_title)) },
            text = { Text(stringResource(R.string.backup_import_confirm_message)) },
            confirmButton = {
                TextButton(onClick = { viewModel.importFrom(uri); pendingImport = null }) {
                    Text(stringResource(R.string.backup_import_confirm_button))
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingImport = null }) {
                    Text(stringResource(R.string.backup_cancel))
                }
            },
        )
    }
}

/** Linha de feedback da última operação (sucesso de export/import ou erro). */
@Composable
private fun StatusMessage(status: BackupStatus) {
    val (textRes, isError) = when (status) {
        BackupStatus.Exported -> R.string.backup_status_exported to false
        BackupStatus.Imported -> R.string.backup_status_imported to false
        BackupStatus.Error -> R.string.backup_status_error to true
    }
    Text(
        stringResource(textRes),
        style = MaterialTheme.typography.bodyMedium,
        color = if (isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
    )
}
