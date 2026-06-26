package com.jonathaxs.gymnutshell.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.jonathaxs.gymnutshell.ui.util.ApplyOrientationLock
import com.jonathaxs.gymnutshell.ui.welcome.WelcomeScreen

/** Raiz do app: mostra o Welcome no primeiro uso, ou o app principal depois (reativo à flag). */
@Composable
fun RootScreen(
    viewModel: RootViewModel = viewModel(),
    pendingRoute: String? = null,
    onRouteConsumed: () -> Unit = {},
) {
    val onboarded by viewModel.onboarded.collectAsStateWithLifecycle()
    // Trava a orientação conforme a preferência (celular); o tablet fica sempre livre.
    val orientation by viewModel.orientation.collectAsStateWithLifecycle()
    ApplyOrientationLock(orientation)

    when (onboarded) {
        true -> MainScreen(pendingRoute = pendingRoute, onRouteConsumed = onRouteConsumed)
        false -> WelcomeScreen()
        null -> Box(Modifier.fillMaxSize()) {} // carregando a flag
    }
}
