package com.jonathaxs.gymnutshell.wear.notifications

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.jonathaxs.gymnutshell.core.data.NotificationHistoryRepository
import com.jonathaxs.gymnutshell.core.domain.NotificationHistoryEntry
import com.jonathaxs.gymnutshell.wear.sync.WatchWearSync
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * ViewModel da página Notifications do relógio — porte da WatchNotificationsView (iOS).
 * O histórico chega via snapshot do celular; excluir aqui apaga local e publica o
 * delta de exclusão pro celular (porte do sendHistoryDelete).
 */
class WearNotificationsViewModel(app: Application) : AndroidViewModel(app) {

    private val historyRepo = NotificationHistoryRepository(app.applicationContext)

    val entries: StateFlow<List<NotificationHistoryEntry>> =
        historyRepo.entries.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun delete(entry: NotificationHistoryEntry) {
        viewModelScope.launch {
            historyRepo.delete(entry.id)
            WatchWearSync.sendHistoryDelete(getApplication(), entry.id)
        }
    }
}
