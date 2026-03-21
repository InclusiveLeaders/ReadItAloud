package com.readitaloud.app.ui.settings

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.readitaloud.app.viewmodel.SettingsViewModel

@Composable
fun SettingsScreen(
    settingsViewModel: SettingsViewModel,
    onNavigateBack: () -> Unit
) {
    Text(text = "Settings Screen")
}
