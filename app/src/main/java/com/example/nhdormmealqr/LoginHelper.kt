package com.example.nhdormmealqr

import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class LoginHelper {
    companion object {
        private val LOGIN_ID_KEY = stringPreferencesKey("loginId")
        private val LOGIN_PASSWORD_KEY = stringPreferencesKey("loginPwd")

        suspend fun saveLoginInfo(id: String, password: String) {
            MyApplication.dataStore.edit { preferences ->
                preferences[LOGIN_ID_KEY] = id
                preferences[LOGIN_PASSWORD_KEY] = password
            }
        }

        fun getLoginInfo(): Flow<LoginInfo> {
            return MyApplication.dataStore.data.map { preferences ->
                val id = preferences[LOGIN_ID_KEY]
                val password = preferences[LOGIN_PASSWORD_KEY]
                LoginInfo(id, password)
            }
        }
    }
}