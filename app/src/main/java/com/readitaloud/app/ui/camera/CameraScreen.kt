package com.readitaloud.app.ui.camera

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
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
import com.readitaloud.app.viewmodel.AppViewModel

private val DarkNavy = Color(0xFF0F1B26)
private val Amber = Color(0xFFC97A1A)
private val MutedWhite = Color(0xFFB0BEC5)

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun CameraScreen(
    appViewModel: AppViewModel,
    onNavigateToReading: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
    @Suppress("UNUSED_VARIABLE")
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
    // TTS is a stub this story — fully wired in Story 2.3.
    // Architecture rule: speak BEFORE visual update (LaunchedEffect fires before next recomposition renders content).
    LaunchedEffect(cameraPermissionState.status) {
        if (!cameraPermissionState.status.isGranted && !cameraPermissionState.status.shouldShowRationale) {
            appViewModel.speakAnnouncement(context.getString(R.string.tts_camera_denied))
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
            // TODO Story 2.1: Replace with actual CameraX PreviewView composable
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(DarkNavy)
                    .padding(16.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Camera Screen — Permission Granted",
                    color = Color.White,
                    fontSize = 18.sp
                )
            }
        }

        cameraPermissionState.status.shouldShowRationale -> {
            // User denied once but can be asked again — show rationale UI
            CameraPermissionNeededContent(
                onRequestPermission = { cameraPermissionState.launchPermissionRequest() }
            )
        }

        else -> {
            // Permanently denied — must send user to system settings
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
