package com.example.data.local

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class UserSettings(
    val userName: String = "আব্দুর রহিম (Rahim)",
    val userPhone: String = "+8801700112233",
    val bloodGroup: String = "B+",
    val medicalNote: String = "অ্যাজমা রোগী / Asthma patient",
    val isPowerButtonDetectionEnabled: Boolean = true,
    val isCommunityBroadcastEnabled: Boolean = true,
    val isSoundAlarmEnabled: Boolean = true,
    val isStrobeFlashEnabled: Boolean = true,
    val countdownSeconds: Int = 3,
    val isBengali: Boolean = true,
    val customSosMessage: String = "আমি চরম বিপদে পড়েছি! দ্রুত এই লোকেশনে সাহায্য পাঠান।"
)

class UserPreferences(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("emergency_sos_prefs", Context.MODE_PRIVATE)

    private val _settings = MutableStateFlow(loadSettings())
    val settings: StateFlow<UserSettings> = _settings.asStateFlow()

    private fun loadSettings(): UserSettings {
        return UserSettings(
            userName = prefs.getString("user_name", "আব্দুর রহিম (Rahim)") ?: "আব্দুর রহিম",
            userPhone = prefs.getString("user_phone", "+8801700112233") ?: "+8801700112233",
            bloodGroup = prefs.getString("blood_group", "B+") ?: "B+",
            medicalNote = prefs.getString("medical_note", "জরুরি সাহায্য প্রয়োজন") ?: "",
            isPowerButtonDetectionEnabled = prefs.getBoolean("power_button_enabled", true),
            isCommunityBroadcastEnabled = prefs.getBoolean("community_broadcast_enabled", true),
            isSoundAlarmEnabled = prefs.getBoolean("sound_alarm_enabled", true),
            isStrobeFlashEnabled = prefs.getBoolean("strobe_flash_enabled", true),
            countdownSeconds = prefs.getInt("countdown_seconds", 3),
            isBengali = prefs.getBoolean("is_bengali", true),
            customSosMessage = prefs.getString("custom_sos_message", "আমি বিপদে পড়েছি, সাহায্য প্রয়োজন!") ?: "আমি বিপদে পড়েছি!"
        )
    }

    fun updateSettings(newSettings: UserSettings) {
        prefs.edit().apply {
            putString("user_name", newSettings.userName)
            putString("user_phone", newSettings.userPhone)
            putString("blood_group", newSettings.bloodGroup)
            putString("medical_note", newSettings.medicalNote)
            putBoolean("power_button_enabled", newSettings.isPowerButtonDetectionEnabled)
            putBoolean("community_broadcast_enabled", newSettings.isCommunityBroadcastEnabled)
            putBoolean("sound_alarm_enabled", newSettings.isSoundAlarmEnabled)
            putBoolean("strobe_flash_enabled", newSettings.isStrobeFlashEnabled)
            putInt("countdown_seconds", newSettings.countdownSeconds)
            putBoolean("is_bengali", newSettings.isBengali)
            putString("custom_sos_message", newSettings.customSosMessage)
            apply()
        }
        _settings.value = newSettings
    }

    fun toggleLanguage() {
        val current = _settings.value
        updateSettings(current.copy(isBengali = !current.isBengali))
    }

    fun togglePowerButtonDetection(enabled: Boolean) {
        val current = _settings.value
        updateSettings(current.copy(isPowerButtonDetectionEnabled = enabled))
    }
}
