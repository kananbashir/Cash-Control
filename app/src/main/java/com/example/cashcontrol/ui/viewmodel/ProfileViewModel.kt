package com.example.cashcontrol.ui.viewmodel

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cashcontrol.data.repository.CashControlRepository
import com.example.cashcontrol.data.entity.Profile
import com.example.cashcontrol.data.entity.User
import com.example.cashcontrol.data.entity.relation.ProfileWithDateFrames
import com.example.cashcontrol.data.entity.relation.UserWithProfiles
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val cashControlRepository: CashControlRepository
): ViewModel() {

    private var _allProfiles: Flow<List<Profile>> = cashControlRepository.profileLocal.getAllProfilesFromDb()
    val allProfiles: Flow<List<Profile>> get() = _allProfiles

    private var _onlineProfile: MutableStateFlow<Profile?> = MutableStateFlow(null)
    val onlineProfile: StateFlow<Profile?> get() = _onlineProfile

    private var _profileWithDateFrames: List<ProfileWithDateFrames> = listOf()
    val profileWithDateFrames: List<ProfileWithDateFrames> get() = _profileWithDateFrames

    init {
        viewModelScope.launch {
            onlineProfile.collect {
                it?.let { onlineProfile ->
                    _profileWithDateFrames = cashControlRepository.profileLocal.getProfileWithDateFrames(onlineProfile.profileId!!)
                }
            }
        }
    }

    fun upsertProfile (profile: Profile) = viewModelScope.launch {
        cashControlRepository.profileLocal.upsertProfile(profile)
    }

    fun deleteProfile (profile: Profile) = viewModelScope.launch {
        cashControlRepository.profileLocal.deleteProfile(profile)
    }

    fun checkOnlineProfile (userWithProfiles: UserWithProfiles, foundOnlineProfile: (Profile?) -> Unit) {
        val onlineProfile = userWithProfiles.profiles.find { p -> p.isOnline }
        onlineProfile?.let {
            _onlineProfile.value = onlineProfile
        }
        foundOnlineProfile(onlineProfile)
    }

    fun updateOnlineProfile (userWithProfiles: UserWithProfiles) {
        val onlineProfile = userWithProfiles.profiles.find { p -> p.isOnline }
        _onlineProfile.value = onlineProfile
    }

    suspend fun updateOnlineProfile (onlineUserId: Int) {
        val allProfilesList = allProfiles.firstOrNull()
        allProfilesList?.let {
            val foundProfile = it.find { p -> p.isOnline && p.userId == onlineUserId }
        }
    }

    fun setProfileOffline (profile: Profile) {
        profile.isOnline = false
        upsertProfile(profile)
    }

    fun checkProfileName (profileName: String, userWithProfiles: UserWithProfiles): Boolean {
        val foundProfile = userWithProfiles.profiles.find { it.profileName == profileName }
        return if (foundProfile == null) {
            upsertProfile(Profile(profileName, true, userWithProfiles.user.userId!!))
            true
        } else {
            false
        }
    }
}