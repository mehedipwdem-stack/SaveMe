package com.example.util

import android.content.Context
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

object FlashlightHelper {

    private var flashJob: Job? = null
    private var isStrobeRunning = false

    fun isStrobing(): Boolean = isStrobeRunning

    fun startSosStrobe(context: Context, scope: CoroutineScope) {
        if (isStrobeRunning) return
        isStrobeRunning = true

        val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as? CameraManager ?: return
        val cameraId = try {
            cameraManager.cameraIdList.firstOrNull { id ->
                val chars = cameraManager.getCameraCharacteristics(id)
                chars.get(android.hardware.camera2.CameraCharacteristics.FLASH_INFO_AVAILABLE) == true
            }
        } catch (_: Exception) {
            null
        } ?: return

        flashJob = scope.launch(Dispatchers.Default) {
            try {
                while (isActive && isStrobeRunning) {
                    // SOS Morse code: 3 short, 3 long, 3 short
                    val pattern = listOf(
                        150L, 150L, 150L, 150L, 150L, 300L, // 3 dots
                        400L, 200L, 400L, 200L, 400L, 300L, // 3 dashes
                        150L, 150L, 150L, 150L, 150L, 600L  // 3 dots
                    )

                    var on = true
                    for (duration in pattern) {
                        if (!isActive || !isStrobeRunning) break
                        try {
                            cameraManager.setTorchMode(cameraId, on)
                        } catch (_: CameraAccessException) {}
                        delay(duration)
                        on = !on
                    }
                }
            } catch (_: Exception) {
            } finally {
                try {
                    cameraManager.setTorchMode(cameraId, false)
                } catch (_: Exception) {}
            }
        }
    }

    fun stopStrobe(context: Context) {
        isStrobeRunning = false
        flashJob?.cancel()
        flashJob = null
        try {
            val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as? CameraManager
            cameraManager?.cameraIdList?.forEach { id ->
                try {
                    cameraManager.setTorchMode(id, false)
                } catch (_: Exception) {}
            }
        } catch (_: Exception) {}
    }
}
