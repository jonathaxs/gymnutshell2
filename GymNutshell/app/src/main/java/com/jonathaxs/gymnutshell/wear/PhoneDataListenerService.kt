package com.jonathaxs.gymnutshell.wear

import com.google.android.gms.wearable.DataEvent
import com.google.android.gms.wearable.DataEventBuffer
import com.google.android.gms.wearable.DataMapItem
import com.google.android.gms.wearable.WearableListenerService
import com.jonathaxs.gymnutshell.core.data.IntakeRepository
import com.jonathaxs.gymnutshell.core.data.NotificationHistoryRepository
import com.jonathaxs.gymnutshell.core.domain.NotificationKind
import com.jonathaxs.gymnutshell.core.sync.WearSyncContract
import com.jonathaxs.gymnutshell.notifications.NotificationScheduler
import com.jonathaxs.gymnutshell.widget.GymWidgets
import kotlinx.coroutines.runBlocking
import java.time.LocalDate

/**
 * Lado celular da recepção de deltas — porte do applyIntakeUpdate (iOS).
 * Recebe os DataItems de intake/descanso publicados pelo relógio (mesmo com o app
 * fechado), aplica nos repos, re-arma os lembretes da meta e atualiza os widgets.
 *
 * Sem risco do ping-pong que o iOS suprimia com `suppressObservation`: aqui nada
 * observa o DataStore pra reenviar — o celular só publica snapshot no onStop.
 */
class PhoneDataListenerService : WearableListenerService() {

    override fun onDataChanged(events: DataEventBuffer) {
        val today = LocalDate.now().toEpochDay()
        var applied = false

        for (event in events) {
            if (event.type != DataEvent.TYPE_CHANGED) continue
            val path = event.dataItem.uri.path ?: continue
            val dataMap = DataMapItem.fromDataItem(event.dataItem).dataMap

            // Exclusão de histórico não tem carimbo de dia (apagar entrada velha é sempre válido).
            val deleteId = WearSyncContract.goalKeyFromPath(path, WearSyncContract.HISTDELETE_PATH_PREFIX)
            if (deleteId != null) {
                runBlocking { NotificationHistoryRepository(applicationContext).delete(deleteId) }
                continue
            }

            // Deltas de outro dia (relógio ficou offline) são descartados:
            // aplicar o valor de ontem em cima do dia de hoje corromperia o registro.
            if (dataMap.getLong(WearSyncContract.KEY_EPOCH_DAY) != today) continue

            val intakeKey = WearSyncContract.goalKeyFromPath(path, WearSyncContract.INTAKE_PATH_PREFIX)
            val restDayKey = WearSyncContract.goalKeyFromPath(path, WearSyncContract.RESTDAY_PATH_PREFIX)
            when {
                intakeKey != null -> runBlocking {
                    IntakeRepository(applicationContext)
                        .setIntake(intakeKey, dataMap.getInt(WearSyncContract.KEY_VALUE))
                    rescheduleReminder(intakeKey)
                    applied = true
                }
                restDayKey != null -> runBlocking {
                    IntakeRepository(applicationContext)
                        .setRestDay(restDayKey, dataMap.getBoolean(WearSyncContract.KEY_ACTIVE))
                    rescheduleReminder(restDayKey)
                    applied = true
                }
            }
        }

        // Widgets da home refletem na hora o que foi feito no relógio.
        if (applied) GymWidgets.update(applicationContext)
    }

    /** Re-arma (ou cancela) o lembrete da meta — mesma regra da TodayViewModel. */
    private suspend fun rescheduleReminder(goalKey: String) {
        val scheduler = NotificationScheduler(applicationContext)
        val kind = NotificationKind.fromTrackingKey(goalKey)
        if (kind != null) {
            scheduler.reschedule(kind)
        } else if (goalKey.startsWith("custom:")) {
            goalKey.removePrefix("custom:").toLongOrNull()?.let { scheduler.rescheduleCustom(it) }
        }
    }
}
