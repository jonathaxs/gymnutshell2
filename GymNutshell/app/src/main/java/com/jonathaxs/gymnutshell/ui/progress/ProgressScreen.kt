package com.jonathaxs.gymnutshell.ui.progress

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.jonathaxs.gymnutshell.R
import com.jonathaxs.gymnutshell.core.domain.AppDateFormatters
import com.jonathaxs.gymnutshell.core.domain.MeasurementSystem
import com.jonathaxs.gymnutshell.core.domain.UnitConverter
import com.jonathaxs.gymnutshell.core.domain.UserGoal
import com.jonathaxs.gymnutshell.ui.settings.StreakBonusInfoSheet
import com.jonathaxs.gymnutshell.ui.settings.TierInfoSheet
import com.jonathaxs.gymnutshell.ui.util.Breakpoints
import java.time.LocalDate

/**
 * Aba Progress — porte da ProgressOverView (iOS): resumo, distribuição por tier, bônus de sequência,
 * atividade, metas ativas, objetivo fitness, dados físicos e últimos 7 dias. Tiers e bônus abrem
 * a sheet de info correspondente (igual aos toques do iOS).
 */
@Composable
fun ProgressScreen(
    modifier: Modifier = Modifier,
    onOpenTheme: () -> Unit = {},
    onOpenGoals: () -> Unit = {},
    onOpenUserGoal: () -> Unit = {},
    onOpenPhysical: () -> Unit = {},
    onOpenAchievements: () -> Unit = {},
    viewModel: ProgressViewModel = viewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val accent = Color(state.accentArgb)
    var showTierInfo by remember { mutableStateOf(false) }
    var showBonusInfo by remember { mutableStateOf(false) }
    // Atividade → Conquistas no modo Lista; tocar num dos últimos 7 dias → Conquistas no calendário naquele dia (iOS).
    val onActivityClick = {
        viewModel.prepareAchievementsList()
        onOpenAchievements()
    }
    val onRecentDayClick: (Long) -> Unit = { day ->
        viewModel.prepareAchievementsDay(day)
        onOpenAchievements()
    }

    BoxWithConstraints(modifier.fillMaxSize()) {
        // Tela larga (tablet/landscape): grid de 2 colunas, capado em 860dp e centralizado, igual ao iOS.
        val isWide = maxWidth >= Breakpoints.WideThreshold
        if (isWide) {
            Column(
                Modifier
                    .fillMaxWidth().widthIn(max = 860.dp).fillMaxHeight().align(Alignment.TopCenter)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                SummaryRow(state, onBonusClick = { showBonusInfo = true })
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    // Coluna esquerda: conquistas → atividade → metas ativas.
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        TiersCard(state, accent) { showTierInfo = true }
                        ActivityCard(state, accent, onActivityClick)
                        GoalsCard(state, accent, onOpenGoals)
                    }
                    // Coluna direita: bônus de sequência → dados físicos.
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        BonusesCard(state, accent) { showBonusInfo = true }
                        state.physical?.let { PhysicalCard(it, accent, onOpenPhysical) }
                    }
                }
                UserGoalCard(state, accent, onOpenUserGoal)
                RecentActivityCard(state, accent, onRecentDayClick)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                item { SummaryRow(state, onBonusClick = { showBonusInfo = true }) }
                item { TiersCard(state, accent) { showTierInfo = true } }
                item { BonusesCard(state, accent) { showBonusInfo = true } }
                item { ActivityCard(state, accent, onActivityClick) }
                item { GoalsCard(state, accent, onOpenGoals) }
                item { UserGoalCard(state, accent, onOpenUserGoal) }
                state.physical?.let { physical -> item { PhysicalCard(physical, accent, onOpenPhysical) } }
                item { RecentActivityCard(state, accent, onRecentDayClick) }
            }
        }
    }

    if (showTierInfo) {
        TierInfoSheet(onDismiss = { showTierInfo = false }, onOpenTheme = onOpenTheme)
    }
    if (showBonusInfo) {
        StreakBonusInfoSheet(onDismiss = { showBonusInfo = false })
    }
}

