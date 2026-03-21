package com.readitaloud.app.core.camera

import android.content.Context
import android.util.Log
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manages CameraX lifecycle binding, preview surface, and ImageCapture use case.
 *
 * Threading: bindCamera callback runs on main executor (ContextCompat.getMainExecutor).
 * Camera pipeline is NOT unit-tested in v1 per architecture spec — validated manually.
 */
@Singleton
class CameraRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private var imageCapture: ImageCapture? = null

    /**
     * Returns the bound ImageCapture use case for Story 2.2 capture triggering.
     * Returns null if bindCamera has not yet completed.
     */
    fun getImageCapture(): ImageCapture? = imageCapture

    /**
     * Binds Preview + ImageCapture use cases to the given lifecycle.
     * Calls unbindAll() before binding to prevent IllegalStateException on resume.
     * On failure, logs the error. TTS spoken error wired in Story 2.3.
     */
    fun bindCamera(lifecycleOwner: LifecycleOwner, surfaceProvider: Preview.SurfaceProvider) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(surfaceProvider)
            }

            imageCapture = ImageCapture.Builder()
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                .build()

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    lifecycleOwner,
                    CameraSelector.DEFAULT_BACK_CAMERA,
                    preview,
                    imageCapture!!
                )
            } catch (e: Exception) {
                Log.e(TAG, "Camera bind failed", e)
                // TODO Story 2.3: Speak tts_camera_bind_error announcement on failure
            }
        }, ContextCompat.getMainExecutor(context))
    }

    /**
     * Unbinds all CameraX use cases. Call on explicit teardown if needed.
     * Lifecycle-bound unbinding happens automatically via CameraX + LifecycleOwner.
     */
    fun unbindAll() {
        try {
            ProcessCameraProvider.getInstance(context).get().unbindAll()
        } catch (e: Exception) {
            Log.e(TAG, "unbindAll failed", e)
        }
    }

    companion object {
        private const val TAG = "CameraRepository"
    }
}
