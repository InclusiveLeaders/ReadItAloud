package com.readitaloud.app.core.tts

/**
 * Spoken voice prompt constants.
 *
 * SCAFFOLD NOTE: These are temporary Kotlin constants for Story 1.x scaffolding.
 * Story 2.3 MUST migrate all values to res/values/strings.xml and replace every
 * usage of these constants with context.getString(R.string.<key>) calls.
 * Hardcoded strings in Kotlin are forbidden in the final implementation.
 */
object TtsAnnouncements {
    // TODO Story 2.3: Move all to strings.xml and reference via R.string.*
    const val READY = "Ready."
    const val READING = "Reading."
    const val NO_TEXT_FOUND = "I couldn't find any text. Try moving a little closer."
    const val HOLD_STILL = "Hold still."
    const val DONE = "Done."
    const val WORKING = "Working on it."
    const val TORCH_ON = "Torch on."
    const val TORCH_OFF = "Torch off."
}
