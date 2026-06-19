package com.jonathaxs.gymnutshell.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

// Fundo da tela acinzentado + cards/superfícies brancos (estilo grouped do iOS).
// Os Card (filled) e bottom sheets usam os surfaceContainer*, então deixamos todos brancos no light.
private val DarkColorScheme = darkColorScheme(
    primary = Purple80,
    secondary = PurpleGrey80,
    tertiary = Pink80,
    background = GroupedBackgroundDark,
    surface = GroupedBackgroundDark,
    surfaceContainerLowest = GroupedSurfaceDark,
    surfaceContainerLow = GroupedSurfaceDark,
    surfaceContainer = GroupedSurfaceDark,
    surfaceContainerHigh = GroupedSurfaceHighDark,
    surfaceContainerHighest = GroupedSurfaceHighDark,
)

private val LightColorScheme = lightColorScheme(
    primary = Purple40,
    secondary = PurpleGrey40,
    tertiary = Pink40,
    background = GroupedBackgroundLight,
    surface = GroupedBackgroundLight,
    surfaceContainerLowest = GroupedSurfaceLight,
    surfaceContainerLow = GroupedSurfaceLight,
    surfaceContainer = GroupedSurfaceLight,
    surfaceContainerHigh = GroupedSurfaceLight,
    surfaceContainerHighest = GroupedSurfaceLight,
)

@Composable
fun GymNutshellTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Desligado: o Gym Nutshell tem identidade visual própria (cores de destaque),
    // então não adotamos a paleta Material You do sistema (Android 12+).
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}