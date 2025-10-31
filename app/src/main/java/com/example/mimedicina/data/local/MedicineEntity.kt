package com.example.mimedicina.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "medicines",
    foreignKeys = [
        ForeignKey(
            entity = ProfileEntity::class,
            parentColumns = ["id"],
            childColumns = ["profile_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["profile_id"]),
        Index(value = ["name"])
    ]
)
data class MedicineEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    @ColumnInfo(name = "profile_id")
    val profileId: Long,
    @ColumnInfo(name = "name")
    val name: String,
    @ColumnInfo(name = "presentation")
    val presentation: String,
    @ColumnInfo(name = "comments")
    val comments: String,
    @ColumnInfo(name = "photo_uri")
    val photoUri: String?,
    @ColumnInfo(name = "frequency_hours")
    val frequencyHours: Int,
    @ColumnInfo(name = "start_time_millis")
    val startTimeMillis: Long,
    @ColumnInfo(name = "next_reminder_time_millis")
    val nextReminderTimeMillis: Long,
    @ColumnInfo(name = "alarm_enabled")
    val alarmEnabled: Boolean
)
