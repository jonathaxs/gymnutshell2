package com.jonathaxs.gymnutshell.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.jonathaxs.gymnutshell.core.data.SettingsRepository
import com.jonathaxs.gymnutshell.core.theme.AccentColor
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

/**
 * ViewModel da tela raiz — equivalente ao @StateObject que segurava estado no MainView (iOS).
 * Lê a cor de destaque do DataStore (via SettingsRepository) e expõe como StateFlow pra UI.
 */
class MainViewModel(app: Application) : AndroidViewModel(app) {

    private val settings = SettingsRepository(app.applicationContext)

    val accentColor: StateFlow<AccentColor> = settings.accentColor.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = AccentColor.Default,
    )
}
