package com.jonathaxs.gymnutshell.core.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.jonathaxs.gymnutshell.core.domain.AppOrientation
import com.jonathaxs.gymnutshell.core.domain.AppTheme
import com.jonathaxs.gymnutshell.core.domain.MeasurementSystem
import com.jonathaxs.gymnutshell.core.theme.AccentColor
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Persiste as preferências do usuário — porte dos "stores" que no iOS eram UserDefaults+Codable.
 * Expõe cada preferência como Flow (a UI reage a mudanças) + setter suspend.
 */
class SettingsRepository(private val context: Context) {

    private val accentKey = stringPreferencesKey(AccentColor.STORAGE_KEY)
    private val themeKey = stringPreferencesKey("app.theme")
    private val measurementKey = stringPreferencesKey("profile.measurementSystem")
    private val orientationKey = stringPreferencesKey(AppOrientation.STORAGE_KEY)

    /** Cor de destaque persistida; cai pra Default se nada salvo ou valor inválido. */
    val accentColor: Flow<AccentColor> = context.appPreferences.data.map { prefs ->
        prefs[accentKey]
            ?.let { name -> runCatching { AccentColor.valueOf(name) }.getOrNull() }
            ?: AccentColor.Default
    }

    suspend fun setAccentColor(color: AccentColor) {
        context.appPreferences.edit { prefs -> prefs[accentKey] = color.name }
    }

    /** Tema de mascote persistido; cai pro Default (gym) se nada salvo ou inválido. */
    val theme: Flow<AppTheme> = context.appPreferences.data.map { prefs ->
        AppTheme.fromRaw(prefs[themeKey])
    }

    suspend fun setTheme(theme: AppTheme) {
        context.appPreferences.edit { prefs -> prefs[themeKey] = theme.rawValue }
    }

    /** Sistema de medida persistido (Metric por padrão). */
    val measurementSystem: Flow<MeasurementSystem> = context.appPreferences.data.map { prefs ->
        MeasurementSystem.fromRaw(prefs[measurementKey])
    }

    suspend fun setMeasurementSystem(system: MeasurementSystem) {
        context.appPreferences.edit { prefs -> prefs[measurementKey] = system.rawValue }
    }

    /**
     * Travamento de orientação persistido (Portrait por padrão). Só é aplicado no celular —
     * em tablet a camada de UI ignora a preferência e mantém o app sempre livre.
     */
    val orientation: Flow<AppOrientation> = context.appPreferences.data.map { prefs ->
        AppOrientation.fromRaw(prefs[orientationKey])
    }

    suspend fun setOrientation(orientation: AppOrientation) {
        context.appPreferences.edit { prefs -> prefs[orientationKey] = orientation.rawValue }
    }
}
