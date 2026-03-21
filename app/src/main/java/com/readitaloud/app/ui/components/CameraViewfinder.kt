package com.readitaloud.app.ui.components

import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.readitaloud.app.R
import com.readitaloud.app.viewmodel.AppViewModel

/**
 * Full-screen camera preview composable.
 * Binds CameraX Preview + ImageCapture use cases to the current lifecycle.
 *
 * bindCamera() is called once in the AndroidView factory, not in update, so OCR
 * state transitions do not trigger spurious unbindAll()/rebind cycles mid-capture.
 *
 * TODO Story 3.1: Overlay amber corner guide brackets (18dp arm, 2dp stroke).
 */
@Composable
fun CameraViewfinder(
    appViewModel: AppViewModel,
    modifier: Modifier = Modifier
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    val cdText = stringResource(R.string.cd_camera_viewfinder)

    AndroidView(
        factory = { ctx ->
            PreviewView(ctx).apply {
                implementationMode = PreviewView.ImplementationMode.COMPATIBLE
                scaleType = PreviewView.ScaleType.FILL_CENTER
                appViewModel.bindCamera(lifecycleOwner, surfaceProvider)
            }
        },
        modifier = modifier
            .fillMaxSize()
            .semantics { contentDescription = cdText }
    )
}
