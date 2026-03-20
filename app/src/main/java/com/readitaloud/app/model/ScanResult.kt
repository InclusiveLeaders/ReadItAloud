package com.readitaloud.app.model

/**
 * Typed result from OCR/camera pipeline. Never throw — always return a ScanResult.
 */
sealed class ScanResult {
    data class Success(val text: String) : ScanResult()
    object NoTextFound : ScanResult()
    data class BlurDetected(val score: Float) : ScanResult()
    data class Error(val message: String) : ScanResult()
}
