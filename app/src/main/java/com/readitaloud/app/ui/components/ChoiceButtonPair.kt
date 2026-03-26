package com.readitaloud.app.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.readitaloud.app.R

/**
 * Post-read choice pair: "Hear again" and "Scan new" (Story 3.1 — AC6, AC7).
 *
 * Fades in over 200ms via [AnimatedVisibility] when [visible] transitions to true.
 * Two full-width equal-weight buttons with 12dp vertical spacing between them.
 * Each button has a 72dp minimum touch target height (WCAG / UX spec requirement).
 *
 * Replaces the plain Material buttons added directly in ReadingScreen in Story 2.3/2.4.
 * Behaviour and touch targets are identical — only the entrance animation is new.
 */
@Composable
fun ChoiceButtonPair(
    onHearAgain: () -> Unit,
    onScanNew: () -> Unit,
    visible: Boolean,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(animationSpec = tween(durationMillis = 200)),   // AC6: 200ms fade-in
        modifier = modifier
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            val cdHearAgain = stringResource(R.string.cd_hear_again_button)
            Button(
                onClick = onHearAgain,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 72.dp)    // AC6: 72dp minimum touch target
                    .semantics { contentDescription = cdHearAgain },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2B1F0A))
            ) {
                Text(
                    text = stringResource(R.string.btn_hear_again),
                    fontSize = 16.sp,
                    color = Color(0xFFC97A1A)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))   // AC6: minimum 12dp spacing between buttons

            val cdScanNew = stringResource(R.string.cd_scan_new_button)
            Button(
                onClick = onScanNew,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 72.dp)    // AC6: 72dp minimum touch target
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
