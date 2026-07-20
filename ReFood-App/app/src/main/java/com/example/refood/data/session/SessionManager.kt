package com.example.refood.data.session

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.sessionDataStore by preferencesDataStore(name = "refood_session")

class SessionManager(private val context: Context) {

    private val userIdKey = longPreferencesKey("logged_in_user_id")

    val currentUserId: Flow<Long?> = context.sessionDataStore.data.map { prefs ->
        prefs[userIdKey]?.takeIf { it > 0 }
    }

    suspend fun setLoggedInUser(userId: Long) {
        context.sessionDataStore.edit { prefs -> prefs[userIdKey] = userId }
    }

    suspend fun clearSession() {
        context.sessionDataStore.edit { prefs -> prefs.remove(userIdKey) }
    }
}
