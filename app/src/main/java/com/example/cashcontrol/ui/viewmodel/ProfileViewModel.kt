package com.example.cashcontrol.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cashcontrol.data.repository.CashControlRepository
import com.example.cashcontrol.data.db.entity.Profile
import com.example.cashcontrol.data.db.entity.relation.ProfileWithDateFrames
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val cashControlRepository: CashControlRepository
): ViewModel() {

    private var _allProfiles: Flow<List<Profile>> = cashControlRepository.profileLocal.getAllProfilesFromDb()
    val allProfiles: Flow<List<Profile>> get() = _allProfiles

    fun upsertProfile (profile: Profile) = viewModelScope.launch {
        cashControlRepository.profileLocal.upsertProfile(profile)
    }

    fun deleteProfile (profile: Profile) = viewModelScope.launch {
        cashControlRepository.profileLocal.deleteProfile(profile)
    }

    suspend fun getOnlineProfileById (userId: Int): Profile? {
        val profileList = cashControlRepository.profileLocal.getOnlineProfileById(userId)
        if (profileList.isNotEmpty()) {
            return profileList.first()
        }
        return null
    }

    suspend fun getProfileOfUserByName (userId: Int, profileName: String): Profile? {
        val profileList = cashControlRepository.profileLocal.getProfileOfUserByName(userId, profileName)
        if (profileList.isNotEmpty()) {
            return profileList.first()
        }
        return null
    }

    suspend fun getProfileWithDateFrames (profileId: Int): ProfileWithDateFrames? {
        val list = cashControlRepository.profileLocal.getProfileWithDateFrames(profileId)
        if (list.isNotEmpty()) {
            return list.first()
        }
        return null
    }

    fun changeOnlineProfileName(profile: Profile, newProfileName: String) {
        profile.profileName = newProfileName
        upsertProfile(profile)
    }

    fun setProfileOffline (profile: Profile) {
        profile.isOnline = false
        upsertProfile(profile)
    }

    fun setProfileOnline (profile: Profile) {
        profile.isOnline = true
        upsertProfile(profile)
    }
}