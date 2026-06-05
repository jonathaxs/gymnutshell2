package com.jonathaxs.gymnutshell.core.domain

import com.jonathaxs.gymnutshell.core.data.DailyRecord
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth

/**
 * Lógica pura de bônus de sequência — porte (refatorado) do StreakBonusChecker (iOS).
 *
 * Avalia os DailyRecords e decide quais bônus deveriam existir, SEM tocar no banco nem
 * disparar notificação (essas viram responsabilidade da camada de serviço/repositório).
 * O tier vem de `percent` (via DailyAchievement.from), eliminando a dependência de AppTheme/emoji.
 *
 * Regras (idênticas ao iOS):
 * - Semanal: âncora = sábado; exige os 7 dias (domingo..sábado) presentes.
 * - Mensal: âncora = último dia do mês; exige todos os dias do mês presentes.
 * - Todos nível 4 → "level4"; todos nível 3 ou 4 → "level3"; senão, nada.
 */
object StreakBonusEvaluator {

    /** Um prêmio a ser concedido. anchorEpochDay = sábado ou último dia do mês. */
    data class Award(
        val anchorEpochDay: Long,
        val bonusType: String,
        val points: Int,
        val emoji: String,
    )

    fun evaluate(
        records: List<DailyRecord>,
        alreadyAwardedAnchors: Set<Long>,
    ): List<Award> {
        val byDay: Map<Long, DailyRecord> = records.associateBy { it.date }
        val awards = mutableListOf<Award>()

        // --- Semanal: cada sábado presente é uma âncora ---
        byDay.keys
            .map { LocalDate.ofEpochDay(it) }
            .filter { it.dayOfWeek == DayOfWeek.SATURDAY }
            .forEach { saturday ->
                val anchor = saturday.toEpochDay()
                if (anchor in alreadyAwardedAnchors) return@forEach
                val weekDays = (0L..6L).map { saturday.minusDays(6 - it) } // domingo..sábado
                val weekRecords = weekDays.mapNotNull { byDay[it.toEpochDay()] }
                if (weekRecords.size != 7) return@forEach
                awardFor(weekRecords, weekly = true, anchor)?.let { awards += it }
            }

        // --- Mensal: cada mês presente tem como âncora seu último dia ---
        byDay.keys
            .map { YearMonth.from(LocalDate.ofEpochDay(it)) }
            .toSet()
            .forEach { ym ->
                val anchor = ym.atEndOfMonth().toEpochDay()
                if (anchor in alreadyAwardedAnchors) return@forEach
                val monthRecords = (1..ym.lengthOfMonth())
                    .mapNotNull { day -> byDay[ym.atDay(day).toEpochDay()] }
                if (monthRecords.size != ym.lengthOfMonth()) return@forEach
                awardFor(monthRecords, weekly = false, anchor)?.let { awards += it }
            }

        return awards
    }

    private fun awardFor(records: List<DailyRecord>, weekly: Boolean, anchor: Long): Award? {
        val tiers = records.map { DailyAchievement.from(it.percent / 100.0) }
        val allExpert = tiers.all { it == DailyAchievement.Level4 }
        val allStrong = tiers.all { it == DailyAchievement.Level3 || it == DailyAchievement.Level4 }

        return when {
            allExpert ->
                if (weekly) Award(anchor, "weekly.level4", 800, "💀")
                else Award(anchor, "monthly.level4", 5000, "☠️")
            allStrong ->
                if (weekly) Award(anchor, "weekly.level3", 400, "🎖️")
                else Award(anchor, "monthly.level3", 2000, "🏆")
            else -> null
        }
    }
}
