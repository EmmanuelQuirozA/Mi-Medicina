package com.example.mimedicina.data.repository

import com.example.mimedicina.data.local.ReminderHistoryDao
import com.example.mimedicina.data.local.ReminderHistoryEntity
import com.example.mimedicina.model.Medicine
import com.example.mimedicina.model.ReminderAction
import com.example.mimedicina.model.ReminderHistory
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ReminderHistoryRepository(
    private val reminderHistoryDao: ReminderHistoryDao
) {

    fun observeHistory(profileId: Long): Flow<List<ReminderHistory>> =
        reminderHistoryDao.observeHistory(profileId).map { entries ->
            entries.map { it.toDomain() }
        }

    suspend fun recordReminderTriggered(medicine: Medicine) {
        val existing = reminderHistoryDao.getPendingReminder(medicine.id)
        if (existing?.scheduledTimeMillis == medicine.nextReminderTimeMillis) {
            return
        }
        val entity = ReminderHistoryEntity(
            medicineId = medicine.id,
            profileId = medicine.profileId,
            medicineName = medicine.name,
            scheduledTimeMillis = medicine.nextReminderTimeMillis,
            actionTimeMillis = null,
            actionType = null
        )
        reminderHistoryDao.insert(entity)
    }

    suspend fun markReminderAction(
        medicine: Medicine,
        action: ReminderAction,
        actionTimeMillis: Long
    ) {
        val pending = reminderHistoryDao.getPendingReminder(medicine.id)
        if (pending == null) {
            reminderHistoryDao.insert(
                ReminderHistoryEntity(
                    medicineId = medicine.id,
                    profileId = medicine.profileId,
                    medicineName = medicine.name,
                    scheduledTimeMillis = medicine.nextReminderTimeMillis,
                    actionTimeMillis = actionTimeMillis,
                    actionType = action.toStorageValue()
                )
            )
        } else {
            reminderHistoryDao.updateAction(
                historyId = pending.id,
                actionTimeMillis = actionTimeMillis,
                actionType = action.toStorageValue()
            )
        }
    }

    private fun ReminderHistoryEntity.toDomain() = ReminderHistory(
        id = id,
        medicineId = medicineId,
        profileId = profileId,
        medicineName = medicineName,
        scheduledTimeMillis = scheduledTimeMillis,
        actionTimeMillis = actionTimeMillis,
        action = ReminderAction.fromStorageValue(actionType)
    )
}
