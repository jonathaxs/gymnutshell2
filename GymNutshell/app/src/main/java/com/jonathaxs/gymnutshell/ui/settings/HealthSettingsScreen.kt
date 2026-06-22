package com.jonathaxs.gymnutshell.ui.settings

import android.content.Context
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.health.connect.client.PermissionController
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.jonathaxs.gymnutshell.R
import com.jonathaxs.gymnutshell.ui.components.GroupRow
import com.jonathaxs.gymnutshell.ui.components.GroupRowDivider
import com.jonathaxs.gymnutshell.ui.components.GroupSection

/**
 * Tela de Saúde — porte de HealthSettingsView (iOS).
 * Dois toggles (sincronizar sono e auto-detectar treinos); ligar cada um pede a permissão
 * correspondente no Health Connect. Banner quando o provedor não está disponível no aparelho.
 */
@Composable
fun HealthSettingsScreen(
    onBack: () -> Unit,
    viewModel: HealthSettingsViewModel = viewModel(),
) {
    val ui by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    // Revalida disponibilidade/permissões ao voltar pra tela (o usuário pode mudar no app do Health Connect).
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) viewModel.refresh()
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    // Pedido de permissões do Health Connect (UI do sistema); ao voltar, relê o estado concedido.
    val requestPermissions = rememberLauncherForActivityResult(
        PermissionController.createRequestPermissionResultContract(),
    ) { viewModel.refresh() }

    Scaffold(topBar = { SettingsTopBar(stringResource(R.string.settings_health), onBack) }) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState())) {
            if (!ui.isAvailable) {
                UnavailableBanner(onSetup = { openHealthConnect(context) })
            } else {
                GroupSection(footer = stringResource(R.string.settings_health_footer)) {
                    GroupRow(
                        title = stringResource(R.string.settings_health_sync_sleep),
                        trailing = {
                            Switch(checked = ui.syncSleep, onCheckedChange = { on ->
                                viewModel.setSyncSleep(on)
                                if (on && !ui.hasSleepPermission) requestPermissions.launch(viewModel.sleepPermissions)
                            })
                        },
                    )
                    GroupRowDivider()
                    GroupRow(
                        title = stringResource(R.string.settings_health_auto_checkin),
                        trailing = {
                            Switch(checked = ui.autoCheckin, onCheckedChange = { on ->
                                viewModel.setAutoCheckin(on)
                                if (on && !ui.hasWorkoutPermission) requestPermissions.launch(viewModel.workoutPermissions)
                            })
                        },
                    )
                }
            }
        }
    }
}

/** Banner quando o Health Connect não está disponível: explica e leva à instalação/configuração. */
@Composable
private fun UnavailableBanner(onSetup: () -> Unit) {
    Card(Modifier.fillMaxWidth().padding(16.dp)) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                stringResource(R.string.settings_health_unavailable_title),
                style = MaterialTheme.typography.titleMedium,
            )
            Text(
                stringResource(R.string.settings_health_unavailable_desc),
                style = MaterialTheme.typography.bodyMedium,
            )
            Button(onClick = onSetup) {
                Text(stringResource(R.string.settings_health_unavailable_button))
            }
        }
    }
}

/** Abre a Play Store no provedor do Health Connect (instalar/atualizar quando indisponível). */
private fun openHealthConnect(context: Context) {
    // Pacote do provedor oficial do Health Connect (a constante do SDK é internal).
    val uri = "market://details?id=$HEALTH_CONNECT_PROVIDER_PACKAGE".toUri()
    runCatching {
        context.startActivity(Intent(Intent.ACTION_VIEW, uri).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    }
}

private const val HEALTH_CONNECT_PROVIDER_PACKAGE = "com.google.android.apps.healthdata"
