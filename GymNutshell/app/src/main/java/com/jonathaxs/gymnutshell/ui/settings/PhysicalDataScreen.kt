package com.jonathaxs.gymnutshell.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.jonathaxs.gymnutshell.R
import com.jonathaxs.gymnutshell.core.domain.AppDateFormatters
import com.jonathaxs.gymnutshell.core.domain.MeasurementSystem
import com.jonathaxs.gymnutshell.core.domain.Profile
import com.jonathaxs.gymnutshell.core.domain.UnitConverter
import com.jonathaxs.gymnutshell.ui.theme.color
import com.jonathaxs.gymnutshell.ui.components.GroupRowDivider
import com.jonathaxs.gymnutshell.ui.components.GroupSection
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

// Aniversário padrão quando o usuário ainda não informou a idade (igual ao Welcome).
private val DEFAULT_BIRTHDAY: LocalDate = LocalDate.of(2001, 1, 1)

/**
 * Sub-tela de dados físicos — porte de PhysicalDataSettingsView (iOS), em seções agrupadas.
 * Pessoal (nome) + Físico (peso, altura, sexo, aniversário), ciente de unidade (kg/cm ou lbs/ft·in).
 * Salva pela top bar; ao salvar recalcula as metas da Today. O objetivo fitness fica em UserGoalScreen.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhysicalDataScreen(onBack: () -> Unit, viewModel: SettingsViewModel = viewModel()) {
    val profile by viewModel.profile.collectAsStateWithLifecycle()
    val measurement by viewModel.measurementSystem.collectAsStateWithLifecycle()
    val accent = viewModel.accentColor.collectAsStateWithLifecycle().value.color

    // Rascunho dos campos; o peso/altura vivem na unidade atual e são convertidos ao salvar.
    var name by remember { mutableStateOf("") }
    var weightText by remember { mutableStateOf("") }
    var heightCmText by remember { mutableStateOf("") }
    var heightFeetText by remember { mutableStateOf("") }
    var heightInchesText by remember { mutableStateOf("") }
    var sex by remember { mutableStateOf("other") }
    var birthday by remember { mutableStateOf(DEFAULT_BIRTHDAY) }

    // (Re)semeia os campos quando o perfil/medida carregam ou mudam (o profile só re-emite após salvar).
    LaunchedEffect(profile, measurement) {
        name = profile.name
        sex = profile.sex
        birthday = birthdayFromAge(profile.age)
        when (measurement) {
            MeasurementSystem.Metric -> {
                weightText = formatWeight(profile.weightKg)
                heightCmText = if (profile.heightCm > 0) profile.heightCm.toString() else ""
            }
            MeasurementSystem.Us -> {
                weightText = if (profile.weightKg > 0) formatWeight(UnitConverter.kgToLbs(profile.weightKg)) else ""
                if (profile.heightCm > 0) {
                    val (feet, inches) = UnitConverter.cmToFeetAndInches(profile.heightCm)
                    heightFeetText = feet.toString()
                    heightInchesText = inches.toString()
                } else {
                    heightFeetText = ""
                    heightInchesText = ""
                }
            }
        }
    }

    fun buildProfile(): Profile {
        val weightKg = when (measurement) {
            MeasurementSystem.Metric -> weightText.toDoubleOrNull() ?: 0.0
            MeasurementSystem.Us -> weightText.toDoubleOrNull()?.let { UnitConverter.lbsToKg(it) } ?: 0.0
        }
        val heightCm = when (measurement) {
            MeasurementSystem.Metric -> heightCmText.toIntOrNull() ?: 0
            MeasurementSystem.Us -> {
                val feet = heightFeetText.toIntOrNull() ?: 0
                val inches = heightInchesText.toIntOrNull() ?: 0
                if (feet > 0) UnitConverter.feetAndInchesToCm(feet, inches) else 0
            }
        }
        return Profile(
            name = name.trim(),
            weightKg = weightKg,
            heightCm = heightCm,
            age = Profile.age(birthday),
            sex = sex,
            goal = profile.goal, // preservado; editado em UserGoalScreen
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings_physical_data)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.cd_back))
                    }
                },
                actions = {
                    TextButton(onClick = {
                        viewModel.saveProfile(buildProfile())
                        onBack()
                    }) {
                        Text(stringResource(R.string.action_save), color = accent)
                    }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(bottom = 24.dp),
        ) {
            GroupSection(title = stringResource(R.string.settings_profile_section_personal)) {
                FieldRow(
                    label = stringResource(R.string.field_name),
                    value = name,
                    onValueChange = { name = it },
                    accent = accent,
                    keyboardType = KeyboardType.Text,
                    placeholder = stringResource(R.string.field_name_hint),
                )
            }

            GroupSection(title = stringResource(R.string.settings_physical_data)) {
                // Peso na unidade atual (kg ou lbs).
                FieldRow(
                    label = stringResource(
                        if (measurement == MeasurementSystem.Metric) R.string.field_weight_kg else R.string.field_weight_lbs,
                    ),
                    value = weightText,
                    onValueChange = { weightText = clampNumber(it, maxIntDigits = 3, allowDecimal = true) },
                    accent = accent,
                    keyboardType = KeyboardType.Decimal,
                )
                GroupRowDivider()

                // Altura: cm (métrico) ou pés + polegadas (US).
                if (measurement == MeasurementSystem.Metric) {
                    FieldRow(
                        label = stringResource(R.string.field_height_cm),
                        value = heightCmText,
                        onValueChange = { heightCmText = clampNumber(it, maxIntDigits = 3) },
                        accent = accent,
                        keyboardType = KeyboardType.Number,
                    )
                } else {
                    HeightImperialRow(
                        feet = heightFeetText,
                        inches = heightInchesText,
                        onFeet = { heightFeetText = clampNumber(it, maxIntDigits = 1) },
                        onInches = { heightInchesText = clampNumber(it, maxIntDigits = 2) },
                        accent = accent,
                    )
                }
                GroupRowDivider()

                SexRow(sex = sex, onSelect = { sex = it })
                GroupRowDivider()

                BirthdayRow(birthday = birthday, onBirthday = { birthday = it })
            }
        }
    }
}

/** Linha "rótulo … campo" no estilo agrupado (campo encostado à direita, como o LabeledContent do iOS). */
@Composable
private fun FieldRow(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    accent: Color,
    keyboardType: KeyboardType,
    placeholder: String? = null,
) {
    Row(
        modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp).padding(horizontal = 20.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, style = MaterialTheme.typography.bodyLarge)
        Spacer(Modifier.weight(1f))
        TrailingInput(value, onValueChange, accent, keyboardType, placeholder, Modifier.widthIn(min = 72.dp, max = 200.dp))
    }
}

