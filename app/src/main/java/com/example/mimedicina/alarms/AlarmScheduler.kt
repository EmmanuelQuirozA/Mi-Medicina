package com.example.mimedicina.alarms

import com.example.mimedicina.model.Medicine

interface AlarmScheduler {
    fun schedule(medicine: Medicine)
    fun cancel(medicineId: Long)
}
