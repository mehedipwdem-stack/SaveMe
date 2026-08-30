package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.GlowRed
import com.example.ui.theme.RedPrimary
import com.example.ui.theme.RedPrimaryDark

@Composable
fun SosPulseButton(
    isEmergencyActive: Boolean,
    countdown: Int?,
    isBengali: Boolean,
    onSosClick: () -> Unit,
    onCancelCountdown: () -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "sos_pulse")

    val pulseScale1 by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.35f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulse1"
    )

    val pulseAlpha1 by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 0.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "alpha1"
    )

    val pulseScale2 by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, delayMillis = 400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulse2"
    )

    val pulseAlpha2 by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 0.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, delayMillis = 400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "alpha2"
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(240.dp)
            .testTag("sos_pulse_button_container")
    ) {
        // Outer glowing pulse ring 1
        Box(
            modifier = Modifier
                .size(190.dp)
                .scale(if (isEmergencyActive || countdown != null) pulseScale1 else 1.08f)
                .clip(CircleShape)
                .background(
                    if (isEmergencyActive) GlowRed.copy(alpha = pulseAlpha1)
                    else RedPrimary.copy(alpha = 0.15f)
                )
        )

        // Outer glowing pulse ring 2
        Box(
            modifier = Modifier
                .size(175.dp)
                .scale(if (isEmergencyActive || countdown != null) pulseScale2 else 1.04f)
                .clip(CircleShape)
                .background(
                    if (isEmergencyActive) RedPrimary.copy(alpha = pulseAlpha2)
                    else RedPrimary.copy(alpha = 0.25f)
                )
        )

        // Main Core SOS Button
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(160.dp)
                .shadow(
                    elevation = if (isEmergencyActive) 24.dp else 12.dp,
                    shape = CircleShape,
                    ambientColor = RedPrimary,
                    spotColor = GlowRed
                )
                .clip(CircleShape)
                .background(
                    brush = Brush.radialGradient(
                        colors = if (isEmergencyActive) {
                            listOf(Color(0xFFFF3B30), RedPrimaryDark, Color(0xFF450A0A))
                        } else {
                            listOf(Color(0xFFEF4444), RedPrimary, RedPrimaryDark)
                        }
                    )
                )
                .border(
                    width = 4.dp,
                    color = if (isEmergencyActive) Color.White.copy(alpha = 0.8f) else Color(0xFFFFB4AB),
                    shape = CircleShape
                )
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = ripple(bounded = true, color = Color.White)
                ) {
                    if (countdown == null) {
                        onSosClick()
                    }
                }
                .testTag("main_sos_button")
        ) {
            if (countdown != null) {
                // Countdown Active state
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(8.dp)
                ) {
                    Text(
                        text = "$countdown",
                        style = MaterialTheme.typography.displayMedium,
                        fontWeight = FontWeight.Black,
                        color = Color.White,
                        fontSize = 44.sp
                    )
                    Text(
                        text = if (isBengali) "সেকেন্ডে এলার্ট যাবে" else "SENDING IN",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFFFFDAD6),
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Button(
                        onClick = onCancelCountdown,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White,
                            contentColor = RedPrimaryDark
                        ),
                        modifier = Modifier
                            .height(28.dp)
                            .testTag("cancel_countdown_button")
                    ) {
                        Text(
                            text = if (isBengali) "বাতিল" else "CANCEL",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            } else {
                // Default / Active SOS state
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(12.dp)
                ) {
                    if (isEmergencyActive) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = "Active Emergency",
                            tint = Color.White,
                            modifier = Modifier.size(32.dp)
                        )
                        Text(
                            text = "ACTIVE",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Black,
                            color = Color.White,
                            letterSpacing = 2.sp
                        )
                        Text(
                            text = if (isBengali) "বিপদ সংকেত চালু" else "SOS BROADCASTING",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFFFFDAD6),
                            fontWeight = FontWeight.SemiBold
                        )
                    } else {
                        Text(
                            text = "SOS",
                            style = MaterialTheme.typography.displayMedium,
                            fontWeight = FontWeight.Black,
                            color = Color.White,
                            fontSize = 42.sp,
                            letterSpacing = 2.sp
                        )
                        Text(
                            text = if (isBengali) "বিপদ সংকেত" else "EMERGENCY",
                            style = MaterialTheme.typography.labelMedium,
                            color = Color(0xFFFFDAD6),
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (isBengali) "বাটন ৩ বার চাপুন" else "Press 3x Power",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFFFFDAD6).copy(alpha = 0.85f),
                            fontSize = 10.sp
                        )
                    }
                }
            }
        }
    }
}
