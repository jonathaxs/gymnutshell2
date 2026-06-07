package com.jonathaxs.gymnutshell.ui.welcome

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.jonathaxs.gymnutshell.core.data.ProfileRepository
import com.jonathaxs.gymnutshell.core.data.SettingsRepository
import com.jonathaxs.gymnutshell.core.domain.AppTheme
import com.jonathaxs.gymnutshell.core.domain.Profile
import kotlinx.coroutines.launch

/**
 * ViewModel do onboarding — porte (MVP) de WelcomeView (iOS).
 * Ao concluir, persiste o perfil + tema e marca o onboarding como feito
 * (o RootScreen reage e troca pra MainScreen).
 */
class WelcomeViewModel(app: Application) : AndroidViewModel(app) {

    private val profileRepo = ProfileRepository(app.applicationContext)
    private val settingsRepo = SettingsRepository(app.applicationContext)

    fun complete(profile: Profile, theme: AppTheme) {
        viewModelScope.launch {
            profileRepo.update(profile)
            settingsRepo.setTheme(theme)
            profileRepo.setOnboardingComplete()
        }
    }
}
