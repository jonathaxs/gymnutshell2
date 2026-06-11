package com.jonathaxs.gymnutshell.wear

import android.content.Context
import com.google.android.gms.wearable.PutDataMapRequest
import com.google.android.gms.wearable.Wearable
import com.jonathaxs.gymnutshell.core.sync.WearSnapshotCodec
import com.jonathaxs.gymnutshell.core.sync.WearSnapshotSync
import com.jonathaxs.gymnutshell.core.sync.WearSyncContract
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

/**
 * Lado celular do sync — porte do sendSnapshot() do WatchConnectivityManager (iOS).
 * Publica o snapshot como DataItem do Data Layer: persiste, "o último vence" e é
 * entregue ao relógio mesmo que ele esteja fora de alcance agora (mesma semântica
 * do updateApplicationContext).
 */
object PhoneWearSync {

    /** Dispara o push sem bloquear (chamado no onStop, igual ao scenePhase do iOS). */
    fun push(context: Context) {
        val appContext = context.applicationContext
        CoroutineScope(Dispatchers.Default).launch { pushNow(appContext) }
    }

    suspend fun pushNow(context: Context) {
        val snapshot = WearSnapshotSync.build(context)
        val request = PutDataMapRequest.create(WearSyncContract.SNAPSHOT_PATH).apply {
            dataMap.putString(WearSyncContract.KEY_PAYLOAD, WearSnapshotCodec.encode(snapshot))
        }.asPutDataRequest().setUrgent()

        // Falha silenciosa (sem Play Services, relógio nunca pareado, etc.);
        // o próximo onStop tenta de novo — mesma postura do iOS.
        runCatching { Wearable.getDataClient(context).putDataItem(request).await() }
    }
}
