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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.UserSettings
import com.example.data.model.EmergencyContact
import com.example.ui.EmergencyUiState
import com.example.ui.theme.RedPrimary
import com.example.ui.theme.RedPrimaryDark
import com.example.ui.theme.SafeGreen
import com.example.util.BatteryHelper
import com.example.util.LocationHelper
import com.example.util.SmsHelper

@Composable
fun ContactsScreen(
    contacts: List<EmergencyContact>,
    settings: UserSettings,
    uiState: EmergencyUiState,
    onAddContact: (EmergencyContact) -> Unit,
    onUpdateContact: (EmergencyContact) -> Unit,
    onDeleteContact: (EmergencyContact) -> Unit,
    onSetPrimary: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isBengali = settings.isBengali

    var showAddDialog by remember { mutableStateOf(false) }
    var editingContact by remember { mutableStateOf<EmergencyContact?>(null) }
    var showPreviewSms by remember { mutableStateOf(false) }

    val formattedSampleMessage = remember(uiState.currentLocation, uiState.batteryInfo, settings) {
        SmsHelper.formatEmergencyMessage(
            userName = settings.userName,
            location = uiState.currentLocation ?: com.example.util.DeviceLocation(23.8103, 90.4125, 15f, "Dhaka, Bangladesh", "https://maps.google.com/?q=23.8103,90.4125"),
            batteryInfo = uiState.batteryInfo,
            customNote = settings.medicalNote,
            isBengali = settings.isBengali
        )
    }

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            // Header Info Card
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
                        Column {
                            Text(
                                text = if (isBengali) "জরুরি কন্ট্যাক্ট লিস্ট (${contacts.size})" else "Emergency Contacts (${contacts.size})",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = if (isBengali) "৩ বার পাওয়ার বাটন চাপলে এদের কাছে এলার্ট যাবে"
                                else "Alerts & Live Location are sent to these contacts",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 11.sp
                            )
                        }

                        Button(
                            onClick = { showAddDialog = true },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = RedPrimary,
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.testTag("add_contact_top_btn")
                        ) {
                            Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = if (isBengali) "যোগ করুন" else "Add", style = MaterialTheme.typography.labelMedium)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // SMS Preview toggle
                    OutlinedButton(
                        onClick = { showPreviewSms = !showPreviewSms },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth().testTag("toggle_sms_preview_btn")
                    ) {
                        Icon(imageVector = Icons.Default.Message, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (showPreviewSms)
                                (if (isBengali) "এসএমএস প্রিভিউ লুকান" else "Hide SMS Preview")
                            else
                                (if (isBengali) "জরুরি এসএমএস বার্তা প্রিভিউ দেখুন" else "View Emergency SMS Message Preview"),
                            style = MaterialTheme.typography.labelSmall
                        )
                    }

                    if (showPreviewSms) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.surface)
                                .padding(10.dp)
                        ) {
                            Text(
                                text = formattedSampleMessage,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Contacts List
            if (contacts.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "No Contacts",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (isBengali) "কোনো কন্ট্যাক্ট যোগ করা হয়নি" else "No Emergency Contacts Added",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = { showAddDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = RedPrimary)
                        ) {
                            Text(if (isBengali) "+ জরুরি কন্ট্যাক্ট যোগ করুন" else "+ Add Emergency Contact")
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(contacts, key = { it.id }) { contact ->
                        ContactItemCard(
                            contact = contact,
                            isBengali = isBengali,
                            onToggleSms = { enabled ->
                                onUpdateContact(contact.copy(sendSms = enabled))
                            },
                            onToggleAutoCall = { enabled ->
                                onUpdateContact(contact.copy(autoCall = enabled))
                            },
                            onSetPrimary = {
                                onSetPrimary(contact.id)
                            },
                            onEdit = {
                                editingContact = contact
                            },
                            onDelete = {
                                onDeleteContact(contact)
                            },
                            onDirectCall = {
                                SmsHelper.makePhoneCall(context, contact.phone)
                            },
                            onSendTestSms = {
                                SmsHelper.openSmsAppFallback(context, contact.phone, formattedSampleMessage)
                            }
                        )
                    }
                    item {
                        Spacer(modifier = Modifier.height(60.dp))
                    }
                }
            }
        }
    }

    // Add or Edit Contact Dialog
    if (showAddDialog || editingContact != null) {
        val target = editingContact
        ContactFormDialog(
            initialContact = target,
            isBengali = isBengali,
            onDismiss = {
                showAddDialog = false
                editingContact = null
            },
            onSave = { newOrUpdated ->
                if (target != null) {
                    onUpdateContact(newOrUpdated)
                } else {
                    onAddContact(newOrUpdated)
                }
                showAddDialog = false
                editingContact = null
            }
        )
    }
}

