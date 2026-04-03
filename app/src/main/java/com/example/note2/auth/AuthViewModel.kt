package com.example.note2.auth

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.note2.data.NoteDao
import com.example.note2.model.NoteModel
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
                    noteDao.migrateGuestNotes(user.uid)

                    syncNotesToFirestore(user.uid, noteDao)
                    
                    _uiState.value = AuthUiState(isSuccess = true)
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
            val batch = firestore.batch()
            
            localNotes.forEach { note ->
                val docRef = firestore.collection("users").document(uid)
                    .collection("notes").document(note.id.toString())
                
                val noteData = hashMapOf(
                    "title" to note.title,
                    "description" to note.description,
                    "timestamp" to note.timestamp,
                    "color" to note.color,
                    "userId" to uid
                )
                batch.set(docRef, noteData)
            }
            
            batch.commit().await()
        } catch (e: Exception) {
        }
    }

    fun logout() {
        repository.logout()
        _currentUser.value = null
        _uiState.value = AuthUiState()
    }
}
