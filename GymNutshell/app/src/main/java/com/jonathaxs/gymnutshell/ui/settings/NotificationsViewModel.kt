package com.jonathaxs.gymnutshell.ui.settings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.jonathaxs.gymnutshell.core.data.CustomGoal
import com.jonathaxs.gymnutshell.core.data.CustomGoalRepository
import com.jonathaxs.gymnutshell.core.data.NotificationPreferencesRepository
import com.jonathaxs.gymnutshell.core.domain.NotificationKind
import com.jonathaxs.gymnutshell.notifications.NotificationScheduler
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** Estado da tela de Notificações: quais kinds estão ligados + metas personalizadas. */
data class NotificationsUiState(
    /** Chave = rawValue do kind (ex.: "water") ou "custom.<id>". */
    val enabled: Map<String, Boolean> = emptyMap(),
    val customGoals: List<CustomGoal> = emptyList(),
) {
    fun isEnabled(kind: NotificationKind): Boolean = enabled[kind.rawValue] ?: false
    fun isCustomEnabled(id: Long): Boolean = enabled["custom.$id"] ?: false
}

/**
 * ViewModel da tela de Notificações — porte de NotificationsSettingsView (iOS).
 * Liga/desliga cada kind e re-agenda os lembretes por intervalo via [NotificationScheduler].
 */
class NotificationsViewModel(app: Application) : AndroidViewModel(app) {

    private val prefs = NotificationPreferencesRepository(app.applicationContext)
    private val customRepo = CustomGoalRepository(app.applicationContext)
    private val scheduler = NotificationScheduler(app.applicationContext)

    val state: StateFlow<NotificationsUiState> =
        combine(prefs.enabledStates, customRepo.goals) { enabled, goals ->
            NotificationsUiState(enabled, goals)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), NotificationsUiState())

    /** Liga/desliga um kind fixo e re-agenda (se for baseado em intervalo). */
    fun setEnabled(kind: NotificationKind, value: Boolean) {
        viewModelScope.launch {
            prefs.setEnabled(value, kind)
            if (kind.isIntervalBased) {
                if (value) scheduler.reschedule(kind) else scheduler.cancel(kind)
            }
        }
    }

    /** Liga/desliga o lembrete de uma meta personalizada e re-agenda. */
    fun setCustomEnabled(id: Long, value: Boolean) {
        viewModelScope.launch {
            prefs.setCustomEnabled(value, id)
            if (value) scheduler.rescheduleCustom(id) else scheduler.cancelCustom(id)
        }
    }

    /** Chamado quando o usuário concede a permissão pela tela: aplica defaults e re-arma tudo. */
    fun onPermissionGranted() {
        viewModelScope.launch {
            prefs.applyDefaultEnabledKindsOnce()
            scheduler.rescheduleAllActive()
        }
    }
}
