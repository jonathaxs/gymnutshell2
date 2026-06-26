package com.jonathaxs.gymnutshell.ui.welcome

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.jonathaxs.gymnutshell.core.data.GoalConfigRepository
import com.jonathaxs.gymnutshell.core.data.ProfileRepository
import com.jonathaxs.gymnutshell.core.data.SettingsRepository
import com.jonathaxs.gymnutshell.core.domain.AppTheme
import com.jonathaxs.gymnutshell.core.domain.Profile
import com.jonathaxs.gymnutshell.core.theme.AccentColor
import kotlinx.coroutines.launch

/**
 * ViewModel do onboarding — porte de WelcomeView (iOS).
 * Ao concluir, persiste o perfil + tema, define a cor de destaque pelo sexo escolhido,
 * marca como removidas as metas opcionais que o usuário não adicionou e finaliza o onboarding
 * (o RootScreen reage e troca pra MainScreen).
 */
class WelcomeViewModel(app: Application) : AndroidViewModel(app) {

    private val profileRepo = ProfileRepository(app.applicationContext)
    private val settingsRepo = SettingsRepository(app.applicationContext)
    private val goalConfigRepo = GoalConfigRepository(app.applicationContext)

    fun complete(profile: Profile, theme: AppTheme, includeFats: Boolean, includeCreatine: Boolean) {
        viewModelScope.launch {
            profileRepo.update(profile)
            settingsRepo.setTheme(theme)
            // Cor de destaque inicial sugerida pelo sexo — o usuário pode trocar em Settings > Cores.
            settingsRepo.setAccentColor(AccentColor.defaultForSex(profile.sex))
            // Metas opcionais começam desligadas no iOS: removidas se o usuário não tocou em "Adicionar".
            if (!includeFats) goalConfigRepo.remove("tracking.goodFat")
            if (!includeCreatine) goalConfigRepo.remove("tracking.creatine")
            profileRepo.setOnboardingComplete()
        }
    }
}
