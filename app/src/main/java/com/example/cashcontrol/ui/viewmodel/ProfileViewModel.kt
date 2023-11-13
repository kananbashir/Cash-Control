package com.example.cashcontrol.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cashcontrol.data.repository.CashControlRepository
import com.example.cashcontrol.data.db.entity.Profile
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
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

    fun setProfileOffline (profile: Profile) {
        profile.isOnline = false
        upsertProfile(profile)
    }
}