package com.example.SkyNote.ui.theme

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map


private val Context.dataStore by preferencesDataStore(name = "settings")
class ThemeManager(private val context: Context) {
    private val THEME_KEY = intPreferencesKey("theme_mode")
    private val AUTO_SYNC_KEY = booleanPreferencesKey("auto_sync")

    val themeModeFlow: Flow<Int> = context.dataStore.data.map { preferences ->
        preferences[THEME_KEY] ?: 0
    }

    val autoSyncFlow: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[AUTO_SYNC_KEY] ?: true
    }

    suspend fun setThemeMode(mode: Int) {
        context.dataStore.edit { preferences ->
            preferences[THEME_KEY] = mode
        }
    }

    suspend fun setAutoSyncEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[AUTO_SYNC_KEY] = enabled
        }
    }
}
