package com.example.SkyNote.viewmodel

data class AuthUiState(
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val errorMessage: String? = null,
    val message: String? = null
)