@Composable
fun ContactItemCard(
    contact: EmergencyContact,
    isBengali: Boolean,
    onToggleSms: (Boolean) -> Unit,
    onToggleAutoCall: (Boolean) -> Unit,
    onSetPrimary: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onDirectCall: () -> Unit,
    onSendTestSms: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (contact.isPrimary) Color(0xFF1E1622) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = if (contact.isPrimary) 1.5.dp else 1.dp,
                color = if (contact.isPrimary) RedPrimary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f),
                shape = RoundedCornerShape(16.dp)
            )
            .testTag("contact_item_${contact.id}")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Top Row: Name, Relationship & Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(if (contact.isPrimary) RedPrimary else MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = if (contact.isPrimary) Color.White else MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = contact.name,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            if (contact.isPrimary) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(RedPrimary)
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = if (isBengali) "প্রধান" else "PRIMARY",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                            }
                        }

                        Text(
                            text = "${contact.phone} • ${contact.relationship}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Row {
                    IconButton(
                        onClick = onSetPrimary,
                        modifier = Modifier.size(32.dp).testTag("star_contact_${contact.id}")
                    ) {
                        Icon(
                            imageVector = if (contact.isPrimary) Icons.Default.Star else Icons.Default.StarBorder,
                            contentDescription = "Primary",
                            tint = if (contact.isPrimary) Color(0xFFFACC15) else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(
                        onClick = onEdit,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(32.dp).testTag("delete_contact_${contact.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = Color(0xFFEF4444),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Toggles Row: Send SMS & Auto Call on SOS
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // SMS Toggle
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = contact.sendSms,
                        onCheckedChange = onToggleSms,
                        colors = CheckboxDefaults.colors(checkedColor = RedPrimary)
                    )
                    Text(
                        text = if (isBengali) "স্বয়ংক্রিয় এসএমএস" else "Auto SMS",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                // Auto Call Toggle
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = contact.autoCall,
                        onCheckedChange = onToggleAutoCall,
                        colors = CheckboxDefaults.colors(checkedColor = SafeGreen)
                    )
                    Text(
                        text = if (isBengali) "স্বয়ংক্রিয় কল" else "Auto Call",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                // Quick Call Action
                IconButton(
                    onClick = onDirectCall,
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(SafeGreen.copy(alpha = 0.2f))
                        .testTag("call_contact_${contact.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Call,
                        contentDescription = "Call",
                        tint = SafeGreen,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun ContactFormDialog(
    initialContact: EmergencyContact?,
    isBengali: Boolean,
    onDismiss: () -> Unit,
    onSave: (EmergencyContact) -> Unit
) {
    var name by remember { mutableStateOf(initialContact?.name ?: "") }
    var phone by remember { mutableStateOf(initialContact?.phone ?: "") }
    var relationship by remember { mutableStateOf(initialContact?.relationship ?: "Family") }
    var isPrimary by remember { mutableStateOf(initialContact?.isPrimary ?: false) }
    var sendSms by remember { mutableStateOf(initialContact?.sendSms ?: true) }
    var autoCall by remember { mutableStateOf(initialContact?.autoCall ?: false) }

    val relationships = listOf(
        "Mother (মা)", "Father (বাবা)", "Brother (ভাই)", "Sister (বোন)",
        "Spouse (স্বামী/স্ত্রী)", "Friend (বন্ধু)", "Guardian (অভিভাবক)", "Police (পুলিশ)"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (initialContact != null) {
                    if (isBengali) "কন্ট্যাক্ট সম্পাদনা করুন" else "Edit Contact"
                } else {
                    if (isBengali) "নতুন জরুরি কন্ট্যাক্ট" else "New Emergency Contact"
                },
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(if (isBengali) "নাম (Name)" else "Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("input_contact_name")
                )

                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text(if (isBengali) "মোবাইল নম্বর (Phone)" else "Phone Number") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("input_contact_phone")
                )

                Text(
                    text = if (isBengali) "সম্পর্ক (Relationship):" else "Relationship:",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold
                )

                // Quick Relationship chips
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    relationships.take(3).forEach { rel ->
                        SuggestionChip(
                            onClick = { relationship = rel },
                            label = { Text(rel, fontSize = 10.sp) }
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(if (isBengali) "প্রধান জরুরি কন্ট্যাক্ট?" else "Set as Primary?")
                    Switch(checked = isPrimary, onCheckedChange = { isPrimary = it })
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank() && phone.isNotBlank()) {
                        onSave(
                            EmergencyContact(
                                id = initialContact?.id ?: 0L,
                                name = name.trim(),
                                phone = phone.trim(),
                                relationship = relationship,
                                isPrimary = isPrimary,
                                sendSms = sendSms,
                                autoCall = autoCall
                            )
                        )
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = RedPrimary),
                modifier = Modifier.testTag("save_contact_btn")
            ) {
                Text(if (isBengali) "সংরক্ষণ করুন" else "Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(if (isBengali) "বাতিল" else "Cancel")
            }
        }
    )
}
