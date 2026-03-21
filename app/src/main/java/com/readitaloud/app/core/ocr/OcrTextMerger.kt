package com.readitaloud.app.core.ocr

/**
 * Merges OCR text lines within 20px vertical proximity into single paragraphs.
 * Pure function — fully unit testable without Android dependencies.
 *
 * TODO Story 2.2: Replace List<Any> with List<com.google.mlkit.vision.text.Text.TextBlock>
 * once the ML Kit dependency is added in Story 1.2.
 * The merge logic: group TextBlocks whose bounding boxes are within 20px vertically,
 * preserving sentence boundaries.
 */
object OcrTextMerger {
    fun merge(blocks: List<Any>): String {
        TODO("Implement in Story 2.2")
    }
}
