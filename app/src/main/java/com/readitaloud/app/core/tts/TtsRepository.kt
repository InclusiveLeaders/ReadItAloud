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
                            // Capture and clear BEFORE invoking — nested speak() calls inside
                            // the callback must be able to set pendingOnDone for the next utterance
                            val callback = pendingOnDone
                            pendingOnDone = null
                            callback?.invoke()
                        }
                    }
                    @Deprecated("Deprecated in API 21")
                    override fun onError(utteranceId: String?) {}
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
        pendingOnDone = onDone
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, UTTERANCE_ID)
    }

    /** Stops any currently playing TTS immediately. */
    fun stop() {
        pendingOnDone = null
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
