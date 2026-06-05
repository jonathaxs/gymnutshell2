package com.jonathaxs.gymnutshell.core.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.jonathaxs.gymnutshell.core.theme.AccentColor
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/** Instância única do DataStore de preferências (arquivo "settings"). */
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

/**
 * Persiste as preferências do usuário — porte dos "stores" que no iOS eram UserDefaults+Codable.
 * Expõe cada preferência como Flow (a UI reage a mudanças) + setter suspend.
 */
class SettingsRepository(private val context: Context) {

    private val accentKey = stringPreferencesKey(AccentColor.STORAGE_KEY)

    /** Cor de destaque persistida; cai pra Default se nada salvo ou valor inválido. */
    val accentColor: Flow<AccentColor> = context.dataStore.data.map { prefs ->
        prefs[accentKey]
            ?.let { name -> runCatching { AccentColor.valueOf(name) }.getOrNull() }
            ?: AccentColor.Default
    }

    suspend fun setAccentColor(color: AccentColor) {
        context.dataStore.edit { prefs -> prefs[accentKey] = color.name }
    }
}
