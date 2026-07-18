package com.jonathaxs.gymnutshell.ui.welcome

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.jonathaxs.gymnutshell.R
import com.jonathaxs.gymnutshell.core.domain.AppDateFormatters
import com.jonathaxs.gymnutshell.core.domain.AppTheme
import com.jonathaxs.gymnutshell.core.domain.GoalsCalculator
import com.jonathaxs.gymnutshell.core.domain.GoalsProvider
import com.jonathaxs.gymnutshell.core.domain.Profile
import com.jonathaxs.gymnutshell.core.domain.ThemeCategory
import com.jonathaxs.gymnutshell.core.domain.UserGoal
import com.jonathaxs.gymnutshell.core.theme.AccentColor
import com.jonathaxs.gymnutshell.ui.settings.ThemeInfoSheet
import com.jonathaxs.gymnutshell.ui.settings.themeNameRes
import com.jonathaxs.gymnutshell.ui.theme.color
import com.jonathaxs.gymnutshell.ui.util.Breakpoints
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

/** Nascimento padrão exibido até o usuário escolher (≈ 2001-01-01 do iOS). */
private val DEFAULT_BIRTHDAY: LocalDate = LocalDate.of(2001, 1, 1)

/** Cores dos objetivos — espelham goalColor do iOS (laranja/azul/verde). */
private val GoalBulkingColor = Color(0xFFFF9500)
private val GoalMaintenanceColor = Color(0xFF007AFF)
private val GoalCuttingColor = Color(0xFF34C759)

/** Vermelho do botão "Remover" das metas opcionais (≈ .red do iOS). */
private val RemoveRed = Color(0xFFFF3B30)

/** Etapas do onboarding — porte de WelcomeStep (iOS). Ordem = ordem de exibição. */
private enum class WelcomeStep { Start, Goal, Physical, Summary, Theme }

/**
 * Onboarding — porte de WelcomeView (iOS, métrico). Cada etapa rola o próprio conteúdo
 * (com cabeçalho emoji + título), barra de progresso em cápsulas fixa no topo e botões no rodapé.
 * Ordem: início → objetivo → dados físicos → resumo → tema.
 */
