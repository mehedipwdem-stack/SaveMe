package com.example.ui.screens

import android.content.Intent
import android.net.Uri
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryAlert
import androidx.compose.material.icons.filled.BatteryStd
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Directions
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.NearMe
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ReportProblem
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.UserSettings
import com.example.data.model.CommunityAlert
import com.example.ui.theme.DarkCardBorder
import com.example.ui.theme.GlowRed
import com.example.ui.theme.RedPrimary
import com.example.ui.theme.RedPrimaryDark
import com.example.ui.theme.SafeGreen
import com.example.util.SmsHelper
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun CommunityScreen(
    communityAlerts: List<CommunityAlert>,
    settings: UserSettings,
    onBroadcastSos: () -> Unit,
    onRespondToAlert: (CommunityAlert) -> Unit,
    onMarkSafe: (CommunityAlert) -> Unit,
    onSimulateAlert: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isBengali = settings.isBengali
    var selectedFilter by remember { mutableStateOf("ALL") }

    val activeCount = communityAlerts.count { it.status == "ACTIVE_DISTRESS" }
    val filteredAlerts = when (selectedFilter) {
        "ACTIVE" -> communityAlerts.filter { it.status == "ACTIVE_DISTRESS" }
        "MINE" -> communityAlerts.filter { it.isUserTriggered }
        else -> communityAlerts
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        // Community Radar Header Card
        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = Color.Transparent),
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(Color(0xFF0F172A), Color(0xFF1E293B), Color(0xFF450A0A))
                    )
                )
                .border(1.dp, Color(0xFFEF4444).copy(alpha = 0.4f), RoundedCornerShape(18.dp))
                .testTag("community_radar_header")
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFDC2626).copy(alpha = 0.3f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Campaign,
                                contentDescription = "Radar",
                                tint = GlowRed,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = if (isBengali) "কমিউনিটি ভিক্টিম নেটওয়ার্ক" else "Community SOS Radar",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Black,
                                color = Color.White
                            )
                            Text(
                                text = if (isBengali) "আশেপাশের লাইভ বিপদ সংকেত" else "Real-time nearby distress alerts",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFF94A3B8)
                            )
                        }
                    }

                    // Active count pill
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(if (activeCount > 0) Color(0xFFDC2626) else SafeGreen)
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = if (isBengali) "$activeCount টি সক্রিয়" else "$activeCount Active",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = if (isBengali)
                        "কেউ বিপদে পড়লে আশেপাশের এই অ্যাপ ব্যবহারকারীরা অবিলম্বে সংকেত পান এবং দ্রুত সহায়তায় এগিয়ে আসতে পারেন।"
                    else
                        "When someone triggers SOS, nearby community users receive immediate alerts to assist the victim in danger.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFFE2E8F0),
                    fontSize = 12.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onBroadcastSos,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = RedPrimary,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(42.dp)
                            .testTag("broadcast_distress_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = "Broadcast",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isBengali) "বিপদ সংকেত পাঠান" else "Broadcast SOS",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.labelSmall
                        )
                    }

                    OutlinedButton(
                        onClick = onSimulateAlert,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color(0xFF38BDF8)
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .height(42.dp)
                            .testTag("simulate_nearby_alert_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.NearMe,
                            contentDescription = "Simulate",
                            modifier = Modifier.size(16.dp),
                            tint = Color(0xFF38BDF8)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isBengali) "বিপদ এলার্ট টেস্ট" else "Test Nearby SOS",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Filter Chips Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = selectedFilter == "ALL",
                onClick = { selectedFilter = "ALL" },
                label = { Text(if (isBengali) "সব এলার্ট (${communityAlerts.size})" else "All (${communityAlerts.size})") },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = RedPrimary.copy(alpha = 0.2f),
                    selectedLabelColor = RedPrimary
                ),
                modifier = Modifier.testTag("filter_all")
            )
            FilterChip(
                selected = selectedFilter == "ACTIVE",
                onClick = { selectedFilter = "ACTIVE" },
                label = { Text(if (isBengali) "সক্রিয় বিপদ ($activeCount)" else "Active ($activeCount)") },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = RedPrimary.copy(alpha = 0.2f),
                    selectedLabelColor = RedPrimary
                ),
                modifier = Modifier.testTag("filter_active")
            )
            FilterChip(
                selected = selectedFilter == "MINE",
                onClick = { selectedFilter = "MINE" },
                label = { Text(if (isBengali) "আমার এলার্ট" else "My Alerts") },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = RedPrimary.copy(alpha = 0.2f),
                    selectedLabelColor = RedPrimary
                ),
                modifier = Modifier.testTag("filter_mine")
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Alert Cards List
        if (filteredAlerts.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = "No alerts",
                        tint = SafeGreen,
                        modifier = Modifier.size(54.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = if (isBengali) "আশেপাশে কোনো সক্রিয় বিপদ নেই" else "No Active Distress in Area",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = if (isBengali) "আপনার কমিউনিটি বর্তমানে নিরাপদ" else "Your community is currently safe",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filteredAlerts, key = { it.id }) { alert ->
                    CommunityAlertItem(
                        alert = alert,
                        isBengali = isBengali,
                        onRespond = { onRespondToAlert(alert) },
                        onMarkSafe = { onMarkSafe(alert) },
                        onCallVictim = {
                            SmsHelper.makePhoneCall(context, alert.phoneMasked)
                        },
                        onOpenDirections = {
                            try {
                                val gmmIntentUri = Uri.parse("google.navigation:q=${alert.latitude},${alert.longitude}")
                                val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri).apply {
                                    setPackage("com.google.android.apps.maps")
                                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                }
                                context.startActivity(mapIntent)
                            } catch (_: Exception) {
                                val browserMap = Intent(Intent.ACTION_VIEW, Uri.parse("https://maps.google.com/?q=${alert.latitude},${alert.longitude}")).apply {
                                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                }
                                context.startActivity(browserMap)
                            }
                        }
                    )
                }
                item {
                    Spacer(modifier = Modifier.height(30.dp))
                }
            }
        }
    }
}

