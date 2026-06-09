package com.jonathaxs.gymnutshell.ui.settings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.jonathaxs.gymnutshell.core.data.HealthPreferencesRepository
import com.jonathaxs.gymnutshell.health.HealthConnectManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** Estado da tela de Saúde: toggles + disponibilidade e permissões do Health Connect. */
data class HealthSettingsUiState(
    val syncSleep: Boolean = false,
    val autoCheckin: Boolean = false,
    val isAvailable: Boolean = true,
    val hasSleepPermission: Boolean = false,
    val hasWorkoutPermission: Boolean = false,
)

/**
 * ViewModel da tela de Saúde — porte de HealthSettingsView (iOS).
 * Persiste os dois toggles e expõe o estado de disponibilidade/permissão do Health Connect,
 * revalidado via [refresh] quando a tela volta ao foco ou após o usuário responder ao pedido.
 */
class HealthSettingsViewModel(app: Application) : AndroidViewModel(app) {

    private val prefs = HealthPreferencesRepository(app.applicationContext)
    private val manager = HealthConnectManager(app.applicationContext)

    // Snapshot de disponibilidade/permissão — não é Flow; é relido sob demanda no refresh().
    private val permission = MutableStateFlow(PermissionSnapshot())

    val state: StateFlow<HealthSettingsUiState> = combine(
        prefs.syncSleepEnabledFlow,
        prefs.autoWorkoutCheckinFlow,
        permission,
    ) { syncSleep, autoCheckin, perm ->
        HealthSettingsUiState(
            syncSleep = syncSleep,
            autoCheckin = autoCheckin,
            isAvailable = perm.available,
            hasSleepPermission = perm.hasSleep,
            hasWorkoutPermission = perm.hasWorkout,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HealthSettingsUiState())

    /** Permissões a pedir ao ligar cada toggle (lançadas pela tela via contract do Health Connect). */
    val sleepPermissions: Set<String> get() = manager.sleepPermissions
    val workoutPermissions: Set<String> get() = manager.workoutPermissions

    init { refresh() }

    /** Relê disponibilidade e permissões concedidas (ON_RESUME e após o pedido de permissão). */
    fun refresh() {
        viewModelScope.launch {
            permission.value = PermissionSnapshot(
                available = manager.isAvailable,
                hasSleep = manager.hasSleepPermission(),
                hasWorkout = manager.hasWorkoutPermission(),
            )
        }
    }

    fun setSyncSleep(enabled: Boolean) {
        viewModelScope.launch { prefs.setSyncSleepEnabled(enabled) }
    }

    fun setAutoCheckin(enabled: Boolean) {
        viewModelScope.launch { prefs.setAutoWorkoutCheckin(enabled) }
    }

    /** Disponibilidade + permissões num instantâneo só (default "disponível" pra evitar piscar o banner). */
    private data class PermissionSnapshot(
        val available: Boolean = true,
        val hasSleep: Boolean = false,
        val hasWorkout: Boolean = false,
    )
}
