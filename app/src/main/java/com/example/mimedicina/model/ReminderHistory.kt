package com.example.mimedicina.model

data class ReminderHistory(
    val id: Long,
    val medicineId: Long,
    val profileId: Long,
    val medicineName: String,
    val scheduledTimeMillis: Long,
    val actionTimeMillis: Long?,
    val action: ReminderAction?
)
