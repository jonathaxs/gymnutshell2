package com.jonathaxs.gymnutshell.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.jonathaxs.gymnutshell.core.data.ProfileRepository
import com.jonathaxs.gymnutshell.core.data.SettingsRepository
import com.jonathaxs.gymnutshell.core.domain.AppOrientation
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

/** Decide a tela raiz: null = carregando, false = Welcome, true = app principal. */
class RootViewModel(app: Application) : AndroidViewModel(app) {

    private val profileRepo = ProfileRepository(app.applicationContext)
    private val settingsRepo = SettingsRepository(app.applicationContext)

    val onboarded: StateFlow<Boolean?> = profileRepo.didCompleteOnboarding.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = null,
    )

    /** Preferência de orientação aplicada já na raiz (cobre Welcome + app principal). */
    val orientation: StateFlow<AppOrientation> = settingsRepo.orientation.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = AppOrientation.Default,
    )
}
