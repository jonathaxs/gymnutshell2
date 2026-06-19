package com.jonathaxs.gymnutshell.ui.today

import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.systemGestureExclusion
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.jonathaxs.gymnutshell.R
import com.jonathaxs.gymnutshell.core.domain.GoalCategory
import com.jonathaxs.gymnutshell.core.domain.ProgressColors
import com.jonathaxs.gymnutshell.ui.settings.ProgressRingInfoSheet
import com.jonathaxs.gymnutshell.ui.settings.TierInfoSheet
import kotlin.math.roundToInt

/**
 * Largura máxima do conteúdo da Today (hero, headers, metas), centralizado — parity com o iOS (330pt).
 * Também afasta o slider das bordas da tela, onde o gesto de "voltar" do sistema captura o arraste.
 */
private val TodayContentMaxWidth = 330.dp

/** Tela "Hoje" — porte (MVP) da TodayView (iOS): header com % do dia + metas agrupadas por categoria. */
@Composable
fun TodayScreen(
    modifier: Modifier = Modifier,
    accent: Color = MaterialTheme.colorScheme.primary,
    onOpenHistory: () -> Unit = {},
    onOpenTheme: () -> Unit = {},
    viewModel: TodayViewModel = viewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    // Bottom sheets de info do anel e da conquista, abertos ao tocar neles (porte das sheets do iOS).
    var showRingInfo by remember { mutableStateOf(false) }
    var showTierInfo by remember { mutableStateOf(false) }

    Column(modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
        // A data de hoje é o próprio botão que abre o histórico de notificações (porte do botão de data da TodayHeroView do iOS).
        TodayHeader(
            state,
            accent = accent,
            onOpenHistory = onOpenHistory,
            onRingClick = { showRingInfo = true },
            onTierClick = { showTierInfo = true },
        )
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            state.sections.forEach { section ->
                // Header + metas no mesmo item pra animar o grupo ao expandir/recolher,
                // porte da .transition(.scale 0.92, anchor .top + opacity) do iOS (easeInOut 0.2s).
                item(key = "cat_${section.category.name}") {
                    Column(
                        Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        CategoryHeader(section, accent = accent) { viewModel.toggleCategory(section.category) }
                        AnimatedVisibility(
                            visible = !section.collapsed,
                            enter = fadeIn(tween(220)) +
                                expandVertically(tween(220), expandFrom = Alignment.Top) +
                                scaleIn(tween(220), initialScale = 0.92f, transformOrigin = TransformOrigin(0.5f, 0f)),
                            exit = fadeOut(tween(220)) +
                                shrinkVertically(tween(220), shrinkTowards = Alignment.Top) +
                                scaleOut(tween(220), targetScale = 0.92f, transformOrigin = TransformOrigin(0.5f, 0f)),
                        ) {
                            Column(
                                Modifier.fillMaxWidth().padding(top = 8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                section.goals.forEach { goal ->
                                    GoalRow(
                                        goal = goal,
                                        accent = accent,
                                        onSet = { viewModel.updateIntake(goal, it) },
                                        onToggleRest = { viewModel.toggleRestDay(goal) },
                                    )
                                }
                            }
                        }
                    }
                }
            }
            // Metas personalizadas sem categoria, no fim.
            items(state.uncategorizedGoals, key = { it.key }) { goal ->
                GoalRow(
                    goal = goal,
                    accent = accent,
                    onSet = { viewModel.updateIntake(goal, it) },
                    onToggleRest = { viewModel.toggleRestDay(goal) },
                )
            }
        }
    }

    // Sheets de info abertos pelo toque no anel / conquista.
    if (showRingInfo) {
        ProgressRingInfoSheet(onDismiss = { showRingInfo = false })
    }
    if (showTierInfo) {
        TierInfoSheet(onDismiss = { showTierInfo = false }, onOpenTheme = onOpenTheme)
    }
}

/**
 * Cabeçalho: data por extenso e, abaixo, anel de progresso (esquerda) + conquista do dia (direita),
 * lado a lado — espelha a HStack do TodayHeroView (iOS). A conquista mostra o rótulo "Achievement",
 * o emoji do tier e o nome "Level N" (nomes localizados por tema ainda não existem no Android).
 */
