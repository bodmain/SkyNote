package com.example.note2.viewmodel

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.note2.data.NoteRepository
import com.example.note2.model.NoteModel
import com.example.note2.model.NotificationModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class NoteViewModel(private val repository: NoteRepository) : ViewModel() {

    companion object {
        const val GUEST_USER_ID = "guest"
    }

    var notes by mutableStateOf<List<NoteModel>>(emptyList())
        private set
    var isSearchActive by mutableStateOf(false)
        private set
    var searchText by mutableStateOf("")
        private set
    var searchResults by mutableStateOf<List<NoteModel>>(emptyList())
        private set
    var noteToDelete by mutableStateOf<NoteModel?>(null)
        private set

    private var notesJob: Job? = null
    private var notificationsJob: Job? = null
    private val _notifications = MutableStateFlow<List<NotificationModel>>(emptyList())
    val notifications: StateFlow<List<NotificationModel>> = _notifications

    private val firestore = FirebaseFirestore.getInstance()

    init {
        startObservingNotes()
        startObservingNotifications()
    }

    fun startObservingNotes() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: GUEST_USER_ID
        notesJob?.cancel()
        notesJob = viewModelScope.launch {
            repository.getNotesByUser(userId).collectLatest { listOfNotes ->
                notes = listOfNotes
            }
        }
    }

    fun startObservingNotifications() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: GUEST_USER_ID
        notificationsJob?.cancel()
        notificationsJob = viewModelScope.launch {
            repository.getNotificationsByUser(userId).collectLatest { list ->
                _notifications.value = list
            }
        }
    }

    fun syncAllNotes() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        viewModelScope.launch {
            try {
                val localNotes = notes
                val batch = firestore.batch()
                
                localNotes.forEach { note ->
                    val docRef = firestore.collection("users").document(userId)
                        .collection("notes").document(note.id.toString())
                    
                    val noteData = hashMapOf(
                        "title" to note.title,
                        "description" to note.description,
                        "timestamp" to note.timestamp,
                        "color" to note.color,
                        "userId" to userId
                    )
                    batch.set(docRef, noteData)
                }
                batch.commit().await()
                Log.d("SYNC", "Đồng bộ thành công ${localNotes.size} ghi chú")
            } catch (e: Exception) {
                Log.e("SYNC", "Lỗi đồng bộ: ${e.message}")
            }
        }
    }

    fun clearData() {
        notesJob?.cancel()
        notificationsJob?.cancel()
        notes = emptyList()
        _notifications.value = emptyList()
        searchText = ""
        isSearchActive = false
        startObservingNotes()
        startObservingNotifications()
    }

    fun addNote(title: String, description: String, color: Long = 0xFFFFFFFF) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: GUEST_USER_ID
        viewModelScope.launch {
            val note = NoteModel(title = title, description = description, color = color, userId = userId)
            repository.insertNote(note)
            if (userId != GUEST_USER_ID) {
                syncNoteToFirestore(userId, note)
            }
        }
    }

    private suspend fun syncNoteToFirestore(uid: String, note: NoteModel) {
        try {
            val noteData = hashMapOf(
                "title" to note.title,
                "description" to note.description,
                "timestamp" to note.timestamp,
                "color" to note.color,
                "userId" to uid
            )
            firestore.collection("users").document(uid)
                .collection("notes").document(note.id.toString())
                .set(noteData).await()
        } catch (e: Exception) {
            Log.e("SYNC", "Error syncing note: ${e.message}")
        }
    }

    fun deleteNote(note: NoteModel) = viewModelScope.launch { 
        repository.deleteNote(note)
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            firestore.collection("users").document(userId)
                .collection("notes").document(note.id.toString())
                .delete().await()
        }
    }
    
    fun updateNote(note: NoteModel) = viewModelScope.launch { 
        repository.updateNote(note)
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            syncNoteToFirestore(userId, note)
        }
    }

    fun toggleSearch() {
        isSearchActive = !isSearchActive
        if (!isSearchActive) {
            searchText = ""
            searchResults = emptyList()
        }
    }

    fun filterNotes(query: String) {
        searchText = query
        searchResults = if (query.isEmpty()) {
            emptyList()
        } else {
            notes.filter {
                it.title.contains(query, ignoreCase = true) ||
                it.description.contains(query, ignoreCase = true)
            }
        }
    }

    fun showDeleteDialog(note: NoteModel) {
        noteToDelete = note
    }

    fun dismissDeleteDialog() {
        noteToDelete = null
    }

    fun confirmDelete() {
        noteToDelete?.let { note ->
            deleteNote(note)
        }
        noteToDelete = null
    }

    fun markNotificationAsRead(notification: NotificationModel) {
        viewModelScope.launch { repository.updateNotification(notification.copy(isRead = true)) }
    }

    fun markAllNotificationsAsRead() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: GUEST_USER_ID
        viewModelScope.launch { repository.markAllAsRead(userId) }
    }

    fun deleteNotification(notification: NotificationModel) {
        viewModelScope.launch { repository.deleteNotification(notification) }
    }

    fun deleteAllNotifications() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: GUEST_USER_ID
        viewModelScope.launch { repository.deleteAllNotifications(userId) }
    }
}
