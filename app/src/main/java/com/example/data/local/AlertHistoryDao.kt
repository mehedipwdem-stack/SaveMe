package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.AlertHistory
import kotlinx.coroutines.flow.Flow

@Dao
interface AlertHistoryDao {
    @Query("SELECT * FROM alert_history ORDER BY timestamp DESC")
    fun getAllHistory(): Flow<List<AlertHistory>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistory(history: AlertHistory): Long

    @Delete
    suspend fun deleteHistory(history: AlertHistory)

    @Query("DELETE FROM alert_history")
    suspend fun clearHistory()
}
