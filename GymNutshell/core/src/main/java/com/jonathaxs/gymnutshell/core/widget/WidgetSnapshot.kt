package com.jonathaxs.gymnutshell.core.widget

import com.jonathaxs.gymnutshell.core.theme.AccentColor
import kotlin.math.floor

/**
 * Snapshot pré-computado do progresso do dia para os widgets (Glance) — porte de WidgetSnapshot (iOS).
 *
 * Diferença de plataforma: no iOS o snapshot é serializado num App Group porque o widget é uma
 * extensão num processo separado. No Android o widget Glance roda no MESMO processo do app e lê os
 * repositórios direto (ver [WidgetSnapshotBuilder]); então aqui o snapshot é só um modelo em memória,
 * montado sob demanda no provideGlance — não precisa persistir nem serializar.
 */
data class WidgetSnapshot(
    /** Progresso normalizado do dia (0.0–1.0): média das metas ativas (descanso conta 100%). */
    val progressNormalized: Double,
    /** Nível do tier atual (1–4). */
    val tier: Int,
    /** Emoji do tier já resolvido com o tema escolhido. */
    val tierEmoji: String,
    /** Cor de destaque do app como ARGB (0xAARRGGBB), pro fundo/anel do widget. */
    val accentArgb: Long,
    /** Momento em que o snapshot foi montado (epoch millis). */
    val updatedAtEpochMillis: Long,
    /** Últimos ~35 dias (do mais antigo ao mais recente, hoje no fim) com o % final de cada um. */
    val recentDays: List<DaySummary>,
    /** Metas ativas com progresso individual, na ordem da TodayView (fixas + personalizadas). */
    val goals: List<GoalProgress>,
) {
    /** % inteiro, arredondado pra baixo (igual TodayView). */
    val progressPercent: Int get() = floor(progressNormalized * 100).toInt()

    /** Pontos do tier atual — derivados do nível (espelha DailyAchievement.points). */
    val tierPoints: Int
        get() = when (tier) {
            2 -> 40
            3 -> 60
            4 -> 90
            else -> 0
        }

    companion object {
        /** Snapshot de exemplo pro preview/placeholder do widget, espelha o do iOS. */
        val placeholder = WidgetSnapshot(
            progressNormalized = 0.65,
            tier = 2,
            tierEmoji = "🏋️",
            accentArgb = AccentColor.Blue.argb,
            updatedAtEpochMillis = System.currentTimeMillis(),
            recentDays = emptyList(),
            goals = listOf(
                GoalProgress("tracking.workout", "🏋️", 80),
                GoalProgress("tracking.water", "💧", 60),
                GoalProgress("tracking.protein", "🍗", 40),
                GoalProgress("tracking.sleep", "💤", 100),
            ),
        )
    }
}

/** Resumo de um dia na janela recente: epoch-day + % final. */
data class DaySummary(val epochDay: Long, val percent: Int)

/**
 * Progresso de uma meta no widget.
 * `label` traz o nome de metas personalizadas; pra metas fixas é null (a UI resolve via string por [key]).
 */
data class GoalProgress(val key: String, val emoji: String, val percent: Int, val label: String? = null)
