package com.example.cashcontrol.data.network.local

import com.example.cashcontrol.data.db.ProfileDao
import com.example.cashcontrol.data.db.entity.Profile
import com.example.cashcontrol.data.db.entity.relation.ProfileWithDateFrames
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ProfileLocalDataSource @Inject constructor(private val profileDao: ProfileDao) {

    suspend fun upsertProfile (profile: Profile) {
        profileDao.upsertProfile(profile)
    }

    suspend fun deleteProfile (profile: Profile) {
        profileDao.deleteProfile(profile)
    }

    fun getAllProfilesFromDb (): Flow<List<Profile>> {
        return profileDao.getAllProfilesFromDb()
    }

    suspend fun getOnlineProfileById (userId: Int): List<Profile> {
        return profileDao.getOnlineProfileById(userId)
    }

    suspend fun getProfileOfUserByName (userId: Int, profileName: String): List<Profile> {
        return profileDao.getProfileOfUserByName(userId, profileName)
    }

    suspend fun getProfileWithDateFrames (profileId: Int): List<ProfileWithDateFrames> {
        return profileDao.getProfileWithDateFrames(profileId)
    }

}