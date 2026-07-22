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
    private val userNameKey = stringPreferencesKey("cached_user_name")
    private val userEmailKey = stringPreferencesKey("cached_user_email")
    private val userPhoneKey = stringPreferencesKey("cached_user_phone")
    private val userAddressKey = stringPreferencesKey("cached_user_address")

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

    /** Cache local minima del perfil (sin Room), para no depender de red en cada getUser(). */
    suspend fun cacheUserProfile(name: String, email: String, phone: String, address: String) {
        context.sessionDataStore.edit { prefs ->
            prefs[userNameKey] = name
            prefs[userEmailKey] = email
            prefs[userPhoneKey] = phone
            prefs[userAddressKey] = address
        }
    }

    suspend fun getCachedUserProfile(): CachedUserProfile? {
        val prefs = context.sessionDataStore.data.first()
        val name = prefs[userNameKey] ?: return null
        val email = prefs[userEmailKey] ?: return null
        return CachedUserProfile(
            name = name,
            email = email,
            phone = prefs[userPhoneKey].orEmpty(),
            address = prefs[userAddressKey].orEmpty()
        )
    }

    suspend fun clearSession() {
        context.sessionDataStore.edit { prefs -> prefs.clear() }
    }
}

data class CachedUserProfile(val name: String, val email: String, val phone: String, val address: String)
