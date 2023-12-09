package com.example.cashcontrol.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.example.cashcontrol.util.constant.SettingsConstants.LANGUAGE_ENGLISH
import com.example.cashcontrol.util.constant.SettingsConstants.THEME_SYSTEM
import com.example.cashcontrol.util.extension.dataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingsDataStoreRepository @Inject constructor(@ApplicationContext context: Context) {

    private val datastore: DataStore<Preferences> = context.dataStore

    private object PreferenceKeys {
        val language = stringPreferencesKey("app_language")
        val theme = stringPreferencesKey("app_theme")
    }

    val languagePreferenceFlow: Flow<String> = datastore.data
        .map {
           it[PreferenceKeys.language] ?: LANGUAGE_ENGLISH
        }

    val themePreferenceFlow: Flow<String> = datastore.data
        .map {
            it[PreferenceKeys.theme] ?: THEME_SYSTEM
        }

    suspend fun updateAppLanguage (language: String) {
        datastore.edit {
            it[PreferenceKeys.language] = language
        }
    }

    suspend fun updateAppTheme (theme: String) {
        datastore.edit {
            it[PreferenceKeys.theme] = theme
        }
    }

}