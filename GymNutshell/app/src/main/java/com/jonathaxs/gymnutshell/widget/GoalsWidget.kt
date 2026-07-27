package com.jonathaxs.gymnutshell.widget

import android.content.Context
import androidx.annotation.StringRes
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.ImageProvider
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
import androidx.glance.layout.ContentScale
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextAlign
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.jonathaxs.gymnutshell.R
import com.jonathaxs.gymnutshell.core.domain.AppDateFormatters
import com.jonathaxs.gymnutshell.core.domain.NotificationRoute
import com.jonathaxs.gymnutshell.core.domain.ProgressColors
import com.jonathaxs.gymnutshell.core.widget.GoalProgress
import com.jonathaxs.gymnutshell.core.widget.WidgetSnapshot
import com.jonathaxs.gymnutshell.core.widget.WidgetSnapshotBuilder
import java.time.Instant
import java.time.ZoneId

/**
 * Widget "Metas" (Glance, large) — porte do GymNutshellGoalsWidget (iOS).
 * Header com anel + % + pontos + data; abaixo, até 6 metas ativas com barra de progresso individual.
 * Tocar abre o app na tela Hoje.
 */
class GoalsWidget : GlanceAppWidget() {

    // Exact: compõe pro tamanho real do widget, pra caber o máximo de metas (Single usaria o mínimo).
    override val sizeMode = SizeMode.Exact

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val snapshot = WidgetSnapshotBuilder.build(context)
        provideContent {
            GlanceTheme {
                Content(context, snapshot)
            }
        }
    }

    @androidx.compose.runtime.Composable
    private fun Content(context: Context, snapshot: WidgetSnapshot) {
        val textColor = widgetTextColor(snapshot)
        // Cor da borda de contraste das barras sobre fundos custom/accent (null em System).
        val borderColor = widgetBorderArgb(snapshot)?.let { Color(it) }
        Column(
            modifier = GlanceModifier
                .fillMaxSize()
                .appWidgetBackground()
                .then(widgetBackgroundModifier(snapshot))
                .cornerRadius(16.dp)
                .clickable(actionStartActivity(appIntent(context, NotificationRoute.Today)))
                .padding(12.dp),
        ) {
            Header(context, snapshot, textColor)
            Spacer(GlanceModifier.height(10.dp))
            // Até 6 metas, igual ao iOS (prefix 6). Sem Spacer entre linhas: o Glance limita ~10
            // filhos diretos por container, então o espaçamento vai como padding dentro de cada linha.
            snapshot.goals.take(6).forEach { goal ->
                GoalRow(context, goal, textColor, borderColor)
            }
        }
    }

    @androidx.compose.runtime.Composable
    private fun Header(context: Context, snapshot: WidgetSnapshot, textColor: ColorProvider) {
        Row(modifier = GlanceModifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Image(
                provider = ImageProvider(
                    WidgetRing.bitmap(
                        context = context,
                        sizeDp = 68,
                        progress = snapshot.progressNormalized.toFloat(),
                        ringArgb = ProgressColors.ringArgb(snapshot.progressNormalized),
                        emoji = snapshot.tierEmoji,
                        strokeDp = 9f,
                        borderArgb = widgetBorderArgb(snapshot),
                    ),
                ),
                contentDescription = null,
                modifier = GlanceModifier.size(68.dp),
            )
            Spacer(GlanceModifier.width(12.dp))
            Column {
                Text(dateLabel(snapshot.updatedAtEpochMillis), style = TextStyle(fontSize = 11.sp, color = textColor))
                Text(
                    context.getString(snapshot.tierNameRes),
                    maxLines = 1,
                    style = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Medium, color = textColor),
                )
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        "${snapshot.progressPercent}%",
                        style = TextStyle(fontSize = 30.sp, fontWeight = FontWeight.Bold, color = textColor),
                    )
                    Text(
                        ", ${snapshot.tierPoints} pts",
                        style = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.Medium, color = textColor),
                    )
                }
            }
        }
    }

    /** Uma linha de meta: emoji + rótulo + barra + %. */
    @androidx.compose.runtime.Composable
    private fun GoalRow(context: Context, goal: GoalProgress, textColor: ColorProvider, borderColor: Color?) {
        val label = goal.label ?: context.getString(goalTitleRes(goal.key))
        val barImage = ImageProvider(
            WidgetBar.bitmap(percent = goal.percent, fillArgb = ProgressColors.ringArgb(goal.percent / 100.0)),
        )
        Row(
            modifier = GlanceModifier.fillMaxWidth().padding(vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(modifier = GlanceModifier.width(24.dp), contentAlignment = Alignment.Center) {
                Text(goal.emoji, style = TextStyle(fontSize = 14.sp))
            }
            Spacer(GlanceModifier.width(6.dp))
            Box(modifier = GlanceModifier.width(64.dp), contentAlignment = Alignment.CenterStart) {
                Text(label, maxLines = 1, style = TextStyle(fontSize = 12.sp, color = textColor))
            }
            Spacer(GlanceModifier.width(8.dp))
            if (borderColor != null) {
                // Contorno de contraste (fundo custom/accent): caixa na cor da borda com 1dp de recuo
                // revelando-a em volta da barra — mesma técnica do "hoje" no calendário.
                Box(
                    modifier = GlanceModifier
                        .defaultWeight()
                        .height(10.dp)
                        .cornerRadius(5.dp)
                        .background(ColorProvider(borderColor))
                        .padding(1.dp),
                ) {
                    Image(
                        provider = barImage,
                        contentDescription = null,
                        contentScale = ContentScale.FillBounds,
                        modifier = GlanceModifier.fillMaxSize().cornerRadius(4.dp),
                    )
                }
            } else {
                Image(
                    provider = barImage,
                    contentDescription = null,
                    contentScale = ContentScale.FillBounds,
                    modifier = GlanceModifier.defaultWeight().height(8.dp).cornerRadius(4.dp),
                )
            }
            Spacer(GlanceModifier.width(8.dp))
            Box(modifier = GlanceModifier.width(38.dp), contentAlignment = Alignment.CenterEnd) {
                Text(
                    "${goal.percent}%",
                    style = TextStyle(fontSize = 11.sp, fontWeight = FontWeight.Medium, color = textColor, textAlign = TextAlign.End),
                )
            }
        }
    }

    private fun dateLabel(epochMillis: Long): String {
        val date = Instant.ofEpochMilli(epochMillis).atZone(ZoneId.systemDefault()).toLocalDate()
        return AppDateFormatters.mediumDate(date)
    }
}

/** Mapeia a chave da meta fixa pro título localizado (mesma tabela da TodayScreen). */
@StringRes
private fun goalTitleRes(key: String): Int = when (key) {
    "tracking.workout" -> R.string.today_workout
    "tracking.cardio" -> R.string.today_cardio
    "tracking.sleep" -> R.string.today_sleep
    "tracking.water" -> R.string.today_water
    "tracking.calories" -> R.string.today_calories
    "tracking.protein" -> R.string.today_protein
    "tracking.carbs" -> R.string.today_carbs
    "tracking.goodFat" -> R.string.today_good_fat
    "tracking.fiber" -> R.string.today_fiber
    "tracking.creatine" -> R.string.today_creatine
    else -> R.string.app_name
}
