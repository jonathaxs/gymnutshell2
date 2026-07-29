package com.jonathaxs.gymnutshell.widget

import android.content.Context
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
import androidx.glance.layout.RowScope
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
import com.jonathaxs.gymnutshell.core.domain.AppDateFormatters
import com.jonathaxs.gymnutshell.core.domain.NotificationRoute
import com.jonathaxs.gymnutshell.core.domain.ProgressColors
import com.jonathaxs.gymnutshell.core.widget.DaySummary
import com.jonathaxs.gymnutshell.core.widget.WidgetSnapshot
import com.jonathaxs.gymnutshell.core.widget.WidgetSnapshotBuilder
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.TextStyle as JavaTextStyle
import java.time.temporal.WeekFields
import java.util.Locale
import kotlin.math.ceil

/**
 * Widget "Calendário" (Glance, large) — porte do GymNutshellCalendarWidget (iOS).
 * Header com anel + % + pontos + data; abaixo, a grade do mês com cada dia colorido pela escala
 * do anel (ProgressColors). Toque no header → Hoje; toque na grade → Conquistas.
 */
class CalendarWidget : GlanceAppWidget() {

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
        val accent = Color(snapshot.accentArgb)
        // Cor da borda de contraste das células do mês sobre fundos custom/accent (null em System).
        val cellBorder = widgetBorderArgb(snapshot)?.let { Color(it) }
        val (weekdays, weeks) = buildMonth(snapshot.recentDays)

