package com.example

import android.app.Application
import com.example.data.local.AppDatabase
import com.example.data.local.UserPreferences
import com.example.data.repository.EmergencyRepository
import com.example.service.SosTriggerService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class EmergencyApp : Application() {

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    val database by lazy { AppDatabase.getDatabase(this, applicationScope) }
    val userPreferences by lazy { UserPreferences(this) }
    val repository by lazy {
        EmergencyRepository(
            context = this,
            contactDao = database.contactDao(),
            communityAlertDao = database.communityAlertDao(),
            alertHistoryDao = database.alertHistoryDao(),
            userPreferences = userPreferences
        )
    }

    override fun onCreate() {
        super.onCreate()
        applicationScope.launch {
            if (userPreferences.settings.value.isPowerButtonDetectionEnabled) {
                SosTriggerService.startService(this@EmergencyApp)
            }
        }
    }
}