@Composable
private fun TodayHeader(
    state: TodayUiState,
    accent: Color,
    onOpenHistory: () -> Unit,
    onRingClick: () -> Unit,
    onTierClick: () -> Unit,
) {
    val progressDesc = stringResource(R.string.cd_daily_progress, state.overallPercent)
    val tierDesc = stringResource(R.string.cd_daily_tier, state.tierLevel)
    val historyDesc = stringResource(R.string.cd_notification_history)
    Column(
        modifier = Modifier
            .widthIn(max = TodayContentMaxWidth)
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // Data de hoje: botão com borda arredondada na cor de destaque que abre o histórico de notificações.
        Text(
            state.dateLabel,
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .widthIn(max = TodayContentMaxWidth)
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .clickable(onClick = onOpenHistory)
                .border(1.5.dp, accent.copy(alpha = 0.33f), RoundedCornerShape(18.dp))
                .padding(horizontal = 14.dp, vertical = 10.dp)
                .semantics { contentDescription = historyDesc },
        )
        Spacer(Modifier.height(16.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Bloco do anel: rótulo "Progress" + anel com % no centro. Toque abre a info do anel.
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(16.dp))
                    .clickable(onClick = onRingClick)
                    .semantics(mergeDescendants = true) { contentDescription = progressDesc },
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                HeroLabel(stringResource(R.string.today_ring_label))
                Spacer(Modifier.height(12.dp))
                TodayProgressRing(progress = state.overallProgress, percent = state.overallPercent)
            }
            // Bloco da conquista: rótulo "Achievement" + emoji do tier + nome "Level N". Toque abre a info da conquista.
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(16.dp))
                    .clickable(onClick = onTierClick)
                    .semantics(mergeDescendants = true) { contentDescription = tierDesc },
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                HeroLabel(stringResource(R.string.today_tier_label))
                Spacer(Modifier.height(12.dp))
                // Emoji cresce e fica opaco em tiers mais altos (porte do DailyTierView do iOS).
                val emojiScale = when (state.tierLevel) {
                    1 -> 0.95f
                    2 -> 1.0f
                    3 -> 1.05f
                    else -> 1.10f
                }
                Text(
                    state.tierEmoji,
                    fontSize = 72.sp,
                    modifier = Modifier
                        .scale(emojiScale)
                        .alpha(if (state.tierLevel == 1) 0.85f else 1f)
                        .clearAndSetSemantics {},
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    stringResource(R.string.progress_tier_level, state.tierLevel),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.clearAndSetSemantics {},
                )
            }
        }
    }
}

