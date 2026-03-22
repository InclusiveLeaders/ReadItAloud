package com.readitaloud.app.ui.reading

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.text.style.TextAlign
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
    val playbackStatus = readingState?.playback?.status ?: PlaybackStatus.Idle
    val speechRate = readingState?.playback?.speechRate ?: 1.0f
    val isPlaying = playbackStatus == PlaybackStatus.Playing
    val isPaused = playbackStatus == PlaybackStatus.Paused
    val isIdle = playbackStatus == PlaybackStatus.Idle

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkNavy)
    ) {
        // Scrollable text display — AC3 (2.3): 18sp minimum
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 32.dp)
                .padding(bottom = if (isIdle) 200.dp else 160.dp)  // space for control bars
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

        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // --- Active playback controls (Playing or Paused) ---
            if (isPlaying || isPaused) {
                // Speech rate row: [−]  1.0×  [+]
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val cdRateDown = stringResource(R.string.cd_rate_decrease)
                    IconButton(
                        onClick = { appViewModel.adjustSpeechRate(-AppViewModel.SPEECH_RATE_STEP) },
                        modifier = Modifier
                            .size(72.dp)               // AC6: 72dp touch target
                            .semantics { contentDescription = cdRateDown }
                    ) {
                        Text(text = "−", fontSize = 24.sp, color = Color(0xFFF9F8F6))
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "${"%.1f".format(speechRate)}×",
                        fontSize = 18.sp,
                        color = Color(0xFFC97A1A),     // amber for active value
                        modifier = Modifier.widthIn(min = 60.dp),
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    val cdRateUp = stringResource(R.string.cd_rate_increase)
                    IconButton(
                        onClick = { appViewModel.adjustSpeechRate(AppViewModel.SPEECH_RATE_STEP) },
                        modifier = Modifier
                            .size(72.dp)               // AC6: 72dp touch target
                            .semantics { contentDescription = cdRateUp }
                    ) {
                        Text(text = "+", fontSize = 24.sp, color = Color(0xFFF9F8F6))
                    }
                }

                // Playback action buttons: [Restart] [Stop] [Play/Pause]
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val cdRestart = stringResource(R.string.cd_restart_button)
                    Button(
                        onClick = { appViewModel.restartPlayback() },
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(min = 72.dp)     // AC6
                            .semantics { contentDescription = cdRestart },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1A2A3A))
                    ) {
                        Text(
                            text = stringResource(R.string.btn_restart),
                            fontSize = 14.sp,
                            color = Color(0xFFF9F8F6)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    val cdStop = stringResource(R.string.cd_stop_button)
                    Button(
                        onClick = { appViewModel.stopPlayback() },
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(min = 72.dp)     // AC6
                            .semantics { contentDescription = cdStop },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1A2A3A))
                    ) {
                        Text(
                            text = stringResource(R.string.btn_stop),
                            fontSize = 14.sp,
                            color = Color(0xFFF9F8F6)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    val cdPlayPause = stringResource(
                        if (isPlaying) R.string.cd_pause_button else R.string.cd_play_button
                    )
                    Button(
                        onClick = { appViewModel.togglePlayback() },
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(min = 72.dp)     // AC6
                            .semantics { contentDescription = cdPlayPause },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2B1F0A))
                    ) {
                        Text(
                            text = if (isPlaying) stringResource(R.string.btn_pause)
                                   else stringResource(R.string.btn_play),
                            fontSize = 14.sp,
                            color = Color(0xFFC97A1A)   // amber for primary action
                        )
                    }
                }
            }

            // --- Post-read buttons (Idle / playback complete) ---
            if (isIdle) {
                val cdHearAgain = stringResource(R.string.cd_hear_again_button)
                Button(
                    onClick = { appViewModel.hearAgain() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 72.dp)
                        .semantics { contentDescription = cdHearAgain },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2B1F0A))
                ) {
                    Text(
                        text = stringResource(R.string.btn_hear_again),
                        fontSize = 16.sp,
                        color = Color(0xFFC97A1A)
                    )
                }

                val cdScanNew = stringResource(R.string.cd_scan_new_button)
                Button(
                    onClick = { appViewModel.scanNew() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 72.dp)
                        .semantics { contentDescription = cdScanNew },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0x14FFFFFF))
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
