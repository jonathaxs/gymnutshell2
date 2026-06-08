package com.jonathaxs.gymnutshell.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.jonathaxs.gymnutshell.R
import com.jonathaxs.gymnutshell.core.data.NotificationPreferencesRepository
import com.jonathaxs.gymnutshell.core.domain.NotificationKind
import com.jonathaxs.gymnutshell.core.domain.NotificationSound
import com.jonathaxs.gymnutshell.notifications.NotificationScheduler
import com.jonathaxs.gymnutshell.notifications.NotificationStrings
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

/**
 * Editor de intervalo + som de uma notificação — porte de NotificationIntervalEditView (iOS).
 * O intervalo só aparece pros kinds editáveis (Metas + Progresso) e metas personalizadas; o som
 * vale pra todos. [target] vem codificado como "kind:water" ou "custom:3".
 */
@Composable
fun NotificationIntervalEditScreen(target: String, onBack: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val prefs = remember { NotificationPreferencesRepository(context.applicationContext) }
    val scheduler = remember { NotificationScheduler(context.applicationContext) }

    val kind = remember(target) {
        if (target.startsWith("kind:")) NotificationKind.fromRaw(target.removePrefix("kind:")) else null
    }
    val customId = remember(target) {
        if (target.startsWith("custom:")) target.removePrefix("custom:").toLongOrNull() else null
    }
    // Intervalo editável: Metas/Progresso (kind.isEditable) e qualquer meta personalizada.
    val showsInterval = kind?.isEditable ?: (customId != null)

    var minutes by remember { mutableIntStateOf(120) }
    var sound by remember { mutableStateOf(NotificationSound.Default) }
    var title by remember { mutableStateOf("") }

    LaunchedEffect(target) {
        when {
            kind != null -> {
                minutes = prefs.intervalMinutes(kind)
                sound = prefs.sound(kind)
                title = context.getString(NotificationStrings.titleRes(kind))
            }
            customId != null -> {
                minutes = prefs.customIntervalMinutes(customId)
                sound = prefs.customSound(customId)
                title = scheduler.customGoal(customId)?.let { "${it.emoji} ${it.name}" }.orEmpty()
            }
        }
    }

    val save: () -> Unit = {
        scope.launch {
            when {
                kind != null -> {
                    if (kind.isEditable) prefs.setIntervalMinutes(minutes, kind)
                    prefs.setSound(sound, kind)
                    if (kind.isIntervalBased) scheduler.reschedule(kind)
                }
                customId != null -> {
                    prefs.setCustomIntervalMinutes(minutes, customId)
                    prefs.setCustomSound(sound, customId)
                    scheduler.rescheduleCustom(customId)
                }
            }
            onBack()
        }
    }

    val screenTitle = title.ifBlank { stringResource(R.string.settings_notifications) }
    Scaffold(topBar = { SettingsTopBar(screenTitle, onBack) }) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            if (showsInterval) {
                Text(
                    stringResource(R.string.settings_notifications_interval_header),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    stringResource(R.string.settings_notifications_interval_value, minutes),
                    style = MaterialTheme.typography.bodyLarge,
                )
                // Slider de 15 a 600 min, em passos de 15 (mesmos limites do Stepper do iOS).
                Slider(
                    value = minutes.toFloat(),
                    onValueChange = { minutes = ((it / 15f).roundToInt() * 15).coerceIn(15, 600) },
                    valueRange = 15f..600f,
                )
                val footer = when {
                    kind != null -> stringResource(NotificationStrings.footerRes(kind))
                    else -> stringResource(R.string.notif_custom_footer)
                }
                Text(footer, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            Text(
                stringResource(R.string.settings_notifications_sound_header),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
            )
            val sounds = NotificationSound.entries
            SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
                sounds.forEachIndexed { index, option ->
                    SegmentedButton(
                        selected = sound == option,
                        onClick = { sound = option },
                        shape = SegmentedButtonDefaults.itemShape(index, sounds.size),
                    ) { Text(stringResource(NotificationStrings.soundRes(option))) }
                }
            }
            Text(
                stringResource(R.string.settings_notifications_sound_footer),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Button(onClick = save, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.action_save))
            }
        }
    }
}
