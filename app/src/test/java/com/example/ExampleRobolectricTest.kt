package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.util.BatteryInfo
import com.example.util.DeviceLocation
import com.example.util.SmsHelper
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("SaveMe", appName)
  }

  @Test
  fun `verify emergency SMS message formatting in Bengali`() {
    val location = DeviceLocation(
      latitude = 23.8103,
      longitude = 90.4125,
      accuracy = 10.0f,
      address = "Dhanmondi, Dhaka",
      mapsUrl = "https://maps.google.com/?q=23.8103,90.4125"
    )
    val batteryInfo = BatteryInfo(
      percentage = 85,
      isCharging = false,
      temperature = 29.0f,
      voltage = 4100,
      health = "Good"
    )

    val msg = SmsHelper.formatEmergencyMessage(
      userName = "রহিম",
      location = location,
      batteryInfo = batteryInfo,
      customNote = "B+",
      isBengali = true
    )

    assertTrue(msg.contains("জরুরি বিপদ সংকেত"))
    assertTrue(msg.contains("85%"))
    assertTrue(msg.contains("Dhanmondi, Dhaka"))
    assertTrue(msg.contains("maps.google.com"))
  }
}
