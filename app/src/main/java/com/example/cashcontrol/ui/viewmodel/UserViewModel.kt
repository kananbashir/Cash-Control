package com.example.cashcontrol.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cashcontrol.data.repository.CashControlRepository
import com.example.cashcontrol.data.db.entity.User
import com.example.cashcontrol.data.db.entity.relation.UserWithProfiles
import com.example.cashcontrol.data.repository.SettingsDataStoreRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UserViewModel @Inject constructor(
    private val cashControlRepository: CashControlRepository,
    private val settingsDataStoreRepository: SettingsDataStoreRepository
): ViewModel() {

    private var _allUsers: Flow<List<User>> = cashControlRepository.userLocal.getAllUsersFromDb()
    val allUsers: Flow<List<User>> get() = _allUsers

    val languagePreferenceFlow: Flow<String> = settingsDataStoreRepository.languagePreferenceFlow
    val themePreferenceFlow: Flow<String> = settingsDataStoreRepository.themePreferenceFlow

    var cachedExpenseCategories: Set<String> = setOf()
    var cachedIncomeSources: Set<String> = setOf()

    fun upsertUser (user: User) = viewModelScope.launch {
        cashControlRepository.userLocal.upsertUser(user)
    }

    suspend fun getUserWithProfiles(userId: Int): UserWithProfiles? {
        val list = cashControlRepository.userLocal.getUserWithProfiles(userId)
        if (list.isNotEmpty()) {
            return list.first()
        }
        return null
    }

    suspend fun getOnlineUser (): User? {
        val userList = cashControlRepository.userLocal.getOnlineUser()
        if (userList.isNotEmpty()) {
            return userList.first()
        }
        return null
    }

    private suspend fun getUserByName (username: String): User? {
        val userList = cashControlRepository.userLocal.getUserByName(username)
        if (userList.isNotEmpty()) {
            return userList.first()
        }
        return null
    }

    suspend fun getUserByNameAndPassword (username: String, password: String): User? {
        val userList = cashControlRepository.userLocal.getUserByNameAndPassword(username, password)
        if (userList.isNotEmpty()) {
            return userList.first()
        }
        return null
    }

    suspend fun isUsernameValid (username: String): Boolean {
        return getUserByName(username) == null
    }

    fun updateCachedCategories(onlineUser: User) {
        cachedExpenseCategories = onlineUser.cachedExpenseCategories
        cachedIncomeSources = onlineUser.cachedIncomeCategories
    }

    suspend fun cacheNewExpenseCategory (expenseCategory: String) {
        getOnlineUser()?.let {
            it.cachedExpenseCategories.add(expenseCategory)
            upsertUser(it)
        }
    }

    suspend fun cacheNewExpenseCategory(expenseCategoryList: List<String>) {
        getOnlineUser()?.let {
            it.cachedExpenseCategories.addAll(expenseCategoryList)
            upsertUser(it)
        }
    }

    suspend fun cacheNewIncomeSource(incomeSource: String) {
        getOnlineUser()?.let {
            it.cachedIncomeCategories.add(incomeSource)
            upsertUser(it)
        }
    }

    suspend fun cacheNewIncomeSource(incomeSourceList: List<String>) {
        getOnlineUser()?.let {
            it.cachedIncomeCategories.addAll(incomeSourceList)
            upsertUser(it)
        }
    }

    suspend fun clearAllCategoriesAndSourcesForUser () {
        getOnlineUser()?.let {
            it.cachedIncomeCategories.clear()
            it.cachedExpenseCategories.clear()
            upsertUser(it)
        }
    }

    fun setUserOffline (user: User) {
        user.isOnline = false
        upsertUser(user)
    }

    fun setUserOnline (user: User) {
        user.isOnline = true
        upsertUser(user)
    }

    fun updateAppTheme (theme: String) = viewModelScope.launch {
        settingsDataStoreRepository.updateAppTheme(theme)
    }

    fun updateAppLanguage (languageCode: String) = viewModelScope.launch {
        settingsDataStoreRepository.updateAppLanguage(languageCode)
    }

}