package com.readitaloud.app

import com.readitaloud.app.viewmodel.AppViewModel
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Unit tests for speech rate clamping and rounding logic used in AppViewModel.adjustSpeechRate().
 * Validates Story 2.4 AC5: rate must stay within 0.8x–1.4x, in 0.1x increments.
 */
class SpeechRateTest {

    /** Replicates the clamping logic from AppViewModel.adjustSpeechRate() */
    private fun clampRate(rate: Float): Float =
        (Math.round(rate * 10) / 10f).coerceIn(
            AppViewModel.MIN_SPEECH_RATE,
            AppViewModel.MAX_SPEECH_RATE
        )

    @Test
    fun `rate clamps to minimum 0_8 when below range`() {
        assertEquals(0.8f, clampRate(0.7f), 0.001f)
        assertEquals(0.8f, clampRate(0.0f), 0.001f)
        assertEquals(0.8f, clampRate(-1.0f), 0.001f)
    }

    @Test
    fun `rate clamps to maximum 1_4 when above range`() {
        assertEquals(1.4f, clampRate(1.5f), 0.001f)
        assertEquals(1.4f, clampRate(9.9f), 0.001f)
        assertEquals(1.4f, clampRate(2.0f), 0.001f)
    }

    @Test
    fun `rate rounds to 1 decimal place`() {
        assertEquals(1.0f, clampRate(1.04f), 0.001f)
        assertEquals(1.1f, clampRate(1.05f), 0.001f)
        assertEquals(1.2f, clampRate(1.15f), 0.001f)
    }

    @Test
    fun `valid boundary rates pass through unchanged`() {
        assertEquals(0.8f, clampRate(0.8f), 0.001f)
        assertEquals(1.0f, clampRate(1.0f), 0.001f)
        assertEquals(1.4f, clampRate(1.4f), 0.001f)
    }

    @Test
    fun `step increments stay within valid range`() {
        // Simulate 6 increase steps from 1.0 — should stop at 1.4
        var rate = 1.0f
        repeat(6) { rate = clampRate(rate + AppViewModel.SPEECH_RATE_STEP) }
        assertEquals(1.4f, rate, 0.001f)
    }

    @Test
    fun `step decrements stay within valid range`() {
        // Simulate 6 decrease steps from 1.0 — should stop at 0.8
        var rate = 1.0f
        repeat(6) { rate = clampRate(rate - AppViewModel.SPEECH_RATE_STEP) }
        assertEquals(0.8f, rate, 0.001f)
    }

    @Test
    fun `all valid 0_1x increments are reachable`() {
        val expected = listOf(0.8f, 0.9f, 1.0f, 1.1f, 1.2f, 1.3f, 1.4f)
        var rate = AppViewModel.MIN_SPEECH_RATE
        val actual = mutableListOf(rate)
        while (rate < AppViewModel.MAX_SPEECH_RATE - 0.05f) {
            rate = clampRate(rate + AppViewModel.SPEECH_RATE_STEP)
            actual.add(rate)
        }
        expected.forEachIndexed { i, exp ->
            assertEquals("index $i", exp, actual[i], 0.001f)
        }
    }
}
