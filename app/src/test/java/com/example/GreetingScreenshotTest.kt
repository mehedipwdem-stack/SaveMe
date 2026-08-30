package com.example

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.example.data.local.UserSettings
import com.example.data.model.CommunityAlert
import com.example.data.model.EmergencyContact
import com.example.ui.EmergencyUiState
import com.example.ui.screens.HomeScreen
import com.example.ui.theme.MyApplicationTheme
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = RobolectricDeviceQualifiers.Pixel8, sdk = [34])
class GreetingScreenshotTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun home_screen_screenshot() {
    composeTestRule.setContent {
      MyApplicationTheme(darkTheme = true) {
        HomeScreen(
          uiState = EmergencyUiState(),
          settings = UserSettings(),
          contacts = listOf(
            EmergencyContact(1, "মা (Mother)", "+8801700000001", "Mother", true, true, false)
          ),
          communityAlerts = listOf(
            CommunityAlert("1", "তানজিলা", "+880171****89", "ধানমন্ডি, ঢাকা", 23.7461, 90.3742, 20, System.currentTimeMillis())
          ),
          onSosClick = {},
          onCancelCountdown = {},
          onCancelEmergency = {},
          onRefreshLocation = {},
          onCallNumber = {},
          onToggleSiren = {},
          onToggleStrobe = {},
          onStartFakeCall = {},
          onNavigateToScreen = {},
          onToggleLanguage = {}
        )
      }
    }

    composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/greeting.png")
  }
}
