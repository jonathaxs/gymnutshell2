package com.jonathaxs.gymnutshell.wear.sync

import com.google.android.gms.wearable.DataEvent
import com.google.android.gms.wearable.DataEventBuffer
import com.google.android.gms.wearable.DataMapItem
import com.google.android.gms.wearable.WearableListenerService
import com.jonathaxs.gymnutshell.core.sync.WearSnapshotCodec
import com.jonathaxs.gymnutshell.core.sync.WearSnapshotSync
import com.jonathaxs.gymnutshell.core.sync.WearSyncContract
import com.jonathaxs.gymnutshell.wear.tile.ProgressTileService
import kotlinx.coroutines.runBlocking

/**
 * Lado relógio do sync — porte do didReceiveApplicationContext (iOS).
 * O sistema inicia este service quando chega DataItem com o prefixo /gymnutshell,
 * mesmo com o app fechado. Aplica o snapshot nos repos locais; as telas (DataStore
 * Flows) reagem sozinhas, sem invalidação manual.
 */
class WearDataListenerService : WearableListenerService() {

    override fun onDataChanged(events: DataEventBuffer) {
        for (event in events) {
            if (event.type != DataEvent.TYPE_CHANGED) continue
            if (event.dataItem.uri.path != WearSyncContract.SNAPSHOT_PATH) continue

            val raw = DataMapItem.fromDataItem(event.dataItem)
                .dataMap.getString(WearSyncContract.KEY_PAYLOAD) ?: continue
            val snapshot = WearSnapshotCodec.decode(raw) ?: continue

            // onDataChanged roda em thread de background própria do service;
            // bloquear aqui é seguro e garante a aplicação antes do service morrer.
            runBlocking { WearSnapshotSync.apply(applicationContext, snapshot) }

            // Tile reflete o estado novo (porte do reloadAllTimelines pós-snapshot do iOS).
            ProgressTileService.requestUpdate(applicationContext)
        }
    }
}
