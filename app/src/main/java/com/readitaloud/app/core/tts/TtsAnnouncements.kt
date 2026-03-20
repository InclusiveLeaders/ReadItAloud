package com.readitaloud.app.core.tts

// All voice prompt strings — load from strings.xml in Story 2.3
// CRITICAL: Never hardcode strings in Kotlin — always reference R.string.*
object TtsAnnouncements {
    const val READY = "Ready."
    const val READING = "Reading."
    const val NO_TEXT_FOUND = "I couldn't find any text. Try moving a little closer."
    const val HOLD_STILL = "Hold still."
    const val DONE = "Done."
    const val WORKING = "Working on it."
    const val TORCH_ON = "Torch on."
    const val TORCH_OFF = "Torch off."
}
