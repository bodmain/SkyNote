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
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout

enum class SyncState { IDLE, SYNCING, SUCCESS, ERROR }

class NoteViewModel(private val repository: NoteRepository) : ViewModel() {

    companion object {
        const val GUEST_USER_ID = "guest"
    }

    var notes by mutableStateOf<List<NoteModel>>(emptyList())
        private set

    var syncState by mutableStateOf(SyncState.IDLE)
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
    private var syncJob: Job? = null
    private var notificationsJob: Job? = null
    
    private val _notifications = MutableStateFlow<List<NotificationModel>>(emptyList())
    val notifications: StateFlow<List<NotificationModel>> = _notifications

    init {
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: GUEST_USER_ID
        startObservingData(currentUserId)
    }

    fun startObservingData(userId: String) {
        Log.d("SYNC", "Starting to observe data for user: $userId")
        startObservingNotes(userId)
        startObservingNotifications(userId)
        if (userId != GUEST_USER_ID) {
            startRealtimeSync(userId)
        } else {
            syncJob?.cancel()
        }
    }

    private fun startObservingNotes(userId: String) {
        notesJob?.cancel()
        notesJob = viewModelScope.launch {
            repository.getNotesByUser(userId).collectLatest { listOfNotes ->
                notes = listOfNotes
            }
        }
    }

    private fun startObservingNotifications(userId: String) {
        notificationsJob?.cancel()
        notificationsJob = viewModelScope.launch {
            val twentyFourHoursAgo = System.currentTimeMillis() - 86400000
            try {
                repository.deleteOldNotifications(twentyFourHoursAgo)
            } catch (e: Exception) {
                Log.e("DATABASE", "Error deleting old notifications: ${e.message}")
            }
            repository.getNotificationsByUser(userId).collectLatest { list ->
                _notifications.value = list
            }
        }
    }

    private fun startRealtimeSync(userId: String) {
        syncJob?.cancel()
        syncJob = viewModelScope.launch {
            repository.startRealtimeSync(userId).collectLatest { }
        }
    }

    fun syncAllNotes() {
        if (syncState != SyncState.IDLE) return

        val user = FirebaseAuth.getInstance().currentUser
        if (user == null) {
            syncState = SyncState.ERROR
            viewModelScope.launch {
                delay(2000)
                syncState = SyncState.IDLE
            }
            return
        }

        viewModelScope.launch {
            syncState = SyncState.SYNCING
            try {
                // Ensure all local notes have the correct userId before syncing
                val notesToSync = notes.map { it.copy(userId = user.uid) }
                Log.d("SYNC", "Starting manual sync for ${notesToSync.size} notes for user ${user.uid}")
                
                withTimeout(15000) {
                    repository.syncNotesToFirestore(notesToSync, user.uid)
                }
                syncState = SyncState.SUCCESS
            } catch (e: Exception) {
                Log.e("SYNC", "Manual sync failed or timed out: ${e.message}")
                syncState = SyncState.ERROR
            } finally {
                delay(2000)
                syncState = SyncState.IDLE
            }
        }
    }

    fun clearData() {
        notesJob?.cancel()
        syncJob?.cancel()
        notificationsJob?.cancel()
        notes = emptyList()
        _notifications.value = emptyList()
        syncState = SyncState.IDLE
    }

    fun addNote(title: String, description: String, color: Long = 0xFFFFFFFF, imagePath: String? = null) {
        val user = FirebaseAuth.getInstance().currentUser
        val userId = user?.uid ?: GUEST_USER_ID
        
        val newNote = NoteModel(
            title = title, 
            description = description, 
            color = color, 
            userId = userId,
            imagePath = imagePath
        )
        
        viewModelScope.launch {
            // Save locally first
            repository.insertNote(newNote)
            
            // Sync to Firestore if logged in
            if (userId != GUEST_USER_ID) {
                try {
                    repository.syncNoteToFirestore(newNote)
                    Log.d("SYNC", "Auto-synced new note to Firestore")
                } catch (e: Exception) {
                    Log.e("SYNC", "Auto sync failed for new note: ${e.message}")
                }
            }
        }
    }

    fun updateNote(note: NoteModel) {
        val user = FirebaseAuth.getInstance().currentUser
        val userId = user?.uid ?: GUEST_USER_ID
        
        val updatedNote = note.copy(
            timestamp = System.currentTimeMillis(),
            userId = userId // Ensure userId is correct
        )
        
        viewModelScope.launch {
            repository.updateNote(updatedNote)
            if (userId != GUEST_USER_ID) {
                try {
                    repository.syncNoteToFirestore(updatedNote)
                } catch (e: Exception) {
                    Log.e("SYNC", "Auto sync failed for updated note")
                }
            }
        }
    }

    fun deleteNote(note: NoteModel) {
        val user = FirebaseAuth.getInstance().currentUser
        val userId = user?.uid ?: GUEST_USER_ID

        viewModelScope.launch {
            repository.deleteNote(note)
            if (userId != GUEST_USER_ID) {
                try {
                    repository.deleteNoteFromFirestore(note.id, userId)
                } catch (e: Exception) {
                    Log.e("SYNC", "Auto sync failed for deleted note")
                }
            }
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

    fun toggleSearch() {
        isSearchActive = !isSearchActive
        if (!isSearchActive) {
            searchText = ""
            searchResults = emptyList()
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
}
