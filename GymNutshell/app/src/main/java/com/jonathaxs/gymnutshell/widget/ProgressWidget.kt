package com.jonathaxs.gymnutshell.widget

import android.content.Context
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalSize
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.appWidgetBackground
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.jonathaxs.gymnutshell.core.domain.AppDateFormatters
import com.jonathaxs.gymnutshell.core.domain.NotificationRoute
import com.jonathaxs.gymnutshell.core.domain.ProgressColors
import com.jonathaxs.gymnutshell.core.widget.WidgetSnapshot
import com.jonathaxs.gymnutshell.core.widget.WidgetSnapshotBuilder
import java.time.Instant
import java.time.ZoneId

/**
 * Widget "Progresso" (Glance) — porte do GymNutshellWidget (iOS): mostra o progresso do dia e o tier.
 * Pequeno (≈2x2): emoji + % + pontos. Médio (≈4x2): anel com emoji + % + pontos + data.
 * Tocar abre o app na tela Hoje (reusa o deep link de EXTRA_ROUTE).
 */
class ProgressWidget : GlanceAppWidget() {

    // Exact: o conteúdo recebe o tamanho real (LocalSize) e escolhe o layout pequeno/médio.
    override val sizeMode = SizeMode.Exact

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        // Monta o snapshot lendo os repositórios direto (mesmo processo do app).
        val snapshot = WidgetSnapshotBuilder.build(context)
        provideContent {
            GlanceTheme {
                Content(context, snapshot)
            }
        }
    }

    @androidx.compose.runtime.Composable
    private fun Content(context: Context, snapshot: WidgetSnapshot) {
        val size = LocalSize.current
        val textColor = GlanceTheme.colors.onSurface
        Box(
            modifier = GlanceModifier
                .fillMaxSize()
                .appWidgetBackground()
                .background(GlanceTheme.colors.widgetBackground)
                .cornerRadius(16.dp)
                .clickable(actionStartActivity(appIntent(context, NotificationRoute.Today)))
                .padding(12.dp),
            contentAlignment = Alignment.Center,
        ) {
            if (size.width < 200.dp) {
                SmallLayout(snapshot, textColor)
            } else {
                MediumLayout(context, snapshot, textColor)
            }
        }
    }

    /** Pequeno: emoji grande + % + pontos (sem anel, igual ao systemSmall do iOS). */
    @androidx.compose.runtime.Composable
    private fun SmallLayout(snapshot: WidgetSnapshot, textColor: ColorProvider) {
        Column(
            modifier = GlanceModifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(snapshot.tierEmoji, style = TextStyle(fontSize = 44.sp))
            Spacer(GlanceModifier.height(6.dp))
            Text(
                "${snapshot.progressPercent}%",
                style = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.Bold, color = textColor),
            )
            Text("${snapshot.tierPoints} pts", style = TextStyle(fontSize = 12.sp, color = textColor))
        }
    }

    /** Médio: anel (com emoji) à esquerda + % / pontos / data à direita. */
    @androidx.compose.runtime.Composable
    private fun MediumLayout(context: Context, snapshot: WidgetSnapshot, textColor: ColorProvider) {
        Row(
            modifier = GlanceModifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Image(
                provider = ImageProvider(
                    WidgetRing.bitmap(
                        context = context,
                        sizeDp = 76,
                        progress = snapshot.progressNormalized.toFloat(),
                        ringArgb = ProgressColors.ringArgb(snapshot.progressNormalized),
                        emoji = snapshot.tierEmoji,
                    ),
                ),
                contentDescription = null,
                modifier = GlanceModifier.size(76.dp),
            )
            Spacer(GlanceModifier.width(16.dp))
            Column(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "${snapshot.progressPercent}%",
                    style = TextStyle(fontSize = 30.sp, fontWeight = FontWeight.Bold, color = textColor),
                )
                Text(
                    "${snapshot.tierPoints} pts",
                    style = TextStyle(fontSize = 13.sp, fontWeight = FontWeight.Medium, color = textColor),
                )
                Text(dateLabel(snapshot.updatedAtEpochMillis), style = TextStyle(fontSize = 11.sp, color = textColor))
            }
        }
    }

    /** Data do snapshot formatada (padrão médio do app). */
    private fun dateLabel(epochMillis: Long): String {
        val date = Instant.ofEpochMilli(epochMillis).atZone(ZoneId.systemDefault()).toLocalDate()
        return AppDateFormatters.mediumDate(date)
    }
}
