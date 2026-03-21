package com.readitaloud.app.core.ocr

/**
 * Merges OCR text lines within 20px vertical proximity into single paragraphs.
 * Pure function — fully unit-testable without Android dependencies.
 *
 * Accepts [TextLineGroup] data class instead of ML Kit's Text.TextBlock directly,
 * keeping this function decoupled from the Android ML Kit dependency.
 * OcrRepository converts Text.TextBlock → TextLineGroup before calling merge().
 */
object OcrTextMerger {

    /**
     * Lightweight representation of a recognised text block.
     * Decoupled from ML Kit so merge() is pure and unit-testable on the JVM.
     */
    data class TextLineGroup(val text: String, val top: Int, val bottom: Int)

    /**
     * Merges [blocks] into paragraphs. Blocks within 20px vertical gap are merged
     * with a space; blocks further apart become separate paragraphs separated by "\n\n".
     * Input order is preserved after sorting by vertical position (top coordinate).
     */
    fun merge(blocks: List<TextLineGroup>): String {
        if (blocks.isEmpty()) return ""

        val sorted = blocks.sortedBy { it.top }
        val paragraphs = mutableListOf<StringBuilder>()
        var current = StringBuilder(sorted.first().text.trim())
        var lastBottom = sorted.first().bottom

        for (block in sorted.drop(1)) {
            val gap = block.top - lastBottom
            if (gap <= 20) {
                current.append(' ').append(block.text.trim())
            } else {
                paragraphs.add(current)
                current = StringBuilder(block.text.trim())
            }
            lastBottom = block.bottom
        }
        if (current.isNotEmpty()) paragraphs.add(current)

        return paragraphs.joinToString("\n\n") { it.toString() }
    }
}
