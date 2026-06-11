package com.jonathaxs.gymnutshell.wear

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.wear.compose.material3.MaterialTheme

/**
 * Entry point do app Wear OS — porte do GymNutshellWatchApp (iOS).
 * Nas próximas fatias ganha a ativação do Data Layer (equivalente ao
 * WCSession.activate() do WatchConnectivity).
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                WearRootScreen()
            }
        }
    }
}
