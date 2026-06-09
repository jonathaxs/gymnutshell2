package com.jonathaxs.gymnutshell.ui.settings

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.jonathaxs.gymnutshell.core.backup.BackupService
import com.jonathaxs.gymnutshell.core.data.ProfileRepository
import com.jonathaxs.gymnutshell.notifications.GymNotifier
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/** Resultado da última operação de backup, pra mostrar feedback na tela. */
enum class BackupStatus { Exported, Imported, Error }

/**
 * ViewModel da tela de Backup. Faz a ponte entre os seletores de arquivo (Storage Access Framework)
 * e o [BackupService]: lê/escreve o JSON via `contentResolver` no Uri escolhido pelo usuário.
 */
class BackupViewModel(app: Application) : AndroidViewModel(app) {

    private val service = BackupService(app.applicationContext)
    private val notifier = GymNotifier(app.applicationContext)
    private val profileRepo = ProfileRepository(app.applicationContext)

    private val _status = MutableStateFlow<BackupStatus?>(null)
    val status: StateFlow<BackupStatus?> = _status.asStateFlow()

    // Nome de arquivo sugerido pro seletor de "criar documento" (depende do nome do perfil).
    private val _suggestedFilename = MutableStateFlow("gymnutshell-backup.json")
    val suggestedFilename: StateFlow<String> = _suggestedFilename.asStateFlow()

    init {
        viewModelScope.launch {
            _suggestedFilename.value = service.suggestedFilename(profileRepo.profile.first().name)
        }
    }

    /** Exporta o estado atual pro Uri escolhido; dispara a notificação de backup ao concluir. */
    fun exportTo(uri: Uri) {
        viewModelScope.launch {
            val ok = runCatching {
                val json = service.exportJson()
                withContext(Dispatchers.IO) {
                    getApplication<Application>().contentResolver.openOutputStream(uri)?.use { out ->
                        out.write(json.toByteArray())
                    } ?: error("sem stream de saída")
                }
            }.isSuccess
            if (ok) notifier.fireBackupCompleted()
            _status.value = if (ok) BackupStatus.Exported else BackupStatus.Error
        }
    }

    /** Lê o JSON do Uri e restaura o app por inteiro (substitui o estado atual). */
    fun importFrom(uri: Uri) {
        viewModelScope.launch {
            val ok = runCatching {
                val text = withContext(Dispatchers.IO) {
                    getApplication<Application>().contentResolver.openInputStream(uri)?.use { input ->
                        input.readBytes().decodeToString()
                    } ?: error("sem stream de entrada")
                }
                service.importJson(text)
            }.isSuccess
            _status.value = if (ok) BackupStatus.Imported else BackupStatus.Error
        }
    }

    fun clearStatus() {
        _status.value = null
    }
}
