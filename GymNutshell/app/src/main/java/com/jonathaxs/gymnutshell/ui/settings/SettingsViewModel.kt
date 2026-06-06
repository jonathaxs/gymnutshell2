package com.jonathaxs.gymnutshell.ui.settings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.jonathaxs.gymnutshell.core.data.SettingsRepository
import com.jonathaxs.gymnutshell.core.theme.AccentColor
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * ViewModel da SettingsView — porte (MVP) começando pelo seletor de cor (ColorSettingsView do iOS).
 * Lê/grava a cor de destaque no DataStore (SettingsRepository); a mudança reflete em todo o app.
 */
class SettingsViewModel(app: Application) : AndroidViewModel(app) {

    private val repo = SettingsRepository(app.applicationContext)

    val accentColor: StateFlow<AccentColor> = repo.accentColor.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = AccentColor.Default,
    )

    fun setAccent(color: AccentColor) {
        viewModelScope.launch { repo.setAccentColor(color) }
    }
}