@Composable
fun WelcomeScreen(viewModel: WelcomeViewModel = viewModel()) {
    val steps = WelcomeStep.entries
    var stepIndex by rememberSaveable { mutableIntStateOf(0) }
    val step = steps[stepIndex]

    var weight by rememberSaveable { mutableStateOf("") }
    var height by rememberSaveable { mutableStateOf("") }
    var sex by rememberSaveable { mutableStateOf("male") }
    var birthdayEpochDay by rememberSaveable { mutableLongStateOf(DEFAULT_BIRTHDAY.toEpochDay()) }
    var goalOrdinal by rememberSaveable { mutableIntStateOf(UserGoal.Maintenance.ordinal) }
    var themeOrdinal by rememberSaveable { mutableIntStateOf(AppTheme.Gym.ordinal) }
    // Metas opcionais começam desligadas (igual ao iOS): o usuário decide adicioná-las no resumo.
    var includeFats by rememberSaveable { mutableStateOf(false) }
    var includeCreatine by rememberSaveable { mutableStateOf(false) }
    // Tema cujo sheet de níveis está aberto (botão ⓘ).
    var infoTheme by remember { mutableStateOf<AppTheme?>(null) }

    val goal = UserGoal.entries[goalOrdinal]
    val theme = AppTheme.entries[themeOrdinal]
    val birthday = LocalDate.ofEpochDay(birthdayEpochDay)
    val profile = Profile(
        name = "",
        weightKg = weight.toDoubleOrNull() ?: 0.0,
        heightCm = height.toIntOrNull() ?: 0,
        age = Profile.age(birthday),
        sex = sex,
        goal = goal,
    )
    val physicalValid = profile.weightKg > 0 && profile.heightCm > 0
    // Cor de destaque do onboarding, derivada do sexo (espelha sexColor do iOS).
    val sexColor = AccentColor.defaultForSex(sex).color

    // Seletor de arquivo pra restaurar um backup local (JSON) — só na etapa inicial.
    val restoreFailed by viewModel.restoreFailed.collectAsStateWithLifecycle()
    val restoreLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri?.let { viewModel.restore(it) }
    }

    Scaffold { padding ->
        // Topo (voltar + progresso) e rodapé (botões) reaproveitados nos dois layouts (estreito e largo).
        val topBar: @Composable (Modifier) -> Unit = { mod ->
            Row(
                mod,
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                if (step != WelcomeStep.Start) {
                    IconButton(onClick = { stepIndex-- }, modifier = Modifier.size(36.dp)) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.a11y_welcome_back),
                        )
                    }
                }
                WelcomeProgressBar(stepIndex, steps.size, sexColor, Modifier.weight(1f))
            }
        }
        val footerButtons: @Composable () -> Unit = {
            val isLast = step == WelcomeStep.Theme
            val label = if (isLast) R.string.welcome_button_start else R.string.welcome_button_continue
            val color = if (step == WelcomeStep.Goal) goalColor(goal) else sexColor
            val enabled = step != WelcomeStep.Physical || physicalValid
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                WelcomePrimaryButton(stringResource(label), color, enabled) {
                    if (isLast) viewModel.complete(profile, theme, includeFats, includeCreatine) else stepIndex++
                }
                TextButton(onClick = { stepIndex-- }, modifier = Modifier.fillMaxWidth()) {
                    Text(stringResource(R.string.action_back), color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
        val stepContent: @Composable () -> Unit = {
            Box(Modifier.fillMaxSize()) {
                when (step) {
                    WelcomeStep.Start -> StartStep(
                        accent = sexColor,
                        onStart = { stepIndex++ },
                        onRestore = { restoreLauncher.launch(arrayOf("application/json")) },
                    )
                    WelcomeStep.Goal -> GoalStep(goal) { goalOrdinal = it.ordinal }
                    WelcomeStep.Physical -> PhysicalStep(
                        weight = weight, height = height, sex = sex, birthday = birthday,
                        onWeight = { weight = it }, onHeight = { height = it }, onSex = { sex = it },
                        onBirthday = { birthdayEpochDay = it.toEpochDay() },
                    )
                    WelcomeStep.Summary -> SummaryStep(
                        goals = GoalsProvider.goals(profile),
                        goal = goal,
                        accent = sexColor,
                        includeFats = includeFats,
                        includeCreatine = includeCreatine,
                        onToggleFats = { includeFats = !includeFats },
                        onToggleCreatine = { includeCreatine = !includeCreatine },
                    )
                    WelcomeStep.Theme -> ThemeStep(
                        selected = theme,
                        sex = sex,
                        accent = sexColor,
                        onSelect = { themeOrdinal = it.ordinal },
                        onInfo = { infoTheme = it },
                    )
                }
            }
        }

        // Wide (tablet/landscape): navegação à esquerda, conteúdo da etapa à direita. Estreito: empilhado.
        BoxWithConstraints(Modifier.fillMaxSize().padding(padding).imePadding()) {
            val isWide = maxWidth >= Breakpoints.WideThreshold
            if (isWide) {
                Row(Modifier.fillMaxSize().padding(horizontal = 24.dp)) {
                    Column(
                        Modifier.width(300.dp).fillMaxHeight().padding(top = 24.dp, bottom = 24.dp, end = 16.dp),
                    ) {
                        topBar(Modifier.fillMaxWidth())
                        Spacer(Modifier.weight(1f))
                        if (step != WelcomeStep.Start) footerButtons()
                    }
                    Box(Modifier.weight(1f).fillMaxHeight().padding(vertical = 16.dp)) { stepContent() }
                }
            } else {
                Column(Modifier.fillMaxSize().padding(horizontal = 24.dp)) {
                    topBar(Modifier.fillMaxWidth().padding(top = 20.dp))
                    Spacer(Modifier.height(8.dp))
                    Box(Modifier.weight(1f).fillMaxWidth()) { stepContent() }
                    if (step != WelcomeStep.Start) {
                        Spacer(Modifier.height(12.dp))
                        Column(Modifier.padding(bottom = 24.dp)) { footerButtons() }
                    }
                }
            }
        }
    }

    // Sheet com os 4 níveis do tema tocado — mesmo porte do ThemeInfoView usado nas Settings.
    infoTheme?.let { t -> ThemeInfoSheet(theme = t, sex = sex, onDismiss = { infoTheme = null }) }

    // Alerta de falha na restauração (arquivo inválido).
    if (restoreFailed) {
        AlertDialog(
            onDismissRequest = { viewModel.clearRestoreError() },
            title = { Text(stringResource(R.string.welcome_restore_error_title)) },
            text = { Text(stringResource(R.string.backup_status_error)) },
            confirmButton = {
                TextButton(onClick = { viewModel.clearRestoreError() }) { Text(stringResource(R.string.action_ok)) }
            },
        )
    }
}

// ---- Etapas ----

@Composable
private fun StartStep(accent: Color, onStart: () -> Unit, onRestore: () -> Unit) {
    Column(Modifier.fillMaxSize()) {
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState())) {
            WelcomeStepHeader("👋", stringResource(R.string.welcome_start_title))
            Spacer(Modifier.height(16.dp))
            Text(stringResource(R.string.welcome_start_intro), style = MaterialTheme.typography.bodyLarge)
        }
        Spacer(Modifier.height(16.dp))
        Text(
            stringResource(R.string.welcome_start_subtitle),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(12.dp))
        // Começar do zero ou restaurar um backup local — espelha os dois botões do WelcomeStartStep (iOS).
        WelcomePrimaryButton(stringResource(R.string.welcome_start_new), accent, enabled = true, onClick = onStart)
        Spacer(Modifier.height(8.dp))
        WelcomeSecondaryButton(stringResource(R.string.backup_import), accent, onClick = onRestore)
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun GoalStep(selected: UserGoal, onSelect: (UserGoal) -> Unit) {
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        WelcomeStepHeader("🎯", stringResource(R.string.welcome_goal_title))
        UserGoal.entries.forEach { goal ->
            GoalCard(goal, goal == selected) { onSelect(goal) }
        }
        Column(
            Modifier.fillMaxWidth().padding(top = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            CenteredCaption(stringResource(R.string.welcome_goal_info_choose))
            CenteredCaption(stringResource(R.string.welcome_goal_info_editable))
        }
    }
}

@Composable
private fun PhysicalStep(
    weight: String, height: String, sex: String, birthday: LocalDate,
    onWeight: (String) -> Unit, onHeight: (String) -> Unit, onSex: (String) -> Unit,
    onBirthday: (LocalDate) -> Unit,
) {
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        WelcomeStepHeader("👤", stringResource(R.string.welcome_physical_title))
        // Sexo primeiro (Female · Male · Do not inform).
        Text(stringResource(R.string.field_sex), style = MaterialTheme.typography.labelLarge)
        val sexes = listOf("female" to R.string.sex_female, "male" to R.string.sex_male, "other" to R.string.sex_other)
        SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
            sexes.forEachIndexed { index, (value, labelRes) ->
                SegmentedButton(
                    selected = sex == value,
                    onClick = { onSex(value) },
                    shape = SegmentedButtonDefaults.itemShape(index, sexes.size),
                ) { Text(stringResource(labelRes)) }
            }
        }
        OutlinedTextField(
            weight, onWeight, label = { Text(stringResource(R.string.field_weight_kg)) }, singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), modifier = Modifier.fillMaxWidth(),
        )
        OutlinedTextField(
            height, onHeight, label = { Text(stringResource(R.string.field_height_cm)) }, singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth(),
        )
        BirthdayField(birthday, onBirthday)
        // Textos informativos abaixo dos campos, igual ao iOS.
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Caption(stringResource(R.string.welcome_physical_info_calculate))
            Caption(stringResource(R.string.welcome_physical_info_editable))
        }
    }
}

