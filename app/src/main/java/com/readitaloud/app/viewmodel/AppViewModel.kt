package com.readitaloud.app.viewmodel

import android.content.Context
import android.util.Log
import androidx.camera.core.Preview
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.readitaloud.app.R
import com.readitaloud.app.core.camera.CameraRepository
import com.readitaloud.app.core.ocr.OcrRepository
import com.readitaloud.app.model.AppUiState
import com.readitaloud.app.model.PlaybackState
import com.readitaloud.app.model.PlaybackStatus
import com.readitaloud.app.model.ScanResult
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class AppViewModel @Inject constructor(
    private val cameraRepository: CameraRepository,
    private val ocrRepository: OcrRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow<AppUiState>(AppUiState.Ready)
    val uiState: StateFlow<AppUiState> = _uiState.asStateFlow()

    /**
     * Binds the CameraX preview to the given lifecycle and surface.
     * Architecture rule: speak BEFORE state update — stub until Story 2.3.
     */
    fun bindCamera(lifecycleOwner: LifecycleOwner, surfaceProvider: Preview.SurfaceProvider) {
        // TODO Story 2.3: speak R.string.tts_ready via TtsRepository BEFORE state update (architecture rule)
        speakAnnouncement(context.getString(R.string.tts_ready))
        _uiState.value = AppUiState.Ready
        cameraRepository.bindCamera(lifecycleOwner, surfaceProvider)
    }

    /**
     * Initiates image capture and on-device OCR pipeline.
     * Architecture rule: speak BEFORE state update.
     * Guard: ignores taps when not in Ready state.
     */
    fun startCapture() {
        if (_uiState.value !is AppUiState.Ready) return   // guard: ignore taps when not Ready

        speakAnnouncement(context.getString(R.string.tts_reading))   // speak BEFORE state update
        _uiState.value = AppUiState.Capturing

        val executor = ContextCompat.getMainExecutor(context)
        cameraRepository.captureImage(
            executor = executor,
            onSuccess = { imageProxy ->
                _uiState.value = AppUiState.Processing
                viewModelScope.launch(Dispatchers.IO) {
                    val result = ocrRepository.recogniseText(imageProxy)
                    withContext(Dispatchers.Main) {
                        when (result) {
                            is ScanResult.Success -> {
                                // Story 2.3 wires full TTS playback; for now transition to Reading state
                                _uiState.value = AppUiState.Reading(
                                    text = result.text,
                                    playback = PlaybackState(status = PlaybackStatus.Playing)
                                )
                            }
                            is ScanResult.NoTextFound -> {
                                speakAnnouncement(context.getString(R.string.tts_no_text_found))
                                _uiState.value = AppUiState.NoTextFound
                            }
                            is ScanResult.BlurDetected -> {
                                // Story 5.1 handles blur detection — treat as NoTextFound for now
                                _uiState.value = AppUiState.NoTextFound
                            }
                            is ScanResult.Error -> {
                                speakAnnouncement(context.getString(R.string.tts_capture_error))
                                _uiState.value = AppUiState.Error(result.message)
                            }
                        }
                    }
                }
            },
            onError = { exception ->
                Log.e(TAG, "Image capture failed", exception)
                speakAnnouncement(context.getString(R.string.tts_capture_error))
                _uiState.value = AppUiState.Error(exception.message ?: "Capture failed")
            }
        )
    }

    /** Resets state to Ready (used after NoTextFound auto-return). */
    fun resetToReady() {
        _uiState.value = AppUiState.Ready
    }

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

    companion object {
        private const val TAG = "AppViewModel"
    }
}
