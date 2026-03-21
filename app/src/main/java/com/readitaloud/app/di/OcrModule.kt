package com.readitaloud.app.di

import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object OcrModule {
    // OcrRepository uses @Singleton + @Inject constructor — no explicit binding needed.
}
