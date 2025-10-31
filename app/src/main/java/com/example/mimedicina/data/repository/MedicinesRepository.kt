package com.example.mimedicina.data.repository

import com.example.mimedicina.data.local.MedicineDao
import com.example.mimedicina.data.local.MedicineEntity
import com.example.mimedicina.model.Medicine
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class MedicinesRepository(
    private val medicineDao: MedicineDao
) {
    fun observeMedicines(profileId: Long): Flow<List<Medicine>> =
        medicineDao.getMedicinesForProfile(profileId).map { entities ->
            entities.map { it.toDomain() }
        }

    suspend fun addMedicine(medicine: Medicine): Long {
        val id = medicineDao.insert(medicine.toEntity())
        if (id == -1L) {
            throw IllegalStateException("No se pudo guardar el medicamento")
        }
        return id
    }

    suspend fun updateMedicine(medicine: Medicine) {
        medicineDao.update(medicine.toEntity())
    }

    suspend fun deleteMedicine(medicine: Medicine) {
        medicineDao.delete(medicine.toEntity())
    }

    suspend fun getMedicine(id: Long): Medicine? =
        medicineDao.getMedicineById(id)?.toDomain()

    suspend fun getMedicinesForProfile(profileId: Long): List<Medicine> =
        medicineDao.getMedicinesForProfileOnce(profileId).map { it.toDomain() }

    private fun MedicineEntity.toDomain() = Medicine(
        id = id,
        profileId = profileId,
        name = name,
        presentation = presentation,
        comments = comments,
        photoUri = photoUri,
        frequencyHours = frequencyHours,
        startTimeMillis = startTimeMillis,
        nextReminderTimeMillis = nextReminderTimeMillis,
        alarmEnabled = alarmEnabled
    )

    private fun Medicine.toEntity() = MedicineEntity(
        id = id,
        profileId = profileId,
        name = name,
        presentation = presentation,
        comments = comments,
        photoUri = photoUri,
        frequencyHours = frequencyHours,
        startTimeMillis = startTimeMillis,
        nextReminderTimeMillis = nextReminderTimeMillis,
        alarmEnabled = alarmEnabled
    )
}
