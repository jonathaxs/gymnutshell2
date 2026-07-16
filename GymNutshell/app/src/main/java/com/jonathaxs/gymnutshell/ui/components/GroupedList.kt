package com.jonathaxs.gymnutshell.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.jonathaxs.gymnutshell.R

/**
 * Lista agrupada moderna (estilo Ajustes do Android / inset-grouped do iOS): cards arredondados
 * com inset lateral sobre o fundo acinzentado do tema, mais respiro e divisores recuados.
 *
 * O tema já define `background`/`surface` cinza e `surfaceContainer*` claros, então basta envolver
 * as linhas num [GroupCard] pra ter o visual de painel branco sobre fundo cinza (e elevado no escuro).
 * Usado tanto nas telas de Settings quanto na Achievements.
 */

/** Inset lateral dos cards e raio dos cantos — tokens do visual agrupado. */
val GroupInset = 16.dp
private val GroupCorner = 22.dp
private val RowMinHeight = 56.dp
private val RowPaddingH = 20.dp
private val RowPaddingV = 12.dp

/** Card arredondado que agrupa um conjunto de linhas. */
@Composable
fun GroupCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Surface(
        modifier = modifier.fillMaxWidth().padding(horizontal = GroupInset),
        shape = RoundedCornerShape(GroupCorner),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
    ) {
        Column(content = content)
    }
}

/**
 * Seção: título opcional acima + [GroupCard] com as linhas + rodapé opcional. Já aplica o
 * espaçamento superior entre seções. `titleColor` costuma ser a cor de destaque (parity com iOS).
 */
@Composable
fun GroupSection(
    modifier: Modifier = Modifier,
    title: String? = null,
    titleColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    footer: String? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(modifier.fillMaxWidth().padding(top = 22.dp)) {
        if (title != null) {
            Text(
                title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = titleColor,
                modifier = Modifier.padding(start = GroupInset + RowPaddingH, end = GroupInset, bottom = 8.dp),
            )
        }
        GroupCard(content = content)
        if (footer != null) {
            Text(
                footer,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = GroupInset + RowPaddingH, end = GroupInset + RowPaddingH, top = 8.dp),
            )
        }
    }
}

/**
 * Linha de uma seção agrupada: leading opcional, título + subtítulo, trailing opcional e/ou chevron.
 * Clicável quando [onClick] != null. As linhas vivem dentro de um [GroupCard], então o ripple já
 * respeita os cantos arredondados.
 */
@Composable
fun GroupRow(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    titleColor: Color = MaterialTheme.colorScheme.onSurface,
    leading: (@Composable () -> Unit)? = null,
    trailing: (@Composable () -> Unit)? = null,
    showChevron: Boolean = false,
    enabled: Boolean = true,
    onClick: (() -> Unit)? = null,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            // `clickable` já funde os filhos, então o TalkBack lê a linha inteira de uma vez e
            // anuncia "toque duas vezes para ativar" pelo Role.Button.
            .then(
                if (onClick != null) Modifier.clickable(enabled = enabled, role = Role.Button, onClick = onClick)
                else Modifier,
            )
            .heightIn(min = RowMinHeight)
            .padding(horizontal = RowPaddingH, vertical = RowPaddingV),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (leading != null) {
            leading()
            Spacer(Modifier.width(16.dp))
        }
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                title,
                style = MaterialTheme.typography.bodyLarge,
                color = if (enabled) titleColor else MaterialTheme.colorScheme.onSurfaceVariant,
            )
            if (subtitle != null) {
                Text(
                    subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        if (trailing != null) {
            Spacer(Modifier.width(12.dp))
            trailing()
        }
        if (showChevron) {
            Spacer(Modifier.width(8.dp))
            Text(
                "›",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                // Decorativo: o Role.Button da linha já diz que ela navega; ler "›" só polui.
                modifier = Modifier.clearAndSetSemantics {},
            )
        }
    }
}

/** Divisor entre linhas, recuado pra alinhar com o conteúdo (estilo inset). */
@Composable
fun GroupRowDivider(startInset: Dp = RowPaddingH) {
    HorizontalDivider(
        modifier = Modifier.padding(start = startInset),
        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
    )
}

/**
 * Checkmark na cor de destaque, usado em linhas de seleção. Vira "Selected" no TalkBack — a linha
 * é lida como "Portrait, Selected" em vez do "✓" cru.
 */
@Composable
fun GroupCheck(color: Color) {
    val selectedDesc = stringResource(R.string.a11y_selected)
    Text(
        "✓",
        color = color,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.semantics { contentDescription = selectedDesc },
    )
}
