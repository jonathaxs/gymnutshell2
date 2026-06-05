package com.jonathaxs.gymnutshell.core.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore

/**
 * Instância única do DataStore de preferências (arquivo "settings"), compartilhada pelos
 * repositórios (Settings, Profile, …). O delegate `preferencesDataStore` deve ser declarado
 * UMA vez por nome no processo — por isso fica centralizado aqui.
 */
internal val Context.appPreferences: DataStore<Preferences> by preferencesDataStore(name = "settings")