/** Card "Conquistas": distribuição de dias por tier; abre a sheet de tiers ao tocar. */
@Composable
private fun TiersCard(state: ProgressUiState, accent: Color, onShowTierInfo: () -> Unit) {
    StatCard(
        stringResource(R.string.progress_section_tiers),
        accent,
        onClick = onShowTierInfo,
        onClickHint = stringResource(R.string.a11y_stats_card_tiers_hint),
    ) {
        state.tierCounts.forEachIndexed { index, tier ->
            if (index > 0) AccentDivider(accent, inset = true)
            StatRow(
                leading = tier.emoji,
                label = stringResource(R.string.progress_tier_level, tier.level),
                value = stringResource(R.string.progress_days_count, tier.days),
            )
        }
    }
}

/** Card "Bônus de sequência": contagens semanais/mensais; abre a sheet de bônus ao tocar. */
@Composable
private fun BonusesCard(state: ProgressUiState, accent: Color, onShowBonusInfo: () -> Unit) {
    StatCard(
        stringResource(R.string.progress_section_bonuses),
        accent,
        onClick = onShowBonusInfo,
        onClickHint = stringResource(R.string.a11y_stats_card_bonuses_hint),
    ) {
        BonusLine(R.string.progress_bonus_weekly_strong, state.weeklyStrong, accent, first = true)
        BonusLine(R.string.progress_bonus_weekly_expert, state.weeklyExpert, accent)
        BonusLine(R.string.progress_bonus_monthly_strong, state.monthlyStrong, accent)
        BonusLine(R.string.progress_bonus_monthly_expert, state.monthlyExpert, accent)
    }
}

/** Card "Atividade": dias de treino e cardio; abre Conquistas no modo Lista ao tocar. */
@Composable
private fun ActivityCard(state: ProgressUiState, accent: Color, onClick: () -> Unit) {
    StatCard(
        stringResource(R.string.progress_section_activity),
        accent,
        onClick = onClick,
        onClickHint = stringResource(R.string.a11y_stats_card_activity_hint),
    ) {
        StatRow("🏋️", stringResource(R.string.progress_activity_workout),
            stringResource(R.string.progress_days_count, state.workoutDays))
        AccentDivider(accent, inset = true)
        StatRow("🏃", stringResource(R.string.progress_activity_cardio),
            stringResource(R.string.progress_days_count, state.cardioDays))
    }
}

/** Card "Metas ativas": quantas metas estão sendo rastreadas; abre Metas nos Ajustes ao tocar. */
@Composable
private fun GoalsCard(state: ProgressUiState, accent: Color, onClick: () -> Unit) {
    StatCard(
        stringResource(R.string.progress_section_goals),
        accent,
        onClick = onClick,
        onClickHint = stringResource(R.string.a11y_stats_card_goals_hint),
    ) {
        StatRow(
            leading = "✅",
            label = stringResource(R.string.progress_goals_active_label),
            value = stringResource(R.string.progress_goals_active, state.activeGoals),
        )
    }
}

/** Card "Objetivo fitness": o objetivo atual (bulking/manutenção/cutting); abre a tela de Objetivo ao tocar. */
@Composable
private fun UserGoalCard(state: ProgressUiState, accent: Color, onClick: () -> Unit) {
    StatCard(
        stringResource(R.string.progress_section_goal),
        accent,
        onClick = onClick,
        onClickHint = stringResource(R.string.a11y_stats_card_usergoal_hint),
    ) {
        StatRow(leading = null, label = stringResource(userGoalLabelRes(state.userGoal)), value = "")
    }
}

/** Linha de 3 cards-resumo em pílula: dias, pontos, bônus. */
@Composable
private fun SummaryRow(state: ProgressUiState, onBonusClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        SummaryStat(state.totalDays.toString(), R.string.progress_days, Modifier.weight(1f))
        SummaryStat(state.totalPoints.toString(), R.string.progress_points, Modifier.weight(1f))
        SummaryStat(
            state.bonusCount.toString(), R.string.progress_bonuses, Modifier.weight(1f),
            onClick = onBonusClick,
            onClickHint = stringResource(R.string.a11y_stats_card_bonuses_hint),
        )
    }
}