@Composable
fun CommunityAlertItem(
    alert: CommunityAlert,
    isBengali: Boolean,
    onRespond: () -> Unit,
    onMarkSafe: () -> Unit,
    onCallVictim: () -> Unit,
    onOpenDirections: () -> Unit
) {
    val isActive = alert.status == "ACTIVE_DISTRESS"
    val isResponding = alert.status == "RESPONDING"
    val isSafe = alert.status == "SAFE"

    val timeFormatted = remember(alert.timestamp) {
        val diffMins = (System.currentTimeMillis() - alert.timestamp) / (1000 * 60)
        when {
            diffMins < 1 -> if (isBengali) "এইমাত্র" else "Just now"
            diffMins < 60 -> if (isBengali) "$diffMins মিনিট আগে" else "$diffMins min ago"
            else -> SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date(alert.timestamp))
        }
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isActive) Color(0xFF1E1114) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = if (isActive) 1.5.dp else 1.dp,
                color = when {
                    isActive -> Color(0xFFDC2626)
                    isResponding -> Color(0xFFF59E0B)
                    else -> MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f)
                },
                shape = RoundedCornerShape(16.dp)
            )
            .testTag("community_alert_${alert.id}")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header Row: Victim Name + Status Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(
                                when {
                                    isActive -> Color(0xFFDC2626).copy(alpha = 0.25f)
                                    isResponding -> Color(0xFFF59E0B).copy(alpha = 0.25f)
                                    else -> SafeGreen.copy(alpha = 0.25f)
                                }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = when {
                                isActive -> Icons.Default.Warning
                                isResponding -> Icons.Default.Group
                                else -> Icons.Default.CheckCircle
                            },
                            contentDescription = null,
                            tint = when {
                                isActive -> Color(0xFFEF4444)
                                isResponding -> Color(0xFFF59E0B)
                                else -> SafeGreen
                            },
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = alert.victimName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "${alert.phoneMasked} • $timeFormatted",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 10.sp
                        )
                    }
                }

                // Status Badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            when {
                                isActive -> Color(0xFFDC2626)
                                isResponding -> Color(0xFFD97706)
                                else -> SafeGreen
                            }
                        )
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = when {
                            isActive -> if (isBengali) "সক্রিয় বিপদ" else "DISTRESS"
                            isResponding -> if (isBengali) "সহায়তা চলছে" else "HELPING"
                            else -> if (isBengali) "নিরাপদ" else "SAFE"
                        },
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Distress Message Box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(10.dp)
            ) {
                Text(
                    text = alert.customMessage,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Location & Battery Telemetry Strip
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Location & Distance
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = "Location",
                        tint = Color(0xFF38BDF8),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${alert.locationName} (${if (alert.distanceMeters > 0) "${alert.distanceMeters}m" else "Here"})",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 11.sp,
                        maxLines = 1
                    )
                }

                // Victim's Battery Level
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (alert.batteryLevel < 20) Icons.Default.BatteryAlert else Icons.Default.BatteryStd,
                        contentDescription = "Battery",
                        tint = if (alert.batteryLevel < 20) Color(0xFFEF4444) else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(
                        text = "${alert.batteryLevel}%",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (alert.batteryLevel < 20) Color(0xFFEF4444) else MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 11.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action Buttons Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (isActive) {
                    // Respond Action
                    Button(
                        onClick = onRespond,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF2563EB),
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1.3f).height(38.dp).testTag("respond_btn_${alert.id}")
                    ) {
                        Icon(imageVector = Icons.Default.Group, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isBengali) "আমি সাহায্য করছি" else "I'll Assist (${alert.responderCount})",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                } else if (alert.isUserTriggered && !isSafe) {
                    Button(
                        onClick = onMarkSafe,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = SafeGreen,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1.3f).height(38.dp)
                    ) {
                        Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isBengali) "নিরাপদ চিহ্নিত করুন" else "Mark as Safe",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Direct Call to Victim Button
                OutlinedButton(
                    onClick = onCallVictim,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f).height(38.dp).testTag("call_victim_${alert.id}")
                ) {
                    Icon(imageVector = Icons.Default.Call, contentDescription = "Call", modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = if (isBengali) "কল" else "Call", style = MaterialTheme.typography.labelSmall)
                }

                // Directions Map Button
                OutlinedButton(
                    onClick = onOpenDirections,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f).height(38.dp).testTag("directions_${alert.id}")
                ) {
                    Icon(imageVector = Icons.Default.Directions, contentDescription = "Map", modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = if (isBengali) "ম্যাপ" else "Map", style = MaterialTheme.typography.labelSmall)
                }
            }
        }
    }
}
