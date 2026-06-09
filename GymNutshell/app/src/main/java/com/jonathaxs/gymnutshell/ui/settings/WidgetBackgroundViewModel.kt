package com.jonathaxs.gymnutshell.ui.settings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.jonathaxs.gymnutshell.core.data.SettingsRepository
import com.jonathaxs.gymnutshell.core.data.WidgetBackgroundRepository
import com.jonathaxs.gymnutshell.core.widget.WidgetBackground
import com.jonathaxs.gymnutshell.core.widget.WidgetBackgroundMode
import com.jonathaxs.gymnutshell.widget.GymWidgets
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * ViewModel da tela de fundo dos widgets — porte de WidgetBackgroundSettingsView (iOS).
 * Expõe modo + cor custom + accent atual, e aplica as mudanças atualizando os widgets na hora.
 */
class WidgetBackgroundViewModel(app: Application) : AndroidViewModel(app) {

    private val repo = WidgetBackgroundRepository(app.applicationContext)
    private val settings = SettingsRepository(app.applicationContext)

    val mode: StateFlow<WidgetBackgroundMode> =
        repo.modeFlow.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), WidgetBackgroundMode.Default)

    val customArgb: StateFlow<Long> =
        repo.customColorFlow.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), WidgetBackground.DEFAULT_CUSTOM_ARGB)

    val accentArgb: StateFlow<Long> =
        settings.accentColor.map { it.argb }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), WidgetBackground.DEFAULT_CUSTOM_ARGB)

    fun setMode(mode: WidgetBackgroundMode) {
        viewModelScope.launch {
            repo.setMode(mode)
            GymWidgets.updateAll(getApplication())
        }
    }

    fun setCustomColor(argb: Long) {
        viewModelScope.launch {
            repo.setCustomColor(argb)
            GymWidgets.updateAll(getApplication())
        }
    }
}