/** Campo de nascimento: abre o DatePicker do Material 3; a idade é derivada da data. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BirthdayField(birthday: LocalDate, onBirthday: (LocalDate) -> Unit) {
    var show by remember { mutableStateOf(false) }
    val fieldLabel = stringResource(R.string.field_birthday)
    val dateText = AppDateFormatters.mediumDate(birthday)
    // Pro TalkBack o conjunto é um botão só ("Birthday, Jan 1, 2001") — o overlay carrega o rótulo
    // e o campo em si fica mudo, senão o foco pararia duas vezes no mesmo controle.
    val fieldDesc = stringResource(R.string.a11y_welcome_birthday, fieldLabel, dateText)
    val hint = stringResource(R.string.a11y_welcome_birthday_hint)
    Box {
        OutlinedTextField(
            value = dateText,
            onValueChange = {},
            readOnly = true,
            label = { Text(fieldLabel) },
            modifier = Modifier.fillMaxWidth().clearAndSetSemantics {},
        )
        // Overlay clicável (o TextField readOnly não recebe clique sozinho).
        Box(
            Modifier.matchParentSize()
                .clickable(onClickLabel = hint) { show = true }
                .semantics { contentDescription = fieldDesc; role = Role.Button },
        )
    }
    if (show) {
        val state = rememberDatePickerState(
            initialSelectedDateMillis = birthday.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli(),
        )
        DatePickerDialog(
            onDismissRequest = { show = false },
            confirmButton = {
                TextButton(onClick = {
                    state.selectedDateMillis?.let {
                        onBirthday(Instant.ofEpochMilli(it).atZone(ZoneOffset.UTC).toLocalDate())
                    }
                    show = false
                }) { Text(stringResource(R.string.action_ok)) }
            },
            dismissButton = {
                TextButton(onClick = { show = false }) { Text(stringResource(R.string.action_cancel)) }
            },
        ) {
            DatePicker(state = state)
        }
    }
}

@Composable
private fun SummaryStep(
    goals: GoalsCalculator.Result,
    goal: UserGoal,
    accent: Color,
    includeFats: Boolean,
    includeCreatine: Boolean,
    onToggleFats: () -> Unit,
    onToggleCreatine: () -> Unit,
) {
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(24.dp),
    ) {
        WelcomeStepHeader("✅", stringResource(R.string.welcome_summary_title), stringResource(R.string.welcome_summary_subtitle))

        SummaryCategory(R.string.category_essencial) {
            SummaryFixedRow("💤", stringResource(R.string.today_sleep), "${goals.sleep}h")
            SummaryFixedRow("💧", stringResource(R.string.today_water), "${goals.water} ml")
        }
        SummaryCategory(R.string.category_nutricao) {
            SummaryFixedRow("🔥", stringResource(R.string.today_calories), "${goals.calories} kcal")
            SummaryFixedRow("🍗", stringResource(R.string.today_protein), "${goals.protein}g")
            SummaryFixedRow("🌾", stringResource(R.string.today_fiber), "${goals.fiber}g")
            SummaryFixedRow("🍞", stringResource(R.string.today_carbs), "${goals.carbs}g")
            SummaryOptionalRow(
                "🧈", stringResource(R.string.today_good_fat), "${goals.goodFat}g",
                included = includeFats, accent = accent, onToggle = onToggleFats,
            )
        }
        SummaryCategory(R.string.category_treino) {
            SummaryFixedRow("🏋️", stringResource(R.string.today_workout), "${goals.workout} min")
            SummaryFixedRow("🏃", stringResource(R.string.today_cardio), "${goals.cardio} min")
        }
        SummaryCategory(R.string.category_suplemento) {
            SummaryOptionalRow(
                "🧪", stringResource(R.string.today_creatine), "${goals.creatine}g",
                included = includeCreatine, accent = accent, onToggle = onToggleCreatine,
            )
        }

        // Rodapé informativo centralizado.
        Column(
            Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            CenteredCaption(stringResource(R.string.welcome_summary_calculated, stringResource(goalLabel(goal))))
            CenteredCaption(stringResource(R.string.welcome_summary_customize))
        }
    }
}

@Composable
private fun ThemeStep(
    selected: AppTheme,
    sex: String,
    accent: Color,
    onSelect: (AppTheme) -> Unit,
    onInfo: (AppTheme) -> Unit,
) {
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        WelcomeStepHeader("🎭", stringResource(R.string.welcome_theme_title), stringResource(R.string.welcome_theme_subtitle))
        ThemeCategory.entries.forEach { category ->
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    stringResource(themeCategoryLabel(category, sex)),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = 4.dp),
                )
                AppTheme.inCategory(category).forEach { theme ->
                    WelcomeThemeRow(theme, sex, theme == selected, accent, onClick = { onSelect(theme) }, onInfo = { onInfo(theme) })
                }
            }
        }
        Caption(stringResource(R.string.welcome_theme_footer), Modifier.padding(start = 4.dp))
    }
}

// ---- Componentes compartilhados ----

/** Cabeçalho da etapa: emoji + título lado a lado e subtítulo opcional — porte de WelcomeStepHeader (iOS). */
@Composable
private fun WelcomeStepHeader(emoji: String, title: String, subtitle: String? = null) {
    Column(
        Modifier.padding(top = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(emoji, fontSize = 44.sp, modifier = Modifier.clearAndSetSemantics {})
            Text(
                title,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.semantics { heading() },
            )
        }
        if (subtitle != null) {
            Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

/** Barra de progresso em cápsulas (uma por etapa), preenchidas até a atual — porte de WelcomeProgressBar (iOS). */
@Composable
private fun WelcomeProgressBar(current: Int, total: Int, activeColor: Color, modifier: Modifier = Modifier) {
    val inactive = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
    val label = stringResource(R.string.welcome_progress_format, current + 1, total)
    Row(
        modifier = modifier.semantics { contentDescription = label },
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        repeat(total) { index ->
            Box(
                Modifier.weight(1f).height(4.dp).clip(CircleShape)
                    .background(if (index <= current) activeColor else inactive),
            )
        }
    }
}

/** Botão primário de largura total na cor da etapa, branco no texto — porte de WelcomeContinueButton (iOS). */
@Composable
private fun WelcomePrimaryButton(text: String, color: Color, enabled: Boolean, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier.fillMaxWidth().height(52.dp),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = color,
            contentColor = Color.White,
            disabledContainerColor = color.copy(alpha = 0.5f),
            disabledContentColor = Color.White.copy(alpha = 0.7f),
        ),
    ) { Text(text, fontWeight = FontWeight.SemiBold) }
}

/** Botão secundário (preenchimento claro na cor de destaque) — porte do botão "Restaurar backup" (iOS). */
@Composable
private fun WelcomeSecondaryButton(text: String, color: Color, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().height(52.dp),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(containerColor = color.copy(alpha = 0.15f), contentColor = color),
    ) { Text(text, fontWeight = FontWeight.SemiBold) }
}

