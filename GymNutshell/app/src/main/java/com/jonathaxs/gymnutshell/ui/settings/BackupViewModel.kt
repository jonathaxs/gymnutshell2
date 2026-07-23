package com.jonathaxs.gymnutshell.ui.settings

import android.app.Application
import android.content.Intent
import android.net.Uri
import androidx.activity.result.IntentSenderRequest
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.jonathaxs.gymnutshell.backup.DriveBackupClient
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

/** Resultado da última operação de backup no Drive. `Empty` = não havia backup pra restaurar. */
enum class DriveStatus { Working, BackedUp, Restored, Empty, Error }

/**
 * ViewModel da tela de Backup. Faz a ponte entre os seletores de arquivo (Storage Access Framework)
 * e o [BackupService]: lê/escreve o JSON via `contentResolver` no Uri escolhido pelo usuário.
 */
class BackupViewModel(app: Application) : AndroidViewModel(app) {

    private val service = BackupService(app.applicationContext)
    private val notifier = GymNotifier(app.applicationContext)
    private val profileRepo = ProfileRepository(app.applicationContext)
    private val driveClient = DriveBackupClient(app.applicationContext)

    private val _status = MutableStateFlow<BackupStatus?>(null)
    val status: StateFlow<BackupStatus?> = _status.asStateFlow()

    private val _driveStatus = MutableStateFlow<DriveStatus?>(null)
    val driveStatus: StateFlow<DriveStatus?> = _driveStatus.asStateFlow()

    // Resolução de autorização (escolha de conta/consentimento) que a UI precisa lançar.
    private val _authRequest = MutableStateFlow<IntentSenderRequest?>(null)
    val authRequest: StateFlow<IntentSenderRequest?> = _authRequest.asStateFlow()

    // Ação pendente a retomar quando o token chegar (após a resolução da UI).
    private var resumeWithToken: (suspend (String) -> Unit)? = null

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

    // MARK: - Backup no Google Drive

    /** Sobe o estado atual pro Drive (cria/atualiza o backup na pasta appDataFolder). */
    fun backupToDrive() {
        viewModelScope.launch {
            _driveStatus.value = DriveStatus.Working
            withToken { token ->
                runCatching { driveClient.upload(token, service.exportJson()) }
                    .onSuccess { notifier.fireBackupCompleted(); _driveStatus.value = DriveStatus.BackedUp }
                    .onFailure { _driveStatus.value = DriveStatus.Error }
            }
        }
    }

    /** Baixa o backup do Drive e restaura o app por inteiro (destrutivo). */
    fun restoreFromDrive() {
        viewModelScope.launch {
            _driveStatus.value = DriveStatus.Working
            withToken { token ->
                runCatching { driveClient.download(token) }
                    .onSuccess { text ->
                        if (text == null) {
                            _driveStatus.value = DriveStatus.Empty
                        } else {
                            service.importJson(text)
                            _driveStatus.value = DriveStatus.Restored
                        }
                    }
                    .onFailure { _driveStatus.value = DriveStatus.Error }
            }
        }
    }

    /**
     * Garante um token do escopo drive.appdata e roda [block]. Se a autorização exigir interação
     * (escolha de conta/consentimento), guarda o [block] e pede pra UI lançar a resolução; a retomada
     * acontece em [onAuthResult].
     */
    private suspend fun withToken(block: suspend (String) -> Unit) {
        val result = runCatching { driveClient.authorize() }.getOrNull()
        if (result == null) {
            _driveStatus.value = DriveStatus.Error
            return
        }
        val token = result.accessToken
        val resolution = result.pendingIntent
        when {
            token != null -> block(token)
            result.hasResolution() && resolution != null -> {
                resumeWithToken = block
                _authRequest.value = IntentSenderRequest.Builder(resolution.intentSender).build()
            }
            else -> _driveStatus.value = DriveStatus.Error
        }
    }

    /** Chamado pela UI após a resolução da autorização: extrai o token e retoma a ação pendente. */
    fun onAuthResult(intent: Intent?) {
        _authRequest.value = null
        val block = resumeWithToken ?: return
        resumeWithToken = null
        viewModelScope.launch {
            val token = runCatching { intent?.let { driveClient.resultFromIntent(it).accessToken } }.getOrNull()
            if (token != null) block(token) else _driveStatus.value = DriveStatus.Error
        }
    }

    fun clearDriveStatus() {
        _driveStatus.value = null
    }
}
