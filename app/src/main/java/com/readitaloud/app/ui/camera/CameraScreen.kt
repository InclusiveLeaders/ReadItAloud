package com.readitaloud.app.ui.camera

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.readitaloud.app.viewmodel.AppViewModel

@Composable
fun CameraScreen(
    appViewModel: AppViewModel,
    onNavigateToReading: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
    val uiState by appViewModel.uiState.collectAsStateWithLifecycle()
    Text(text = "Camera Screen")
}
