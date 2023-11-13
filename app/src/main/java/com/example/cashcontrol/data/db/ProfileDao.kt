package com.example.cashcontrol.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.cashcontrol.data.entity.Profile
import com.example.cashcontrol.data.entity.relation.ProfileWithDateFrames
import kotlinx.coroutines.flow.Flow

@Dao
interface ProfileDao {

    @Insert (onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertProfile (profile: Profile)

    @Delete
    suspend fun deleteProfile (profile: Profile)

    @Query ("SELECT * FROM profile_table")
    fun getAllProfilesFromDb (): Flow<List<Profile>>

    @Transaction
    @Query ("SELECT * FROM profile_table WHERE isOnline = 1")
    suspend fun getOnlineProfile (): List<Profile>

    @Transaction
    @Query ("SELECT * FROM profile_table WHERE profileId = :profileId")
    suspend fun getProfileWithDateFrames (profileId: Int): List<ProfileWithDateFrames>

}