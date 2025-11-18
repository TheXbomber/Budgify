package com.example.budgify.applicationlogic

import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.budgify.userpreferences.AppTheme
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

// Define a top-level property for the DataStore instance, tied to the Context.
// The name "user_gamification_prefs" will be the filename of the DataStore.
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_gamification_prefs")

class UserPreferencesRepository(private val context: Context) {

    private object PreferencesKeys {
        val USER_LEVEL = intPreferencesKey("user_level")
        val USER_XP = intPreferencesKey("user_xp")
        val UNLOCKED_THEMES = stringSetPreferencesKey("unlocked_themes")
    }

    val userLevel: Flow<Int> = context.dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.USER_LEVEL] ?: 1 // Default to level 1
        }

    val userXp: Flow<Int> = context.dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.USER_XP] ?: 0 // Default to 0 XP
        }

    suspend fun updateUserLevelAndXp(level: Int, xp: Int) {
        Log.d("XP_DEBUG_DATASTORE", "DataStore: Updating level to $level, XP to $xp")
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.USER_LEVEL] = level
            preferences[PreferencesKeys.USER_XP] = xp
        }
    }

    suspend fun resetUserLevelAndXpToDefault() { // New function for level/XP reset
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.USER_LEVEL] = 1 // Default level
            preferences[PreferencesKeys.USER_XP] = 0   // Default XP
        }
    }

    suspend fun getInitialUserLevel(): Int {
        return userLevel.first()
    }

    suspend fun getInitialUserXp(): Int {
        return userXp.first()
    }

    // Flow to observe unlocked themes
    val unlockedThemes: Flow<Set<String>> = context.dataStore.data
        .map { preferences ->
            // Default to a set containing themes unlocked at level 1
            preferences[PreferencesKeys.UNLOCKED_THEMES] ?: AppTheme.entries.filter { it.unlockLevel <= 1 }.map { it.name }.toSet()
        }

    // Function to add a newly unlocked theme
    suspend fun addUnlockedTheme(themeName: String) {
        context.dataStore.edit { preferences ->
            val currentUnlocked = preferences[PreferencesKeys.UNLOCKED_THEMES] ?: emptySet()
            preferences[PreferencesKeys.UNLOCKED_THEMES] = currentUnlocked + themeName
            Log.d("ThemeUnlock", "Unlocked theme: $themeName. All unlocked: ${preferences[PreferencesKeys.UNLOCKED_THEMES]}")
        }
    }

    suspend fun resetUnlockedThemesToDefault() { // New function for theme reset
        context.dataStore.edit { preferences ->
            // Reset to only themes unlockable at level 1
            preferences[PreferencesKeys.UNLOCKED_THEMES] = AppTheme.entries
                .filter { it.unlockLevel <= 1 }
                .map { it.name }
                .toSet()
        }
    }
}