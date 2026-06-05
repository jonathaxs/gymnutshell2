package com.jonathaxs.gymnutshell

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.jonathaxs.gymnutshell.ui.MainScreen
import com.jonathaxs.gymnutshell.ui.theme.GymNutshellTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            GymNutshellTheme {
                MainScreen()
            }
        }
    }
}
