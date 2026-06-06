package com.jonathaxs.gymnutshell.ui.settings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.jonathaxs.gymnutshell.core.data.ProfileRepository
import com.jonathaxs.gymnutshell.core.data.SettingsRepository
import com.jonathaxs.gymnutshell.core.domain.Profile
import com.jonathaxs.gymnutshell.core.theme.AccentColor
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * ViewModel da SettingsView — porte (MVP): seletor de cor (ColorSettingsView) + dados físicos
 * (PhysicalDataSettingsView). Grava a cor no DataStore e o perfil no ProfileRepository;
 * salvar o perfil faz as metas da Today virem dos dados reais (substitui o perfil-demo).
 */
class SettingsViewModel(app: Application) : AndroidViewModel(app) {

    private val settingsRepo = SettingsRepository(app.applicationContext)
    private val profileRepo = ProfileRepository(app.applicationContext)

    val accentColor: StateFlow<AccentColor> = settingsRepo.accentColor.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = AccentColor.Default,
    )

    val profile: StateFlow<Profile> = profileRepo.profile.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = Profile(),
    )

    fun setAccent(color: AccentColor) {
        viewModelScope.launch { settingsRepo.setAccentColor(color) }
    }

    fun saveProfile(profile: Profile) {
        viewModelScope.launch { profileRepo.update(profile) }
    }
}