/** Card-resumo em pílula (cantos totalmente arredondados, como o iOS). */
@Composable
private fun SummaryStat(
    value: String,
    @StringRes labelRes: Int,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    onClickHint: String? = null,
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(percent = 50))
            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
            .then(
                if (onClick != null) {
                    Modifier.clickable(onClickLabel = onClickHint, role = Role.Button, onClick = onClick)
                } else {
                    // Sem clique não há fusão automática: junta número e rótulo num só foco ("12, Days").
                    Modifier.semantics(mergeDescendants = true) {}
                },
            )
            .padding(vertical = 14.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Text(
            stringResource(labelRes),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}

/**
 * Card de estatísticas com título em cor secundária e divisória na cor de destaque — porte do
 * StatisticsCard do iOS. Quando `onClick` != null, o card inteiro vira botão (abre a sheet de info)
 * e `onClickHint` descreve a ação pro TalkBack (porte do accessibilityHint do iOS).
 */
@Composable
private fun StatCard(
    title: String,
    accent: Color,
    onClick: (() -> Unit)? = null,
    onClickHint: String? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
            .then(
                if (onClick != null) {
                    Modifier.clickable(onClickLabel = onClickHint, role = Role.Button, onClick = onClick)
                } else {
                    Modifier
                },
            ),
    ) {
        Text(
            title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 14.dp, bottom = 8.dp),
        )
        AccentDivider(accent, inset = false)
        content()
    }
}

/** Divisória fina na cor de destaque; `inset` recua à esquerda (entre linhas) ou ocupa tudo (sob o título). */
@Composable
private fun AccentDivider(accent: Color, inset: Boolean) {
    Box(
        Modifier
            .fillMaxWidth()
            .padding(start = if (inset) 16.dp else 0.dp)
            .height(1.dp)
            .background(accent.copy(alpha = 0.25f)),
    )
}

/** Linha genérica de um card: emoji opcional + rótulo (esquerda) e valor (direita). */
@Composable
private fun StatRow(leading: String?, label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (leading != null) {
            // Emoji decorativo: o rótulo ao lado já nomeia a linha ("🏋️" + "Workout").
            Text(leading, style = MaterialTheme.typography.titleMedium, modifier = Modifier.clearAndSetSemantics {})
            Spacer(Modifier.width(12.dp))
        }
        Text(label, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyLarge)
        if (value.isNotEmpty()) {
            Text(
                value,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.End,
            )
        }
    }
}

/** Linha de bônus: rótulo (já com emoji) + "N times"; divisória de destaque entre elas. */
@Composable
private fun BonusLine(@StringRes labelRes: Int, count: Int, accent: Color, first: Boolean = false) {
    if (!first) AccentDivider(accent, inset = true)
    StatRow(
        leading = null,
        label = stringResource(labelRes),
        value = stringResource(R.string.progress_bonus_times, count),
    )
}

/** Card de dados físicos (altura, peso, idade, sexo) nas unidades preferidas; abre Seus dados nos Ajustes ao tocar. */
@Composable
private fun PhysicalCard(physical: PhysicalUi, accent: Color, onClick: () -> Unit) {
    val hint = stringResource(R.string.a11y_stats_card_physical_hint)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
            .clickable(onClickLabel = hint, role = Role.Button, onClick = onClick),
    ) {
        Text(
            stringResource(R.string.progress_section_physical),
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 14.dp, bottom = 8.dp),
        )
        AccentDivider(accent, inset = false)

        // Resolve as strings no contexto @Composable antes de montar a lista (buildList não é composable).
        val heightLabel = stringResource(R.string.progress_physical_height)
        val weightLabel = stringResource(R.string.progress_physical_weight)
        val ageLabel = stringResource(R.string.progress_physical_age)
        val sexLabel = stringResource(R.string.field_sex)
        val sexValue = sexLabelRes(physical.sexRaw)?.let { stringResource(it) }
        val rows = buildList {
            if (physical.heightCm > 0) add(heightLabel to heightDisplay(physical))
            if (physical.weightKg > 0) add(weightLabel to weightDisplay(physical))
            if (physical.age > 0) add(ageLabel to physical.age.toString())
            if (sexValue != null) add(sexLabel to sexValue)
        }
        rows.forEachIndexed { index, (label, value) ->
            if (index > 0) AccentDivider(accent, inset = true)
            StatRow(leading = null, label = label, value = value)
        }
    }
}

