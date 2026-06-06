package com.jonathaxs.gymnutshell.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.jonathaxs.gymnutshell.R
import com.jonathaxs.gymnutshell.core.domain.Profile

/**
 * Formulário de dados físicos — porte (MVP, métrico) de PhysicalDataSettingsView (iOS).
 * Carrega os valores do perfil e, ao salvar, grava no ProfileRepository (recalcula as metas da Today).
 * O objetivo fitness (Goal) fica em página separada (UserGoalScreen), preservado aqui ao salvar.
 */
@Composable
fun PhysicalDataSection(profile: Profile, onSave: (Profile) -> Unit) {
    var name by remember { mutableStateOf(profile.name) }
    var weight by remember { mutableStateOf(formatWeight(profile.weightKg)) }
    var height by remember { mutableStateOf(if (profile.heightCm > 0) profile.heightCm.toString() else "") }
    var age by remember { mutableStateOf(if (profile.age > 0) profile.age.toString() else "") }
    var sex by remember { mutableStateOf(profile.sex) }

    // Sincroniza os campos quando o perfil carrega/muda (o profile só re-emite após salvar).
    LaunchedEffect(profile) {
        name = profile.name
        weight = formatWeight(profile.weightKg)
        height = if (profile.heightCm > 0) profile.heightCm.toString() else ""
        age = if (profile.age > 0) profile.age.toString() else ""
        sex = profile.sex
    }

    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        OutlinedTextField(
            value = name, onValueChange = { name = it },
            label = { Text(stringResource(R.string.field_name)) },
            singleLine = true, modifier = Modifier.fillMaxWidth(),
        )
        OutlinedTextField(
            value = weight, onValueChange = { weight = it },
            label = { Text(stringResource(R.string.field_weight_kg)) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier.fillMaxWidth(),
        )
        OutlinedTextField(
            value = height, onValueChange = { height = it },
            label = { Text(stringResource(R.string.field_height_cm)) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth(),
        )
        OutlinedTextField(
            value = age, onValueChange = { age = it },
            label = { Text(stringResource(R.string.field_age)) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth(),
        )

        Text(stringResource(R.string.field_sex), style = MaterialTheme.typography.labelLarge)
        val sexes = listOf("male" to R.string.sex_male, "female" to R.string.sex_female, "other" to R.string.sex_other)
        SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
            sexes.forEachIndexed { index, (value, labelRes) ->
                SegmentedButton(
                    selected = sex == value,
                    onClick = { sex = value },
                    shape = SegmentedButtonDefaults.itemShape(index, sexes.size),
                ) { Text(stringResource(labelRes)) }
            }
        }

        Button(
            onClick = {
                onSave(
                    Profile(
                        name = name.trim(),
                        weightKg = weight.toDoubleOrNull() ?: 0.0,
                        heightCm = height.toIntOrNull() ?: 0,
                        age = age.toIntOrNull() ?: 0,
                        sex = sex,
                        goal = profile.goal, // preservado; editado em UserGoalScreen
                    ),
                )
            },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(stringResource(R.string.action_save))
        }
    }
}

/** Mostra o peso sem ".0" quando é inteiro. */
private fun formatWeight(kg: Double): String = when {
    kg <= 0.0 -> ""
    kg % 1.0 == 0.0 -> kg.toInt().toString()
    else -> kg.toString()
}
