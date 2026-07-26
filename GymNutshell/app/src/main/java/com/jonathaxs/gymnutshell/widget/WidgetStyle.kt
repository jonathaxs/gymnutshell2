package com.jonathaxs.gymnutshell.widget

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.ImageProvider
import androidx.glance.background
import androidx.glance.unit.ColorProvider
import com.jonathaxs.gymnutshell.core.widget.WidgetBackground
import com.jonathaxs.gymnutshell.core.widget.WidgetBackgroundMode
import com.jonathaxs.gymnutshell.core.widget.WidgetSnapshot

/**
 * Estilo de fundo/texto dos widgets conforme o modo escolhido — espelha a lógica do iOS.
 * Padrão: fundo do sistema + texto onSurface. Destaque/Personalizado: gradiente (Bitmap) na cor
 * escolhida + texto contrastante (preto/branco por luminância).
 */

/** Modifier de fundo pra raiz do widget (cor do sistema ou gradiente na cor accent/custom). */
@Composable
fun widgetBackgroundModifier(snapshot: WidgetSnapshot): GlanceModifier = when (snapshot.backgroundMode) {
    WidgetBackgroundMode.System ->
        GlanceModifier.background(GlanceTheme.colors.widgetBackground)
    WidgetBackgroundMode.Accent ->
        GlanceModifier.background(ImageProvider(WidgetGradient.bitmap(snapshot.accentArgb)))
    WidgetBackgroundMode.Custom ->
        GlanceModifier.background(ImageProvider(WidgetGradient.bitmap(snapshot.customBackgroundArgb)))
}

/** Cor de texto que contrasta com o fundo atual do widget. */
@Composable
fun widgetTextColor(snapshot: WidgetSnapshot): ColorProvider = when (snapshot.backgroundMode) {
    WidgetBackgroundMode.System -> GlanceTheme.colors.onSurface
    WidgetBackgroundMode.Accent -> ColorProvider(Color(WidgetBackground.contrastingTextArgb(snapshot.accentArgb)))
    WidgetBackgroundMode.Custom -> ColorProvider(Color(WidgetBackground.contrastingTextArgb(snapshot.customBackgroundArgb)))
}

/**
 * Cor (ARGB) do contorno de contraste dos elementos — anel, barras, células — sobre fundos custom/accent,
 * onde a cor do elemento pode se confundir com o gradiente. `null` no modo System, onde o fundo do sistema
 * já contrasta e o iOS também não desenha borda (regra `needsBorder = mode != .system`).
 */
fun widgetBorderArgb(snapshot: WidgetSnapshot): Long? = when (snapshot.backgroundMode) {
    WidgetBackgroundMode.System -> null
    WidgetBackgroundMode.Accent -> WidgetBackground.contrastingTextArgb(snapshot.accentArgb)
    WidgetBackgroundMode.Custom -> WidgetBackground.contrastingTextArgb(snapshot.customBackgroundArgb)
}
