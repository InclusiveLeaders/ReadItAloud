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
import com.readitaloud.app.core.tts.TtsRepository
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
    private val ttsRepository: TtsRepository,
    private val ocrRepository: OcrRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow<AppUiState>(AppUiState.Ready)
    val uiState: StateFlow<AppUiState> = _uiState.asStateFlow()

    /**
     * Binds the CameraX preview to the given lifecycle and surface.
     * Architecture rule: speak BEFORE state update.
     *
     * NOTE: Only fires once on initial bind — not on every recomposition.
     * CameraViewfinder ensures this by calling bindCamera() in the AndroidView factory, not update.
     */
    fun bindCamera(lifecycleOwner: LifecycleOwner, surfaceProvider: Preview.SurfaceProvider) {
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
                                val text = result.text
                                val playingState = AppUiState.Reading(
                                    text,
                                    PlaybackState(status = PlaybackStatus.Playing)
                                )
                                // Architecture rule: speak BEFORE visual update
                                ttsRepository.speak(text, onDone = {
                                    ttsRepository.speak(
                                        context.getString(R.string.tts_done),
                                        onDone = {
                                            // Read speechRate from current state at completion time —
                                            // user may have adjusted it during playback (M1 fix).
                                            val speechRate = (_uiState.value as? AppUiState.Reading)
                                                ?.playback?.speechRate ?: 1.0f
                                            _uiState.value = AppUiState.Reading(
                                                text = text,
                                                playback = PlaybackState(
                                                    status = PlaybackStatus.Idle,
                                                    speechRate = speechRate
                                                )
                                            )
                                        }
                                    )
                                })
                                _uiState.value = playingState   // navigate to ReadingScreen
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
                // Wrap in Main dispatch — onError fires on the capture executor (not Main).
                viewModelScope.launch(Dispatchers.Main) {
                    Log.e(TAG, "Image capture failed", exception)
                    speakAnnouncement(context.getString(R.string.tts_capture_error))
                    _uiState.value = AppUiState.Error(exception.message ?: "Capture failed")
                }
            }
        )
    }

    /** Restarts TTS from the beginning of the current recognised text. Preserves speechRate. */
    fun hearAgain() {
        val current = _uiState.value as? AppUiState.Reading ?: return
        val text = current.text
        val speechRate = current.playback.speechRate
        // Use speak() not restart() — currentText may be "Done." after playback chain completed,
        // so we must pass the original text explicitly to reset currentText correctly.
        ttsRepository.speak(text, onDone = {
            ttsRepository.speak(context.getString(R.string.tts_done), onDone = {
                _uiState.value = AppUiState.Reading(
                    text = text,
                    playback = PlaybackState(status = PlaybackStatus.Idle, speechRate = speechRate)
                )
            })
        })
        _uiState.value = AppUiState.Reading(text, PlaybackState(status = PlaybackStatus.Playing, speechRate = speechRate))
    }

    /** Stops TTS and resets state to Ready (returns to CameraScreen). */
    fun scanNew() {
        ttsRepository.stop()
        _uiState.value = AppUiState.Ready
    }

    /** Resets state to Ready (used after NoTextFound auto-return). */
    fun resetToReady() {
        _uiState.value = AppUiState.Ready
    }

    /**
     * Stops TTS, stays on ReadingScreen, shows post-read buttons.
     * Different from scanNew() which navigates back to CameraScreen.
     */
    fun stopPlayback() {
        val current = _uiState.value as? AppUiState.Reading ?: return
        ttsRepository.stop()
        _uiState.value = current.copy(playback = current.playback.copy(status = PlaybackStatus.Idle))
    }

    /**
     * Toggles between Playing and Paused states.
     * Used by the single play/pause button on ReadingScreen.
     */
    fun togglePlayback() {
        val current = _uiState.value as? AppUiState.Reading ?: return
        when (current.playback.status) {
            PlaybackStatus.Playing -> {
                ttsRepository.pause()
                _uiState.value = current.copy(
                    playback = current.playback.copy(status = PlaybackStatus.Paused)
                )
            }
            PlaybackStatus.Paused -> {
                ttsRepository.resume()
                _uiState.value = current.copy(
                    playback = current.playback.copy(status = PlaybackStatus.Playing)
                )
            }
            PlaybackStatus.Idle -> { /* no-op — use hearAgain() from post-read state */ }
        }
    }

    /** Restarts TTS from the very beginning of the current text. */
    fun restartPlayback() {
        val current = _uiState.value as? AppUiState.Reading ?: return
        val text = current.text
        val speechRate = current.playback.speechRate
        ttsRepository.restart(onDone = {
            ttsRepository.speak(context.getString(R.string.tts_done), onDone = {
                _uiState.value = AppUiState.Reading(
                    text = text,
                    playback = PlaybackState(status = PlaybackStatus.Idle, speechRate = speechRate)
                )
            })
        })
        _uiState.value = current.copy(playback = current.playback.copy(status = PlaybackStatus.Playing))
    }

    /**
     * Adjusts speech rate by [delta] (±0.1f).
     * Clamps to [MIN_SPEECH_RATE, MAX_SPEECH_RATE]. Applies immediately if playing.
     */
    fun adjustSpeechRate(delta: Float) {
        val current = _uiState.value as? AppUiState.Reading ?: return
        val rawRate = current.playback.speechRate + delta
        val newRate = (Math.round(rawRate * 10) / 10f).coerceIn(MIN_SPEECH_RATE, MAX_SPEECH_RATE)
        ttsRepository.setSpeechRate(newRate)
        _uiState.value = current.copy(playback = current.playback.copy(speechRate = newRate))
        // Apply within 1 second during active playback: pause + resume at new rate
        if (current.playback.status == PlaybackStatus.Playing) {
            ttsRepository.pause()
            ttsRepository.resume()
        }
    }

    /**
     * Speaks an announcement via TTS before UI state updates.
     * Architecture rule: TTS must fire BEFORE visual state change.
     */
    fun speakAnnouncement(text: String) {
        ttsRepository.speak(text)
    }

    /**
     * Called when the user taps the torch toggle icon.
     * FLASHLIGHT runtime permission is requested here (on first tap only).
     * TODO Story 3.4: Implement FLASHLIGHT permission request + CameraX torch control.
     */
    fun onTorchToggleTapped() {
        // Stub — FLASHLIGHT permission requested on-demand in Story 3.4
    }

    override fun onCleared() {
        super.onCleared()
        ttsRepository.stop()
        // Note: TtsRepository is @Singleton — do NOT call shutdown() here.
        // shutdown() is called only when the Application process ends.
    }

    companion object {
        private const val TAG = "AppViewModel"
        const val MIN_SPEECH_RATE = 0.8f
        const val MAX_SPEECH_RATE = 1.4f
        const val SPEECH_RATE_STEP = 0.1f
    }
}
