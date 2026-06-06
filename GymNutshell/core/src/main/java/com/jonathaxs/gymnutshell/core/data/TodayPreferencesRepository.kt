package com.jonathaxs.gymnutshell.core.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Estado de UI da TodayView que persiste — porte do @AppStorage "today.goal.collapsed" (iOS).
 * Guarda o conjunto de categorias recolhidas (pelo rawValue da GoalCategory).
 */
class TodayPreferencesRepository(private val context: Context) {

    private val collapsedKey = stringSetPreferencesKey("today.collapsedCategories")

    val collapsedCategories: Flow<Set<String>> =
        context.appPreferences.data.map { prefs -> prefs[collapsedKey] ?: emptySet() }

    suspend fun toggleCategory(categoryRaw: String) {
        context.appPreferences.edit { prefs ->
            val current = prefs[collapsedKey] ?: emptySet()
            prefs[collapsedKey] =
                if (categoryRaw in current) current - categoryRaw else current + categoryRaw
        }
    }
}
