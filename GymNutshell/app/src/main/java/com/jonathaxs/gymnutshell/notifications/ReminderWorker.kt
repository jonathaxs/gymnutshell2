package com.jonathaxs.gymnutshell.notifications

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.jonathaxs.gymnutshell.core.domain.NotificationKind

/**
 * Worker do WorkManager que posta um lembrete e reagenda o próximo (cadeia auto-renovável).
 * Porte do disparo de notificações de intervalo (iOS), mas reavaliando a cada disparo:
 * só posta + reencadeia enquanto [NotificationScheduler.shouldRemind] for true; senão a cadeia para.
 */
class ReminderWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val scheduler = NotificationScheduler(applicationContext)
        val notifier = GymNotifier(applicationContext)

        val kindRaw = inputData.getString(KEY_KIND)
        val customId = inputData.getLong(KEY_CUSTOM_ID, -1L)

        when {
            kindRaw != null -> {
                val kind = NotificationKind.fromRaw(kindRaw) ?: return Result.success()
                if (scheduler.shouldRemind(kind)) {
                    notifier.postReminder(kind)
                    scheduler.scheduleNext(kind) // encadeia o próximo
                }
            }
            customId >= 0 -> {
                if (scheduler.shouldRemindCustom(customId)) {
                    scheduler.customGoal(customId)?.let { notifier.postCustomReminder(it) }
                    scheduler.scheduleNextCustom(customId)
                }
            }
        }
        return Result.success()
    }

    companion object {
        const val KEY_KIND = "kind"
        const val KEY_CUSTOM_ID = "customId"
    }
}
