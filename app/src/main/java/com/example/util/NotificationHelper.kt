package com.example.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R
import com.example.data.model.CommunityAlert

object NotificationHelper {

    const val CHANNEL_COMMUNITY_ALERT = "community_distress_alert_channel"
    const val CHANNEL_SOS_DISPATCH = "sos_dispatch_status_channel"

    fun initNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(NotificationManager::class.java) ?: return

            // 1. Community Distress Channel (High Priority Heads-Up Alert)
            val communityChannel = NotificationChannel(
                CHANNEL_COMMUNITY_ALERT,
                "Community Distress & Victim Alerts",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifies nearby users when someone in the community triggers emergency SOS"
                enableLights(true)
                lightColor = Color.RED
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 500, 200, 500, 200, 800)
                setShowBadge(true)
            }

            // 2. SOS Dispatch Channel
            val sosChannel = NotificationChannel(
                CHANNEL_SOS_DISPATCH,
                "SOS Emergency Broadcast Status",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Displays the real-time status of outgoing SOS alerts and location dispatches"
                enableLights(true)
                lightColor = Color.RED
                setShowBadge(true)
            }

            notificationManager.createNotificationChannel(communityChannel)
            notificationManager.createNotificationChannel(sosChannel)
        }
    }

    /**
     * Dispatches a high-priority heads-up notification when a nearby community user triggers SOS.
     */
    fun showCommunityDistressNotification(context: Context, alert: CommunityAlert, isBengali: Boolean = true) {
        initNotificationChannels(context)

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager ?: return

        val openAppIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra("EXTRA_COMMUNITY_ALERT_ID", alert.id)
            putExtra("EXTRA_OPEN_COMMUNITY", true)
        }
        val appPendingIntent = PendingIntent.getActivity(
            context,
            alert.id.hashCode(),
            openAppIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        // Maps Intent Action
        val mapsUri = Uri.parse("https://maps.google.com/?q=${alert.latitude},${alert.longitude}")
        val mapIntent = Intent(Intent.ACTION_VIEW, mapsUri).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        val mapPendingIntent = PendingIntent.getActivity(
            context,
            alert.id.hashCode() + 1,
            mapIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val title = if (isBengali) "🚨 আশেপাশের বিপদ সংকেত! (${alert.victimName})" else "🚨 Nearby SOS Alert! (${alert.victimName})"
        val content = if (isBengali) {
            "লোকেশন: ${alert.locationName} (${alert.distanceMeters}মি দূরে)\nব্যাটারি: ${alert.batteryLevel}% | স্ট্যাটাস: ${alert.customMessage.ifBlank { "বিপদগ্রস্ত" }}"
        } else {
            "Location: ${alert.locationName} (${alert.distanceMeters}m away)\nBattery: ${alert.batteryLevel}% | Status: ${alert.customMessage.ifBlank { "In Danger" }}"
        }

        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        val notification = NotificationCompat.Builder(context, CHANNEL_COMMUNITY_ALERT)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle(title)
            .setContentText(content)
            .setStyle(NotificationCompat.BigTextStyle().bigText(content))
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(true)
            .setSound(defaultSoundUri)
            .setVibrate(longArrayOf(0, 500, 200, 500, 200, 800))
            .setContentIntent(appPendingIntent)
            .addAction(
                android.R.drawable.ic_dialog_map,
                if (isBengali) "📍 লোকেশন দেখুন" else "📍 View Location",
                mapPendingIntent
            )
            .addAction(
                android.R.drawable.ic_menu_help,
                if (isBengali) "🚨 সাহায্য করুন" else "🚨 Respond",
                appPendingIntent
            )
            .build()

        notificationManager.notify(alert.id.hashCode(), notification)
    }

    /**
     * Displays notification confirming emergency SOS dispatch to emergency contacts.
     */
    fun showSosDispatchNotification(
        context: Context,
        contactsCount: Int,
        locationAddress: String,
        isBengali: Boolean = true
    ) {
        initNotificationChannels(context)
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager ?: return

        val launchIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra("EXTRA_SOS_TRIGGERED", true)
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            9992,
            launchIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val title = if (isBengali) "🚨 জরুরি এলার্ট পাঠানো হয়েছে!" else "🚨 SOS Alert Broadcasted!"
        val text = if (isBengali) {
            "$contactsCount টি জরুরি কন্টাক্টে লাইভ লোকেশন ($locationAddress) ও SMS পাঠানো হয়েছে।"
        } else {
            "Live location ($locationAddress) and emergency SMS sent to $contactsCount contacts."
        }

        val notification = NotificationCompat.Builder(context, CHANNEL_SOS_DISPATCH)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle(title)
            .setContentText(text)
            .setStyle(NotificationCompat.BigTextStyle().bigText(text))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        notificationManager.notify(9992, notification)
    }
}
