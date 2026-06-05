package com.jonathaxs.gymnutshell.ui.theme

import androidx.compose.ui.graphics.Color
import com.jonathaxs.gymnutshell.core.theme.AccentColor

/**
 * Converte a AccentColor (valor cru ARGB no :core) pro Color do Compose.
 * Mantém o :core livre de UI: a ponte pro toolkit vive aqui na camada do app.
 */
val AccentColor.color: Color
    get() = Color(argb)
