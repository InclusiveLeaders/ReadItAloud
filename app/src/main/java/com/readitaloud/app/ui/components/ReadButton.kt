package com.readitaloud.app.ui.components

import android.database.ContentObserver
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.readitaloud.app.R

private val Amber = Color(0xFFC97A1A)

/**
 * Custom READ button component (Story 3.1 — AC1, AC2).
 *
 * Visual: 130dp amber circle, white "READ" 24sp bold.
 * Tappable area: 144dp total via outer [Box] wrapper (extra 7dp each side = transparent padding).
 * Animation: scale pulse 1.0→1.04 (1200ms loop) when [isPulsing];
 *   suppressed when system ANIMATOR_DURATION_SCALE == 0 (Android "Remove animations").
 */
@Composable
fun ReadButton(
    onClick: () -> Unit,
    enabled: Boolean = true,
    isPulsing: Boolean = false,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    // produceState + ContentObserver: re-reads ANIMATOR_DURATION_SCALE whenever the user
    // changes the "Remove animations" system setting — including while the app is in the
    // foreground (critical for an accessibility-focused app).
    val animationsEnabled by produceState(
        initialValue = Settings.Global.getFloat(
            context.contentResolver,
            Settings.Global.ANIMATOR_DURATION_SCALE,
            1f
        ) > 0f,
        key1 = context
    ) {
        val settingUri = Settings.Global.getUriFor(Settings.Global.ANIMATOR_DURATION_SCALE)
        val observer = object : ContentObserver(Handler(Looper.getMainLooper())) {
            override fun onChange(selfChange: Boolean) {
                value = Settings.Global.getFloat(
                    context.contentResolver,
                    Settings.Global.ANIMATOR_DURATION_SCALE,
                    1f
                ) > 0f
            }
        }
        context.contentResolver.registerContentObserver(settingUri, false, observer)
        awaitDispose { context.contentResolver.unregisterContentObserver(observer) }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "ReadButtonPulse")

    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = if (isPulsing && animationsEnabled) 1.04f else 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 600),   // 600ms each direction = 1200ms full loop
            repeatMode = RepeatMode.Reverse
        ),
        label = "ReadButtonScale"
    )

    val cdText = stringResource(R.string.cd_read_button)

    // 144dp outer Box provides the required tappable area (AC1)
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier.size(144.dp)
    ) {
        Button(
            onClick = onClick,
            enabled = enabled,
            shape = CircleShape,
            colors = ButtonDefaults.buttonColors(
                containerColor = Amber,
                disabledContainerColor = Amber.copy(alpha = 0.4f)
            ),
            modifier = Modifier
                .size(130.dp)
                .scale(pulseScale)
                .semantics { contentDescription = cdText }
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
