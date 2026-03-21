package com.readitaloud.app

import com.readitaloud.app.core.ocr.OcrTextMerger
import com.readitaloud.app.core.ocr.OcrTextMerger.TextLineGroup
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Unit tests for OcrTextMerger.merge().
 * Runs on JVM — no Android dependencies required.
 */
class OcrTextMergerTest {

    @Test
    fun `empty list returns empty string`() {
        val result = OcrTextMerger.merge(emptyList())
        assertEquals("", result)
    }

    @Test
    fun `single block returns its text trimmed`() {
        val blocks = listOf(TextLineGroup("  Hello world  ", top = 0, bottom = 20))
        val result = OcrTextMerger.merge(blocks)
        assertEquals("Hello world", result)
    }

    @Test
    fun `two blocks within 20px gap merge into one paragraph`() {
        val blocks = listOf(
            TextLineGroup("First line", top = 0, bottom = 20),
            TextLineGroup("Second line", top = 30, bottom = 50)  // gap = 30 - 20 = 10px
        )
        val result = OcrTextMerger.merge(blocks)
        assertEquals("First line Second line", result)
    }

    @Test
    fun `two blocks with gap exactly 20px merge into one paragraph`() {
        val blocks = listOf(
            TextLineGroup("Line A", top = 0, bottom = 20),
            TextLineGroup("Line B", top = 40, bottom = 60)  // gap = 40 - 20 = 20px (boundary)
        )
        val result = OcrTextMerger.merge(blocks)
        assertEquals("Line A Line B", result)
    }

    @Test
    fun `two blocks with gap of 21px become separate paragraphs`() {
        val blocks = listOf(
            TextLineGroup("Paragraph one", top = 0, bottom = 20),
            TextLineGroup("Paragraph two", top = 41, bottom = 60)  // gap = 41 - 20 = 21px
        )
        val result = OcrTextMerger.merge(blocks)
        assertEquals("Paragraph one\n\nParagraph two", result)
    }

    @Test
    fun `three blocks first two close third far produces two paragraphs`() {
        val blocks = listOf(
            TextLineGroup("Line 1", top = 0, bottom = 20),
            TextLineGroup("Line 2", top = 25, bottom = 45),   // gap = 5px — same paragraph
            TextLineGroup("Line 3", top = 200, bottom = 220)  // gap = 155px — new paragraph
        )
        val result = OcrTextMerger.merge(blocks)
        assertEquals("Line 1 Line 2\n\nLine 3", result)
    }

    @Test
    fun `blocks out of vertical order are sorted before merging`() {
        // Deliver in reverse order — merge must sort by top coordinate first
        val blocks = listOf(
            TextLineGroup("Third", top = 100, bottom = 120),
            TextLineGroup("First", top = 0, bottom = 20),
            TextLineGroup("Second", top = 10, bottom = 30)   // gap from 20 to 10 is negative after sort, so ≤ 20
        )
        val result = OcrTextMerger.merge(blocks)
        // First (top=0, bottom=20), Second (top=10, gap=-10 → ≤20, merges), Third (top=100, gap=80 → new para)
        assertEquals("First Second\n\nThird", result)
    }

    @Test
    fun `whitespace in block text is preserved as trimmed`() {
        val blocks = listOf(
            TextLineGroup("  Trim me  ", top = 0, bottom = 20),
            TextLineGroup("  And me  ", top = 25, bottom = 45)
        )
        val result = OcrTextMerger.merge(blocks)
        assertEquals("Trim me And me", result)
    }
}
