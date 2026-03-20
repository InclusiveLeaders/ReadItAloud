package com.readitaloud.app.viewmodel

import androidx.lifecycle.ViewModel
import com.readitaloud.app.model.AppUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

// TODO Story 1.3: Add @HiltViewModel + @Inject constructor with CameraRepository, OcrRepository, TtsRepository
class AppViewModel : ViewModel() {

    private val _uiState = MutableStateFlow<AppUiState>(AppUiState.Ready)
    val uiState: StateFlow<AppUiState> = _uiState.asStateFlow()
}
