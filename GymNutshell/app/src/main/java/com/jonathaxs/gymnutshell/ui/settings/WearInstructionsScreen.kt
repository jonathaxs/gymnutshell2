package com.jonathaxs.gymnutshell.ui.settings

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.jonathaxs.gymnutshell.R
import com.jonathaxs.gymnutshell.ui.theme.color

/**
 * Instruções pra instalar o app no relógio — porte da AppleWatchInstructionsView (iOS),
 * com os passos adaptados pro fluxo do Wear OS (Play Store no próprio relógio).
 */
@Composable
fun WearInstructionsScreen(onBack: () -> Unit, viewModel: SettingsViewModel = viewModel()) {
    val accent = viewModel.accentColor.collectAsStateWithLifecycle().value.color

    Scaffold(topBar = { SettingsTopBar(stringResource(R.string.wear_sheet_title), onBack) }) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
        ) {
            Spacer(Modifier.height(24.dp))
            // Ícone azul do relógio, redondo — mesma arte do watchOS/Wear OS (não o ícone
            // preto do app do celular). Espelha o Image("AppleWatchIcon") do iOS.
            Image(
                painter = painterResource(R.drawable.ic_wear_app),
                contentDescription = stringResource(R.string.a11y_app_icon),
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .align(Alignment.CenterHorizontally),
            )

            Spacer(Modifier.height(20.dp))
            Text(stringResource(R.string.wear_sheet_intro), style = MaterialTheme.typography.bodyLarge)

            Spacer(Modifier.height(20.dp))
            StepRow(1, stringResource(R.string.wear_sheet_step1), accent)
            StepRow(2, stringResource(R.string.wear_sheet_step2), accent)
            StepRow(3, stringResource(R.string.wear_sheet_step3), accent)

            Spacer(Modifier.height(12.dp))
            Text(
                stringResource(R.string.wear_sheet_note),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

/** Passo numerado: círculo na cor de destaque + texto, igual ao step() do iOS. */
@Composable
private fun StepRow(number: Int, text: String, accent: androidx.compose.ui.graphics.Color) {
    Row(Modifier.fillMaxWidth().padding(vertical = 6.dp), verticalAlignment = Alignment.Top) {
        Box(
            Modifier.size(28.dp).clip(CircleShape).background(accent),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                "$number",
                color = androidx.compose.ui.graphics.Color.White,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
            )
        }
        Spacer(Modifier.width(12.dp))
        Text(
            text,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f).padding(top = 2.dp),
        )
    }
}
