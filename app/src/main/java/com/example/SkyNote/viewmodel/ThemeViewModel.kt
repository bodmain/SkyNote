package com.example.SkyNote.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.SkyNote.ui.theme.ThemeManager
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ThemeViewModel(private val themeManager: ThemeManager): ViewModel() {

    val themeMode = themeManager.themeModeFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 0
    )

    fun toggleTheme(isDark: Boolean) {
        viewModelScope.launch {
            val mode = if (isDark) 2 else 1
            themeManager.setThemeMode(mode)
        }
    }
}