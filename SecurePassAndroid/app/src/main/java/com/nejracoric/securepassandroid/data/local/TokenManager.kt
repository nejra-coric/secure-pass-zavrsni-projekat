package com.nejracoric.securepassandroid.data.local

import android.content.Context
import com.nejracoric.securepassandroid.data.api.TokenHolder
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "securepass_prefs")

class TokenManager(private val context: Context) {

    companion object {
        private val TOKEN_KEY = stringPreferencesKey("auth_token")
        private val EMAIL_KEY = stringPreferencesKey("user_email")
        private val USER_ID_KEY = stringPreferencesKey("user_id")
    }

    val tokenFlow: Flow<String?> = context.dataStore.data.map { prefs ->
        prefs[TOKEN_KEY]
    }

    val emailFlow: Flow<String?> = context.dataStore.data.map { prefs ->
        prefs[EMAIL_KEY]
    }

    suspend fun getToken(): String? {
        val token = context.dataStore.data.first()[TOKEN_KEY]
        TokenHolder.token = token
        return token
    }

    suspend fun getEmail(): String? = context.dataStore.data.first()[EMAIL_KEY]

    suspend fun saveSession(token: String, userId: String, email: String) {
        TokenHolder.token = token
        context.dataStore.edit { prefs ->
            prefs[TOKEN_KEY] = token
            prefs[USER_ID_KEY] = userId
            prefs[EMAIL_KEY] = email
        }
    }

    suspend fun clearSession() {
        TokenHolder.token = null
        context.dataStore.edit { prefs ->
            prefs.remove(TOKEN_KEY)
            prefs.remove(USER_ID_KEY)
            prefs.remove(EMAIL_KEY)
        }
    }

    suspend fun hasSession(): Boolean = !getToken().isNullOrBlank()
}
