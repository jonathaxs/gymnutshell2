package com.jonathaxs.gymnutshell.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
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
import com.jonathaxs.gymnutshell.ui.components.GroupInset
import com.jonathaxs.gymnutshell.ui.components.GroupRow
import com.jonathaxs.gymnutshell.ui.components.GroupRowDivider
import com.jonathaxs.gymnutshell.ui.components.GroupSection
import com.jonathaxs.gymnutshell.ui.theme.color
import kotlinx.coroutines.launch

// Páginas informativas da seção About — portes de ProgressRingInfoView,
// TierInfoView e StreakBonusInfoView (iOS). Sem contexto de progresso atual
// (no iOS ele só existe quando abertas como sheet a partir da Today).

/** Anel de progresso: o que ele mostra e o significado de cada cor. */
@Composable
fun ProgressRingInfoScreen(onBack: () -> Unit) {
    InfoPage(title = stringResource(R.string.settings_about_progress_ring), onBack = onBack) {
        ProgressRingInfoContent()
    }
}

/** Conteúdo da info do anel (sem chrome), reutilizado pela página (Settings) e pelo sheet (Today). */
@Composable
fun ProgressRingInfoContent() {
    // Mesmas cores e faixas do ProgressColors do :core (única fonte da escala).
    val rangeSuffix = stringResource(R.string.tier_info_range_suffix)
    val colors = listOf(
        Triple(stringResource(R.string.ring_info_color_red), Color(0xFFFF3B30), "0 – 32%"),
        Triple(stringResource(R.string.ring_info_color_orange), Color(0xFFFF9500), "33 – 65%"),
        Triple(stringResource(R.string.ring_info_color_green), Color(0xFF34C759), "66 – 89%"),
        Triple(stringResource(R.string.ring_info_color_cyan), Color(0xFF32ADE6), "90 – 99%"),
        Triple(stringResource(R.string.ring_info_color_blue), Color(0xFF007AFF), "100%"),
    )
    IntroText(stringResource(R.string.ring_info_intro))
    GroupSection(title = stringResource(R.string.ring_info_section_colors)) {
        colors.forEachIndexed { index, (label, swatch, range) ->
            if (index > 0) GroupRowDivider()
            GroupRow(
                title = label,
                subtitle = range + rangeSuffix,
                leading = { Box(Modifier.size(24.dp).clip(CircleShape).background(swatch)) },
            )
        }
    }
}

/** Conquistas: os 4 tiers do tema atual com emoji e faixa de progresso. */
@Composable
fun TierInfoScreen(
    onBack: () -> Unit,
    onOpenTheme: () -> Unit,
) {
    InfoPage(title = stringResource(R.string.settings_about_achievement), onBack = onBack) {
        TierInfoContent(onOpenTheme = onOpenTheme)
    }
}

/** Conteúdo da info de conquistas (sem chrome), reutilizado pela página (Settings) e pelo sheet (Today). */
@Composable
fun TierInfoContent(
    onOpenTheme: () -> Unit,
    viewModel: SettingsViewModel = viewModel(),
) {
    val theme by viewModel.theme.collectAsStateWithLifecycle()
    val sex = viewModel.profile.collectAsStateWithLifecycle().value.sex
    val accent = viewModel.accentColor.collectAsStateWithLifecycle().value.color
    val rangeSuffix = stringResource(R.string.tier_info_range_suffix)

    IntroText(stringResource(R.string.tier_info_intro))
    GroupSection(title = stringResource(R.string.tier_info_section_levels)) {
        DailyAchievement.entries.forEachIndexed { index, tier ->
            if (index > 0) GroupRowDivider()
            GroupRow(
                title = stringResource(theme.tierNameRes(tier, sex)),
                subtitle = tierRange(tier) + rangeSuffix,
                leading = { Text(theme.emoji(tier, sex), style = MaterialTheme.typography.headlineSmall) },
            )
        }
    }

    // Atalho pra trocar o tema, como o NavigationLink do iOS.
    GroupSection {
        GroupRow(
            title = stringResource(R.string.tier_info_change_theme),
            titleColor = accent,
            showChevron = true,
            onClick = onOpenTheme,
        )
    }
}

