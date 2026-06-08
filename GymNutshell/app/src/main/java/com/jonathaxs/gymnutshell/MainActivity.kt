package com.jonathaxs.gymnutshell

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.jonathaxs.gymnutshell.core.data.NotificationPreferencesRepository
import com.jonathaxs.gymnutshell.ui.RootScreen
import com.jonathaxs.gymnutshell.ui.theme.GymNutshellTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    // Resultado do pedido de permissão de notificações (Android 13+). Ao conceder, aplica os defaults.
    private val requestNotificationsPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) applyNotificationDefaults()
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        maybeRequestNotificationsPermission()
        setContent {
            GymNutshellTheme {
                RootScreen()
            }
        }
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
        }
    }
}
