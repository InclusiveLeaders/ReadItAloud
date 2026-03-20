package com.readitaloud.app.viewmodel

import androidx.lifecycle.ViewModel
import com.readitaloud.app.model.AppSettings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

// TODO Story 1.3: Add @HiltViewModel + @Inject constructor with SettingsRepository
class SettingsViewModel : ViewModel() {

    private val _settings = MutableStateFlow(AppSettings())
    val settings: StateFlow<AppSettings> = _settings.asStateFlow()
}
