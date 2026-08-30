package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.NavigateNext
import androidx.compose.material.icons.filled.PhoneCallback
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.UserSettings
import com.example.data.model.CommunityAlert
import com.example.data.model.EmergencyContact
import com.example.ui.AppScreen
import com.example.ui.EmergencyUiState
import com.example.ui.components.EmergencyHotlinesBar
import com.example.ui.components.SosPulseButton
import com.example.ui.components.TelemetryCard
import com.example.ui.theme.DarkCardBorder
import com.example.ui.theme.GlowRed
import com.example.ui.theme.RedPrimary
import com.example.ui.theme.RedPrimaryDark
import com.example.ui.theme.SafeGreen

@Composable
fun HomeScreen(
    uiState: EmergencyUiState,
    settings: UserSettings,
    contacts: List<EmergencyContact>,
    communityAlerts: List<CommunityAlert>,
    onSosClick: () -> Unit,
    onCancelCountdown: () -> Unit,
    onCancelEmergency: () -> Unit,
    onRefreshLocation: () -> Unit,
    onCallNumber: (String) -> Unit,
    onToggleSiren: () -> Unit,
    onToggleStrobe: () -> Unit,
    onStartFakeCall: () -> Unit,
    onNavigateToScreen: (AppScreen) -> Unit,
    onToggleLanguage: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    val isBengali = settings.isBengali
    val activeNearbyAlert = communityAlerts.firstOrNull { it.status == "ACTIVE_DISTRESS" && !it.isUserTriggered }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(RedPrimary.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = "Shield",
                        tint = RedPrimary,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = if (isBengali) "জরুরি এলার্ট (SOS)" else "Emergency SOS",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = if (isBengali) "লাইভ সুরক্ষা নেটওয়ার্ক" else "Live Safety Network",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Language Toggle Button
            OutlinedButton(
                onClick = onToggleLanguage,
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.primary
                ),
                modifier = Modifier.height(34.dp).testTag("toggle_language_btn")
            ) {
                Text(
                    text = if (isBengali) "বাংলা (BN)" else "English (EN)",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Active Emergency Banner (if SOS triggered)
        AnimatedVisibility(
            visible = uiState.isEmergencyActive,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF450A0A)),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(2.dp, GlowRed, RoundedCornerShape(16.dp))
                    .padding(bottom = 14.dp)
                    .testTag("active_emergency_banner")
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = "Emergency Active",
                            tint = GlowRed,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isBengali) "🚨 বিপদ সংকেত সক্রিয়! 🚨" else "🚨 SOS ALERT BROADCASTED! 🚨",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = if (isBengali)
                            "আপনার লাইভ লোকেশন, ব্যাটারি তথ্য ও এলার্ট মেসেজ সংরক্ষিত কন্ট্যাক্ট এবং কমিউনিটিতে পাঠানো হয়েছে।"
                        else
                            "Your live GPS location, battery level, and emergency alert were dispatched to your contacts and community.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFFFFDAD6),
                        fontSize = 12.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Safe Mode Button
                    Button(
                        onClick = onCancelEmergency,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = SafeGreen,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .testTag("i_am_safe_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Safe",
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isBengali) "আমি এখন নিরাপদ (বিপদ সংকেত বন্ধ করুন)" else "I AM SAFE (Cancel Alert)",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Live Telemetry Bar (GPS + Battery + 3x Power Status)
        TelemetryCard(
            batteryInfo = uiState.batteryInfo,
            location = uiState.currentLocation,
            isLoadingLocation = uiState.isLoadingLocation,
            isPowerDetectionEnabled = settings.isPowerButtonDetectionEnabled,
            isBengali = isBengali,
            onRefreshLocation = onRefreshLocation
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Main SOS Pulse Button
        SosPulseButton(
            isEmergencyActive = uiState.isEmergencyActive,
            countdown = uiState.countdownRemaining,
            isBengali = isBengali,
            onSosClick = onSosClick,
            onCancelCountdown = onCancelCountdown
        )

        Spacer(modifier = Modifier.height(18.dp))

        // Power Button 3x Instruction Guide Hint
        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            ),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f), RoundedCornerShape(14.dp))
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFEAB308).copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.PowerSettingsNew,
                        contentDescription = "Power Button",
                        tint = Color(0xFFFACC15),
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (isBengali) "পাওয়ার বাটন ৩ বার চাপার ফিচার" else "3x Power Button Trigger",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = if (isBengali) "যেকোনো সময় পাওয়ার বাটন পরপর ৩ বার চাপলে স্ক্রিন লক থাকলেও অটোমেটিক SOS চলে যাবে।"
                        else "Press the phone power button 3 times consecutively to trigger auto SOS even when locked.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 11.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 999 National Emergency Calling Bar
        EmergencyHotlinesBar(
            isBengali = isBengali,
            onCallNumber = onCallNumber
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Quick Tools Row (Siren, Flashlight Strobe, Fake Call)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Siren Tool
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (uiState.isSirenPlaying) Color(0xFFDC2626)
                    else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                ),
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(14.dp))
                    .clickable(onClick = onToggleSiren)
                    .testTag("quick_siren_btn")
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.VolumeUp,
                        contentDescription = "Siren",
                        tint = if (uiState.isSirenPlaying) Color.White else MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (isBengali) (if (uiState.isSirenPlaying) "সাইরেন বন্ধ" else "সাইরেন চালু")
                        else (if (uiState.isSirenPlaying) "Stop Siren" else "Loud Siren"),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (uiState.isSirenPlaying) Color.White else MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            // Strobe Flash Tool
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (uiState.isStrobeActive) Color(0xFFEAB308)
                    else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                ),
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(14.dp))
                    .clickable(onClick = onToggleStrobe)
                    .testTag("quick_strobe_btn")
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.FlashOn,
                        contentDescription = "Strobe",
                        tint = if (uiState.isStrobeActive) Color.Black else Color(0xFFF59E0B),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (isBengali) (if (uiState.isStrobeActive) "ফ্ল্যাশ বন্ধ" else "SOS ফ্ল্যাশ")
                        else (if (uiState.isStrobeActive) "Stop Strobe" else "SOS Flash"),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (uiState.isStrobeActive) Color.Black else MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            // Fake Call Tool
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                ),
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(14.dp))
                    .clickable(onClick = onStartFakeCall)
                    .testTag("quick_fakecall_btn")
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.PhoneCallback,
                        contentDescription = "Fake Call",
                        tint = Color(0xFF10B981),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (isBengali) "ফেক কল" else "Fake Call",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Community Active Victim Alert Sneak-Peek Banner
        if (activeNearbyAlert != null) {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF7F1D1D).copy(alpha = 0.4f)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0xFFDC2626).copy(alpha = 0.5f), RoundedCornerShape(14.dp))
                    .clickable { onNavigateToScreen(AppScreen.COMMUNITY) }
                    .testTag("community_nearby_banner")
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFDC2626)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Campaign,
                            contentDescription = "Alert",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = if (isBengali) "আশেপাশে ভিক্টিম এলার্ট" else "Nearby Victim Alert",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFFCA5A5)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "• ${activeNearbyAlert.distanceMeters}m",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFFFCA5A5),
                                fontSize = 10.sp
                            )
                        }
                        Text(
                            text = "${activeNearbyAlert.victimName}: ${activeNearbyAlert.customMessage}",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Icon(
                        imageVector = Icons.Default.NavigateNext,
                        contentDescription = "View",
                        tint = Color(0xFFFCA5A5)
                    )
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
        }

        Spacer(modifier = Modifier.height(20.dp))
    }
}
