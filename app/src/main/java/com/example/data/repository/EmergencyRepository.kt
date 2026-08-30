package com.example.data.repository

import android.content.Context
import com.example.data.local.AlertHistoryDao
import com.example.data.local.CommunityAlertDao
import com.example.data.local.ContactDao
import com.example.data.local.UserPreferences
import com.example.data.model.AlertHistory
import com.example.data.model.CommunityAlert
import com.example.data.model.EmergencyContact
import com.example.util.BatteryHelper
import com.example.util.BatteryInfo
import com.example.util.DeviceLocation
import com.example.util.LocationHelper
import com.example.util.NotificationHelper
import com.example.util.SmsHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.util.UUID

data class SosDispatchResult(
    val isSuccess: Boolean,
    val contactsNotified: Int,
    val location: DeviceLocation,
    val batteryInfo: BatteryInfo,
    val message: String,
    val error: String? = null
)

class EmergencyRepository(
    private val context: Context,
    private val contactDao: ContactDao,
    private val communityAlertDao: CommunityAlertDao,
    private val alertHistoryDao: AlertHistoryDao,
    private val userPreferences: UserPreferences
) {
    val allContacts: Flow<List<EmergencyContact>> = contactDao.getAllContacts()
    val allCommunityAlerts: Flow<List<CommunityAlert>> = communityAlertDao.getAllCommunityAlerts()
    val activeCommunityAlerts: Flow<List<CommunityAlert>> = communityAlertDao.getActiveAlerts()
    val alertHistory: Flow<List<AlertHistory>> = alertHistoryDao.getAllHistory()
    val userSettings = userPreferences.settings

    suspend fun addContact(contact: EmergencyContact): Long = withContext(Dispatchers.IO) {
        contactDao.insertContact(contact)
    }

    suspend fun updateContact(contact: EmergencyContact) = withContext(Dispatchers.IO) {
        contactDao.updateContact(contact)
    }

    suspend fun deleteContact(contact: EmergencyContact) = withContext(Dispatchers.IO) {
        contactDao.deleteContact(contact)
    }

    suspend fun setPrimaryContact(contactId: Long) = withContext(Dispatchers.IO) {
        contactDao.clearPrimaryFlags()
        contactDao.setPrimaryContact(contactId)
    }

    suspend fun respondToCommunityAlert(alertId: String) = withContext(Dispatchers.IO) {
        communityAlertDao.updateAlertStatusAndResponder(alertId, "RESPONDING")
    }

    suspend fun markAlertSafe(alert: CommunityAlert) = withContext(Dispatchers.IO) {
        communityAlertDao.updateAlert(alert.copy(status = "SAFE"))
    }

    suspend fun resolveAllUserAlerts() = withContext(Dispatchers.IO) {
        communityAlertDao.resolveUserAlerts()
    }

    suspend fun triggerIncomingCommunityAlert(alert: CommunityAlert) = withContext(Dispatchers.IO) {
        communityAlertDao.insertAlert(alert)
        NotificationHelper.showCommunityDistressNotification(context, alert, userSettings.value.isBengali)
    }

    suspend fun executeSosDispatch(triggerSource: String = "SOS_BUTTON"): SosDispatchResult =
        withContext(Dispatchers.IO) {
            val settings = userSettings.value
            val batteryInfo = BatteryHelper.getBatteryInfo(context)
            val location = LocationHelper.getCurrentLocation(context)
            val contacts = contactDao.getSmsContacts()

            val formattedMessage = SmsHelper.formatEmergencyMessage(
                userName = settings.userName,
                location = location,
                batteryInfo = batteryInfo,
                customNote = "${settings.bloodGroup} | ${settings.medicalNote} | ${settings.customSosMessage}",
                isBengali = settings.isBengali
            )

            val smsResult = SmsHelper.sendSmsDirect(context, contacts, formattedMessage)
            val sentCount = smsResult.getOrDefault(0)

            // Broadcast to Community Network if enabled
            if (settings.isCommunityBroadcastEnabled) {
                val userAlert = CommunityAlert(
                    id = "user_sos_${UUID.randomUUID().toString().take(8)}",
                    victimName = "${settings.userName} (You)",
                    phoneMasked = if (settings.userPhone.length > 6) settings.userPhone.take(6) + "****" else "SOS Victim",
                    locationName = location.address,
                    latitude = location.latitude,
                    longitude = location.longitude,
                    batteryLevel = batteryInfo.percentage,
                    timestamp = System.currentTimeMillis(),
                    status = "ACTIVE_DISTRESS",
                    emergencyType = "GENERAL_DANGER",
                    distanceMeters = 0,
                    responderCount = 0,
                    isUserTriggered = true,
                    customMessage = settings.customSosMessage
                )
                communityAlertDao.insertAlert(userAlert)
                NotificationHelper.showCommunityDistressNotification(context, userAlert, settings.isBengali)
            }

            // Post SOS notification
            NotificationHelper.showSosDispatchNotification(
                context = context,
                contactsCount = sentCount,
                locationAddress = location.address,
                isBengali = settings.isBengali
            )

            // Save in Alert History
            val history = AlertHistory(
                timestamp = System.currentTimeMillis(),
                triggerSource = triggerSource,
                latitude = location.latitude,
                longitude = location.longitude,
                locationAddress = location.address,
                batteryPercentage = batteryInfo.percentage,
                contactsNotified = sentCount,
                smsStatus = if (smsResult.isSuccess && sentCount > 0) "DELIVERED" else if (contacts.isEmpty()) "NO_CONTACTS" else "FALLBACK_INTENT",
                communityBroadcasted = settings.isCommunityBroadcastEnabled
            )
            alertHistoryDao.insertHistory(history)

            // Auto call primary contact if designated
            val primaryContact = contactDao.getPrimaryContact()
            if (primaryContact != null && primaryContact.autoCall && primaryContact.phone.isNotBlank()) {
                withContext(Dispatchers.Main) {
                    SmsHelper.makePhoneCall(context, primaryContact.phone)
                }
            }

            SosDispatchResult(
                isSuccess = true,
                contactsNotified = sentCount,
                location = location,
                batteryInfo = batteryInfo,
                message = formattedMessage,
                error = smsResult.exceptionOrNull()?.localizedMessage
            )
        }
}
