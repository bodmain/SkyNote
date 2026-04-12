package com.example.note2.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.note2.data.local.NoteDao
import com.example.note2.data.repository.AuthRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class AuthViewModel(
    private val repository: AuthRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    private val _currentUser = MutableStateFlow<FirebaseUser?>(null)
    val currentUser: StateFlow<FirebaseUser?> = _currentUser

    private val firestore = FirebaseFirestore.getInstance()

    init {
        _currentUser.value = FirebaseAuth.getInstance().currentUser
    }

    fun signInWithGoogle(context: Context, noteDao: NoteDao) {
        _uiState.value = AuthUiState(isLoading = true)
        viewModelScope.launch {
            val result = repository.signInWithGoogle(context)
            if (result.isSuccess) {
                val user = FirebaseAuth.getInstance().currentUser
                _currentUser.value = user
                
                if (user != null) {
                    try {
                        noteDao.migrateGuestNotes(user.uid)
                        syncNotesToFirestore(user.uid, noteDao)
                        _uiState.value = AuthUiState(isSuccess = true)
                    } catch (e: Exception) {
                        Log.e("AUTH", "Error after sign in: ${e.message}")
                        _uiState.value = AuthUiState(
                            isLoading = false,
                            errorMessage = "Đăng nhập thành công nhưng lỗi đồng bộ: ${e.message}",
                            isSuccess = true
                        )
                    }
                }
            } else {
                _uiState.value = AuthUiState(
                    isLoading = false,
                    errorMessage = result.exceptionOrNull()?.message ?: "Đăng nhập thất bại",
                    isSuccess = false
                )
            }
        }
    }

    private suspend fun syncNotesToFirestore(uid: String, noteDao: NoteDao) {
        try {
            val localNotes = noteDao.getNotesByUserList(uid)
            if (localNotes.isEmpty()) return

            localNotes.chunked(500).forEach { chunk ->
                val batch = firestore.batch()
                chunk.forEach { note ->
                    val docRef = firestore.collection("users").document(uid)
                        .collection("notes").document(note.id)
                    batch.set(docRef, note)
                }
                batch.commit().await()
            }
            Log.d("AUTH", "Successfully synced ${localNotes.size} notes to Firestore")
        } catch (e: Exception) {
            Log.e("AUTH", "Error syncing notes to firestore: ${e.message}")
            throw e
        }
    }

    fun logout() {
        repository.logout()
        _currentUser.value = null
        _uiState.value = AuthUiState()
    }
}
