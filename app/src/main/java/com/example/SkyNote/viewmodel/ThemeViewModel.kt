package com.example.SkyNote.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.SkyNote.ui.theme.ThemeManager
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ThemeViewModel(private val themeManager: ThemeManager): ViewModel() {

    val themeMode: StateFlow<Int> = themeManager.themeModeFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 0
    )

    val autoSyncEnabled: StateFlow<Boolean> = themeManager.autoSyncFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = true
    )

    fun toggleTheme(isDark: Boolean) {
        viewModelScope.launch {
            val mode = if (isDark) 2 else 1
            themeManager.setThemeMode(mode)
        }
    }

    fun toggleAutoSync(enabled: Boolean) {
        viewModelScope.launch {
            themeManager.setAutoSyncEnabled(enabled)
        }
    }
}
