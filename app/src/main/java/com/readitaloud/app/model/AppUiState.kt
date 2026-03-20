package com.readitaloud.app.model

/**
 * Single source of truth for app state machine.
 * CRITICAL: Do NOT add, remove, or rename states without updating ALL when expressions that consume this.
 */
sealed class AppUiState {
    object Ready : AppUiState()
    object Capturing : AppUiState()
    object Processing : AppUiState()
    data class Reading(val text: String, val playback: PlaybackState) : AppUiState()
    object NoTextFound : AppUiState()
    data class Error(val message: String) : AppUiState()
}
