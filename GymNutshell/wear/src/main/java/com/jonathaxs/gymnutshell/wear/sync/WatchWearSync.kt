package com.jonathaxs.gymnutshell.wear.sync

import android.content.Context
import com.google.android.gms.wearable.PutDataMapRequest
import com.google.android.gms.wearable.Wearable
import com.jonathaxs.gymnutshell.core.sync.WearSyncContract
import java.time.LocalDate

/**
 * Lado relógio do envio de deltas — porte do sendIntakeUpdate/sendHistoryDelete (iOS).
 * Cada meta vira um DataItem próprio: o Data Layer persiste e entrega quando o
 * celular estiver alcançável, com "último valor vence" por chave (não precisa de
 * fila FIFO nem do fallback sendMessage→transferUserInfo do WatchConnectivity).
 */
object WatchWearSync {

    /** Publica o valor atual de uma meta após o usuário mexer no relógio. */
    fun sendIntake(context: Context, goalKey: String, value: Int) {
        val request = PutDataMapRequest.create(WearSyncContract.intakePath(goalKey)).apply {
            dataMap.putInt(WearSyncContract.KEY_VALUE, value)
            stampCommonFields(this)
        }.asPutDataRequest().setUrgent()
        // Fire-and-forget; sem Play Services (ou nunca pareado) falha silencioso.
        runCatching { Wearable.getDataClient(context).putDataItem(request) }
    }

    /** Publica o estado do toggle de "dia de descanso". */
    fun sendRestDay(context: Context, goalKey: String, active: Boolean) {
        val request = PutDataMapRequest.create(WearSyncContract.restDayPath(goalKey)).apply {
            dataMap.putBoolean(WearSyncContract.KEY_ACTIVE, active)
            stampCommonFields(this)
        }.asPutDataRequest().setUrgent()
        runCatching { Wearable.getDataClient(context).putDataItem(request) }
    }

    /** Carimba dia (descarte de deltas velhos no celular) e horário (bytes sempre mudam). */
    private fun stampCommonFields(request: PutDataMapRequest) {
        request.dataMap.putLong(WearSyncContract.KEY_EPOCH_DAY, LocalDate.now().toEpochDay())
        request.dataMap.putLong(WearSyncContract.KEY_SENT_AT, System.currentTimeMillis())
    }
}
