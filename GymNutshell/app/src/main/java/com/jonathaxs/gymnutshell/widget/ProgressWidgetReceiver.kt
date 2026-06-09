package com.jonathaxs.gymnutshell.widget

import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver

/** Receiver do sistema pro widget "Progresso" — liga o AppWidget ao [ProgressWidget]. */
class ProgressWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = ProgressWidget()
}
