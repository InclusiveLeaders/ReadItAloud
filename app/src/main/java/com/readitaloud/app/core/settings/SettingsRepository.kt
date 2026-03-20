package com.readitaloud.app.core.settings

import com.readitaloud.app.model.AppSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

// TODO Story 4.2: Implement Jetpack DataStore (Preferences) read/write
// Keys: speech_rate (Float), selected_voice (String), auto_flash (Boolean), volume_shortcut (Boolean)
class SettingsRepository {
    val settingsFlow: Flow<AppSettings> = flowOf(AppSettings())
}
