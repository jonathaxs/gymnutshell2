package com.jonathaxs.gymnutshell.ui.history

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.jonathaxs.gymnutshell.core.data.NotificationHistoryRepository
import com.jonathaxs.gymnutshell.core.domain.NotificationHistoryEntry
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** ViewModel do histórico de notificações — porte (MVP) do NotificationHistoryStore (iOS). */
class NotificationHistoryViewModel(app: Application) : AndroidViewModel(app) {

    private val repo = NotificationHistoryRepository(app.applicationContext)

    val entries: StateFlow<List<NotificationHistoryEntry>> = repo.entries.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList(),
    )

    fun delete(id: String) {
        viewModelScope.launch { repo.delete(id) }
    }

    fun clear() {
        viewModelScope.launch { repo.clear() }
    }
}
