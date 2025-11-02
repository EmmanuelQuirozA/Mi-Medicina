package com.example.mimedicina.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "reminder_history",
    foreignKeys = [
        ForeignKey(
            entity = MedicineEntity::class,
            parentColumns = ["id"],
            childColumns = ["medicine_id"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = ProfileEntity::class,
            parentColumns = ["id"],
            childColumns = ["profile_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["profile_id"]),
        Index(value = ["medicine_id"])
    ]
)
data class ReminderHistoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    @ColumnInfo(name = "medicine_id")
    val medicineId: Long,
    @ColumnInfo(name = "profile_id")
    val profileId: Long,
    @ColumnInfo(name = "medicine_name")
    val medicineName: String,
    @ColumnInfo(name = "scheduled_time_millis")
    val scheduledTimeMillis: Long,
    @ColumnInfo(name = "action_time_millis")
    val actionTimeMillis: Long?,
    @ColumnInfo(name = "action_type")
    val actionType: String?
)
