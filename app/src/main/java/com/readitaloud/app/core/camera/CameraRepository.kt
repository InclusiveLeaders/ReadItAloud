package com.readitaloud.app.core.camera

import android.content.Context
import android.util.Log
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.concurrent.Executor
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
    private var cameraProvider: ProcessCameraProvider? = null

    /**
     * Returns the bound ImageCapture use case for Story 2.2 capture triggering.
     * Returns null if bindCamera has not yet completed.
     */
    fun getImageCapture(): ImageCapture? = imageCapture

    /**
     * Binds Preview + ImageCapture use cases to the given lifecycle.
     * Caches the resolved [ProcessCameraProvider] for safe use in [unbindAll].
     * Calls unbindAll() before binding to prevent IllegalStateException on resume.
     * On failure, logs the error. TTS spoken error wired in Story 2.3.
     */
    fun bindCamera(lifecycleOwner: LifecycleOwner, surfaceProvider: Preview.SurfaceProvider) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        cameraProviderFuture.addListener({
            val provider = cameraProviderFuture.get()
            cameraProvider = provider

            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(surfaceProvider)
            }

            // Use local val to avoid force unwrap on the class property
            val capture = ImageCapture.Builder()
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                .build()
            imageCapture = capture

            try {
                provider.unbindAll()
                provider.bindToLifecycle(
                    lifecycleOwner,
                    CameraSelector.DEFAULT_BACK_CAMERA,
                    preview,
                    capture
                )
            } catch (e: Exception) {
                Log.e(TAG, "Camera bind failed", e)
                // TODO Story 2.3: Speak tts_camera_bind_error announcement on failure
            }
        }, ContextCompat.getMainExecutor(context))
    }

    /**
     * Triggers a single image capture via ImageCapture.OnImageCapturedCallback.
     * CRITICAL: caller MUST call imageProxy.close() in both onSuccess and onError paths.
     * [executor] — use ContextCompat.getMainExecutor(context) for the callback thread.
     */
    fun captureImage(
        executor: Executor,
        onSuccess: (ImageProxy) -> Unit,
        onError: (Exception) -> Unit
    ) {
        val capture = imageCapture
        if (capture == null) {
            onError(IllegalStateException("ImageCapture not initialized — ensure bindCamera() was called first"))
            return
        }
        capture.takePicture(executor, object : ImageCapture.OnImageCapturedCallback() {
            override fun onCaptureSuccess(image: ImageProxy) = onSuccess(image)
            override fun onError(exception: ImageCaptureException) = onError(exception)
        })
    }

    /**
     * Unbinds all CameraX use cases. Call on explicit teardown if needed.
     * Uses the cached [cameraProvider] to avoid a blocking [ListenableFuture.get] call.
     * Lifecycle-bound unbinding happens automatically via CameraX + LifecycleOwner.
     */
    fun unbindAll() {
        cameraProvider?.unbindAll() ?: Log.w(TAG, "unbindAll called before camera provider was ready")
    }

    companion object {
        private const val TAG = "CameraRepository"
    }
}
