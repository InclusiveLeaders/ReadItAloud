package com.readitaloud.app.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val TextColour = Color(0xFFF9F8F6)              // near-white on dark navy background
private val AmberHighlightBg = Color(0xFFC97A1A).copy(alpha = 0.30f)  // 30% amber overlay
private val HighlightTextColour = Color(0xFF1A1A1A)     // dark text on amber highlight (contrast)

/**
 * Displays recognised OCR text with the currently-spoken word highlighted (Story 3.1 — AC5).
 *
 * [text] — full recognised text string from OCR.
 * [currentWordRange] — char range of the word currently being spoken by TTS.
 *   Sourced from TtsRepository.currentWordRange → AppViewModel.init collector → PlaybackState.
 *   IntRange.EMPTY = no current word (idle or paused).
 *
 * Uses [AnnotatedString] with [SpanStyle] background for the word highlight.
 * Text size is 18sp per AC5 and architecture accessibility requirements.
 */
@Composable
fun PlaybackTextDisplay(
    text: String,
    currentWordRange: IntRange,
    modifier: Modifier = Modifier
) {
    val annotated = buildHighlightedText(text, currentWordRange)

    LazyColumn(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 32.dp)
    ) {
        item {
            Text(
                text = annotated,
                fontSize = 18.sp,       // AC5: 18sp minimum
                lineHeight = 28.sp
            )
        }
    }
}

/**
 * Resolves the start (inclusive) and end (exclusive) indices of the highlight region.
 * Returns null when no highlight should be applied.
 *
 * Pure Kotlin — no Compose dependencies — making it unit-testable without instrumentation.
 * The AnnotatedString assembly in [buildHighlightedText] is a thin, mechanical wrapper.
 */
internal fun resolveHighlightBounds(textLength: Int, range: IntRange): Pair<Int, Int>? {
    if (textLength == 0) return null
    if (range == IntRange.EMPTY) return null
    val start = range.first
    if (start < 0 || start >= textLength) return null
    val end = minOf(range.last + 1, textLength)
    return start to end
}

/**
 * Builds an [AnnotatedString] with an amber background span on the current word.
 * Delegates range validation to [resolveHighlightBounds] — safe for all edge cases.
 *
 * The highlighted word uses [HighlightTextColour] (dark) for readability against the amber
 * background. White text on 30% amber over dark navy would fail contrast requirements.
 */
private fun buildHighlightedText(text: String, currentWordRange: IntRange): AnnotatedString {
    if (text.isEmpty()) return AnnotatedString("")

    val bounds = resolveHighlightBounds(text.length, currentWordRange)
        ?: return buildAnnotatedString {
            withStyle(SpanStyle(color = TextColour)) { append(text) }
        }

    val (highlightStart, highlightEnd) = bounds
    return buildAnnotatedString {
        // Text before highlighted word
        if (highlightStart > 0) {
            withStyle(SpanStyle(color = TextColour)) {
                append(text.substring(0, highlightStart))
            }
        }
        // Highlighted current word
        withStyle(SpanStyle(color = HighlightTextColour, background = AmberHighlightBg)) {
            append(text.substring(highlightStart, highlightEnd))
        }
        // Text after highlighted word
        if (highlightEnd < text.length) {
            withStyle(SpanStyle(color = TextColour)) {
                append(text.substring(highlightEnd))
            }
        }
    }
}