/** Linha de altura imperial: dois campos pequenos (pés/polegadas) com as unidades ao lado. */
@Composable
private fun HeightImperialRow(
    feet: String,
    inches: String,
    onFeet: (String) -> Unit,
    onInches: (String) -> Unit,
    accent: Color,
) {
    Row(
        modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp).padding(horizontal = 20.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(stringResource(R.string.field_height), style = MaterialTheme.typography.bodyLarge)
        Spacer(Modifier.weight(1f))
        TrailingInput(feet, onFeet, accent, KeyboardType.Number, modifier = Modifier.width(40.dp))
        Spacer(Modifier.width(4.dp))
        Text(stringResource(R.string.unit_feet), color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.width(12.dp))
        TrailingInput(inches, onInches, accent, KeyboardType.Number, modifier = Modifier.width(40.dp))
        Spacer(Modifier.width(4.dp))
        Text(stringResource(R.string.unit_inches), color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

/** Campo de texto sem borda, alinhado à direita (visual do trailing TextField do iOS). */
@Composable
private fun TrailingInput(
    value: String,
    onValueChange: (String) -> Unit,
    accent: Color,
    keyboardType: KeyboardType,
    placeholder: String? = null,
    modifier: Modifier = Modifier,
) {
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        singleLine = true,
        textStyle = MaterialTheme.typography.bodyLarge.copy(
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.End,
        ),
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        cursorBrush = SolidColor(accent),
        modifier = modifier,
        decorationBox = { innerTextField ->
            Box(contentAlignment = Alignment.CenterEnd) {
                if (value.isEmpty() && placeholder != null) {
                    Text(
                        placeholder,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.End,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
                innerTextField()
            }
        },
    )
}

/** Linha de sexo: mostra o valor atual e abre um menu suspenso (porte do Picker do iOS). */
@Composable
private fun SexRow(sex: String, onSelect: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    val options = listOf("male" to R.string.sex_male, "female" to R.string.sex_female, "other" to R.string.sex_other)
    val currentLabel = stringResource(options.firstOrNull { it.first == sex }?.second ?: R.string.sex_other)
    Box {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = true }
                .heightIn(min = 56.dp)
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(stringResource(R.string.field_sex), modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyLarge)
            Text(currentLabel, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.width(8.dp))
            Text("›", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { (value, labelRes) ->
                DropdownMenuItem(
                    text = { Text(stringResource(labelRes)) },
                    onClick = {
                        onSelect(value)
                        expanded = false
                    },
                )
            }
        }
    }
}

/** Linha de aniversário: mostra a data formatada e abre o DatePicker do Material 3 (idade derivada). */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BirthdayRow(birthday: LocalDate, onBirthday: (LocalDate) -> Unit) {
    var show by remember { mutableStateOf(false) }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { show = true }
            .heightIn(min = 56.dp)
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(stringResource(R.string.field_birthday), modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyLarge)
        Text(AppDateFormatters.mediumDate(birthday), color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.width(8.dp))
        Text("›", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
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

/** Aniversário aproximado a partir da idade salva (1º de janeiro de ano-idade), como a migração do iOS. */
private fun birthdayFromAge(age: Int): LocalDate =
    if (age > 0) LocalDate.of(LocalDate.now().year - age, 1, 1) else DEFAULT_BIRTHDAY

/** Mostra o número sem ".0" quando é inteiro (peso). Sempre com ponto (Locale.US) pra `toDoubleOrNull` parsear. */
private fun formatWeight(value: Double): String = when {
    value <= 0.0 -> ""
    value % 1.0 == 0.0 -> value.toInt().toString()
    else -> String.format(java.util.Locale.US, "%.1f", value)
}

/** Trunca pra no máximo `maxIntDigits` algarismos; com `allowDecimal`, aceita um ponto e 1 casa. */
private fun clampNumber(text: String, maxIntDigits: Int, allowDecimal: Boolean = false): String {
    val normalized = text.replace(',', '.')
    if (!allowDecimal) return normalized.filter { it.isDigit() }.take(maxIntDigits)
    val parts = normalized.split('.', limit = 2)
    val intPart = parts[0].filter { it.isDigit() }.take(maxIntDigits)
    return if (parts.size > 1) {
        val frac = parts[1].filter { it.isDigit() }.take(1)
        "$intPart.$frac"
    } else if (normalized.contains('.')) {
        "$intPart."
    } else {
        intPart
    }
}