/** Card de objetivo: nome + descrição, destacado na cor do objetivo quando selecionado — porte de UserGoalPickerView (iOS). */
@Composable
private fun GoalCard(goal: UserGoal, selected: Boolean, onClick: () -> Unit) {
    val bg = if (selected) goalColor(goal) else MaterialTheme.colorScheme.surfaceVariant
    val fg = if (selected) Color.White else MaterialTheme.colorScheme.onSurface
    // Estado de escolha por stateDescription (mesma convenção do ColorSwatch): o TalkBack anuncia
    // "Selected" em vez de depender do "✓" e da cor de fundo.
    val stateDesc = stringResource(if (selected) R.string.a11y_selected else R.string.a11y_not_selected)
    Row(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(bg)
            .clickable(role = Role.Button, onClick = onClick)
            .semantics { stateDescription = stateDesc }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(stringResource(goalLabel(goal)), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, color = fg)
            Text(
                stringResource(goalDescRes(goal)),
                style = MaterialTheme.typography.bodySmall,
                color = if (selected) Color.White.copy(alpha = 0.85f) else MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        if (selected) {
            Spacer(Modifier.width(8.dp))
            Text("✓", color = Color.White, style = MaterialTheme.typography.titleMedium, modifier = Modifier.clearAndSetSemantics {})
        }
    }
}

/** Linha selecionável de tema: prévia dos emojis + nome + check + botão ⓘ — porte do card de WelcomeThemeStep (iOS). */
@Composable
private fun WelcomeThemeRow(theme: AppTheme, sex: String, selected: Boolean, accent: Color, onClick: () -> Unit, onInfo: () -> Unit) {
    val themeName = stringResource(themeNameRes(theme, sex))
    val bg = if (selected) accent else MaterialTheme.colorScheme.surfaceVariant
    val fg = if (selected) Color.White else MaterialTheme.colorScheme.onSurface
    // Idem GoalCard: o estado vem do stateDescription, não do "✓" (que é decorativo).
    val stateDesc = stringResource(if (selected) R.string.a11y_selected else R.string.a11y_not_selected)
    Row(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(bg)
            .clickable(role = Role.Button, onClick = onClick)
            .semantics { stateDescription = stateDesc }
            .padding(start = 16.dp, top = 4.dp, bottom = 4.dp, end = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(theme.previewEmojis(sex).joinToString("  "), style = MaterialTheme.typography.titleMedium, modifier = Modifier.clearAndSetSemantics {})
        Spacer(Modifier.width(12.dp))
        Text(themeName, Modifier.weight(1f), style = MaterialTheme.typography.bodyLarge, color = fg)
        if (selected) Text("✓", color = Color.White, style = MaterialTheme.typography.titleMedium, modifier = Modifier.clearAndSetSemantics {})
        IconButton(onClick = onInfo) {
            Icon(
                Icons.Outlined.Info,
                contentDescription = stringResource(R.string.a11y_theme_info, themeName),
                tint = if (selected) Color.White else accent,
            )
        }
    }
}

/** Seção de categoria do resumo (cabeçalho + linhas), não colapsável — porte de summaryCategory (iOS). */
@Composable
private fun SummaryCategory(@StringRes titleRes: Int, content: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            stringResource(titleRes),
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(start = 4.dp),
        )
        content()
    }
}

