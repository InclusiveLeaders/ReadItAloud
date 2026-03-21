package com.readitaloud.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.compose.rememberNavController
import com.readitaloud.app.navigation.ReadItAloudNavGraph
import com.readitaloud.app.ui.theme.ReadItAloudTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ReadItAloudTheme {
                val navController = rememberNavController()
                ReadItAloudNavGraph(navController = navController)
            }
        }
    }
}
