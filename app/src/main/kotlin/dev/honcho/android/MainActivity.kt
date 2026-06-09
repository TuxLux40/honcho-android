package dev.honcho.android

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.rememberNavController
import dev.honcho.android.ui.HonchoNavHost
import dev.honcho.android.ui.Setup
import dev.honcho.android.ui.Workspaces
import dev.honcho.android.ui.theme.HonchoTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_SECURE,
            WindowManager.LayoutParams.FLAG_SECURE
        )
        enableEdgeToEdge()
        setContent {
            HonchoTheme {
                val settings by AppModule.settingsRepository.settings
                    .collectAsStateWithLifecycle(initialValue = null)

                val navController = rememberNavController()
                var startDestinationReady by remember { mutableStateOf(false) }
                var startDestination by remember { mutableStateOf<Any>(Setup) }

                LaunchedEffect(settings) {
                    if (settings != null && !startDestinationReady) {
                        startDestination = if (settings!!.isConfigured) Workspaces else Setup
                        startDestinationReady = true
                    }
                }

                if (startDestinationReady) {
                    HonchoNavHost(
                        navController = navController,
                        startDestination = startDestination
                    )
                }
            }
        }
    }
}
