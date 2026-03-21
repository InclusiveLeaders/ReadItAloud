package com.readitaloud.app.viewmodel

import androidx.lifecycle.ViewModel
import com.readitaloud.app.model.AppUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class AppViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow<AppUiState>(AppUiState.Ready)
    val uiState: StateFlow<AppUiState> = _uiState.asStateFlow()

    fun startCapture() {}
    fun stopPlayback() {}
    fun togglePlayback() {}
}
