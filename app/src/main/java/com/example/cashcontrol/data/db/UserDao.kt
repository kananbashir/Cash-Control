package com.example.cashcontrol.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.cashcontrol.data.entity.User
import com.example.cashcontrol.data.entity.relation.UserWithProfiles
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {

    @Insert (onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertUser (user: User)

    @Delete
    suspend fun deleteUser (user: User)

    @Query ("SELECT * FROM user_table")
    fun getAllUsersFromDb (): Flow<List<User>>

    @Transaction
    @Query ("SELECT * FROM user_table WHERE isOnline = 1")
    suspend fun getOnlineUser (): List<User>

    @Transaction
    @Query ("SELECT * FROM user_table WHERE userId = :userId")
    suspend fun getUserWithProfiles (userId: Int): List<UserWithProfiles>

}