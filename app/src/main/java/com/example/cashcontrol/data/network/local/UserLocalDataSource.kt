package com.example.cashcontrol.data.network.local

import com.example.cashcontrol.data.db.UserDao
import com.example.cashcontrol.data.db.entity.User
import com.example.cashcontrol.data.db.entity.relation.UserWithProfiles
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class UserLocalDataSource @Inject constructor(private val userDao: UserDao) {

    suspend fun upsertUser (user: User) {
        userDao.upsertUser(user)
    }

    fun getAllUsersFromDb (): Flow<List<User>> {
        return userDao.getAllUsersFromDb()
    }

    suspend fun getOnlineUser (): List<User> {
        return userDao.getOnlineUser()
    }

    suspend fun getUserWithProfiles (userId: Int): List<UserWithProfiles> {
        return userDao.getUserWithProfiles(userId)
    }

    suspend fun getUserByName (username: String): List<User> {
        return userDao.getUserByName(username)
    }

    suspend fun getUserByNameAndPassword (username: String, password: String): List<User> {
        return userDao.getUserByNameAndPassword(username, password)
    }

}