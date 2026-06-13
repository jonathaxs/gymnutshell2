package com.jonathaxs.gymnutshell.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.jonathaxs.gymnutshell.R
import com.jonathaxs.gymnutshell.core.domain.DailyAchievement
import com.jonathaxs.gymnutshell.core.domain.ProgressColors
import com.jonathaxs.gymnutshell.ui.theme.color

// Páginas informativas da seção About — portes de ProgressRingInfoView,
// TierInfoView e StreakBonusInfoView (iOS). Sem contexto de progresso atual
// (no iOS ele só existe quando abertas como sheet a partir da Today).

/** Anel de progresso: o que ele mostra e o significado de cada cor. */
@Composable
fun ProgressRingInfoScreen(onBack: () -> Unit) {
    // Mesmas cores e faixas do ProgressColors do :core (única fonte da escala).
    val rangeSuffix = stringResource(R.string.tier_info_range_suffix)
    val colors = listOf(
        Triple(stringResource(R.string.ring_info_color_red), Color(0xFFFF3B30), "0 – 32%"),
        Triple(stringResource(R.string.ring_info_color_orange), Color(0xFFFF9500), "33 – 65%"),
        Triple(stringResource(R.string.ring_info_color_green), Color(0xFF34C759), "66 – 89%"),
        Triple(stringResource(R.string.ring_info_color_cyan), Color(0xFF32ADE6), "90 – 99%"),
        Triple(stringResource(R.string.ring_info_color_blue), Color(0xFF007AFF), "100%"),
    )

    InfoPage(title = stringResource(R.string.settings_about_progress_ring), onBack = onBack) {
        IntroText(stringResource(R.string.ring_info_intro))
        InfoSectionHeader(stringResource(R.string.ring_info_section_colors))
        colors.forEachIndexed { index, (label, swatch, range) ->
            if (index > 0) HorizontalDivider()
            InfoRow(
                leading = {
                    Box(Modifier.size(24.dp).clip(CircleShape).background(swatch))
                },
                title = label,
                subtitle = range + rangeSuffix,
            )
        }
    }
}

/** Conquistas: os 4 tiers do tema atual com emoji e faixa de progresso. */
@Composable
fun TierInfoScreen(
    onBack: () -> Unit,
    onOpenTheme: () -> Unit,
    viewModel: SettingsViewModel = viewModel(),
) {
    val theme by viewModel.theme.collectAsStateWithLifecycle()
    val accent = viewModel.accentColor.collectAsStateWithLifecycle().value.color
    val rangeSuffix = stringResource(R.string.tier_info_range_suffix)

    InfoPage(title = stringResource(R.string.settings_about_achievement), onBack = onBack) {
        IntroText(stringResource(R.string.tier_info_intro))
        InfoSectionHeader(stringResource(R.string.tier_info_section_levels))
        DailyAchievement.entries.forEachIndexed { index, tier ->
            if (index > 0) HorizontalDivider()
            InfoRow(
                leading = {
                    Text(theme.emoji(tier), style = MaterialTheme.typography.headlineSmall)
                },
                // Nomes de tier por tema ainda não existem no Android; até lá o título é "Level N".
                title = stringResource(R.string.ring_info_level_label, index + 1),
                subtitle = tierRange(tier) + rangeSuffix,
            )
        }

        Spacer(Modifier.height(16.dp))
        HorizontalDivider()
        // Atalho pra trocar o tema, como o NavigationLink do iOS.
        Text(
            stringResource(R.string.tier_info_change_theme),
            style = MaterialTheme.typography.bodyLarge,
            color = accent,
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onOpenTheme)
                .padding(horizontal = 16.dp, vertical = 16.dp),
        )
        HorizontalDivider()
    }
}

/** Bônus de sequência: os 4 tipos com condição de desbloqueio e pontos. */
@Composable
fun StreakBonusInfoScreen(onBack: () -> Unit) {
    // Emojis e pontos idênticos ao StreakBonusEvaluator do :core.
    val bonuses = listOf(
        BonusRow("🎖️", R.string.streak_bonus_weekly_l3_title, R.string.streak_bonus_weekly_l3_desc, 400),
        BonusRow("💀", R.string.streak_bonus_weekly_l4_title, R.string.streak_bonus_weekly_l4_desc, 800),
        BonusRow("🏆", R.string.streak_bonus_monthly_l3_title, R.string.streak_bonus_monthly_l3_desc, 2000),
        BonusRow("☠️", R.string.streak_bonus_monthly_l4_title, R.string.streak_bonus_monthly_l4_desc, 5000),
    )

    InfoPage(title = stringResource(R.string.settings_about_streak_bonus), onBack = onBack) {
        IntroText(stringResource(R.string.streak_bonus_info_intro))
        InfoSectionHeader(stringResource(R.string.settings_about_streak_bonus))
        bonuses.forEachIndexed { index, bonus ->
            if (index > 0) HorizontalDivider()
            InfoRow(
                leading = { Text(bonus.emoji, style = MaterialTheme.typography.headlineSmall) },
                title = stringResource(bonus.titleRes),
                subtitle = stringResource(bonus.descRes),
                trailing = stringResource(R.string.streak_bonus_points_format, bonus.points),
            )
        }
    }
}

private data class BonusRow(val emoji: String, val titleRes: Int, val descRes: Int, val points: Int)

/** Faixa de porcentagem de cada tier, igual ao rangeLabel do iOS. */
private fun tierRange(tier: DailyAchievement): String = when (tier) {
    DailyAchievement.Level1 -> "0 – 32%"
    DailyAchievement.Level2 -> "33 – 65%"
    DailyAchievement.Level3 -> "66 – 89%"
    DailyAchievement.Level4 -> "90 – 100%"
}

// ---- Blocos compartilhados pelas três páginas ----

/** Scaffold + coluna scrollável padrão das páginas informativas. */
@Composable
private fun InfoPage(title: String, onBack: () -> Unit, content: @Composable () -> Unit) {
    Scaffold(topBar = { SettingsTopBar(title, onBack) }) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState()),
        ) {
            content()
        }
    }
}

/** Parágrafo introdutório em cor secundária, igual à primeira Section do iOS. */
@Composable
private fun IntroText(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.bodyLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp),
    )
}

/** Header de seção das páginas informativas. */
@Composable
private fun InfoSectionHeader(title: String) {
    Text(
        title,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 4.dp),
    )
}

/** Linha: indicador à esquerda, título + subtítulo, texto opcional à direita. */
@Composable
private fun InfoRow(
    leading: @Composable () -> Unit,
    title: String,
    subtitle: String,
    trailing: String? = null,
) {
    Row(
        Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.width(40.dp), contentAlignment = Alignment.Center) { leading() }
        Spacer(Modifier.width(8.dp))
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
            Text(
                subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        if (trailing != null) {
            Spacer(Modifier.width(8.dp))
            Text(
                trailing,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
