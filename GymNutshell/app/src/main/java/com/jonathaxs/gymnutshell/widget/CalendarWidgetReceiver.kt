package com.jonathaxs.gymnutshell.widget

import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver

/** Receiver do sistema pro widget "Calendário" — liga o AppWidget ao [CalendarWidget]. */
class CalendarWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = CalendarWidget()
}