/** Linha de meta fixa no resumo: emoji + nome + valor, em card. */
@Composable
private fun SummaryFixedRow(emoji: String, label: String, value: String) {
    Row(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(MaterialTheme.colorScheme.surfaceVariant).padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(emoji, modifier = Modifier.clearAndSetSemantics {})
        Spacer(Modifier.width(12.dp))
        Text(label, Modifier.weight(1f), style = MaterialTheme.typography.bodyLarge)
        Text(value, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
    }
}

/** Linha de meta opcional no resumo: badge "Recomendada" + botão Adicionar/Remover — porte de OptionalTrackingGoalRow (iOS). */
@Composable
private fun SummaryOptionalRow(
    emoji: String,
    label: String,
    value: String,
    included: Boolean,
    accent: Color,
    onToggle: () -> Unit,
) {
    val toggleDesc = stringResource(
        if (included) R.string.a11y_welcome_optional_remove else R.string.a11y_welcome_optional_add,
        label,
    )
    Row(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(MaterialTheme.colorScheme.surfaceVariant).padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(emoji, modifier = Modifier.clearAndSetSemantics {})
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(label, style = MaterialTheme.typography.bodyLarge)
            Text(
                stringResource(R.string.welcome_summary_optional_badge),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
                    .padding(horizontal = 8.dp, vertical = 2.dp),
            )
        }
        Text(value, style = MaterialTheme.typography.bodyLarge, fontWeight = if (included) FontWeight.Bold else FontWeight.Normal)
        Spacer(Modifier.width(8.dp))
        TextButton(onClick = onToggle, modifier = Modifier.semantics { contentDescription = toggleDesc }) {
            Text(
                stringResource(if (included) R.string.welcome_option_remove else R.string.welcome_option_add),
                color = if (included) RemoveRed else accent,
                fontWeight = FontWeight.Medium,
            )
        }
    }
}