/** Bônus de sequência: os 4 tipos com condição de desbloqueio e pontos. */
@Composable
fun StreakBonusInfoScreen(onBack: () -> Unit) {
    InfoPage(title = stringResource(R.string.settings_about_streak_bonus), onBack = onBack) {
        StreakBonusInfoContent()
    }
}

/** Conteúdo da info de bônus (sem chrome), reutilizado pela página (Settings) e pelo sheet (Progress). */
@Composable
fun StreakBonusInfoContent() {
    // Emojis e pontos idênticos ao StreakBonusEvaluator do :core.
    val bonuses = listOf(
        BonusRow("🎖️", R.string.streak_bonus_weekly_l3_title, R.string.streak_bonus_weekly_l3_desc, 400),
        BonusRow("💀", R.string.streak_bonus_weekly_l4_title, R.string.streak_bonus_weekly_l4_desc, 800),
        BonusRow("🏆", R.string.streak_bonus_monthly_l3_title, R.string.streak_bonus_monthly_l3_desc, 2000),
        BonusRow("☠️", R.string.streak_bonus_monthly_l4_title, R.string.streak_bonus_monthly_l4_desc, 5000),
    )

    IntroText(stringResource(R.string.streak_bonus_info_intro))
    GroupSection(title = stringResource(R.string.settings_about_streak_bonus)) {
        bonuses.forEachIndexed { index, bonus ->
            if (index > 0) GroupRowDivider()
            GroupRow(
                title = stringResource(bonus.titleRes),
                subtitle = stringResource(bonus.descRes),
                leading = { Text(bonus.emoji, style = MaterialTheme.typography.headlineSmall) },
                trailing = {
                    Text(
                        stringResource(R.string.streak_bonus_points_format, bonus.points),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                },
            )
        }
    }
}

// ---- Bottom sheets das infos, abertos ao tocar no anel / conquista da Today (porte das sheets do iOS) ----

/** Sheet com a info do anel de progresso (no iOS, ProgressRingInfoView aberta como sheet). */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProgressRingInfoSheet(onDismiss: () -> Unit) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        InfoSheetTitle(stringResource(R.string.settings_about_progress_ring))
        Column(
            Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(bottom = 24.dp),
        ) {
            ProgressRingInfoContent()
        }
    }
}

/** Sheet com a info de bônus de sequência (aberto ao tocar nos bônus da Progress). */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StreakBonusInfoSheet(onDismiss: () -> Unit) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        InfoSheetTitle(stringResource(R.string.settings_about_streak_bonus))
        Column(
            Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(bottom = 24.dp),
        ) {
            StreakBonusInfoContent()
        }
    }
}

/** Sheet com a info de conquistas (no iOS, TierInfoView aberta como sheet). */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TierInfoSheet(onDismiss: () -> Unit, onOpenTheme: () -> Unit) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()
    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        InfoSheetTitle(stringResource(R.string.settings_about_achievement))
        Column(
            Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(bottom = 24.dp),
        ) {
            TierInfoContent(onOpenTheme = {
                // Fecha o sheet animando e só então navega pra tela de tema.
                scope.launch { sheetState.hide() }.invokeOnCompletion {
                    if (!sheetState.isVisible) {
                        onDismiss()
                        onOpenTheme()
                    }
                }
            })
        }
    }
}

/** Título do bottom sheet de info. */
@Composable
internal fun InfoSheetTitle(title: String) {
    Text(
        title,
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(start = 24.dp, end = 24.dp, top = 4.dp, bottom = 8.dp),
    )
}

private data class BonusRow(val emoji: String, val titleRes: Int, val descRes: Int, val points: Int)

/** Faixa de porcentagem de cada tier, igual ao rangeLabel do iOS. */
internal fun tierRange(tier: DailyAchievement): String = when (tier) {
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
            Spacer(Modifier.height(24.dp))
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
        modifier = Modifier.padding(horizontal = GroupInset + 4.dp, vertical = 16.dp),
    )
}
