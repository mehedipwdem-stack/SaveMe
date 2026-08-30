package com.example.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.IBinder
import android.content.pm.ServiceInfo
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import com.example.EmergencyApp
import com.example.MainActivity
import com.example.R
import com.example.util.FlashlightHelper
import com.example.util.SirenAudioPlayer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class SosTriggerService : Service() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var screenReceiver: ScreenReceiver? = null

    companion object {
        const val CHANNEL_ID = "sos_power_button_channel"
        const val NOTIFICATION_ID = 9991
        const val ACTION_START_MONITORING = "com.example.service.START_MONITORING"
        const val ACTION_STOP_MONITORING = "com.example.service.STOP_MONITORING"
        const val ACTION_TRIGGER_SOS_IMMEDIATE = "com.example.service.TRIGGER_SOS_IMMEDIATE"

        fun startService(context: Context) {
            val intent = Intent(context, SosTriggerService::class.java).apply {
                action = ACTION_START_MONITORING
            }
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(intent)
                } else {
                    context.startService(intent)
                }
            } catch (_: Exception) {
            }
        }

        fun stopService(context: Context) {
            val intent = Intent(context, SosTriggerService::class.java).apply {
                action = ACTION_STOP_MONITORING
            }
            try {
                context.startService(intent)
            } catch (_: Exception) {
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_STOP_MONITORING -> {
                unregisterScreenReceiver()
                try {
                    stopForeground(STOP_FOREGROUND_REMOVE)
                } catch (_: Exception) {}
                stopSelf()
                return START_NOT_STICKY
            }
            ACTION_TRIGGER_SOS_IMMEDIATE -> {
                executeImmediateSos("POWER_BUTTON_3X")
            }
            else -> {
                try {
                    val notification = buildForegroundNotification()
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        ServiceCompat.startForeground(
                            this,
                            NOTIFICATION_ID,
                            notification,
                            ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
                        )
                    } else {
                        startForeground(NOTIFICATION_ID, notification)
                    }
                } catch (_: Exception) {}
                registerScreenReceiver()
            }
        }
        return START_STICKY
    }

    private fun registerScreenReceiver() {
        if (screenReceiver == null) {
            screenReceiver = ScreenReceiver {
                // Triple press detected!
                executeImmediateSos("POWER_BUTTON_3X")
            }
            val filter = IntentFilter().apply {
                addAction(Intent.ACTION_SCREEN_ON)
                addAction(Intent.ACTION_SCREEN_OFF)
            }
            registerReceiver(screenReceiver, filter)
        }
    }

    private fun unregisterScreenReceiver() {
        screenReceiver?.let {
            try {
                unregisterReceiver(it)
            } catch (_: Exception) {}
            screenReceiver = null
        }
    }

    private fun executeImmediateSos(source: String) {
        val app = application as? EmergencyApp ?: return

        serviceScope.launch {
            val settings = app.userPreferences.settings.value

            // Activate Siren if enabled
            if (settings.isSoundAlarmEnabled) {
                SirenAudioPlayer.startSiren(serviceScope)
            }
            // Activate Flashlight strobe if enabled
            if (settings.isStrobeFlashEnabled) {
                FlashlightHelper.startSosStrobe(applicationContext, serviceScope)
            }

            // Dispatch emergency SMS, Location, Battery, & Community Alert
            app.repository.executeSosDispatch(source)

            // Open App to Emergency Active screen
            val launchIntent = Intent(applicationContext, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                putExtra("EXTRA_SOS_TRIGGERED", true)
            }
            startActivity(launchIntent)
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Emergency SOS Protection",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Monitors 3x Power Button presses for immediate emergency distress alert"
                setShowBadge(false)
            }
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager?.createNotificationChannel(channel)
        }
    }

    private fun buildForegroundNotification(): Notification {
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("🛡️ Emergency Protection Active")
            .setContentText("পাওয়ার বাটন ৩ বার চাপলে সাথে সাথে SOS এলার্ট যাবে")
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    override fun onDestroy() {
        unregisterScreenReceiver()
        serviceScope.cancel()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
