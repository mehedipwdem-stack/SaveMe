package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.EmergencyApp
import com.example.data.local.UserSettings
import com.example.data.model.AlertHistory
import com.example.data.model.CommunityAlert
import com.example.data.model.EmergencyContact
import com.example.data.repository.SosDispatchResult
import com.example.service.SosTriggerService
import com.example.util.BatteryHelper
import com.example.util.BatteryInfo
import com.example.util.DeviceLocation
import com.example.util.FlashlightHelper
import com.example.util.LocationHelper
import com.example.util.SirenAudioPlayer
import com.example.util.SmsHelper
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class AppScreen {
    HOME,
    COMMUNITY,
    CONTACTS,
    TOOLS
}

data class EmergencyUiState(
    val isEmergencyActive: Boolean = false,
    val emergencyTriggerSource: String = "",
    val countdownRemaining: Int? = null, // null when not in countdown
    val isSirenPlaying: Boolean = false,
    val isStrobeActive: Boolean = false,
    val isFakeCallActive: Boolean = false,
    val batteryInfo: BatteryInfo = BatteryInfo(85, false, 28.5f, 4000, "Good"),
    val currentLocation: DeviceLocation? = null,
    val isLoadingLocation: Boolean = false,
    val lastDispatchResult: SosDispatchResult? = null,
    val statusMessage: String? = null
)

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as EmergencyApp
    private val repository = app.repository

    val contacts: StateFlow<List<EmergencyContact>> = repository.allContacts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val communityAlerts: StateFlow<List<CommunityAlert>> = repository.allCommunityAlerts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val alertHistory: StateFlow<List<AlertHistory>> = repository.alertHistory
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val settings: StateFlow<UserSettings> = repository.userSettings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UserSettings())

    private val _uiState = MutableStateFlow(EmergencyUiState())
    val uiState: StateFlow<EmergencyUiState> = _uiState.asStateFlow()

    private val _currentScreen = MutableStateFlow(AppScreen.HOME)
    val currentScreen: StateFlow<AppScreen> = _currentScreen.asStateFlow()

    private var countdownJob: Job? = null

    init {
        refreshBatteryInfo()
        refreshLocation()
        observeBattery()
    }

    fun setScreen(screen: AppScreen) {
        _currentScreen.value = screen
    }

    private fun observeBattery() {
        viewModelScope.launch {
            BatteryHelper.observeBattery(app).collect { info ->
                _uiState.value = _uiState.value.copy(batteryInfo = info)
            }
        }
    }

    fun refreshBatteryInfo() {
        val info = BatteryHelper.getBatteryInfo(app)
        _uiState.value = _uiState.value.copy(batteryInfo = info)
    }

    fun refreshLocation() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoadingLocation = true)
            val location = LocationHelper.getCurrentLocation(app)
            _uiState.value = _uiState.value.copy(
                currentLocation = location,
                isLoadingLocation = false
            )
        }
    }

    fun triggerSosWithCountdown(source: String = "SOS_BUTTON") {
        val count = settings.value.countdownSeconds
        if (count <= 0) {
            executeSos(source)
            return
        }

        countdownJob?.cancel()
        countdownJob = viewModelScope.launch {
            for (i in count downTo 1) {
                _uiState.value = _uiState.value.copy(countdownRemaining = i)
                delay(1000)
            }
            _uiState.value = _uiState.value.copy(countdownRemaining = null)
            executeSos(source)
        }
    }

    fun cancelSosCountdown() {
        countdownJob?.cancel()
        countdownJob = null
        _uiState.value = _uiState.value.copy(
            countdownRemaining = null,
            statusMessage = if (settings.value.isBengali) "এলার্ট বাতিল করা হয়েছে" else "SOS Cancelled"
        )
    }

    fun executeSos(source: String = "SOS_BUTTON") {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isEmergencyActive = true,
                emergencyTriggerSource = source,
                countdownRemaining = null
            )

            // Start Audio Siren if enabled
            if (settings.value.isSoundAlarmEnabled) {
                SirenAudioPlayer.startSiren(viewModelScope)
                _uiState.value = _uiState.value.copy(isSirenPlaying = true)
            }

            // Start Flashlight Strobe if enabled
            if (settings.value.isStrobeFlashEnabled) {
                FlashlightHelper.startSosStrobe(app, viewModelScope)
                _uiState.value = _uiState.value.copy(isStrobeActive = true)
            }

            val result = repository.executeSosDispatch(source)
            _uiState.value = _uiState.value.copy(
                lastDispatchResult = result,
                currentLocation = result.location,
                batteryInfo = result.batteryInfo,
                statusMessage = if (settings.value.isBengali) "জরুরি এলার্ট পাঠানো হয়েছে (${result.contactsNotified} টি কন্ট্যাক্ট)" else "SOS Broadcasted to ${result.contactsNotified} contacts"
            )
        }
    }

    fun cancelActiveEmergency() {
        viewModelScope.launch {
            SirenAudioPlayer.stopSiren()
            FlashlightHelper.stopStrobe(app)
            repository.resolveAllUserAlerts()

            _uiState.value = _uiState.value.copy(
                isEmergencyActive = false,
                isSirenPlaying = false,
                isStrobeActive = false,
                statusMessage = if (settings.value.isBengali) "আপনি নিরাপদ আছেন (Safe Mode)" else "Marked Safe"
            )
        }
    }

    fun toggleSiren() {
        if (_uiState.value.isSirenPlaying) {
            SirenAudioPlayer.stopSiren()
            _uiState.value = _uiState.value.copy(isSirenPlaying = false)
        } else {
            SirenAudioPlayer.startSiren(viewModelScope)
            _uiState.value = _uiState.value.copy(isSirenPlaying = true)
        }
    }

    fun toggleStrobe() {
        if (_uiState.value.isStrobeActive) {
            FlashlightHelper.stopStrobe(app)
            _uiState.value = _uiState.value.copy(isStrobeActive = false)
        } else {
            FlashlightHelper.startSosStrobe(app, viewModelScope)
            _uiState.value = _uiState.value.copy(isStrobeActive = true)
        }
    }

    fun startFakeCall() {
        _uiState.value = _uiState.value.copy(isFakeCallActive = true)
    }

    fun endFakeCall() {
        _uiState.value = _uiState.value.copy(isFakeCallActive = false)
    }

    fun call999Emergency(number: String = "999") {
        SmsHelper.makePhoneCall(app, number)
    }

    fun respondToCommunityVictim(alert: CommunityAlert) {
        viewModelScope.launch {
            repository.respondToCommunityAlert(alert.id)
            _uiState.value = _uiState.value.copy(
                statusMessage = if (settings.value.isBengali) "${alert.victimName}-এর জন্য রেসপন্স পাঠানো হয়েছে" else "Responded to ${alert.victimName}"
            )
        }
    }

    fun markCommunityAlertSafe(alert: CommunityAlert) {
        viewModelScope.launch {
            repository.markAlertSafe(alert)
        }
    }

    fun simulateIncomingDistressAlert() {
        viewModelScope.launch {
            val randomLat = 23.8103 + (Math.random() - 0.5) * 0.015
            val randomLng = 90.4125 + (Math.random() - 0.5) * 0.015
            val distance = (150..850).random()
            val sampleNames = listOf("তানজিলা আকতার", "সাকিব মাহমুদ", "নুসরাত জাহান", "আরিফুল ইসলাম", "মেহনাজ করিম")
            val sampleLocations = listOf("ধানমন্ডি ২৭", "গুলশান ২ চত্বর", "মিরপুর ১০ মোড়", "শাহবাগ মোড়", "উত্তরা সেক্টর ৭")
            val sampleName = sampleNames.random()
            val sampleLoc = sampleLocations.random()

            val mockAlert = CommunityAlert(
                id = "alert_${System.currentTimeMillis()}",
                victimName = sampleName,
                phoneMasked = "+88017****${(1000..9999).random()}",
                locationName = sampleLoc,
                latitude = randomLat,
                longitude = randomLng,
                batteryLevel = (12..48).random(),
                timestamp = System.currentTimeMillis(),
                status = "ACTIVE_DISTRESS",
                emergencyType = "STREET_HARASSMENT",
                distanceMeters = distance,
                responderCount = 0,
                isUserTriggered = false,
                customMessage = if (settings.value.isBengali) "জরুরি সাহায্য প্রয়োজন! আমাকে কয়েকজন অনুসরণ করছে।" else "Emergency! Need immediate assistance."
            )
            repository.triggerIncomingCommunityAlert(mockAlert)
            _uiState.value = _uiState.value.copy(
                statusMessage = if (settings.value.isBengali) "নতুন বিপদ সংকেত নোটিফিকেশন পাঠানো হয়েছে!" else "New nearby distress notification posted!"
            )
        }
    }

    fun addContact(contact: EmergencyContact) {
        viewModelScope.launch {
            repository.addContact(contact)
        }
    }

    fun updateContact(contact: EmergencyContact) {
        viewModelScope.launch {
            repository.updateContact(contact)
        }
    }

    fun deleteContact(contact: EmergencyContact) {
        viewModelScope.launch {
            repository.deleteContact(contact)
        }
    }

    fun setPrimaryContact(contactId: Long) {
        viewModelScope.launch {
            repository.setPrimaryContact(contactId)
        }
    }

    fun updateSettings(newSettings: UserSettings) {
        app.userPreferences.updateSettings(newSettings)
        if (newSettings.isPowerButtonDetectionEnabled) {
            SosTriggerService.startService(app)
        } else {
            SosTriggerService.stopService(app)
        }
    }

    fun toggleLanguage() {
        app.userPreferences.toggleLanguage()
    }

    fun clearStatusMessage() {
        _uiState.value = _uiState.value.copy(statusMessage = null)
    }
}
