package com.example.mimedicina

import android.app.Application
import androidx.room.Room
import com.example.mimedicina.alarms.AlarmScheduler
import com.example.mimedicina.alarms.AndroidAlarmScheduler
import com.example.mimedicina.data.local.AppDatabase
import com.example.mimedicina.data.local.AppDatabaseMigrations
import com.example.mimedicina.data.repository.MedicinesRepository
import com.example.mimedicina.data.repository.ProfilesRepository
import com.example.mimedicina.data.repository.ReminderHistoryRepository

class MiMedicinaApp : Application() {
    lateinit var database: AppDatabase
        private set

    lateinit var profilesRepository: ProfilesRepository
        private set

    lateinit var medicinesRepository: MedicinesRepository
        private set

    lateinit var reminderHistoryRepository: ReminderHistoryRepository
        private set

    lateinit var alarmScheduler: AlarmScheduler
        private set

    override fun onCreate() {
        super.onCreate()
        database = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "mi_medicina.db"
        )
            .addMigrations(AppDatabaseMigrations.MIGRATION_1_2)
            .build()

        profilesRepository = ProfilesRepository(database.profileDao())
        medicinesRepository = MedicinesRepository(database.medicineDao())
        reminderHistoryRepository = ReminderHistoryRepository(database.reminderHistoryDao())
        alarmScheduler = AndroidAlarmScheduler(this)
    }
}