/** Legenda secundária alinhada à esquerda. */
@Composable
private fun Caption(text: String, modifier: Modifier = Modifier) {
    Text(text, modifier = modifier, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
}

/** Legenda secundária centralizada (rodapés do iOS). */
@Composable
private fun CenteredCaption(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center,
        modifier = Modifier.fillMaxWidth(),
    )
}

private fun goalColor(goal: UserGoal): Color = when (goal) {
    UserGoal.Bulking -> GoalBulkingColor
    UserGoal.Maintenance -> GoalMaintenanceColor
    UserGoal.Cutting -> GoalCuttingColor
}

@StringRes
private fun goalLabel(goal: UserGoal): Int = when (goal) {
    UserGoal.Bulking -> R.string.goal_bulking
    UserGoal.Maintenance -> R.string.goal_maintenance
    UserGoal.Cutting -> R.string.goal_cutting
}

@StringRes
private fun goalDescRes(goal: UserGoal): Int = when (goal) {
    UserGoal.Bulking -> R.string.welcome_goal_bulking_desc
    UserGoal.Maintenance -> R.string.welcome_goal_maintenance_desc
    UserGoal.Cutting -> R.string.welcome_goal_cutting_desc
}

@StringRes
private fun themeCategoryLabel(category: ThemeCategory): Int = when (category) {
    ThemeCategory.Sport -> R.string.theme_cat_sport
    ThemeCategory.Animals -> R.string.theme_cat_animals
    ThemeCategory.Warrior -> R.string.theme_cat_warrior
    ThemeCategory.Space -> R.string.theme_cat_space
    ThemeCategory.Elements -> R.string.theme_cat_elements
    ThemeCategory.Competition -> R.string.theme_cat_competition
}

/** Nome da categoria com variante por sexo (só Guerreiro→Guerreira) — espelha localizedName(sex:). */
@StringRes
private fun themeCategoryLabel(category: ThemeCategory, sex: String): Int =
    if (category == ThemeCategory.Warrior && sex != "male") R.string.theme_cat_warrior_fem
    else themeCategoryLabel(category)
