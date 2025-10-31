package com.example.mimedicina.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ProfileDao {
    @Query("SELECT * FROM profiles ORDER BY name")
    fun getProfiles(): Flow<List<ProfileEntity>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(profile: ProfileEntity): Long

    @Delete
    suspend fun delete(profile: ProfileEntity)

    @Query("SELECT * FROM profiles WHERE id = :profileId")
    suspend fun getProfileById(profileId: Long): ProfileEntity?
}
