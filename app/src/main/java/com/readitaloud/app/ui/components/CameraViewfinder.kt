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
 * The [update] lambda is called on first composition and on any recomposition where
 * captured state changes. CameraRepository.bindCamera() calls unbindAll() before
 * rebinding, so duplicate bindings are safe.
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
            }
        },
        // NOTE: update is called on every recomposition where captured state changes.
        // bindCamera() calls unbindAll() internally, so re-entrancy is safe but keep
        // captured state (lifecycleOwner, appViewModel) stable to avoid unnecessary rebinds.
        update = { previewView ->
            appViewModel.bindCamera(lifecycleOwner, previewView.surfaceProvider)
        },
        modifier = modifier
            .fillMaxSize()
            .semantics { contentDescription = cdText }
    )
}
