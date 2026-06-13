package com.jonathaxs.gymnutshell.ui.settings

import android.widget.ImageView
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.jonathaxs.gymnutshell.R
import com.jonathaxs.gymnutshell.ui.theme.color

/**
 * Sobre o app — porte da AboutView (iOS): ícone + versão, descrição
 * e links de contato do desenvolvedor (feedback e site).
 */
@Composable
fun AboutScreen(onBack: () -> Unit, viewModel: SettingsViewModel = viewModel()) {
    val accent = viewModel.accentColor.collectAsStateWithLifecycle().value.color
    val context = LocalContext.current
    val uriHandler = LocalUriHandler.current

    // Versão lida do PackageManager (mesma fonte do CFBundleShortVersionString no iOS).
    val versionName = remember {
        runCatching {
            context.packageManager.getPackageInfo(context.packageName, 0).versionName
        }.getOrNull() ?: "-"
    }

    Scaffold(topBar = { SettingsTopBar(stringResource(R.string.settings_about_title), onBack) }) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(Modifier.height(24.dp))

            // Ícone do launcher clipado em retângulo arredondado, no padrão do iOS.
            AppIcon()

            Spacer(Modifier.height(12.dp))
            Text(
                "${stringResource(R.string.settings_about_version)} $versionName",
                style = MaterialTheme.typography.bodyMedium,
                color = accent,
            )

            Spacer(Modifier.height(16.dp))
            Text(
                stringResource(R.string.settings_about_description),
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 24.dp),
            )

            Spacer(Modifier.height(24.dp))

            // Links de contato: feedback primeiro, depois site (mesma ordem do iOS).
            SectionHeader(stringResource(R.string.settings_about_developer_header))
            LinkRow(stringResource(R.string.settings_about_feedback), accent) {
                uriHandler.openUri("mailto:jonathasmrt@me.com")
            }
            HorizontalDivider()
            LinkRow(stringResource(R.string.settings_about_website), accent) {
                uriHandler.openUri("https://jonathasmotta.com")
            }
        }
    }
}

/** Ícone do app via PackageManager (suporta adaptive icon, que o painterResource não desenha). */
@Composable
private fun AppIcon() {
    val context = LocalContext.current
    val iconLabel = stringResource(R.string.a11y_app_icon)
    val drawable = remember { context.packageManager.getApplicationIcon(context.packageName) }
    AndroidView(
        factory = { ImageView(it).apply { setImageDrawable(drawable) } },
        modifier = Modifier
            .size(96.dp)
            .clip(RoundedCornerShape(21.dp))
            .semantics { contentDescription = iconLabel },
    )
}

/** Header de seção alinhado à esquerda, mesmo estilo da lista raiz. */
@Composable
private fun SectionHeader(title: String) {
    Text(
        title,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, bottom = 4.dp),
    )
}

/** Linha de link colorida pela accent, espelha os Links da AboutView do iOS. */
@Composable
private fun LinkRow(title: String, accent: Color, onClick: () -> Unit) {
    Text(
        title,
        style = MaterialTheme.typography.bodyLarge,
        color = accent,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 16.dp),
    )
}
