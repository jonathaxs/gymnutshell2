package com.jonathaxs.gymnutshell.ui.settings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.jonathaxs.gymnutshell.core.data.CustomGoal
import com.jonathaxs.gymnutshell.core.data.CustomGoalRepository
import com.jonathaxs.gymnutshell.core.domain.GoalCategory
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** ViewModel das metas personalizadas — porte (MVP) do gerenciamento de CustomTrackingGoal (iOS). */
class CustomGoalsViewModel(app: Application) : AndroidViewModel(app) {

    private val repo = CustomGoalRepository(app.applicationContext)

    val goals: StateFlow<List<CustomGoal>> = repo.goals.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList(),
    )

    fun add(name: String, emoji: String, unit: String, target: Int, increment: Int, category: GoalCategory?) {
        viewModelScope.launch {
            repo.add(
                CustomGoal(
                    emoji = emoji.ifBlank { "🎯" },
                    name = name.trim(),
                    unit = unit.trim(),
                    target = target,
                    increment = increment.coerceAtLeast(1),
                    categoryRaw = category?.rawValue,
                ),
            )
        }
    }

    fun delete(goal: CustomGoal) {
        viewModelScope.launch { repo.delete(goal) }
    }
}
