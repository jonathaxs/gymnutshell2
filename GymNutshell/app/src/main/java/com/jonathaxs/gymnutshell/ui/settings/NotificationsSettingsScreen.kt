package com.jonathaxs.gymnutshell.ui.settings

import android.Manifest
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.jonathaxs.gymnutshell.R
import com.jonathaxs.gymnutshell.core.domain.NotificationKind
import com.jonathaxs.gymnutshell.notifications.NotificationStrings

// Kinds da seção Sistema e da seção Metas (mesma ordem do iOS).
private val SYSTEM_KINDS = listOf(
    NotificationKind.Progress, NotificationKind.Achievement, NotificationKind.StreakBonus,
    NotificationKind.HealthSync, NotificationKind.Backup,
)
private val GOAL_KINDS = listOf(
    NotificationKind.Sleep, NotificationKind.Water, NotificationKind.Calories, NotificationKind.Protein,
    NotificationKind.Carbs, NotificationKind.GoodFat, NotificationKind.Fiber, NotificationKind.Workout,
    NotificationKind.Cardio, NotificationKind.Creatine,
)

/**
 * Tela de Notificações — porte de NotificationsSettingsView (iOS).
 * Toggles por kind (Sistema + Metas + metas personalizadas); tocar na linha abre o editor de
 * intervalo/som. Banner de permissão quando as notificações estão bloqueadas no SO.
 * [onEditTarget] recebe o alvo codificado ("kind:water" ou "custom:3").
 */
@Composable
fun NotificationsSettingsScreen(
    onBack: () -> Unit,
    onEditTarget: (String) -> Unit,
    viewModel: NotificationsViewModel = viewModel(),
) {
    val ui by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    // Estado de "notificações permitidas" no SO, revalidado ao voltar dos Ajustes do sistema.
    var allowed by remember { mutableStateOf(NotificationManagerCompat.from(context).areNotificationsEnabled()) }
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                allowed = NotificationManagerCompat.from(context).areNotificationsEnabled()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        allowed = NotificationManagerCompat.from(context).areNotificationsEnabled()
        if (granted) viewModel.onPermissionGranted() else openAppNotificationSettings(context)
    }
    val onEnableClick: () -> Unit = {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            openAppNotificationSettings(context)
        }
    }

    Scaffold(topBar = { SettingsTopBar(stringResource(R.string.settings_notifications), onBack) }) { padding ->
        LazyColumn(Modifier.fillMaxSize().padding(padding)) {
            if (!allowed) {
                item(key = "authorize") { AuthorizeBanner(onEnableClick) }
            }

            item(key = "header_system") { SectionHeader(stringResource(R.string.settings_notifications_section_system)) }
            items(SYSTEM_KINDS, key = { it.rawValue }) { kind ->
                NotificationRow(
                    leadingEmoji = kind.emoji,
                    title = stringResource(NotificationStrings.titleRes(kind)),
                    description = stringResource(NotificationStrings.descRes(kind)),
                    checked = ui.isEnabled(kind),
                    enabled = allowed,
                    onCheckedChange = { viewModel.setEnabled(kind, it) },
                    onOpen = { onEditTarget("kind:${kind.rawValue}") },
                )
            }

            item(key = "header_goals") { SectionHeader(stringResource(R.string.settings_notifications_section_goals)) }
            items(GOAL_KINDS, key = { it.rawValue }) { kind ->
                NotificationRow(
                    leadingEmoji = kind.emoji,
                    title = stringResource(NotificationStrings.titleRes(kind)),
                    description = stringResource(NotificationStrings.descRes(kind)),
                    checked = ui.isEnabled(kind),
                    enabled = allowed,
                    onCheckedChange = { viewModel.setEnabled(kind, it) },
                    onOpen = { onEditTarget("kind:${kind.rawValue}") },
                )
            }

            if (ui.customGoals.isNotEmpty()) {
                item(key = "header_custom") { SectionHeader(stringResource(R.string.settings_notifications_section_custom)) }
                items(ui.customGoals, key = { it.id }) { goal ->
                    NotificationRow(
                        leadingEmoji = goal.emoji,
                        title = goal.name,
                        description = stringResource(R.string.notif_custom_desc),
                        checked = ui.isCustomEnabled(goal.id),
                        enabled = allowed,
                        onCheckedChange = { viewModel.setCustomEnabled(goal.id, it) },
                        onOpen = { onEditTarget("custom:${goal.id}") },
                    )
                }
            }
        }
    }
}

/** Banner pra habilitar as notificações no SO (mostrado só quando estão bloqueadas). */
@Composable
private fun AuthorizeBanner(onEnable: () -> Unit) {
    Card(Modifier.fillMaxWidth().padding(16.dp)) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                stringResource(R.string.settings_notifications_authorize_footer),
                style = MaterialTheme.typography.bodyMedium,
            )
            Button(onClick = onEnable) {
                Text(stringResource(R.string.settings_notifications_authorize_button))
            }
        }
    }
}

/** Cabeçalho de seção. */
@Composable
private fun SectionHeader(title: String) {
    Text(
        title,
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
    )
}

/** Linha de notificação: emoji opcional + título/descrição (toca pra editar) + switch. */
@Composable
private fun NotificationRow(
    leadingEmoji: String?,
    title: String,
    description: String,
    checked: Boolean,
    enabled: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    onOpen: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = enabled, onClick = onOpen)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (leadingEmoji != null) {
            Text(leadingEmoji, style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.width(12.dp))
        }
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge)
            Text(
                description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange, enabled = enabled)
    }
}

/** Abre a tela de notificações do app nos Ajustes do sistema (fallback de permissão negada). */
private fun openAppNotificationSettings(context: Context) {
    val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
        putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    context.startActivity(intent)
}
