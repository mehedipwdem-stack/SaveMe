package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.CommunityAlert
import kotlinx.coroutines.flow.Flow

@Dao
interface CommunityAlertDao {
    @Query("SELECT * FROM community_alerts ORDER BY timestamp DESC")
    fun getAllCommunityAlerts(): Flow<List<CommunityAlert>>

    @Query("SELECT * FROM community_alerts WHERE status = 'ACTIVE_DISTRESS' ORDER BY timestamp DESC")
    fun getActiveAlerts(): Flow<List<CommunityAlert>>

    @Query("SELECT * FROM community_alerts WHERE id = :id")
    suspend fun getAlertById(id: String): CommunityAlert?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAlert(alert: CommunityAlert)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAlerts(alerts: List<CommunityAlert>)

    @Update
    suspend fun updateAlert(alert: CommunityAlert)

    @Delete
    suspend fun deleteAlert(alert: CommunityAlert)

    @Query("UPDATE community_alerts SET status = :status, responderCount = responderCount + 1 WHERE id = :id")
    suspend fun updateAlertStatusAndResponder(id: String, status: String)

    @Query("UPDATE community_alerts SET status = 'SAFE' WHERE isUserTriggered = 1")
    suspend fun resolveUserAlerts()

    @Query("SELECT COUNT(*) FROM community_alerts")
    suspend fun getAlertCount(): Int
}
