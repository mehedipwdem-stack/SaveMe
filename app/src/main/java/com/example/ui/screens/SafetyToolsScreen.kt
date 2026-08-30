package com.example.ui.screens

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bloodtype
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.MedicalInformation
import androidx.compose.material.icons.filled.PhoneCallback
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Radio
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.UserSettings
import com.example.data.model.AlertHistory
import com.example.ui.EmergencyUiState
import com.example.ui.theme.RedPrimary
import com.example.ui.theme.SafeGreen
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun SafetyToolsScreen(
    settings: UserSettings,
    uiState: EmergencyUiState,
    alertHistory: List<AlertHistory>,
    onUpdateSettings: (UserSettings) -> Unit,
    onToggleSiren: () -> Unit,
    onToggleStrobe: () -> Unit,
    onStartFakeCall: () -> Unit,
    onTestPowerTrigger: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    val isBengali = settings.isBengali

    var isEditingProfile by remember { mutableStateOf(false) }
    var userName by remember { mutableStateOf(settings.userName) }
    var userPhone by remember { mutableStateOf(settings.userPhone) }
    var bloodGroup by remember { mutableStateOf(settings.bloodGroup) }
    var medicalNote by remember { mutableStateOf(settings.medicalNote) }
    var customSosMessage by remember { mutableStateOf(settings.customSosMessage) }

    val bloodGroups = listOf("A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-")

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 3x Power Button Trigger Hardware Card
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
            ),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, Color(0xFFEAB308).copy(alpha = 0.4f), RoundedCornerShape(16.dp))
                .testTag("power_trigger_settings_card")
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFEAB308).copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.PowerSettingsNew,
                                contentDescription = null,
                                tint = Color(0xFFFACC15),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = if (isBengali) "পাওয়ার বাটন ৩ বার চাপার গার্ড" else "3x Power Button Trigger",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = if (settings.isPowerButtonDetectionEnabled)
                                    (if (isBengali) "ব্যাকগ্রাউন্ড সার্ভিস চালু আছে" else "Background service running")
                                else
                                    (if (isBengali) "সার্ভিস বন্ধ আছে" else "Service stopped"),
                                style = MaterialTheme.typography.labelSmall,
                                color = if (settings.isPowerButtonDetectionEnabled) Color(0xFF4ADE80) else Color(0xFFF87171)
                            )
                        }
                    }

                    Switch(
                        checked = settings.isPowerButtonDetectionEnabled,
                        onCheckedChange = { enabled ->
                            onUpdateSettings(settings.copy(isPowerButtonDetectionEnabled = enabled))
                        },
                        colors = SwitchDefaults.colors(checkedThumbColor = Color(0xFFEAB308)),
                        modifier = Modifier.testTag("toggle_power_guard_switch")
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = if (isBengali)
                        "ফোনটি লক থাকা অবস্থাতেও পাওয়ার বাটন পরপর ৩ বার চাপলে সরাসরি সিলেক্ট করা নাম্বারে এসএমএস, লাইভ লোকেশন এবং কমিউনিটিতে এলার্ট চলে যাবে।"
                    else
                        "Pressing the physical power button 3 times in rapid succession immediately dispatches SOS SMS, GPS location, and battery level to your emergency contacts.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 11.sp
                )

                Spacer(modifier = Modifier.height(10.dp))

                Button(
                    onClick = onTestPowerTrigger,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFD97706),
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth().testTag("simulate_power_press_btn")
                ) {
                    Icon(imageVector = Icons.Default.PowerSettingsNew, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isBengali) "পাওয়ার বাটন ৩x ট্রিগার টেস্ট করুন" else "Test 3x Power Button Trigger",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Emergency Medical Card & Profile
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            ),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f), RoundedCornerShape(16.dp))
                .testTag("medical_card")
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(RedPrimary.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.MedicalInformation,
                                contentDescription = null,
                                tint = RedPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = if (isBengali) "মেডিকেল ও ইমারজেন্সি কার্ড" else "Emergency Medical Card",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = if (isBengali) "বিপদের সময় এই তথ্য পাঠানো হবে" else "Included in emergency SMS broadcast",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    IconButton(
                        onClick = {
                            if (isEditingProfile) {
                                onUpdateSettings(
                                    settings.copy(
                                        userName = userName,
                                        userPhone = userPhone,
                                        bloodGroup = bloodGroup,
                                        medicalNote = medicalNote,
                                        customSosMessage = customSosMessage
                                    )
                                )
                            }
                            isEditingProfile = !isEditingProfile
                        },
                        modifier = Modifier.testTag("edit_medical_card_btn")
                    ) {
                        Icon(
                            imageVector = if (isEditingProfile) Icons.Default.Check else Icons.Default.Edit,
                            contentDescription = if (isEditingProfile) "Save" else "Edit",
                            tint = if (isEditingProfile) SafeGreen else MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                if (isEditingProfile) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = userName,
                            onValueChange = { userName = it },
                            label = { Text(if (isBengali) "আপনার নাম" else "Your Name") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = userPhone,
                            onValueChange = { userPhone = it },
                            label = { Text(if (isBengali) "আপনার মোবাইল" else "Your Phone") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Text(
                            text = if (isBengali) "রক্তের গ্রুপ (Blood Group):" else "Blood Group:",
                            style = MaterialTheme.typography.labelMedium
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            bloodGroups.take(4).forEach { bg ->
                                FilterChip(
                                    selected = bloodGroup == bg,
                                    onClick = { bloodGroup = bg },
                                    label = { Text(bg, fontSize = 11.sp) }
                                )
                            }
                        }
                        OutlinedTextField(
                            value = medicalNote,
                            onValueChange = { medicalNote = it },
                            label = { Text(if (isBengali) "মেডিকেল নোট / এলার্জি" else "Medical Notes / Allergy") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = customSosMessage,
                            onValueChange = { customSosMessage = it },
                            label = { Text(if (isBengali) "কাস্টম SOS বার্তা" else "Custom SOS Message") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = if (isBengali) "নাম: ${settings.userName}" else "Name: ${settings.userName}",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = if (isBengali) "নোট: ${settings.medicalNote}" else "Note: ${settings.medicalNote}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(RedPrimary.copy(alpha = 0.2f))
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "🩸 ${settings.bloodGroup}",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = RedPrimary
                            )
                        }
                    }
                }
            }
        }

        // Safety Preferences (Siren, Strobe, Countdown, Community Broadcast)
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            ),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f), RoundedCornerShape(16.dp))
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = if (isBengali) "SOS অপশন ও সুরক্ষা টুলস" else "SOS Preferences & Tools",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Sound Alarm Switch
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.VolumeUp, contentDescription = null, tint = RedPrimary, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(if (isBengali) "SOS-এ উচ্চশব্দে সাইরেন বাজবে" else "Loud Siren on SOS", style = MaterialTheme.typography.bodyMedium)
                    }
                    Switch(
                        checked = settings.isSoundAlarmEnabled,
                        onCheckedChange = { onUpdateSettings(settings.copy(isSoundAlarmEnabled = it)) }
                    )
                }

                // Strobe Flashlight Switch
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.FlashOn, contentDescription = null, tint = Color(0xFFF59E0B), modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(if (isBengali) "SOS-এ ফ্ল্যাশলাইট ব্লিঙ্ক করবে" else "SOS Flashlight Strobe", style = MaterialTheme.typography.bodyMedium)
                    }
                    Switch(
                        checked = settings.isStrobeFlashEnabled,
                        onCheckedChange = { onUpdateSettings(settings.copy(isStrobeFlashEnabled = it)) }
                    )
                }

                // Community Broadcast Switch
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Radio, contentDescription = null, tint = Color(0xFF0284C7), modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(if (isBengali) "কমিউনিটিতে লাইভ এলার্ট ব্রডকাস্ট" else "Community Network Broadcast", style = MaterialTheme.typography.bodyMedium)
                    }
                    Switch(
                        checked = settings.isCommunityBroadcastEnabled,
                        onCheckedChange = { onUpdateSettings(settings.copy(isCommunityBroadcastEnabled = it)) }
                    )
                }

                // Countdown Selector
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Timer, contentDescription = null, tint = Color(0xFF8B5CF6), modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(if (isBengali) "কাউন্টডাউন সময়:" else "SOS Countdown:", style = MaterialTheme.typography.bodyMedium)
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        listOf(0, 3, 5).forEach { sec ->
                            FilterChip(
                                selected = settings.countdownSeconds == sec,
                                onClick = { onUpdateSettings(settings.copy(countdownSeconds = sec)) },
                                label = { Text(if (sec == 0) (if (isBengali) "তাত্ক্ষণিক" else "0s") else "${sec}s", fontSize = 10.sp) }
                            )
                        }
                    }
                }
            }
        }

        // Alert History Log
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            ),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f), RoundedCornerShape(16.dp))
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.History, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isBengali) "বিগত এলার্ট ইতিহাস (${alertHistory.size})" else "Alert History (${alertHistory.size})",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                if (alertHistory.isEmpty()) {
                    Text(
                        text = if (isBengali) "এখনও কোনো এলার্ট ট্রিগার করা হয়নি।" else "No past emergency alerts recorded.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        alertHistory.take(5).forEach { history ->
                            val timeStr = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()).format(Date(history.timestamp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.surface)
                                    .padding(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text(
                                            text = "🚨 ${history.triggerSource}",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = RedPrimary
                                        )
                                        Text(
                                            text = "${history.locationAddress} • 🔋 ${history.batteryPercentage}%",
                                            style = MaterialTheme.typography.bodySmall,
                                            fontSize = 10.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Text(
                                        text = timeStr,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontSize = 9.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
    }
}
