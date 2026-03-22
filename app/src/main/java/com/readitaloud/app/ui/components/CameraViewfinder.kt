package com.readitaloud.app.ui.components

import androidx.camera.view.PreviewView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.systemBars
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.readitaloud.app.R
import com.readitaloud.app.viewmodel.AppViewModel

/**
 * Full-screen camera preview composable (Story 3.1 — AC3).
 * Binds CameraX Preview + ImageCapture use cases to the current lifecycle.
 *
 * bindCamera() is called once in the AndroidView factory, not in update, so OCR
 * state transitions do not trigger spurious unbindAll()/rebind cycles mid-capture.
 *
 * Amber corner guide brackets (18dp arm, 2dp stroke) are drawn over the preview via Canvas.
 * Bracket positions are inset by WindowInsets.systemBars so they remain visible when the
 * viewfinder extends edge-to-edge under the status bar and navigation bar.
 */
@Composable
fun CameraViewfinder(
    appViewModel: AppViewModel,
    modifier: Modifier = Modifier
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    val cdText = stringResource(R.string.cd_camera_viewfinder)

    Box(modifier = modifier.fillMaxSize()) {
        AndroidView(
            factory = { ctx ->
                PreviewView(ctx).apply {
                    implementationMode = PreviewView.ImplementationMode.COMPATIBLE
                    scaleType = PreviewView.ScaleType.FILL_CENTER
                    appViewModel.bindCamera(lifecycleOwner, surfaceProvider)
                }
            },
            modifier = Modifier
                .fillMaxSize()
                .semantics { contentDescription = cdText }
        )
        // Amber corner guide brackets — Story 3.1 (AC3)
        CornerBracketOverlay(
            modifier = Modifier.matchParentSize(),
            armLength = 18.dp,
            strokeWidth = 2.dp,
            color = Color(0xFFC97A1A)
        )
    }
}

/**
 * Draws amber L-shaped corner bracket guides at all four screen corners.
 * [armLength] controls the length of each bracket arm (spec: 18dp).
 * [strokeWidth] controls the line thickness (spec: 2dp).
 *
 * Bracket origins are offset inward by the system bar heights (status bar top, navigation bar
 * bottom) so brackets are always drawn within the visible safe area.
 * When edge-to-edge is not active both insets are 0 and brackets draw at the raw screen edges.
 */
@Composable
private fun CornerBracketOverlay(
    modifier: Modifier = Modifier,
    armLength: Dp = 18.dp,
    strokeWidth: Dp = 2.dp,
    color: Color = Color(0xFFC97A1A)
) {
    // Resolve system bar heights as Dp in the Composable scope (requires Composition context).
    // asPaddingValues() is the idiomatic Compose API for converting WindowInsets to Dp values.
    val systemBarPadding = WindowInsets.systemBars.asPaddingValues()
    val topInset: Dp = systemBarPadding.calculateTopPadding()
    val bottomInset: Dp = systemBarPadding.calculateBottomPadding()

    Canvas(modifier = modifier) {
        val arm = armLength.toPx()
        val stroke = strokeWidth.toPx()
        // Convert Dp insets to px inside DrawScope (which extends Density).
        val topOffset = topInset.toPx()
        val bottomOffset = bottomInset.toPx()
        val w = size.width
        val h = size.height
        val margin = arm   // inset from the safe-area edge

        // Helper: draw one L-shaped bracket at (cx, cy) oriented by direction signs
        fun drawBracket(cx: Float, cy: Float, xDir: Float, yDir: Float) {
            // Horizontal arm
            drawLine(
                color = color,
                start = Offset(cx, cy),
                end = Offset(cx + xDir * arm, cy),
                strokeWidth = stroke,
                cap = StrokeCap.Square
            )
            // Vertical arm
            drawLine(
                color = color,
                start = Offset(cx, cy),
                end = Offset(cx, cy + yDir * arm),
                strokeWidth = stroke,
                cap = StrokeCap.Square
            )
        }

        drawBracket(margin, topOffset + margin, 1f, 1f)              // Top-left:     → and ↓
        drawBracket(w - margin, topOffset + margin, -1f, 1f)         // Top-right:    ← and ↓
        drawBracket(margin, h - bottomOffset - margin, 1f, -1f)      // Bottom-left:  → and ↑
        drawBracket(w - margin, h - bottomOffset - margin, -1f, -1f) // Bottom-right: ← and ↑
    }
}
