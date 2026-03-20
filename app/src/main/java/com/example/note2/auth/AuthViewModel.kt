package com.example.note2.auth

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow


class AuthViewModel(
    private val repository: AuthRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()


    fun login(email: String, password: String) {
        if (email.isBlank()) {
            _uiState.value = AuthUiState(errorMessage = "Email không được để trống")
            return
        }
        if (password.isBlank()) {
            _uiState.value = AuthUiState(errorMessage = "Mật khẩu không được để trống")
            return
        }
        _uiState.value = AuthUiState(isLoading = true)
        repository.login(email, password) { success, error ->
            if (success) {
                _uiState.value = AuthUiState(isSuccess = true)
            } else {
                _uiState.value = AuthUiState(
                    isLoading = false,
                    errorMessage = error,
                    isSuccess = false
                )
            }
        }

    }

    fun register(email: String, password: String, confirmPassword: String) {
        if (email.isBlank()) {
            _uiState.value = AuthUiState(errorMessage = "Email không được để trống")
            return
        }
        if (password.isBlank()) {
            _uiState.value = AuthUiState(errorMessage = "Mật khẩu không được để trống")
            return

        }
        if (password != confirmPassword) {
            _uiState.value = AuthUiState(errorMessage = "Mật khẩu nhập lại không khớp")
            return
        }
        _uiState.value = AuthUiState(isLoading = true)
        repository.register(email, password) { success, error ->
            _uiState.value = if (success) {
                AuthUiState(isSuccess = true)
            } else {
                AuthUiState(errorMessage = error)
            }
        }
    }

    fun resetState() {
        _uiState.value = AuthUiState()


    }


}
