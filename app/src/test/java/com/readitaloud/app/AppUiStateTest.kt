package com.readitaloud.app

import com.readitaloud.app.model.AppUiState
import com.readitaloud.app.model.PlaybackState
import com.readitaloud.app.model.PlaybackStatus
import com.readitaloud.app.model.ScanResult
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests for AppUiState and related model classes.
 *
 * Architecture note: Camera pipeline (CameraRepository, CameraViewfinder) is NOT unit-tested
 * in v1 — requires device/emulator per architecture spec. Manual AC verification in Task 7.
 * AppViewModel requires CameraRepository (Android context dependency) so is tested via
 * instrumented tests or manual verification.
 *
 * These tests guard against accidental modification of the sealed class state machine,
 * which is the single source of truth for the entire app.
 */
class AppUiStateTest {

    // ── AppUiState sealed class structure ──────────────────────────────────────

    @Test
    fun `AppUiState_Ready is correct state`() {
        val state: AppUiState = AppUiState.Ready
        assertTrue(state is AppUiState.Ready)
    }

    @Test
    fun `AppUiState_Capturing is correct state`() {
        val state: AppUiState = AppUiState.Capturing
        assertTrue(state is AppUiState.Capturing)
    }

    @Test
    fun `AppUiState_Processing is correct state`() {
        val state: AppUiState = AppUiState.Processing
        assertTrue(state is AppUiState.Processing)
    }

    @Test
    fun `AppUiState_Reading holds text and playback state`() {
        val playback = PlaybackState(status = PlaybackStatus.Playing, speechRate = 1.0f)
        val state: AppUiState = AppUiState.Reading(text = "Hello world", playback = playback)

        assertTrue(state is AppUiState.Reading)
        val reading = state as AppUiState.Reading
        assertEquals("Hello world", reading.text)
        assertEquals(PlaybackStatus.Playing, reading.playback.status)
        assertEquals(1.0f, reading.playback.speechRate)
    }

    @Test
    fun `AppUiState_NoTextFound is correct state`() {
        val state: AppUiState = AppUiState.NoTextFound
        assertTrue(state is AppUiState.NoTextFound)
    }

    @Test
    fun `AppUiState_Error holds message`() {
        val state: AppUiState = AppUiState.Error(message = "Camera failed")
        assertTrue(state is AppUiState.Error)
        assertEquals("Camera failed", (state as AppUiState.Error).message)
    }

    @Test
    fun `AppUiState when expression covers all states`() {
        // Verifies the sealed class has exactly the expected 6 states.
        // A compile error here means a state was added/removed without updating this test.
        val states: List<AppUiState> = listOf(
            AppUiState.Ready,
            AppUiState.Capturing,
            AppUiState.Processing,
            AppUiState.Reading("text", PlaybackState()),
            AppUiState.NoTextFound,
            AppUiState.Error("err")
        )
        assertEquals(6, states.size)
        states.forEach { state ->
            val label = when (state) {
                is AppUiState.Ready -> "Ready"
                is AppUiState.Capturing -> "Capturing"
                is AppUiState.Processing -> "Processing"
                is AppUiState.Reading -> "Reading"
                is AppUiState.NoTextFound -> "NoTextFound"
                is AppUiState.Error -> "Error"
            }
            assertTrue(label.isNotEmpty())
        }
    }

    // ── PlaybackState ──────────────────────────────────────────────────────────

    @Test
    fun `PlaybackState default values are correct`() {
        val state = PlaybackState()
        assertEquals(PlaybackStatus.Idle, state.status)
        assertEquals(1.0f, state.speechRate)
    }

    @Test
    fun `PlaybackStatus has exactly three states`() {
        val values = PlaybackStatus.entries
        assertEquals(3, values.size)
        assertTrue(PlaybackStatus.Idle in values)
        assertTrue(PlaybackStatus.Playing in values)
        assertTrue(PlaybackStatus.Paused in values)
    }

    // ── ScanResult ────────────────────────────────────────────────────────────

    @Test
    fun `ScanResult_Success holds text`() {
        val result: ScanResult = ScanResult.Success("Recognised text")
        assertTrue(result is ScanResult.Success)
        assertEquals("Recognised text", (result as ScanResult.Success).text)
    }

    @Test
    fun `ScanResult_NoTextFound is correct`() {
        val result: ScanResult = ScanResult.NoTextFound
        assertTrue(result is ScanResult.NoTextFound)
    }

    @Test
    fun `ScanResult_BlurDetected holds score`() {
        val result: ScanResult = ScanResult.BlurDetected(score = 75.5f)
        assertTrue(result is ScanResult.BlurDetected)
        assertEquals(75.5f, (result as ScanResult.BlurDetected).score)
    }

    @Test
    fun `ScanResult_Error holds message`() {
        val result: ScanResult = ScanResult.Error("OCR failed")
        assertTrue(result is ScanResult.Error)
        assertEquals("OCR failed", (result as ScanResult.Error).message)
    }
}
