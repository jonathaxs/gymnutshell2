package com.jonathaxs.gymnutshell.widget

import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver

/** Receiver do sistema pro widget "Metas" — liga o AppWidget ao [GoalsWidget]. */
class GoalsWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = GoalsWidget()
}
