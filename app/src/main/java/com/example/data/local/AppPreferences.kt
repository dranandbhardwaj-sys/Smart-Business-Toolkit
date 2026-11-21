package com.example.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "smart_biz_prefs")

data class SessionPreferences(
    val isLoggedIn: Boolean = false,
    val isOnboarded: Boolean = false,
    val darkMode: Boolean = false,
    val language: String = "en", // "en" or "hi"
    val activeRole: String = "merchant", // "merchant" or "admin"
    val userEmail: String = "rajesh@sharmatraders.in",
)

class AppPreferences(private val context: Context) {
    private object Keys {
        val IS_LOGGED_IN = booleanPreferencesKey("is_logged_in")
        val IS_ONBOARDED = booleanPreferencesKey("is_onboarded")
        val DARK_MODE = booleanPreferencesKey("dark_mode")
        val LANGUAGE = stringPreferencesKey("language")
        val ACTIVE_ROLE = stringPreferencesKey("active_role")
        val USER_EMAIL = stringPreferencesKey("user_email")
    }

    val sessionFlow: Flow<SessionPreferences> = context.dataStore.data.map { prefs ->
        SessionPreferences(
            isLoggedIn = prefs[Keys.IS_LOGGED_IN] ?: false,
            isOnboarded = prefs[Keys.IS_ONBOARDED] ?: false,
            darkMode = prefs[Keys.DARK_MODE] ?: false,
            language = prefs[Keys.LANGUAGE] ?: "en",
            activeRole = prefs[Keys.ACTIVE_ROLE] ?: "merchant",
            userEmail = prefs[Keys.USER_EMAIL] ?: "rajesh@sharmatraders.in",
        )
    }

    suspend fun setLoggedIn(loggedIn: Boolean, email: String? = null) {
        context.dataStore.edit { prefs ->
            prefs[Keys.IS_LOGGED_IN] = loggedIn
            if (email != null) prefs[Keys.USER_EMAIL] = email
        }
    }

    suspend fun setOnboarded(onboarded: Boolean) {
        context.dataStore.edit { it[Keys.IS_ONBOARDED] = onboarded }
    }

    suspend fun setDarkMode(enabled: Boolean) {
        context.dataStore.edit { it[Keys.DARK_MODE] = enabled }
    }

    suspend fun setLanguage(language: String) {
        context.dataStore.edit { it[Keys.LANGUAGE] = language }
    }

    suspend fun setActiveRole(role: String) {
        context.dataStore.edit { it[Keys.ACTIVE_ROLE] = role }
    }

    suspend fun clearSession() {
        context.dataStore.edit { prefs ->
            prefs[Keys.IS_LOGGED_IN] = false
        }
    }
}
