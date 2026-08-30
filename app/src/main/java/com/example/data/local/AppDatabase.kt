package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.AlertHistory
import com.example.data.model.CommunityAlert
import com.example.data.model.EmergencyContact
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        EmergencyContact::class,
        CommunityAlert::class,
        AlertHistory::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun contactDao(): ContactDao
    abstract fun communityAlertDao(): CommunityAlertDao
    abstract fun alertHistoryDao(): AlertHistoryDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "emergency_sos_db"
                )
                    .addCallback(AppDatabaseCallback(scope))
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class AppDatabaseCallback(
            private val scope: CoroutineScope
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        populateInitialData(database)
                    }
                }
            }
        }

        suspend fun populateInitialData(database: AppDatabase) {
            val contactDao = database.contactDao()
            val communityDao = database.communityAlertDao()

            if (contactDao.getContactCount() == 0) {
                contactDao.insertContacts(
                    listOf(
                        EmergencyContact(
                            name = "মা / Mother (Family)",
                            phone = "+8801700000001",
                            relationship = "Mother (মা)",
                            isPrimary = true,
                            sendSms = true,
                            autoCall = true
                        ),
                        EmergencyContact(
                            name = "ভাই / Brother",
                            phone = "+8801800000002",
                            relationship = "Brother (ভাই)",
                            isPrimary = false,
                            sendSms = true,
                            autoCall = false
                        ),
                        EmergencyContact(
                            name = "জরুরি অভিভাবক / Guardian",
                            phone = "+8801900000003",
                            relationship = "Friend (বন্ধু)",
                            isPrimary = false,
                            sendSms = true,
                            autoCall = false
                        )
                    )
                )
            }

            if (communityDao.getAlertCount() == 0) {
                val now = System.currentTimeMillis()
                communityDao.insertAlerts(
                    listOf(
                        CommunityAlert(
                            id = "alert_101",
                            victimName = "তানজিলা আক্তার (Tanzila)",
                            phoneMasked = "+880171****89",
                            locationName = "ধানমন্ডি লেক রোড, ঢাকা (Dhanmondi Lake)",
                            latitude = 23.7461,
                            longitude = 90.3742,
                            batteryLevel = 19,
                            timestamp = now - (1000 * 60 * 4), // 4 mins ago
                            status = "ACTIVE_DISTRESS",
                            emergencyType = "HARASSMENT",
                            distanceMeters = 240,
                            responderCount = 2,
                            isUserTriggered = false,
                            customMessage = "অপরিচিত কয়েকজন পিছু নিয়েছে, জরুরি সাহায্য দরকার!"
                        ),
                        CommunityAlert(
                            id = "alert_102",
                            victimName = "রাকিবুল হাসান (Rakib)",
                            phoneMasked = "+880182****34",
                            locationName = "মিরপুর ১০ গোলচত্বর, ঢাকা (Mirpur 10)",
                            latitude = 23.8069,
                            longitude = 90.3687,
                            batteryLevel = 45,
                            timestamp = now - (1000 * 60 * 12), // 12 mins ago
                            status = "ACTIVE_DISTRESS",
                            emergencyType = "ACCIDENT",
                            distanceMeters = 850,
                            responderCount = 4,
                            isUserTriggered = false,
                            customMessage = "বাইক দুর্ঘটনা ঘটেছে। অ্যাম্বুলেন্স ও প্রাথমিক চিকিৎসার সহায়তা প্রয়োজন।"
                        ),
                        CommunityAlert(
                            id = "alert_103",
                            victimName = "নুসরাত জাহান (Nusrat)",
                            phoneMasked = "+880193****12",
                            locationName = "উত্তরা সেক্টর ৭, ঢাকা (Uttara Sec 7)",
                            latitude = 23.8759,
                            longitude = 90.3795,
                            batteryLevel = 82,
                            timestamp = now - (1000 * 60 * 25), // 25 mins ago
                            status = "RESPONDING",
                            emergencyType = "MEDICAL_EMERGENCY",
                            distanceMeters = 1600,
                            responderCount = 6,
                            isUserTriggered = false,
                            customMessage = "হঠাৎ তীব্র অসুস্থতা বোধ করছি, অক্সিজেন বা মেডিকেল হেল্প প্রয়োজন।"
                        )
                    )
                )
            }
        }
    }
}