/** Rótulo pequeno acima do anel e da conquista ("Progress" / "Achievement"), em cor secundária. */
@Composable
private fun HeroLabel(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

/**
 * Cabeçalho de categoria, clicável pra abrir/fechar — porte do TodayCategoryHeader (iOS).
 * Texto centralizado e fundo arredondado que sinaliza o estado: expandido arredonda só em CIMA
 * (cor de destaque, "conecta" com as metas que aparecem abaixo); colapsado arredonda só EMBAIXO (cinza).
 */
@Composable
private fun CategoryHeader(section: TodayCategoryUi, accent: Color, onToggle: () -> Unit) {
    val title = stringResource(categoryTitleRes(section.category))
    val stateDesc = stringResource(
        if (section.collapsed) R.string.state_collapsed else R.string.state_expanded,
    )
    // Cantos arredondados conforme o estado (UnevenRoundedRectangle no iOS).
    val shape = if (section.collapsed) {
        RoundedCornerShape(bottomStart = 14.dp, bottomEnd = 14.dp)
    } else {
        RoundedCornerShape(topStart = 14.dp, topEnd = 14.dp)
    }
    // Cor do fundo (anima a troca cinza↔destaque na expansão/colapso).
    val bg by animateColorAsState(
        targetValue = if (section.collapsed) {
            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.20f)
        } else {
            accent.copy(alpha = 0.22f)
        },
        label = "categoryHeaderBg",
    )
    Row(
        modifier = Modifier
            .widthIn(max = TodayContentMaxWidth)
            .fillMaxWidth()
            .clip(shape)
            .background(bg)
            .clickable(onClick = onToggle)
            .semantics { stateDescription = stateDesc }
            .padding(vertical = 6.dp, horizontal = 10.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // chevron visual escondido do TalkBack (o estado já vem pelo stateDescription)
        Text(if (section.collapsed) "▸" else "▾", modifier = Modifier.clearAndSetSemantics {})
        Spacer(Modifier.width(6.dp))
        Text(
            title.uppercase(),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.5.sp,
        )
    }
}

/** Linha de uma meta: emoji, título, valor/alvo, botão de descanso e slider. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun GoalRow(goal: TodayGoalUi, accent: Color, onSet: (Int) -> Unit, onToggleRest: () -> Unit) {
    // Metas custom já trazem o título; built-in resolvem via string resource.
    val title = goal.title ?: stringResource(titleRes(goal.key))
    val restLabel = stringResource(R.string.rest_day)

    // Valor do slider em estado local (atualiza ao vivo no arraste) e sincronizado com o persistido.
    var sliderValue by remember(goal.key) { mutableFloatStateOf(goal.intake.toFloat()) }
    LaunchedEffect(goal.intake) { sliderValue = goal.intake.toFloat() }
    val shownValue = snapToIncrement(sliderValue, goal.increment, goal.target)

    // Cor do slider segue o progresso da meta (vermelho→laranja→verde→ciano→azul), como no iOS.
    val progressFraction = if (goal.target > 0) shownValue.toFloat() / goal.target else 0f
    val sliderColor = Color(ProgressColors.ringArgb(progressFraction.toDouble()))

    Card(Modifier.widthIn(max = TodayContentMaxWidth).fillMaxWidth()) {
        Column(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(goal.emoji, style = MaterialTheme.typography.titleLarge)
                Spacer(Modifier.width(12.dp))
                Text(title, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyLarge)
                // Valor com unidade — fica à esquerda do botão de descanso, como no iOS.
                Text(
                    text = "$shownValue/${goal.target} ${goal.unit}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                // Botão ON/OFF de descanso (só metas de Treino), no lugar do controle de valor, como o iOS.
                if (goal.supportsRestDay) {
                    Spacer(Modifier.width(10.dp))
                    RestDayToggle(isRestDay = goal.isRestDay, accent = accent, onClick = onToggleRest)
                }
            }
            // Dia de descanso: substitui o slider por um texto centralizado (como o iOS).
            if (goal.isRestDay) {
                Text(
                    restLabel,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                )
            } else {
                val maxTarget = goal.target.toFloat().coerceAtLeast(1f)
                Slider(
                    // Exclui a área do slider do gesto de "voltar" do sistema (arraste pela borda), evitando sair do app.
                    modifier = Modifier.systemGestureExclusion(),
                    value = sliderValue,
                    onValueChange = { sliderValue = it },
                    onValueChangeFinished = { onSet(snapToIncrement(sliderValue, goal.increment, goal.target)) },
                    valueRange = 0f..maxTarget,
                    colors = SliderDefaults.colors(
                        thumbColor = sliderColor,
                        activeTrackColor = sliderColor,
                        inactiveTrackColor = sliderColor.copy(alpha = 0.24f),
                    ),
                    // Track fino e contínuo (sem os segmentos do Material 3 expressive) + thumb circular com
                    // leve sombra: visual sóbrio, próximo dos sliders do próprio sistema Android.
                    track = {
                        val fraction = (sliderValue / maxTarget).coerceIn(0f, 1f)
                        Box(
                            Modifier
                                .fillMaxWidth()
                                .height(4.dp)
                                .clip(CircleShape)
                                .background(sliderColor.copy(alpha = 0.24f)),
                        ) {
                            Box(
                                Modifier
                                    .fillMaxWidth(fraction)
                                    .height(4.dp)
                                    .clip(CircleShape)
                                    .background(sliderColor),
                            )
                        }
                    },
                    thumb = {
                        Box(
                            Modifier
                                .size(20.dp)
                                .shadow(2.dp, CircleShape)
                                .background(sliderColor, CircleShape),
                        )
                    },
                )
            }
        }
    }
}

/**
 * Botão ON/OFF de dia de descanso (porte do restDayToggle do iOS): pílula à direita do valor.
 * "ON" (accent sobre cinza) = treinando; "OFF" (cinza sobre accent) = em descanso. O texto reflete o treino.
 */
@Composable
private fun RestDayToggle(isRestDay: Boolean, accent: Color, onClick: () -> Unit) {
    val label = if (isRestDay) stringResource(R.string.rest_day_off) else stringResource(R.string.rest_day_on)
    val textColor = if (isRestDay) MaterialTheme.colorScheme.onSurfaceVariant else accent
    val bgColor = if (isRestDay) accent.copy(alpha = 0.25f) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
    Text(
        label,
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.Bold,
        color = textColor,
        modifier = Modifier
            .clip(CircleShape)
            .clickable(onClick = onClick)
            .background(bgColor)
            .padding(horizontal = 14.dp, vertical = 6.dp),
    )
}

/** Arredonda o valor do slider pro múltiplo de increment mais próximo, dentro de [0, target]. */
private fun snapToIncrement(value: Float, increment: Int, target: Int): Int =
    ((value / increment).roundToInt() * increment).coerceIn(0, target)

/** Mapeia a categoria pro título localizado. */
@StringRes
private fun categoryTitleRes(category: GoalCategory): Int = when (category) {
    GoalCategory.Essencial -> R.string.category_essencial
    GoalCategory.Nutricao -> R.string.category_nutricao
    GoalCategory.Treino -> R.string.category_treino
    GoalCategory.Suplemento -> R.string.category_suplemento
}

/** Mapeia a chave da meta pro título localizado (strings.xml). */
@StringRes
private fun titleRes(key: String): Int = when (key) {
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
