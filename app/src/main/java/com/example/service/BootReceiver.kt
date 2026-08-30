package com.example.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.data.local.UserPreferences

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action == Intent.ACTION_BOOT_COMPLETED && context != null) {
            val prefs = UserPreferences(context)
            if (prefs.settings.value.isPowerButtonDetectionEnabled) {
                SosTriggerService.startService(context)
            }
        }
    }
}
