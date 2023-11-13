package com.example.cashcontrol.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cashcontrol.data.repository.CashControlRepository
import com.example.cashcontrol.data.entity.User
import com.example.cashcontrol.data.entity.relation.UserWithProfiles
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UserViewModel @Inject constructor(
    private val cashControlRepository: CashControlRepository
): ViewModel() {

    private var _allUsers: Flow<List<User>> = cashControlRepository.userLocal.getAllUsersFromDb()
    val allUsers: Flow<List<User>> get() = _allUsers

    private val _onlineUser: MutableStateFlow<User?> = MutableStateFlow(null)
    val onlineUser: StateFlow<User?> get() = _onlineUser

    private var _userWithProfiles: List<UserWithProfiles> = listOf()
    val userWithProfiles: List<UserWithProfiles> get() = _userWithProfiles

    init {
        viewModelScope.launch {
            onlineUser.collect {
                it?.let { onlineUser ->
                    onlineUser.userId?.let {
                        _userWithProfiles = cashControlRepository.userLocal.getUserWithProfiles(onlineUser.userId!!)
                    }
                }
            }
        }
    }

    fun upsertUser (user: User) = viewModelScope.launch {
        cashControlRepository.userLocal.upsertUser(user)
    }

    fun deleteUser (user: User) = viewModelScope.launch {
        cashControlRepository.userLocal.deleteUser(user)
    }

    suspend fun getUserWithProfilesOfOnlineUser(): List<UserWithProfiles> {
        return cashControlRepository.userLocal.getUserWithProfiles(onlineUser.value?.userId!!)
    }

    suspend fun getUserWithProfilesById(userId: Int): List<UserWithProfiles> {
        return cashControlRepository.userLocal.getUserWithProfiles(userId)
    }


    fun updateOnlineUser() = viewModelScope.launch {
        val allUsersList = allUsers.firstOrNull()
        allUsersList?.let {
            if (it.isNotEmpty()) {
                _onlineUser.value = it.find { u -> u.isOnline }
            }
        }
    }

    fun checkOnlineUser (foundOnlineUser: (User?) -> Unit) {
        viewModelScope.launch {
            val allUsersList = _allUsers.firstOrNull()
            allUsersList?.let {
                if (it.isNotEmpty()) {
                    val onlineUser = it.find { u -> u.isOnline }
                    foundOnlineUser (onlineUser)

                    onlineUser?.let {
                        _onlineUser.value = onlineUser
                    }
                } else {
                    foundOnlineUser (null)
                }
            }
        }
    }

    fun cacheNewExpenseCategory (expenseCategory: String) {
        _onlineUser.value?.let {
            it.cachedExpenseCategories.add(expenseCategory)
            upsertUser(it)
        }
    }

    fun cacheNewExpenseCategory (expenseCategoryList: List<String>) {
        _onlineUser.value?.let {
            it.cachedExpenseCategories.addAll(expenseCategoryList)
            upsertUser(it)
        }
    }

    fun cacheNewIncomeCategory(incomeCategory: String) {
        _onlineUser.value?.let {
            it.cachedIncomeCategories.add(incomeCategory)
        }
    }

    fun cacheNewIncomeCategory (incomeCategoryList: List<String>) {
        _onlineUser.value?.let {
            it.cachedIncomeCategories.addAll(incomeCategoryList)
        }
    }

    fun isUsernameAndPasswordAvailable (username: String, password: String, userList: List<User>): Boolean {
        val foundUser = userList.find { u -> u.username == username && u.password == password }
        foundUser?.let {
//            setUserOnline(it)
        }
        return foundUser != null
    }

    fun isUsernameValid (username: String, callback: (Boolean) -> Unit) = viewModelScope.launch{
        val allUsersList = _allUsers.firstOrNull()
        allUsersList?.let {
            if (it.isNotEmpty()) {
                val foundUser = it.find { u -> u.username == username }
                if (foundUser == null) {
                    callback(true)
                } else {
                    callback(false)
                }
            } else {
                callback(false)
            }
        }
    }

}