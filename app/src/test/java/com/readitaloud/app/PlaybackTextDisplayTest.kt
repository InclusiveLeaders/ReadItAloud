package com.readitaloud.app

import com.readitaloud.app.ui.components.resolveHighlightBounds
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * Unit tests for [resolveHighlightBounds] — the pure-Kotlin range validation logic extracted
 * from PlaybackTextDisplay to be testable without Compose/Android instrumentation (M4 review fix).
 *
 * Covers: empty text, IntRange.EMPTY, negative start, out-of-bounds start, clamped end,
 * highlight at index 0, highlight at last char, valid mid-text range.
 */
class PlaybackTextDisplayTest {

    @Test
    fun `empty text returns null`() {
        assertNull(resolveHighlightBounds(textLength = 0, range = 0..5))
    }

    @Test
    fun `IntRange EMPTY returns null`() {
        assertNull(resolveHighlightBounds(textLength = 100, range = IntRange.EMPTY))
    }

    @Test
    fun `start equal to text length returns null`() {
        assertNull(resolveHighlightBounds(textLength = 10, range = 10..15))
    }

    @Test
    fun `start beyond text length returns null`() {
        assertNull(resolveHighlightBounds(textLength = 5, range = 10..15))
    }

    @Test
    fun `negative start returns null`() {
        assertNull(resolveHighlightBounds(textLength = 10, range = -1..3))
    }

    @Test
    fun `valid mid-text range returns correct inclusive start and exclusive end`() {
        val result = resolveHighlightBounds(textLength = 20, range = 5..9)
        assertNotNull(result)
        assertEquals(5, result!!.first)
        assertEquals(10, result.second)   // last + 1 = exclusive end
    }

    @Test
    fun `highlight starting at index 0 returns correct bounds`() {
        val result = resolveHighlightBounds(textLength = 10, range = 0..3)
        assertNotNull(result)
        assertEquals(0, result!!.first)
        assertEquals(4, result.second)
    }

    @Test
    fun `range ending beyond text length is clamped to textLength`() {
        val result = resolveHighlightBounds(textLength = 10, range = 8..20)
        assertNotNull(result)
        assertEquals(8, result!!.first)
        assertEquals(10, result.second)   // clamped: minOf(20+1, 10) = 10
    }

    @Test
    fun `range ending at last char index is correct`() {
        val result = resolveHighlightBounds(textLength = 10, range = 7..9)
        assertNotNull(result)
        assertEquals(7, result!!.first)
        assertEquals(10, result.second)   // last char: 9+1 = 10 = textLength
    }

    @Test
    fun `single character text with range 0 to 0 returns bounds`() {
        val result = resolveHighlightBounds(textLength = 1, range = 0..0)
        assertNotNull(result)
        assertEquals(0, result!!.first)
        assertEquals(1, result.second)
    }

    @Test
    fun `full text highlighted returns start 0 and end equal to textLength`() {
        val result = resolveHighlightBounds(textLength = 15, range = 0..14)
        assertNotNull(result)
        assertEquals(0, result!!.first)
        assertEquals(15, result.second)
    }
}
