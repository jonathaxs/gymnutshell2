package com.jonathaxs.gymnutshell.ui.welcome

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.jonathaxs.gymnutshell.core.backup.BackupService
import com.jonathaxs.gymnutshell.core.data.GoalConfigRepository
import com.jonathaxs.gymnutshell.core.data.ProfileRepository
import com.jonathaxs.gymnutshell.core.data.SettingsRepository
import com.jonathaxs.gymnutshell.core.domain.AppTheme
import com.jonathaxs.gymnutshell.core.domain.Profile
import com.jonathaxs.gymnutshell.core.theme.AccentColor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * ViewModel do onboarding — porte de WelcomeView (iOS).
 * Ao concluir, persiste o perfil + tema, define a cor de destaque pelo sexo escolhido,
 * marca como removidas as metas opcionais que o usuário não adicionou e finaliza o onboarding
 * (o RootScreen reage e troca pra MainScreen). Também permite restaurar um backup local logo no início.
 */
class WelcomeViewModel(app: Application) : AndroidViewModel(app) {

    private val profileRepo = ProfileRepository(app.applicationContext)
    private val settingsRepo = SettingsRepository(app.applicationContext)
    private val goalConfigRepo = GoalConfigRepository(app.applicationContext)
    private val backupService = BackupService(app.applicationContext)

    // Vira true quando uma restauração falha (arquivo inválido), pra mostrar o alerta de erro.
    private val _restoreFailed = MutableStateFlow(false)
    val restoreFailed: StateFlow<Boolean> = _restoreFailed.asStateFlow()

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

    /**
     * Restaura um backup local (JSON) escolhido pelo usuário e finaliza o onboarding em caso de sucesso
     * — espelha o "Restaurar backup" do WelcomeStartStep (iOS, via arquivo). Em falha, sinaliza o erro.
     */
    fun restore(uri: Uri) {
        viewModelScope.launch {
            val ok = runCatching {
                val text = withContext(Dispatchers.IO) {
                    getApplication<Application>().contentResolver.openInputStream(uri)?.use { input ->
                        input.readBytes().decodeToString()
                    } ?: error("sem stream de entrada")
                }
                backupService.importJson(text)
            }.isSuccess
            if (ok) profileRepo.setOnboardingComplete() else _restoreFailed.value = true
        }
    }

    fun clearRestoreError() {
        _restoreFailed.value = false
    }
}
