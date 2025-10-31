package com.example.mimedicina.model

data class Medicine(
    val id: Long,
    val profileId: Long,
    val name: String,
    val presentation: String,
    val comments: String,
    val photoUri: String?,
    val frequencyHours: Int,
    val startTimeMillis: Long,
    val nextReminderTimeMillis: Long,
    val alarmEnabled: Boolean
)
