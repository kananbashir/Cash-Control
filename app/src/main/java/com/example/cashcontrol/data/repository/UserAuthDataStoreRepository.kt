package com.example.cashcontrol.data.repository

import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import com.example.cashcontrol.R
import com.example.cashcontrol.util.extension.dataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserAuthDataStoreRepository @Inject constructor(@ApplicationContext private val context: Context){

    private val dataStore: DataStore<Preferences> = context.dataStore

    private object PreferenceKey {
        val userExists = booleanPreferencesKey("user_exists")
        val onlineUserExists = booleanPreferencesKey("online_user_exists")
        val profilesExists = booleanPreferencesKey("profiles_exists")
        val onlineProfileExists = booleanPreferencesKey("online_profiles_exists")
        val dateFramesExists = booleanPreferencesKey("date_frames_exists")
        val unfinishedDateFrameExists = booleanPreferencesKey("unfinished_date_frames_exists")

        val startDestinationId = intPreferencesKey("start_destination_id")
    }

    val startDestinationId: Flow<Int> = dataStore.data
        .map {
            it[PreferenceKey.startDestinationId] ?: R.id.login_session
        }

    suspend fun updateUserState (
        userExists: Boolean,
        onlineUserExists: Boolean,
        profilesExists: Boolean,
        onlineProfileExists: Boolean,
        dateFramesExists: Boolean,
        unfinishedDateFrameExists: Boolean
    ) {
        dataStore.edit {
            it[PreferenceKey.userExists] = userExists
            it[PreferenceKey.onlineUserExists] = onlineUserExists
            it[PreferenceKey.profilesExists] = profilesExists
            it[PreferenceKey.onlineProfileExists] = onlineProfileExists
            it[PreferenceKey.dateFramesExists] = dateFramesExists
            it[PreferenceKey.unfinishedDateFrameExists] = unfinishedDateFrameExists
        }
    }

    suspend fun updateUserState (state: Boolean) {
        dataStore.edit {
            it[PreferenceKey.userExists] = state
            Log.i("ActivityMain","Datastore -> updateUserState: $state")
        }
    }

    suspend fun updateUserOnlineState (state: Boolean) {
        dataStore.edit {
            it[PreferenceKey.onlineUserExists] = state
            Log.i("ActivityMain","Datastore -> updateUserOnlineState: $state")
        }
    }

    suspend fun updateUserProfilesState (state: Boolean) {
        dataStore.edit {
            it[PreferenceKey.profilesExists] = state
            Log.i("ActivityMain","Datastore -> updateUserProfilesState: $state")
        }
    }

    suspend fun updateProfileOnlineState (state: Boolean) {
        dataStore.edit {
            it[PreferenceKey.onlineProfileExists] = state
            Log.i("ActivityMain","Datastore -> updateProfileOnlineState: $state")
        }
    }

    suspend fun updateUserDateFrameState (state: Boolean) {
        dataStore.edit {
            it[PreferenceKey.dateFramesExists] = state
            Log.i("ActivityMain","Datastore -> updateUserDateFrameState: $state")
        }
    }

    suspend fun updateDateFrameFinishedState (state: Boolean) {
        dataStore.edit {
            it[PreferenceKey.unfinishedDateFrameExists] = state
            Log.i("ActivityMain","Datastore -> updateDateFrameFinishedState: $state")
        }
    }

    suspend fun updateStartDestination () {
        Log.i("ActivityMain","Datastore -> updateStartDestination()")
        dataStore.edit {
            val userExists = it[PreferenceKey.userExists] ?: false
            val onlineUserExists = it[PreferenceKey.onlineUserExists] ?: false
            val profilesExists = it[PreferenceKey.profilesExists] ?: false
            val onlineProfileExists = it[PreferenceKey.onlineProfileExists] ?: false
            val dateFramesExists = it[PreferenceKey.dateFramesExists] ?: false
            val unfinishedDateFrameExists = it[PreferenceKey.unfinishedDateFrameExists] ?: false

            when {
                !userExists -> { it[PreferenceKey.startDestinationId] = R.id.login_session }
                !onlineUserExists -> { it[PreferenceKey.startDestinationId] = R.id.login_session }
                !profilesExists -> { it[PreferenceKey.startDestinationId] = R.id.onBoardingProfileFragment }
                !onlineProfileExists -> { it[PreferenceKey.startDestinationId] = R.id.onBoardingProfileFragment }
                !dateFramesExists -> { it[PreferenceKey.startDestinationId] = R.id.onboarding_session }
                !unfinishedDateFrameExists -> { it[PreferenceKey.startDestinationId] = R.id.onboarding_session }
                else -> { it[PreferenceKey.startDestinationId] = R.id.main_session }
            }

            Log.i("ActivityMain","Datastore -> startDestinationId: ${it[PreferenceKey.startDestinationId]}")
        }
    }

}