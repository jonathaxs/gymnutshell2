package com.jonathaxs.gymnutshell.widget

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.jonathaxs.gymnutshell.MainActivity
import com.jonathaxs.gymnutshell.core.domain.NotificationRoute
import com.jonathaxs.gymnutshell.notifications.GymNotifier

/**
 * Intent que abre a MainActivity numa rota (mesmo mecanismo de deep link das notificações).
 *
 * A `data` única por rota é importante: o PendingIntent dedup ignora extras (filterEquals),
 * então dois toques que só diferem no extra colidiriam. A Uri distinta por rota mantém os
 * destinos separados num mesmo widget (ex.: header→Hoje e grade→Conquistas no calendário).
 */
internal fun appIntent(context: Context, route: NotificationRoute): Intent =
    Intent(context, MainActivity::class.java).apply {
        action = Intent.ACTION_VIEW
        data = Uri.parse("gymnutshell://${route.rawValue}")
        putExtra(GymNotifier.EXTRA_ROUTE, route.rawValue)
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
    }
