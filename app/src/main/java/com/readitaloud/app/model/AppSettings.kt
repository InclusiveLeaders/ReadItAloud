package com.readitaloud.app.model

data class AppSettings(
    val speechRate: Float = 1.0f,
    val selectedVoice: String = "",
    val autoFlash: Boolean = false,
    val volumeShortcutEnabled: Boolean = true
)
