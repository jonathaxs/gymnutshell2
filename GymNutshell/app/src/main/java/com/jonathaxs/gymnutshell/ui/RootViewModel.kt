package com.jonathaxs.gymnutshell.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.jonathaxs.gymnutshell.core.data.ProfileRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

/** Decide a tela raiz: null = carregando, false = Welcome, true = app principal. */
class RootViewModel(app: Application) : AndroidViewModel(app) {

    private val profileRepo = ProfileRepository(app.applicationContext)

    val onboarded: StateFlow<Boolean?> = profileRepo.didCompleteOnboarding.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = null,
    )
}
