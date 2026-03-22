package com.readitaloud.app.model

/**
 * Represents TTS playback state.
 * Three distinct states are required: Idle (stopped/not started), Playing, Paused.
 * A simple Boolean cannot express all three — do not collapse to isPlaying.
 */
data class PlaybackState(
    val status: PlaybackStatus = PlaybackStatus.Idle,
    val speechRate: Float = 1.0f,
    val currentWordRange: IntRange = IntRange.EMPTY   // Story 3.1: drives PlaybackTextDisplay highlighting
)

enum class PlaybackStatus {
    Idle,    // TTS not started, or fully stopped (position reset)
    Playing, // TTS actively speaking
    Paused   // TTS paused mid-speech (position preserved for resume)
}
