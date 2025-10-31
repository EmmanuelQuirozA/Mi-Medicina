package com.example.mimedicina.data.repository

import com.example.mimedicina.data.local.ProfileDao
import com.example.mimedicina.data.local.ProfileEntity
import com.example.mimedicina.model.Profile
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ProfilesRepository(
    private val profileDao: ProfileDao
) {
    fun observeProfiles(): Flow<List<Profile>> =
        profileDao.getProfiles().map { entities ->
            entities.map { it.toDomain() }
        }

    suspend fun addProfile(name: String): Result<Long> {
        val id = profileDao.insert(ProfileEntity(name = name))
        return if (id == -1L) {
            Result.failure(IllegalArgumentException("Ya existe un perfil con ese nombre"))
        } else {
            Result.success(id)
        }
    }

    suspend fun removeProfile(profile: Profile) {
        profileDao.delete(profile.toEntity())
    }

    suspend fun getProfile(id: Long): Profile? =
        profileDao.getProfileById(id)?.toDomain()

    private fun ProfileEntity.toDomain() = Profile(id = id, name = name)

    private fun Profile.toEntity() = ProfileEntity(id = id, name = name)
}
