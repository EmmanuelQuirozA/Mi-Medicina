package com.example.mimedicina.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface MedicineDao {
    @Query(
        "SELECT * FROM medicines WHERE profile_id = :profileId ORDER BY next_reminder_time_millis"
    )
    fun getMedicinesForProfile(profileId: Long): Flow<List<MedicineEntity>>

    @Query("SELECT * FROM medicines WHERE id = :medicineId")
    suspend fun getMedicineById(medicineId: Long): MedicineEntity?

    @Query("SELECT * FROM medicines WHERE profile_id = :profileId")
    suspend fun getMedicinesForProfileOnce(profileId: Long): List<MedicineEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(medicine: MedicineEntity): Long

    @Update
    suspend fun update(medicine: MedicineEntity)

    @Delete
    suspend fun delete(medicine: MedicineEntity)
}
