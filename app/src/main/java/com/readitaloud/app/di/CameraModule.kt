package com.readitaloud.app.di

import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object CameraModule {
    // CameraRepository binding will be added in Story 2.1
    // FLASHLIGHT permission requested on-demand in Story 3.4 — do not pre-request here
}