        Column(
            modifier = GlanceModifier
                .fillMaxSize()
                .appWidgetBackground()
                .then(widgetBackgroundModifier(snapshot))
                .cornerRadius(16.dp)
                .padding(12.dp),
        ) {
            // Header → Hoje
            Header(context, snapshot, textColor)

            Spacer(GlanceModifier.height(8.dp))

            // Grade do mês → Conquistas
            Column(
                modifier = GlanceModifier
                    .fillMaxSize()
                    .clickable(actionStartActivity(appIntent(context, NotificationRoute.AchievementsToday))),
            ) {
                Text(
                    monthLabel(),
                    style = TextStyle(
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = textColor,
                        textAlign = TextAlign.Center,
                    ),
                    modifier = GlanceModifier.fillMaxWidth(),
                )
                Spacer(GlanceModifier.height(4.dp))
                WeekdayRow(weekdays, textColor)
                weeks.forEach { week ->
                    Row(modifier = GlanceModifier.fillMaxWidth()) {
                        week.forEach { day -> DayCellView(day, accent, textColor, cellBorder) }
                    }
                }
            }
        }
    }

    @androidx.compose.runtime.Composable
    private fun Header(context: Context, snapshot: WidgetSnapshot, textColor: ColorProvider) {
        Row(
            modifier = GlanceModifier
                .fillMaxWidth()
                .clickable(actionStartActivity(appIntent(context, NotificationRoute.Today))),
            verticalAlignment = Alignment.CenterVertically,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Image(
                provider = ImageProvider(
                    WidgetRing.bitmap(
                        context = context,
                        sizeDp = 72,
                        progress = snapshot.progressNormalized.toFloat(),
                        ringArgb = ProgressColors.ringArgb(snapshot.progressNormalized),
                        emoji = snapshot.tierEmoji,
                        strokeDp = 10f,
                        borderArgb = widgetBorderArgb(snapshot),
                    ),
                ),
                contentDescription = null,
                modifier = GlanceModifier.size(72.dp),
            )
            Spacer(GlanceModifier.width(14.dp))
            Column {
                Text(dateLabel(snapshot.updatedAtEpochMillis), style = TextStyle(fontSize = 12.sp, color = textColor))
                Text(
                    context.getString(snapshot.tierNameRes),
                    maxLines = 1,
                    style = TextStyle(fontSize = 17.sp, fontWeight = FontWeight.Medium, color = textColor),
                )
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        "${snapshot.progressPercent}%",
                        style = TextStyle(fontSize = 32.sp, fontWeight = FontWeight.Bold, color = textColor),
                    )
                    Text(
                        ", ${snapshot.tierPoints} pts",
                        style = TextStyle(fontSize = 13.sp, fontWeight = FontWeight.Medium, color = textColor),
                    )
                }
            }
        }
    }

    @androidx.compose.runtime.Composable
    private fun WeekdayRow(weekdays: List<String>, textColor: ColorProvider) {
        Row(modifier = GlanceModifier.fillMaxWidth()) {
            weekdays.forEach { sym ->
                Box(
                    modifier = GlanceModifier.defaultWeight().height(18.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(sym, style = TextStyle(fontSize = 10.sp, fontWeight = FontWeight.Medium, color = textColor, textAlign = TextAlign.Center))
                }
            }
        }
    }

    @androidx.compose.runtime.Composable
    private fun RowScope.DayCellView(day: CalDay, accent: Color, textColor: ColorProvider, cellBorder: Color?) {
        Box(
            modifier = GlanceModifier.defaultWeight().height(30.dp).padding(2.dp),
            contentAlignment = Alignment.Center,
        ) {
            when {
                day.isToday ->
                    // "Borda" de hoje: caixa accent com 2dp de recuo revelando a cor do dia por dentro.
                    Box(modifier = GlanceModifier.fillMaxSize().cornerRadius(8.dp).background(accent).padding(2.dp)) {
                        CellInner(day, textColor)
                    }
                // Borda de contraste nas células do mês sobre fundo custom/accent (só as do mês atual,
                // pra não competir com o foco de hoje); mesma técnica de caixa+recuo, a 50% de opacidade.
                cellBorder != null && day.inMonth ->
                    Box(
                        modifier = GlanceModifier
                            .fillMaxSize()
                            .cornerRadius(7.dp)
                            .background(ColorProvider(cellBorder.copy(alpha = 0.5f)))
                            .padding(1.dp),
                    ) {
                        CellInner(day, textColor)
                    }
                else -> CellInner(day, textColor)
            }
        }
    }

    @androidx.compose.runtime.Composable
    private fun CellInner(day: CalDay, textColor: ColorProvider) {
        Box(
            modifier = GlanceModifier.fillMaxSize().cornerRadius(6.dp).background(cellBg(day)),
            contentAlignment = Alignment.Center,
        ) {
            if (day.inMonth) {
                Text(
                    day.dayNumber.toString(),
                    style = TextStyle(
                        fontSize = 11.sp,
                        fontWeight = if (day.isToday) FontWeight.Bold else FontWeight.Normal,
                        color = cellNumberColor(day, textColor),
                    ),
                )
            }
        }
    }

    // MARK: - Cores das células (mesma lógica do DayCell do iOS)

    private fun cellBg(day: CalDay): Color = when {
        !day.inMonth -> Color.Transparent
        day.isFuture -> Color(0x14808080)
        !day.hasData || day.percent == 0 -> Color(0x33808080)
        else -> Color(ProgressColors.ringArgb(day.percent / 100.0))
    }

    private fun cellNumberColor(day: CalDay, textColor: ColorProvider): ColorProvider = when {
        day.isFuture -> ColorProvider(Color(0xFF9E9E9E))
        day.hasData && day.percent > 0 -> ColorProvider(Color.White)
        else -> textColor
    }

    // MARK: - Datas

    private fun monthLabel(): String = AppDateFormatters.monthYear(YearMonth.now().atDay(1))

    private fun dateLabel(epochMillis: Long): String {
        val date = Instant.ofEpochMilli(epochMillis).atZone(ZoneId.systemDefault()).toLocalDate()
        return AppDateFormatters.mediumDate(date)
    }

    /**
     * Monta os símbolos de dia-da-semana + a grade do mês atual (semanas completas), alinhada ao
     * primeiro dia da semana do locale. Espelha a AchievementsViewModel + o calendário do iOS.
     */
    private fun buildMonth(recentDays: List<DaySummary>): Pair<List<String>, List<List<CalDay>>> {
        val locale = Locale.getDefault()
        val today = LocalDate.now()
        val ym = YearMonth.now()
        val firstDow = WeekFields.of(locale).firstDayOfWeek
        val firstOfMonth = ym.atDay(1)
        val shift = ((firstOfMonth.dayOfWeek.value - firstDow.value) + 7) % 7
        val gridStart = firstOfMonth.minusDays(shift.toLong())
        val totalCells = ceil((shift + ym.lengthOfMonth()) / 7.0).toInt() * 7
        val byDay = recentDays.associate { it.epochDay to it.percent }

        val cells = (0 until totalCells).map { i ->
            val date = gridStart.plusDays(i.toLong())
            val epoch = date.toEpochDay()
            CalDay(
                dayNumber = date.dayOfMonth,
                inMonth = date.monthValue == ym.monthValue && date.year == ym.year,
                isToday = date == today,
                isFuture = date.isAfter(today),
                hasData = byDay.containsKey(epoch),
                percent = byDay[epoch] ?: 0,
            )
        }
        val weekdays = (0L..6L).map {
            firstDow.plus(it).getDisplayName(JavaTextStyle.NARROW, locale).uppercase(locale)
        }
        return weekdays to cells.chunked(7)
    }
}

/** Modelo de uma célula da grade do widget. */
private data class CalDay(
    val dayNumber: Int,
    val inMonth: Boolean,
    val isToday: Boolean,
    val isFuture: Boolean,
    val hasData: Boolean,
    val percent: Int,
)
