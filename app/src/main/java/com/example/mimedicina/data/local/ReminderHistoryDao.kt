package com.example.mimedicina.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ReminderHistoryDao {
    @Query(
        "SELECT * FROM reminder_history WHERE profile_id = :profileId AND action_time_millis IS NOT NULL ORDER BY action_time_millis DESC"
    )
    fun observeHistory(profileId: Long): Flow<List<ReminderHistoryEntity>>

    @Query(
        "SELECT * FROM reminder_history WHERE medicine_id = :medicineId AND action_time_millis IS NULL ORDER BY scheduled_time_millis DESC LIMIT 1"
    )
    suspend fun getPendingReminder(medicineId: Long): ReminderHistoryEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(history: ReminderHistoryEntity): Long

    @Query(
        "UPDATE reminder_history SET action_time_millis = :actionTimeMillis, action_type = :actionType WHERE id = :historyId"
    )
    suspend fun updateAction(historyId: Long, actionTimeMillis: Long, actionType: String)
}
