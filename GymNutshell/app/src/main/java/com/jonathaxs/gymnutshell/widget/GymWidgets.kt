package com.jonathaxs.gymnutshell.widget

import android.content.Context
import androidx.glance.appwidget.updateAll
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Ponto único pra atualizar os widgets da tela inicial — espelha o WidgetCenter.reloadAllTimelines() do iOS.
 * Chamado quando os dados mudam (ex.: ao sair do app). Os widgets remontam o snapshot lendo os repositórios.
 */
object GymWidgets {

    /** Dispara a atualização em background (fire-and-forget). */
    fun update(context: Context) {
        val appContext = context.applicationContext
        CoroutineScope(Dispatchers.Default).launch { updateAll(appContext) }
    }

    /** Atualiza todos os widgets do app. (Metas entra aqui na fatia 6D.) */
    suspend fun updateAll(context: Context) {
        ProgressWidget().updateAll(context)
        CalendarWidget().updateAll(context)
    }
}
