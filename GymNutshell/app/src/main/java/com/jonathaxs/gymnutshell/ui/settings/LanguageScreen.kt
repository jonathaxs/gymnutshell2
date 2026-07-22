package com.jonathaxs.gymnutshell.ui.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.jonathaxs.gymnutshell.R
import com.jonathaxs.gymnutshell.ui.components.GroupCheck
import com.jonathaxs.gymnutshell.ui.components.GroupRow
import com.jonathaxs.gymnutshell.ui.components.GroupRowDivider
import com.jonathaxs.gymnutshell.ui.components.GroupSection
import com.jonathaxs.gymnutshell.ui.theme.color
import com.jonathaxs.gymnutshell.util.AppLocales

/**
 * Sub-tela de idioma — espelha a opção "Language" do iOS (abaixo do Backup).
 * Troca o idioma por-app dentro do próprio app, funcionando em todas as versões (ver [AppLocales]).
 * A escolha recria a Activity, então `current` relido na recomposição já reflete a seleção nova.
 */
@Composable
fun LanguageScreen(onBack: () -> Unit, viewModel: SettingsViewModel = viewModel()) {
    val context = LocalContext.current
    val accent = viewModel.accentColor.collectAsStateWithLifecycle().value.color
    val current = remember { AppLocales.current(context) }

    Scaffold(topBar = { SettingsTopBar(stringResource(R.string.settings_language), onBack) }) { padding ->
        Column(
            Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()),
        ) {
            GroupSection {
                AppLocales.OPTIONS.forEachIndexed { index, tag ->
                    if (index > 0) GroupRowDivider()
                    GroupRow(
                        title = languageLabel(tag),
                        trailing = if (tag == current) ({ GroupCheck(accent) }) else null,
                        onClick = { if (tag != current) AppLocales.apply(context, tag) },
                    )
                }
            }
        }
    }
}

/** Nomes de idioma no próprio idioma (não se traduzem); só "padrão do sistema" é localizado. */
@Composable
private fun languageLabel(tag: String): String = when (tag) {
    "en" -> "English"
    "pt-BR" -> "Português (Brasil)"
    else -> stringResource(R.string.language_system_default)
}
