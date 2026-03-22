package com.readitaloud.app.core.tts

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TtsRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private var tts: TextToSpeech? = null
    private var isInitialised = false
    private var pendingOnDone: (() -> Unit)? = null

    // Story 2.4: pause/resume/speech-rate state
    private var currentText: String = ""
    @Volatile private var pausedAtChar: Int = 0   // written by TTS BG thread (onRangeStart), read on Main
    private var currentSpeechRate: Float = 1.0f
    private var isPaused: Boolean = false          // guards onDone from firing while paused

    init {
        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                isInitialised = true
                tts?.language = Locale.getDefault()   // AC2: locale-matched voice
                tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                    override fun onStart(utteranceId: String?) {}
                    override fun onDone(utteranceId: String?) {
                        // onDone may fire on a background thread — use Handler to post to main
                        Handler(Looper.getMainLooper()).post {
                            // Guard: some OEM builds fire onDone() when stop() is called rather
                            // than onStop(). isPaused blocks premature callback execution.
                            if (isPaused) return@post
                            // Capture and clear BEFORE invoking — nested speak() calls inside
                            // the callback must be able to set pendingOnDone for the next utterance
                            val callback = pendingOnDone
                            pendingOnDone = null
                            callback?.invoke()
                        }
                    }
                    @Deprecated("Deprecated in API 21")
                    override fun onError(utteranceId: String?) {}

                    // Explicit no-op: stop() during pause fires onStop() on standard Android.
                    // isPaused guards onDone as a secondary defence for OEM variants.
                    override fun onStop(utteranceId: String?, interrupted: Boolean) {}

                    // Story 2.4: track char position for pause/resume (API 26+ — matches minSdk)
                    override fun onRangeStart(utteranceId: String?, start: Int, end: Int, frame: Int) {
                        pausedAtChar = start
                    }
                })
            }
        }
    }

    /**
     * Speaks [text] immediately, interrupting any currently playing TTS.
     * [onDone] is called on the main thread when utterance finishes.
     * Architecture rule: caller must invoke speak() BEFORE updating AppUiState.
     */
    fun speak(text: String, onDone: (() -> Unit)? = null) {
        if (!isInitialised) {
            Log.w("TtsRepository", "speak() called before TTS initialised — ignoring")
            return
        }
        currentText = text
        pausedAtChar = 0
        pendingOnDone = onDone
        tts?.setSpeechRate(currentSpeechRate)
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, UTTERANCE_ID)
    }

    /**
     * Pauses TTS by stopping the engine.
     * Preserves pendingOnDone for resume — DO NOT null it here.
     * pausedAtChar is already tracked via onRangeStart.
     */
    fun pause() {
        isPaused = true    // must be set BEFORE stop() so onDone guard fires if OEM triggers it
        tts?.stop()
        // pendingOnDone intentionally NOT cleared — preserved for resume() → onDone chain
    }

    /**
     * Resumes TTS from the last known character position (set by onRangeStart).
     * If no position saved, speaks from beginning of currentText.
     *
     * H1 fix: currentText is updated to resumeText so that any subsequent onRangeStart
     * callbacks report positions relative to the text actually being spoken.
     * Without this, a second pause would compute the wrong substring on next resume.
     */
    fun resume() {
        if (!isInitialised) return
        val resumeText = if (pausedAtChar > 0 && pausedAtChar < currentText.length) {
            currentText.substring(pausedAtChar)
        } else {
            currentText
        }
        isPaused = false      // clear BEFORE speak so onDone fires normally when this utterance ends
        currentText = resumeText  // track the text now being spoken (fixes double-pause position)
        pausedAtChar = 0          // reset position tracker for the resumed segment
        tts?.setSpeechRate(currentSpeechRate)
        tts?.speak(resumeText, TextToSpeech.QUEUE_FLUSH, null, UTTERANCE_ID)
        // pendingOnDone still set from original speak() — onDone fires normally when done
    }

    /**
     * Restarts TTS from the beginning of currentText.
     * [onDone] overrides pendingOnDone only when non-null (e.g. restartPlayback / hearAgain).
     */
    fun restart(onDone: (() -> Unit)? = null) {
        if (!isInitialised) return
        pausedAtChar = 0
        if (onDone != null) pendingOnDone = onDone
        tts?.setSpeechRate(currentSpeechRate)
        tts?.speak(currentText, TextToSpeech.QUEUE_FLUSH, null, UTTERANCE_ID)
    }

    /**
     * Sets speech rate for all subsequent speak()/resume()/restart() calls.
     * Rate validation (0.8–1.4, 0.1 increments) is the caller's responsibility.
     */
    fun setSpeechRate(rate: Float) {
        currentSpeechRate = rate
        tts?.setSpeechRate(rate)
    }

    /** Stops any currently playing TTS immediately. Clears all pending state. */
    fun stop() {
        isPaused = false
        pendingOnDone = null
        pausedAtChar = 0
        tts?.stop()
    }

    /** Releases TTS engine resources. Call when app is closing. */
    fun shutdown() {
        pendingOnDone = null
        tts?.shutdown()
        tts = null
        isInitialised = false
    }

    companion object {
        private const val UTTERANCE_ID = "ria_utterance"
    }
}
