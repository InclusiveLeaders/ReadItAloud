package com.readitaloud.app.ui.camera

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import com.readitaloud.app.R
import com.readitaloud.app.model.AppUiState
import com.readitaloud.app.ui.components.CameraViewfinder
import com.readitaloud.app.viewmodel.AppViewModel

private val Amber = Color(0xFFC97A1A)
private val MutedWhite = Color(0xFFB0BEC5)
private val DarkNavy = Color(0xFF0F1B26)

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun CameraScreen(
    appViewModel: AppViewModel,
    onNavigateToReading: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
    val uiState by appViewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    val cameraPermissionState = rememberPermissionState(android.Manifest.permission.CAMERA)

    // Trigger permission request on first composition if not yet granted
    LaunchedEffect(Unit) {
        if (!cameraPermissionState.status.isGranted) {
            cameraPermissionState.launchPermissionRequest()
        }
    }

    // Speak TTS announcement when permission is permanently denied.
    LaunchedEffect(cameraPermissionState.status) {
        if (!cameraPermissionState.status.isGranted && !cameraPermissionState.status.shouldShowRationale) {
            appViewModel.speakAnnouncement(context.getString(R.string.tts_camera_denied))
        }
    }

    // Navigate to ReadingScreen when OCR succeeds; reset to Ready on NoTextFound.
    // TODO Story 2.3 pre-condition: NoTextFound reset must wait for TTS completion
    // (or a minimum delay) before calling resetToReady() — otherwise the "no text" announcement
    // gets cut off by the immediate state transition back to Ready.
    LaunchedEffect(uiState) {
        when (uiState) {
            is AppUiState.Reading -> onNavigateToReading()
            is AppUiState.NoTextFound -> appViewModel.resetToReady()
            else -> { /* no navigation */ }
        }
    }

    val onOpenSettings: () -> Unit = {
        val intent = Intent(
            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
            Uri.fromParts("package", context.packageName, null)
        )
        context.startActivity(intent)
    }

    when {
        cameraPermissionState.status.isGranted -> {
            Box(modifier = Modifier.fillMaxSize()) {
                CameraViewfinder(
                    appViewModel = appViewModel,
                    modifier = Modifier.fillMaxSize()
                )

                // READ button overlay — bottom-centre above system nav bar
                // Story 3.1 will replace this Button with the custom ReadButton composable
                val cdRead = stringResource(R.string.cd_read_button)
                val isReady = uiState is AppUiState.Ready
                Button(
                    onClick = { appViewModel.startCapture() },
                    enabled = isReady,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 48.dp)
                        .size(130.dp)
                        .semantics { contentDescription = cdRead },
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Amber,
                        disabledContainerColor = Amber.copy(alpha = 0.4f)
                    )
                ) {
                    Text(
                        text = stringResource(R.string.btn_read),
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }

        cameraPermissionState.status.shouldShowRationale -> {
            CameraPermissionNeededContent(
                onRequestPermission = { cameraPermissionState.launchPermissionRequest() }
            )
        }

        else -> {
            CameraPermissionDeniedContent(onOpenSettings = onOpenSettings)
        }
    }
}

@Composable
private fun CameraPermissionNeededContent(onRequestPermission: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkNavy)
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(R.string.permission_camera_needed_title),
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = stringResource(R.string.permission_camera_needed_body),
            fontSize = 16.sp,
            color = MutedWhite,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(32.dp))
        val btnDesc = stringResource(R.string.permission_camera_request_button)
        Button(
            onClick = onRequestPermission,
            modifier = Modifier
                .heightIn(min = 72.dp)
                .widthIn(min = 200.dp)
                .semantics { contentDescription = btnDesc },
            colors = ButtonDefaults.buttonColors(containerColor = Amber)
        ) {
            Text(
                text = btnDesc,
                fontSize = 16.sp,
                color = Color.White
            )
        }
    }
}

@Composable
private fun CameraPermissionDeniedContent(onOpenSettings: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkNavy)
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(R.string.permission_camera_denied_body),
            fontSize = 16.sp,
            color = Color.White,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(32.dp))
        val btnDesc = stringResource(R.string.permission_camera_denied_button)
        Button(
            onClick = onOpenSettings,
            modifier = Modifier
                .heightIn(min = 72.dp)
                .widthIn(min = 200.dp)
                .semantics { contentDescription = btnDesc },
            colors = ButtonDefaults.buttonColors(containerColor = Amber)
        ) {
            Text(
                text = btnDesc,
                fontSize = 16.sp,
                color = Color.White
            )
        }
    }
}