/** Grade "Últimos 7 dias": emoji do tier (ou ponto) + número do dia + inicial do dia da semana. */
@Composable
private fun RecentActivityCard(state: ProgressUiState, accent: Color, onDayClick: (Long) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
            .padding(16.dp),
    ) {
        Text(
            stringResource(R.string.progress_section_recent),
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(10.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            state.recentDays.forEach { day ->
                RecentDayCell(day, accent, Modifier.weight(1f)) { onDayClick(day.epochDay) }
            }
        }
    }
}

/** Uma célula da grade dos últimos 7 dias; abre Conquistas no calendário naquele dia ao tocar. */
@Composable
private fun RecentDayCell(day: RecentDayUi, accent: Color, modifier: Modifier, onClick: () -> Unit) {
    // Descrição: data por extenso + se houve conquista no dia (porte do A11y "recent day" do iOS).
    val dateText = AppDateFormatters.longDate(LocalDate.ofEpochDay(day.epochDay))
    val desc = if (day.emoji != null) {
        stringResource(R.string.a11y_recent_day_done, dateText)
    } else {
        stringResource(R.string.a11y_recent_day_empty, dateText)
    }
    val hint = stringResource(R.string.a11y_recent_day_hint)
    Column(
        modifier
            .clip(RoundedCornerShape(10.dp))
            .clickable(onClickLabel = hint, onClick = onClick)
            .semantics(mergeDescendants = true) { contentDescription = desc; role = Role.Button },
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(
                    if (day.emoji != null) accent.copy(alpha = 0.15f)
                    else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f),
                )
                .then(if (day.isToday) Modifier.border(1.5.dp, accent, CircleShape) else Modifier),
            contentAlignment = Alignment.Center,
        ) {
            Text(day.emoji ?: "·", fontSize = if (day.emoji != null) 18.sp else 14.sp)
        }
        Spacer(Modifier.height(3.dp))
        Text(
            day.dayNumber.toString(),
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            color = if (day.isToday) accent else MaterialTheme.colorScheme.onSurface,
        )
        Text(
            day.weekday,
            fontSize = 10.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

// ---- Formatação dos dados físicos (mesma lógica do ProfilePhysicalDataView do iOS) ----

private fun heightDisplay(physical: PhysicalUi): String = when (physical.measurement) {
    MeasurementSystem.Metric -> "${physical.heightCm} cm"
    MeasurementSystem.Us -> {
        val (feet, inches) = UnitConverter.cmToFeetAndInches(physical.heightCm)
        "$feet'$inches\""
    }
}

private fun weightDisplay(physical: PhysicalUi): String = when (physical.measurement) {
    MeasurementSystem.Metric -> "%.1f kg".format(physical.weightKg)
    MeasurementSystem.Us -> "%.1f lbs".format(UnitConverter.kgToLbs(physical.weightKg))
}

/** Rótulo do sexo só pra male/female (other/não informado não vira linha). */
@StringRes
private fun sexLabelRes(sexRaw: String): Int? = when (sexRaw) {
    "male" -> R.string.sex_male
    "female" -> R.string.sex_female
    else -> null
}

/** Rótulo do objetivo fitness. */
@StringRes
private fun userGoalLabelRes(goal: UserGoal): Int = when (goal) {
    UserGoal.Bulking -> R.string.goal_bulking
    UserGoal.Maintenance -> R.string.goal_maintenance
    UserGoal.Cutting -> R.string.goal_cutting
}
