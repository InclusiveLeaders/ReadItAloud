package com.readitaloud.app

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Unit tests for the rangeOffset logic introduced in Story 3.1 (pause/resume fix).
 *
 * TtsRepository.resume() speaks a substring of the original text (from pausedAtChar onwards).
 * onRangeStart() positions are relative to that substring, not the original full text.
 * rangeOffset accumulates the substring start position on each resume so that
 * highlight positions map back correctly to the full original text shown in PlaybackTextDisplay.
 *
 * These tests validate the offset arithmetic in isolation (pure Kotlin, no Android deps).
 */
class WordRangeOffsetTest {

    // Simulates TtsRepository.onRangeStart offset application
    private fun applyOffset(start: Int, end: Int, offset: Int): IntRange =
        (start + offset) until (end + offset)

    @Test
    fun `no pause - offset is zero - range unchanged`() {
        val rangeOffset = 0
        val result = applyOffset(start = 4, end = 9, offset = rangeOffset)
        assertEquals(4..8, result)
    }

    @Test
    fun `single pause at char 50 - first word of resumed text maps to correct position`() {
        // Original: "The quick brown fox..." paused at char 50
        // Resume speaks substring from 50: "fox jumped over..."
        // onRangeStart fires with start=0, end=3 for "fox"
        val rangeOffset = 50
        val result = applyOffset(start = 0, end = 3, offset = rangeOffset)
        assertEquals(50..52, result)   // "fox" is at chars 50-52 in original text
    }

    @Test
    fun `single pause mid-word - range maps correctly into original text`() {
        val rangeOffset = 25
        val result = applyOffset(start = 5, end = 10, offset = rangeOffset)
        assertEquals(30..34, result)
    }

    @Test
    fun `double pause - offsets accumulate correctly`() {
        // First resume: paused at char 50, rangeOffset = 50
        // Second pause within resumed text at char 20 (= char 70 in original)
        // Second resume: rangeOffset = 50 + 20 = 70
        var rangeOffset = 0
        val firstPausedAtChar = 50
        rangeOffset += firstPausedAtChar    // after first resume: offset = 50

        val secondPausedAtChar = 20
        rangeOffset += secondPausedAtChar   // after second resume: offset = 70

        // onRangeStart fires with start=0 for first word of second resumed segment
        val result = applyOffset(start = 0, end = 5, offset = rangeOffset)
        assertEquals(70..74, result)
    }

    @Test
    fun `offset reset on new speak - next utterance starts from position 0`() {
        var rangeOffset = 50   // had been accumulated from previous session
        rangeOffset = 0        // speak() resets to 0

        val result = applyOffset(start = 0, end = 4, offset = rangeOffset)
        assertEquals(0..3, result)
    }

    @Test
    fun `mid-text word range with offset is correct`() {
        val rangeOffset = 100
        val result = applyOffset(start = 15, end = 22, offset = rangeOffset)
        assertEquals(115..121, result)
    }
}
