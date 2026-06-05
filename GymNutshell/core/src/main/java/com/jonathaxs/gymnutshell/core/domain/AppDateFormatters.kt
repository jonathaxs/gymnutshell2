package com.jonathaxs.gymnutshell.core.domain

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale

/**
 * Formatação de datas — porte de AppDateFormatters (iOS), agora sobre java.time.LocalDate.
 *
 * No iOS eram DateFormatter cacheados; aqui usamos DateTimeFormatter (imutável e thread-safe).
 * A `dayKey` é determinística (locale fixo en_US_POSIX → Locale.US); as demais seguem o locale.
 */
object AppDateFormatters {

    private val dayKeyFormatter: DateTimeFormatter =
        DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.US)

    /** Chave estável de dia "yyyy-MM-dd", independente de locale (ID determinístico). */
    fun dayKey(date: LocalDate): String = date.format(dayKeyFormatter)

    /** Inicial do dia da semana (1 letra) — "S", "M"… no locale do sistema. */
    fun weekdayInitial(date: LocalDate, locale: Locale = Locale.getDefault()): String =
        date.format(DateTimeFormatter.ofPattern("EEEEE", locale))

    /** Data localizada em estilo medium, ex.: "May 16, 2026" / "16 de mai. de 2026". */
    fun mediumDate(date: LocalDate, locale: Locale = Locale.getDefault()): String =
        date.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withLocale(locale))

    /** Mês e ano standalone, ex.: "May 2026" / "Maio 2026" (LLLL = standalone, capitaliza certo). */
    fun monthYear(date: LocalDate, locale: Locale = Locale.getDefault()): String =
        date.format(DateTimeFormatter.ofPattern("LLLL yyyy", locale)).capitalizeFirst()

    /**
     * Data por extenso, sem ano ("Sábado, 16 de maio"). Usa o padrão localizado ICU
     * (getBestDateTimePattern) — equivalente do setLocalizedDateFormatFromTemplate do iOS —
     * pra ordem/separadores corretos por idioma. É display: validado no dispositivo, não em teste JVM.
     */
    fun longDate(date: LocalDate, locale: Locale = Locale.getDefault()): String {
        val pattern = android.text.format.DateFormat.getBestDateTimePattern(locale, "EEEEMMMMd")
        return date.format(DateTimeFormatter.ofPattern(pattern, locale)).capitalizeFirst()
    }

    private fun String.capitalizeFirst(): String =
        if (isEmpty()) this else this[0].uppercaseChar() + substring(1)
}
