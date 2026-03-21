package com.readitaloud.app.di

import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object CameraModule {
    // CameraRepository is provided via @Inject constructor + @Singleton — no @Provides needed.
    // FLASHLIGHT permission requested on-demand in Story 3.4 — do not pre-request here.
}
