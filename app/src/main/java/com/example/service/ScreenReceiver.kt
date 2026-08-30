package com.example.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class ScreenReceiver(
    private val onTriplePressDetected: () -> Unit
) : BroadcastReceiver() {

    private var clickCount = 0
    private var lastClickTimestamp = 0L
    private val timeWindowMillis = 3000L // 3 seconds window

    override fun onReceive(context: Context?, intent: Intent?) {
        val action = intent?.action
        if (action == Intent.ACTION_SCREEN_ON || action == Intent.ACTION_SCREEN_OFF) {
            val now = System.currentTimeMillis()

            if (now - lastClickTimestamp > timeWindowMillis) {
                clickCount = 1
            } else {
                clickCount++
            }
            lastClickTimestamp = now

            // 3 button actions detected (screen state toggles)
            if (clickCount >= 3) {
                clickCount = 0
                lastClickTimestamp = 0L
                onTriplePressDetected()
            }
        }
    }
}
