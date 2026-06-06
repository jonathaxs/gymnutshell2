package com.jonathaxs.gymnutshell.ui.achievements

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.jonathaxs.gymnutshell.core.data.DailyRecordRepository
import com.jonathaxs.gymnutshell.core.data.SettingsRepository
import com.jonathaxs.gymnutshell.core.domain.AppDateFormatters
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.time.temporal.WeekFields
import java.util.Locale

/** Uma célula do calendário. */
data class CalendarDayUi(
    val epochDay: Long,
    val dayNumber: Int,
    val inMonth: Boolean,
    val emoji: String?,
    val isToday: Boolean,
    val isSelected: Boolean,
)

/** Uma linha do histórico. */
data class HistoryItemUi(
    val epochDay: Long,
    val dateLabel: String,
    val emoji: String,
    val percent: Int,
)

/** Estado da AchievementsView. */
data class AchievementsUiState(
    val monthLabel: String = "",
    val weekdays: List<String> = emptyList(),
    val days: List<CalendarDayUi> = emptyList(),
    val history: List<HistoryItemUi> = emptyList(),
    val accentArgb: Long = 0xFF007AFF,
)

/**
 * ViewModel da AchievementsView — porte (MVP) da AchievementsView (iOS).
 * Lê os DailyRecord (Room) e monta o calendário do mês + o histórico, na cor de destaque do usuário.
 */
class AchievementsViewModel(app: Application) : AndroidViewModel(app) {

    private val recordRepo = DailyRecordRepository(app.applicationContext)
    private val settingsRepo = SettingsRepository(app.applicationContext)

    private val month = MutableStateFlow(YearMonth.now())
    private val selectedDay = MutableStateFlow(LocalDate.now().toEpochDay())

    val uiState: StateFlow<AchievementsUiState> = combine(
        recordRepo.records,
        month,
        selectedDay,
        settingsRepo.accentColor,
    ) { records, ym, selected, accent ->
        val emojiByDay = records.associate { it.date to it.achievementEmoji }
        val todayEpoch = LocalDate.now().toEpochDay()
        val locale = Locale.getDefault()
        val firstDow = WeekFields.of(locale).firstDayOfWeek

        // Grade: começa no primeiro dia da semana que contém o dia 1, vai até o fim do mês.
        val firstOfMonth = ym.atDay(1)
        val shift = ((firstOfMonth.dayOfWeek.value - firstDow.value) + 7) % 7
        val gridStart = firstOfMonth.minusDays(shift.toLong())
        val lastOfMonth = ym.atEndOfMonth()

        val days = generateSequence(gridStart) { it.plusDays(1) }
            .takeWhile { !it.isAfter(lastOfMonth) }
            .map { date ->
                val epochDay = date.toEpochDay()
                CalendarDayUi(
                    epochDay = epochDay,
                    dayNumber = date.dayOfMonth,
                    inMonth = date.monthValue == ym.monthValue && date.year == ym.year,
                    emoji = emojiByDay[epochDay],
                    isToday = epochDay == todayEpoch,
                    isSelected = epochDay == selected,
                )
            }.toList()

        val weekdays = (0L..6L).map {
            firstDow.plus(it).getDisplayName(TextStyle.NARROW, locale).uppercase()
        }

        val history = records.sortedByDescending { it.date }.map { record ->
            HistoryItemUi(
                epochDay = record.date,
                dateLabel = AppDateFormatters.mediumDate(LocalDate.ofEpochDay(record.date)),
                emoji = record.achievementEmoji,
                percent = record.percent,
            )
        }

        AchievementsUiState(
            monthLabel = AppDateFormatters.monthYear(ym.atDay(1)),
            weekdays = weekdays,
            days = days,
            history = history,
            accentArgb = accent.argb,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AchievementsUiState())

    fun previousMonth() = month.update { it.minusMonths(1) }
    fun nextMonth() = month.update { it.plusMonths(1) }
    fun selectDay(epochDay: Long) { selectedDay.value = epochDay }
}
