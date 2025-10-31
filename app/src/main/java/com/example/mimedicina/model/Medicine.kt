package com.example.mimedicina.model

import java.util.Date

data class Medicine(
    val name: String,
    val presentation: String,
    val comments: String,
    val photoUri: String? = null,
    val frequency: Long,
    val startDate: Date,
    val alarmEnabled: Boolean = false
)