package com.example.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.telephony.SmsManager
import com.example.data.model.EmergencyContact
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object SmsHelper {

    fun formatEmergencyMessage(
        userName: String,
        location: DeviceLocation,
        batteryInfo: BatteryInfo,
        customNote: String = "",
        isBengali: Boolean = true
    ): String {
        val timeStr = SimpleDateFormat("hh:mm a, dd MMM", Locale.getDefault()).format(Date())
        val chargingStatus = if (batteryInfo.isCharging) {
            if (isBengali) "চার্জ হচ্ছে" else "Charging"
        } else {
            if (isBengali) "ব্যাটারি লেভেল" else "Discharging"
        }

        return if (isBengali) {
            """
            🚨 জরুরি বিপদ সংকেত (SOS ALERT)! 🚨
            আমি বিপদে পড়েছি, দ্রুত সাহায্য প্রয়োজন!
            
            👤 নাম: ${if (userName.isNotBlank()) userName else "ব্যবহারকারী"}
            📍 অবস্থান: ${location.address}
            🗺️ লাইভ ম্যাপ: ${location.mapsUrl}
            🔋 ব্যাটারি: ${batteryInfo.percentage}% ($chargingStatus)
            ⏰ সময়: $timeStr
            ${if (customNote.isNotBlank()) "📝 নোট: $customNote" else ""}
            """.trimIndent()
        } else {
            """
            🚨 EMERGENCY SOS ALERT! 🚨
            I am in danger and need immediate assistance!
            
            👤 Name: ${if (userName.isNotBlank()) userName else "App User"}
            📍 Location: ${location.address}
            🗺️ Live Map: ${location.mapsUrl}
            🔋 Battery: ${batteryInfo.percentage}% ($chargingStatus)
            ⏰ Time: $timeStr
            ${if (customNote.isNotBlank()) "📝 Note: $customNote" else ""}
            """.trimIndent()
        }
    }

    fun sendSmsDirect(
        context: Context,
        contacts: List<EmergencyContact>,
        message: String
    ): Result<Int> {
        return try {
            val smsManager: SmsManager = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                context.getSystemService(SmsManager::class.java)
            } else {
                @Suppress("DEPRECATION")
                SmsManager.getDefault()
            }

            var sentCount = 0
            for (contact in contacts) {
                if (contact.sendSms && contact.phone.isNotBlank()) {
                    val cleanPhone = contact.phone.replace(" ", "").replace("-", "")
                    val parts = smsManager.divideMessage(message)
                    smsManager.sendMultipartTextMessage(cleanPhone, null, parts, null, null)
                    sentCount++
                }
            }
            Result.success(sentCount)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun openSmsAppFallback(context: Context, phone: String, message: String) {
        try {
            val intent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("smsto:$phone")
                putExtra("sms_body", message)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (_: Exception) {
            val genericIntent = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse("sms:$phone?body=${Uri.encode(message)}")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(genericIntent)
        }
    }

    fun makePhoneCall(context: Context, phoneNumber: String) {
        try {
            val cleanPhone = phoneNumber.replace(" ", "").replace("-", "")
            val intent = Intent(Intent.ACTION_CALL).apply {
                data = Uri.parse("tel:$cleanPhone")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: SecurityException) {
            // Fallback to dialer if CALL_PHONE permission not granted
            val dialIntent = Intent(Intent.ACTION_DIAL).apply {
                data = Uri.parse("tel:$phoneNumber")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(dialIntent)
        }
    }
}
