package com.readitaloud.app.ui.reading

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.readitaloud.app.R
import com.readitaloud.app.model.AppUiState
import com.readitaloud.app.model.PlaybackStatus
import com.readitaloud.app.viewmodel.AppViewModel

@Composable
fun ReadingScreen(
    appViewModel: AppViewModel,
    onNavigateBack: () -> Unit
) {
    val uiState by appViewModel.uiState.collectAsStateWithLifecycle()

    // When state returns to Ready (via scanNew()), navigate back to CameraScreen
    LaunchedEffect(uiState) {
        if (uiState is AppUiState.Ready) {
            onNavigateBack()
        }
    }

    // Stop TTS and reset state when user presses Android back gesture
    BackHandler {
        appViewModel.scanNew()
        // onNavigateBack() is called automatically by LaunchedEffect when state becomes Ready
    }

    val readingState = uiState as? AppUiState.Reading
    val text = readingState?.text ?: ""
    val isPlaybackDone = readingState?.playback?.status == PlaybackStatus.Idle

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkNavy)
    ) {
        // Scrollable text display — AC3: 18sp minimum
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 32.dp)
                .padding(bottom = if (isPlaybackDone) 200.dp else 80.dp)   // space for buttons
        ) {
            item {
                Text(
                    text = text,
                    fontSize = 18.sp,
                    color = Color(0xFFF9F8F6),   // near-white for dark background
                    lineHeight = 28.sp
                )
            }
        }

        // Post-read buttons — appear when playback complete (AC4, AC5, AC6)
        if (isPlaybackDone) {
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 32.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // "Hear again" button — AC6
                val cdHearAgain = stringResource(R.string.cd_hear_again_button)
                Button(
                    onClick = { appViewModel.hearAgain() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 72.dp)          // AC4: 72dp minimum touch target
                        .semantics { contentDescription = cdHearAgain },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF2B1F0A)   // amber-tinted dark
                    )
                ) {
                    Text(
                        text = stringResource(R.string.btn_hear_again),
                        fontSize = 16.sp,
                        color = Color(0xFFC97A1A)        // amber text
                    )
                }

                // "Scan new" button — AC5
                val cdScanNew = stringResource(R.string.cd_scan_new_button)
                Button(
                    onClick = { appViewModel.scanNew() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 72.dp)          // AC4: 72dp minimum touch target
                        .semantics { contentDescription = cdScanNew },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0x14FFFFFF)  // subtle white-tinted
                    )
                ) {
                    Text(
                        text = stringResource(R.string.btn_scan_new),
                        fontSize = 16.sp,
                        color = Color.White
                    )
                }
            }
        }
    }
}

private val DarkNavy = Color(0xFF0F1B26)
