package com.jonathaxs.gymnutshell

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.jonathaxs.gymnutshell.core.data.NotificationPreferencesRepository
import com.jonathaxs.gymnutshell.notifications.GymNotifier
import com.jonathaxs.gymnutshell.notifications.NotificationScheduler
import com.jonathaxs.gymnutshell.ui.RootScreen
import com.jonathaxs.gymnutshell.ui.theme.GymNutshellTheme
import com.jonathaxs.gymnutshell.wear.PhoneWearSync
import com.jonathaxs.gymnutshell.widget.GymWidgets
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    // Rota pendente vinda do toque numa notificação (deep-link), observada pelo Compose.
    private var pendingRoute by mutableStateOf<String?>(null)

    // Resultado do pedido de permissão de notificações (Android 13+). Ao conceder, aplica os defaults.
    private val requestNotificationsPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) applyNotificationDefaults()
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        maybeRequestNotificationsPermission()
        pendingRoute = intent?.getStringExtra(GymNotifier.EXTRA_ROUTE)
        setContent {
            GymNutshellTheme {
                RootScreen(
                    pendingRoute = pendingRoute,
                    onRouteConsumed = { pendingRoute = null },
                )
            }
        }
    }

    // App já aberto (launchMode singleTop): captura a rota do novo Intent.
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        pendingRoute = intent.getStringExtra(GymNotifier.EXTRA_ROUTE)
    }

    // Ao sair do app, atualiza os widgets e publica o snapshot pro relógio com o
    // estado mais recente — espelha o "scenePhase background" do iOS.
    override fun onStop() {
        super.onStop()
        GymWidgets.update(applicationContext)
        PhoneWearSync.push(applicationContext)
    }

    /**
     * Pede POST_NOTIFICATIONS no Android 13+; em versões anteriores não há permissão runtime.
     * (A tela de Ajustes de Notificações será o lugar "oficial" do pedido na fatia 5A-4.)
     */
    private fun maybeRequestNotificationsPermission() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            if (NotificationManagerCompat.from(this).areNotificationsEnabled()) applyNotificationDefaults()
            return
        }
        val granted = ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) ==
            PackageManager.PERMISSION_GRANTED
        if (granted) {
            applyNotificationDefaults()
        } else {
            requestNotificationsPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    private fun applyNotificationDefaults() {
        lifecycleScope.launch {
            NotificationPreferencesRepository(applicationContext).applyDefaultEnabledKindsOnce()
            // Re-arma os lembretes por intervalo a cada launch (cobre a virada de dia).
            NotificationScheduler(applicationContext).rescheduleAllActive()
        }
    }
}
