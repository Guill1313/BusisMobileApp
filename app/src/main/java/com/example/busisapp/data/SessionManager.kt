package com.example.busisapp.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import android.util.Base64
import com.google.gson.Gson

// Create the DataStore instance
val Context.dataStore by preferencesDataStore(name = "user_session")

/**
 * Class that handles saving and retrieving user data from DataStore.
 *
 * @param context The application context.
 */
class SessionManager(private val context: Context) {

    /**
     * Companion object containing keys for saving and retrieving data from DataStore.
     */
    companion object {
        val TOKEN_KEY = stringPreferencesKey("jwt_token")
        val LAST_ACTIVE_KEY = longPreferencesKey("last_active_time")
        const val EXPIRATION_TIME_MS = 10 * 60 * 1000L // 10 minutes in milliseconds
    }

    /**
     * Saves a token to DataStore.
     *
     * @param token The token to save.
     */
    suspend fun saveToken(token: String) {
        context.dataStore.edit { prefs ->
            prefs[TOKEN_KEY] = token
            prefs[LAST_ACTIVE_KEY] = System.currentTimeMillis()
        }
    }

    /**
     * Retrieves the token from DataStore.
     *
     * @return The token as a string, or null if not found.
     */
    suspend fun getToken(): String? {
        val prefs = context.dataStore.data.first()
        return prefs[TOKEN_KEY]
    }

    /**
     * Updates the last active time in DataStore.
     */
    suspend fun updateActivityTimestamp() {
        context.dataStore.edit { prefs ->
            // Only update if we actually logged in
            if (prefs[TOKEN_KEY] != null) {
                prefs[LAST_ACTIVE_KEY] = System.currentTimeMillis()
            }
        }
    }

    /**
     * Clears the token and last active time from DataStore.
     */
    suspend fun clearSession() {
        context.dataStore.edit { prefs ->
            prefs.remove(TOKEN_KEY)
            prefs.remove(LAST_ACTIVE_KEY)
        }
    }

    /**
     * Checks if the session is valid based on the token and last active time.
     *
     * @return True if the session is valid, false otherwise.
     */
    suspend fun isSessionValid(): Boolean {
        val prefs = context.dataStore.data.first()
        val token = prefs[TOKEN_KEY]
        val lastActiveTime = prefs[LAST_ACTIVE_KEY] ?: 0L

        if (token == null) return false

        val currentTime = System.currentTimeMillis()
        val timePassed = currentTime - lastActiveTime

        return if (timePassed > EXPIRATION_TIME_MS) {
            clearSession()
            false
        } else {
            true
        }
    }

    /**
     * Retrieves the user data from the token encoded in Base64 into a UserDto object.
     *
     * @return The user data as a UserDto object, or null if not found.
     */
    suspend fun getUserData(): UserDto? {
        val token = getToken() ?: return null
        return try {
            val jsonString = String(Base64.decode(token, Base64.DEFAULT))
            Gson().fromJson(jsonString, UserDto::class.java)
        } catch (_: Exception) {
            null
        }
    }
}