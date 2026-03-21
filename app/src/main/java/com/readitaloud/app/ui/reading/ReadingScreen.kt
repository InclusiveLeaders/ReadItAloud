package com.readitaloud.app.ui.reading

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.readitaloud.app.viewmodel.AppViewModel

@Composable
fun ReadingScreen(
    appViewModel: AppViewModel,
    onNavigateBack: () -> Unit
) {
    val uiState by appViewModel.uiState.collectAsStateWithLifecycle()
    Text(text = "Reading Screen")
}
