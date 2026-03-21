package com.readitaloud.app.di

import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object CameraModule {
    // CameraRepository binding will be added in Story 2.1
}
