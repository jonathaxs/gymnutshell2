package com.jonathaxs.gymnutshell.wear.stats

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.jonathaxs.gymnutshell.core.data.SettingsRepository
import com.jonathaxs.gymnutshell.core.data.WearStatsRepository
import com.jonathaxs.gymnutshell.core.sync.WearStatsSummary
import com.jonathaxs.gymnutshell.core.theme.AccentColor
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

/**
 * ViewModel da página Stats do relógio — porte da WatchStatsView (iOS).
 * Só renderiza o resumo calculado no celular; null = nunca sincronizou.
 */
class WearStatsViewModel(app: Application) : AndroidViewModel(app) {

    val stats: StateFlow<WearStatsSummary?> =
        WearStatsRepository(app.applicationContext).stats
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val accentArgb: StateFlow<Long> =
        SettingsRepository(app.applicationContext).accentColor
            .map { it.argb }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AccentColor.Default.argb)
}
