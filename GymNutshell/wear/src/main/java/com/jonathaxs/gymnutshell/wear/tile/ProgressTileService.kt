package com.jonathaxs.gymnutshell.wear.tile

import android.content.Context
import androidx.concurrent.futures.CallbackToFutureAdapter
import androidx.wear.protolayout.ActionBuilders
import androidx.wear.protolayout.ColorBuilders.argb
import androidx.wear.protolayout.DeviceParametersBuilders.DeviceParameters
import androidx.wear.protolayout.DimensionBuilders.expand
import androidx.wear.protolayout.LayoutElementBuilders
import androidx.wear.protolayout.ModifiersBuilders
import androidx.wear.protolayout.ResourceBuilders
import androidx.wear.protolayout.TimelineBuilders
import androidx.wear.protolayout.material.CircularProgressIndicator
import androidx.wear.protolayout.material.ProgressIndicatorColors
import androidx.wear.protolayout.material.Text
import androidx.wear.protolayout.material.Typography
import androidx.wear.protolayout.material.layouts.EdgeContentLayout
import androidx.wear.tiles.RequestBuilders
import androidx.wear.tiles.TileBuilders
import androidx.wear.tiles.TileService
import com.google.common.util.concurrent.ListenableFuture
import com.jonathaxs.gymnutshell.core.domain.ProgressColors
import com.jonathaxs.gymnutshell.core.widget.WidgetSnapshot
import com.jonathaxs.gymnutshell.core.widget.WidgetSnapshotBuilder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Tile de progresso — porte do GymNutshellWatchWidget (complication do iOS).
 * Anel de progresso na borda + emoji do tier + percentual + pontos; toque abre o app.
 * Reusa o WidgetSnapshotBuilder do :core (mesma matemática dos widgets do celular),
 * lendo os repos locais do relógio.
 */
class ProgressTileService : TileService() {

    override fun onTileRequest(
        requestParams: RequestBuilders.TileRequest,
    ): ListenableFuture<TileBuilders.Tile> =
        CallbackToFutureAdapter.getFuture { completer ->
            CoroutineScope(Dispatchers.Default).launch {
                runCatching {
                    val snapshot = WidgetSnapshotBuilder.build(applicationContext)
                    completer.set(buildTile(snapshot, requestParams.deviceConfiguration))
                }.onFailure(completer::setException)
            }
            "ProgressTileRequest"
        }

    override fun onTileResourcesRequest(
        requestParams: RequestBuilders.ResourcesRequest,
    ): ListenableFuture<ResourceBuilders.Resources> =
        CallbackToFutureAdapter.getFuture { completer ->
            // Tile é só texto + arco: nenhum bitmap pra registrar.
            completer.set(ResourceBuilders.Resources.Builder().setVersion(RESOURCES_VERSION).build())
            "ProgressTileResources"
        }

    private fun buildTile(snapshot: WidgetSnapshot, device: DeviceParameters): TileBuilders.Tile {
        val ringArgb = ProgressColors.ringArgb(snapshot.progressNormalized).toInt()
        val trackArgb = 0x33808080.toInt()
        val grayArgb = 0xFF9E9E9E.toInt()

        // Toque em qualquer lugar do tile abre o app (igual à complication do iOS).
        val openApp = ModifiersBuilders.Clickable.Builder()
            .setId("open")
            .setOnClick(
                ActionBuilders.LaunchAction.Builder()
                    .setAndroidActivity(
                        ActionBuilders.AndroidActivity.Builder()
                            .setPackageName(packageName)
                            .setClassName("com.jonathaxs.gymnutshell.wear.MainActivity")
                            .build(),
                    )
                    .build(),
            )
            .build()

        val center = LayoutElementBuilders.Column.Builder()
            .addContent(
                Text.Builder(this, snapshot.tierEmoji)
                    .setTypography(Typography.TYPOGRAPHY_DISPLAY3)
                    .build(),
            )
            .addContent(
                Text.Builder(this, "${snapshot.progressPercent}%")
                    .setTypography(Typography.TYPOGRAPHY_TITLE2)
                    .setColor(argb(ringArgb))
                    .build(),
            )
            .addContent(
                Text.Builder(this, "${snapshot.tierPoints} pts")
                    .setTypography(Typography.TYPOGRAPHY_CAPTION2)
                    .setColor(argb(grayArgb))
                    .build(),
            )
            .build()

        val layout = EdgeContentLayout.Builder(device)
            .setEdgeContent(
                CircularProgressIndicator.Builder()
                    .setProgress(snapshot.progressNormalized.toFloat())
                    .setCircularProgressIndicatorColors(
                        ProgressIndicatorColors(argb(ringArgb), argb(trackArgb)),
                    )
                    .build(),
            )
            .setContent(center)
            .build()

        val root = LayoutElementBuilders.Box.Builder()
            .setWidth(expand())
            .setHeight(expand())
            .setModifiers(ModifiersBuilders.Modifiers.Builder().setClickable(openApp).build())
            .addContent(layout)
            .build()

        return TileBuilders.Tile.Builder()
            .setResourcesVersion(RESOURCES_VERSION)
            // Dica de cadência pro sistema; updates imediatos vêm via requestUpdate().
            .setFreshnessIntervalMillis(30L * 60 * 1000)
            .setTileTimeline(TimelineBuilders.Timeline.fromLayoutElement(root))
            .build()
    }

    companion object {
        private const val RESOURCES_VERSION = "1"

        /** Pede refresh do tile (chamado quando intake muda ou snapshot chega do celular). */
        fun requestUpdate(context: Context) {
            getUpdater(context).requestUpdate(ProgressTileService::class.java)
        }
    }
}
