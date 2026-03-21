package com.readitaloud.app.viewmodel

import androidx.camera.core.Preview
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import com.readitaloud.app.core.camera.CameraRepository
import com.readitaloud.app.model.AppUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class AppViewModel @Inject constructor(
    private val cameraRepository: CameraRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<AppUiState>(AppUiState.Ready)
    val uiState: StateFlow<AppUiState> = _uiState.asStateFlow()

    /**
     * Binds the CameraX preview to the given lifecycle and surface.
     * Architecture rule: speak BEFORE state update — stub until Story 2.3.
     */
    fun bindCamera(lifecycleOwner: LifecycleOwner, surfaceProvider: Preview.SurfaceProvider) {
        // TODO Story 2.3: speak R.string.tts_ready via TtsRepository BEFORE state update (architecture rule)
        _uiState.value = AppUiState.Ready
        cameraRepository.bindCamera(lifecycleOwner, surfaceProvider)
    }

    fun startCapture() {}
    fun stopPlayback() {}
    fun togglePlayback() {}

    /**
     * Speaks an announcement via TTS before UI state updates.
     * Architecture rule: TTS must fire BEFORE visual state change (200–300ms lead).
     * TODO Story 2.3: Wire to TtsRepository for actual speech output.
     */
    fun speakAnnouncement(text: String) {
        // Stub — no-op until Story 2.3 wires TtsRepository
    }

    /**
     * Called when the user taps the torch toggle icon.
     * FLASHLIGHT runtime permission is requested here (on first tap only).
     * TODO Story 3.4: Implement FLASHLIGHT permission request + CameraX torch control.
     */
    fun onTorchToggleTapped() {
        // Stub — FLASHLIGHT permission requested on-demand in Story 3.4
    }
}
