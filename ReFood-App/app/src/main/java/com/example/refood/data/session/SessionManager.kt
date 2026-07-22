package com.example.refood.data.session

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.sessionDataStore by preferencesDataStore(name = "refood_session")

class SessionManager(private val context: Context) {

    private val userIdKey = longPreferencesKey("logged_in_user_id")
    private val accessTokenKey = stringPreferencesKey("access_token")
    private val refreshTokenKey = stringPreferencesKey("refresh_token")

    val currentUserId: Flow<Long?> = context.sessionDataStore.data.map { prefs ->
        prefs[userIdKey]?.takeIf { it > 0 }
    }

    suspend fun setLoggedInUser(userId: Long) {
        context.sessionDataStore.edit { prefs -> prefs[userIdKey] = userId }
    }

    suspend fun setTokens(accessToken: String, refreshToken: String) {
        context.sessionDataStore.edit { prefs ->
            prefs[accessTokenKey] = accessToken
            prefs[refreshTokenKey] = refreshToken
        }
    }

    suspend fun getAccessToken(): String? = context.sessionDataStore.data.first()[accessTokenKey]

    suspend fun getRefreshToken(): String? = context.sessionDataStore.data.first()[refreshTokenKey]

    suspend fun clearSession() {
        context.sessionDataStore.edit { prefs ->
            prefs.remove(userIdKey)
            prefs.remove(accessTokenKey)
            prefs.remove(refreshTokenKey)
        }
    }
}
