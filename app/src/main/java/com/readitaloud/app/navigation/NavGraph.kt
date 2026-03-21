package com.readitaloud.app.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.readitaloud.app.ui.camera.CameraScreen
import com.readitaloud.app.ui.reading.ReadingScreen
import com.readitaloud.app.ui.settings.SettingsScreen
import com.readitaloud.app.viewmodel.AppViewModel
import androidx.hilt.navigation.compose.hiltViewModel
import com.readitaloud.app.viewmodel.SettingsViewModel

@Composable
fun ReadItAloudNavGraph(navController: NavHostController) {
    // AppViewModel scoped to nav graph — shared across CameraScreen and ReadingScreen
    val appViewModel: AppViewModel = hiltViewModel()

    NavHost(navController = navController, startDestination = Routes.CAMERA) {
        composable(Routes.CAMERA) {
            CameraScreen(
                appViewModel = appViewModel,
                onNavigateToReading = { navController.navigate(Routes.READING) },
                onNavigateToSettings = {
                    navController.navigate(Routes.SETTINGS) {
                        launchSingleTop = true
                    }
                }
            )
        }
        composable(Routes.READING) {
            ReadingScreen(
                appViewModel = appViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(Routes.SETTINGS) {
            val settingsViewModel: SettingsViewModel = hiltViewModel()
            SettingsScreen(
                settingsViewModel = settingsViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
