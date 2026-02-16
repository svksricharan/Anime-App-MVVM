package com.svksricharan.animeapp.ui.main

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import androidx.navigation.compose.rememberNavController
import com.svksricharan.animeapp.AnimeApplication
import com.svksricharan.animeapp.ui.navigation.AppNavigation
import com.svksricharan.animeapp.ui.theme.AnimeAppTheme

// Single activity — all screens are Compose destinations managed by Navigation Compose
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Edge-to-edge — lets content draw behind the status bar for a modern look
        WindowCompat.setDecorFitsSystemWindows(window, false)

        // Grab the DI container that was initialized in AnimeApplication
        val appContainer = (application as AnimeApplication).appContainer

        setContent {
            AnimeAppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    AppNavigation(
                        navController = navController,
                        appContainer = appContainer
                    )
                }
            }
        }
    }
}
