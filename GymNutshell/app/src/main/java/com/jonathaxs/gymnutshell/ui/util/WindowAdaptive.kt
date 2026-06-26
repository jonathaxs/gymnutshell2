package com.jonathaxs.gymnutshell.ui.util

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.pm.ActivityInfo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.jonathaxs.gymnutshell.core.domain.AppOrientation

/**
 * Limiares de adaptação de layout — mesmos valores do iOS (que compara `geo.size` contra 700/500 pt).
 * `WideThreshold` decide telas largas (tablet ou celular na horizontal); `TallThreshold` decide se,
 * numa tela larga, ainda sobra altura pra empilhar o hero (tablet) em vez do layout enxuto (celular landscape).
 */
object Breakpoints {
    val WideThreshold: Dp = 700.dp
    val TallThreshold: Dp = 500.dp

    /** Largura mínima (smallest width) que classifica o dispositivo como tablet (convenção Android sw600dp). */
    const val TabletSmallestWidthDp = 600
}

/** True quando o menor lado da tela é de tablet (≥ sw600dp) — o tablet nunca trava a orientação. */
@Composable
fun rememberIsTablet(): Boolean =
    LocalConfiguration.current.smallestScreenWidthDp >= Breakpoints.TabletSmallestWidthDp

/**
 * Aplica o travamento de orientação à Activity hospedeira — porte do OrientationLockManager (iOS).
 * No tablet ignora a preferência e deixa sempre livre; no celular respeita a escolha do usuário
 * (Portrait trava na vertical; Both aceita ambas, seguindo o auto-rotate do sistema).
 */
@Composable
fun ApplyOrientationLock(orientation: AppOrientation) {
    val context = LocalContext.current
    val isTablet = rememberIsTablet()
    DisposableEffect(orientation, isTablet) {
        val activity = context.findActivity()
        activity?.requestedOrientation = when {
            isTablet -> ActivityInfo.SCREEN_ORIENTATION_FULL_USER
            orientation == AppOrientation.Portrait -> ActivityInfo.SCREEN_ORIENTATION_USER_PORTRAIT
            else -> ActivityInfo.SCREEN_ORIENTATION_USER
        }
        onDispose { }
    }
}

/** Sobe a cadeia de ContextWrapper até achar a Activity hospedeira (ou null fora dela). */
private fun Context.findActivity(): Activity? {
    var ctx: Context? = this
    while (ctx is ContextWrapper) {
        if (ctx is Activity) return ctx
        ctx = ctx.baseContext
    }
    return null
}
