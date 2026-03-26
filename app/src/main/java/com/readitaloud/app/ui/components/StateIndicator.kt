package com.readitaloud.app.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.readitaloud.app.model.AppUiState

private val Amber = Color(0xFFC97A1A)

/**
 * Displays the current app state as an uppercase amber label (Story 3.1 — AC4).
 *
 * Maps AppUiState to a human-readable label shown above the READ button on CameraScreen.
 * Marked as a TalkBack live region so state changes are announced automatically.
 * Voice prompts (TTS) remain the primary accessibility layer — this is visual confirmation.
 */
@Composable
fun StateIndicator(
    uiState: AppUiState,
    modifier: Modifier = Modifier
) {
    val label = when (uiState) {
        is AppUiState.Ready       -> "READY"
        is AppUiState.Capturing   -> "HOLD STILL"   // AC4: camera shutter active, user must not move
        is AppUiState.Processing  -> "READING"       // AC4: OCR is actively processing
        is AppUiState.Reading     -> "READING"
        is AppUiState.NoTextFound -> "NO TEXT"
        is AppUiState.Error       -> "ERROR"
    }

    Text(
        text = label,
        fontSize = 14.sp,           // AC4: minimum 14sp
        fontWeight = FontWeight.Bold,
        color = Amber,              // AC4: amber
        modifier = modifier
            .padding(bottom = 12.dp)
            .semantics {
                // Announce state changes to TalkBack automatically (groundwork for Story 3.3)
                liveRegion = LiveRegionMode.Polite
            }
    )
}
