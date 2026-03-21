package com.readitaloud.app.core.ocr

import android.util.Log
import androidx.camera.core.ImageProxy
import com.google.android.gms.tasks.Tasks
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.readitaloud.app.model.ScanResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Wraps ML Kit Text Recognition v2 (bundled, offline).
 * CRITICAL: Uses bundled artifact (com.google.mlkit:text-recognition), NOT the
 * Play Services variant — ensures fully offline operation with no runtime downloads.
 *
 * Threading: recogniseText() must be called from a coroutine context; it uses
 * Dispatchers.IO internally via withContext. Tasks.await() blocks the IO thread.
 *
 * CRITICAL: ImageProxy MUST be closed in both success and error paths.
 * Failing to close causes CameraX to stop delivering new frames silently.
 */
@Singleton
class OcrRepository @Inject constructor() {

    /**
     * Runs on-device OCR on the given [imageProxy].
     * Handles 90° rotation automatically via [imageProxy.imageInfo.rotationDegrees].
     * Always returns a typed [ScanResult] — never throws to the caller.
     */
    suspend fun recogniseText(imageProxy: ImageProxy): ScanResult = withContext(Dispatchers.IO) {
        try {
            val mediaImage = imageProxy.image
                ?: return@withContext ScanResult.Error("Image buffer unavailable").also {
                    imageProxy.close()
                }

            val inputImage = InputImage.fromMediaImage(
                mediaImage,
                imageProxy.imageInfo.rotationDegrees   // handles up to 90° rotation (AC4)
            )

            val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
            val result = Tasks.await(recognizer.process(inputImage))
            imageProxy.close()   // CRITICAL: close before returning

            if (result.textBlocks.isEmpty()) {
                ScanResult.NoTextFound
            } else {
                val groups = result.textBlocks.map { block ->
                    OcrTextMerger.TextLineGroup(
                        text = block.text,
                        top = block.boundingBox?.top ?: 0,
                        bottom = block.boundingBox?.bottom ?: 0
                    )
                }
                ScanResult.Success(OcrTextMerger.merge(groups))
            }
        } catch (e: Exception) {
            imageProxy.close()   // CRITICAL: close on error path too
            Log.e(TAG, "OCR processing failed", e)
            ScanResult.Error(e.message ?: "OCR processing failed")
        }
    }

    companion object {
        private const val TAG = "OcrRepository"
    }
}
