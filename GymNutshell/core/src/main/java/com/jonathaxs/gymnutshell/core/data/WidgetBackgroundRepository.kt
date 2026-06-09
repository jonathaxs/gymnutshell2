package com.jonathaxs.gymnutshell.core.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.jonathaxs.gymnutshell.core.widget.WidgetBackground
import com.jonathaxs.gymnutshell.core.widget.WidgetBackgroundMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

/**
 * Persiste o modo de fundo dos widgets + a cor personalizada — porte do WidgetBackgroundStore (iOS).
 * Guarda a cor escolhida separada do modo, pra não perder a seleção ao alternar entre os modos.
 */
class WidgetBackgroundRepository(private val context: Context) {

    private val modeKey = stringPreferencesKey("widget.background.mode")
    private val customKey = longPreferencesKey("widget.background.customArgb")

    val modeFlow: Flow<WidgetBackgroundMode> =
        context.appPreferences.data.map { WidgetBackgroundMode.fromRaw(it[modeKey]) }

    suspend fun mode(): WidgetBackgroundMode =
        WidgetBackgroundMode.fromRaw(context.appPreferences.data.first()[modeKey])

    suspend fun setMode(mode: WidgetBackgroundMode) {
        context.appPreferences.edit { it[modeKey] = mode.rawValue }
    }

    val customColorFlow: Flow<Long> =
        context.appPreferences.data.map { it[customKey] ?: WidgetBackground.DEFAULT_CUSTOM_ARGB }

    suspend fun customColor(): Long =
        context.appPreferences.data.first()[customKey] ?: WidgetBackground.DEFAULT_CUSTOM_ARGB

    suspend fun setCustomColor(argb: Long) {
        context.appPreferences.edit { it[customKey] = argb }
    }
}
